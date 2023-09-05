package monopol.client;

public class ClientPlayer {
    public final boolean isHost;
    private String name;

    public ClientPlayer(boolean isHost) {
        this.isHost = isHost;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
