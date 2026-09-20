package disaster.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class AlertFrame extends BaseFrame {

    private JTable table;
    private DefaultTableModel model;

    public AlertFrame() {
        super("Smart Disaster Management - Active Alerts Bulletin", 920, 600);
        setMinimumSize(new Dimension(800, 500));
        buildUI();
    }

    private void buildUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 14));
        mainPanel.setBackground(UITheme.BACKGROUND);
        mainPanel.setBorder(new EmptyBorder(18, 22, 18, 22));

        // Header Block
        JPanel header = createStandardHeader(
                "Active Alerts & Meteorological Bulletins",
                "Official bulletins synchronized with Kerala State Disaster Management Authority",
                "6 ACTIVE BULLETINS",
                UITheme.BRAND_CRIMSON
        );
        mainPanel.add(header, BorderLayout.NORTH);

        // Center Card with Table
        JPanel card = createCard(8);
        card.setLayout(new BorderLayout(0, 10));

        String[] columns = {
                "Hazard Nature",
                "District / Region",
                "Warning Level",
                "Affected Population",
                "Current Status"
        };

        java.util.List<disaster.service.AdminDataManager.DisasterAlert> alertList = disaster.service.AdminDataManager.getInstance().getAlerts();
        Object[][] data = new Object[alertList.size()][5];
        for (int i = 0; i < alertList.size(); i++) {
            disaster.service.AdminDataManager.DisasterAlert a = alertList.get(i);
            data[i] = new Object[]{a.getHazard(), a.getDistrict(), a.getLevel(), a.getAffected(), a.getStatus()};
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
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(true);
        table.setGridColor(UITheme.BORDER);
        table.setSelectionBackground(UITheme.HERO_BADGE_BG);
        table.setSelectionForeground(UITheme.BRAND_CRIMSON);
        table.setFillsViewportHeight(true);

        table.getTableHeader().setFont(UITheme.BUTTON_FONT);
        table.getTableHeader().setBackground(UITheme.PANEL_BG);
        table.getTableHeader().setForeground(UITheme.TEXT);
        Dimension headerDim = table.getTableHeader().getPreferredSize();
        headerDim.height = 38;
        table.getTableHeader().setPreferredSize(headerDim);

        // Column widths
        if (table.getColumnModel().getColumnCount() >= 5) {
            table.getColumnModel().getColumn(0).setPreferredWidth(220);
            table.getColumnModel().getColumn(1).setPreferredWidth(180);
            table.getColumnModel().getColumn(2).setPreferredWidth(120);
            table.getColumnModel().getColumn(3).setPreferredWidth(140);
            table.getColumnModel().getColumn(4).setPreferredWidth(140);
        }

        // Custom renderer for Severity column
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setBorder(new EmptyBorder(0, 10, 0, 10));
                String val = value != null ? value.toString() : "";
                if (val.equalsIgnoreCase("Critical")) {
                    l.setForeground(UITheme.BRAND_CRIMSON);
                    l.setFont(UITheme.BOLD_FONT);
                } else if (val.equalsIgnoreCase("High")) {
                    l.setForeground(UITheme.WARNING);
                    l.setFont(UITheme.BOLD_FONT);
                } else {
                    l.setForeground(UITheme.SECONDARY_TEXT);
                    l.setFont(UITheme.NORMAL_FONT);
                }
                return l;
            }
        });

        // Cell padding renderer for other columns
        DefaultTableCellRenderer paddedRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setBorder(new EmptyBorder(0, 10, 0, 10));
                return l;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (i != 2) {
                table.getColumnModel().getColumn(i).setCellRenderer(paddedRenderer);
            }
        }

        JScrollPane scroll = createTableScrollPane(table);
        card.add(scroll, BorderLayout.CENTER);

        mainPanel.add(card, BorderLayout.CENTER);

        // Load dynamic backend alerts
        loadBackendAlerts();

        // Bottom Action Bar
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottom.setOpaque(false);

        JButton refreshBtn = createOutlineButton("🔄 Refresh Live Backend Alerts", UITheme.BORDER_DARK);
        refreshBtn.setPreferredSize(new Dimension(210, 38));
        refreshBtn.addActionListener(e -> loadBackendAlerts());
        bottom.add(refreshBtn);

        JButton viewBtn = button("View Selected Bulletin", UITheme.BRAND_CRIMSON);
        viewBtn.setPreferredSize(new Dimension(175, 38));
        viewBtn.addActionListener(e -> showSelectedDetails());
        bottom.add(viewBtn);

        JButton backBtn = createBackButton("← Back to Portal");
        backBtn.setPreferredSize(new Dimension(140, 38));
        bottom.add(backBtn);

        mainPanel.add(bottom, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void loadBackendAlerts() {
        new Thread(() -> {
            // 1. Read live SOS alerts from disaster_db
            java.util.List<disaster.model.SOSAlert> sosList =
                    disaster.backend.DatabaseManager.getInstance().getActiveSOSAlerts();

            // 2. Read live Incident Reports from disaster_db
            try (java.sql.Connection conn = disaster.backend.DatabaseManager.getInstance().getConnection()) {
                if (conn != null) {
                    String sql = "SELECT incident_id, disaster_type, severity_level, description, status FROM incident_reports ORDER BY incident_id DESC";
                    try (java.sql.PreparedStatement ps = conn.prepareStatement(sql);
                         java.sql.ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            int id = rs.getInt("incident_id");
                            String type = rs.getString("disaster_type");
                            int sev = rs.getInt("severity_level");
                            String desc = rs.getString("description");
                            String stat = rs.getString("status");
                            String sevStr = sev >= 8 ? "Critical" : (sev >= 5 ? "High" : "Medium");
                            SwingUtilities.invokeLater(() -> {
                                model.insertRow(0, new Object[]{
                                        "📢 [REPORT #" + id + "] " + type,
                                        desc != null && desc.length() > 32 ? desc.substring(0, 32) + "..." : desc,
                                        sevStr,
                                        "District Triage",
                                        stat + " (disaster_db)"
                                });
                            });
                        }
                    }
                }
            } catch (Exception ignored) {}

            if (sosList != null && !sosList.isEmpty()) {
                SwingUtilities.invokeLater(() -> {
                    for (disaster.model.SOSAlert sos : sosList) {
                        String hazard = "🚨 [LIVE SOS #" + sos.getSosId() + "] " + (sos.getDescription().length() > 30 ? sos.getDescription().substring(0, 30) + "..." : sos.getDescription());
                        String region = "Lat " + sos.getLatitude() + ", Lon " + sos.getLongitude();
                        String level = sos.getUrgencyLevel();
                        String responders = sos.getRespondersCount() + " Responders";
                        String status = sos.getStatus() + " (disaster_db)";
                        model.insertRow(0, new Object[]{hazard, region, level, responders, status});
                    }
                });
            }
        }).start();
    }

    private void showSelectedDetails() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an alert from the table first.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String hazard = (String) table.getValueAt(row, 0);
        String region = (String) table.getValueAt(row, 1);
        String sev = (String) table.getValueAt(row, 2);
        String pop = (String) table.getValueAt(row, 3);
        String status = (String) table.getValueAt(row, 4);

        JOptionPane.showMessageDialog(
                this,
                "KERALA SDMA EMERGENCY ADVISORY:\n\n"
                        + "Hazard Nature: " + hazard + "\n"
                        + "Affected Region: " + region + "\n"
                        + "Warning Level: " + sev + "\n"
                        + "Affected Population: " + pop + "\n"
                        + "Operational Status: " + status + "\n\n"
                        + "Instructions: Follow local tahsildar & SDMA evacuation directives. Relief camps are operational.",
                "Advisory Intelligence Briefing",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}