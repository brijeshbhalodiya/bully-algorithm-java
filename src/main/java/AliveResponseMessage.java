public class AliveResponseMessage implements Response {
    private final static ResponseType type = ResponseType.ALIVE;

    private Node sender;

    public AliveResponseMessage(Node sender) {
        this.sender = sender;
    }

    @Override
    public ResponseType getType() {
        return type;
    }

    @Override
    public Node getSender() {
        return sender;
    }
}
