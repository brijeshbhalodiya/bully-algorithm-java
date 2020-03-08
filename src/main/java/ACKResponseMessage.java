public class ACKResponseMessage implements Response {

    private final static ResponseType type = ResponseType.ACK;

    private Node sender;

    public ACKResponseMessage(Node sender){
        this.sender = sender;
    }

    @Override
    public ResponseType getType() {
        return type;
    }

    @Override
    public Node getSender() {
        return this.sender;
    }

    public void setSender(Node sender) {
        this.sender = sender;
    }
}
