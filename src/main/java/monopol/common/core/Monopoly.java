package monopol.common.core;

import monopol.client.screen.PrototypeMenu;
import monopol.common.packets.Packets;
import monopol.common.utils.*;
import monopol.server.Server;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class Monopoly {
    public static final Monopoly INSTANCE = new Monopoly();
    private static StartupProgressBar bar;

    private Server server;
    private boolean serverEnabled;
    private GameState state;

    private Monopoly() {}

    private void startServer() {
        if(server == null) {
            try {
                server = new Server(25565, success -> serverEnabled = success);
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
    public boolean serverEnabled() {
        return serverEnabled;
    }

    public static void closeBar() {
        bar.close();
    }

    public static void main(String[] args) {
        for (int i = 0; i < args.length;) {
            i += arg(i, args);
        }

        System.out.println("Starting Monopoly...");
        bar = new StartupProgressBar(VersionChecker.title(), 5, 0);
        bar.hideBottom();
        bar.show();

        bar.setTop("Verbinde mit Github...", 0);
        System.out.println("Connecting to Github...");
        GitUtils.connect(bar);

        bar.setTop("Starte Issue-Reporter...", 1);
        System.out.println("Starting Issue-Reporter...");
        GitHubIssueReporter.register();

        bar.setTop("Suche nach Updates...", 2);
        System.out.println("Checking for updates...");
        boolean updated = VersionChecker.check(bar);

        if(!updated) {
            bar.setTop("Starte Server...", 3);
            bar.bottomBar.setVisible(false);
            System.out.println("Starting server...");
            Packets.register();
            INSTANCE.startServer();

            bar.setTop("Erstelle GUI...", 4);
            System.out.println("Creating GUI...");
            UIManager.getDefaults().put("Button.disabledText", Color.BLACK);
            PrototypeMenu menu = new PrototypeMenu();
            menu.prepareMenu();

            bar.setTop("Fertig!", 5);
            System.out.println("Done!");
        } else System.exit(1);
    }

    private static int arg(int pos, String[] args) {
        return switch (args[pos]) {
            case "-delete" -> {
                File file = new File(args[pos+1]);
                if(file.exists())
                    //noinspection ResultOfMethodCallIgnored
                    file.delete();
                yield 2;
            }
            case "-rename" -> {
                String source = System.getProperty("java.class.path");
                String dest = args[pos+1];
                try {
                    File file = new File(dest);
                    int i = 0;
                    while (file.exists() && i < 100 && !file.delete()) {
                        Thread.sleep(10);
                        i++;
                    }
                    Files.copy(Path.of(source), Path.of(dest));
                    Runtime.getRuntime().exec(new String[]{"java", "-jar", dest, "-delete", source});
                    System.exit(1);
                } catch (IOException | InterruptedException ignored) {}
                yield 2;
            }
            default -> 1;
        };
    }
}