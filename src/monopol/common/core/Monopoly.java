package monopol.common.core;

import monopol.common.data.GameData;
import monopol.client.screen.PrototypeMenu;
import monopol.server.Server;
import monopol.common.Player;
import monopol.server.ServerSettings;
import monopol.common.utils.ProjectStructure;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class Monopoly {
    public static final Monopoly INSTANCE = new Monopoly();
    public static final GameData GAME_DATA = new GameData();
    private final Server server;
    GameState state;

    private Monopoly() {
        try {
            server = new Server(25565);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setState(GameState gameState) {
        state = gameState;
    }
    public GameState getState() {
        return state;
    }
    public void openServer(ServerSettings settings) {
        server.open(settings);
    }
    public void setHost(String name) {
        server.setHost(name);
    }
    public ArrayList<String> getAllPlayerNamesOfOwnServer() {
        ArrayList<String> list = new ArrayList<>();
        try {
            for(Player player : server.getPlayers()) {
                list.add(player.getName());
            }
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        return list;
    }
    public Server server() {
        return server;
    }
    public void closeServer() {
        server.close();
    }

    public static void main(String[] args) {
        INSTANCE.state = GameState.MAIN_MENU;
        printStartupInfo();
        PrototypeMenu menu = new PrototypeMenu();
        menu.prepareMenu();
    }

    public static void printStartupInfo() {
        System.out.println("Starting Monopoly...");
        ProjectStructure.printProjectStructureAsTree(false);
        System.out.println("Done!");
    }
}