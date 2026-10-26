package disaster.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Backward-compatible entrypoint for Emergency SOS.
 * Forwards directly to EmergencySOSDialog with 3-second auto-dispatch countdown.
 */
public class SOSFrame extends BaseFrame {

    public SOSFrame() {
        this("Demo User");
    }

    public SOSFrame(String userName) {
        super("Emergency SOS - Priority Distress Dispatch", 520, 380);
        setVisible(false);
        SwingUtilities.invokeLater(() -> {
            dispose();
            Window parent = DashboardFrame.getActiveInstance();
            EmergencySOSDialog.showSOS(parent, userName);
        });
    }
}