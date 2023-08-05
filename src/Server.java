import java.io.*;
import java.net.*;

public class Server {
    private ServerSocket server;

    public Server(int port) {
        try {
            server = new ServerSocket(port);
            server.setSoTimeout(100000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() {
        while (true) {
            try {
                String hs=null;
                try
                {
                    System.out.println(InetAddress.getLocalHost().getHostAddress());
                } catch (UnknownHostException e)
                {
                    e.printStackTrace();
                }

                System.out.println(server.getLocalSocketAddress());
                System.out.println("Waiting for client at port " + server.getLocalPort());
                Socket client = server.accept();
                DataInputStream input = new DataInputStream(client.getInputStream());
                System.out.println(input.readUTF());
                System.out.println(client.getRemoteSocketAddress());
                System.out.println(client.getLocalAddress());
                DataOutputStream output = new DataOutputStream(client.getOutputStream());
                output.writeUTF("Diese Nachricht kommt vom Server");
                client.close();
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
    }
    public static void main(String[] args) {
        Server s = new Server(25565);
        s.start();
    }
}