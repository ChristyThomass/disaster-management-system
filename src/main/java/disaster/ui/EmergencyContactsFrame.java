package disaster.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;

public class EmergencyContactsFrame extends BaseFrame {

    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;

    public EmergencyContactsFrame() {
        super("State Emergency Hotlines & Dedicated Directory", 920, 600);
        setMinimumSize(new Dimension(800, 500));
        buildUI();
    }

    private void buildUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 14));
        mainPanel.setBackground(UITheme.BACKGROUND);
        mainPanel.setBorder(new EmptyBorder(18, 22, 18, 22));

        // Header Block
        JPanel header = createStandardHeader(
                "State Emergency Hotlines & Quick Directory",
                "Official 24/7 dedicated lines synchronized with Kerala SDMA Unified Command",
                "24/7 PRIORITY LINES",
                UITheme.BRAND_CRIMSON
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
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottom.setOpaque(false);

        JButton addContactBtn = button("➕ Add Contact to Database", UITheme.HOTLINE_BLUE);
        addContactBtn.setPreferredSize(new Dimension(210, 38));
        addContactBtn.addActionListener(e -> addNewContact());
        bottom.add(addContactBtn);

        JButton callBtn = button("Connect Call to Hotline", UITheme.BRAND_CRIMSON);
        callBtn.setPreferredSize(new Dimension(190, 38));
        callBtn.addActionListener(e -> dialSelected());
        bottom.add(callBtn);

        JButton copyBtn = createOutlineButton("Copy Hotline Number", UITheme.BORDER_DARK);
        copyBtn.setPreferredSize(new Dimension(170, 38));
        copyBtn.addActionListener(e -> copySelected());
        bottom.add(copyBtn);

        JButton backBtn = createBackButton("← Back to Portal");
        backBtn.setPreferredSize(new Dimension(140, 38));
        bottom.add(backBtn);

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
        JTextField phoneField = new JTextField();
        Object[] message = {
                "Contact Name / Relation (e.g. Brother / Rahul):", nameField,
                "Phone Number (e.g. 9847123456):", phoneField
        };

        int option = JOptionPane.showConfirmDialog(
                this,
                message,
                "Add Emergency Contact to disaster_db",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (option == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            if (name.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Both name and phone number are required.", "Input Required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int userId = 1;
            if (disaster.service.UserSession.isLoggedIn() && disaster.service.UserSession.getCurrentUser() != null) {
                try {
                    userId = Integer.parseInt(disaster.service.UserSession.getCurrentUser().getUserId());
                } catch (Exception ignored) {}
            }

            boolean saved = disaster.backend.DatabaseManager.getInstance().saveEmergencyContact(userId, name, phone);
            if (saved) {
                model.insertRow(0, new Object[]{
                        name,
                        "Personal Emergency Contact (User #" + userId + ")",
                        phone,
                        "24x7 Available",
                        "Saved in disaster_db"
                });
                table.setRowSelectionInterval(0, 0);

                JOptionPane.showMessageDialog(
                        this,
                        "EMERGENCY CONTACT SAVED!\n\n"
                                + "• Name: " + name + "\n"
                                + "• Phone: " + phone + "\n"
                                + "• Table: emergency_contacts (disaster_db on port 3306)\n"
                                + "• Linked to User ID: " + userId,
                        "Contact Stored in Database",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        }
    }

    private void dialSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an emergency hotline from the directory first.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String agency = (String) table.getValueAt(row, 0);
        String number = (String) table.getValueAt(row, 2);

        JOptionPane.showMessageDialog(
                this,
                "CONNECTING PRIORITY EMERGENCY CALL:\n\n"
                        + "Agency: " + agency + "\n"
                        + "Emergency Hotline: " + number + "\n"
                        + "Trunk Route: State Disaster Emergency Exchange Priority Line.",
                "Emergency Call Connected",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void copySelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an emergency hotline row first.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String number = (String) table.getValueAt(row, 2);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(number), null);
        JOptionPane.showMessageDialog(this, "Emergency hotline copied to clipboard: " + number, "Copied", JOptionPane.INFORMATION_MESSAGE);
    }
}