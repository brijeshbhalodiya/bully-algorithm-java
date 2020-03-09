import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Cluster implements Serializable {

    private static final long serialVersionUID = -299482035708790407L;

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
        Logger.logMsg("Node added into the cluster " + node);
    }

    public synchronized void updateNodesList(Cluster cluster){
        this.nodes = cluster.getNodes();
        Logger.logMsg("List of nodes in cluster is updated");
    }

    public synchronized boolean removeNode(Node node, ServiceHandler handler){
        Node removedNode = null;
        for(Node n: nodes){
            if(n.equals(node)){
                removedNode = n;
            }
        }
        if(removedNode != null && this.nodes.remove(removedNode)){
            Logger.logMsg("Node removed from the cluster " + node);
//            handler.sendClusterUpdateRequest(this);
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

    public int getNumOfNodes(){
        return this.nodes.size();
    }

}