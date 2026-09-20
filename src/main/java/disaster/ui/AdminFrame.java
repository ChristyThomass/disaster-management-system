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

        JButton addBtn = button("+ Add Volunteer", UITheme.PRIMARY);
        addBtn.setPreferredSize(new Dimension(150, 34));
        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        addBtn.addActionListener(e -> showAddVolunteerDialog());
        toolbar.add(addBtn);

        JButton editBtn = createOutlinedButton("Edit Selected Volunteer", UITheme.BRAND_CRIMSON, UITheme.BRAND_CRIMSON);
        editBtn.setPreferredSize(new Dimension(180, 34));
        editBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        editBtn.addActionListener(e -> showEditVolunteerDialog());
        toolbar.add(editBtn);

        JButton deleteBtn = createOutlinedButton("Delete Volunteer", UITheme.BORDER_DARK, UITheme.DANGER);
        deleteBtn.setPreferredSize(new Dimension(140, 34));
        deleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        deleteBtn.addActionListener(e -> deleteSelectedVolunteer());
        toolbar.add(deleteBtn);

        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"ID", "Volunteer Name", "Certified Skill / Role", "Assigned Sector / Camp", "Contact Number", "Deployment Status"};
        volunteerModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        volunteerTable = createStyledTable(volunteerModel);
        panel.add(new JScrollPane(volunteerTable), BorderLayout.CENTER);

        return panel;
    }

    private void showAddVolunteerDialog() {
        JTextField nameF = createTextField("e.g. Sreekumar N.");
        JTextField skillF = createTextField("e.g. First Aid & Evacuation Support");
        JTextField sectorF = createTextField("e.g. Wayanad Sector #2");
        JTextField phoneF = createTextField("e.g. +91 94470 54321");
        JComboBox<String> statusC = new JComboBox<>(new String[]{"Deployed", "On Duty", "Active", "Standby"});

        JPanel form = createFormGrid(
                new String[]{"Volunteer Full Name:", "Skill / Specialty:", "Assigned Sector / Camp:", "Contact Phone:", "Deployment Status:"},
                new JComponent[]{nameF, skillF, sectorF, phoneF, statusC}
        );

        int res = JOptionPane.showConfirmDialog(this, form, "Add New Certified Volunteer", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            if (nameF.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Volunteer name is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            dataManager.addVolunteer(nameF.getText().trim(), skillF.getText().trim(), sectorF.getText().trim(), phoneF.getText().trim(), (String) statusC.getSelectedItem());
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
        JTextField skillF = createTextField(""); skillF.setText((String) volunteerModel.getValueAt(row, 2));
        JTextField sectorF = createTextField(""); sectorF.setText((String) volunteerModel.getValueAt(row, 3));
        JTextField phoneF = createTextField(""); phoneF.setText((String) volunteerModel.getValueAt(row, 4));
        JComboBox<String> statusC = new JComboBox<>(new String[]{"Deployed", "On Duty", "Active", "Standby"});
        statusC.setSelectedItem(volunteerModel.getValueAt(row, 5));

        JPanel form = createFormGrid(
                new String[]{"Volunteer Full Name:", "Skill / Specialty:", "Assigned Sector / Camp:", "Contact Phone:", "Deployment Status:"},
                new JComponent[]{nameF, skillF, sectorF, phoneF, statusC}
        );

        int res = JOptionPane.showConfirmDialog(this, form, "Re-edit Volunteer #" + id, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            dataManager.updateVolunteer(id, nameF.getText().trim(), skillF.getText().trim(), sectorF.getText().trim(), phoneF.getText().trim(), (String) statusC.getSelectedItem());
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
            volunteerModel.addRow(new Object[]{v.getId(), v.getName(), v.getSkill(), v.getSector(), v.getPhone(), v.getStatus()});
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
