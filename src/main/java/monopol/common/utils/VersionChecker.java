package monopol.common.utils;

import com.sun.tools.javac.Main;
import org.apache.commons.io.IOUtils;
import org.kohsuke.github.GHRepository;

import javax.swing.*;
import javax.swing.plaf.basic.BasicProgressBarUI;
import javax.swing.plaf.metal.MetalProgressBarUI;
import javax.swing.plaf.synth.SynthProgressBarUI;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class VersionChecker {
    private static JFrame barFrame;

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
                            removeFrame();
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
        removeFrame();
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
            return false;
        }
        return true;
    }

    private static void update(GHRepository repository) throws IOException {
        JProgressBar bar = createProgressbar();
        bar.setValue(0);
        bar.setString("Verbinde mit github...");
        InputStream in = new URL(repository.getFileContent("Monopoly-" + remoteVersion(repository) + ".jar").getDownloadUrl()).openStream();
        bar.setValue(1);
        bar.setString("Erstelle Zieldatei...");
        FileOutputStream out = new FileOutputStream("Monopoly-" + remoteVersion(repository) + ".jar");
        bar.setValue(2);
        bar.setString("Lade daten herunter...");
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        bar.setValue(3);
        bar.setString("Fertig!");
        in.close();
        out.close();
    }

    private static JProgressBar createProgressbar() {
        removeFrame();
        barFrame = new JFrame("Version-Checker");
        barFrame.setLocationRelativeTo(null);
        barFrame.setLocation((int) (JUtils.SCREEN_WIDTH/2d-150), (int) (JUtils.SCREEN_HEIGHT/2d-200));
        barFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        barFrame.setSize(300, 100);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setMaximum(3);

        barFrame.add(progressBar, BorderLayout.CENTER);
        barFrame.setVisible(true);

        return progressBar;
    }

    private static void removeFrame() {
        if(barFrame != null) {
            barFrame.setVisible(false);
            barFrame.dispose();
            barFrame = null;
        }
    }
}
