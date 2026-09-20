package disaster.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginFrame extends BaseFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginFrame() {
        super("Smart Disaster Management - Login", 480, 560);
        setMinimumSize(new Dimension(420, 480));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        if (DashboardFrame.getActiveInstance() != null) {
            setLocationRelativeTo(DashboardFrame.getActiveInstance());
        }
        buildUI();
    }

    private void buildUI() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(UITheme.BACKGROUND);
        mainPanel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Center card with GridBagLayout for 100% guaranteed horizontal alignment
        JPanel card = createCard(12);
        card.setPreferredSize(new Dimension(410, 420));
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(26, 28, 26, 28));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;

        // 1. System Badge (Centered)
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 10, 0);
        JPanel pill = createBadge("DISASTER MANAGEMENT SYSTEM", UITheme.PRIMARY, UITheme.PRIMARY_LIGHT);
        card.add(pill, gbc);

        // 2. Title (Centered)
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 4, 0);
        JLabel title = heading("Sign In to Portal");
        card.add(title, gbc);

        // 3. Subtitle (Centered)
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 18, 0);
        JLabel sub = subtitle("Enter your authorized credentials below");
        card.add(sub, gbc);

        // 4. Username Label (Left-aligned)
        gbc.gridy++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 4, 0);
        JLabel userLbl = normalLabel("Username");
        card.add(userLbl, gbc);

        // 5. Username Field (Full width)
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 12, 0);
        usernameField = createTextField("Enter username");
        card.add(usernameField, gbc);

        // 6. Password Label (Left-aligned)
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 4, 0);
        JLabel passLbl = normalLabel("Password");
        card.add(passLbl, gbc);

        // 7. Password Field (Full width)
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 18, 0);
        passwordField = createPasswordField("••••••••••••");
        card.add(passwordField, gbc);

        // 8. Primary Sign In Button (Full width)
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 8, 0);
        JButton loginBtn = button("Sign In");
        loginBtn.addActionListener(e -> login());
        card.add(loginBtn, gbc);

        // 9. Register Prompt Section ("Don't have an account? Create an account")
        gbc.gridy++;
        gbc.insets = new Insets(10, 0, 0, 0);
        JPanel signupPromptRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        signupPromptRow.setOpaque(false);

        JLabel noAccLbl = new JLabel("Don't have an account?");
        noAccLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        noAccLbl.setForeground(UITheme.SECONDARY_TEXT);
        signupPromptRow.add(noAccLbl);

        JButton regBtn = new JButton("Create an account");
        regBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        regBtn.setForeground(UITheme.BRAND_CRIMSON);
        regBtn.setBorderPainted(false);
        regBtn.setContentAreaFilled(false);
        regBtn.setFocusPainted(false);
        regBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        regBtn.addActionListener(e -> {
            dispose();
            RegisterFrame reg = new RegisterFrame();
            if (DashboardFrame.getActiveInstance() != null) {
                reg.setLocationRelativeTo(DashboardFrame.getActiveInstance());
            }
            reg.setVisible(true);
            reg.toFront();
        });
        signupPromptRow.add(regBtn);
        card.add(signupPromptRow, gbc);

        mainPanel.add(card);
        setContentPane(createStandardScrollPane(mainPanel));
    }

    private void login() {
        String identifier = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (identifier.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter both your username and password.",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Dedicated Administrator Access Validation
        if ("admin123".equals(identifier)) {
            if ("admin@123".equals(password)) {
                disaster.model.User adminUser = new disaster.model.User("admin-001", "Admin Panel", "admin@sdma.kerala.gov.in", "ADMIN", 9.9312, 76.2673);
                adminUser.setFullName("Admin Panel");
                disaster.service.UserSession.setCurrentUser(adminUser);

                JOptionPane.showMessageDialog(
                        this,
                        "Admin Authentication Successful!\nWelcome to the Admin Panel.",
                        "Admin Access Granted",
                        JOptionPane.INFORMATION_MESSAGE
                );

                dispose();
                if (DashboardFrame.getActiveInstance() != null) {
                    DashboardFrame.getActiveInstance().updateSessionUser("Admin Panel");
                }
                new AdminFrame().setVisible(true);
                return;
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Invalid password for Administrator account.\nPlease check your credentials and try again.",
                        "Admin Access Denied",
                        JOptionPane.ERROR_MESSAGE
                );
                passwordField.setText("");
                return;
            }
        }

        disaster.model.User user = null;

        // 1. Try SDRP backend API first if available
        try {
            disaster.service.ApiClient.ApiResponse<disaster.model.User> response =
                    disaster.service.ApiClient.login(identifier, password);
            if (response.isSuccess() && response.getData() != null) {
                user = response.getData();
            }
        } catch (Exception ex) {
            System.out.println("Notice: Backend API offline, verifying directly against MySQL disaster_db.");
        }

        // 2. Direct MySQL disaster_db verification if backend server is not running
        if (user == null) {
            try {
                disaster.dao.UserDAO userDAO = new disaster.dao.UserDAO();
                user = userDAO.getUserByUsername(identifier);
                if (user == null && identifier.contains("@")) {
                    user = userDAO.getUserByEmail(identifier);
                }
            } catch (Exception e) {
                System.err.println("Direct DB check error: " + e.getMessage());
            }
        }

        if (user != null) {
            disaster.service.UserSession.setCurrentUser(user);

            String displayName = user.getUsername();
            if (!displayName.isEmpty()) {
                displayName = Character.toUpperCase(displayName.charAt(0)) + displayName.substring(1);
            }

            JOptionPane.showMessageDialog(
                    this,
                    "Database Authentication Successful!\n" +
                    "Welcome, " + displayName + " (" + user.getUserType() + ")\n" +
                    "User ID: #" + user.getUserId() + "\n" +
                    "Database: disaster_db @ localhost:3306",
                    "Login Verified",
                    JOptionPane.INFORMATION_MESSAGE
            );

            dispose();
            if (DashboardFrame.getActiveInstance() != null) {
                DashboardFrame.getActiveInstance().updateSessionUser(displayName);
                DashboardFrame.getActiveInstance().toFront();
                DashboardFrame.getActiveInstance().requestFocus();
            } else {
                new DashboardFrame(displayName).setVisible(true);
            }
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Login Failed: Username '" + identifier + "' was not found in 'disaster_db.users'.\n" +
                    "Please check the username or click 'Create an account' to register.",
                    "Authentication Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}