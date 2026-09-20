package disaster.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Real-Time Interactive Kerala Relief Map
 * Replaces old radar with an authentic OpenStreetMap / Leaflet style geographic map
 * with interactive pins, live disaster telemetry, GPS locator, pan & zoom.
 */
public class GPSFrame extends BaseFrame {

    private JLabel statusBadge;
    private KeralaRealtimeMapPanel mapPanel;
    private String activeFilter = "ALL";
    private JLabel coordinatesLabel;

    public GPSFrame() {
        super("Kerala Live Disaster & Relief Map - Kerala SDMA", 1120, 750);
        setMinimumSize(new Dimension(980, 640));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        if (DashboardFrame.getActiveInstance() != null) {
            setLocationRelativeTo(DashboardFrame.getActiveInstance());
        }
        buildUI();
    }

    private void buildUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 8));
        mainPanel.setBackground(UITheme.BACKGROUND);
        mainPanel.setBorder(new EmptyBorder(14, 18, 14, 18));

        // 1. TOP HEADER & FILTER BAR
        JPanel topBar = new JPanel(new BorderLayout(14, 8));
        topBar.setOpaque(false);

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        titleRow.setOpaque(false);

        JButton topBackBtn = createBackButton("← Back");
        topBackBtn.setPreferredSize(new Dimension(85, 34));
        titleRow.add(topBackBtn);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel titleLbl = heading("Kerala Live Real-Time Relief Map & Telemetry");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 17));
        titleBlock.add(titleLbl);

        JLabel subLbl = subtitle("Live geospatial intelligence across reservoir basins, relief depots & triage hubs");
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleBlock.add(subLbl);

        titleRow.add(titleBlock);
        topBar.add(titleRow, BorderLayout.WEST);

        // Right side: Active Live Badge + Coordinates display
        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        headerRight.setOpaque(false);

        coordinatesLabel = new JLabel("Lat: 9.9312° N  Lon: 76.2673° E");
        coordinatesLabel.setFont(UITheme.CODE_FONT);
        coordinatesLabel.setForeground(UITheme.MUTED);
        headerRight.add(coordinatesLabel);

        statusBadge = new JLabel("● LIVE SATELLITE & GROUND TELEMETRY");
        statusBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        statusBadge.setForeground(UITheme.SUCCESS);
        headerRight.add(statusBadge);

        topBar.add(headerRight, BorderLayout.EAST);

        // Filter Bar (All / Code Red Alerts / Relief Camps / Medical Units)
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filterRow.setOpaque(false);

        filterRow.add(createFilterButton("All Points (12)", "ALL", true));
        filterRow.add(createFilterButton("Code Red Alerts (3)", "ALERTS", false));
        filterRow.add(createFilterButton("Relief Camps (4)", "CAMPS", false));
        filterRow.add(createFilterButton("Medical Units (2)", "MEDICAL", false));
        filterRow.add(createFilterButton("Logistics Depots (3)", "DEPOTS", false));

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setOpaque(false);
        topContainer.add(topBar, BorderLayout.NORTH);
        topContainer.add(Box.createVerticalStrut(6), BorderLayout.CENTER);
        topContainer.add(filterRow, BorderLayout.SOUTH);

        mainPanel.add(topContainer, BorderLayout.NORTH);

        // 2. CENTER REAL-TIME INTERACTIVE MAP
        mapPanel = new KeralaRealtimeMapPanel();
        mainPanel.add(mapPanel, BorderLayout.CENTER);

        // 3. BOTTOM ACTION BAR
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);
        bottomBar.setBorder(new EmptyBorder(6, 4, 0, 4));

        JLabel legend = new JLabel("<html><span style='color:#991B1B;'>●</span> Red Alert Dam Spill &nbsp; <span style='color:#D97706;'>●</span> Advisory &nbsp; <span style='color:#059669;'>●</span> Broadcast &nbsp; <span style='color:#2563EB;'>●</span> Shelter Camp &nbsp; <span style='color:#DC2626;'>●</span> Medical Triage</html>");
        legend.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        bottomBar.add(legend, BorderLayout.WEST);

        JPanel bottomActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomActions.setOpaque(false);

        JButton copyGpsBtn = createOutlineButton("Copy Selected GPS", UITheme.BORDER_DARK);
        copyGpsBtn.setPreferredSize(new Dimension(145, 34));
        copyGpsBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        copyGpsBtn.addActionListener(e -> {
            MapPin pin = mapPanel.getSelectedPin();
            String coords = pin != null ? (pin.lat + "° N, " + pin.lon + "° E (" + pin.title + ")") : "9.9312° N, 76.2673° E (Kerala Central SDMA)";
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(coords), null);
            JOptionPane.showMessageDialog(this, "Coordinates copied to clipboard:\n" + coords, "GPS Copied", JOptionPane.INFORMATION_MESSAGE);
        });
        bottomActions.add(copyGpsBtn);

        JButton backBtn = button("← Back to Portal", UITheme.BRAND_CRIMSON);
        backBtn.setPreferredSize(new Dimension(135, 34));
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        backBtn.addActionListener(e -> closeAndReturnToPortal());
        bottomActions.add(backBtn);

        bottomBar.add(bottomActions, BorderLayout.EAST);
        mainPanel.add(bottomBar, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JButton createFilterButton(String text, String filterKey, boolean defaultSelected) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        Runnable updateStyle = () -> {
            if (activeFilter.equals(filterKey)) {
                btn.setBackground(UITheme.BRAND_CRIMSON);
                btn.setForeground(Color.WHITE);
                btn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
            } else {
                btn.setBackground(Color.WHITE);
                btn.setForeground(UITheme.TEXT);
                btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(UITheme.BORDER, 1),
                        BorderFactory.createEmptyBorder(5, 11, 5, 11)
                ));
            }
        };

        btn.addActionListener(e -> {
            activeFilter = filterKey;
            mapPanel.setFilter(filterKey);
            // update all sibling buttons
            Container parent = btn.getParent();
            if (parent != null) {
                for (Component c : parent.getComponents()) {
                    if (c instanceof JButton) {
                        c.repaint();
                    }
                }
            }
            btn.repaint();
        });

        // custom paint to respect active state
        btn.addPropertyChangeListener("activeFilter", evt -> updateStyle.run());
        updateStyle.run();
        return btn;
    }

    // =========================================================================
    // MAP PIN DATA MODEL
    // =========================================================================
    public static class MapPin {
        public double lat, lon;
        public String title;
        public String category; // ALERTS, CAMPS, MEDICAL, DEPOTS
        public String status;
        public String details;
        public Color pinColor;
        public String iconType; // "TRIANGLE", "CROSS", "MEGAPHONE", "DEPOT"

        public MapPin(double lat, double lon, String title, String category, String status, String details, Color pinColor, String iconType) {
            this.lat = lat;
            this.lon = lon;
            this.title = title;
            this.category = category;
            this.status = status;
            this.details = details;
            this.pinColor = pinColor;
            this.iconType = iconType;
        }
    }

    // =========================================================================
    // REAL-TIME KERALA MAP PANEL (OPENSTREETMAP / LEAFLET RENDERING)
    // =========================================================================
    private class KeralaRealtimeMapPanel extends JPanel {

        private double zoom = 1.0;
        private double panX = 0;
        private double panY = 0;
        private Point dragStartPoint;

        private final List<MapPin> pins = new ArrayList<>();
        private MapPin hoveredPin = null;
        private MapPin selectedPin = null;

        // GPS Pulse animation
        private float pulseRadius = 12f;
        private boolean pulseGrowing = true;
        private Timer pulseTimer;

        // Bounding box for Kerala & Southern Coastline
        private final double minLat = 7.9;
        private final double maxLat = 13.2;
        private final double minLon = 71.5;
        private final double maxLon = 78.5;

        // Geographic City Centers
        private final List<CityLabel> cities = new ArrayList<>();

        private String toastText = null;
        private Timer toastTimer;

        public KeralaRealtimeMapPanel() {
            setBackground(new Color(179, 216, 229)); // Arabian / Lakshadweep Sea Soft Blue
            setOpaque(true);
            setLayout(null);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            toastTimer = new Timer(3500, e -> {
                toastText = null;
                repaint();
            });
            toastTimer.setRepeats(false);

            initMapData();
            initListeners();

            pulseTimer = new Timer(50, e -> {
                if (pulseGrowing) {
                    pulseRadius += 0.8f;
                    if (pulseRadius > 26f) pulseGrowing = false;
                } else {
                    pulseRadius -= 0.8f;
                    if (pulseRadius < 10f) pulseGrowing = true;
                }
                repaint();
            });
            pulseTimer.start();
        }

        public MapPin getSelectedPin() {
            return selectedPin != null ? selectedPin : (pins.isEmpty() ? null : pins.get(0));
        }

        public void setFilter(String filter) {
            repaint();
        }

        private void initMapData() {
            // 1. Critical Disaster Pins matching uploaded screenshot
            pins.add(new MapPin(11.6664, 75.9558, "Banasurasagar Dam Spill Zone", "ALERTS", "Code Red - Spill Active", "Shutter raised 10cm. Downstream Kabini river flood surge warning.", new Color(185, 28, 28), "TRIANGLE"));
            pins.add(new MapPin(11.2588, 75.7804, "Kozhikode Coastal Safety Command", "ALERTS", "Broadcast Active", "High swell alert along Beypore and Puthiyappa harbors.", new Color(5, 150, 105), "MEGAPHONE"));
            pins.add(new MapPin(11.0510, 76.0711, "Malappuram & Palakkad Weather Advisory", "ALERTS", "Squall Alert", "Severe thunderstorm & high winds up to 55 km/h recorded.", new Color(217, 119, 6), "MEGAPHONE"));

            pins.add(new MapPin(10.5276, 76.2144, "Thrissur Emergency Shelter Cluster", "CAMPS", "Capacity: 380/500", "St. Thomas Relief Camp active with dry ration kits & blankets.", new Color(37, 99, 235), "DEPOT"));
            pins.add(new MapPin(9.9312, 76.2673, "Kochi Unified Disaster Logistics HQ", "DEPOTS", "HQ Operational", "Central stockpile of 8,000 rations, dewatering pumps, boats.", new Color(30, 64, 175), "DEPOT"));
            pins.add(new MapPin(9.4981, 76.3388, "Alappuzha Triage & Flood Medical Center", "MEDICAL", "Medical Level 3", "20 paramedical staff, ORS supplies, anti-venom on standby.", new Color(220, 38, 38), "CROSS"));
            pins.add(new MapPin(9.3834, 76.5741, "Kuttanad Emergency Boat Station", "MEDICAL", "Rescue Unit", "Motorized fiberglass rescue boats deployed for submerged wards.", new Color(220, 38, 38), "CROSS"));

            pins.add(new MapPin(9.8494, 76.9720, "Idukki Dam High-Range Reservoir Watch", "ALERTS", "Code Orange Alert", "Reservoir elevation at 2,398.2 ft. Controlled outflow ready.", new Color(185, 28, 28), "TRIANGLE"));
            pins.add(new MapPin(9.2648, 76.7870, "Pamba River Basin Watch • Pathanamthitta", "ALERTS", "Advisory Active", "Downstream overflow monitoring at Ranni & Aranmula.", new Color(217, 119, 6), "MEGAPHONE"));
            pins.add(new MapPin(8.8932, 76.6141, "Kollam Coastal Relief Station", "CAMPS", "Capacity: 240/350", "Cooked food rations & shelter for displaced coastal residents.", new Color(5, 150, 105), "MEGAPHONE"));
            pins.add(new MapPin(8.5241, 76.9366, "Thiruvananthapuram State Control Center", "DEPOTS", "Command Hub", "Kerala State Disaster Management Authority Central Ops.", new Color(30, 64, 175), "DEPOT"));
            pins.add(new MapPin(11.8745, 75.3704, "Kannur Northern Relief Camp", "CAMPS", "Capacity: 290/400", "Operational shelter zone at Government High School.", new Color(37, 99, 235), "DEPOT"));

            // Load live GPS locations from MySQL disaster_db
            try (java.sql.Connection conn = disaster.backend.DatabaseManager.getInstance().getConnection()) {
                if (conn != null) {
                    String sql = "SELECT g.latitude, g.longitude, s.sos_id, s.status, r.update_message "
                            + "FROM gps_locations g "
                            + "JOIN sos_requests s ON g.sos_id = s.sos_id "
                            + "LEFT JOIN rescue_status r ON s.sos_id = r.sos_id";
                    try (java.sql.Statement st = conn.createStatement();
                         java.sql.ResultSet rs = st.executeQuery(sql)) {
                        while (rs.next()) {
                            double lat = rs.getDouble("latitude");
                            double lon = rs.getDouble("longitude");
                            int id = rs.getInt("sos_id");
                            String desc = rs.getString("update_message");
                            pins.add(new MapPin(lat, lon, "SOS Distress #" + id, "ALERTS", "Live MySQL Telemetry", desc != null ? desc : "Active distress signal broadcast", UITheme.BRAND_CRIMSON, "TRIANGLE"));
                        }
                    }
                }
            } catch (Exception ignored) {}

            // 2. City Labels Matching Image
            cities.add(new CityLabel("Mangaluru", 12.9141, 74.8560));
            cities.add(new CityLabel("Hassan", 13.0033, 76.1004));
            cities.add(new CityLabel("Mysuru", 12.2958, 76.6394));
            cities.add(new CityLabel("Kannur", 11.8745, 75.3704));
            cities.add(new CityLabel("Kozhikode", 11.2588, 75.7804));
            cities.add(new CityLabel("Malappuram", 11.0510, 76.0711));
            cities.add(new CityLabel("Palakkad", 10.7867, 76.6548));
            cities.add(new CityLabel("Coimbatore", 11.0168, 76.9558));
            cities.add(new CityLabel("Salem", 11.6643, 78.1460));
            cities.add(new CityLabel("Erode", 11.3410, 77.7172));
            cities.add(new CityLabel("Thrissur", 10.5276, 76.2144));
            cities.add(new CityLabel("Kochi", 9.9312, 76.2673));
            cities.add(new CityLabel("Alappuzha", 9.4981, 76.3388));
            cities.add(new CityLabel("Dindigul", 10.3673, 77.9803));
            cities.add(new CityLabel("Madurai", 9.9252, 78.1198));
            cities.add(new CityLabel("Sivakasi", 9.4533, 77.7981));
            cities.add(new CityLabel("Kollam", 8.8932, 76.6141));
            cities.add(new CityLabel("Thiruvananthapuram", 8.5241, 76.9366));
            cities.add(new CityLabel("Thoothukudi", 8.7642, 78.1348));
            cities.add(new CityLabel("Nagercoil", 8.1833, 77.4119));
        }

        private void initListeners() {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    int w = getWidth();
                    int h = getHeight();

                    // 1. Check Floating "Locate My Position" Button
                    int btnW = 160, btnH = 36;
                    int btnX = w - btnW - 18, btnY = 16;
                    if (e.getX() >= btnX && e.getX() <= btnX + btnW && e.getY() >= btnY && e.getY() <= btnY + btnH) {
                        locateUser();
                        return;
                    }

                    // 2. Check Zoom In [+]
                    int zX = w - 44, zY = h - 95;
                    if (e.getX() >= zX && e.getX() <= zX + 28 && e.getY() >= zY && e.getY() <= zY + 28) {
                        zoomIn();
                        return;
                    }

                    // 3. Check Zoom Out [-]
                    int zoY = zY + 32;
                    if (e.getX() >= zX && e.getX() <= zX + 28 && e.getY() >= zoY && e.getY() <= zoY + 28) {
                        zoomOut();
                        return;
                    }

                    dragStartPoint = e.getPoint();

                    // 4. Check Pin Click
                    MapPin clicked = findPinAt(e.getX(), e.getY());
                    if (clicked != null) {
                        selectedPin = clicked;
                        coordinatesLabel.setText("Lat: " + String.format("%.4f", clicked.lat) + "° N  Lon: " + String.format("%.4f", clicked.lon) + "° E");
                    } else {
                        // Dismiss popup when clicking empty space
                        selectedPin = null;
                        hoveredPin = null;
                    }
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    dragStartPoint = null;
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (dragStartPoint != null) {
                        panX += (e.getX() - dragStartPoint.x);
                        panY += (e.getY() - dragStartPoint.y);
                        dragStartPoint = e.getPoint();
                        repaint();
                    }
                }

                @Override
                public void mouseMoved(MouseEvent e) {
                    MapPin found = findPinAt(e.getX(), e.getY());
                    if (found != hoveredPin) {
                        hoveredPin = found;
                        if (hoveredPin != null) {
                            coordinatesLabel.setText("Lat: " + String.format("%.4f", hoveredPin.lat) + "° N  Lon: " + String.format("%.4f", hoveredPin.lon) + "° E");
                        }
                        repaint();
                    }
                }
            });

            // Wheel to Zoom
            addMouseWheelListener(e -> {
                double zoomDelta = e.getWheelRotation() < 0 ? 1.15 : 0.85;
                double newZoom = zoom * zoomDelta;
                if (newZoom >= 0.7 && newZoom <= 3.5) {
                    int mx = e.getX();
                    int my = e.getY();
                    panX = mx - (mx - panX) * zoomDelta;
                    panY = my - (my - panY) * zoomDelta;
                    zoom = newZoom;
                    repaint();
                }
            });
        }

        private Point2D.Double geoToScreen(double lat, double lon) {
            int w = getWidth();
            int h = getHeight();

            double normX = (lon - minLon) / (maxLon - minLon);
            double normY = (maxLat - lat) / (maxLat - minLat);

            double baseW = Math.max(w, 800);
            double baseH = Math.max(h, 600);

            double sx = (normX * baseW) * zoom + panX;
            double sy = (normY * baseH) * zoom + panY;

            return new Point2D.Double(sx, sy);
        }

        private MapPin findPinAt(int mouseX, int mouseY) {
            for (MapPin pin : pins) {
                if (!matchesFilter(pin)) continue;
                Point2D.Double pt = geoToScreen(pin.lat, pin.lon);
                double dist = pt.distance(mouseX, mouseY);
                if (dist <= 18) {
                    return pin;
                }
            }
            return null;
        }

        private boolean matchesFilter(MapPin pin) {
            if ("ALL".equals(activeFilter)) return true;
            return activeFilter.equals(pin.category);
        }

        public void locateUser() {
            // Center on Central Kerala / Kochi: Lat 9.9312, Lon 76.2673
            int w = getWidth();
            int h = getHeight();
            zoom = 1.35;
            double normX = (76.2673 - minLon) / (maxLon - minLon);
            double normY = (maxLat - 9.9312) / (maxLat - minLat);
            double baseW = Math.max(w, 800);
            double baseH = Math.max(h, 600);
            panX = (w / 2.0) - (normX * baseW * zoom);
            panY = (h / 2.0) - (normY * baseH * zoom);

            statusBadge.setText("● GPS LOCKED: KOCHI HQ (±4m)");
            coordinatesLabel.setText("Lat: 9.9312° N  Lon: 76.2673° E");
            toastText = "📍 Position Located: 9.9312° N, 76.2673° E (Kochi Central) • Accuracy: ±4m";
            toastTimer.restart();
            repaint();
        }

        public void zoomIn() {
            if (zoom < 3.2) {
                panX -= (getWidth() / 2.0 - panX) * 0.2;
                panY -= (getHeight() / 2.0 - panY) * 0.2;
                zoom *= 1.2;
                repaint();
            }
        }

        public void zoomOut() {
            if (zoom > 0.7) {
                zoom /= 1.2;
                repaint();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            UITheme.applyQualityHints(g2);

            int w = getWidth();
            int h = getHeight();

            // 1. Ocean Background (Already set to sea blue #B3D8E5)
            g2.setColor(new Color(175, 214, 228));
            g2.fillRect(0, 0, w, h);

            // 2. Lakshadweep Island Outline & Sea Zone (Left Sea Polygon)
            drawLakshadweepZone(g2);

            // 3. Indian Mainland / Kerala Landmass
            drawLandmassAndCoast(g2);

            // 4. Inland Waterbodies (Vembanad Lake, Ashtamudi Lake)
            drawLakes(g2);

            // 5. Geographic City Labels
            drawCities(g2);

            // 6. Current GPS Position Beacon
            drawGpsBeacon(g2);

            // 7. Interactive Disaster Pins
            drawPins(g2);

            // 8. Hovered or Selected Pin Details Card
            if (hoveredPin != null) {
                drawPinPopup(g2, hoveredPin);
            } else if (selectedPin != null) {
                drawPinPopup(g2, selectedPin);
            }

            // 9. Floating On-Screen Map Controls
            drawFloatingControls(g2);

            // 10. Non-blocking Toast Notification Banner
            if (toastText != null) {
                drawToastBanner(g2, w);
            }

            // 11. Bottom-Right Leaflet / OSM Attribution Strip
            drawAttributionBar(g2, w, h);

            g2.dispose();
        }

        private void drawLakshadweepZone(Graphics2D g2) {
            // Draw maritime territory outline as shown in the screenshot
            Point2D.Double p1 = geoToScreen(11.8, 72.0);
            Point2D.Double p2 = geoToScreen(11.5, 73.2);
            Point2D.Double p3 = geoToScreen(9.2, 73.7);
            Point2D.Double p4 = geoToScreen(8.8, 72.8);

            Path2D lakZone = new Path2D.Double();
            lakZone.moveTo(p1.x, p1.y);
            lakZone.lineTo(p2.x, p2.y);
            lakZone.lineTo(p3.x, p3.y);
            lakZone.lineTo(p4.x, p4.y);
            lakZone.closePath();

            g2.setColor(new Color(160, 195, 210, 100));
            g2.fill(lakZone);
            g2.setColor(new Color(130, 170, 190));
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(lakZone);

            // Label "Lakshadweep"
            Point2D.Double lakCenter = geoToScreen(10.5, 72.6);
            g2.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            g2.setColor(new Color(100, 145, 165));
            g2.drawString("Lakshadweep", (float) lakCenter.x - 36, (float) lakCenter.y);
        }

        private void drawLandmassAndCoast(Graphics2D g2) {
            // Authentic coast coordinates running south-southeast from Mangaluru to Kanyakumari
            double[][] coast = {
                    {13.2, 74.7},
                    {12.91, 74.85}, // Mangaluru
                    {12.51, 74.98}, // Kasaragod
                    {12.20, 75.12},
                    {11.87, 75.37}, // Kannur
                    {11.75, 75.48},
                    {11.25, 75.78}, // Kozhikode
                    {10.92, 75.92}, // Ponnani
                    {10.52, 76.08},
                    {10.15, 76.18},
                    {9.93, 76.26},  // Kochi
                    {9.49, 76.33},  // Alappuzha
                    {9.02, 76.53},
                    {8.89, 76.61},  // Kollam
                    {8.52, 76.93},  // Thiruvananthapuram
                    {8.28, 77.10},
                    {8.18, 77.41},  // Nagercoil / Kanyakumari
                    {8.08, 77.55},
                    // Eastward & inland boundary enclosing the eastern peninsula
                    {8.00, 78.50},
                    {13.2, 78.50}
            };

            Path2D land = new Path2D.Double();
            Point2D.Double start = geoToScreen(coast[0][0], coast[0][1]);
            land.moveTo(start.x, start.y);

            for (int i = 1; i < coast.length; i++) {
                Point2D.Double pt = geoToScreen(coast[i][0], coast[i][1]);
                land.lineTo(pt.x, pt.y);
            }
            land.closePath();

            // Land base color (Soft terrain #EEF3E2)
            g2.setColor(new Color(238, 243, 226));
            g2.fill(land);

            // Western Ghats mountain ridge shading along east Kerala border
            drawWesternGhats(g2);

            // Coastline stroke
            g2.setColor(new Color(145, 178, 195));
            g2.setStroke(new BasicStroke(1.8f));
            for (int i = 0; i < coast.length - 3; i++) {
                Point2D.Double pA = geoToScreen(coast[i][0], coast[i][1]);
                Point2D.Double pB = geoToScreen(coast[i + 1][0], coast[i + 1][1]);
                g2.draw(new Line2D.Double(pA, pB));
            }
        }

        private void drawWesternGhats(Graphics2D g2) {
            // Hill terrain belt (#D6E8C7)
            double[][] hills = {
                    {12.2, 75.6},
                    {11.7, 76.0},
                    {11.3, 76.3},
                    {10.8, 76.8}, // Palakkad gap bypass
                    {10.2, 77.0},
                    {9.8, 77.2},  // Munnar / Idukki
                    {9.3, 77.3},
                    {8.7, 77.2},
                    {8.3, 77.4},
                    {8.3, 77.8},
                    {9.8, 77.8},
                    {12.2, 77.8}
            };

            Path2D ridge = new Path2D.Double();
            Point2D.Double s = geoToScreen(hills[0][0], hills[0][1]);
            ridge.moveTo(s.x, s.y);
            for (int i = 1; i < hills.length; i++) {
                Point2D.Double pt = geoToScreen(hills[i][0], hills[i][1]);
                ridge.lineTo(pt.x, pt.y);
            }
            ridge.closePath();

            g2.setColor(new Color(214, 232, 199, 160));
            g2.fill(ridge);
        }

        private void drawLakes(Graphics2D g2) {
            // Vembanad Lake at Kochi/Alappuzha
            Point2D.Double v1 = geoToScreen(9.88, 76.32);
            Point2D.Double v2 = geoToScreen(9.56, 76.38);
            Point2D.Double v3 = geoToScreen(9.62, 76.42);

            Path2D vembanad = new Path2D.Double();
            vembanad.moveTo(v1.x, v1.y);
            vembanad.lineTo(v2.x, v2.y);
            vembanad.lineTo(v3.x, v3.y);
            vembanad.closePath();

            g2.setColor(new Color(175, 214, 228));
            g2.fill(vembanad);
        }

        private void drawCities(Graphics2D g2) {
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            for (CityLabel city : cities) {
                Point2D.Double pt = geoToScreen(city.lat, city.lon);
                // Draw small city anchor dot
                g2.setColor(new Color(130, 145, 150));
                g2.fill(new Ellipse2D.Double(pt.x - 2, pt.y - 2, 4, 4));

                // City Name
                g2.setColor(new Color(75, 85, 95));
                g2.drawString(city.name, (float) pt.x + 5, (float) pt.y + 4);
            }
        }

        private void drawGpsBeacon(Graphics2D g2) {
            // Centered on User GPS Location (Kochi - 9.9312, 76.2673)
            Point2D.Double userPt = geoToScreen(9.9312, 76.2673);

            // Pulsing translucent GPS beacon ring
            g2.setColor(new Color(37, 99, 235, 60));
            g2.fill(new Ellipse2D.Double(userPt.x - pulseRadius, userPt.y - pulseRadius, pulseRadius * 2, pulseRadius * 2));
            g2.setColor(new Color(37, 99, 235, 180));
            g2.setStroke(new BasicStroke(1.4f));
            g2.draw(new Ellipse2D.Double(userPt.x - pulseRadius, userPt.y - pulseRadius, pulseRadius * 2, pulseRadius * 2));

            // Solid blue position dot with white ring
            g2.setColor(Color.WHITE);
            g2.fill(new Ellipse2D.Double(userPt.x - 6, userPt.y - 6, 12, 12));
            g2.setColor(new Color(37, 99, 235));
            g2.fill(new Ellipse2D.Double(userPt.x - 4, userPt.y - 4, 8, 8));
        }

        private void drawPins(Graphics2D g2) {
            for (MapPin pin : pins) {
                if (!matchesFilter(pin)) continue;

                Point2D.Double pt = geoToScreen(pin.lat, pin.lon);
                boolean isHover = (pin == hoveredPin || pin == selectedPin);
                int r = isHover ? 16 : 14;

                // Pin Shadow
                g2.setColor(new Color(0, 0, 0, 40));
                g2.fill(new Ellipse2D.Double(pt.x - r + 1, pt.y - r + 2, r * 2, r * 2));

                // Active Halo Ring if hovered/selected
                if (isHover) {
                    g2.setColor(new Color(pin.pinColor.getRed(), pin.pinColor.getGreen(), pin.pinColor.getBlue(), 70));
                    g2.fill(new Ellipse2D.Double(pt.x - r - 5, pt.y - r - 5, (r + 5) * 2, (r + 5) * 2));
                }

                // Main Circular Pin Body
                g2.setColor(pin.pinColor);
                g2.fill(new Ellipse2D.Double(pt.x - r, pt.y - r, r * 2, r * 2));

                // White Border Ring
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2.0f));
                g2.draw(new Ellipse2D.Double(pt.x - r, pt.y - r, r * 2, r * 2));

                // Draw Internal Glyph based on iconType
                drawPinGlyph(g2, pt.x, pt.y, pin.iconType);
            }
        }

        private void drawPinGlyph(Graphics2D g2, double cx, double cy, String iconType) {
            g2.setColor(Color.WHITE);
            if ("TRIANGLE".equals(iconType)) {
                // Warning Triangle ▲
                Path2D tri = new Path2D.Double();
                tri.moveTo(cx, cy - 6);
                tri.lineTo(cx + 6, cy + 5);
                tri.lineTo(cx - 6, cy + 5);
                tri.closePath();
                g2.fill(tri);
            } else if ("CROSS".equals(iconType)) {
                // Medical Plus Cross +
                g2.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine((int) cx - 5, (int) cy, (int) cx + 5, (int) cy);
                g2.drawLine((int) cx, (int) cy - 5, (int) cx, (int) cy + 5);
            } else if ("MEGAPHONE".equals(iconType)) {
                // Speaker cone
                Path2D cone = new Path2D.Double();
                cone.moveTo(cx - 4, cy - 3);
                cone.lineTo(cx + 3, cy - 6);
                cone.lineTo(cx + 3, cy + 6);
                cone.lineTo(cx - 4, cy + 3);
                cone.closePath();
                g2.fill(cone);
            } else {
                // Depot / Shelter Box
                g2.fillRect((int) cx - 4, (int) cy - 4, 8, 8);
                g2.setColor(new Color(30, 64, 175));
                g2.drawLine((int) cx - 4, (int) cy, (int) cx + 4, (int) cy);
            }
        }

        private void drawPinPopup(Graphics2D g2, MapPin pin) {
            Point2D.Double pt = geoToScreen(pin.lat, pin.lon);

            int popW = 280;
            int popH = 100;
            int popX = (int) pt.x - popW / 2;
            int popY = (int) pt.y - popH - 24;

            // Constrain popup inside map viewport
            if (popX < 14) popX = 14;
            if (popX + popW > getWidth() - 14) popX = getWidth() - popW - 14;
            if (popY < 14) popY = (int) pt.y + 24;

            // Card Shadow
            g2.setColor(new Color(0, 0, 0, 50));
            g2.fill(new RoundRectangle2D.Float(popX + 2, popY + 3, popW, popH, 10, 10));

            // Card Background
            g2.setColor(Color.WHITE);
            g2.fill(new RoundRectangle2D.Float(popX, popY, popW, popH, 10, 10));
            g2.setColor(UITheme.BORDER);
            g2.setStroke(new BasicStroke(1.0f));
            g2.draw(new RoundRectangle2D.Float(popX, popY, popW, popH, 10, 10));

            // Top Status Strip
            g2.setColor(pin.pinColor);
            g2.fill(new RoundRectangle2D.Float(popX, popY, popW, 4, 10, 10));

            // Pin Title
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.setColor(UITheme.TEXT);
            g2.drawString(pin.title, popX + 12, popY + 22);

            // Status Badge
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g2.setColor(pin.pinColor);
            g2.drawString(pin.status, popX + 12, popY + 38);

            // Details Description
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g2.setColor(UITheme.SECONDARY_TEXT);
            String det = pin.details;
            if (det.length() > 46) det = det.substring(0, 44) + "...";
            g2.drawString(det, popX + 12, popY + 56);

            // Coordinates Readout
            g2.setFont(UITheme.CODE_FONT);
            g2.setColor(UITheme.MUTED);
            g2.drawString("GPS: " + pin.lat + "° N, " + pin.lon + "° E", popX + 12, popY + 74);

            // Action Prompt
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            g2.setColor(UITheme.BRAND_CRIMSON);
            g2.drawString("CLICK TO SELECT • REAL-TIME SYNCED", popX + 12, popY + 90);
        }

        private void drawFloatingControls(Graphics2D g2) {
            int w = getWidth();

            // 1. Top-Right "Locate My Position" Pill Button (Matching user screenshot)
            int btnW = 160;
            int btnH = 36;
            int btnX = w - btnW - 18;
            int btnY = 16;

            // Shadow
            g2.setColor(new Color(0, 0, 0, 30));
            g2.fill(new RoundRectangle2D.Float(btnX + 1, btnY + 2, btnW, btnH, 8, 8));

            // Pill Background
            g2.setColor(Color.WHITE);
            g2.fill(new RoundRectangle2D.Float(btnX, btnY, btnW, btnH, 8, 8));
            g2.setColor(new Color(219, 234, 254)); // Soft light blue border
            g2.draw(new RoundRectangle2D.Float(btnX, btnY, btnW, btnH, 8, 8));

            // Target Crosshair Icon
            g2.setColor(new Color(37, 99, 235));
            g2.setStroke(new BasicStroke(1.8f));
            int icX = btnX + 18;
            int icY = btnY + 18;
            g2.drawOval(icX - 6, icY - 6, 12, 12);
            g2.drawLine(icX - 9, icY, icX - 6, icY);
            g2.drawLine(icX + 6, icY, icX + 9, icY);
            g2.drawLine(icX, icY - 9, icX, icY - 6);
            g2.drawLine(icX, icY + 6, icX, icY + 9);

            // Button Text
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.drawString("Locate My Position", btnX + 34, btnY + 23);

            // 2. Bottom-Right Zoom In / Out Buttons ([+] and [-])
            int zX = w - 44;
            int zY = getHeight() - 95;

            // Zoom In [+]
            g2.setColor(Color.WHITE);
            g2.fill(new RoundRectangle2D.Float(zX, zY, 28, 28, 6, 6));
            g2.setColor(UITheme.BORDER);
            g2.draw(new RoundRectangle2D.Float(zX, zY, 28, 28, 6, 6));
            g2.setColor(UITheme.TEXT);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
            g2.drawString("+", zX + 8, zY + 19);

            // Zoom Out [-]
            int zoY = zY + 32;
            g2.setColor(Color.WHITE);
            g2.fill(new RoundRectangle2D.Float(zX, zoY, 28, 28, 6, 6));
            g2.setColor(UITheme.BORDER);
            g2.draw(new RoundRectangle2D.Float(zX, zoY, 28, 28, 6, 6));
            g2.setColor(UITheme.TEXT);
            g2.drawString("−", zX + 8, zoY + 19);
        }

        private void drawToastBanner(Graphics2D g2, int w) {
            int tW = 480;
            int tH = 34;
            int tX = (w - tW) / 2;
            int tY = 16;
            g2.setColor(new Color(15, 23, 42, 225));
            g2.fill(new RoundRectangle2D.Float(tX, tY, tW, tH, 17, 17));
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.drawString(toastText, tX + 16, tY + 22);
        }

        private void drawAttributionBar(Graphics2D g2, int w, int h) {
            int barH = 22;
            int barY = h - barH;

            g2.setColor(new Color(255, 255, 255, 220));
            g2.fillRect(0, barY, w, barH);
            g2.setColor(new Color(226, 232, 240));
            g2.drawLine(0, barY, w, barY);

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g2.setColor(new Color(100, 116, 139));
            g2.drawString("Leaflet | © OpenStreetMap contributors | Kerala State Disaster Management Cell", 12, barY + 15);
        }
    }

    private static class CityLabel {
        String name;
        double lat, lon;

        public CityLabel(String name, double lat, double lon) {
            this.name = name;
            this.lat = lat;
            this.lon = lon;
        }
    }
}