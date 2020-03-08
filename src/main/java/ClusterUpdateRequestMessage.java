public class ClusterUpdateRequestMessage implements Request {
    private final static RequestType type = RequestType.CLUSTER_UPDATE;

    private Node sender;
    private Cluster cluster;
    private Node leaderNode;

    public ClusterUpdateRequestMessage(Node sender, Cluster cluster, Node leaderNode) {
        this.sender = sender;
        this.cluster = cluster;
        this.leaderNode = leaderNode;
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

    public Node getLeaderNode() {
        return leaderNode;
    }

    public void setLeaderNode(Node leaderNode) {
        this.leaderNode = leaderNode;
    }
}
