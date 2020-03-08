public class ElectionRequestMessage implements Request {

    private final static RequestType type = RequestType.ELECTION;

    private Node sender;

    public ElectionRequestMessage(Node sender) {
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

    public void setSender(Node sender) {
        this.sender = sender;
    }
}
