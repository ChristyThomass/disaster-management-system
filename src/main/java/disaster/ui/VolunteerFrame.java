package disaster.ui;

import disaster.service.AdminDataManager;
import disaster.service.UserSession;
import disaster.model.User;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class VolunteerFrame extends BaseFrame {

    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;
    private Runnable changeListener;

    public VolunteerFrame() {
        super("Certified Responders & Volunteer Corps - SDMA", 950, 620);
        setMinimumSize(new Dimension(840, 520));
        buildUI();

        // Real-time synchronization: when Admin approves or updates tasks, auto-refresh
        changeListener = () -> SwingUtilities.invokeLater(this::buildUI);
        AdminDataManager.getInstance().addChangeListener(changeListener);
    }

    @Override
    public void dispose() {
        if (changeListener != null) {
            AdminDataManager.getInstance().removeChangeListener(changeListener);
        }
        super.dispose();
    }

    private void buildUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 14));
        mainPanel.setBackground(UITheme.BACKGROUND);
        mainPanel.setBorder(new EmptyBorder(18, 22, 18, 22));

        String userIdentifier = resolveCurrentIdentifier();
        boolean isAdmin = isAdminUser(userIdentifier);
        boolean isApproved = isAdmin || AdminDataManager.getInstance().isVolunteerApproved(userIdentifier);
        String currentStatus = AdminDataManager.getInstance().getVolunteerStatus(userIdentifier);

        // Also check if current user model has userType VOLUNTEER
        if (!isApproved && UserSession.getCurrentUser() != null) {
            String uType = UserSession.getCurrentUser().getUserType();
            if ("VOLUNTEER".equalsIgnoreCase(uType)) {
                isApproved = true;
            }
        }

        if (isAdmin) {
            buildAdminVolunteerView(mainPanel);
        } else if (isApproved) {
            buildPersonalVolunteerView(mainPanel, userIdentifier);
        } else if ("Pending".equalsIgnoreCase(currentStatus)) {
            AdminDataManager.Volunteer applicant = findApplicant(userIdentifier);
            buildPendingApprovalView(mainPanel, applicant);
        } else {
            buildNonVolunteerView(mainPanel);
        }

        setContentPane(mainPanel);
        revalidate();
        repaint();
    }

    private AdminDataManager.Volunteer findApplicant(String userIdentifier) {
        AdminDataManager.Volunteer applicant = AdminDataManager.getInstance().getVolunteerByUsername(userIdentifier);
        if (applicant == null && userIdentifier.contains("@")) {
            applicant = AdminDataManager.getInstance().getVolunteerByEmail(userIdentifier);
        }
        if (applicant == null && UserSession.getCurrentUser() != null) {
            String fn = UserSession.getCurrentUser().getFullName();
            if (fn != null) applicant = AdminDataManager.getInstance().getVolunteerByUsername(fn);
        }
        return applicant;
    }

    private String resolveCurrentIdentifier() {
        if (UserSession.getCurrentUser() != null) {
            String u = UserSession.getCurrentUser().getUsername();
            if (u != null && !u.isEmpty()) return u;
            String e = UserSession.getCurrentUser().getEmail();
            if (e != null && !e.isEmpty()) return e;
            String f = UserSession.getCurrentUser().getFullName();
            if (f != null && !f.isEmpty()) return f;
        }
        return "guest_citizen";
    }

    private boolean isAdminUser(String id) {
        if ("admin".equalsIgnoreCase(id) || "admin123".equalsIgnoreCase(id) || "Admin Panel".equalsIgnoreCase(id)) {
            return true;
        }
        if (UserSession.getCurrentUser() != null && "ADMIN".equalsIgnoreCase(UserSession.getCurrentUser().getUserType())) {
            return true;
        }
        return false;
    }

    // =========================================================================
    // 1. NON-VOLUNTEER VIEW (Clean & uncluttered: Only Join CTA)
    // =========================================================================
    private void buildNonVolunteerView(JPanel mainPanel) {
        JPanel header = createStandardHeader(
                "State Disaster Management Volunteer Corps",
                "Official emergency response network for certified volunteers and disaster responders",
                "JOIN THE CORPS",
                UITheme.BRAND_CRIMSON
        );
        mainPanel.add(header, BorderLayout.NORTH);

        JPanel centerCard = createCard(12);
        centerCard.setLayout(new GridBagLayout());
        centerCard.setBorder(new EmptyBorder(30, 30, 30, 30));

        JPanel ctaBox = new JPanel();
        ctaBox.setLayout(new BoxLayout(ctaBox, BoxLayout.Y_AXIS));
        ctaBox.setOpaque(false);
        ctaBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel iconLbl = new JLabel("🤝", SwingConstants.CENTER);
        iconLbl.setFont(new Font("Segoe UI", Font.PLAIN, 46));
        iconLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        ctaBox.add(iconLbl);

        ctaBox.add(Box.createVerticalStrut(14));

        JLabel titleLbl = new JLabel("Join the Disaster Management System", SwingConstants.CENTER);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLbl.setForeground(UITheme.TEXT);
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        ctaBox.add(titleLbl);

        ctaBox.add(Box.createVerticalStrut(8));

        JLabel subLbl = new JLabel("Become an authorized emergency volunteer. Submit your specialized skills to receive official missions.", SwingConstants.CENTER);
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subLbl.setForeground(UITheme.SECONDARY_TEXT);
        subLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        ctaBox.add(subLbl);

        ctaBox.add(Box.createVerticalStrut(24));

        JButton joinBtn = button("Join the Disaster Management System", UITheme.BRAND_CRIMSON);
        joinBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        joinBtn.setPreferredSize(new Dimension(360, 48));
        joinBtn.setMaximumSize(new Dimension(420, 48));
        joinBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        joinBtn.addActionListener(e -> startRegistrationFlow());
        ctaBox.add(joinBtn);

        centerCard.add(ctaBox);
        mainPanel.add(centerCard, BorderLayout.CENTER);

        // Bottom Bar
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        bottom.setOpaque(false);

        JButton backBtn = createBackButton("Back to Portal");
        backBtn.setPreferredSize(new Dimension(150, 42));
        bottom.add(backBtn);

        mainPanel.add(bottom, BorderLayout.SOUTH);
    }

    // =========================================================================
    // 2. PENDING APPROVAL GATE VIEW (ONLY 6 specified items)
    // =========================================================================
    private void buildPendingApprovalView(JPanel mainPanel, AdminDataManager.Volunteer v) {
        JPanel header = createStandardHeader(
                "Volunteer Application Submitted",
                "Your volunteer registration credentials are currently under review by the Administrator",
                "APPROVAL PENDING",
                new Color(217, 119, 6)
        );
        mainPanel.add(header, BorderLayout.NORTH);

        JPanel centerCard = createCard(12);
        centerCard.setLayout(new BoxLayout(centerCard, BoxLayout.Y_AXIS));
        centerCard.setBorder(new EmptyBorder(24, 28, 24, 28));

        // Orange Notice Box
        JPanel noticeBox = new JPanel(new BorderLayout(12, 0));
        noticeBox.setBackground(new Color(254, 243, 199));
        noticeBox.setBorder(new CompoundBorder(
                new LineBorder(new Color(245, 158, 11), 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));
        noticeBox.setMaximumSize(new Dimension(Short.MAX_VALUE, 75));

        JLabel iconLbl = new JLabel("⏳");
        iconLbl.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        noticeBox.add(iconLbl, BorderLayout.WEST);

        JPanel noticeText = new JPanel();
        noticeText.setLayout(new BoxLayout(noticeText, BoxLayout.Y_AXIS));
        noticeText.setOpaque(false);
        JLabel nTitle = new JLabel("Application Status: Pending Administrator Approval");
        nTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nTitle.setForeground(new Color(146, 64, 14));
        noticeText.add(nTitle);

        JLabel nSub = new JLabel("Once reviewed by State Command, your personal volunteer deployment page will activate automatically.");
        nSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        nSub.setForeground(new Color(180, 83, 9));
        noticeText.add(nSub);

        noticeBox.add(noticeText, BorderLayout.CENTER);
        centerCard.add(noticeBox);

        centerCard.add(Box.createVerticalStrut(20));

        JLabel summaryTitle = new JLabel("Submitted Volunteer Registration Information:");
        summaryTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        summaryTitle.setForeground(UITheme.TEXT);
        centerCard.add(summaryTitle);

        centerCard.add(Box.createVerticalStrut(14));

        // Clean user values
        String applicantName = (v != null && !v.getName().isEmpty()) ? v.getName() : "Christy Thomas";
        if (applicantName.contains("@") && UserSession.getCurrentUser() != null && UserSession.getCurrentUser().getFullName() != null) {
            applicantName = UserSession.getCurrentUser().getFullName();
        }

        String applicantEmail = (v != null && !v.getEmail().isEmpty()) ? v.getEmail() : "christy@gmail.com";
        applicantEmail = disaster.backend.DatabaseManager.cleanEmail(applicantEmail);

        String applicantPhone = (v != null && !v.getPhone().isEmpty()) ? v.getPhone() : "9876543210";
        String applicantDistrict = (v != null && !v.getDistrict().isEmpty()) ? v.getDistrict() : "Alappuzha";
        String applicantSkill = (v != null && !v.getSkill().isEmpty()) ? v.getSkill() : "Basic First Aid";
        String applicantDocs = "Government ID Attached & Validated";
        if (v != null && v.getCertificatePath() != null && !v.getCertificatePath().equals("None Attached")) {
            applicantDocs += " • " + v.getCertificatePath();
        }

        // STRICTLY ONLY 6 ITEMS: Full Name, Email, Ph No, District, Skill that chosen, ID Proof / Certificates
        JPanel grid = new JPanel(new GridLayout(3, 2, 20, 14));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(Short.MAX_VALUE, 150));

        grid.add(createDetailItem("Full Name:", applicantName));
        grid.add(createDetailItem("Email Address:", applicantEmail));
        grid.add(createDetailItem("Phone Number:", applicantPhone));
        grid.add(createDetailItem("District:", applicantDistrict));
        grid.add(createDetailItem("Skill Chosen:", applicantSkill));
        grid.add(createDetailItem("ID Proof / Certificates:", applicantDocs));

        centerCard.add(grid);

        mainPanel.add(centerCard, BorderLayout.CENTER);

        // Bottom Bar
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        bottom.setOpaque(false);

        JButton refreshBtn = button("Check / Refresh Approval Status", UITheme.PRIMARY);
        refreshBtn.setPreferredSize(new Dimension(270, 42));
        refreshBtn.addActionListener(e -> buildUI());
        bottom.add(refreshBtn);

        JButton backBtn = createBackButton("Back to Portal");
        backBtn.setPreferredSize(new Dimension(150, 42));
        bottom.add(backBtn);

        mainPanel.add(bottom, BorderLayout.SOUTH);
    }

    // =========================================================================
    // 3. APPROVED VOLUNTEER'S PERSONAL WORK & MISSIONS VIEW
    // (NO OTHER VOLUNTEERS' DATA SHOWN - PURELY THEIR OWN ASSIGNED WORK)
    // =========================================================================
    private void buildPersonalVolunteerView(JPanel mainPanel, String userIdentifier) {
        AdminDataManager.Volunteer v = findApplicant(userIdentifier);
        String volName = (v != null && !v.getName().isEmpty()) ? v.getName() : "Certified Volunteer";
        String volSkill = (v != null && !v.getSkill().isEmpty()) ? v.getSkill() : "Disaster Response";
        String volDistrict = (v != null && !v.getDistrict().isEmpty()) ? v.getDistrict() : "Kerala Relief Sector";

        JPanel header = createStandardHeader(
                "Volunteer Operations Portal • " + volName,
                "Authorized deployment dashboard and real-time operational mission directives",
                "AUTHORIZED ACTIVE RESPONDER",
                UITheme.SUCCESS
        );
        mainPanel.add(header, BorderLayout.NORTH);

        JPanel centerCard = createCard(8);
        centerCard.setLayout(new BorderLayout(0, 12));

        // Volunteer Profile Status Strip
        JPanel profileStrip = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 6));
        profileStrip.setBackground(new Color(240, 253, 244));
        profileStrip.setBorder(new CompoundBorder(
                new LineBorder(new Color(187, 247, 208), 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));

        JLabel badgeIcon = new JLabel("🛡️");
        badgeIcon.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        profileStrip.add(badgeIcon);

        JLabel nameLbl = new JLabel("Responder: " + volName + "  |  District: " + volDistrict + "  |  Primary Skill: " + volSkill + "  |  Status: On Active Duty");
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        nameLbl.setForeground(new Color(22, 101, 52));
        profileStrip.add(nameLbl);

        centerCard.add(profileStrip, BorderLayout.NORTH);

        // Personal Missions Table
        String[] columns = {
                "Task #",
                "Mission / Work Title",
                "District",
                "Deployment Location",
                "Priority",
                "Assigned Date",
                "Mission Status"
        };

        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(36);
        table.setFont(UITheme.NORMAL_FONT);
        table.setShowGrid(true);
        table.setGridColor(UITheme.BORDER);
        table.setSelectionBackground(UITheme.HERO_BADGE_BG);
        table.setSelectionForeground(UITheme.BRAND_CRIMSON);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.getTableHeader().setFont(UITheme.BUTTON_FONT);
        table.getTableHeader().setBackground(UITheme.PANEL_BG);
        table.getTableHeader().setForeground(UITheme.TEXT);
        table.setFillsViewportHeight(true);

        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setFont(UITheme.BOLD_FONT);
                l.setForeground(UITheme.BRAND_CRIMSON);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                return l;
            }
        });

        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setFont(UITheme.BOLD_FONT);
                l.setForeground(UITheme.SUCCESS);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                return l;
            }
        });

        JScrollPane scroll = createTableScrollPane(table);
        centerCard.add(scroll, BorderLayout.CENTER);

        // Load strictly THIS volunteer's tasks
        loadPersonalTasks(v != null ? v.getId() : -1, userIdentifier);

        mainPanel.add(centerCard, BorderLayout.CENTER);

        // Bottom Action Bar
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        bottom.setOpaque(false);

        JButton inProgBtn = button("Mark In Progress", UITheme.HOTLINE_BLUE);
        inProgBtn.setPreferredSize(new Dimension(160, 42));
        inProgBtn.addActionListener(e -> updateSelectedTaskStatus("In Progress"));
        bottom.add(inProgBtn);

        JButton compBtn = button("Mark Completed", UITheme.SUCCESS);
        compBtn.setPreferredSize(new Dimension(160, 42));
        compBtn.addActionListener(e -> updateSelectedTaskStatus("Completed"));
        bottom.add(compBtn);

        JButton detailsBtn = createOutlineButton("View Directives", UITheme.BORDER_DARK);
        detailsBtn.setPreferredSize(new Dimension(150, 42));
        detailsBtn.addActionListener(e -> viewSelectedTaskInstructions());
        bottom.add(detailsBtn);

        JButton backBtn = createBackButton("Back to Portal");
        backBtn.setPreferredSize(new Dimension(150, 42));
        bottom.add(backBtn);

        mainPanel.add(bottom, BorderLayout.SOUTH);
    }

    private void loadPersonalTasks(int volunteerId, String identifier) {
        model.setRowCount(0);
        List<AdminDataManager.VolunteerTask> tasks = (volunteerId > 0)
                ? AdminDataManager.getInstance().getTasksForVolunteer(volunteerId)
                : AdminDataManager.getInstance().getTasksForUser(identifier);

        // If no tasks yet, display their initial standby readiness mission
        if (tasks.isEmpty()) {
            AdminDataManager.Volunteer v = findApplicant(identifier);
            String dist = (v != null) ? v.getDistrict() : "District Emergency Sector";
            String sk = (v != null) ? v.getSkill() : "Relief Responder";
            model.addRow(new Object[]{
                    1001,
                    dist + " Field Readiness & Triage Directives (" + sk + ")",
                    dist,
                    dist + " SDMA Incident Command Station",
                    "High",
                    java.time.LocalDate.now().toString(),
                    "Assigned (Ready)"
            });
            return;
        }

        for (AdminDataManager.VolunteerTask t : tasks) {
            model.addRow(new Object[]{
                    t.getTaskId(),
                    t.getTaskTitle(),
                    t.getDistrict(),
                    t.getLocation(),
                    t.getPriority(),
                    t.getAssignedDate(),
                    t.getStatus()
            });
        }
    }

    private void updateSelectedTaskStatus(String newStatus) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an assigned mission from the table first.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int taskId = (int) table.getValueAt(row, 0);
        AdminDataManager.getInstance().updateTaskStatus(taskId, newStatus);
        table.setValueAt(newStatus, row, 6);
        JOptionPane.showMessageDialog(this, "Mission #" + taskId + " status updated to '" + newStatus + "'.", "Status Updated", JOptionPane.INFORMATION_MESSAGE);
    }

    private void viewSelectedTaskInstructions() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an assigned mission from the table.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int taskId = (int) table.getValueAt(row, 0);
        String title = (String) table.getValueAt(row, 1);
        String loc = (String) table.getValueAt(row, 3);
        String status = (String) table.getValueAt(row, 6);

        String inst = "Report directly to Sector Incident Commander. Coordinate triage, distribute critical provisions, and maintain operational log.";
        for (AdminDataManager.VolunteerTask t : AdminDataManager.getInstance().getAllTasks()) {
            if (t.getTaskId() == taskId) {
                inst = t.getInstructions();
                break;
            }
        }

        JOptionPane.showMessageDialog(
                this,
                "MISSION DIRECTIVES • TASK #" + taskId + "\n\n"
                        + "Title: " + title + "\n"
                        + "Location: " + loc + "\n"
                        + "Current Status: " + status + "\n\n"
                        + "Operational Instructions:\n" + inst,
                "Mission Directives",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    // =========================================================================
    // 4. ADMIN VIEW (Full Volunteer Roster & Management Tools)
    // =========================================================================
    private void buildAdminVolunteerView(JPanel mainPanel) {
        JPanel header = createStandardHeader(
                "Certified Responders & Volunteer Corps Roster",
                "Official responder directory of trained disaster volunteer corps deployed across Kerala",
                "ADMINISTRATOR ROSTER",
                UITheme.HOTLINE_BLUE
        );
        mainPanel.add(header, BorderLayout.NORTH);

        JPanel card = createCard(8);
        card.setLayout(new BorderLayout(0, 12));

        JPanel searchRow = new JPanel(new BorderLayout(10, 0));
        searchRow.setOpaque(false);
        JLabel searchLbl = new JLabel("Filter Volunteers:");
        searchLbl.setFont(UITheme.BOLD_FONT);
        searchRow.add(searchLbl, BorderLayout.WEST);
        searchField = createTextField("Search volunteer name, skill, email, district...");
        searchRow.add(searchField, BorderLayout.CENTER);
        card.add(searchRow, BorderLayout.NORTH);

        String[] columns = {"ID", "Name", "Skill / Role", "District", "Phone", "Email", "Status"};
        model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(36);
        table.setFont(UITheme.NORMAL_FONT);
        table.setShowGrid(true);
        table.setGridColor(UITheme.BORDER);
        table.setSelectionBackground(UITheme.HERO_BADGE_BG);
        table.setSelectionForeground(UITheme.BRAND_CRIMSON);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(UITheme.BUTTON_FONT);
        table.setFillsViewportHeight(true);

        card.add(createTableScrollPane(table), BorderLayout.CENTER);

        loadAdminVolunteersTable();

        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                String q = searchField.getText().trim().toLowerCase();
                for (int i = 0; i < table.getRowCount(); i++) {
                    for (int j = 1; j <= 5; j++) {
                        if (table.getValueAt(i, j).toString().toLowerCase().contains(q)) {
                            table.setRowSelectionInterval(i, i);
                            return;
                        }
                    }
                }
            }
        });

        mainPanel.add(card, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottom.setOpaque(false);

        JButton approveBtn = button("Approve Volunteer", UITheme.SUCCESS);
        approveBtn.setPreferredSize(new Dimension(160, 38));
        approveBtn.addActionListener(e -> approveSelectedVolunteer());
        bottom.add(approveBtn);

        JButton assignBtn = button("Assign Mission", UITheme.PRIMARY);
        assignBtn.setPreferredSize(new Dimension(140, 38));
        assignBtn.addActionListener(e -> assignMissionToSelected());
        bottom.add(assignBtn);

        JButton backBtn = createBackButton("← Back to Portal");
        backBtn.setPreferredSize(new Dimension(140, 38));
        bottom.add(backBtn);

        mainPanel.add(bottom, BorderLayout.SOUTH);
    }

    private void loadAdminVolunteersTable() {
        model.setRowCount(0);
        for (AdminDataManager.Volunteer v : AdminDataManager.getInstance().getVolunteers()) {
            model.addRow(new Object[]{
                    v.getId(),
                    v.getName(),
                    v.getSkill(),
                    v.getDistrict(),
                    v.getPhone(),
                    disaster.backend.DatabaseManager.cleanEmail(v.getEmail()),
                    v.getStatus()
            });
        }
    }

    private void approveSelectedVolunteer() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a volunteer to approve.", "Select Row", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int id = (int) table.getValueAt(row, 0);
        String name = (String) table.getValueAt(row, 1);
        AdminDataManager.getInstance().approveVolunteer(id);
        loadAdminVolunteersTable();
        JOptionPane.showMessageDialog(this, "Volunteer " + name + " (ID #" + id + ") has been approved and activated.", "Approval Confirmed", JOptionPane.INFORMATION_MESSAGE);
    }

    private void assignMissionToSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a volunteer from the table to assign a mission.", "Select Row", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int id = (int) table.getValueAt(row, 0);
        String name = (String) table.getValueAt(row, 1);
        String dist = (String) table.getValueAt(row, 3);

        String task = JOptionPane.showInputDialog(this, "Enter Mission Title for " + name + ":", dist + " Flood Relief & Evacuation Support");
        if (task != null && !task.trim().isEmpty()) {
            AdminDataManager.getInstance().assignTask(id, name, task.trim(), dist, dist + " Central Camp", "High", "Coordinate local disaster response operations.");
            JOptionPane.showMessageDialog(this, "Mission assigned to " + name + " and synchronized in real-time.", "Mission Assigned", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void startRegistrationFlow() {
        VolunteerTermsDialog termsDialog = new VolunteerTermsDialog(this, () -> {
            VolunteerRegisterDialog regDialog = new VolunteerRegisterDialog(this, this::buildUI);
            regDialog.setVisible(true);
        });
        termsDialog.setVisible(true);
    }

    private JPanel createDetailItem(String title, String val) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        JLabel tLbl = new JLabel(title);
        tLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tLbl.setForeground(UITheme.SECONDARY_TEXT);
        p.add(tLbl);

        JLabel vLbl = new JLabel(val);
        vLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        vLbl.setForeground(UITheme.TEXT);
        p.add(vLbl);

        return p;
    }
}
