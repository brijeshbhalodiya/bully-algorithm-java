import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.TimeUnit;

public class FailureDetector extends Thread {
    private static final int port = 3000;
    Selector selector;
    private boolean running = false;
    private Node leaderNode;
    private Node node;
    private boolean responseArrived = false;
    private ServiceHandler handler;


    public FailureDetector(Selector selector, Node node, ServiceHandler handler){
        this.selector = selector;
        this.node = node;
        this.handler = handler;
    }

    public FailureDetector(Selector selector, Node node, Node leaderNode, ServiceHandler handler){
        this.selector = selector;
        this.leaderNode = leaderNode;
        this.node = node;
        this.handler = handler;
    }

    @Override
    public void run(){
        Logger.logMsg("Failure Detector started");
        while (true){
            if(this.isRunning()){
                IsAliveRequestMessage msg = new IsAliveRequestMessage(node);
                try {
                    this.responseArrived = false;
                    Logger.logMsg("Sending isAlive Message to leader");
                    SocketChannel clientChannel = ServerListener.send(leaderNode.getHost(), port, msg);
                    if(clientChannel != null){
                        clientChannel.register(this.selector, SelectionKey.OP_READ);
                        TimeUnit.SECONDS.sleep(2);

                        if(!this.responseArrived){
                            this.startElection();
                        }

                    }else {
                        throw new Exception("NULL clientChannel");
                    }

                }catch(Exception ex){
                    Logger.logError(ex.getMessage());
                    Logger.logError("ERROR: Sending the IsAlive Request Message to leaders");
                    this.responseArrived = false;
                    this.stopFailureDetector();
                }

            }
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void stopFailureDetector(){
        Logger.logMsg("Stopping Failure Detector");
        this.running = false;
    }

    public void start(){
        Logger.logMsg("Starting Failure Detector");
        if(!this.isRunning()){
            if(!node.equals(leaderNode)){
                this.running = true;
                super.start();
            }
        }else{
            Logger.logMsg("Failure Detector is already running");
        }
    }

    public void startElection(){
        this.handler.startElection();
    }

    public void start(Node leaderNode){
        this.leaderNode = leaderNode;
        this.start();
    }
}
