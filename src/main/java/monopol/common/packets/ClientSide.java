package monopol.common.packets;

import monopol.client.Client;
import monopol.client.screen.RootPane;

public record ClientSide(Client client, RootPane display) implements Side {}