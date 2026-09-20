package disaster.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class VolunteerFrame extends BaseFrame {

    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;

    public VolunteerFrame() {
        super("Certified Responders & Volunteer Corps - SDMA", 920, 600);
        setMinimumSize(new Dimension(800, 500));
        buildUI();
    }

    private void buildUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 14));
        mainPanel.setBackground(UITheme.BACKGROUND);
        mainPanel.setBorder(new EmptyBorder(18, 22, 18, 22));

        // Header Block
        JPanel header = createStandardHeader(
                "Certified Responders & Volunteer Roster",
                "Trained volunteer corps deployed across relief camps and field sectors",
                "4 DEPLOYED READY",
                UITheme.BRAND_CRIMSON
        );
        mainPanel.add(header, BorderLayout.NORTH);

        // Center Card with Search and Table
        JPanel card = createCard(8);
        card.setLayout(new BorderLayout(0, 12));

        // Search Bar Row
        JPanel searchRow = new JPanel(new BorderLayout(10, 0));
        searchRow.setOpaque(false);

        JLabel searchLbl = new JLabel("Filter Volunteers:");
        searchLbl.setFont(UITheme.BOLD_FONT);
        searchLbl.setForeground(UITheme.TEXT);
        searchRow.add(searchLbl, BorderLayout.WEST);

        searchField = createTextField("Search by volunteer name, skill, or district...");
        searchRow.add(searchField, BorderLayout.CENTER);

        card.add(searchRow, BorderLayout.NORTH);

        // Table
        String[] columns = {
                "Volunteer Name",
                "Certified Skill / Role",
                "Assigned Sector",
                "Contact Number",
                "Deployment Status"
        };

        java.util.List<disaster.service.AdminDataManager.Volunteer> volList = disaster.service.AdminDataManager.getInstance().getVolunteers();
        Object[][] data = new Object[volList.size()][5];
        for (int i = 0; i < volList.size(); i++) {
            disaster.service.AdminDataManager.Volunteer v = volList.get(i);
            data[i] = new Object[]{v.getName(), v.getSkill(), v.getSector(), v.getPhone(), v.getStatus()};
        }

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

        // Center status column styling
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
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
        card.add(scroll, BorderLayout.CENTER);

        // Load live volunteers from disaster_db
        loadDatabaseVolunteers();

        // Search action
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                String q = searchField.getText().trim().toLowerCase();
                for (int i = 0; i < table.getRowCount(); i++) {
                    String name = table.getValueAt(i, 0).toString().toLowerCase();
                    String skill = table.getValueAt(i, 1).toString().toLowerCase();
                    String sec = table.getValueAt(i, 2).toString().toLowerCase();
                    if (name.contains(q) || skill.contains(q) || sec.contains(q)) {
                        table.setRowSelectionInterval(i, i);
                        break;
                    }
                }
            }
        });

        mainPanel.add(card, BorderLayout.CENTER);

        // Bottom Action Bar
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottom.setOpaque(false);

        JButton registerBtn = button("Register as Volunteer", UITheme.BRAND_CRIMSON);
        registerBtn.setPreferredSize(new Dimension(180, 38));
        registerBtn.addActionListener(e -> registerVolunteer());
        bottom.add(registerBtn);

        JButton contactBtn = createOutlineButton("Contact Volunteer", UITheme.BORDER_DARK);
        contactBtn.setPreferredSize(new Dimension(145, 38));
        contactBtn.addActionListener(e -> contactSelected());
        bottom.add(contactBtn);

        JButton backBtn = createBackButton("← Back to Portal");
        backBtn.setPreferredSize(new Dimension(140, 38));
        bottom.add(backBtn);

        mainPanel.add(bottom, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void loadDatabaseVolunteers() {
        try (java.sql.Connection conn = disaster.backend.DatabaseManager.getInstance().getConnection()) {
            if (conn != null) {
                String sql = "SELECT u.user_id, u.username, u.phone, p.full_name, p.address "
                        + "FROM users u LEFT JOIN user_profiles p ON u.user_id = p.user_id "
                        + "ORDER BY u.user_id DESC";
                try (java.sql.PreparedStatement ps = conn.prepareStatement(sql);
                     java.sql.ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String name = rs.getString("full_name");
                        if (name == null || name.isEmpty()) name = rs.getString("username");
                        String phone = rs.getString("phone");
                        String addr = rs.getString("address");
                        if (addr == null || addr.isEmpty()) addr = "Kerala State Sector";
                        model.insertRow(0, new Object[]{
                                name + " (ID #" + rs.getInt("user_id") + ")",
                                "Disaster Relief Volunteer",
                                addr,
                                phone != null ? phone : "Unlisted",
                                "Active (disaster_db)"
                        });
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    private void registerVolunteer() {
        JTextField nameTf = new JTextField();
        JTextField skillTf = new JTextField();
        JTextField phoneTf = new JTextField();

        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        form.add(new JLabel("Full Name:"));
        form.add(nameTf);
        form.add(new JLabel("Specialized Skill:"));
        form.add(skillTf);
        form.add(new JLabel("Mobile Number:"));
        form.add(phoneTf);

        int res = JOptionPane.showConfirmDialog(
                this,
                form,
                "Register New Disaster Volunteer",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (res == JOptionPane.OK_OPTION) {
            String name = nameTf.getText().trim();
            String skill = skillTf.getText().trim();
            String phone = phoneTf.getText().trim();

            if (!name.isEmpty() && !phone.isEmpty()) {
                String cleanPhone = phone.replaceAll("[^0-9+]", "");
                if (cleanPhone.length() > 15) cleanPhone = cleanPhone.substring(0, 15);

                String uname = name.toLowerCase().replaceAll("\\s+", "_");
                if (uname.length() < 3) uname = uname + "_vol";

                disaster.model.User volUser = new disaster.model.User();
                volUser.setUsername(uname);
                volUser.setPhone(cleanPhone);
                volUser.setFullName(name);
                volUser.setAddress("Sector: " + (skill.isEmpty() ? "General Relief" : skill));
                volUser.setUserType("VOLUNTEER");

                boolean saved = false;
                try {
                    saved = new disaster.dao.UserDAO().addUser(volUser);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                model.insertRow(0, new Object[]{
                        name + " (ID #" + volUser.getUserId() + ")",
                        skill.isEmpty() ? "General Relief" : skill,
                        volUser.getAddress(),
                        cleanPhone,
                        "Active (disaster_db)"
                });
                table.setRowSelectionInterval(0, 0);

                String status = saved ? "✓ Saved to XAMPP MySQL disaster_db (users & user_profiles)" : "ℹ Stored locally";
                JOptionPane.showMessageDialog(
                        this,
                        "VOLUNTEER ENROLLED SUCCESSFULLY!\n\n"
                                + "• Name: " + name + "\n"
                                + "• Skill: " + (skill.isEmpty() ? "General Relief" : skill) + "\n"
                                + "• Phone: " + cleanPhone + "\n"
                                + "• Assigned User ID: #" + volUser.getUserId() + "\n\n"
                                + "Database Status: " + status,
                        "Registration Confirmed",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        }
    }

    private void contactSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a volunteer row first.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String name = (String) table.getValueAt(row, 0);
        String phone = (String) table.getValueAt(row, 3);
        JOptionPane.showMessageDialog(this, "Calling Volunteer: " + name + " (" + phone + ")...", "Call Dispatched", JOptionPane.INFORMATION_MESSAGE);
    }
}
