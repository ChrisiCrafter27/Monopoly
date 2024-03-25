package monopol.common.message;

import monopol.common.data.DataReader;
import monopol.common.data.DataWriter;
import monopol.common.packets.Packet;
import monopol.common.utils.Json;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

public class Message {
    public List<Object> content;
    public String clazz;

    public Message() {}

    public Message(Packet<?> packet) {
        DataWriter writer = new DataWriter();
        packet.serialize(writer);
        this.content = writer.getData();
        this.clazz = packet.getClass().getName();
    }

    public List<Object> getContent() {
        return content;
    }

    public Class<?> getClazz() {
        try {
            return Class.forName(clazz);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public void send(Socket target) throws IOException {
        try {
            DataOutputStream output = new DataOutputStream(target.getOutputStream());
            output.writeUTF(Json.toString(this, false));
        } catch (NullPointerException e) {
            e.printStackTrace(System.err);
        }
    }
}
