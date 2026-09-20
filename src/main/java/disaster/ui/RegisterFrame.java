package disaster.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class RegisterFrame extends BaseFrame {

    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JComboBox<String> bloodGroupCombo;
    private JTextField addressField;
    private JPasswordField passwordField;
    private JComboBox<String> roleCombo;

    public RegisterFrame() {
        super("Smart Disaster Management - Registration", 520, 780);
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
        card.setPreferredSize(new Dimension(430, 740));
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

        // Blood Group Label
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 3, 0);
        card.add(normalLabel("Blood Group"), gbc);

        // Blood Group Dropdown (Full width)
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 10, 0);
        bloodGroupCombo = new JComboBox<>(new String[]{
                "Select", "A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"
        });
        bloodGroupCombo.setFont(UITheme.NORMAL_FONT);
        bloodGroupCombo.setBackground(Color.WHITE);
        bloodGroupCombo.setPreferredSize(new Dimension(280, 38));
        card.add(bloodGroupCombo, gbc);

        // Residential Address Label
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 3, 0);
        card.add(normalLabel("Residential Address"), gbc);

        // Residential Address Field (Full width)
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 10, 0);
        addressField = createTextField("e.g. 123 Relief Camp Rd, Kochi");
        card.add(addressField, gbc);

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
        String bloodGroupSel = (String) bloodGroupCombo.getSelectedItem();
        String bloodGroup = ("Select".equals(bloodGroupSel) || bloodGroupSel == null) ? "" : bloodGroupSel;
        String address = addressField.getText().trim();

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

        // Pre-invocation payload logs
        System.out.println("==================================================");
        System.out.println("[RegisterFrame] ACTION TRIGGER: Account Creation Form Submitted");
        System.out.println("   Input Name:        '" + name + "'");
        System.out.println("   Input Email:       '" + email + "'");
        System.out.println("   Input Phone:       '" + phone + "'");
        System.out.println("   Input Blood Group: '" + bloodGroup + "'");
        System.out.println("   Input Address:     '" + address + "'");
        System.out.println("   Selected Role:     '" + sdrpRole + "'");
        System.out.println("==================================================");

        // Invoke UserDAO.registerNewUser via transactional method
        disaster.dao.UserDAO userDAO = new disaster.dao.UserDAO();
        int newUserId = userDAO.registerNewUserAndGetId(name, email, phone, sdrpRole, bloodGroup, address, password);

        if (newUserId > 0) {
            // Construct User entity for active session
            disaster.model.User newUser = new disaster.model.User();
            newUser.setUserId(String.valueOf(newUserId));
            newUser.setUsername(email);
            newUser.setFullName(name);
            newUser.setEmail(email);
            newUser.setPhone(phone);
            newUser.setUserType(sdrpRole);
            newUser.setBloodGroup(bloodGroup);
            newUser.setAddress(address);

            // Also register with background SDRP session if server is up
            try {
                disaster.service.ApiClient.register(email, email, password, sdrpRole, 9.9312, 76.2673);
            } catch (Exception ignored) {}

            disaster.service.UserSession.setCurrentUser(newUser);

            JOptionPane.showMessageDialog(
                    this,
                    "User registration successful!",
                    "Registration Successful",
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
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Registration failed. Could not save user record to database.",
                    "Registration Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}