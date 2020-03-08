public class ElectionResponse implements Response {
    private final static ResponseType type = ResponseType.ELECTION_OK;

    private Node sender;

    public ElectionResponse(Node sender) {
        this.sender = sender;
    }

    @Override
    public Node getSender() {
        return sender;
    }

    public void setSender(Node sender) {
        this.sender = sender;
    }

    @Override
    public ResponseType getType() {
        return type;
    }
}
