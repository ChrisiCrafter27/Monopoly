package monopol.common.packets.custom;

import monopol.client.Client;
import monopol.client.screen.RootPane;
import monopol.common.data.DataReader;
import monopol.common.data.DataWriter;
import monopol.common.message.DisconnectReason;
import monopol.common.packets.S2CPacket;

import javax.swing.*;
import java.util.List;

public class DisconnectS2CPacket extends S2CPacket<DisconnectS2CPacket> {
    private final DisconnectReason reason;

    public DisconnectS2CPacket(DisconnectReason reason) {
        this.reason = reason;
    }

    @Override
    public void serialize(DataWriter writer) {
        writer.writeEnum(reason);
    }

    @SuppressWarnings("unused")
    public static DisconnectS2CPacket deserialize(DataReader reader) {
        return new DisconnectS2CPacket(reader.readEnum(DisconnectReason.class));
    }

    @Override
    public void handleOnClient(Client client, RootPane display) {
        client.interrupt();
        String s = switch (reason) {
            case CONNECTION_LOST -> "Verbindung verloren: Zeitüberschreitung.";
            case SERVER_CLOSED -> "Verbindung verloren: Server geschlossen.";
            case CLIENT_CLOSED -> "Verbindung verloren: Spiel verlassen";
            case KICKED -> "Verbindung verloren: Von anderem Spieler gekickt";
            case SERVER_FULL -> "Verbindung verloren: Der Server ist voll.";
            case GAME_RUNNING -> "Verbindung verloren: Das Spiel wurde schon gestartet.";
            default -> "Verbindung verloren: Keine weiteren Informationen.";
        };
        new Thread(() -> JOptionPane.showMessageDialog(display, s, "Verbindung verloren: " + client.player(), JOptionPane.WARNING_MESSAGE)).start();
    }
}
