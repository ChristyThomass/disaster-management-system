package disaster.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.RoundRectangle2D;

public class DashboardFrame extends BaseFrame {

    private static DashboardFrame activeInstance;
    private String username;
    private SmoothMarqueePanel smoothTicker;
    private JPanel topTelemetryPanel;
    private JPanel rightActions;
    private final String alertMessage = "[Emergency Alert] Controlled Reservoir Spill Discharge: Banasurasagar & Pamba Dams    •    [Emergency Alert] Severe Thunderstorm & High Wind Advisory for Interior Kerala    •    [Emergency Alert] Heavy Rain & Landslide Warning for Wayanad & Idukki Ghats    •    ";

    public static DashboardFrame getActiveInstance() {
        return activeInstance;
    }

    public DashboardFrame() {
        this("Demo User");
    }

    public DashboardFrame(String username) {
        super("Smart Disaster Management System - Kerala SDMA Portal", 1240, 840);
        setMinimumSize(new Dimension(1020, 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        activeInstance = this;
        this.username = username;
        disaster.service.UserSession.addListener(u -> {
            if (u != null) {
                updateSessionUser(u.getUsername());
            } else {
                updateSessionUser("Demo User");
            }
        });
        buildUI();
        startMarquee();
    }

    private void buildUI() {
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(UITheme.BACKGROUND);

        // =========================================================================
        // 1. TOP SECTION (CRITICAL UPDATES MARQUEE + SINGLE-ROW MAIN NAV BAR)
        // =========================================================================
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);

        // Top Critical Updates Bar (Dark Crimson) with System Telemetry
        JPanel marqueeBar = new JPanel(new BorderLayout(12, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(UITheme.TOP_BAR_BG);
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        marqueeBar.setOpaque(false);
        marqueeBar.setPreferredSize(new Dimension(1200, 36));
        marqueeBar.setMaximumSize(new Dimension(Short.MAX_VALUE, 36));
        marqueeBar.setBorder(new EmptyBorder(4, 16, 4, 16));

        JPanel marqueeTag = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        marqueeTag.setOpaque(false);

        // Clean custom drawn alert icon
        JPanel alertIconBadge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UITheme.applyQualityHints(g2);
                g2.setColor(new Color(253, 224, 71)); // Yellow
                int[] xPoints = {getWidth() / 2, getWidth() - 2, 2};
                int[] yPoints = {2, getHeight() - 2, getHeight() - 2};
                g2.fillPolygon(xPoints, yPoints, 3);
                g2.setColor(Color.BLACK);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                g2.drawString("!", getWidth() / 2 - 2, getHeight() - 4);
                g2.dispose();
            }
        };
        alertIconBadge.setPreferredSize(new Dimension(14, 14));
        alertIconBadge.setOpaque(false);
        marqueeTag.add(alertIconBadge);

        JLabel updateLbl = new JLabel("CRITICAL UPDATES  • ");
        updateLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        updateLbl.setForeground(Color.WHITE);
        marqueeTag.add(updateLbl);
        marqueeBar.add(marqueeTag, BorderLayout.WEST);

        smoothTicker = new SmoothMarqueePanel(
                alertMessage,
                new Color(254, 226, 226),
                new Font("Segoe UI", Font.PLAIN, 12)
        );
        marqueeBar.add(smoothTicker, BorderLayout.CENTER);

        topContainer.add(marqueeBar);

        // Main Navigation Bar (Clean White, Single Row Guaranteed Alignment)
        JPanel navBar = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(UITheme.BORDER);
                g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
                super.paintComponent(g);
            }
        };
        navBar.setOpaque(false);
        navBar.setPreferredSize(new Dimension(1200, 56));
        navBar.setMaximumSize(new Dimension(Short.MAX_VALUE, 65));
        navBar.setBorder(new EmptyBorder(8, 16, 8, 16));

        // Brand Logo Text
        JLabel brandTitle = new JLabel("Smart Disaster Management");
        brandTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        brandTitle.setForeground(UITheme.BRAND_CRIMSON);
        navBar.add(brandTitle, BorderLayout.WEST);

        // Navigation Tabs (Center - Single Line with balanced spacing)
        JPanel tabsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        tabsPanel.setOpaque(false);

        tabsPanel.add(createNavTab("Home", true, null));
        tabsPanel.add(createNavTab("Active Alerts", false, e -> openPopup(new AlertFrame())));

        // Kerala Map with LIVE Badge
        JPanel mapTabPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        mapTabPanel.setOpaque(false);
        mapTabPanel.add(createNavTab("Kerala Map", false, e -> openPopup(new GPSFrame())));
        mapTabPanel.add(createBadge("LIVE", Color.WHITE, UITheme.BRAND_CRIMSON));
        tabsPanel.add(mapTabPanel);

        tabsPanel.add(createNavTab("Report", false, e -> openPopup(new IncidentReportFrame())));
        tabsPanel.add(createNavTab("Volunteers", false, e -> openPopup(new VolunteerFrame())));
        tabsPanel.add(createNavTab("Inventory", false, e -> openPopup(new InventoryFrame())));
        tabsPanel.add(createNavTab("Records", false, e -> openPopup(new EmergencyContactsFrame())));

        navBar.add(tabsPanel, BorderLayout.CENTER);

        // Right Nav Actions: SOS + Sign In / User Profile
        rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightActions.setOpaque(false);
        refreshNavRightActions();

        navBar.add(rightActions, BorderLayout.EAST);
        topContainer.add(navBar);

        rootPanel.add(topContainer, BorderLayout.NORTH);

        // =========================================================================
        // 2. MAIN SCROLLABLE CONTENT BODY (SMOOTH ACCELERATED SCROLLING)
        // =========================================================================
        JPanel contentContainer = new JPanel();
        contentContainer.setLayout(new BoxLayout(contentContainer, BoxLayout.Y_AXIS));
        contentContainer.setBackground(UITheme.BACKGROUND);
        contentContainer.setBorder(new EmptyBorder(22, 28, 22, 28));

        // ---------------- SECTION 1: HERO CARD ----------------
        JPanel heroCard = createCustomCard(12, UITheme.HERO_BG, UITheme.HERO_BORDER);
        heroCard.setLayout(new BoxLayout(heroCard, BoxLayout.Y_AXIS));
        heroCard.setBorder(new EmptyBorder(26, 32, 26, 32));
        heroCard.setMaximumSize(new Dimension(Short.MAX_VALUE, 280));
        heroCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Clean Live Monitoring Active Pill
        JPanel heroBadge = createHeroBadge("LIVE MONITORING ACTIVE");
        heroBadge.setAlignmentX(Component.LEFT_ALIGNMENT);
        heroCard.add(heroBadge);

        heroCard.add(Box.createVerticalStrut(14));

        // Giant Title
        JLabel heroTitle = new JLabel("<html>Rapid Response.<br>Resilient Communities.</html>");
        heroTitle.setFont(UITheme.HERO_TITLE_FONT);
        heroTitle.setForeground(UITheme.TEXT);
        heroTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        heroCard.add(heroTitle);

        heroCard.add(Box.createVerticalStrut(10));

        // Subtitle
        JLabel heroSubtitle = new JLabel("<html>Coordinating real-time disaster intelligence to empower first responders and protect vulnerable populations when seconds matter most.</html>");
        heroSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        heroSubtitle.setForeground(UITheme.SECONDARY_TEXT);
        heroSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        heroCard.add(heroSubtitle);

        heroCard.add(Box.createVerticalStrut(18));

        // 3 Action Buttons Row (Clean Text Without Broken Glyph Boxes)
        JPanel heroBtnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        heroBtnRow.setOpaque(false);
        heroBtnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton mapBtn = button("Kerala Live Relief Map", UITheme.BRAND_CRIMSON);
        mapBtn.setPreferredSize(new Dimension(190, 40));
        mapBtn.addActionListener(e -> openPopup(new GPSFrame()));
        heroBtnRow.add(mapBtn);

        JButton reportBtn = createOutlinedButton("Report a Disaster", UITheme.BRAND_CRIMSON, UITheme.BRAND_CRIMSON);
        reportBtn.setPreferredSize(new Dimension(160, 40));
        reportBtn.addActionListener(e -> openPopup(new IncidentReportFrame()));
        heroBtnRow.add(reportBtn);

        JButton helpBtn = createOutlinedButton("Request Help", UITheme.HOTLINE_BLUE, UITheme.HOTLINE_BLUE);
        helpBtn.setPreferredSize(new Dimension(140, 40));
        helpBtn.addActionListener(e -> openPopup(new SOSFrame(username)));
        heroBtnRow.add(helpBtn);

        heroCard.add(heroBtnRow);
        contentContainer.add(heroCard);

        contentContainer.add(Box.createVerticalStrut(18));

        // ---------------- SECTION 2: 4 METRIC KPI CARDS ----------------
        JPanel kpiRow = new JPanel(new GridLayout(1, 4, 14, 0));
        kpiRow.setOpaque(false);
        kpiRow.setMaximumSize(new Dimension(Short.MAX_VALUE, 135));
        kpiRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        kpiRow.add(createKpiBox(
                "KERALA RELIEF CAMPS",
                "LIVE KERALA MAP",
                String.valueOf(disaster.service.AdminDataManager.getInstance().getShelterHomes().size()),
                "Occupancy & Shelter Zones",
                e -> openPopup(new GPSFrame())
        ));

        kpiRow.add(createKpiBox(
                "ACTIVE ALERTS",
                "CRITICAL BULLETINS",
                String.valueOf(disaster.service.AdminDataManager.getInstance().getAlerts().size()),
                "Code Red & Warning Notices",
                e -> openPopup(new AlertFrame())
        ));

        kpiRow.add(createKpiBox(
                "CERTIFIED RESPONDERS",
                "DEPLOYMENT READY",
                String.valueOf(disaster.service.AdminDataManager.getInstance().getVolunteers().size()),
                "Active Field Volunteers",
                e -> openPopup(new VolunteerFrame())
        ));

        kpiRow.add(createKpiBox(
                "WAREHOUSE SUPPLIES",
                "LOGISTICS DEPOT",
                String.valueOf(disaster.service.AdminDataManager.getInstance().getInventoryItems().size()),
                "Medical, Rations & Equipment",
                e -> openPopup(new InventoryFrame())
        ));

        contentContainer.add(kpiRow);

        contentContainer.add(Box.createVerticalStrut(20));

        // ---------------- SECTION 3: STATE EMERGENCY HOTLINES ----------------
        JPanel hotlineContainer = createCard(10);
        hotlineContainer.setLayout(new BoxLayout(hotlineContainer, BoxLayout.Y_AXIS));
        hotlineContainer.setBorder(new EmptyBorder(16, 20, 18, 20));
        hotlineContainer.setMaximumSize(new Dimension(Short.MAX_VALUE, 175));
        hotlineContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel hotlineTitle = new JLabel("State Emergency Hotlines & Quick Contact");
        hotlineTitle.setFont(UITheme.HEADER_FONT);
        hotlineTitle.setForeground(UITheme.TEXT);
        hotlineTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        hotlineContainer.add(hotlineTitle);

        hotlineContainer.add(Box.createVerticalStrut(12));

        JPanel hotlineTiles = new JPanel(new GridLayout(1, 5, 12, 0));
        hotlineTiles.setOpaque(false);
        hotlineTiles.setAlignmentX(Component.LEFT_ALIGNMENT);

        hotlineTiles.add(createHotlineCard("National Emergency", "112", UITheme.HOTLINE_RED));
        hotlineTiles.add(createHotlineCard("District Control Room", "1077", UITheme.BRAND_CRIMSON));
        hotlineTiles.add(createHotlineCard("State Ops Center", "1070", UITheme.HOTLINE_TEAL));
        hotlineTiles.add(createHotlineCard("Ambulance / Triage", "108", UITheme.HOTLINE_BLUE));
        hotlineTiles.add(createHotlineCard("Fire & Rescue Force", "101", UITheme.HOTLINE_RED));

        hotlineContainer.add(hotlineTiles);
        contentContainer.add(hotlineContainer);

        contentContainer.add(Box.createVerticalStrut(20));

        // ---------------- SECTION 4: GROUND BULLETINS ----------------
        JPanel bulletinsContainer = createCard(10);
        bulletinsContainer.setLayout(new BoxLayout(bulletinsContainer, BoxLayout.Y_AXIS));
        bulletinsContainer.setBorder(new EmptyBorder(16, 20, 18, 20));
        bulletinsContainer.setMaximumSize(new Dimension(Short.MAX_VALUE, 220));
        bulletinsContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel bulletinTitle = new JLabel("Live Disaster Intelligence & Ground Bulletins");
        bulletinTitle.setFont(UITheme.HEADER_FONT);
        bulletinTitle.setForeground(UITheme.TEXT);
        bulletinTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        bulletinsContainer.add(bulletinTitle);

        bulletinsContainer.add(Box.createVerticalStrut(10));

        for (disaster.service.AdminDataManager.GroundBulletin b : disaster.service.AdminDataManager.getInstance().getBulletins()) {
            Color tagColor = "DANGER".equalsIgnoreCase(b.getSeverity()) ? UITheme.DANGER :
                             "PRIMARY".equalsIgnoreCase(b.getSeverity()) ? UITheme.PRIMARY : UITheme.SUCCESS;
            bulletinsContainer.add(createBulletinRow(b.getTime(), b.getMessage(), tagColor));
            bulletinsContainer.add(Box.createVerticalStrut(6));
        }

        contentContainer.add(bulletinsContainer);

        // Smooth responsive scrollPane
        JScrollPane scrollPane = new JScrollPane(contentContainer);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        scrollPane.getVerticalScrollBar().setBlockIncrement(100);

        rootPanel.add(scrollPane, BorderLayout.CENTER);

        // =========================================================================
        // 3. BOTTOM FOOTER STATUS BAR
        // =========================================================================
        JPanel footerBar = new JPanel(new BorderLayout());
        footerBar.setBackground(Color.WHITE);
        footerBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER),
                new EmptyBorder(8, 24, 8, 24)
        ));

        JLabel leftFoot = new JLabel("Smart Disaster Management System • Kerala SDMA Unified Command");
        leftFoot.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        leftFoot.setForeground(UITheme.SECONDARY_TEXT);
        footerBar.add(leftFoot, BorderLayout.WEST);

        JLabel rightFoot = new JLabel("Status: Operations Active • Real-time Monitoring");
        rightFoot.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rightFoot.setForeground(UITheme.SUCCESS);
        footerBar.add(rightFoot, BorderLayout.EAST);

        rootPanel.add(footerBar, BorderLayout.SOUTH);

        setContentPane(rootPanel);
    }

    private JPanel createHeroBadge(String text) {
        JPanel badge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UITheme.applyQualityHints(g2);
                g2.setColor(UITheme.HERO_BADGE_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 6, 6));
                g2.setColor(new Color(UITheme.HERO_BADGE_TEXT.getRed(), UITheme.HERO_BADGE_TEXT.getGreen(), UITheme.HERO_BADGE_TEXT.getBlue(), 100));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, 6, 6));

                // Clean small red square dot
                g2.setColor(UITheme.HERO_BADGE_TEXT);
                g2.fillRect(10, getHeight() / 2 - 3, 7, 7);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setOpaque(false);
        badge.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 3));
        badge.setBorder(new EmptyBorder(0, 16, 0, 8));
        JLabel lbl = new JLabel(text);
        lbl.setFont(UITheme.BADGE_FONT);
        lbl.setForeground(UITheme.HERO_BADGE_TEXT);
        badge.add(lbl);
        return badge;
    }

    private JButton createNavTab(String title, boolean isActive, java.awt.event.ActionListener action) {
        JButton btn = new JButton(title);
        btn.setFont(new Font("Segoe UI", isActive ? Font.BOLD : Font.PLAIN, 12));
        btn.setForeground(isActive ? UITheme.BRAND_CRIMSON : UITheme.SECONDARY_TEXT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBorder(new EmptyBorder(4, 5, 4, 5));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        if (action != null) {
            btn.addActionListener(action);
        }
        return btn;
    }

    private JPanel createKpiBox(String title, String badgeText, String count, String subtitle, java.awt.event.ActionListener action) {
        JPanel card = createCard(8);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(14, 16, 14, 16));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Top Header Row
        JPanel top = new JPanel(new BorderLayout(8, 0));
        top.setOpaque(false);
        top.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 10));
        t.setForeground(UITheme.MUTED);
        top.add(t, BorderLayout.WEST);

        JPanel b = createBadge(badgeText, UITheme.HERO_BADGE_TEXT, UITheme.HERO_BADGE_BG);
        top.add(b, BorderLayout.EAST);
        card.add(top);

        card.add(Box.createVerticalStrut(8));

        // Big Count
        JLabel countLbl = new JLabel(count);
        countLbl.setFont(UITheme.KPI_NUMBER_FONT);
        countLbl.setForeground(UITheme.BRAND_CRIMSON);
        countLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(countLbl);

        card.add(Box.createVerticalStrut(4));

        // Subtitle
        JLabel sub = new JLabel(subtitle);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sub.setForeground(UITheme.SECONDARY_TEXT);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(sub);

        if (action != null) {
            card.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    action.actionPerformed(null);
                }
            });
        }

        return card;
    }

    private JPanel createHotlineCard(String label, String number, Color numColor) {
        JPanel tile = new JPanel();
        tile.setLayout(new BoxLayout(tile, BoxLayout.Y_AXIS));
        tile.setBackground(Color.WHITE);
        tile.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1),
                new EmptyBorder(10, 14, 10, 14)
        ));
        tile.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(UITheme.SECONDARY_TEXT);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        tile.add(l);

        tile.add(Box.createVerticalStrut(4));

        JLabel n = new JLabel(number);
        n.setFont(new Font("Segoe UI", Font.BOLD, 22));
        n.setForeground(numColor);
        n.setAlignmentX(Component.LEFT_ALIGNMENT);
        tile.add(n);

        tile.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int res = JOptionPane.showConfirmDialog(
                        DashboardFrame.this,
                        "Connect priority emergency call to " + label + " (" + number + ")?",
                        "Emergency Helpline",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );
                if (res == JOptionPane.YES_OPTION) {
                    JOptionPane.showMessageDialog(DashboardFrame.this, "Connecting to " + label + " at " + number + "...", "Call Connected", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        return tile;
    }

    private JPanel createBulletinRow(String time, String text, Color dotColor) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel dot = new JLabel("●");
        dot.setFont(new Font("Segoe UI", Font.BOLD, 12));
        dot.setForeground(dotColor);
        row.add(dot);

        JLabel timeLbl = new JLabel("[" + time + "]");
        timeLbl.setFont(UITheme.CODE_FONT);
        timeLbl.setForeground(UITheme.MUTED);
        row.add(timeLbl);

        JLabel contentLbl = new JLabel(text);
        contentLbl.setFont(UITheme.NORMAL_FONT);
        contentLbl.setForeground(UITheme.TEXT);
        row.add(contentLbl);

        return row;
    }

    private void startMarquee() {
        if (smoothTicker != null) {
            smoothTicker.start();
        }
    }

    public void openPopup(JFrame frame) {
        if (frame != null) {
            frame.setLocationRelativeTo(this);
            frame.setVisible(true);
            frame.toFront();
            frame.requestFocus();
        }
    }

    public void updateSessionUser(String newUsername) {
        this.username = (newUsername == null || newUsername.trim().isEmpty()) ? "Demo User" : newUsername.trim();
        refreshNavRightActions();
        refreshTopTelemetry();
    }

    private void refreshTopTelemetry() {
        if (topTelemetryPanel == null) return;
        topTelemetryPanel.removeAll();
        topTelemetryPanel.revalidate();
        topTelemetryPanel.repaint();
    }

    private void refreshNavRightActions() {
        if (rightActions == null) return;
        rightActions.removeAll();

        JButton sosBtn = button("EMERGENCY SOS", UITheme.BRAND_CRIMSON);
        sosBtn.setPreferredSize(new Dimension(130, 34));
        sosBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        sosBtn.addActionListener(e -> openPopup(new SOSFrame(username)));
        rightActions.add(sosBtn);

        if ("Demo User".equalsIgnoreCase(username)) {
            JButton signInBtn = createOutlinedButton("Sign In", UITheme.BORDER_DARK, UITheme.TEXT);
            signInBtn.setPreferredSize(new Dimension(85, 34));
            signInBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            signInBtn.addActionListener(e -> openPopup(new LoginFrame()));
            rightActions.add(signInBtn);
        } else {
            JButton userBtn = createOutlinedButton(username, UITheme.BRAND_CRIMSON, UITheme.BRAND_CRIMSON);
            int textW = userBtn.getFontMetrics(new Font("Segoe UI", Font.BOLD, 12)).stringWidth(username);
            userBtn.setPreferredSize(new Dimension(Math.max(110, textW + 28), 34));
            userBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            userBtn.setToolTipText("User Profile & Options");

            // Profile popup menu containing Profile/Admin and Sign Out
            JPopupMenu userMenu = new JPopupMenu();
            userMenu.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1));
            userMenu.setBackground(Color.WHITE);

            boolean isAdmin = "Admin Panel".equalsIgnoreCase(username);
            JMenuItem profileItem = new JMenuItem(isAdmin ? "  ⚙️ Admin Panel" : "  👤 My Profile");
            profileItem.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            profileItem.setBackground(Color.WHITE);
            profileItem.setForeground(UITheme.TEXT);
            profileItem.setCursor(new Cursor(Cursor.HAND_CURSOR));
            profileItem.addActionListener(e -> {
                if (isAdmin) {
                    new AdminFrame().setVisible(true);
                } else {
                    openPopup(new ProfileFrame(username));
                }
            });
            userMenu.add(profileItem);

            userMenu.addSeparator();

            JMenuItem signOutItem = new JMenuItem("  🚪 Sign Out");
            signOutItem.setFont(new Font("Segoe UI", Font.BOLD, 13));
            signOutItem.setBackground(Color.WHITE);
            signOutItem.setForeground(UITheme.BRAND_CRIMSON);
            signOutItem.setCursor(new Cursor(Cursor.HAND_CURSOR));
            signOutItem.addActionListener(e -> {
                int res = JOptionPane.showConfirmDialog(
                        this,
                        "Are you sure you want to sign out from SDMA Portal?",
                        "Sign Out",
                        JOptionPane.YES_NO_OPTION
                );
                if (res == JOptionPane.YES_OPTION) {
                    disaster.service.UserSession.setCurrentUser(null);
                    updateSessionUser("Demo User");
                    if (isAdmin) {
                        new LoginFrame().setVisible(true);
                    }
                }
            });
            userMenu.add(signOutItem);

            userBtn.addActionListener(e -> {
                userMenu.show(userBtn, 0, userBtn.getHeight() + 2);
            });
            rightActions.add(userBtn);
        }

        rightActions.revalidate();
        rightActions.repaint();
    }

    /**
     * 60 FPS Continuous Pixel-by-Pixel Smooth Marquee Component
     * Antialiased, double-buffered, seamlessly looped, and pauses on hover.
     */
    public static class SmoothMarqueePanel extends JComponent {
        private String text;
        private double xOffset = 0;
        private final Timer timer;
        private boolean isHovered = false;
        private final Color textColor;
        private final Font font;

        public SmoothMarqueePanel(String text, Color textColor, Font font) {
            this.text = text;
            this.textColor = textColor;
            this.font = font;
            setDoubleBuffered(true);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setToolTipText("Live emergency bulletins — Hover to pause ticker");

            // 60 FPS animation timer (~16ms per frame)
            // Moving ~0.95 px/frame provides ultra-smooth, jitter-free scrolling
            timer = new Timer(16, e -> {
                if (!isHovered && getWidth() > 0) {
                    xOffset += 0.95;
                    repaint();
                }
            });

            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    isHovered = true;
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    isHovered = false;
                }
            });
        }

        public void start() {
            if (!timer.isRunning()) {
                timer.start();
            }
        }

        public void stop() {
            if (timer.isRunning()) {
                timer.stop();
            }
        }

        public void setText(String newText) {
            this.text = newText;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (text == null || text.trim().isEmpty() || getWidth() <= 0) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setFont(font);
            g2.setColor(textColor);

            FontMetrics fm = g2.getFontMetrics();
            int textWidth = fm.stringWidth(text);
            int gap = 80;
            int cycle = textWidth + gap;

            if (cycle <= 0) {
                g2.dispose();
                return;
            }

            if (xOffset >= cycle) {
                xOffset %= cycle;
            }

            // Vertically center text in the bar
            int textY = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();

            // Render seamless loops across the visible width
            double drawX = -xOffset;
            while (drawX < getWidth()) {
                g2.drawString(text, (int) Math.round(drawX), textY);
                drawX += cycle;
            }

            g2.dispose();
        }
    }
}