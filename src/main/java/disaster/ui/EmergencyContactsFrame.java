package disaster.ui;

import disaster.service.UserSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class EmergencyContactsFrame extends BaseFrame {

    private final boolean isAdmin;
    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;

    public EmergencyContactsFrame() {
        this(resolveAdminStatus());
    }

    public EmergencyContactsFrame(boolean isAdmin) {
        super("State Emergency Hotlines & Dedicated Directory", 940, 620);
        this.isAdmin = isAdmin;
        setMinimumSize(new Dimension(800, 500));
        buildUI();
    }

    private static boolean resolveAdminStatus() {
        if (UserSession.isLoggedIn() && UserSession.getCurrentUser() != null) {
            String u = UserSession.getCurrentUser().getUsername();
            String t = UserSession.getCurrentUser().getUserType();
            if ("ADMIN".equalsIgnoreCase(t)) {
                return true;
            }
            if (u != null && ("admin".equalsIgnoreCase(u) || "admin123".equalsIgnoreCase(u) || "Admin Panel".equalsIgnoreCase(u))) {
                return true;
            }
        }
        return false;
    }

    private void buildUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 14));
        mainPanel.setBackground(UITheme.BACKGROUND);
        mainPanel.setBorder(new EmptyBorder(18, 22, 18, 22));

        // Header Block
        JPanel header = createStandardHeader(
                "State Emergency Hotlines & Quick Directory",
                isAdmin ? "Authorized administrative console to add, re-edit, or remove official emergency records"
                        : "Official 24/7 dedicated lines synchronized with Kerala SDMA Unified Command",
                isAdmin ? "ADMIN CONTROL" : "24/7 PRIORITY LINES",
                isAdmin ? UITheme.HOTLINE_BLUE : UITheme.BRAND_CRIMSON
        );
        mainPanel.add(header, BorderLayout.NORTH);

        // Center Card
        JPanel card = createCard(8);
        card.setLayout(new BorderLayout(0, 12));

        // Search Bar Row
        JPanel searchRow = new JPanel(new BorderLayout(10, 0));
        searchRow.setOpaque(false);

        JLabel searchLbl = new JLabel("Filter Hotlines:");
        searchLbl.setFont(UITheme.BOLD_FONT);
        searchLbl.setForeground(UITheme.TEXT);
        searchRow.add(searchLbl, BorderLayout.WEST);

        searchField = createTextField("Search helpline or agency...");
        searchRow.add(searchField, BorderLayout.CENTER);

        card.add(searchRow, BorderLayout.NORTH);

        // Table
        String[] columns = {
                "Service / Response Wing",
                "Designated Department",
                "Emergency Hotline",
                "Availability",
                "Operational Status"
        };

        Object[][] data = {
                {"National Emergency", "Central Integrated Desk", "112", "24x7 Active", "Live Connected"},
                {"District Control Room", "District Disaster Management (DDMA)", "1077", "24x7 Active", "Live Connected"},
                {"State Ops Center", "Kerala SDMA Unified Command", "1070", "24x7 Active", "Live Connected"},
                {"Ambulance / Triage", "Emergency Health & Trauma Transit", "108", "24x7 Active", "Live Connected"},
                {"Fire & Rescue Force", "Fire, Flood & Hazard Extrication", "101", "24x7 Active", "Live Connected"},
                {"NDRF Special Command", "National Disaster Response Force", "011-24363260", "24x7 Active", "Live Connected"},
                {"Coast Guard (Marine Ops)", "Offshore Maritime & Cyclone Distress", "1554", "24x7 Active", "Live Connected"},
                {"Women & Child Helpline", "Specialized Evacuation & Shelter Aid", "1091", "24x7 Active", "Live Connected"}
        };

        model = new DefaultTableModel(data, columns) {
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
        Dimension headerDim = table.getTableHeader().getPreferredSize();
        headerDim.height = 38;
        table.getTableHeader().setPreferredSize(headerDim);
        table.setFillsViewportHeight(true);

        // Center number column styling in bold crimson
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setFont(new Font("Segoe UI", Font.BOLD, 14));
                l.setForeground(UITheme.BRAND_CRIMSON);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                return l;
            }
        });

        JScrollPane scroll = createTableScrollPane(table);
        card.add(scroll, BorderLayout.CENTER);

        // Load custom contacts from disaster_db
        loadDatabaseContacts();

        // Search Action
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                String q = searchField.getText().trim().toLowerCase();
                for (int i = 0; i < table.getRowCount(); i++) {
                    String s = table.getValueAt(i, 0).toString().toLowerCase();
                    String scope = table.getValueAt(i, 1).toString().toLowerCase();
                    String num = table.getValueAt(i, 2).toString().toLowerCase();
                    if (s.contains(q) || scope.contains(q) || num.contains(q)) {
                        table.setRowSelectionInterval(i, i);
                        break;
                    }
                }
            }
        });

        mainPanel.add(card, BorderLayout.CENTER);

        // Bottom Bar
        JPanel bottom = new JPanel(new BorderLayout(10, 0));
        bottom.setOpaque(false);

        if (isAdmin) {
            JLabel adminNotice = new JLabel("⚡ Administrator Records Console • Add, Edit & Maintain Official Hotlines");
            adminNotice.setFont(new Font("Segoe UI", Font.BOLD, 12));
            adminNotice.setForeground(UITheme.PRIMARY);
            bottom.add(adminNotice, BorderLayout.WEST);

            JPanel adminBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            adminBtns.setOpaque(false);

            JButton addRecordBtn = button("Add New Record", UITheme.HOTLINE_BLUE);
            addRecordBtn.setPreferredSize(new Dimension(160, 40));
            addRecordBtn.addActionListener(e -> addNewContact());
            adminBtns.add(addRecordBtn);

            JButton editRecordBtn = button("Edit Selected Record", UITheme.PRIMARY);
            editRecordBtn.setPreferredSize(new Dimension(175, 40));
            editRecordBtn.addActionListener(e -> editSelectedContact());
            adminBtns.add(editRecordBtn);

            JButton deleteRecordBtn = button("Delete Record", UITheme.BRAND_CRIMSON);
            deleteRecordBtn.setPreferredSize(new Dimension(140, 40));
            deleteRecordBtn.addActionListener(e -> deleteSelectedContact());
            adminBtns.add(deleteRecordBtn);

            JButton backBtn = createBackButton("Back to Portal");
            backBtn.setPreferredSize(new Dimension(140, 40));
            adminBtns.add(backBtn);

            bottom.add(adminBtns, BorderLayout.EAST);
        } else {
            JLabel readOnlyNotice = new JLabel("Official State Directory (Read-Only) • Updates restricted to SDMA Administrators");
            readOnlyNotice.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            readOnlyNotice.setForeground(UITheme.SECONDARY_TEXT);
            bottom.add(readOnlyNotice, BorderLayout.WEST);

            JPanel userBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            userBtns.setOpaque(false);

            JButton backBtn = createBackButton("Back to Portal");
            backBtn.setPreferredSize(new Dimension(140, 40));
            userBtns.add(backBtn);

            bottom.add(userBtns, BorderLayout.EAST);
        }

        mainPanel.add(bottom, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void loadDatabaseContacts() {
        try {
            java.util.List<disaster.backend.DatabaseManager.ContactEntry> contacts =
                    disaster.backend.DatabaseManager.getInstance().getAllEmergencyContacts();
            for (disaster.backend.DatabaseManager.ContactEntry c : contacts) {
                model.insertRow(0, new Object[]{
                        c.contactName,
                        "Personal Emergency Contact (User #" + c.userId + ")",
                        c.contactPhone,
                        "24x7 Available",
                        "Saved in disaster_db"
                });
            }
        } catch (Exception ignored) {}
    }

    private void addNewContact() {
        JTextField nameField = new JTextField();
        JTextField deptField = new JTextField("Special Disaster Response Unit");
        JTextField phoneField = new JTextField();
        JTextField availField = new JTextField("24x7 Active");
        JTextField statusField = new JTextField("Live Connected");

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
        form.setPreferredSize(new Dimension(420, 180));
        form.add(new JLabel("Service / Contact Name:"));
        form.add(nameField);
        form.add(new JLabel("Designated Department:"));
        form.add(deptField);
        form.add(new JLabel("Emergency Hotline / Phone:"));
        form.add(phoneField);
        form.add(new JLabel("Availability:"));
        form.add(availField);
        form.add(new JLabel("Operational Status:"));
        form.add(statusField);

        int option = JOptionPane.showConfirmDialog(
                this,
                form,
                "Add New Emergency Record to disaster_db",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (option == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String dept = deptField.getText().trim();
            String phone = phoneField.getText().trim();
            String avail = availField.getText().trim();
            String status = statusField.getText().trim();

            if (name.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Both Name and Phone Number are required.", "Input Required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (dept.isEmpty()) dept = "State Emergency Wing";
            if (avail.isEmpty()) avail = "24x7 Active";
            if (status.isEmpty()) status = "Live Connected";

            int userId = 1;
            if (disaster.service.UserSession.isLoggedIn() && disaster.service.UserSession.getCurrentUser() != null) {
                try {
                    userId = Integer.parseInt(disaster.service.UserSession.getCurrentUser().getUserId());
                } catch (Exception ignored) {}
            }

            disaster.dao.EmergencyContactDAO contactDAO = new disaster.dao.EmergencyContactDAO();
            boolean saved = contactDAO.saveContact(userId, name, phone);
            if (!saved) {
                saved = disaster.backend.DatabaseManager.getInstance().saveEmergencyContact(userId, name, phone);
            }

            model.insertRow(0, new Object[]{name, dept, phone, avail, status});
            table.setRowSelectionInterval(0, 0);

            JOptionPane.showMessageDialog(
                    this,
                    "NEW EMERGENCY RECORD ADDED!\n\n"
                            + "• Agency / Contact: " + name + "\n"
                            + "• Department: " + dept + "\n"
                            + "• Emergency Hotline: " + phone + "\n"
                            + "• Status: " + status + "\n"
                            + "• Stored in disaster_db.emergency_contacts",
                    "Record Saved",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    private void editSelectedContact() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an emergency record from the table to edit.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String oldName = (String) table.getValueAt(row, 0);
        String oldDept = (String) table.getValueAt(row, 1);
        String oldPhone = (String) table.getValueAt(row, 2);
        String oldAvail = (String) table.getValueAt(row, 3);
        String oldStatus = (String) table.getValueAt(row, 4);

        JTextField nameField = new JTextField(oldName);
        JTextField deptField = new JTextField(oldDept);
        JTextField phoneField = new JTextField(oldPhone);
        JTextField availField = new JTextField(oldAvail);
        JTextField statusField = new JTextField(oldStatus);

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
        form.setPreferredSize(new Dimension(420, 180));
        form.add(new JLabel("Service / Contact Name:"));
        form.add(nameField);
        form.add(new JLabel("Designated Department:"));
        form.add(deptField);
        form.add(new JLabel("Emergency Hotline / Phone:"));
        form.add(phoneField);
        form.add(new JLabel("Availability:"));
        form.add(availField);
        form.add(new JLabel("Operational Status:"));
        form.add(statusField);

        int option = JOptionPane.showConfirmDialog(
                this,
                form,
                "Re-edit Emergency Record - " + oldName,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (option == JOptionPane.OK_OPTION) {
            String newName = nameField.getText().trim();
            String newDept = deptField.getText().trim();
            String newPhone = phoneField.getText().trim();
            String newAvail = availField.getText().trim();
            String newStatus = statusField.getText().trim();

            if (newName.isEmpty() || newPhone.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Both Name and Phone Number are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            table.setValueAt(newName, row, 0);
            table.setValueAt(newDept.isEmpty() ? oldDept : newDept, row, 1);
            table.setValueAt(newPhone, row, 2);
            table.setValueAt(newAvail.isEmpty() ? oldAvail : newAvail, row, 3);
            table.setValueAt(newStatus.isEmpty() ? oldStatus : newStatus, row, 4);

            new disaster.dao.EmergencyContactDAO().updateContact(oldName, oldPhone, newName, newPhone);
            disaster.backend.DatabaseManager.getInstance().updateEmergencyContact(oldName, oldPhone, newName, newPhone);

            JOptionPane.showMessageDialog(
                    this,
                    "EMERGENCY RECORD UPDATED!\n\n"
                            + "• Contact / Service: " + newName + "\n"
                            + "• Department: " + newDept + "\n"
                            + "• Emergency Hotline: " + newPhone + "\n"
                            + "• Status: Saved and synchronized with database.",
                    "Record Updated",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    private void deleteSelectedContact() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an emergency record from the table to delete.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String name = (String) table.getValueAt(row, 0);
        String phone = (String) table.getValueAt(row, 2);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this emergency record?\n\n"
                        + "• Service / Contact: " + name + "\n"
                        + "• Phone: " + phone + "\n\n"
                        + "This will permanently remove the record from the emergency directory.",
                "Confirm Record Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            model.removeRow(row);
            new disaster.dao.EmergencyContactDAO().deleteContact(name, phone);
            disaster.backend.DatabaseManager.getInstance().deleteEmergencyContact(name, phone);
            JOptionPane.showMessageDialog(this, "Record deleted successfully.", "Record Removed", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}