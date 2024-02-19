package monopol.common.core;

import monopol.client.screen.PrototypeMenu;
import monopol.common.utils.GitHubIssueReporter;
import monopol.common.utils.ProjectStructure;
import monopol.server.Server;
import monopol.common.utils.ServerSettings;

import java.io.IOException;

public class Monopoly {
    public static final Monopoly INSTANCE = new Monopoly();
    private final Server server;
    private boolean serverEnabled;
    private GameState state;

    private Monopoly() {
        try {
            server = new Server(25565, flag -> serverEnabled = flag);
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
    public Server server() {
        return server;
    }
    public void closeServer() {
        server.close();
    }
    public boolean serverEnabled() {
        return serverEnabled;
    }

    public static void main(String[] args) {
        GitHubIssueReporter.register();
        INSTANCE.state = GameState.MAIN_MENU;
        printStartupInfo();
        PrototypeMenu menu = new PrototypeMenu();
        menu.prepareMenu();

        new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            throw new RuntimeException("got you");
        }).start();
    }

    public static void printStartupInfo() {
        System.out.println("Starting Monopoly...");
        ProjectStructure.printProjectStructureAsTree(false);
        System.out.println("Done!");
    }
}