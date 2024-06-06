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
    public Component parentComponent;

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
        if(preStarting()) System.exit(1);

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

    private static boolean preStarting() {
        File delFile = new File(".delete.txt");
        if(delFile.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(delFile));
                File file = new File(reader.readLine());
                reader.close();
                if(!delFile.delete()) JOptionPane.showMessageDialog(null, "Du kannst " + delFile + " jetzt löschen.", "Monopoly Pre-Starting", JOptionPane.INFORMATION_MESSAGE);
                if(file.exists() && !file.delete()) JOptionPane.showMessageDialog(null, "Du kannst " + file.getName() + " jetzt löschen.", "Monopoly Pre-Starting", JOptionPane.INFORMATION_MESSAGE);
                return false;
            } catch (IOException ignored) {}
        }

        File nameFile = new File(".rename.txt");
        if(nameFile.exists()) {
            try {
                String source = System.getProperty("java.class.path");
                BufferedReader reader = new BufferedReader(new FileReader(nameFile));
                String dest = reader.readLine();
                reader.close();
                if(!nameFile.delete()) JOptionPane.showMessageDialog(null, "Du kannst " + nameFile + " jetzt löschen.", "Monopoly Pre-Starting", JOptionPane.INFORMATION_MESSAGE);
                File file = new File(dest);
                int i = 0;
                while (file.exists() && i < 100 && !file.delete()) {
                    Thread.sleep(10);
                    i++;
                }
                Files.copy(Path.of(source), Path.of(dest));
                File file1 = new File(".delete.txt");
                if(file1.createNewFile()) {
                    FileWriter writer = new FileWriter(file1);
                    writer.write(source);
                    writer.close();
                }
                JOptionPane.showMessageDialog(null, "Du kannst " + dest + " jetzt starten.", "Monopoly Pre-Starting", JOptionPane.INFORMATION_MESSAGE);
                return true;
            } catch (IOException | InterruptedException ignored) {}
        }

        return false;
    }
}