package monopol.client;

import monopol.client.screen.RootPane;
import monopol.common.core.Monopoly;
import monopol.common.packets.ClientSide;
import monopol.common.packets.PacketManager;
import monopol.common.packets.custom.DisconnectC2SPacket;
import monopol.common.packets.custom.PingC2SPacket;
import monopol.common.utils.Json;
import monopol.common.message.DisconnectReason;
import monopol.common.message.IServer;
import monopol.common.message.Message;
import monopol.common.utils.ServerProperties;

import javax.swing.*;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
    public static final int CLIENT_TIMEOUT = 5000;
    private final Socket client;
    private final IServer serverInterface;
    private final ClientPlayer player;
    private final RootPane root;
    public final TradeData tradeData = new TradeData();
    private long ping = -1;
    private boolean received = true;
    public String requestRejoin;

    private final Thread clientThread = new Thread() {
        @Override
        public void run() {
            while(!Thread.interrupted()) {
                try {
                    DataInputStream input = new DataInputStream(client.getInputStream());
                    String data = input.readUTF();
                    messageReceived(data);
                } catch (Exception e) {
                    if(!Thread.interrupted()) {
                        e.printStackTrace(System.err);
                        System.out.println("[Client]: Connection lost: No further information.");
                        close();
                    }
                }
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    break;
                }
            }
            close();
        }
    };

    private final Thread pingThread = new Thread(() -> {
        while(!Thread.interrupted()) {
            PacketManager.sendC2S(new PingC2SPacket(), this, Throwable::printStackTrace);
            if (!received) {
                try {
                    System.out.println("[Client] Verbindung zum Server verloren");
                    serverMethod().kick(player().getName(), DisconnectReason.CONNECTION_LOST);
                } catch (IllegalStateException e) {
                    close();
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                    close();
                }
            }
            if(player().getName() != null) received = false;
            try {
                Thread.sleep(CLIENT_TIMEOUT);
            } catch (InterruptedException e) {
                return;
            }
        }
    });

    public Client(Inet4Address ip, ServerProperties serverProperties, boolean isHost, RootPane root) throws NotBoundException {
        this(ip, serverProperties, isHost, root, null);
    }

    public Client(Inet4Address ip, ServerProperties serverProperties, boolean isHost, RootPane root, String requestRejoin) throws NotBoundException {
        try {
            this.root = root;
            this.player = new ClientPlayer(isHost);
            this.requestRejoin = requestRejoin;
            client = new Socket(ip, serverProperties.port1);
            Registry registry = LocateRegistry.getRegistry(ip.getHostAddress(), serverProperties.port2);
            serverInterface = (IServer) registry.lookup("Server");
            if(serverMethod().stopped() || !serverInterface.acceptsNewClient()) {
                JOptionPane.showMessageDialog(Monopoly.INSTANCE.parentComponent, "Beitreten nicht möglich", "Spiel beitreten", JOptionPane.WARNING_MESSAGE);
                client.close();
                return;
            }
            clientThread.start();
            pingThread.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void messageReceived(String value) {
        try {
            PacketManager.handle(Json.toObject(value, Message.class), new ClientSide(this, root));
        } catch (IOException e) {
            System.out.println("[Client]: Connection lost: No further information.");
            close();
        }
    }

    public void ping(long ping) {
        this.ping = ping;
        received = true;
    }

    public String requestRejoin() {
        return requestRejoin;
    }

    public Socket socket() {
        return client;
    }

    public ClientPlayer player() {
        return player;
    }

    public long getPing() {
        return ping;
    }

    public void close() {
        PacketManager.sendC2S(new DisconnectC2SPacket(DisconnectReason.CLIENT_CLOSED), this, Throwable::printStackTrace);
        interrupt();
        if(!closed()) try {
            client.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean closed() {
        return client.isClosed();
    }

    public IServer serverMethod() {
        return serverInterface;
    }

    public void interrupt() {
        clientThread.interrupt();
        pingThread.interrupt();
    }
}