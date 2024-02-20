package monopol.common.core;

import monopol.client.screen.PrototypeMenu;
import monopol.common.utils.*;
import monopol.server.Server;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class Monopoly {
    public static final Monopoly INSTANCE = new Monopoly();
    private Server server;
    private boolean serverEnabled;
    private GameState state;

    private Monopoly() {

    }

    private void startServer() {
        if(server == null) {
            try {
                server = new Server(25565, flag -> serverEnabled = flag);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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
        System.out.println("Starting Monopoly...");
        ProjectStructure.printProjectStructureAsTree(false);

        System.out.println("Initializing GitUtils...");
        GitUtils.connect();

        System.out.println("Registering Issue Reporter...");
        GitHubIssueReporter.register();

        System.out.println("Checking for updates...");
        boolean updated = VersionChecker.check();

        if(!updated) {
            System.out.println("Starting server...");
            INSTANCE.startServer();
            INSTANCE.state = GameState.MAIN_MENU;

            System.out.println("Creating GUI...");
            PrototypeMenu menu = new PrototypeMenu();

            System.out.println("Preparing main menu...");
            menu.prepareMenu();

            System.out.println("Done!");
        } else {
            System.out.println("Starting updated Monopoly...");
            System.exit(1);
        }

        if(args.length > 1 && args[0].equals("-updated")) {
            File file = new File(args[1]);
            if(file.exists() && JOptionPane.showConfirmDialog(null, "Möchtest du die alte Version\nvon Monopoly löschen?", "Version-Checker", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                file.delete();
            }
        }
    }
}