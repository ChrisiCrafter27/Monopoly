package monopol.server;

import java.io.Serializable;

public class ServerPlayer implements Serializable {
    private String name;

    public ServerPlayer(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
