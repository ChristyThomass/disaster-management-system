package disaster.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class RegisterFrame extends BaseFrame {

    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JPasswordField passwordField;
    private JComboBox<String> roleCombo;

    public RegisterFrame() {
        super("Smart Disaster Management - Registration", 520, 660);
        setMinimumSize(new Dimension(460, 520));
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

        // Card with GridBagLayout for pixel-perfect label and field alignment
        JPanel card = createCard(12);
        card.setPreferredSize(new Dimension(430, 580));
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(24, 28, 24, 28));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;

        // Title (Centered)
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 4, 0);
        JLabel title = heading("Create Your Account");
        card.add(title, gbc);

        // Subtitle (Centered)
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 16, 0);
        JLabel sub = subtitle("Register for rapid disaster alert & response access");
        card.add(sub, gbc);

        // Full Name Label (Left-aligned)
        gbc.gridy++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 3, 0);
        card.add(normalLabel("Full Name"), gbc);

        // Full Name Field (Full width)
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 10, 0);
        nameField = createTextField("e.g. Rahul Sharma");
        card.add(nameField, gbc);

        // Role Label
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 3, 0);
        card.add(normalLabel("Operational Role"), gbc);

        // Role Dropdown (Full width)
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 10, 0);
        roleCombo = new JComboBox<>(new String[]{
                "Citizen / Resident",
                "Certified First Responder",
                "Disaster Relief Volunteer",
                "Emergency Medical Staff",
                "Municipal Coordinator"
        });
        roleCombo.setFont(UITheme.NORMAL_FONT);
        roleCombo.setBackground(Color.WHITE);
        roleCombo.setPreferredSize(new Dimension(280, 38));
        card.add(roleCombo, gbc);

        // Email Label
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 3, 0);
        card.add(normalLabel("Email Address"), gbc);

        // Email Field (Full width)
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 10, 0);
        emailField = createTextField("rahul.sharma@example.com");
        card.add(emailField, gbc);

        // Phone Label
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 3, 0);
        card.add(normalLabel("Phone Number"), gbc);

        // Phone Field (Full width)
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 10, 0);
        phoneField = createTextField("+91 98765 43210");
        card.add(phoneField, gbc);

        // Password Label
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 3, 0);
        card.add(normalLabel("Security Password"), gbc);

        // Password Field (Full width)
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 18, 0);
        passwordField = createPasswordField("Minimum 8 characters");
        card.add(passwordField, gbc);

        // Register Button (Full width)
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 8, 0);
        JButton regBtn = button("Create Account");
        regBtn.addActionListener(e -> register());
        card.add(regBtn, gbc);

        // Back to Login Button (Full width)
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 0, 0);
        JButton backBtn = createOutlineButton("Back to Login", UITheme.BORDER_DARK);
        backBtn.addActionListener(e -> {
            dispose();
            LoginFrame login = new LoginFrame();
            if (DashboardFrame.getActiveInstance() != null) {
                login.setLocationRelativeTo(DashboardFrame.getActiveInstance());
            }
            login.setVisible(true);
            login.toFront();
        });
        card.add(backBtn, gbc);

        mainPanel.add(card);
        JScrollPane scroll = createStandardScrollPane(mainPanel);
        setContentPane(scroll);
    }

    private void register() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = new String(passwordField.getPassword());
        String selectedRole = (String) roleCombo.getSelectedItem();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please fill in all the required registration fields (Name, Email, Password).",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Map role to SDRP backend role (RESPONDER, VICTIM, ADMIN)
        String sdrpRole = "VICTIM";
        if (selectedRole != null) {
            if (selectedRole.contains("Responder") || selectedRole.contains("Medical") || selectedRole.contains("Volunteer")) {
                sdrpRole = "RESPONDER";
            } else if (selectedRole.contains("Coordinator")) {
                sdrpRole = "ADMIN";
            }
        }

        String username = name.replaceAll("\\s+", "_").toLowerCase();
        if (username.length() < 3) username = username + "_user";

        // Pre-invocation payload logs
        System.out.println("==================================================");
        System.out.println("[RegisterFrame] ACTION TRIGGER: Account Creation Form Submitted");
        System.out.println("   Input Name:        '" + name + "'");
        System.out.println("   Input Email:       '" + email + "'");
        System.out.println("   Input Phone:       '" + phone + "'");
        System.out.println("   Derived Username:  '" + username + "'");
        System.out.println("   Selected Role:     '" + sdrpRole + "'");
        System.out.println("==================================================");

        // Construct User entity
        disaster.model.User newUser = new disaster.model.User();
        newUser.setUsername(username);
        newUser.setPhone(phone);
        newUser.setFullName(name);
        newUser.setEmail(email);
        newUser.setUserType(sdrpRole);

        // 1. Direct persistence to XAMPP MySQL via UserDAO.addUser()
        boolean userDaoSynced = false;
        String daoStatus = "";
        try {
            disaster.dao.UserDAO userDAO = new disaster.dao.UserDAO();
            System.out.println("[RegisterFrame] Invoking UserDAO.addUser() with verified payload...");
            userDaoSynced = userDAO.addUser(newUser);
            daoStatus = "\n• UserDAO: Successfully saved to disaster_db (User ID #" + newUser.getUserId() + ")";
        } catch (java.sql.SQLException ex) {
            System.err.println("❌ [RegisterFrame] UserDAO.addUser() FAILED with SQLException!");
            System.err.println("   Message: " + ex.getMessage());
            System.err.println("   Error Code: " + ex.getErrorCode());
            System.err.println("   SQL State: " + ex.getSQLState());
            ex.printStackTrace();
            daoStatus = "\n• UserDAO (MySQL Error): " + ex.getMessage();
        } catch (Exception ex) {
            System.err.println("❌ [RegisterFrame] UserDAO.addUser() FAILED with unexpected exception!");
            ex.printStackTrace();
            daoStatus = "\n• UserDAO Error: " + ex.getMessage();
        }

        // 2. Also register with background SDRP session if server is up
        try {
            disaster.service.ApiClient.register(username, email, password, sdrpRole, 9.9312, 76.2673);
        } catch (Exception ignored) {}

        disaster.service.UserSession.setCurrentUser(newUser);

        JOptionPane.showMessageDialog(
                this,
                "Successfully registered",
                "Account Created",
                JOptionPane.INFORMATION_MESSAGE
        );

        dispose();
        if (DashboardFrame.getActiveInstance() != null) {
            DashboardFrame.getActiveInstance().updateSessionUser(name);
            DashboardFrame.getActiveInstance().toFront();
            DashboardFrame.getActiveInstance().requestFocus();
        } else {
            new DashboardFrame(name).setVisible(true);
        }
    }
}