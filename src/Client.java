import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class Client {

    public static void main(String[] args) {
        try {
            //Kommentar
            Socket client = new Socket("localhost", 25565);
            DataOutputStream output = new DataOutputStream(client.getOutputStream());
            output.writeUTF("Diese Nachricht kommt vom Client");
            DataInputStream input = new DataInputStream(client.getInputStream());
            System.out.println(input.readUTF());
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}