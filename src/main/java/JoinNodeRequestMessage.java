public class JoinNodeRequestMessage implements Request {

    private final static RequestType type = RequestType.JOIN;

    private Node sender;

    public JoinNodeRequestMessage(Node sender) {
        this.sender = sender;
    }

    @Override
    public RequestType getType() {
        return type;
    }

    public Node getSender() {
        return sender;
    }

    public void setSender(Node sender) {
        this.sender = sender;
    }
}
