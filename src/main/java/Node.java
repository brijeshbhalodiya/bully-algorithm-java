import java.io.Serializable;

public class Node implements Serializable {

    private static final long serialVersionUID = -299482035708790407L;

    private int pid;
    private String host;
    private int port;

    public Node(int pid, String host, int port){
        this.pid = pid;
        this.host = host;
        this.port = port;
    }

    public int getPid(){
        return this.pid;
    }


    @Override
    public String toString(){
        return "PID: " + this.pid + " HOSTADDRESS: " + this.host + ":" + this.port;
    }


}