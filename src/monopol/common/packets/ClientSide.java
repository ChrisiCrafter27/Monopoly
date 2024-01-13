package monopol.common.packets;

import monopol.client.Client;
import monopol.client.screen.RootPane;

public class ClientSide implements Side {
    public final Client client;
    public final RootPane display;

    public ClientSide(Client client, RootPane display) {
        this.client = client;
        this.display = display;
    }
}
