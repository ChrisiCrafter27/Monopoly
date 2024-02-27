package monopol.common.utils;

import com.sun.tools.javac.Main;
import org.apache.commons.io.IOUtils;
import org.kohsuke.github.GHRepository;

import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class VersionChecker {
    public static boolean check(StartupProgressBar bar) {
        if(!GitUtils.connected()) return false;
        try {
            GHRepository repository = GitUtils.jarRepository();
            if(inIdea()) {
                if(!upToDate(repository)) JOptionPane.showMessageDialog(null, "Du bist in einer Entwicklungsumgebung.\nAutomatische Updates sind daher deaktiviert.", "Version-Checker", JOptionPane.INFORMATION_MESSAGE);
            } else  {
                if(!upToDate(repository)) {
                    if(JOptionPane.showConfirmDialog(null, "Eine neue Version von Monopoly ist verfügbar.\nMöchtest du sie herunterladen?\nDeine Version: " + version() + "\nNeueste Version: " + remoteVersion(repository), "Version-Checker", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                        File destFile = new File("Monopoly-" + remoteVersion(repository) + ".jar");
                        try {
                            update(repository, bar);
                        } catch (Exception e) {
                            e.printStackTrace(System.out);
                            if(destFile.exists()) destFile.delete();
                        }
                        if(destFile.exists()) {
                            if(System.getProperty("java.class.path").endsWith("Monopoly-" + version() + ".jar"))
                                Runtime.getRuntime().exec(new String[]{"java", "-jar", "Monopoly-" + remoteVersion(repository) + ".jar", "-delete", System.getProperty("java.class.path")});
                            else
                                Runtime.getRuntime().exec(new String[]{"java", "-jar", "Monopoly-" + remoteVersion(repository) + ".jar", "-rename", System.getProperty("java.class.path")});
                            return true;
                        } else {
                            JOptionPane.showMessageDialog(null, "Update nicht erfolgreich", "Version-Checker", JOptionPane.WARNING_MESSAGE);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to check for/install update:");
            e.printStackTrace(System.err);
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

    public static String title() {
        try {
            return "Monopoly " + version();
        } catch (IOException e) {
            return "Monopoly";
        }
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
            return false;
        }
        return true;
    }

    private static void update(GHRepository repository, StartupProgressBar bar) throws IOException {
        bar.bottomBar.setMaximum(3);
        bar.setBottom("Verbinde mit github...", 0);
        InputStream in = new URL(repository.getFileContent("Monopoly-" + remoteVersion(repository) + ".jar").getDownloadUrl()).openStream();
        bar.setBottom("Erstelle Zieldatei...", 1);
        FileOutputStream out = new FileOutputStream("Monopoly-" + remoteVersion(repository) + ".jar");
        bar.setBottom("Lade daten herunter...", 2);
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        bar.setBottom("Fertig!", 3);
        in.close();
        out.close();
    }
}
