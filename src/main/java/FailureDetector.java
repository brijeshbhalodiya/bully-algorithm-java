import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

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
        while (this.running){
            IsAliveRequestMessage msg = new IsAliveRequestMessage(node);
            try {
                this.responseArrived = false;
                Logger.logMsg("Sending isAlive request Message to leader " + leaderNode);
                SocketChannel clientChannel = ServerListener.send(leaderNode.getHost(), port, msg);
                if(clientChannel != null){
                    clientChannel.register(this.selector, SelectionKey.OP_READ);
                    try{
//                            TimeUnit.SECONDS.sleep(2);
                        LockSupport.parkNanos(this, TimeUnit.SECONDS.toNanos(2 + node.getPid() - 1));

                    }catch (Exception iex){
                        Logger.logError("Thread interrupted");
                        iex.printStackTrace();
                    }
                    if(!this.responseArrived){
                        Cluster cluster = Cluster.getInstance();
                        cluster.removeNode(leaderNode, handler);
                        Logger.logMsg("Response not arrived");
                        this.stopFailureDetector();
                        this.startElection();
                    }

                }else {
                    throw new Exception("NULL clientChannel");
                }

            }catch(Exception ex){
                ex.printStackTrace();
                Logger.logError(ex.getMessage());
                Logger.logError("ERROR: Sending the IsAlive Request Message to leaders");
                Cluster cluster = Cluster.getInstance();
                cluster.removeNode(leaderNode, handler);
                this.responseArrived = false;
                this.stopFailureDetector();
                this.startElection();
            }

        }
    }

    synchronized public void setResponseArrived(boolean responseArrived) {
        this.responseArrived = responseArrived;
    }

    public boolean isRunning() {
        return running;
    }

    synchronized public void stopFailureDetector(){
        Logger.logMsg("Stopping Failure Detector");
        this.running = false;
    }

    synchronized public void start(){
        Logger.logMsg("Starting Failure Detector");
        if(!this.running){
            if(!node.equals(leaderNode)){
                this.running = true;
                if(!this.isAlive()){
                    Logger.logMsg("Not alive");
                    super.start();
                }else{
                    Logger.logMsg("Thread Alive");
                }
            }else{
                this.stopFailureDetector();
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
