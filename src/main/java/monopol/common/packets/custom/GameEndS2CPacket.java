package monopol.common.packets.custom;

import monopol.client.Client;
import monopol.client.screen.RootPane;
import monopol.common.data.DataReader;
import monopol.common.data.DataWriter;
import monopol.common.packets.S2CPacket;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class GameEndS2CPacket extends S2CPacket<GameEndS2CPacket> {
    private final Map<String, Integer> result;

    public GameEndS2CPacket(Map<String, Integer> result) {
        this.result = result;
    }

    @Override
    public void serialize(DataWriter writer) {
        writer.writeMap(result, DataWriter::writeString, DataWriter::writeInt);
    }

    public static GameEndS2CPacket deserialize(DataReader reader) {
        return new GameEndS2CPacket(reader.readMap(HashMap::new, DataReader::readString, DataReader::readInt));
    }

    @Override
    public void handleOnClient(Client client, RootPane display) {
        client.close();
        new Thread(() -> {
            StringBuilder builder = new StringBuilder("Endergebnis:");
            int i = 0;
            while(!result.isEmpty()) {
                i++;
                String name = null;
                int value = Integer.MIN_VALUE;
                for(Map.Entry<String, Integer> entry : result.entrySet()) {
                    if(entry.getValue() > value) {
                        name = entry.getKey();
                        value = entry.getValue();
                    }
                }
                result.remove(name);
                builder.append('\n').append(i).append(". ").append(name).append(" (").append(value).append("€)");
            }
            JOptionPane.showMessageDialog(display, builder.toString(), "Spielende", JOptionPane.INFORMATION_MESSAGE);
        }).start();
    }
}
