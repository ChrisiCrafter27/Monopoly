package monopol.client;

public class ClientPlayer {
    public final boolean isHost;
    private boolean isTrading = false;
    private String name;

    public ClientPlayer(boolean isHost) {
        this.isHost = isHost;
    }

    public boolean isTrading() {
        return isTrading;
    }
    public String getName() {
        return name;
    }
    public void setTrading(boolean trading) {
        isTrading = trading;
    }
    public void setName(String name) {
        this.name = name;
    }
}
