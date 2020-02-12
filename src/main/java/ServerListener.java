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
import java.util.Set;

public class ServerListener extends Thread{
    private int port = 3000;
    ServerSocketChannel serverSocketChannel;
    Selector selector;
    private final static Cluster cluster = Cluster.getInstance();


    public ServerListener() throws IOException {
        this.selector = Selector.open();
        System.out.println("Selector is ready to make the connection: " + this.selector.isOpen());

        this.serverSocketChannel = ServerSocketChannel.open();
        InetSocketAddress hostAddress = new InetSocketAddress(this.port);

        this.serverSocketChannel.bind(hostAddress);
        this.serverSocketChannel.configureBlocking(false);

        //Create node on which server is running & add that node into cluster
        Node node = new Node(1, "localhost");
        this.cluster.addNode(node);

        int ops = serverSocketChannel.validOps();
        SelectionKey selectionKey = this.serverSocketChannel.register(selector, ops);
        System.out.println("ServerListener.ServerListener");
    }

    @Override
    public void run(){
        System.out.println("ServerListener.run");
        while(true){
            try {


                Set<SelectionKey> selectedKey = this.selector.selectedKeys();
//                System.out.println(selectedKey);
                Iterator<SelectionKey> itr = selectedKey.iterator();
                while(itr.hasNext()){
                    SelectionKey key = itr.next();
                    System.out.println("hasnext");
                    if(key.isValid() && key.isAcceptable()){
                        System.out.println("Valid");
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
                System.out.println("join request");
                break;


        }

    }



}