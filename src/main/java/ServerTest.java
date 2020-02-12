import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ServerTest {
    public static void main(String[] args) {
        try {
            InetSocketAddress socketAddress = new InetSocketAddress("localhost",3000);
            SocketChannel client = SocketChannel.open(socketAddress);
            JoinNodeRequestMessage msg = new JoinNodeRequestMessage(new Node(2, "127.0.0.1"));
            byte[] reqmsg = Util.serialize(msg);
            ByteBuffer buffer = ByteBuffer.wrap(reqmsg);
            client.write(buffer);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
