import java.util.ArrayList;
import java.util.List;

public class Cluster{

    private final List<Node> nodes = new ArrayList<Node>();

    public synchronized void addNode(Node node){
        this.nodes.add(node);
        System.out.println("Node added into the cluster " + node);
    }

    public synchronized boolean removeNode(Node node){
        if(this.nodes.remove(node)){
            System.out.println("Node removed from the cluster " + node);
            return true;
        }

        return false;
    }
}