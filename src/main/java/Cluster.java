import java.util.ArrayList;
import java.util.List;

public class Cluster{

    private static Cluster cluster = null;

    private List<Node> nodes = new ArrayList<Node>();

    //For singleton object
    public static Cluster getInstance(){
        if(cluster == null){
            cluster = new Cluster();
        }
        return  cluster;
    }

    public synchronized void addNode(Node node){
        this.nodes.add(node);
        System.out.println("Node added into the cluster " + node);
    }

    public synchronized void updateNodesList(Cluster cluster){
        this.nodes = cluster.getNodes();
        System.out.println("List of nodes in cluster is updated");
    }

    public synchronized boolean removeNode(Node node){
        if(this.nodes.remove(node)){
            System.out.println("Node removed from the cluster " + node);
            return true;
        }

        return false;
    }

    public static Cluster getCluster() {
        return cluster;
    }

    public static void setCluster(Cluster cluster) {
        Cluster.cluster = cluster;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }
}