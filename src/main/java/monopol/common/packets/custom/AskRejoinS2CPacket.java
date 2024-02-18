package monopol.common.packets.custom;

import monopol.client.Client;
import monopol.client.screen.RootPane;
import monopol.common.packets.PacketManager;
import monopol.common.packets.S2CPacket;

import javax.swing.*;
import java.util.List;

public class AskRejoinS2CPacket extends S2CPacket<AskRejoinS2CPacket> {
    private final List<String> names;

    public AskRejoinS2CPacket(List<String> names) {
        this.names = names;
    }

    @SuppressWarnings("unused")
    public static AskRejoinS2CPacket deserialize(Object[] objects) {
        return new AskRejoinS2CPacket((List<String>) objects[0]);
    }

    @Override
    public Object[] serialize() {
        return new Object[]{names};
    }

    @Override
    public void handleOnClient(Client client, RootPane display) {
        if(client.requestRejoin != null) {
            PacketManager.sendC2S(new RequestRejoinC2SPacket(client.requestRejoin), client, Throwable::printStackTrace);
            client.requestRejoin = null;
            return;
        }
        int result1 = JOptionPane.showConfirmDialog(display, "Ein Spieler hat die Verbindung verloren.\nBist du dieser Spieler und möchtest wieder beitreten?", "Rejoin Server", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        int result2;
        if (result1 == JOptionPane.YES_OPTION)
            if (names.size() > 1)
                result2 = JOptionPane.showOptionDialog(display, "Welcher Spieler bist du?", "Rejoin Server", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, names.toArray(), null);
            else result2 = 0;
        else result2 = JOptionPane.CLOSED_OPTION;
        String name = result2 == JOptionPane.CLOSED_OPTION ? null : names.get(result2);
        PacketManager.sendC2S(new RequestRejoinC2SPacket(name), client, Throwable::printStackTrace);
    }
}
