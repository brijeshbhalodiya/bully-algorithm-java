import java.io.Serializable;

public interface Response extends Serializable {
    ResponseType getType();
}
