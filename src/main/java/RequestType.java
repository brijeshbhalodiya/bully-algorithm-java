public enum RequestType {
    IS_ALIVE, //used to check failure of leader
    ELECTION, //used to send election request to higher priority nodes
    JOIN, //When new node want to join the cluster
    CLUSTER_UPDATE, //When new node joined the cluster this req type used to send all the cluster nodes details to all the nodes of cluster
    LEADER_UPDATE //When new leader is elected
}
