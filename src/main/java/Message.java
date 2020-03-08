import java.io.Serializable;

public interface Message extends Serializable {

    static final long serialVersionUID = -299482035708790407L;

    Node getSender();

}
