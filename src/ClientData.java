import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

/**
 * Created with IntelliJ IDEA.
 * User: Vlad
 * Date: 08.11.13
 * Time: 16:59
 * To change this template use File | Settings | File Templates.
 */
public class ClientData {
    public int number;
    public DataOutputStream dataOutputStream;
    public DataInputStream dataInputStream;

    public ClientData( int number, DataOutputStream dataOutputStream, DataInputStream dataInputStream ) {
        this.number = number;
        this.dataOutputStream = dataOutputStream;
        this.dataInputStream = dataInputStream;
    }
}
