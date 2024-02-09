package monopol.common.packets.custom.update;

import monopol.client.Client;
import monopol.client.screen.RootPane;
import monopol.common.data.Player;
import monopol.common.packets.S2CPacket;

import java.rmi.RemoteException;
import java.util.List;
import java.util.stream.Collectors;

public class UpdatePositionS2CPacket extends S2CPacket<UpdatePositionS2CPacket> {
    private final boolean anim;

    public UpdatePositionS2CPacket(boolean anim) {
        this.anim = anim;
    }

    @Override
    public Object[] serialize() {
        return new Object[]{anim};
    }

    @SuppressWarnings("unused")
    public static UpdatePositionS2CPacket deserialize(Object[] objects) {
        return new UpdatePositionS2CPacket((boolean) objects[0]);
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
