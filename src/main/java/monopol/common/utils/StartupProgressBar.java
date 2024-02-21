package monopol.common.utils;

import javax.swing.*;
import java.awt.*;

public class StartupProgressBar {
    public final JFrame barFrame = new JFrame();
    public final JProgressBar topBar = new JProgressBar();
    public final JProgressBar bottomBar = new JProgressBar();

    public StartupProgressBar(String title, int topMax, int bottomMax) {
        barFrame.setLayout(null);
        barFrame.setTitle("Version-Checker");
        barFrame.setLocationRelativeTo(null);
        barFrame.setLocation((int) (JUtils.SCREEN_WIDTH/2d-150), (int) (JUtils.SCREEN_HEIGHT/2d-300));
        barFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        barFrame.setSize(300, 200);

        topBar.setStringPainted(true);
        topBar.setMaximum(topMax);
        bottomBar.setStringPainted(true);
        bottomBar.setMaximum(bottomMax);

        topBar.setBounds(0, 0, 300, 80);
        barFrame.add(topBar, BorderLayout.CENTER);
        bottomBar.setBounds(0, 80, 300, 80);
        barFrame.add(bottomBar, BorderLayout.CENTER);
        barFrame.setVisible(true);
    }

    public void close() {
        barFrame.setVisible(false);
        barFrame.dispose();
    }

    public void setTop(String string, int progress) {
        topBar.setString(string);
        topBar.setValue(progress);
    }

    public void setTop(int progress) {
        topBar.setValue(progress);
    }

    public void setBottom(String string, int progress) {
        bottomBar.setString(string);
        bottomBar.setValue(progress);
    }

    public void setBottom(int progress) {
        bottomBar.setValue(progress);
    }
}
