package disaster.ui;

import disaster.service.AdminDataManager;
import disaster.service.UserSession;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Window displaying disaster deployment works, rescue assignments,
 * and field operational tasks assigned by Administrators to approved volunteers.
 */
public class VolunteerWorksFrame extends BaseFrame {

    private JTable table;
    private DefaultTableModel model;
    private JLabel subtitleLbl;
    private final boolean isAdmin;
    private final String userIdentifier;

    public VolunteerWorksFrame() {
        this(resolveCurrentUsername());
    }

    public VolunteerWorksFrame(String userIdentifier) {
        super("Disaster Response Works & Assigned Missions - SDMA", 950, 620);
        setMinimumSize(new Dimension(840, 520));
        this.userIdentifier = userIdentifier;
        this.isAdmin = "admin".equalsIgnoreCase(userIdentifier)
                || "admin123".equalsIgnoreCase(userIdentifier)
                || "Admin Panel".equalsIgnoreCase(userIdentifier)
                || (UserSession.getCurrentUser() != null && "ADMIN".equalsIgnoreCase(UserSession.getCurrentUser().getUserType()));
        buildUI();
        loadTasks();
        AdminDataManager.getInstance().addChangeListener(this::loadTasks);
        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    private static String resolveCurrentUsername() {
        if (UserSession.getCurrentUser() != null) {
            String u = UserSession.getCurrentUser().getUsername();
            if (u != null && !u.isEmpty()) return u;
            String f = UserSession.getCurrentUser().getFullName();
            if (f != null && !f.isEmpty()) return f;
        }
        return "Volunteer";
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(UITheme.BACKGROUND);
        root.setBorder(new EmptyBorder(18, 22, 18, 22));

        // 1. Header
        String badgeText = isAdmin ? "ADMINISTRATIVE TASK DISPATCH" : "AUTHORIZED FIELD DEPLOYMENT DIRECTIVES";
        JPanel header = createStandardHeader(
                "Assigned Works & Disaster Deployment Operations",
                "Official emergency response missions assigned to certified volunteer corps",
                badgeText,
                UITheme.BRAND_CRIMSON
        );
        root.add(header, BorderLayout.NORTH);

        // 2. Center Card & Table
        JPanel card = createCard(8);
        card.setLayout(new BorderLayout(0, 12));

        // Info Banner
        JPanel infoBanner = new JPanel(new BorderLayout(10, 0));
        infoBanner.setBackground(new Color(248, 250, 252));
        infoBanner.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER, 1),
                new EmptyBorder(10, 14, 10, 14)
        ));

        subtitleLbl = new JLabel(isAdmin
                ? "Displaying all active field assignments across Kerala districts."
                : "Displaying deployment tasks assigned to your volunteer credentials.");
        subtitleLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        subtitleLbl.setForeground(UITheme.TEXT);
        infoBanner.add(subtitleLbl, BorderLayout.WEST);

        card.add(infoBanner, BorderLayout.NORTH);

        String[] cols = {
                "Task #",
                "Assigned Volunteer",
                "Mission / Work Title",
                "District",
                "Specific Location",
                "Priority",
                "Status",
                "Date Assigned",
                "Instructions"
        };

        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
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
        Dimension hDim = table.getTableHeader().getPreferredSize();
        hDim.height = 38;
        table.getTableHeader().setPreferredSize(hDim);

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(55);
        table.getColumnModel().getColumn(1).setPreferredWidth(130);
        table.getColumnModel().getColumn(2).setPreferredWidth(200);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(150);
        table.getColumnModel().getColumn(5).setPreferredWidth(80);
        table.getColumnModel().getColumn(6).setPreferredWidth(95);
        table.getColumnModel().getColumn(7).setPreferredWidth(95);
        table.getColumnModel().getColumn(8).setPreferredWidth(220);

        // Priority column renderer
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setFont(new Font("Segoe UI", Font.BOLD, 12));
                String v = String.valueOf(value);
                if ("Critical".equalsIgnoreCase(v)) {
                    l.setForeground(UITheme.DANGER);
                } else if ("High".equalsIgnoreCase(v)) {
                    l.setForeground(new Color(217, 83, 79));
                } else {
                    l.setForeground(UITheme.PRIMARY);
                }
                return l;
            }
        });

        // Status column renderer
        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setFont(new Font("Segoe UI", Font.BOLD, 12));
                String v = String.valueOf(value);
                if ("Completed".equalsIgnoreCase(v)) {
                    l.setForeground(UITheme.SUCCESS);
                } else if ("In Progress".equalsIgnoreCase(v)) {
                    l.setForeground(UITheme.HOTLINE_BLUE);
                } else {
                    l.setForeground(new Color(180, 83, 9));
                }
                return l;
            }
        });

        JScrollPane scroll = createTableScrollPane(table);
        card.add(scroll, BorderLayout.CENTER);

        root.add(card, BorderLayout.CENTER);

        // 3. Bottom Action Bar
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottom.setOpaque(false);

        if (isAdmin) {
            JButton assignBtn = button("+ Assign New Work", UITheme.PRIMARY);
            assignBtn.setPreferredSize(new Dimension(175, 42));
            assignBtn.addActionListener(e -> showAssignWorkDialog());
            bottom.add(assignBtn);
        }

        JButton progressBtn = createOutlineButton("Mark In Progress", UITheme.HOTLINE_BLUE);
        progressBtn.setPreferredSize(new Dimension(150, 42));
        progressBtn.addActionListener(e -> updateSelectedTaskStatus("In Progress"));
        bottom.add(progressBtn);

        JButton completeBtn = button("Mark Completed", UITheme.SUCCESS);
        completeBtn.setPreferredSize(new Dimension(160, 42));
        completeBtn.addActionListener(e -> updateSelectedTaskStatus("Completed"));
        bottom.add(completeBtn);

        JButton refreshBtn = createOutlineButton("Refresh", UITheme.BORDER_DARK);
        refreshBtn.setPreferredSize(new Dimension(100, 42));
        refreshBtn.addActionListener(e -> loadTasks());
        bottom.add(refreshBtn);

        JButton closeBtn = createOutlinedButton("Back / Close", UITheme.BORDER_DARK, UITheme.TEXT);
        closeBtn.setPreferredSize(new Dimension(140, 42));
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        closeBtn.addActionListener(e -> dispose());
        bottom.add(closeBtn);

        root.add(bottom, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void loadTasks() {
        model.setRowCount(0);
        List<AdminDataManager.VolunteerTask> tasks;
        if (isAdmin) {
            tasks = AdminDataManager.getInstance().getAllTasks();
        } else {
            tasks = AdminDataManager.getInstance().getTasksForUser(userIdentifier);
            if (tasks.isEmpty() && UserSession.getCurrentUser() != null) {
                if (UserSession.getCurrentUser().getFullName() != null) {
                    tasks = AdminDataManager.getInstance().getTasksForUser(UserSession.getCurrentUser().getFullName());
                }
                if (tasks.isEmpty() && UserSession.getCurrentUser().getEmail() != null) {
                    tasks = AdminDataManager.getInstance().getTasksForUser(UserSession.getCurrentUser().getEmail());
                }
            }
            if (tasks.isEmpty()) {
                subtitleLbl.setText("No missions currently assigned to your station. Stand by for Admin operational dispatch.");
            } else {
                subtitleLbl.setText("Displaying " + tasks.size() + " active deployment mission(s) assigned specifically to you.");
            }
        }

        for (AdminDataManager.VolunteerTask t : tasks) {
            model.addRow(new Object[]{
                    "#" + t.getTaskId(),
                    isAdmin ? t.getVolunteerName() : "You (" + t.getVolunteerName() + ")",
                    t.getTaskTitle(),
                    t.getDistrict(),
                    t.getLocation(),
                    t.getPriority(),
                    t.getStatus(),
                    t.getAssignedDate(),
                    t.getInstructions()
            });
        }
    }

    private void updateSelectedTaskStatus(String newStatus) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an assigned work row from the table.", "No Row Selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String rawId = (String) model.getValueAt(row, 0);
        int taskId = Integer.parseInt(rawId.replace("#", ""));
        boolean ok = AdminDataManager.getInstance().updateTaskStatus(taskId, newStatus);
        if (ok) {
            model.setValueAt(newStatus, row, 6);
            JOptionPane.showMessageDialog(this, "Work Directive #" + taskId + " updated to: " + newStatus, "Status Updated", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showAssignWorkDialog() {
        List<AdminDataManager.Volunteer> approved = AdminDataManager.getInstance().getApprovedVolunteers();
        if (approved.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No approved volunteers available to assign tasks.\nPlease approve volunteers from the Admin Panel first.", "No Approved Volunteers", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String[] volNames = new String[approved.size()];
        for (int i = 0; i < approved.size(); i++) {
            volNames[i] = approved.get(i).getName() + " (" + approved.get(i).getDistrict() + " - " + approved.get(i).getSkill() + ")";
        }

        JComboBox<String> volCombo = new JComboBox<>(volNames);
        JTextField titleF = createTextField("e.g. Flood Water Rescue Evacuation");
        JComboBox<String> distCombo = new JComboBox<>(disaster.service.VolunteerSkillsData.KERALA_DISTRICTS);
        JTextField locF = createTextField("e.g. Kuttanad Ward 4 Relief Zone");
        JComboBox<String> prioCombo = new JComboBox<>(new String[]{"Critical", "High", "Medium", "Low"});
        JTextArea instArea = new JTextArea(3, 20);
        instArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        instArea.setLineWrap(true);
        instArea.setWrapStyleWord(true);
        instArea.setText("Coordinate with local disaster officer and ensure rapid distribution.");

        JPanel form = new JPanel(new GridLayout(6, 2, 8, 8));
        form.add(new JLabel("Select Approved Volunteer:"));
        form.add(volCombo);
        form.add(new JLabel("Work / Mission Title:"));
        form.add(titleF);
        form.add(new JLabel("District / Sector:"));
        form.add(distCombo);
        form.add(new JLabel("Specific Deployment Location:"));
        form.add(locF);
        form.add(new JLabel("Operational Priority:"));
        form.add(prioCombo);
        form.add(new JLabel("Operational Guidelines:"));
        form.add(new JScrollPane(instArea));

        int res = JOptionPane.showConfirmDialog(this, form, "Dispatch Disaster Deployment Work", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            String title = titleF.getText().trim();
            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Mission title is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int selIdx = volCombo.getSelectedIndex();
            AdminDataManager.Volunteer selVol = approved.get(selIdx);

            AdminDataManager.getInstance().assignTask(
                    selVol.getId(),
                    selVol.getName(),
                    title,
                    (String) distCombo.getSelectedItem(),
                    locF.getText().trim(),
                    (String) prioCombo.getSelectedItem(),
                    instArea.getText().trim()
            );

            loadTasks();
            JOptionPane.showMessageDialog(this, "Deployment task assigned successfully to " + selVol.getName() + "!", "Task Dispatched", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
