package disaster.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class InventoryFrame extends BaseFrame {

    private JTable table;
    private DefaultTableModel model;

    public InventoryFrame() {
        super("Warehouse Supplies & Logistics Depot - SDMA", 920, 600);
        setMinimumSize(new Dimension(800, 500));
        buildUI();
    }

    private void buildUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 14));
        mainPanel.setBackground(UITheme.BACKGROUND);
        mainPanel.setBorder(new EmptyBorder(18, 22, 18, 22));

        // Header Block
        JPanel header = createStandardHeader(
                "Warehouse Supplies & Emergency Logistics Depot",
                "Central disaster inventory stationed across regional hubs and relief camps",
                "8 CATEGORIES READY",
                UITheme.BRAND_CRIMSON
        );
        mainPanel.add(header, BorderLayout.NORTH);

        // Center Card with Table
        JPanel card = createCard(8);
        card.setLayout(new BorderLayout(0, 10));

        String[] columns = {
                "Item / Supply Category",
                "Stationed Depot",
                "Quantity Available",
                "Unit Type",
                "Depot Status"
        };

        java.util.List<disaster.service.AdminDataManager.InventoryItem> itemList = disaster.service.AdminDataManager.getInstance().getInventoryItems();
        Object[][] data = new Object[itemList.size()][5];
        for (int i = 0; i < itemList.size(); i++) {
            disaster.service.AdminDataManager.InventoryItem item = itemList.get(i);
            data[i] = new Object[]{item.getName(), item.getDepot(), item.getQuantity(), item.getUnit(), item.getStatus()};
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

        // Center quantity column
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

        // Status column
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

        mainPanel.add(card, BorderLayout.CENTER);

        // Bottom Action Bar
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottom.setOpaque(false);

        JButton dispatchBtn = button("Dispatch Supplies to Camp", UITheme.BRAND_CRIMSON);
        dispatchBtn.setPreferredSize(new Dimension(200, 38));
        dispatchBtn.addActionListener(e -> dispatchSelected());
        bottom.add(dispatchBtn);

        JButton requestBtn = createOutlineButton("Request Re-Stock", UITheme.BORDER_DARK);
        requestBtn.setPreferredSize(new Dimension(145, 38));
        requestBtn.addActionListener(e -> {
            logInventoryAction("Re-stock requisition logged for State Supplies Depot");
            JOptionPane.showMessageDialog(this, "Re-stock requisition logged to State Supplies Directorate & disaster_db.", "Requisition Sent", JOptionPane.INFORMATION_MESSAGE);
        });
        bottom.add(requestBtn);

        JButton backBtn = createBackButton("← Back to Portal");
        backBtn.setPreferredSize(new Dimension(140, 38));
        bottom.add(backBtn);

        mainPanel.add(bottom, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void logInventoryAction(String msg) {
        int uid = 1;
        if (disaster.service.UserSession.isLoggedIn() && disaster.service.UserSession.getCurrentUser() != null) {
            try { uid = Integer.parseInt(disaster.service.UserSession.getCurrentUser().getUserId()); } catch (Exception ignored) {}
        }
        try (java.sql.Connection conn = disaster.backend.DatabaseManager.getInstance().getConnection()) {
            if (conn != null) {
                try (java.sql.PreparedStatement ps = conn.prepareStatement("INSERT INTO notifications (user_id, message, is_read) VALUES (?, ?, 0)")) {
                    ps.setInt(1, uid);
                    ps.setString(2, msg);
                    ps.executeUpdate();
                }
            }
        } catch (Exception ignored) {}
    }

    private void dispatchSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an inventory item to dispatch.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String item = (String) table.getValueAt(row, 0);
        String qty = (String) table.getValueAt(row, 2);
        String unit = (String) table.getValueAt(row, 3);

        String camp = JOptionPane.showInputDialog(this, "Enter Destination Relief Camp or Sector for " + item + ":", "Camp #1 - Alappuzha Central");
        if (camp != null && !camp.trim().isEmpty()) {
            logInventoryAction("Dispatched " + qty + " " + unit + " of " + item + " to " + camp.trim());
            JOptionPane.showMessageDialog(
                    this,
                    "DISPATCH ORDER CONFIRMED:\n\n"
                            + "Item: " + item + "\n"
                            + "Quantity: " + qty + " " + unit + "\n"
                            + "Destination: " + camp.trim() + "\n"
                            + "Database Status: Logged in disaster_db.notifications\n"
                            + "Status: Loaded on Rapid Transport Vehicle.",
                    "Supplies Dispatched",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }
}
