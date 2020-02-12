import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Cluster{

    private static Cluster cluster = null;

    private final Map<Integer, Node> nodes = new HashMap<Integer, Node>();

    //For singleton object
    public static Cluster getInstance(){
        if(cluster == null){
            cluster = new Cluster();
        }
        return  cluster;
    }

    public synchronized void addNode(Node node){
        this.nodes.put(node.getPid(), node);
        System.out.println("Node added into the cluster " + node);
    }

    public synchronized boolean removeNode(Node node){
        if(this.nodes.remove(node.getPid()) != null ){
            System.out.println("Node removed from the cluster " + node);
            return true;
        }
        return false;
    }
}