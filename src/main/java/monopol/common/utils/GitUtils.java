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

    public static GitHub connect() {
        if(connection == null) {
            try {
                URL url = new URL("https://raw.githubusercontent.com/ChrisiCrafter27/MonopolyIssueTrackerKey/main/token.txt");
                URLConnection c = url.openConnection();
                InputStream i = c.getInputStream();
                String s = IOUtils.toString(i, StandardCharsets.UTF_8);
                String token = IOUtils.toString(url.openConnection().getInputStream(), StandardCharsets.UTF_8).replace("#", "").replace("\n", "");
                token = "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";
                connection = GitHub.connectUsingOAuth(token);
                monopolyRepository();
                jarRepository();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, e.getMessage());
                throw new RuntimeException(e);
            }
        }
        return connection;
    }

    public static GHRepository monopolyRepository() throws IOException {
        return connect().getRepository(REPOSITORY_OWNER + "/" + REPOSITORY_NAME);
    }

    public static GHRepository jarRepository() throws IOException {
        return connect().getRepository(REPOSITORY_OWNER + "/Jars");
    }
}
