import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ServerListener extends Thread implements ServiceHandler{
    private static final int port = 3000;
    ServerSocketChannel serverSocketChannel;
    Selector selector;
    private static Node node;
    private static Node leaderNode;

    private final static Cluster cluster = Cluster.getInstance();
    private static FailureDetector failureDetector;

    //For Election
    private static boolean electionResponseArrived = false;
    private static boolean leaderElected = false;
    private static boolean isElectionRunning = false;

    public ServerListener(String nodeHostAddress, int priority) throws IOException{
        this.node = new Node(priority, InetAddress.getLocalHost().getHostAddress());

        this.init();
        JoinNodeRequestMessage joinNodeRequestMessage = new JoinNodeRequestMessage(this.node);
        try {
            Logger.logMsg("Sending JOIN_REQUEST");
            SocketChannel clientChannel = send(nodeHostAddress, this.port, joinNodeRequestMessage);
            clientChannel.register(this.selector, SelectionKey.OP_READ);

        }catch(Exception ex){
            Logger.logError("Unable to send joinNodeRequestMessage");
        }

    }

    public ServerListener(int priority) throws  IOException{
        this.node = new Node(priority, InetAddress.getLocalHost().getHostAddress());
        this.init();
    }

    public ServerListener() throws IOException {
        this.node = new Node(1, InetAddress.getLocalHost().getHostAddress());
        this.node = new Node(1, InetAddress.getLocalHost().getHostAddress());
        this.init();
    }

    public void init() throws IOException{
        this.selector = Selector.open();
        Logger.logMsg("Selector is ready to make the connection: " + this.selector.isOpen());

        this.serverSocketChannel = ServerSocketChannel.open();
        InetSocketAddress hostAddress = new InetSocketAddress(this.port);

        this.serverSocketChannel.bind(hostAddress);
        this.serverSocketChannel.configureBlocking(false);

        //Create node on which server is running & add that node into cluster
        this.cluster.addNode(this.node);

        int ops = serverSocketChannel.validOps();
        SelectionKey selectionKey = this.serverSocketChannel.register(selector, ops);
    }

    @Override
    public void run(){
        while(true){
            try {
                this.selector.select();

                Set<SelectionKey> selectedKey = this.selector.selectedKeys();
                Iterator<SelectionKey> itr = selectedKey.iterator();
                while(itr.hasNext()){
                    SelectionKey key = itr.next();
                    if(key.isValid() && key.isAcceptable()){
                        SocketChannel clientChannel = this.serverSocketChannel.accept();

                        clientChannel.configureBlocking(false);
                        clientChannel.register(this.selector, SelectionKey.OP_READ);

                        Socket socket = clientChannel.socket();
                        socket.setTcpNoDelay(true);
                        InetAddress ip = socket.getInetAddress();
                        Logger.logMsg("New connection: " + ip.getHostName());

                    }else if(key.isValid() && key.isReadable()){

                        SocketChannel clientChannel = (SocketChannel)key.channel();
                        clientChannel.configureBlocking(false);
                        byte[] payload = read(clientChannel);
                        if(payload.length > 0){

                            Object obj = Util.deserialize(payload);
                            if(obj instanceof Request){
                                this.handleRequest((Request) obj, clientChannel);
                            }

                            if(obj instanceof Response){
                                this.handleResponse((Response) obj, clientChannel);
                            }

                        }

                    }

                    itr.remove();

                }

            } catch (Exception e) {
                Logger.logError("Encountered problem, exiting service loop");
                e.printStackTrace();
                System.exit(1);
            }

        }
    }

    public static SocketChannel send(String host, int port, Message msg) throws Exception{

        InetSocketAddress socketAddress = new InetSocketAddress(host, port);
        SocketChannel socketChannel = SocketChannel.open(socketAddress);

        return send(socketChannel, msg);

    }

    public static SocketChannel send(SocketChannel socketChannel, Message msg) throws Exception{
        Logger.logMsg("sending message");
        socketChannel.configureBlocking(false);

        if(socketChannel.isConnected()){
            byte[] msgBytes = Util.serialize(msg);
            ByteBuffer buffer = ByteBuffer.wrap(msgBytes);
            socketChannel.write(buffer);
            return socketChannel;
        }

        return null;
    }

    public static byte[] read(SocketChannel clientChannel) throws Exception{
        clientChannel.configureBlocking(false);

        if((clientChannel == null) || !clientChannel.isConnected()){
            Logger.logMsg("Can't read from closed channel");
            return new byte[0];
        }
        ByteBuffer buffer = ByteBuffer.allocate(4096);
        int bytesRead = clientChannel.read(buffer);
        int totalBytesRead = bytesRead;
//        while(bytesRead > 0){
//            bytesRead = clientChannel.read(buffer);
//            System.out.println("bytesRead " + bytesRead);
//            totalBytesRead += bytesRead;
//        }

        if(bytesRead == -1){
            clientChannel.close();
            Logger.logMsg("Closed the client channel");
            return new byte[0];
        }

        byte[] bytes = new byte[totalBytesRead];
        buffer.flip();
        buffer.get(bytes);
        return bytes;

    }

    public void handleRequest(Request request, SocketChannel clientChannel){

        Logger.logMsg("Request Type " + request.getType());

        switch (request.getType()){

            case JOIN:
            {
                Node node = request.getSender();
                cluster.addNode(node);
                //Send ack to Join_Node requested node
                ACKResponseMessage ackResponseMessage = new ACKResponseMessage(node);
                try {
                    Logger.logMsg("sending ACK Response");
                    send(clientChannel, ackResponseMessage);
                }catch(Exception ex){
                    Logger.logError("Can't able to send response");
                }
                try {
                    clientChannel.close();
                }catch (IOException ex){
                    Logger.logError("Unable to close channel");
                }
                //Send updated cluster list to all the nodes in cluster for synchronization of nodes
                this.sendClusterUpdateRequest(cluster);
                break;
            }

            case CLUSTER_UPDATE:
            {
                ClusterUpdateRequestMessage reqMsg = (ClusterUpdateRequestMessage)request;
                cluster.updateNodesList(reqMsg.getCluster());
                Node leader = reqMsg.getLeaderNode();
                if(leader != null){
                    leaderNode = leader;
                    leaderElected = true;
                }
                break;
            }

            case ELECTION:
            {
                ElectionRequestMessage msg = (ElectionRequestMessage)request;
                Node n = msg.getSender();
                ElectionResponse response = new ElectionResponse(node);
                try {
                    send(clientChannel, response);
                }catch (Exception ex){
                    Logger.logError(ex.getMessage());
                }
                break;
            }

            case LEADER_UPDATE:
            {
                LeaderUpdateRequestMessage msg = (LeaderUpdateRequestMessage) request;
                leaderNode = msg.getLeaderNode();
                leaderElected = true;
                break;
            }
        }

    }

    public void handleResponse(Response response, SocketChannel clientChannel){

        Logger.logMsg("Respone Type " + response.getType());


        switch (response.getType()){
            case ELECTION_OK:
            {
                isElectionRunning = false;
                electionResponseArrived = true;
                break;
            }
        }


    }

    public void sendClusterUpdateRequest(Cluster cluster){
        Logger.logMsg("sending cluster update request");
        System.out.println();
        final List<Node> nodes = cluster.getNodes();
        for(final Node n: nodes){
            if(!n.equals(node)){
                final ClusterUpdateRequestMessage msg = new ClusterUpdateRequestMessage(node, cluster, leaderNode);
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Logger.logMsg("Sending message to " + n + " from " + node);
                            send(n.getHost(), port, msg);
                        }catch(Exception ex){
                            Logger.logError("Unable to send cluster update message to " + n);
                        }
                    }
                });
                t.start();
            }
        }
    }

    public void startElection(){
        Logger.logMsg("Starting Election.......");
        final List<Node> nodes = cluster.getNodes();
        if(nodes.size() >= 2){
            final boolean[] higherPriorityNodeAvail = {false};
            electionResponseArrived = false;
            leaderElected = false;
            isElectionRunning = true;
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    for(Node n: nodes){
                        if(!n.equals(node)){
                            if(n.getPid() > node.getPid()){
                                ElectionRequestMessage msg = new ElectionRequestMessage(node);
                                Logger.logMsg("Sending message to " + n + " from " + node);
                                try {
                                    SocketChannel clientChannel = send(n.getHost(), port, msg);
                                    clientChannel.register(selector, SelectionKey.OP_READ);
                                    higherPriorityNodeAvail[0] = true;
                                }catch (Exception ex){
                                    Logger.logError("Can't able to send the election message to " + n + " from " + node);
                                }
                            }
                        }
                    }
                    //If no any higher priority node available in cluster
                    if(!higherPriorityNodeAvail[0]){
                        leaderElected = true;
                        leaderNode = node;
                        sendLeaderElectedMessage(cluster, leaderNode);
                    }
                    try{

                        TimeUnit.SECONDS.sleep(2);
                        if(!electionResponseArrived){
                            leaderElected = true;
                            leaderNode = node;
                            sendLeaderElectedMessage(cluster, leaderNode);
                        }

                    }catch (Exception ex){
                        Logger.logError(ex.getMessage());
                    }
                }
            });


        }
    }

    @Override
    public void sendLeaderElectedMessage(Cluster cluster, Node leaderNode) {
        Logger.logMsg("sending leader update request");
        final List<Node> nodes = cluster.getNodes();
        for(final Node n: nodes){
            if(!n.equals(node)){
                final LeaderUpdateRequestMessage msg = new LeaderUpdateRequestMessage(leaderNode, node);
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Logger.logMsg("Sending message to " + n + " from " + node);
                            SocketChannel clientChannel = send(n.getHost(), port, msg);
                        }catch(Exception ex){
                            Logger.logError("Unable to send cluster update message to " + n);
                        }
                    }
                });
                t.start();
            }
        }
    }

}