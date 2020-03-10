import java.nio.channels.Selector;

public interface ServiceHandler {

    void startElection();

    void sendClusterUpdateRequest(Cluster cluster);

    void sendLeaderElectedMessage(Cluster cluster, Node leaderNode);

    void startFailureDetector(Selector selector, Node node, Node leaderNode, ServiceHandler handler);

}
