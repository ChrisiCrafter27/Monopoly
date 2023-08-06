public class Message {
    public Object[] objects;
    public MessageType messageType;

    public Message() {

    }

    public Message(Object[] objects, MessageType messageType) {
        this.objects = objects;
        this.messageType = messageType;
    }

    public Object[] getMessage() {
        return objects;
    }

    public MessageType getMessageType() {
        return messageType;
    }
}
