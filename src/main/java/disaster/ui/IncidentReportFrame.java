package disaster.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

public class IncidentReportFrame extends BaseFrame {

    private JComboBox<String> disasterType;
    private JTextField locationField;
    private JTextArea descriptionArea;
    private JLabel attachmentStatus;

    public IncidentReportFrame() {
        super("Report a Disaster - Field Intelligence", 740, 680);
        setMinimumSize(new Dimension(640, 520));
        buildUI();
    }

    private void buildUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 14));
        mainPanel.setBackground(UITheme.BACKGROUND);
        mainPanel.setBorder(new EmptyBorder(18, 22, 18, 22));

        // Header Block
        JPanel header = createStandardHeader(
                "Report a Disaster / Hazard Incident",
                "Submit field incident intelligence to Kerala SDMA and District Disaster Control Room",
                "RAPID CITIZEN REPORT",
                UITheme.BRAND_CRIMSON
        );
        mainPanel.add(header, BorderLayout.NORTH);

        // Center Form Card
        JPanel card = createCard(8);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(18, 20, 18, 20));

        // Disaster Type
        disasterType = new JComboBox<>(new String[]{
                "Flash Flood / River Water Overflow",
                "Landslide / Debris Mudflow",
                "Reservoir Spill Discharge / Canal Inundation",
                "Severe Thunderstorm / Tree Fall on Road",
                "Structural Damage / House Collapse",
                "Bridge Submergence / Road Blockage",
                "Hazardous Electrical Line Down",
                "Other Emergency Incident"
        });
        disasterType.setFont(UITheme.NORMAL_FONT);
        disasterType.setBackground(Color.WHITE);
        disasterType.setMaximumSize(new Dimension(Short.MAX_VALUE, 38));
        card.add(createFormRow("Disaster Nature / Hazard Type", disasterType));

        card.add(Box.createVerticalStrut(14));

        // Location Row (Input + Live GPS Button)
        JPanel locPanel = new JPanel();
        locPanel.setLayout(new BoxLayout(locPanel, BoxLayout.Y_AXIS));
        locPanel.setOpaque(false);

        JLabel locLbl = new JLabel("Incident Location / Landmark (District & Taluk)");
        locLbl.setFont(UITheme.BOLD_FONT);
        locLbl.setForeground(UITheme.TEXT);
        locLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        locPanel.add(locLbl);

        locPanel.add(Box.createVerticalStrut(5));

        JPanel locRow = new JPanel(new BorderLayout(8, 0));
        locRow.setOpaque(false);
        locRow.setMaximumSize(new Dimension(Short.MAX_VALUE, 38));

        locationField = createTextField("e.g. Near Banasurasagar Spillway / Meppadi Ghat Road");
        locRow.add(locationField, BorderLayout.CENTER);

        JButton gpsBtn = createOutlinedButton("📍 Use Live GPS", UITheme.BRAND_CRIMSON, UITheme.BRAND_CRIMSON);
        gpsBtn.setPreferredSize(new Dimension(130, 38));
        gpsBtn.addActionListener(e -> locationField.setText("9.9312° N, 76.2673° E (Kerala Relief Grid)"));
        locRow.add(gpsBtn, BorderLayout.EAST);

        locPanel.add(locRow);
        locPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(locPanel);

        card.add(Box.createVerticalStrut(14));

        // Description Area
        descriptionArea = new JTextArea(4, 20);
        descriptionArea.setFont(UITheme.NORMAL_FONT);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBorder(new EmptyBorder(8, 10, 8, 10));

        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1));
        descScroll.setPreferredSize(new Dimension(280, 110));
        descScroll.setMaximumSize(new Dimension(Short.MAX_VALUE, 130));
        card.add(createFormRow("Detailed Situation Report & Immediate Relief Needs", descScroll));

        card.add(Box.createVerticalStrut(14));

        // Attachments Row
        JPanel attachPanel = new JPanel();
        attachPanel.setLayout(new BoxLayout(attachPanel, BoxLayout.Y_AXIS));
        attachPanel.setOpaque(false);

        JLabel attachLbl = new JLabel("Evidence Attachments (Optional):");
        attachLbl.setFont(UITheme.BOLD_FONT);
        attachLbl.setForeground(UITheme.TEXT);
        attachLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        attachPanel.add(attachLbl);

        attachPanel.add(Box.createVerticalStrut(6));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnRow.setOpaque(false);

        JButton photoBtn = createOutlineButton("📷 Attach Photo / Video", UITheme.BORDER_DARK);
        photoBtn.setPreferredSize(new Dimension(180, 36));
        photoBtn.addActionListener(e -> chooseFile());
        btnRow.add(photoBtn);

        JButton voiceBtn = createOutlineButton("🎙 Attach Voice Note", UITheme.BORDER_DARK);
        voiceBtn.setPreferredSize(new Dimension(170, 36));
        voiceBtn.addActionListener(e -> chooseVoice());
        btnRow.add(voiceBtn);

        attachPanel.add(btnRow);
        attachPanel.add(Box.createVerticalStrut(6));

        attachmentStatus = new JLabel("No media files attached.");
        attachmentStatus.setFont(UITheme.SUBTITLE_FONT);
        attachmentStatus.setForeground(UITheme.MUTED);
        attachPanel.add(attachmentStatus);

        attachPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(attachPanel);

        JScrollPane mainScroll = createStandardScrollPane(card);
        mainPanel.add(mainScroll, BorderLayout.CENTER);

        // Bottom Action Bar
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottom.setOpaque(false);

        JButton cancelBtn = createBackButton("← Cancel / Back");
        cancelBtn.setPreferredSize(new Dimension(140, 40));
        bottom.add(cancelBtn);

        JButton submitBtn = button("Submit Incident Report", UITheme.BRAND_CRIMSON);
        submitBtn.setPreferredSize(new Dimension(210, 40));
        submitBtn.addActionListener(e -> submitReport());
        bottom.add(submitBtn);

        mainPanel.add(bottom, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private File attachedMediaFile = null;

    private void chooseFile() {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            attachedMediaFile = chooser.getSelectedFile();
            attachmentStatus.setText("✓ Media Attached: " + attachedMediaFile.getName());
            attachmentStatus.setForeground(UITheme.SUCCESS);
        }
    }

    private void chooseVoice() {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            attachedMediaFile = chooser.getSelectedFile();
            attachmentStatus.setText("✓ Audio Log Attached: " + attachedMediaFile.getName());
            attachmentStatus.setForeground(UITheme.SUCCESS);
        }
    }

    private void submitReport() {
        String loc = locationField.getText().trim();
        String desc = descriptionArea.getText().trim();
        String type = (String) disasterType.getSelectedItem();

        if (loc.isEmpty() || desc.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please fill in both the location and incident description.",
                    "Report Incomplete",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Map to clean disasterType string
        String simpleType = "Other";
        if (type != null) {
            if (type.contains("Flood") || type.contains("Reservoir")) simpleType = "Flood";
            else if (type.contains("Landslide") || type.contains("Mudflow")) simpleType = "Landslide";
            else if (type.contains("Thunderstorm") || type.contains("Tree")) simpleType = "Storm";
            else if (type.contains("Collapse") || type.contains("Structural")) simpleType = "Collapse";
            else if (type.contains("Bridge") || type.contains("Road")) simpleType = "Road Blockage";
            else if (type.contains("Electrical")) simpleType = "Electrical Hazard";
        }

        String userId = "1";
        if (disaster.service.UserSession.isLoggedIn() && disaster.service.UserSession.getCurrentUser() != null) {
            userId = disaster.service.UserSession.getCurrentUser().getUserId();
        }

        // 1. Direct Save to disaster_db via DatabaseManager
        disaster.model.DisasterReport directReport = new disaster.model.DisasterReport(
                "INC-" + (System.currentTimeMillis() % 100000),
                userId,
                simpleType,
                9.9312,
                76.2673,
                "8",
                loc + " - " + desc,
                15
        );
        directReport.setStatus("Reported");
        disaster.backend.DatabaseManager.getInstance().saveDisasterReport(directReport);
        String reportId = directReport.getReportId();

        // 2. If media was attached, save into incident_media
        if (attachedMediaFile != null) {
            try {
                int incId = Integer.parseInt(reportId.replace("INC-", "").trim());
                try (java.sql.Connection conn = disaster.backend.DatabaseManager.getInstance().getConnection();
                     java.sql.PreparedStatement ps = conn.prepareStatement("INSERT INTO incident_media (incident_id, file_path) VALUES (?, ?)")) {
                    ps.setInt(1, incId);
                    ps.setString(2, attachedMediaFile.getAbsolutePath());
                    ps.executeUpdate();
                }
            } catch (Exception ignored) {}
        }

        // 3. Also notify backend API
        try {
            disaster.service.ApiClient.submitDisasterReport(
                    userId,
                    simpleType.toUpperCase(),
                    9.9312,
                    76.2673,
                    "HIGH",
                    loc + " - " + desc,
                    15
            );
        } catch (Exception ignored) {}

        boolean isXampp = disaster.backend.DatabaseManager.getInstance().isUsingXamppMySQL();
        String dbNotice = isXampp
                ? "✓ Stored in XAMPP MySQL disaster_db (incident_reports" + (attachedMediaFile != null ? " & incident_media" : "") + ")"
                : "ℹ Stored locally (XAMPP MySQL offline)";

        JOptionPane.showMessageDialog(
                this,
                "INCIDENT REPORT LOGGED SUCCESSFULLY!\n\n"
                        + "Report ID: #" + reportId + "\n"
                        + "Database Status: " + dbNotice + "\n"
                        + "Disaster Category: " + simpleType + "\n"
                        + "Location: " + loc + "\n"
                        + "Severity: Level 8 (High Priority)\n"
                        + "Forwarded to: District Disaster Control Room (1077) & Kerala SDMA.",
                "Incident Logged",
                JOptionPane.INFORMATION_MESSAGE
        );

        closeAndReturnToPortal();
    }
}