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

public class ServerListener extends Thread{
    private static final int port = 3000;
    ServerSocketChannel serverSocketChannel;
    Selector selector;
    private static Node node;
    private final static Cluster cluster = Cluster.getInstance();


    public ServerListener(String nodeHostAddress, int priority) throws IOException{
        this.node = new Node(priority, InetAddress.getLocalHost().getHostAddress());

        this.init();
        JoinNodeRequestMessage joinNodeRequestMessage = new JoinNodeRequestMessage(this.node);
        try {
            SocketChannel clientChannel = send(nodeHostAddress, this.port, joinNodeRequestMessage);
            byte[] responseBytes = read(clientChannel);
            Object obj = Util.deserialize(responseBytes);

            if(obj instanceof Response){
                Response response = (Response)obj;
                Node senderNode = response.getSender();
            }

        }catch(Exception ex){
            System.err.println("Unable to send joinNodeRequestMessage");
        }

    }

    public ServerListener(int priority) throws  IOException{
        this.node = new Node(priority, InetAddress.getLocalHost().getHostAddress());
        this.init();
    }

    public ServerListener() throws IOException {
        this.node = new Node(1, InetAddress.getLocalHost().getHostAddress());
        this.init();
    }

    public void init() throws IOException{
        this.selector = Selector.open();
        System.out.println("Selector is ready to make the connection: " + this.selector.isOpen());

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
        System.out.println("ServerListener.run");
        while(true){
            try {
                this.selector.select();

                Set<SelectionKey> selectedKey = this.selector.selectedKeys();
//                System.out.println(selectedKey);
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
                        System.out.println("New connection: " + ip.getHostName());

                    }else if(key.isValid() && key.isReadable()){

                        SocketChannel clientChannel = (SocketChannel)key.channel();
                        clientChannel.configureBlocking(false);

                        byte[] payload = read(clientChannel);

                        if(payload.length > 0){
                            Object obj = Util.deserialize(payload);

                            if(obj instanceof Request){
                                handleRequest((Request) obj, clientChannel);
                            }

                        }

                    }

                    itr.remove();

                }

            } catch (Exception e) {
                System.out.println("Encountered problem, exiting service loop");
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

        if((clientChannel == null) || !clientChannel.isConnected()){
            System.out.println("Can't read from closed channel");
            return new byte[0];
        }
        ByteBuffer buffer = ByteBuffer.allocate(4096);
        int bytesRead = clientChannel.read(buffer);
        int totalBytesRead = bytesRead;
        while(bytesRead > 0){
            bytesRead = clientChannel.read(buffer);
            totalBytesRead += bytesRead;
        }

        if(bytesRead == -1){
            clientChannel.close();
            System.out.println("Closed the client channel");
            return new byte[0];
        }

        byte[] bytes = new byte[totalBytesRead];
        buffer.flip();
        buffer.get(bytes);

        return bytes;

    }

    public static void handleRequest(Request request, SocketChannel clientChannel){

        switch (request.getType()){

            case JOIN:
                Node node = request.getSender();
                cluster.addNode(node);
                //Send ack to Join_Node requested node
                ACKResponseMessage ackResponseMessage = new ACKResponseMessage(node);
                try {
                    send(clientChannel, ackResponseMessage);
                }catch(Exception ex){
                    System.err.println("Can't able to send response");
                }
                try {
                    clientChannel.close();
                }catch (IOException ex){
                    System.err.println("Unable to close channel");
                }
                sendClusterUpdateRequest(cluster);
                break;


        }

    }

    public static void sendClusterUpdateRequest(Cluster cluster){
        boolean status = true;
        final List<Node> nodes = cluster.getNodes();
        for(final Node n: nodes){
            final ClusterUpdateRequestMessage msg = new ClusterUpdateRequestMessage(node, cluster);
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        send(n.getHost(), port, msg);
                    }catch(Exception ex){
                        System.err.println("Unable to send cluster update message to " + n);
                    }
                }
            });
        }
    }

//    public static void handleJoinRequest(SocketChannel clientChannel){
//
//    }



}