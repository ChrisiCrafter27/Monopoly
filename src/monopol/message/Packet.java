package monopol.message;

import java.io.Serializable;

public interface Packet<T extends Packet<T>> extends Serializable {
    default T of(Message message) {
        return (T) message.getMessage()[0];
    }
    default Message toMessage() {
        return new Message((T) this, MessageType.PACKET);
    }
    void handle();
}
