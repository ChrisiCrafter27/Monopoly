package monopol.common.utils;

import org.apache.commons.io.IOUtils;
import org.kohsuke.github.*;

import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

public class GitHubIssueReporter implements Thread.UncaughtExceptionHandler {
    private static boolean registered = false;
    private static final String REPOSITORY_OWNER = "ChrisiCrafter27";
    private static final String REPOSITORY_NAME = "Monopoly";

    private GitHubIssueReporter() {
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        System.err.print("Exception in thread \""
                + t.getName() + "\" ");
        e.printStackTrace(System.err);
        int result = JOptionPane.showOptionDialog(null, "Ein unerwarteter Fehler ist aufgetreten:\n" + e.getMessage() + "\nEs kann sein, dass das Spiel nun nicht mehr\nwie erwartet funktioniert.", "Fehler", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, new String[]{"Melden und schließen", "Melden und ignorieren", "Informationen hinzufügen"}, null);
        String info = "";
        if(result == 2) {
            info = JOptionPane.showInputDialog(null, "Wann ist der Fehler passiert?", "Fehler", JOptionPane.QUESTION_MESSAGE);
        }
        report(e, info);
        if(result == 0) System.exit(1);
    }

    private static String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    public static void report(Throwable exception, String userInfo) {
        report("Fehlerbericht: " + exception.getMessage(), "Ein unbehandelter Fehler ist aufgetreten:\n" + getStackTrace(exception) + "\nNutzerinformationen:\n" + userInfo);
    }

    public static void report(String title, String body) {
        try {
            URL url = new URL("https://raw.githubusercontent.com/ChrisiCrafter27/MonopolyIssueTrackerKey/main/token.txt");
            URLConnection connection = url.openConnection();
            String token = IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8).replace("#", "").replace("\n", "");
            GitHub github = GitHub.connectUsingOAuth(token);
            GHRepository repository = github.getRepository(REPOSITORY_OWNER + "/" + REPOSITORY_NAME);
            GHIssue issue = repository.createIssue(title)
                    .body(body)
                    .create();
            JOptionPane.showMessageDialog(null, "Ein neuer GitHub-Issue wurde erstellt:\n" + issue.getHtmlUrl(), "Fehler melden", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(null, "Ein GitHub-Issue konnte nicht erstellt werden:\n" + ioe.getMessage(), "Fehler melden", JOptionPane.WARNING_MESSAGE);
            ioe.printStackTrace(System.out);
        }
    }

    public static void register() {
        if(!registered) new GitHubIssueReporter();
        registered = true;
    }
}
