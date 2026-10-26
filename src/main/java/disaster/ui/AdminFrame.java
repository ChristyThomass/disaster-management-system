package disaster.ui;

import disaster.service.AdminDataManager;
import disaster.service.AdminDataManager.*;
import disaster.service.UserSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Dedicated Admin Panel for SDMA portal.
 * Allows administrators to monitor and re-edit all system records
 * including Volunteers, Inventory Supplies, Shelter Homes, Alerts, and Bulletins.
 */
public class AdminFrame extends BaseFrame {

    private final AdminDataManager dataManager = AdminDataManager.getInstance();

    // Summary KPI Labels
    private JLabel shelterKpiCount;
    private JLabel volunteerKpiCount;
    private JLabel inventoryKpiCount;
    private JLabel alertKpiCount;

    // Tables & Models
    private DefaultTableModel shelterModel;
    private JTable shelterTable;

    private DefaultTableModel volunteerModel;
    private JTable volunteerTable;

    private DefaultTableModel inventoryModel;
    private JTable inventoryTable;

    private DefaultTableModel alertModel;
    private JTable alertTable;

    private DefaultTableModel bulletinModel;
    private JTable bulletinTable;

    private DefaultTableModel emergencyRecordsModel;
    private JTable emergencyRecordsTable;

    public AdminFrame() {
        super("Admin Panel", 1120, 720);
        setMinimumSize(new Dimension(980, 640));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        buildUI();

        // Listen for live data updates
        dataManager.addChangeListener(this::refreshAllTables);
    }

    private void buildUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 14));
        mainPanel.setBackground(UITheme.BACKGROUND);
        mainPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        // 1. TOP HEADER - Exclusively displays "Admin Panel" as requested
        JPanel topHeader = new JPanel(new BorderLayout(14, 0));
        topHeader.setBackground(Color.WHITE);
        topHeader.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1),
                new EmptyBorder(14, 20, 14, 20)
        ));

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JPanel badgeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        badgeRow.setOpaque(false);
        JPanel adminBadge = createBadge("AUTHORIZED SYSTEM COMMAND", UITheme.BRAND_CRIMSON, UITheme.HERO_BADGE_BG);
        badgeRow.add(adminBadge);
        titleBlock.add(badgeRow);

        titleBlock.add(Box.createVerticalStrut(4));

        JLabel titleLbl = new JLabel("Admin Panel");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLbl.setForeground(UITheme.BRAND_CRIMSON);
        titleBlock.add(titleLbl);

        JLabel subLbl = new JLabel("Central Disaster Management & Live Operational Control • Full Re-edit Access");
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLbl.setForeground(UITheme.SECONDARY_TEXT);
        titleBlock.add(subLbl);

        topHeader.add(titleBlock, BorderLayout.WEST);

        // Header Actions (Right): Switch Portal view & Sign Out
        JPanel rightHeaderActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        rightHeaderActions.setOpaque(false);

        JButton viewPortalBtn = createOutlinedButton("View Live Portal", UITheme.BORDER_DARK, UITheme.TEXT);
        viewPortalBtn.setPreferredSize(new Dimension(130, 36));
        viewPortalBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        viewPortalBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewPortalBtn.addActionListener(e -> {
            if (DashboardFrame.getActiveInstance() != null) {
                DashboardFrame.getActiveInstance().toFront();
                DashboardFrame.getActiveInstance().requestFocus();
            } else {
                new DashboardFrame("Admin Panel").setVisible(true);
            }
        });
        rightHeaderActions.add(viewPortalBtn);

        JButton signOutBtn = button("Sign Out", UITheme.BRAND_CRIMSON);
        signOutBtn.setPreferredSize(new Dimension(105, 36));
        signOutBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        signOutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        signOutBtn.addActionListener(e -> performAdminSignOut());
        rightHeaderActions.add(signOutBtn);

        topHeader.add(rightHeaderActions, BorderLayout.EAST);
        mainPanel.add(topHeader, BorderLayout.NORTH);

        // 2. CENTER CONTENT (KPI Metrics + Management Tabs)
        JPanel centerContent = new JPanel();
        centerContent.setLayout(new BoxLayout(centerContent, BoxLayout.Y_AXIS));
        centerContent.setOpaque(false);

        // KPI Summary Cards Row (matching home page)
        JPanel kpiRow = new JPanel(new GridLayout(1, 4, 12, 0));
        kpiRow.setOpaque(false);
        kpiRow.setMaximumSize(new Dimension(Short.MAX_VALUE, 85));

        shelterKpiCount = new JLabel();
        kpiRow.add(createKpiCard("RELIEF SHELTER HOMES", shelterKpiCount, "Active Occupancy & Zones", UITheme.INFO));

        volunteerKpiCount = new JLabel();
        kpiRow.add(createKpiCard("FIELD VOLUNTEERS", volunteerKpiCount, "Deployed / On Duty Ready", UITheme.SUCCESS));

        inventoryKpiCount = new JLabel();
        kpiRow.add(createKpiCard("WAREHOUSE INVENTORY", inventoryKpiCount, "Stocked Logistics Depots", UITheme.BRAND_CRIMSON));

        alertKpiCount = new JLabel();
        kpiRow.add(createKpiCard("CRITICAL BULLETINS", alertKpiCount, "Red Alerts & Advisories", UITheme.WARNING));

        centerContent.add(kpiRow);
        centerContent.add(Box.createVerticalStrut(14));

        // Management Tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabbedPane.setBackground(Color.WHITE);

        tabbedPane.addTab("🏕️  Shelter Homes", createShelterHomePanel());
        tabbedPane.addTab("👥  Volunteers Roster", createVolunteersPanel());
        tabbedPane.addTab("📦  Warehouse Supplies", createInventoryPanel());
        tabbedPane.addTab("☎️  Emergency Records", createEmergencyRecordsPanel());
        tabbedPane.addTab("⚠️  Disaster Alerts", createAlertsPanel());
        tabbedPane.addTab("📢  Live Ground Bulletins", createBulletinsPanel());

        centerContent.add(tabbedPane);
        mainPanel.add(centerContent, BorderLayout.CENTER);

        setContentPane(mainPanel);

        // Initial Data Populate
        refreshAllTables();
    }

    private JPanel createKpiCard(String title, JLabel valueLabel, String subtitle, Color themeColor) {
        JPanel card = createCard(8);
        card.setLayout(new BorderLayout(4, 4));
        card.setBorder(new EmptyBorder(10, 14, 10, 14));

        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 11));
        t.setForeground(UITheme.SECONDARY_TEXT);
        card.add(t, BorderLayout.NORTH);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(themeColor);
        card.add(valueLabel, BorderLayout.CENTER);

        JLabel sub = new JLabel(subtitle);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sub.setForeground(UITheme.MUTED);
        card.add(sub, BorderLayout.SOUTH);

        return card;
    }

    // =========================================================================
    // 1. SHELTER HOMES PANEL
    // =========================================================================
    private JPanel createShelterHomePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(12, 14, 12, 14));

        // Action Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);

        JButton addBtn = button("+ Add Shelter Home", UITheme.PRIMARY);
        addBtn.setPreferredSize(new Dimension(160, 34));
        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        addBtn.addActionListener(e -> showAddShelterDialog());
        toolbar.add(addBtn);

        JButton editBtn = createOutlinedButton("Edit Selected Shelter", UITheme.BRAND_CRIMSON, UITheme.BRAND_CRIMSON);
        editBtn.setPreferredSize(new Dimension(170, 34));
        editBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        editBtn.addActionListener(e -> showEditShelterDialog());
        toolbar.add(editBtn);

        JButton deleteBtn = createOutlinedButton("Delete Shelter", UITheme.BORDER_DARK, UITheme.DANGER);
        deleteBtn.setPreferredSize(new Dimension(130, 34));
        deleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        deleteBtn.addActionListener(e -> deleteSelectedShelter());
        toolbar.add(deleteBtn);

        panel.add(toolbar, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Shelter Home / Camp Name", "District", "Occupancy", "Max Capacity", "Coordinator Name", "Contact Phone", "Status"};
        shelterModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        shelterTable = createStyledTable(shelterModel);
        panel.add(new JScrollPane(shelterTable), BorderLayout.CENTER);

        return panel;
    }

    private void showAddShelterDialog() {
        JTextField nameF = createTextField("e.g. St. Joseph Relief Camp");
        JTextField distF = createTextField("e.g. Alappuzha");
        JTextField occF = createTextField("e.g. 200");
        JTextField capF = createTextField("e.g. 500");
        JTextField coordF = createTextField("e.g. Sri. Ramesh V.");
        JTextField phoneF = createTextField("e.g. +91 98470 12345");
        JTextField statusF = createTextField("e.g. Active (Food & Medical Supply)");

        JPanel form = createFormGrid(
                new String[]{"Shelter Name:", "District / Region:", "Current Occupancy:", "Max Capacity:", "Camp Coordinator:", "Emergency Phone:", "Operational Status:"},
                new JComponent[]{nameF, distF, occF, capF, coordF, phoneF, statusF}
        );

        int res = JOptionPane.showConfirmDialog(this, form, "Add New Shelter Home", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            String name = nameF.getText().trim();
            String dist = distF.getText().trim();
            String coord = coordF.getText().trim();
            String phone = phoneF.getText().trim();
            String status = statusF.getText().trim();
            int occ = 0, cap = 100;
            try { occ = Integer.parseInt(occF.getText().trim()); } catch (Exception ignored) {}
            try { cap = Integer.parseInt(capF.getText().trim()); } catch (Exception ignored) {}

            if (name.isEmpty() || dist.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please provide at least a shelter name and district.", "Input Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            dataManager.addShelterHome(name, dist, occ, cap, coord, phone, status);
        }
    }

    private void showEditShelterDialog() {
        int row = shelterTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a shelter home from the table to edit.", "No Row Selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int id = (int) shelterModel.getValueAt(row, 0);
        String curName = (String) shelterModel.getValueAt(row, 1);
        String curDist = (String) shelterModel.getValueAt(row, 2);
        int curOcc = (int) shelterModel.getValueAt(row, 3);
        int curCap = (int) shelterModel.getValueAt(row, 4);
        String curCoord = (String) shelterModel.getValueAt(row, 5);
        String curPhone = (String) shelterModel.getValueAt(row, 6);
        String curStatus = (String) shelterModel.getValueAt(row, 7);

        JTextField nameF = createTextField(""); nameF.setText(curName);
        JTextField distF = createTextField(""); distF.setText(curDist);
        JTextField occF = createTextField(""); occF.setText(String.valueOf(curOcc));
        JTextField capF = createTextField(""); capF.setText(String.valueOf(curCap));
        JTextField coordF = createTextField(""); coordF.setText(curCoord);
        JTextField phoneF = createTextField(""); phoneF.setText(curPhone);
        JTextField statusF = createTextField(""); statusF.setText(curStatus);

        JPanel form = createFormGrid(
                new String[]{"Shelter Name:", "District / Region:", "Current Occupancy:", "Max Capacity:", "Camp Coordinator:", "Emergency Phone:", "Operational Status:"},
                new JComponent[]{nameF, distF, occF, capF, coordF, phoneF, statusF}
        );

        int res = JOptionPane.showConfirmDialog(this, form, "Re-edit Shelter Home #" + id, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            int occ = curOcc, cap = curCap;
            try { occ = Integer.parseInt(occF.getText().trim()); } catch (Exception ignored) {}
            try { cap = Integer.parseInt(capF.getText().trim()); } catch (Exception ignored) {}
            dataManager.updateShelterHome(id, nameF.getText().trim(), distF.getText().trim(), occ, cap, coordF.getText().trim(), phoneF.getText().trim(), statusF.getText().trim());
        }
    }

    private void deleteSelectedShelter() {
        int row = shelterTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a shelter home to delete.", "Select Row", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int id = (int) shelterModel.getValueAt(row, 0);
        String name = (String) shelterModel.getValueAt(row, 1);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete shelter:\n" + name + " (ID #" + id + ")?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dataManager.deleteShelterHome(id);
        }
    }

    // =========================================================================
    // 2. VOLUNTEERS PANEL
    // =========================================================================
    private JPanel createVolunteersPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(12, 14, 12, 14));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);

        JButton approveBtn = button("Approve Application", UITheme.SUCCESS);
        approveBtn.setPreferredSize(new Dimension(175, 36));
        approveBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        approveBtn.addActionListener(e -> approveSelectedVolunteer());
        toolbar.add(approveBtn);

        JButton rejectBtn = createOutlinedButton("Reject", UITheme.BORDER_DARK, UITheme.DANGER);
        rejectBtn.setPreferredSize(new Dimension(100, 36));
        rejectBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        rejectBtn.addActionListener(e -> rejectSelectedVolunteer());
        toolbar.add(rejectBtn);

        JButton viewDocBtn = createOutlinedButton("View ID & Cert", UITheme.BORDER_DARK, UITheme.TEXT);
        viewDocBtn.setPreferredSize(new Dimension(145, 36));
        viewDocBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        viewDocBtn.addActionListener(e -> viewSelectedVolunteerDocuments());
        toolbar.add(viewDocBtn);

        JButton assignWorkBtn = button("Assign Work", UITheme.PRIMARY);
        assignWorkBtn.setPreferredSize(new Dimension(140, 36));
        assignWorkBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        assignWorkBtn.addActionListener(e -> assignWorkToSelectedVolunteer());
        toolbar.add(assignWorkBtn);

        JButton viewWorksBtn = createOutlinedButton("All Field Works", UITheme.BRAND_CRIMSON, UITheme.BRAND_CRIMSON);
        viewWorksBtn.setPreferredSize(new Dimension(150, 36));
        viewWorksBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        viewWorksBtn.addActionListener(e -> new VolunteerWorksFrame("admin").setVisible(true));
        toolbar.add(viewWorksBtn);

        JButton addBtn = createOutlinedButton("+ Add Volunteer", UITheme.BORDER_DARK, UITheme.PRIMARY);
        addBtn.setPreferredSize(new Dimension(140, 36));
        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        addBtn.addActionListener(e -> showAddVolunteerDialog());
        toolbar.add(addBtn);

        JButton deleteBtn = createOutlinedButton("Delete", UITheme.BORDER_DARK, UITheme.DANGER);
        deleteBtn.setPreferredSize(new Dimension(95, 36));
        deleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        deleteBtn.addActionListener(e -> deleteSelectedVolunteer());
        toolbar.add(deleteBtn);

        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {
                "ID",
                "Volunteer Name",
                "Email Address",
                "Contact Phone",
                "District / Sector",
                "Certified Skillset",
                "ID Proof Document",
                "Certificate",
                "Status"
        };
        volunteerModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        volunteerTable = createStyledTable(volunteerModel);
        volunteerTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        volunteerTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        volunteerTable.getColumnModel().getColumn(2).setPreferredWidth(160);
        volunteerTable.getColumnModel().getColumn(3).setPreferredWidth(110);
        volunteerTable.getColumnModel().getColumn(4).setPreferredWidth(110);
        volunteerTable.getColumnModel().getColumn(5).setPreferredWidth(160);
        volunteerTable.getColumnModel().getColumn(6).setPreferredWidth(120);
        volunteerTable.getColumnModel().getColumn(7).setPreferredWidth(120);
        volunteerTable.getColumnModel().getColumn(8).setPreferredWidth(100);

        // Status column styling
        volunteerTable.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setFont(new Font("Segoe UI", Font.BOLD, 12));
                String v = String.valueOf(value).toLowerCase();
                if (v.contains("pending")) {
                    l.setForeground(new Color(217, 119, 6)); // Orange
                } else if (v.contains("approved") || v.contains("active") || v.contains("deployed") || v.contains("on duty")) {
                    l.setForeground(UITheme.SUCCESS); // Green
                } else if (v.contains("rejected")) {
                    l.setForeground(UITheme.DANGER); // Red
                } else {
                    l.setForeground(UITheme.TEXT);
                }
                return l;
            }
        });

        panel.add(new JScrollPane(volunteerTable), BorderLayout.CENTER);

        return panel;
    }

    private void approveSelectedVolunteer() {
        int row = volunteerTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a volunteer from the table to approve.", "No Row Selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int id = (int) volunteerModel.getValueAt(row, 0);
        String name = (String) volunteerModel.getValueAt(row, 1);
        boolean ok = dataManager.approveVolunteer(id);
        if (ok) {
            // Update in DB if online
            try (java.sql.Connection conn = disaster.backend.DatabaseManager.getInstance().getConnection()) {
                if (conn != null) {
                    try (java.sql.PreparedStatement ps = conn.prepareStatement("UPDATE volunteers SET status='Approved' WHERE id=?")) {
                        ps.setInt(1, id);
                        ps.executeUpdate();
                    }
                }
            } catch (Exception ignored) {}

            refreshAllTables();
            JOptionPane.showMessageDialog(
                    this,
                    "✓ VOLUNTEER REGISTRATION APPROVED!\n\n"
                            + "Volunteer: " + name + " (ID #" + id + ")\n"
                            + "Status: Approved & Deployment Ready\n\n"
                            + "This volunteer now has immediate read access to the full Volunteer Roster\n"
                            + "and can be assigned field disaster relief works.",
                    "Application Approved",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    private void rejectSelectedVolunteer() {
        int row = volunteerTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a volunteer from the table to reject.", "No Row Selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int id = (int) volunteerModel.getValueAt(row, 0);
        String name = (String) volunteerModel.getValueAt(row, 1);
        int conf = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to REJECT the volunteer application for:\n" + name + " (ID #" + id + ")?",
                "Confirm Rejection",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (conf == JOptionPane.YES_OPTION) {
            dataManager.rejectVolunteer(id);
            try (java.sql.Connection conn = disaster.backend.DatabaseManager.getInstance().getConnection()) {
                if (conn != null) {
                    try (java.sql.PreparedStatement ps = conn.prepareStatement("UPDATE volunteers SET status='Rejected' WHERE id=?")) {
                        ps.setInt(1, id);
                        ps.executeUpdate();
                    }
                }
            } catch (Exception ignored) {}
            refreshAllTables();
        }
    }

    private void viewSelectedVolunteerDocuments() {
        int row = volunteerTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a volunteer row from the table first.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int id = (int) volunteerModel.getValueAt(row, 0);
        String name = (String) volunteerModel.getValueAt(row, 1);
        String idDoc = (String) volunteerModel.getValueAt(row, 6);
        String certDoc = (String) volunteerModel.getValueAt(row, 7);
        String status = (String) volunteerModel.getValueAt(row, 8);

        JOptionPane.showMessageDialog(
                this,
                "VERIFICATION DOCUMENTS FOR VOLUNTEER #" + id + "\n\n"
                        + "• Applicant Legal Name: " + name + "\n"
                        + "• Current Status: " + status + "\n\n"
                        + "📁 Government Identity Proof:\n"
                        + "   " + idDoc + "\n\n"
                        + "📜 Training & Skill Credentials:\n"
                        + "   " + certDoc + "\n\n"
                        + "Click 'Approve Application' in the toolbar to grant active status.",
                "Volunteer Credentials & Documents",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void assignWorkToSelectedVolunteer() {
        int row = volunteerTable.getSelectedRow();
        Volunteer targetVol = null;
        if (row >= 0) {
            int id = (int) volunteerModel.getValueAt(row, 0);
            targetVol = dataManager.getVolunteerById(id);
        }

        if (targetVol == null) {
            List<Volunteer> approved = dataManager.getApprovedVolunteers();
            if (approved.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No approved volunteers available to assign tasks.\nPlease approve an applicant first.", "No Volunteers", JOptionPane.WARNING_MESSAGE);
                return;
            }
            targetVol = approved.get(0);
        }

        JTextField titleF = createTextField("e.g. Flash Flood Water Rescue & Evacuation");
        JComboBox<String> distCombo = new JComboBox<>(disaster.service.VolunteerSkillsData.KERALA_DISTRICTS);
        distCombo.setSelectedItem(targetVol.getDistrict());
        JTextField locF = createTextField("e.g. Relief Camp Ward #5");
        JComboBox<String> prioCombo = new JComboBox<>(new String[]{"Critical", "High", "Medium", "Low"});
        JTextArea instArea = new JTextArea(3, 20);
        instArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        instArea.setLineWrap(true);
        instArea.setWrapStyleWord(true);
        instArea.setText("Deploy with emergency team and maintain line-of-sight communications.");

        JPanel form = createFormGrid(
                new String[]{"Assigned Volunteer:", "Work / Mission Title:", "District / Sector:", "Specific Location:", "Operational Priority:", "Field Guidelines:"},
                new JComponent[]{
                        new JLabel(targetVol.getName() + " (ID #" + targetVol.getId() + " - " + targetVol.getSkill() + ")"),
                        titleF, distCombo, locF, prioCombo, new JScrollPane(instArea)
                }
        );

        int res = JOptionPane.showConfirmDialog(this, form, "Assign Disaster Deployment Work", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            String title = titleF.getText().trim();
            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Mission title is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            dataManager.assignTask(
                    targetVol.getId(),
                    targetVol.getName(),
                    title,
                    (String) distCombo.getSelectedItem(),
                    locF.getText().trim(),
                    (String) prioCombo.getSelectedItem(),
                    instArea.getText().trim()
            );

            JOptionPane.showMessageDialog(
                    this,
                    "✓ FIELD WORK DIRECTIVE DISPATCHED!\n\n"
                            + "Assigned Volunteer: " + targetVol.getName() + "\n"
                            + "Mission: " + title + "\n"
                            + "Sector: " + distCombo.getSelectedItem() + " - " + locF.getText().trim() + "\n\n"
                            + "The volunteer can view this mission in their 'Assigned Works' window.",
                    "Mission Assigned",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    private void showAddVolunteerDialog() {
        JTextField nameF = createTextField("e.g. Sreekumar N.");
        JTextField emailF = createTextField("e.g. sreekumar@gmail.com");
        JTextField phoneF = createTextField("e.g. +91 94470 54321");
        JComboBox<String> distC = new JComboBox<>(disaster.service.VolunteerSkillsData.KERALA_DISTRICTS);
        JTextField skillF = createTextField("e.g. First Aid & Evacuation Support");
        JComboBox<String> statusC = new JComboBox<>(new String[]{"Approved", "Active", "Deployed", "On Duty", "Pending"});

        JPanel form = createFormGrid(
                new String[]{"Volunteer Full Name:", "Email Address:", "Contact Phone:", "District:", "Skill / Specialty:", "Deployment Status:"},
                new JComponent[]{nameF, emailF, phoneF, distC, skillF, statusC}
        );

        int res = JOptionPane.showConfirmDialog(this, form, "Add New Certified Volunteer", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            if (nameF.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Volunteer name is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            dataManager.addVolunteerApplication(
                    nameF.getText().trim().toLowerCase().replaceAll("\\s+", "_"),
                    nameF.getText().trim(),
                    emailF.getText().trim(),
                    phoneF.getText().trim(),
                    (String) distC.getSelectedItem(),
                    skillF.getText().trim(),
                    "Admin Enrolled ID",
                    "Admin Enrolled Cert"
            );
            if (!"Pending".equalsIgnoreCase((String) statusC.getSelectedItem())) {
                List<Volunteer> all = dataManager.getVolunteers();
                if (!all.isEmpty()) {
                    all.get(all.size() - 1).setStatus((String) statusC.getSelectedItem());
                }
            }
            refreshAllTables();
        }
    }

    private void showEditVolunteerDialog() {
        int row = volunteerTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a volunteer from the table to edit.", "No Row Selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int id = (int) volunteerModel.getValueAt(row, 0);
        JTextField nameF = createTextField(""); nameF.setText((String) volunteerModel.getValueAt(row, 1));
        JTextField emailF = createTextField(""); emailF.setText((String) volunteerModel.getValueAt(row, 2));
        JTextField phoneF = createTextField(""); phoneF.setText((String) volunteerModel.getValueAt(row, 3));
        JComboBox<String> distC = new JComboBox<>(disaster.service.VolunteerSkillsData.KERALA_DISTRICTS);
        distC.setSelectedItem(volunteerModel.getValueAt(row, 4));
        JTextField skillF = createTextField(""); skillF.setText((String) volunteerModel.getValueAt(row, 5));
        JComboBox<String> statusC = new JComboBox<>(new String[]{"Approved", "Active", "Deployed", "On Duty", "Pending", "Rejected"});
        statusC.setSelectedItem(volunteerModel.getValueAt(row, 8));

        JPanel form = createFormGrid(
                new String[]{"Volunteer Full Name:", "Email Address:", "Contact Phone:", "District:", "Skill / Specialty:", "Deployment Status:"},
                new JComponent[]{nameF, emailF, phoneF, distC, skillF, statusC}
        );

        int res = JOptionPane.showConfirmDialog(this, form, "Re-edit Volunteer #" + id, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            Volunteer v = dataManager.getVolunteerById(id);
            if (v != null) {
                v.setName(nameF.getText().trim());
                v.setEmail(emailF.getText().trim());
                v.setPhone(phoneF.getText().trim());
                v.setDistrict((String) distC.getSelectedItem());
                v.setSkill(skillF.getText().trim());
                v.setStatus((String) statusC.getSelectedItem());
                refreshAllTables();
            }
        }
    }

    private void deleteSelectedVolunteer() {
        int row = volunteerTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a volunteer to delete.", "Select Row", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int id = (int) volunteerModel.getValueAt(row, 0);
        String name = (String) volunteerModel.getValueAt(row, 1);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove volunteer:\n" + name + " (ID #" + id + ")?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dataManager.deleteVolunteer(id);
            refreshAllTables();
        }
    }


    // =========================================================================
    // 3. INVENTORY PANEL
    // =========================================================================
    private JPanel createInventoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(12, 14, 12, 14));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);

        JButton addBtn = button("+ Add Supply Item", UITheme.PRIMARY);
        addBtn.setPreferredSize(new Dimension(160, 34));
        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        addBtn.addActionListener(e -> showAddInventoryDialog());
        toolbar.add(addBtn);

        JButton editBtn = createOutlinedButton("Edit Selected Supply", UITheme.BRAND_CRIMSON, UITheme.BRAND_CRIMSON);
        editBtn.setPreferredSize(new Dimension(175, 34));
        editBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        editBtn.addActionListener(e -> showEditInventoryDialog());
        toolbar.add(editBtn);

        JButton deleteBtn = createOutlinedButton("Delete Item", UITheme.BORDER_DARK, UITheme.DANGER);
        deleteBtn.setPreferredSize(new Dimension(120, 34));
        deleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        deleteBtn.addActionListener(e -> deleteSelectedInventory());
        toolbar.add(deleteBtn);

        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"ID", "Item / Supply Category", "Stationed Depot", "Quantity Available", "Unit Type", "Depot Status"};
        inventoryModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        inventoryTable = createStyledTable(inventoryModel);
        panel.add(new JScrollPane(inventoryTable), BorderLayout.CENTER);

        return panel;
    }

    private void showAddInventoryDialog() {
        JTextField nameF = createTextField("e.g. Life Jackets & Buoys");
        JTextField depotF = createTextField("e.g. Alappuzha Logistics Hub");
        JTextField qtyF = createTextField("e.g. 500");
        JTextField unitF = createTextField("e.g. Units");
        JTextField statusF = createTextField("e.g. Certified Stocked & Ready");

        JPanel form = createFormGrid(
                new String[]{"Supply Item Name:", "Stationed Depot:", "Quantity Available:", "Unit (Units/Kits/Liters):", "Depot Status:"},
                new JComponent[]{nameF, depotF, qtyF, unitF, statusF}
        );

        int res = JOptionPane.showConfirmDialog(this, form, "Add Warehouse Supply Item", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            if (nameF.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Item name is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            dataManager.addInventoryItem(nameF.getText().trim(), depotF.getText().trim(), qtyF.getText().trim(), unitF.getText().trim(), statusF.getText().trim());
        }
    }

    private void showEditInventoryDialog() {
        int row = inventoryTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an inventory item from the table to edit.", "No Row Selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int id = (int) inventoryModel.getValueAt(row, 0);
        JTextField nameF = createTextField(""); nameF.setText((String) inventoryModel.getValueAt(row, 1));
        JTextField depotF = createTextField(""); depotF.setText((String) inventoryModel.getValueAt(row, 2));
        JTextField qtyF = createTextField(""); qtyF.setText((String) inventoryModel.getValueAt(row, 3));
        JTextField unitF = createTextField(""); unitF.setText((String) inventoryModel.getValueAt(row, 4));
        JTextField statusF = createTextField(""); statusF.setText((String) inventoryModel.getValueAt(row, 5));

        JPanel form = createFormGrid(
                new String[]{"Supply Item Name:", "Stationed Depot:", "Quantity Available:", "Unit Type:", "Depot Status:"},
                new JComponent[]{nameF, depotF, qtyF, unitF, statusF}
        );

        int res = JOptionPane.showConfirmDialog(this, form, "Re-edit Inventory Item #" + id, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            dataManager.updateInventoryItem(id, nameF.getText().trim(), depotF.getText().trim(), qtyF.getText().trim(), unitF.getText().trim(), statusF.getText().trim());
        }
    }

    private void deleteSelectedInventory() {
        int row = inventoryTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an item to delete.", "Select Row", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int id = (int) inventoryModel.getValueAt(row, 0);
        String name = (String) inventoryModel.getValueAt(row, 1);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete supply item:\n" + name + " (ID #" + id + ")?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dataManager.deleteInventoryItem(id);
        }
    }

    // =========================================================================
    // EMERGENCY RECORDS PANEL (ADMIN RECORD SECTION)
    // =========================================================================
    private JPanel createEmergencyRecordsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(12, 14, 12, 14));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);

        JButton addBtn = button("Add New Record", UITheme.PRIMARY);
        addBtn.setPreferredSize(new Dimension(160, 36));
        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        addBtn.addActionListener(e -> showAddEmergencyRecordDialog());
        toolbar.add(addBtn);

        JButton editBtn = createOutlinedButton("Edit Selected Record", UITheme.HOTLINE_BLUE, UITheme.HOTLINE_BLUE);
        editBtn.setPreferredSize(new Dimension(175, 36));
        editBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        editBtn.addActionListener(e -> showEditEmergencyRecordDialog());
        toolbar.add(editBtn);

        JButton deleteBtn = createOutlinedButton("Delete Record", UITheme.BRAND_CRIMSON, UITheme.BRAND_CRIMSON);
        deleteBtn.setPreferredSize(new Dimension(140, 36));
        deleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        deleteBtn.addActionListener(e -> deleteSelectedEmergencyRecord());
        toolbar.add(deleteBtn);

        JButton openWindowBtn = createOutlinedButton("Open Directory Window", UITheme.BORDER_DARK, UITheme.TEXT);
        openWindowBtn.setPreferredSize(new Dimension(185, 36));
        openWindowBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        openWindowBtn.addActionListener(e -> new EmergencyContactsFrame(true).setVisible(true));
        toolbar.add(openWindowBtn);

        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"Record ID", "Service / Response Wing", "Designated Department", "Emergency Hotline", "Operational Status"};
        emergencyRecordsModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        emergencyRecordsTable = createStyledTable(emergencyRecordsModel);
        panel.add(new JScrollPane(emergencyRecordsTable), BorderLayout.CENTER);

        return panel;
    }

    private void showAddEmergencyRecordDialog() {
        JTextField nameF = createTextField("");
        JTextField deptF = createTextField("Disaster Rapid Response Desk");
        JTextField phoneF = createTextField("");
        JTextField statusF = createTextField("Live Connected");

        JPanel form = createFormGrid(new String[]{
                "Service / Contact Name:",
                "Designated Department:",
                "Emergency Hotline / Phone:",
                "Operational Status:"
        }, new JComponent[]{nameF, deptF, phoneF, statusF});

        int res = JOptionPane.showConfirmDialog(this, form, "Add Emergency Record", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            String name = nameF.getText().trim();
            String dept = deptF.getText().trim();
            String phone = phoneF.getText().trim();
            String status = statusF.getText().trim();
            if (name.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Both Name and Phone are required.", "Input Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int uid = 1;
            try { uid = Integer.parseInt(disaster.service.UserSession.getCurrentUser().getUserId()); } catch (Exception ignored) {}
            new disaster.dao.EmergencyContactDAO().saveContact(uid, name, phone);
            disaster.backend.DatabaseManager.getInstance().saveEmergencyContact(uid, name, phone);
            refreshAllTables();
            JOptionPane.showMessageDialog(this, "Emergency record added to database.", "Record Saved", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showEditEmergencyRecordDialog() {
        int row = emergencyRecordsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an emergency record from the table to edit.", "Select Row", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String oldName = String.valueOf(emergencyRecordsModel.getValueAt(row, 1));
        String oldDept = String.valueOf(emergencyRecordsModel.getValueAt(row, 2));
        String oldPhone = String.valueOf(emergencyRecordsModel.getValueAt(row, 3));
        String oldStatus = String.valueOf(emergencyRecordsModel.getValueAt(row, 4));

        JTextField nameF = createTextField(""); nameF.setText(oldName);
        JTextField deptF = createTextField(""); deptF.setText(oldDept);
        JTextField phoneF = createTextField(""); phoneF.setText(oldPhone);
        JTextField statusF = createTextField(""); statusF.setText(oldStatus);

        JPanel form = createFormGrid(new String[]{
                "Service / Contact Name:",
                "Designated Department:",
                "Emergency Hotline / Phone:",
                "Operational Status:"
        }, new JComponent[]{nameF, deptF, phoneF, statusF});

        int res = JOptionPane.showConfirmDialog(this, form, "Re-edit Emergency Record: " + oldName, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            String newName = nameF.getText().trim();
            String newDept = deptF.getText().trim();
            String newPhone = phoneF.getText().trim();
            String newStatus = statusF.getText().trim();
            if (newName.isEmpty() || newPhone.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Both Name and Phone are required.", "Input Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            new disaster.dao.EmergencyContactDAO().updateContact(oldName, oldPhone, newName, newPhone);
            disaster.backend.DatabaseManager.getInstance().updateEmergencyContact(oldName, oldPhone, newName, newPhone);
            emergencyRecordsModel.setValueAt(newName, row, 1);
            emergencyRecordsModel.setValueAt(newDept, row, 2);
            emergencyRecordsModel.setValueAt(newPhone, row, 3);
            emergencyRecordsModel.setValueAt(newStatus, row, 4);
            JOptionPane.showMessageDialog(this, "Emergency record updated successfully.", "Record Updated", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void deleteSelectedEmergencyRecord() {
        int row = emergencyRecordsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an emergency record to delete.", "Select Row", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String name = String.valueOf(emergencyRecordsModel.getValueAt(row, 1));
        String phone = String.valueOf(emergencyRecordsModel.getValueAt(row, 3));
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete emergency record:\n" + name + " (" + phone + ")?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            new disaster.dao.EmergencyContactDAO().deleteContact(name, phone);
            disaster.backend.DatabaseManager.getInstance().deleteEmergencyContact(name, phone);
            emergencyRecordsModel.removeRow(row);
            JOptionPane.showMessageDialog(this, "Record deleted successfully.", "Record Removed", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // =========================================================================
    // 4. DISASTER ALERTS PANEL
    // =========================================================================
    private JPanel createAlertsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(12, 14, 12, 14));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);

        JButton addBtn = button("+ Issue New Alert", UITheme.PRIMARY);
        addBtn.setPreferredSize(new Dimension(150, 34));
        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        addBtn.addActionListener(e -> showAddAlertDialog());
        toolbar.add(addBtn);

        JButton editBtn = createOutlinedButton("Edit Selected Alert", UITheme.BRAND_CRIMSON, UITheme.BRAND_CRIMSON);
        editBtn.setPreferredSize(new Dimension(160, 34));
        editBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        editBtn.addActionListener(e -> showEditAlertDialog());
        toolbar.add(editBtn);

        JButton deleteBtn = createOutlinedButton("Revoke Alert", UITheme.BORDER_DARK, UITheme.DANGER);
        deleteBtn.setPreferredSize(new Dimension(120, 34));
        deleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        deleteBtn.addActionListener(e -> deleteSelectedAlert());
        toolbar.add(deleteBtn);

        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"ID", "Hazard Nature", "District / Region", "Warning Level", "Affected Population", "Current Status"};
        alertModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        alertTable = createStyledTable(alertModel);
        panel.add(new JScrollPane(alertTable), BorderLayout.CENTER);

        return panel;
    }

    private void showAddAlertDialog() {
        JTextField hazardF = createTextField("e.g. High River Swell Alert");
        JTextField distF = createTextField("e.g. Pathanamthitta");
        JComboBox<String> levelC = new JComboBox<>(new String[]{"Critical", "High", "Medium", "Advisory"});
        JTextField affF = createTextField("e.g. 15,000");
        JTextField statusF = createTextField("e.g. Active Monitoring");

        JPanel form = createFormGrid(
                new String[]{"Hazard Title / Type:", "District / Region:", "Warning Level:", "Affected Population:", "Current Status:"},
                new JComponent[]{hazardF, distF, levelC, affF, statusF}
        );

        int res = JOptionPane.showConfirmDialog(this, form, "Issue New Disaster Warning", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            if (hazardF.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Hazard nature title is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            dataManager.addAlert(hazardF.getText().trim(), distF.getText().trim(), (String) levelC.getSelectedItem(), affF.getText().trim(), statusF.getText().trim());
        }
    }

    private void showEditAlertDialog() {
        int row = alertTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an alert from the table to edit.", "No Row Selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int id = (int) alertModel.getValueAt(row, 0);
        JTextField hazardF = createTextField(""); hazardF.setText((String) alertModel.getValueAt(row, 1));
        JTextField distF = createTextField(""); distF.setText((String) alertModel.getValueAt(row, 2));
        JComboBox<String> levelC = new JComboBox<>(new String[]{"Critical", "High", "Medium", "Advisory"});
        levelC.setSelectedItem(alertModel.getValueAt(row, 3));
        JTextField affF = createTextField(""); affF.setText((String) alertModel.getValueAt(row, 4));
        JTextField statusF = createTextField(""); statusF.setText((String) alertModel.getValueAt(row, 5));

        JPanel form = createFormGrid(
                new String[]{"Hazard Title / Type:", "District / Region:", "Warning Level:", "Affected Population:", "Current Status:"},
                new JComponent[]{hazardF, distF, levelC, affF, statusF}
        );

        int res = JOptionPane.showConfirmDialog(this, form, "Re-edit Disaster Alert #" + id, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            dataManager.updateAlert(id, hazardF.getText().trim(), distF.getText().trim(), (String) levelC.getSelectedItem(), affF.getText().trim(), statusF.getText().trim());
        }
    }

    private void deleteSelectedAlert() {
        int row = alertTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an alert to revoke.", "Select Row", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int id = (int) alertModel.getValueAt(row, 0);
        String hazard = (String) alertModel.getValueAt(row, 1);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to revoke / delete alert:\n" + hazard + " (ID #" + id + ")?", "Confirm Revoke", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dataManager.deleteAlert(id);
        }
    }

    // =========================================================================
    // 5. GROUND BULLETINS PANEL
    // =========================================================================
    private JPanel createBulletinsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(12, 14, 12, 14));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);

        JButton addBtn = button("+ Post Ground Bulletin", UITheme.PRIMARY);
        addBtn.setPreferredSize(new Dimension(175, 34));
        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        addBtn.addActionListener(e -> showAddBulletinDialog());
        toolbar.add(addBtn);

        JButton editBtn = createOutlinedButton("Edit Selected Bulletin", UITheme.BRAND_CRIMSON, UITheme.BRAND_CRIMSON);
        editBtn.setPreferredSize(new Dimension(175, 34));
        editBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        editBtn.addActionListener(e -> showEditBulletinDialog());
        toolbar.add(editBtn);

        JButton deleteBtn = createOutlinedButton("Delete Bulletin", UITheme.BORDER_DARK, UITheme.DANGER);
        deleteBtn.setPreferredSize(new Dimension(130, 34));
        deleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        deleteBtn.addActionListener(e -> deleteSelectedBulletin());
        toolbar.add(deleteBtn);

        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"ID", "Timestamp (IST)", "Ground Intelligence Broadcast Message", "Severity Tag"};
        bulletinModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        bulletinTable = createStyledTable(bulletinModel);
        panel.add(new JScrollPane(bulletinTable), BorderLayout.CENTER);

        return panel;
    }

    private void showAddBulletinDialog() {
        JTextField timeF = createTextField("e.g. 19:15 IST");
        JTextField msgF = createTextField("e.g. Additional medical triage team deployed to Kuttanad.");
        JComboBox<String> sevC = new JComboBox<>(new String[]{"DANGER", "PRIMARY", "SUCCESS"});

        JPanel form = createFormGrid(
                new String[]{"Broadcast Timestamp:", "Bulletin Message:", "Severity Tag (Color):"},
                new JComponent[]{timeF, msgF, sevC}
        );

        int res = JOptionPane.showConfirmDialog(this, form, "Broadcast Ground Bulletin", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            if (msgF.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Bulletin message is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            dataManager.addBulletin(timeF.getText().trim(), msgF.getText().trim(), (String) sevC.getSelectedItem());
        }
    }

    private void showEditBulletinDialog() {
        int row = bulletinTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a bulletin from the table to edit.", "No Row Selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int id = (int) bulletinModel.getValueAt(row, 0);
        JTextField timeF = createTextField(""); timeF.setText((String) bulletinModel.getValueAt(row, 1));
        JTextField msgF = createTextField(""); msgF.setText((String) bulletinModel.getValueAt(row, 2));
        JComboBox<String> sevC = new JComboBox<>(new String[]{"DANGER", "PRIMARY", "SUCCESS"});
        sevC.setSelectedItem(bulletinModel.getValueAt(row, 3));

        JPanel form = createFormGrid(
                new String[]{"Broadcast Timestamp:", "Bulletin Message:", "Severity Tag (Color):"},
                new JComponent[]{timeF, msgF, sevC}
        );

        int res = JOptionPane.showConfirmDialog(this, form, "Re-edit Bulletin #" + id, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            dataManager.updateBulletin(id, timeF.getText().trim(), msgF.getText().trim(), (String) sevC.getSelectedItem());
        }
    }

    private void deleteSelectedBulletin() {
        int row = bulletinTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a bulletin to delete.", "Select Row", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int id = (int) bulletinModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove bulletin #" + id + "?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dataManager.deleteBulletin(id);
        }
    }

    // =========================================================================
    // REFRESH DATA & RE-RENDER
    // =========================================================================
    private void refreshAllTables() {
        // 1. Shelters
        List<ShelterHome> shelters = dataManager.getShelterHomes();
        shelterModel.setRowCount(0);
        for (ShelterHome s : shelters) {
            shelterModel.addRow(new Object[]{s.getId(), s.getName(), s.getDistrict(), s.getOccupancy(), s.getCapacity(), s.getCoordinator(), s.getPhone(), s.getStatus()});
        }
        shelterKpiCount.setText(String.valueOf(shelters.size()));

        // 2. Volunteers
        List<Volunteer> vols = dataManager.getVolunteers();
        volunteerModel.setRowCount(0);
        for (Volunteer v : vols) {
            volunteerModel.addRow(new Object[]{
                    v.getId(),
                    v.getName(),
                    v.getEmail(),
                    v.getPhone(),
                    v.getDistrict(),
                    v.getSkill(),
                    v.getIdProofPath(),
                    v.getCertificatePath(),
                    v.getStatus()
            });
        }
        volunteerKpiCount.setText(String.valueOf(vols.size()));

        // 3. Inventory
        List<InventoryItem> items = dataManager.getInventoryItems();
        inventoryModel.setRowCount(0);
        for (InventoryItem i : items) {
            inventoryModel.addRow(new Object[]{i.getId(), i.getName(), i.getDepot(), i.getQuantity(), i.getUnit(), i.getStatus()});
        }
        inventoryKpiCount.setText(String.valueOf(items.size()));

        // 4. Alerts
        List<DisasterAlert> alertList = dataManager.getAlerts();
        alertModel.setRowCount(0);
        for (DisasterAlert a : alertList) {
            alertModel.addRow(new Object[]{a.getId(), a.getHazard(), a.getDistrict(), a.getLevel(), a.getAffected(), a.getStatus()});
        }
        alertKpiCount.setText(String.valueOf(alertList.size()));

        // 5. Bulletins
        List<GroundBulletin> bulList = dataManager.getBulletins();
        bulletinModel.setRowCount(0);
        for (GroundBulletin b : bulList) {
            bulletinModel.addRow(new Object[]{b.getId(), b.getTime(), b.getMessage(), b.getSeverity()});
        }

        // 6. Emergency Records
        if (emergencyRecordsModel != null) {
            emergencyRecordsModel.setRowCount(0);
            emergencyRecordsModel.addRow(new Object[]{101, "National Emergency", "Central Integrated Desk", "112", "Live Connected"});
            emergencyRecordsModel.addRow(new Object[]{102, "District Control Room", "District Disaster Management (DDMA)", "1077", "Live Connected"});
            emergencyRecordsModel.addRow(new Object[]{103, "State Ops Center", "Kerala SDMA Unified Command", "1070", "Live Connected"});
            emergencyRecordsModel.addRow(new Object[]{104, "Ambulance / Triage", "Emergency Health & Trauma Transit", "108", "Live Connected"});
            emergencyRecordsModel.addRow(new Object[]{105, "Fire & Rescue Force", "Fire, Flood & Hazard Extrication", "101", "Live Connected"});
            emergencyRecordsModel.addRow(new Object[]{106, "NDRF Special Command", "National Disaster Response Force", "011-24363260", "Live Connected"});
            try {
                List<disaster.backend.DatabaseManager.ContactEntry> dbContacts = disaster.backend.DatabaseManager.getInstance().getAllEmergencyContacts();
                for (disaster.backend.DatabaseManager.ContactEntry c : dbContacts) {
                    emergencyRecordsModel.addRow(new Object[]{c.contactId, c.contactName, "Personal Contact (User #" + c.userId + ")", c.contactPhone, "Saved in disaster_db"});
                }
            } catch (Exception ignored) {}
        }
    }

    private void performAdminSignOut() {
        int res = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to sign out from the Admin Panel?\n\nYou will need to re-enter your username and password to log in again.",
                "Admin Sign Out",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (res == JOptionPane.YES_OPTION) {
            UserSession.setCurrentUser(null);
            if (DashboardFrame.getActiveInstance() != null) {
                DashboardFrame.getActiveInstance().updateSessionUser("Demo User");
            }
            dispose();
            // Automatically prompt the user with LoginFrame to re-enter credentials
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        }
    }

    // =========================================================================
    // UI HELPERS
    // =========================================================================
    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(34);
        table.setFont(UITheme.NORMAL_FONT);
        table.setShowGrid(true);
        table.setGridColor(UITheme.BORDER);
        table.setSelectionBackground(UITheme.HERO_BADGE_BG);
        table.setSelectionForeground(UITheme.BRAND_CRIMSON);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.getTableHeader().setFont(UITheme.BUTTON_FONT);
        table.getTableHeader().setBackground(UITheme.PANEL_BG);
        table.getTableHeader().setForeground(UITheme.TEXT);
        Dimension headerDim = table.getTableHeader().getPreferredSize();
        headerDim.height = 36;
        table.getTableHeader().setPreferredSize(headerDim);
        table.setFillsViewportHeight(true);

        // Center align ID column
        if (table.getColumnModel().getColumnCount() > 0) {
            table.getColumnModel().getColumn(0).setMaxWidth(60);
            table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    l.setHorizontalAlignment(SwingConstants.CENTER);
                    l.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    l.setForeground(UITheme.MUTED);
                    return l;
                }
            });
        }
        return table;
    }

    private JPanel createFormGrid(String[] labels, JComponent[] fields) {
        JPanel panel = new JPanel(new GridLayout(labels.length, 2, 8, 8));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setPreferredSize(new Dimension(500, labels.length * 42));

        for (int i = 0; i < labels.length; i++) {
            JLabel lbl = new JLabel(labels[i]);
            lbl.setFont(UITheme.BOLD_FONT);
            lbl.setForeground(UITheme.TEXT);
            panel.add(lbl);

            JComponent field = fields[i];
            field.setFont(UITheme.NORMAL_FONT);
            panel.add(field);
        }
        return panel;
    }
}
