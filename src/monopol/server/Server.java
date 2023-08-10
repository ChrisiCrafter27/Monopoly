package monopol.server;

import monopol.utils.Json;
import monopol.utils.Message;
import monopol.utils.MessageType;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {
    public final ServerSocket server;
    public final HashMap<Integer, Socket> clients = new HashMap<>();
    public final HashMap<Socket, Boolean> pingCheck = new HashMap<>();
    private boolean acceptNewClients = false;

    private final Thread connectionThread = new Thread() {
        @Override
        public void run() {
            while(!isInterrupted()) {
                if(acceptNewClients) {
                    try {
                        System.out.println("Waiting for client at port " + server.getLocalPort());
                        Socket newClient = server.accept();
                        if (clients.containsValue(newClient)) continue;
                        clients.put(clients.size() + 1, newClient);
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException();
                    }
                }
            }
        }
    };
    private final Thread requestThread = new Thread() {
        @Override
        public void run() {
            while(!isInterrupted())
            {
                try {
                    clients.forEach((id, client) -> {
                        try {
                            DataInputStream input = new DataInputStream(client.getInputStream());
                            try {
                                String data = input.readUTF();
                                messageReceived(data, client);
                            } catch (IOException e) {
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            throw new RuntimeException();
                        }
                    });
                } catch (ConcurrentModificationException e) {}
                try {
                    sleep(10);
                } catch (InterruptedException e) {}
            }
        }
    };

    private final Thread pingThread = new Thread() {
        @Override
        public void run() {
            while(!isInterrupted())
            {
                List<Socket> kick = new ArrayList<>();
                clients.forEach((id, client) -> {
                    if(!pingCheck.containsKey(client)) pingCheck.put(client, true);
                    if(!pingCheck.get(client)) kick.add(client);
                    pingCheck.replace(client, false);
                });
                for (Socket client : kick) {
                    kick(client);
                }
                try {
                    sleep(10000);
                } catch (InterruptedException e) {}
            }
        }
    };

    public Server(int port) {
        try {
            server = new ServerSocket(port);
            connectionThread.start();
            requestThread.start();
            pingThread.start();
            //server.setSoTimeout(100000);
        } catch (IOException e) {
            System.err.println("[Server]: [ERROR]: Failed to start server. That could be due to an occupied port. The server usually uses the port 25565");
            throw new RuntimeException();
        }
    }

    public String open() {
        acceptNewClients = true;
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        acceptNewClients = false;
    }

    public void kick(Socket client) {
        if(!clients.containsValue(client)) throw new RuntimeException();
        final int[] idArray = new int[1];
        clients.forEach((k, v) -> {
            if(v == client) {
                idArray[0] = k;
            }
        });
        int id = idArray[0];
        try {
            Message.sendTypeOnly(MessageType.TERMINATE, client);
            for(int i = id; i < clients.size(); i++) {
                clients.replace(i, clients.get(i + 1));
            }
            clients.remove(clients.size());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void messageReceived(String value, Socket client) {
        Message message;
        try {
            message = Json.toObject(value, Message.class);
            switch (message.getMessageType()) {
                case PRINTLN:
                    System.out.println(message.getMessage()[0]);
                    break;
                case PING:
                    DataOutputStream output = new DataOutputStream(client.getOutputStream());
                    Object[] array = new Object[1];
                    array[0] = message.getMessage()[0];
                    output.writeUTF(Json.toString(new Message(array, MessageType.PING_BACK), false));
                    break;
                case PING_BACK:
                    long delay = System.currentTimeMillis() - (long) message.getMessage()[0];
                    if(pingCheck.containsKey(client)) pingCheck.replace(client, true);
                    System.out.println("[Server]: Ping to " + client.getInetAddress().getHostAddress() + " is " + delay + "ms");
                    break;
                case DISCONNECT:
                    kick(client);
                    break;
                case NULL:
                    break;
                default:
                    throw new RuntimeException();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public boolean serverActive() {
        return !requestThread.isInterrupted() && requestThread.isAlive() && !connectionThread.isInterrupted() && connectionThread.isAlive(); //FALSCH
    }

    public static void main(String[] args) {
        Server server = new Server(25565);
        System.out.println("Server IP: " + server.open());
    }
}