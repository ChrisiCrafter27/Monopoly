package monopol.common.utils;

import com.sun.tools.javac.Main;
import org.apache.commons.io.IOUtils;
import org.kohsuke.github.GHRepository;

import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class VersionChecker {
    public static boolean check() {
        if(inIdea()) {
            JOptionPane.showMessageDialog(null, "Du bist in einer Entwicklungsumgebung.\nAutomatische Updates sind daher deaktiviert.", "Version-Checker", JOptionPane.INFORMATION_MESSAGE);
        } else  {
            try {
                GHRepository repository = GitUtils.jarRepository();
                if(!upToDate(repository)) {
                    if(JOptionPane.showConfirmDialog(null, "Eine neue Version von Monopoly ist verfügbar.\nMöchtest du sie herunterladen?\nDeine Version: " + version() + "\nNeueste Version: " + remoteVersion(repository), "Version-Checker", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                        File destFile = new File("Monopoly-" + remoteVersion(repository) + ".jar");
                        try {
                            update(repository);
                        } catch (Exception e) {
                            e.printStackTrace(System.out);
                            if(destFile.exists()) destFile.delete();
                        }
                        if(destFile.exists()) {
                            JOptionPane.showMessageDialog(null, "Update erfolgreich.\n" + destFile.getName() + " wird gestartet.", "Version-Checker", JOptionPane.INFORMATION_MESSAGE);
                            Runtime.getRuntime().exec(new String[]{"java", "-jar", "Monopoly-" + remoteVersion(repository) + ".jar", "-updated", "Monopoly-" + version() + ".jar"});
                            return true;
                        } else {
                            JOptionPane.showMessageDialog(null, "Update nicht erfolgreich", "Version-Checker", JOptionPane.WARNING_MESSAGE);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        }
        return false;
    }

    private static boolean inIdea() {
        return new File(".idea").exists();
    }

    private static String remoteVersion(GHRepository repository) throws IOException {
        return IOUtils.toString(repository.getFileContent("monopoly_version.txt").read(), StandardCharsets.UTF_8).replace("\n", "");
    }

    private static String version() throws IOException {
        return IOUtils.toString(Main.class.getClassLoader().getResourceAsStream("assets/version.txt"), StandardCharsets.UTF_8);
    }

    private static boolean upToDate(GHRepository repository) throws IOException {
        String remoteVersionString = remoteVersion(repository);
        String versionString = version();
        double remoteVersion = Double.parseDouble(remoteVersionString);
        double version = Double.parseDouble(versionString);
        return remoteVersion <= version || !fileExists(repository, "Monopoly-" + remoteVersionString + ".jar");
    }

    private static boolean fileExists(GHRepository repository, String path) {
        try {
            repository.getFileContent(path);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static void update(GHRepository repository) throws IOException {
        InputStream in = new URL(repository.getFileContent("Monopoly-" + remoteVersion(repository) + ".jar").getDownloadUrl()).openStream();
        FileOutputStream out = new FileOutputStream("Monopoly-" + remoteVersion(repository) + ".jar");
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        in.close();
        out.close();
    }
}
