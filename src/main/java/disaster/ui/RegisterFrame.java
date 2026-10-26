package disaster.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;

public class RegisterFrame extends BaseFrame {

    private JTextField nameField;
    private JTextField emailField;
    private JLabel emailErrorLabel;

    private JTextField phoneField;
    private JLabel phoneErrorLabel;

    private JPasswordField passwordField;
    private JButton eyeBtn;
    private boolean passwordVisible = false;
    private PasswordStrengthBar strengthBar;
    private JLabel strengthLabel;

    private JPasswordField confirmPasswordField;
    private JLabel confirmErrorLabel;

    public RegisterFrame() {
        super("Smart Disaster Management - Registration", 490, 720);
        setMinimumSize(new Dimension(430, 620));
        if (DashboardFrame.getActiveInstance() != null) {
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setLocationRelativeTo(DashboardFrame.getActiveInstance());
        } else {
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }
        buildUI();
    }

    private void buildUI() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(UITheme.BACKGROUND);
        mainPanel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Card with GridBagLayout for pixel-perfect label and field alignment
        JPanel card = createCard(12);
        card.setPreferredSize(new Dimension(420, 660));
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(20, 26, 20, 26));

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
        gbc.insets = new Insets(0, 0, 12, 0);
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
        gbc.insets = new Insets(0, 0, 8, 0);
        nameField = createTextField("e.g. Rahul Sharma");
        card.add(nameField, gbc);

        // Email Label
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 3, 0);
        card.add(normalLabel("Email Address"), gbc);

        // Email Field (Full width) — validated on submit only
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 2, 0);
        emailField = createTextField("example@gmail.com");
        card.add(emailField, gbc);

        // Email Inline Error Label
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 8, 0);
        emailErrorLabel = new JLabel("");
        emailErrorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        emailErrorLabel.setForeground(new Color(220, 38, 38));
        card.add(emailErrorLabel, gbc);

        // Phone Label
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 3, 0);
        card.add(normalLabel("Phone Number (10 digits)"), gbc);

        // Phone Field (Full width) — digits-only filter, validated on submit
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 2, 0);
        phoneField = createTextField("9876543210");
        ((AbstractDocument) phoneField.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr)
                    throws BadLocationException {
                if (text != null && text.matches("[0-9]*")) super.insertString(fb, offset, text, attr);
            }
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                if (text != null && text.matches("[0-9]*")) super.replace(fb, offset, length, text, attrs);
            }
        });
        card.add(phoneField, gbc);

        // Phone Inline Error Label
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 8, 0);
        phoneErrorLabel = new JLabel("");
        phoneErrorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        phoneErrorLabel.setForeground(new Color(220, 38, 38));
        card.add(phoneErrorLabel, gbc);

        // Password Label
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 3, 0);
        card.add(normalLabel("Password"), gbc);

        // Password Field Row with eye-toggle button
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 6, 0);
        card.add(buildPasswordRowWithEye(), gbc);

        // Real-Time Password Strength Progress Bar with Simple / Medium / Complex
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 8, 0);
        JPanel strengthRow = new JPanel(new BorderLayout(8, 0));
        strengthRow.setOpaque(false);

        strengthBar = new PasswordStrengthBar();
        strengthLabel = new JLabel("");
        strengthLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        strengthLabel.setPreferredSize(new Dimension(75, 16));
        strengthLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        strengthRow.add(strengthBar, BorderLayout.CENTER);
        strengthRow.add(strengthLabel, BorderLayout.EAST);
        card.add(strengthRow, gbc);

        // Confirm Password Label
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 3, 0);
        card.add(normalLabel("Confirm Password"), gbc);

        // Confirm Password Field (Full width) — validated on submit only
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 2, 0);
        confirmPasswordField = createPasswordField("Re-enter password");
        card.add(confirmPasswordField, gbc);

        // Confirm Password Inline Error Label
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 14, 0);
        confirmErrorLabel = new JLabel("");
        confirmErrorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        confirmErrorLabel.setForeground(new Color(220, 38, 38));
        card.add(confirmErrorLabel, gbc);

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

    /**
     * Builds the password input row: JPasswordField + eye-toggle button.
     * Attaches the real-time strength DocumentListener here.
     */
    private JPanel buildPasswordRowWithEye() {
        JPanel row = new JPanel(new BorderLayout(0, 0));
        row.setOpaque(false);

        passwordField = createPasswordField("Enter password");

        eyeBtn = new JButton("\uD83D\uDC41");
        eyeBtn.setFocusPainted(false);
        eyeBtn.setBorderPainted(false);
        eyeBtn.setContentAreaFilled(false);
        eyeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        eyeBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15));
        eyeBtn.setForeground(UITheme.SECONDARY_TEXT);
        eyeBtn.setToolTipText("Show / Hide password");
        eyeBtn.setPreferredSize(new Dimension(38, passwordField.getPreferredSize().height));
        eyeBtn.addActionListener(e -> {
            passwordVisible = !passwordVisible;
            if (passwordVisible) {
                passwordField.setEchoChar((char) 0);
                eyeBtn.setForeground(UITheme.PRIMARY);
            } else {
                passwordField.setEchoChar('\u2022');
                eyeBtn.setForeground(UITheme.SECONDARY_TEXT);
            }
            passwordField.requestFocusInWindow();
        });

        // Real-time strength bar — visual feedback only, not an error
        passwordField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateStrength(); }
            @Override public void removeUpdate(DocumentEvent e) { updateStrength(); }
            @Override public void changedUpdate(DocumentEvent e) { updateStrength(); }
        });

        row.add(passwordField, BorderLayout.CENTER);
        row.add(eyeBtn, BorderLayout.EAST);
        return row;
    }

    private int evaluateStrength(String pass) {
        if (pass == null || pass.isEmpty()) return 0;
        boolean hasUpper = pass.matches(".*[A-Z].*");
        boolean hasLower = pass.matches(".*[a-z].*");
        boolean hasDigit = pass.matches(".*[0-9].*");
        boolean hasSpecial = pass.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");

        int criteriaMet = 0;
        if (hasUpper) criteriaMet++;
        if (hasLower) criteriaMet++;
        if (hasDigit) criteriaMet++;
        if (hasSpecial) criteriaMet++;

        // Simple: under 6 chars or missing any core requirement
        if (pass.length() < 6 || criteriaMet < 4) {
            return 1; // Simple (will not allow)
        }

        // Complex: satisfies all requirements and has length >= 9
        if (pass.length() >= 9 && criteriaMet == 4) {
            return 3; // Complex (can allow)
        }

        // Medium: satisfies all requirements with length 6 to 8
        return 2; // Medium (can allow)
    }

    private void updateStrength() {
        String pass = new String(passwordField.getPassword());
        int lvl = evaluateStrength(pass);
        if (pass.isEmpty()) {
            strengthBar.setStrength(0, new Color(226, 232, 240));
            strengthLabel.setText("");
        } else if (lvl == 1) {
            // Simple: vibrant Red bar & label
            strengthBar.setStrength(33, new Color(239, 68, 68));
            strengthLabel.setText("Simple");
            strengthLabel.setForeground(new Color(220, 38, 38));
        } else if (lvl == 2) {
            // Medium: warm Amber/Orange bar & label
            strengthBar.setStrength(66, new Color(245, 158, 11));
            strengthLabel.setText("Medium");
            strengthLabel.setForeground(new Color(217, 119, 6));
        } else {
            // Complex: vibrant Green bar & label
            strengthBar.setStrength(100, new Color(16, 185, 129));
            strengthLabel.setText("Complex");
            strengthLabel.setForeground(new Color(5, 150, 105));
        }
    }

    /** Convenience: show a warning dialog then request focus. */
    private void showError(String message, String title, Component focusTarget) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.WARNING_MESSAGE);
        if (focusTarget != null) focusTarget.requestFocusInWindow();
    }

    private void register() {
        // Clear previous inline error labels
        emailErrorLabel.setText("");
        phoneErrorLabel.setText("");
        confirmErrorLabel.setText("");

        String name            = nameField.getText().trim();
        String email           = emailField.getText().trim();
        String phone           = phoneField.getText().trim();
        String password        = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        // 1. Full Name
        if (name.isEmpty()) {
            showError("Please enter your full name.", "Missing Name", nameField);
            return;
        }

        // 2. Email — two-layer validation
        //    Layer 1: Basic structural format  (local@domain.tld)
        String emailStructureRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
        //    Layer 2: Domain whitelist
        //       • Well-known personal providers  (gmail, yahoo, hotmail, outlook …)
        //       • Institutional / government     (.gov, .gov.in, .ac.in, .nic.in, .org, .edu …)
        String knownProviders =
            "(gmail|yahoo|ymail|hotmail|outlook|live|rediffmail|icloud|" +
            "protonmail|zoho|aol|yandex|gmx|mail|rocketmail|inbox|" +
            "msn|googlemail|me|mac|pm)\\.com" +
            "|yahoo\\.co\\.in|yahoo\\.in|rediffmail\\.com|" +
            "outlook\\.in|hotmail\\.in|live\\.in|icloud\\.com";
        String institutionalDomain =
            "[A-Za-z0-9-]+(\\.(gov|edu|ac|org|nic|net|mil|int|co))" +
            "(\\.(in|uk|us|au|nz|jp|ca|de|fr|za|sg|my|lk|pk|bd|np|io))?";
        String emailDomainRegex = "^[A-Za-z0-9+_.-]+@(" + knownProviders + "|" + institutionalDomain + ")$";

        if (email.isEmpty()) {
            emailErrorLabel.setText("Email address is required.");
            showError("Please enter your email address.", "Missing Email", emailField);
            return;
        }
        if (!email.matches(emailStructureRegex)) {
            emailErrorLabel.setText("Enter a valid email — e.g. example@gmail.com");
            showError("Invalid email format.\nExample of a valid email: example@gmail.com", "Invalid Email", emailField);
            return;
        }
        if (!email.matches(emailDomainRegex)) {
            emailErrorLabel.setText("Use a valid provider — e.g. @gmail.com, @yahoo.com, @outlook.com");
            showError(
                "Unrecognised email domain.\n\n" +
                "Please use an email from a known provider, such as:\n" +
                "  • example@gmail.com\n" +
                "  • example@yahoo.com\n" +
                "  • example@outlook.com\n" +
                "  • example@hotmail.com\n" +
                "  • example@rediffmail.com\n" +
                "  • example@icloud.com\n" +
                "  • (or a government/institutional email: .gov.in, .ac.in, .org)",
                "Invalid Email Domain",
                emailField
            );
            return;
        }

        // 3. Phone — exactly 10 digits (alphabets already blocked by DocumentFilter)
        if (phone.isEmpty()) {
            phoneErrorLabel.setText("Phone number is required.");
            showError("Please enter your 10-digit phone number.", "Missing Phone", phoneField);
            return;
        }
        if (!phone.matches("^[0-9]{10}$")) {
            phoneErrorLabel.setText("Phone number must be exactly 10 digits.");
            showError("Phone number must be exactly 10 digits.\nMore than or less than 10 digits are not allowed.", "Invalid Phone", phoneField);
            return;
        }

        // 4. Password strength
        if (password.isEmpty()) {
            showError("Please create a password.", "Missing Password", passwordField);
            return;
        }
        int strength = evaluateStrength(password);
        if (strength == 1) {
            showError(
                "Password strength is too weak (Simple).\n\n" +
                "Your password must contain:\n" +
                "  \u2022 At least 6 characters\n" +
                "  \u2022 At least one uppercase letter (A\u2013Z)\n" +
                "  \u2022 At least one lowercase letter (a\u2013z)\n" +
                "  \u2022 At least one digit (0\u20139)\n" +
                "  \u2022 At least one special character (e.g. _, @, -, !)",
                "Password Too Weak",
                passwordField
            );
            return;
        }

        // 5. Confirm Password
        if (confirmPassword.isEmpty()) {
            confirmErrorLabel.setText("Please confirm your password.");
            showError("Please re-enter your password in the Confirm Password field.", "Missing Confirm Password", confirmPasswordField);
            return;
        }
        if (!password.equals(confirmPassword)) {
            confirmErrorLabel.setText("Passwords do not match.");
            showError("Passwords do not match.\nPlease re-enter your password.", "Password Mismatch", confirmPasswordField);
            confirmPasswordField.setText("");
            return;
        }

        // All validations passed
        String sdrpRole = "CITIZEN";
        System.out.println("==================================================");
        System.out.println("[RegisterFrame] ACTION: Account Creation Submitted");
        System.out.println("   Name:  " + name);
        System.out.println("   Email: " + email);
        System.out.println("   Phone: " + phone);
        System.out.println("   Role:  " + sdrpRole);
        System.out.println("==================================================");

        disaster.dao.UserDAO userDAO = new disaster.dao.UserDAO();
        int newUserId = userDAO.registerNewUserAndGetId(name, email, phone, sdrpRole, "", "", password);

        if (newUserId > 0) {
            try {
                disaster.service.ApiClient.register(email, email, password, sdrpRole, 9.9312, 76.2673);
            } catch (Exception ignored) {}

            JOptionPane.showMessageDialog(
                    this,
                    "Account created successfully!\nPlease sign in with your credentials to access the portal.",
                    "Registration Successful",
                    JOptionPane.INFORMATION_MESSAGE
            );

            dispose();
            LoginFrame login = new LoginFrame();
            if (DashboardFrame.getActiveInstance() != null) {
                login.setLocationRelativeTo(DashboardFrame.getActiveInstance());
            }
            login.setVisible(true);
            login.toFront();
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Registration failed. Could not save user record to database.\nThe email or phone may already be registered.",
                    "Registration Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Custom antialiased rounded pill progress bar that paints real-time colors
     * (Red = Simple, Orange = Medium, Green = Complex) without Windows native L&F override.
     */
    public static class PasswordStrengthBar extends JComponent {
        private int percent = 0;
        private Color color = new Color(226, 232, 240);

        public PasswordStrengthBar() {
            setPreferredSize(new Dimension(240, 7));
            setMinimumSize(new Dimension(100, 7));
            setOpaque(false);
        }

        public void setStrength(int pct, Color c) {
            this.percent = pct;
            this.color = c;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            int arc = h;

            // Background track
            g2.setColor(new Color(226, 232, 240));
            g2.fillRoundRect(0, 0, w, h, arc, arc);

            // Colored progress fill
            if (percent > 0) {
                int fillW = Math.max(arc, (int) Math.round((w * percent) / 100.0));
                g2.setColor(color);
                g2.fillRoundRect(0, 0, fillW, h, arc, arc);
            }
            g2.dispose();
        }
    }
}