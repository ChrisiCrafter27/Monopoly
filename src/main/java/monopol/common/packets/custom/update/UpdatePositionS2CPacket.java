package monopol.common.packets.custom.update;

import monopol.client.Client;
import monopol.client.screen.RootPane;
import monopol.common.data.DataReader;
import monopol.common.data.DataWriter;
import monopol.common.packets.S2CPacket;
import monopol.common.data.Player;

import java.rmi.RemoteException;
import java.util.List;
import java.util.stream.Collectors;

public class UpdatePositionS2CPacket extends S2CPacket<UpdatePositionS2CPacket> {
    private final boolean anim;

    public UpdatePositionS2CPacket(boolean anim) {
        this.anim = anim;
    }

    @Override
    public void serialize(DataWriter writer) {
        writer.writeBool(anim);
    }

        public static UpdatePositionS2CPacket deserialize(DataReader reader) {
        return new UpdatePositionS2CPacket(reader.readBool());
    }

    @Override
    public void handleOnClient(Client client, RootPane display) {
        try {
            List<Player> players = client.serverMethod().getPlayers();
            display.playerDisplayPane.check(players.stream().map(Player::getName).collect(Collectors.toSet()));
            players.forEach(player -> {
                if(anim) display.playerDisplayPane.setPosWithAnim(player.getName(), player.getPosition(), player.getColor());
                else display.playerDisplayPane.setPos(player.getName(), player.getPosition(), player.getColor());
            });
        } catch (RemoteException e) {
            e.printStackTrace(System.err);
        }
    }
}
