package disaster.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SOSFrame extends BaseFrame {

    private final String userName;
    private JLabel statusLabel;
    private JRadioButton medicalRadio;
    private JRadioButton floodRadio;
    private JRadioButton landslideRadio;
    private JRadioButton trappedRadio;

    public SOSFrame() {
        this("Demo User");
    }

    public SOSFrame(String userName) {
        super("Emergency SOS - Priority Distress Dispatch", 640, 580);
        this.userName = userName;
        buildUI();
    }

    private void buildUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 16));
        mainPanel.setBackground(UITheme.BACKGROUND);
        mainPanel.setBorder(new EmptyBorder(22, 26, 22, 26));

        // Header Block
        JPanel header = createStandardHeader(
                "Emergency SOS Distress Dispatch",
                "Direct signal transmission to Kerala State Disaster Management Authority",
                "PRIORITY DESK READY",
                UITheme.BRAND_CRIMSON
        );
        mainPanel.add(header, BorderLayout.NORTH);

        // Center Card
        JPanel card = createCard(8);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(20, 22, 20, 22));

        JLabel info = new JLabel("<html><body>"
                + "<b>Critical Notice:</b> Trigger this emergency broadcast only during active life hazards.<br>"
                + "Your signal and live satellite coordinates will be prioritized on rescue consoles."
                + "</body></html>");
        info.setFont(UITheme.NORMAL_FONT);
        info.setForeground(UITheme.SECONDARY_TEXT);
        info.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(info);

        card.add(Box.createVerticalStrut(16));

        JLabel selectLbl = new JLabel("Select Emergency Category:");
        selectLbl.setFont(UITheme.BOLD_FONT);
        selectLbl.setForeground(UITheme.TEXT);
        selectLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(selectLbl);

        card.add(Box.createVerticalStrut(6));

        // Radio group
        JPanel radioPanel = new JPanel(new GridLayout(2, 2, 10, 8));
        radioPanel.setOpaque(false);
        radioPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 65));

        medicalRadio = new JRadioButton("Critical Medical Emergency", true);
        floodRadio = new JRadioButton("Reservoir Spill / Flash Flood");
        landslideRadio = new JRadioButton("Landslide / Ghat Mudflow");
        trappedRadio = new JRadioButton("Trapped in Debris / House Inundated");

        medicalRadio.setFont(UITheme.NORMAL_FONT);
        floodRadio.setFont(UITheme.NORMAL_FONT);
        landslideRadio.setFont(UITheme.NORMAL_FONT);
        trappedRadio.setFont(UITheme.NORMAL_FONT);

        medicalRadio.setOpaque(false);
        floodRadio.setOpaque(false);
        landslideRadio.setOpaque(false);
        trappedRadio.setOpaque(false);

        ButtonGroup group = new ButtonGroup();
        group.add(medicalRadio);
        group.add(floodRadio);
        group.add(landslideRadio);
        group.add(trappedRadio);

        radioPanel.add(medicalRadio);
        radioPanel.add(floodRadio);
        radioPanel.add(landslideRadio);
        radioPanel.add(trappedRadio);

        radioPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(radioPanel);

        card.add(Box.createVerticalStrut(18));

        // GPS Telemetry Strip
        JPanel telemetryBox = new JPanel(new GridLayout(1, 3, 10, 0));
        telemetryBox.setBackground(UITheme.PANEL_BG);
        telemetryBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1),
                new EmptyBorder(8, 14, 8, 14)
        ));
        telemetryBox.setMaximumSize(new Dimension(Short.MAX_VALUE, 44));

        JLabel latLbl = new JLabel("Lat: 9.9312° N");
        latLbl.setFont(UITheme.CODE_FONT);
        latLbl.setForeground(UITheme.TEXT);

        JLabel lonLbl = new JLabel("Lon: 76.2673° E");
        lonLbl.setFont(UITheme.CODE_FONT);
        lonLbl.setForeground(UITheme.TEXT);

        JLabel accLbl = new JLabel("GPS Precision: ±2.4m");
        accLbl.setFont(UITheme.CODE_FONT);
        accLbl.setForeground(UITheme.SUCCESS);

        telemetryBox.add(latLbl);
        telemetryBox.add(lonLbl);
        telemetryBox.add(accLbl);
        telemetryBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(telemetryBox);

        card.add(Box.createVerticalStrut(18));

        // Primary SOS Button
        JButton sosBtn = button("🚨 TRANSMIT EMERGENCY SOS BROADCAST", UITheme.BRAND_CRIMSON);
        sosBtn.setPreferredSize(new Dimension(320, 46));
        sosBtn.setMaximumSize(new Dimension(Short.MAX_VALUE, 46));
        sosBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sosBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        sosBtn.addActionListener(e -> sendSOS());
        card.add(sosBtn);

        card.add(Box.createVerticalStrut(12));

        statusLabel = new JLabel("Status: Ready to broadcast • Connected to State Control Room (1070)");
        statusLabel.setFont(UITheme.NORMAL_FONT);
        statusLabel.setForeground(UITheme.SECONDARY_TEXT);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(statusLabel);

        JScrollPane scroll = createStandardScrollPane(card);
        mainPanel.add(scroll, BorderLayout.CENTER);

        // Bottom Action Bar
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottom.setOpaque(false);

        JButton backBtn = createBackButton("← Back to Portal");
        backBtn.setPreferredSize(new Dimension(150, 38));
        bottom.add(backBtn);

        mainPanel.add(bottom, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void sendSOS() {
        String nature = "Critical Medical Emergency";
        if (floodRadio.isSelected()) nature = "Reservoir Spill / Flash Flood";
        else if (landslideRadio.isSelected()) nature = "Landslide / Ghat Mudflow";
        else if (trappedRadio.isSelected()) nature = "Trapped in Debris / House Inundated";

        int result = JOptionPane.showConfirmDialog(
                this,
                "🚨 CONFIRM IMMEDIATE SOS BROADCAST?\n\n"
                        + "Emergency Category: " + nature + "\n"
                        + "Transmitting User: " + userName + "\n"
                        + "GPS Coordinates: 9.9312° N, 76.2673° E (Kerala Relief Grid)\n\n"
                        + "This transmits priority alert to NDRF & State Emergency Center.",
                "Confirm Emergency Dispatch",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (result == JOptionPane.YES_OPTION) {
            String activeUserId = "1";
            if (disaster.service.UserSession.isLoggedIn() && disaster.service.UserSession.getCurrentUser() != null) {
                activeUserId = disaster.service.UserSession.getCurrentUser().getUserId();
            }

            String sosId = "SOS-" + (System.currentTimeMillis() % 100000);
            disaster.model.SOSAlert directAlert = new disaster.model.SOSAlert(
                    sosId,
                    activeUserId,
                    9.9312,
                    76.2673,
                    "CRITICAL",
                    nature
            );
            System.out.println("🚨 [SOSFrame] Broadcasting SOS for User #" + activeUserId + " (" + userName + ")...");
            disaster.dao.SOSDAO sosDAO = new disaster.dao.SOSDAO();
            boolean dbSuccess = sosDAO.saveSOSAlert(directAlert);
            if (!dbSuccess) {
                dbSuccess = disaster.backend.DatabaseManager.getInstance().saveSOSAlert(directAlert);
            }
            if (directAlert.getSosId() != null) {
                sosId = directAlert.getSosId();
            }

            // 2. Also notify background SDRP service
            try {
                disaster.service.ApiClient.createSOS(activeUserId, 9.9312, 76.2673, "CRITICAL", nature);
            } catch (Exception ignored) {}

            String backendNote = dbSuccess ? "(Saved to disaster_db)" : "(Local)";

            boolean isXampp = disaster.backend.DatabaseManager.getInstance().isUsingXamppMySQL();
            String dbStatus = isXampp
                    ? "✓ Saved in XAMPP MySQL disaster_db: sos_requests, gps_locations & rescue_status"
                    : "ℹ Stored locally (XAMPP MySQL offline)";

            statusLabel.setText("🚨 DISTRESS SIGNAL ACTIVE • ID: #" + sosId + " " + backendNote);
            statusLabel.setForeground(UITheme.BRAND_CRIMSON);

            JOptionPane.showMessageDialog(
                    this,
                    "EMERGENCY BROADCAST CONFIRMED!\n\n"
                            + "Dispatch ID: #" + sosId + "\n"
                            + "Database Status: " + dbStatus + "\n"
                            + "Category: " + nature + "\n"
                            + "User: " + userName + " (User ID: " + activeUserId + ")\n"
                            + "GPS Coordinates: 9.9312° N, 76.2673° E\n"
                            + "Priority Unit: Kerala Fire & Rescue + NDRF 04 Battalion\n"
                            + "Estimated Time of Arrival (ETA): 5-7 minutes\n\n"
                            + "Move to higher ground if possible and stay in safe position.",
                    "SOS Signal Transmitted",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }
}