package disaster;

import disaster.backend.SdrpServer;
import disaster.ui.DashboardFrame;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        // High-fidelity antialiasing for text and vector rendering
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        // Start SDRP backend REST server (Soulghost9/backend) in background
        new Thread(() -> {
            SdrpServer.startIfNotRunning(8085);
        }, "SDRP-Startup").start();

        SwingUtilities.invokeLater(() -> {
            new DashboardFrame().setVisible(true);
        });
    }
}