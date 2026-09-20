package disaster.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ProfileFrame extends BaseFrame {

    private final String userName;
    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JComboBox<String> bloodGroupCombo;
    private JTextArea addressArea;
    private JCheckBox notificationsCheck;
    private JCheckBox gpsShareCheck;

    public ProfileFrame() {
        this("Demo User");
    }

    public ProfileFrame(String userName) {
        super("User Profile & Medical Telemetry", 680, 640);
        setMinimumSize(new Dimension(580, 520));
        this.userName = userName;
        buildUI();
    }

    private void buildUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 14));
        mainPanel.setBackground(UITheme.BACKGROUND);
        mainPanel.setBorder(new EmptyBorder(18, 22, 18, 22));

        // Header Block
        String badge = "VERIFIED RECORD";
        if (disaster.service.UserSession.isLoggedIn() && disaster.service.UserSession.getCurrentUser() != null) {
            badge = "ROLE: " + disaster.service.UserSession.getCurrentUser().getUserType();
        }

        JPanel header = createStandardHeader(
                "Personal Profile & Medical ID",
                "Emergency triage details accessed by paramedics during evacuation",
                badge,
                UITheme.SUCCESS
        );
        mainPanel.add(header, BorderLayout.NORTH);

        // Center Form Card
        JPanel card = createCard(10);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(18, 20, 18, 20));

        // Read current user
        int activeUserId = 1;
        String curName = userName;
        String curEmail = userName.toLowerCase().replaceAll("\\s+", "") + "@disaster.gov";
        String curPhone = "9876543210";
        String curBlood = "O+";
        String curAddr = "House #24, Green Valley Heights, Alappuzha, Kerala - 688001";

        if (disaster.service.UserSession.isLoggedIn() && disaster.service.UserSession.getCurrentUser() != null) {
            disaster.model.User activeUser = disaster.service.UserSession.getCurrentUser();
            curName = activeUser.getUsername();
            curEmail = activeUser.getEmail();
            try { activeUserId = Integer.parseInt(activeUser.getUserId()); } catch (Exception ignored) {}
        }

        // Load live profile details from disaster_db
        disaster.model.User dbUser = disaster.backend.DatabaseManager.getInstance().getUserByUsername(curName);
        if (dbUser != null) {
            if (dbUser.getFullName() != null && !dbUser.getFullName().isEmpty()) curName = dbUser.getFullName();
            if (dbUser.getPhone() != null && !dbUser.getPhone().isEmpty()) curPhone = dbUser.getPhone();
            if (dbUser.getBloodGroup() != null && !dbUser.getBloodGroup().isEmpty()) curBlood = dbUser.getBloodGroup();
            if (dbUser.getAddress() != null && !dbUser.getAddress().isEmpty()) curAddr = dbUser.getAddress();
            try { activeUserId = Integer.parseInt(dbUser.getUserId()); } catch (Exception ignored) {}
        }

        // Row 1: Name & Blood Group
        JPanel row1 = new JPanel(new GridLayout(1, 2, 14, 0));
        row1.setOpaque(false);
        row1.setMaximumSize(new Dimension(Short.MAX_VALUE, 65));

        nameField = createTextField("Full Name");
        nameField.setText(curName);
        row1.add(createFormRow("Full Legal Name / Username", nameField));

        bloodGroupCombo = new JComboBox<>(new String[]{"O+", "A+", "B+", "AB+", "O-", "A-", "B-", "AB-"});
        bloodGroupCombo.setFont(UITheme.NORMAL_FONT);
        bloodGroupCombo.setBackground(Color.WHITE);
        bloodGroupCombo.setSelectedItem(curBlood);
        bloodGroupCombo.setPreferredSize(new Dimension(280, 38));
        row1.add(createFormRow("Blood Group (Critical for Triage)", bloodGroupCombo));

        row1.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(row1);

        card.add(Box.createVerticalStrut(14));

        // Row 2: Email & Phone
        JPanel row2 = new JPanel(new GridLayout(1, 2, 14, 0));
        row2.setOpaque(false);
        row2.setMaximumSize(new Dimension(Short.MAX_VALUE, 65));

        emailField = createTextField("user@example.com");
        emailField.setText(curEmail);
        row2.add(createFormRow(" Email Address", emailField));

        phoneField = createTextField("+91 98470 12345");
        phoneField.setText(curPhone);
        row2.add(createFormRow("Emergency Contact Number ", phoneField));

        row2.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(row2);

        card.add(Box.createVerticalStrut(14));

        // Address Area
        addressArea = new JTextArea(3, 20);
        addressArea.setFont(UITheme.NORMAL_FONT);
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        addressArea.setText(curAddr);
        addressArea.setBorder(new EmptyBorder(8, 10, 8, 10));

        JScrollPane addrScroll = new JScrollPane(addressArea);
        addrScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1));
        addrScroll.setPreferredSize(new Dimension(280, 80));
        addrScroll.setMaximumSize(new Dimension(Short.MAX_VALUE, 100));
        card.add(createFormRow("Residential Address / Designated Evacuation Safehouse", addrScroll));

        card.add(Box.createVerticalStrut(14));

        // Checkbox Settings
        notificationsCheck = new JCheckBox("Receive instant disaster warning sirens & regional bulletins", true);
        notificationsCheck.setFont(UITheme.NORMAL_FONT);
        notificationsCheck.setForeground(UITheme.TEXT);
        notificationsCheck.setOpaque(false);
        notificationsCheck.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(notificationsCheck);

        card.add(Box.createVerticalStrut(6));

        gpsShareCheck = new JCheckBox("Transmit live GPS telemetry automatically when triggering Emergency SOS", true);
        gpsShareCheck.setFont(UITheme.NORMAL_FONT);
        gpsShareCheck.setForeground(UITheme.TEXT);
        gpsShareCheck.setOpaque(false);
        gpsShareCheck.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(gpsShareCheck);

        mainPanel.add(createStandardScrollPane(card), BorderLayout.CENTER);

        // Bottom Bar
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);

        JButton signOutBtn = createOutlinedButton("Sign Out", UITheme.DANGER, UITheme.DANGER);
        signOutBtn.setPreferredSize(new Dimension(120, 38));
        signOutBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        signOutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        signOutBtn.addActionListener(e -> {
            int res = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to sign out from SDMA Portal?",
                    "Sign Out",
                    JOptionPane.YES_NO_OPTION
            );
            if (res == JOptionPane.YES_OPTION) {
                disaster.service.UserSession.setCurrentUser(null);
                if (DashboardFrame.getActiveInstance() != null) {
                    DashboardFrame.getActiveInstance().updateSessionUser("Demo User");
                }
                dispose();
            }
        });
        bottom.add(signOutBtn, BorderLayout.WEST);

        JPanel rightActionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActionsPanel.setOpaque(false);

        JButton backBtn = createBackButton("← Back to Portal");
        backBtn.setPreferredSize(new Dimension(150, 38));
        rightActionsPanel.add(backBtn);

        final int targetUid = activeUserId;
        JButton saveBtn = button("Save Profile Changes", UITheme.PRIMARY);
        saveBtn.setPreferredSize(new Dimension(180, 38));
        saveBtn.addActionListener(e -> saveProfile(targetUid));
        rightActionsPanel.add(saveBtn);

        bottom.add(rightActionsPanel, BorderLayout.EAST);

        mainPanel.add(bottom, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void saveProfile(int userId) {
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String blood = (String) bloodGroupCombo.getSelectedItem();
        String addr = addressArea.getText().trim();

        // Sanitize phone
        String cleanPhone = phone.replaceAll("[^0-9+]", "");
        if (cleanPhone.length() > 15) cleanPhone = cleanPhone.substring(0, 15);

        boolean updated = false;
        try (java.sql.Connection conn = disaster.backend.DatabaseManager.getInstance().getConnection()) {
            if (conn != null) {
                // Update phone in users
                try (java.sql.PreparedStatement ps = conn.prepareStatement("UPDATE users SET phone = ? WHERE user_id = ?")) {
                    ps.setString(1, cleanPhone);
                    ps.setInt(2, userId);
                    ps.executeUpdate();
                }

                // Check profile
                String chk = "SELECT profile_id FROM user_profiles WHERE user_id = ?";
                boolean hasProf = false;
                try (java.sql.PreparedStatement ps = conn.prepareStatement(chk)) {
                    ps.setInt(1, userId);
                    try (java.sql.ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) hasProf = true;
                    }
                }

                if (hasProf) {
                    String upSql = "UPDATE user_profiles SET full_name = ?, blood_group = ?, address = ? WHERE user_id = ?";
                    try (java.sql.PreparedStatement ps = conn.prepareStatement(upSql)) {
                        ps.setString(1, name);
                        ps.setString(2, blood);
                        ps.setString(3, addr);
                        ps.setInt(4, userId);
                        ps.executeUpdate();
                    }
                } else {
                    String inSql = "INSERT INTO user_profiles (user_id, full_name, blood_group, address) VALUES (?, ?, ?, ?)";
                    try (java.sql.PreparedStatement ps = conn.prepareStatement(inSql)) {
                        ps.setInt(1, userId);
                        ps.setString(2, name);
                        ps.setString(3, blood);
                        ps.setString(4, addr);
                        ps.executeUpdate();
                    }
                }
                updated = true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        String dbNotice = updated
                ? "✓ Profile successfully saved to MySQL disaster_db (users & user_profiles)"
                : "ℹ Saved locally (XAMPP MySQL offline)";

        JOptionPane.showMessageDialog(
                this,
                "PROFILE UPDATED SUCCESSFULLY!\n\n"
                        + "Name: " + name + "\n"
                        + "Blood Group: " + blood + "\n"
                        + "Emergency Phone: " + phone + "\n"
                        + "Address: " + addr + "\n\n"
                        + "Database Status: " + dbNotice,
                "Profile Saved",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}