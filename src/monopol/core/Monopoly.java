package monopol.core;

import monopol.annotations.AnnotationManager;
import monopol.annotations.Autostart;
import monopol.data.GameData;
import monopol.screen.PrototypeMenu;
import monopol.server.Server;
import monopol.server.ServerPlayer;
import monopol.server.ServerSettings;
import monopol.utils.ProjectStructure;

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
            for(ServerPlayer serverPlayer : server.getServerPlayers()) {
                list.add(serverPlayer.getName());
            }
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        return list;
    }
    public void closeServer() {
        server.close();
    }

    public static void main(String[] args) {
        INSTANCE.state = GameState.MAIN_MENU;
        AnnotationManager.setup();
        PrototypeMenu menu = new PrototypeMenu();
        menu.prepareMenu();
    }

    @Autostart
    public static void printStartupInfo() {
        System.out.println("Starting Monopoly...");
        ProjectStructure.printProjectStructureAsTree(false);
        System.out.println("Done!");
    }
}