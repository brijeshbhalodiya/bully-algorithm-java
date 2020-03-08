public interface ServiceHandler {

    void startElection();

    void sendClusterUpdateRequest(Cluster cluster);

    void sendLeaderElectedMessage(Cluster cluster, Node leaderNode);

}
