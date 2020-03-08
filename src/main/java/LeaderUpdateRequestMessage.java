public class LeaderUpdateRequestMessage implements Request {

    private final static RequestType type = RequestType.LEADER_UPDATE;

    private Node leaderNode;
    private Node sender;

    public LeaderUpdateRequestMessage(Node leaderNode, Node sender) {
        this.leaderNode = leaderNode;
        this.sender = sender;
    }

    @Override
    public RequestType getType() {
        return type;
    }

    @Override
    public Node getSender() {
        return sender;
    }

    public Node getLeaderNode(){
        return this.leaderNode;
    }
}
