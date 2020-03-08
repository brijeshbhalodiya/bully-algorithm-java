public class ClusterUpdateRequestMessage implements Request {
    private final static RequestType type = RequestType.CLUSTER_UPDATE;

    private Node sender;
    private Cluster cluster;

    public ClusterUpdateRequestMessage(Node sender, Cluster cluster) {
        this.sender = sender;
        this.cluster = cluster;
    }

    @Override
    public RequestType getType() {
        return type;
    }

    @Override
    public Node getSender() {
        return sender;
    }

    public void setSender(Node sender) {
        this.sender = sender;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }
}
