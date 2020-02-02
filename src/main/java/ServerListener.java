import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
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
    private final Cluster cluster = new Cluster();


    public ServerListener() throws IOException {
        this.selector = Selector.open();
        System.out.println("Selector is ready to make the connection: " + this.selector.isOpen());

        this.serverSocketChannel = ServerSocketChannel.open();
        InetSocketAddress hostAddress = new InetSocketAddress(this.port);

        this.serverSocketChannel.bind(hostAddress);
        this.serverSocketChannel.configureBlocking(false);

        //Create node on which server is running & add that node into cluster
        Node node = new Node(1, "localhost", this.port);
        this.cluster.addNode(node);

        int ops = serverSocketChannel.validOps();
        SelectionKey selectionKey = this.serverSocketChannel.register(selector, ops);
    }

    @Override
    public void run(){
        while(true){
            while(true){
                try {
                    int noOfKeys = this.selector.select();

                    Set<SelectionKey> selectedKey = selector.selectedKeys();
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
                            Node node = new Node(2, ip.getHostAddress(), socket.getPort());

                            this.cluster.addNode(node);
                            itr.remove();
                        }

                    }

                } catch (Exception e) {
                    System.out.println("Encountered problem, exiting service loop");
                    e.printStackTrace();
                    System.exit(1);
                }

            }
        }
    }




}