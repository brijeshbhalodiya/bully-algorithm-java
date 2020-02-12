import java.io.Serializable;

public class Node implements Serializable {

    private static final long serialVersionUID = -299482035708790407L;

    //Priority
    private int pid;
    //Host address
    private String host;

    public Node(int pid, String host){
        this.pid = pid;
        this.host = host;
    }

    public int getPid(){
        return this.pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public String toString(){
        return "PID: " + this.pid + " HOSTADDRESS: " + this.host;
    }


}