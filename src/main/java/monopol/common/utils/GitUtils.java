package monopol.common.utils;

import org.apache.commons.io.IOUtils;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

public class GitUtils {
    public static final String REPOSITORY_OWNER = "ChrisiCrafter27";
    public static final String REPOSITORY_NAME = "Monopoly";

    private static GitHub connection;
    private static boolean connected = false;

    public static void connect(StartupProgressBar bar) {
        bar.bottomBar.setMaximum(7);
        if(connection == null) {
            try {
                bar.setBottom("Erstelle Link...", 0);
                URL url = new URL("https://raw.githubusercontent.com/ChrisiCrafter27/MonopolyIssueTrackerKey/main/token.txt");
                bar.setBottom("Öffne Verbindung...", 1);
                URLConnection c = url.openConnection();
                bar.setBottom("Öffne Stream...", 2);
                InputStream i = c.getInputStream();
                bar.setBottom("Erstelle Token...", 3);
                String token = IOUtils.toString(i, StandardCharsets.UTF_8).replace("#", "").replace("\n", "");
                bar.setBottom("Verbinde mit Token...", 4);
                connection = GitHub.connectUsingOAuth(token);
                bar.setBottom("Suche nach Monopoly Repo...", 5);
                monopolyRepository();
                bar.setBottom("Suche nach Jars Repo...", 6);
                jarRepository();
                connected = true;
            } catch (Exception ignored) {}
        }
        bar.hideBottom();
    }

    public static boolean connected() {
        return connected;
    }

    public static GHRepository monopolyRepository() throws IOException {
        return connection.getRepository(REPOSITORY_OWNER + "/" + REPOSITORY_NAME);
    }

    public static GHRepository jarRepository() throws IOException {
        return connection.getRepository(REPOSITORY_OWNER + "/Jars");
    }
}
