public class IsAliveRequestMessage implements Request {
    private final static RequestType type = RequestType.IS_ALIVE;

    private Node sender;

    public IsAliveRequestMessage(Node sender) {
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
}
