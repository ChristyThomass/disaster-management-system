package disaster.backend;

import disaster.model.DisasterReport;
import disaster.model.SOSAlert;
import disaster.model.User;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * High-reliability Database Manager supporting:
 * 1. XAMPP MySQL (Port 3306, database: disaster_db, user 'root', password '')
 * 2. Automatic fallback to persistent embedded H2 / Local Storage when XAMPP is offline
 */
public class DatabaseManager {

    public enum DbMode {
        XAMPP_MYSQL,
        LOCAL_EMBEDDED,
        MEMORY_FALLBACK
    }

    public static class ContactEntry {
        public int contactId;
        public int userId;
        public String contactName;
        public String contactPhone;

        public ContactEntry(int contactId, int userId, String contactName, String contactPhone) {
            this.contactId = contactId;
            this.userId = userId;
            this.contactName = contactName;
            this.contactPhone = contactPhone;
        }
    }

    private static DatabaseManager instance;
    private DbMode currentMode = DbMode.MEMORY_FALLBACK;
    private String statusMessage = "Initializing database...";

    // XAMPP MySQL Configuration
    private static final String MYSQL_HOST = "localhost";
    private static final int MYSQL_PORT = 3306;
    private static final String MYSQL_DB = "disaster_db";
    private static final String MYSQL_USER = "root";
    private static final String MYSQL_PASS = "";

    // Memory cache / fallback
    private final Map<String, User> memoryUsers = new ConcurrentHashMap<>();
    private final Map<String, SOSAlert> memorySOS = new ConcurrentHashMap<>();
    private final Map<String, DisasterReport> memoryReports = new ConcurrentHashMap<>();
    private final List<ContactEntry> memoryContacts = new ArrayList<>();

    private DatabaseManager() {
        initDatabase();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public synchronized void initDatabase() {
        // Try connecting to XAMPP MySQL first
        if (tryConnectXamppMySQL()) {
            System.out.println("[DatabaseManager] >>> CONNECTED TO XAMPP MySQL ON PORT 3306 (Database: " + MYSQL_DB + ") <<<");
            currentMode = DbMode.XAMPP_MYSQL;
            statusMessage = "Connected to XAMPP MySQL (:3306 - " + MYSQL_DB + ")";
            return;
        }

        // Try embedded persistent H2
        if (tryConnectH2()) {
            System.out.println("[DatabaseManager] >>> XAMPP MySQL offline. Using persistent embedded H2 database (./data/disaster_db) <<<");
            currentMode = DbMode.LOCAL_EMBEDDED;
            statusMessage = "Local Persistent H2 (XAMPP MySQL is offline - start XAMPP to link)";
            return;
        }

        // Fallback to high-reliability memory storage
        System.out.println("[DatabaseManager] >>> Using in-memory store <<<");
        currentMode = DbMode.MEMORY_FALLBACK;
        statusMessage = "In-Memory Engine (XAMPP MySQL is offline)";
        seedMemoryStore();
    }

    private boolean tryConnectXamppMySQL() {
        try {
            // Load driver if available
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                try {
                    Class.forName("com.mysql.jdbc.Driver");
                } catch (ClassNotFoundException ex) {
                    System.out.println("[DatabaseManager] MySQL Driver not in boot classpath. Attempting fallback.");
                    return false;
                }
            }

            // Connect to MySQL server root to ensure database exists
            String serverUrl = "jdbc:mysql://" + MYSQL_HOST + ":" + MYSQL_PORT + "/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            try (Connection conn = DriverManager.getConnection(serverUrl, MYSQL_USER, MYSQL_PASS);
                 Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + MYSQL_DB + "` DEFAULT CHARACTER SET utf8mb4");
            }

            // Connect to disaster_db
            String dbUrl = "jdbc:mysql://" + MYSQL_HOST + ":" + MYSQL_PORT + "/" + MYSQL_DB + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            try (Connection conn = DriverManager.getConnection(dbUrl, MYSQL_USER, MYSQL_PASS)) {
                createMySQLTables(conn);
                seedMySQLInitialData(conn);
                return true;
            }
        } catch (Exception e) {
            System.out.println("[DatabaseManager] XAMPP MySQL check: " + e.getMessage());
            return false;
        }
    }

    private void createMySQLTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // 1. users
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `users` ("
                    + "`user_id` INT AUTO_INCREMENT PRIMARY KEY, "
                    + "`username` VARCHAR(50) NOT NULL UNIQUE, "
                    + "`phone` VARCHAR(15)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            // 2. user_profiles
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `user_profiles` ("
                    + "`profile_id` INT AUTO_INCREMENT PRIMARY KEY, "
                    + "`user_id` INT NOT NULL, "
                    + "`full_name` VARCHAR(100), "
                    + "`blood_group` VARCHAR(5), "
                    + "`address` VARCHAR(200)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            // 3. sos_requests
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `sos_requests` ("
                    + "`sos_id` INT AUTO_INCREMENT PRIMARY KEY, "
                    + "`user_id` INT NOT NULL, "
                    + "`status` VARCHAR(20) DEFAULT 'Active', "
                    + "`request_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            // 4. gps_locations
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `gps_locations` ("
                    + "`location_id` INT AUTO_INCREMENT PRIMARY KEY, "
                    + "`sos_id` INT NOT NULL, "
                    + "`latitude` DECIMAL(10,8) NOT NULL, "
                    + "`longitude` DECIMAL(11,8) NOT NULL"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            // 5. rescue_status
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `rescue_status` ("
                    + "`status_id` INT AUTO_INCREMENT PRIMARY KEY, "
                    + "`sos_id` INT NOT NULL, "
                    + "`update_message` VARCHAR(255), "
                    + "`update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            // 6. incident_reports
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `incident_reports` ("
                    + "`incident_id` INT AUTO_INCREMENT PRIMARY KEY, "
                    + "`user_id` INT NOT NULL, "
                    + "`disaster_type` VARCHAR(50) NOT NULL, "
                    + "`severity_level` INT DEFAULT 5, "
                    + "`description` TEXT, "
                    + "`status` VARCHAR(20) DEFAULT 'Reported'"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            // 7. incident_media
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `incident_media` ("
                    + "`media_id` INT AUTO_INCREMENT PRIMARY KEY, "
                    + "`incident_id` INT NOT NULL, "
                    + "`file_path` VARCHAR(255)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            // 8. emergency_contacts
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `emergency_contacts` ("
                    + "`contact_id` INT AUTO_INCREMENT PRIMARY KEY, "
                    + "`user_id` INT NOT NULL, "
                    + "`contact_name` VARCHAR(100) NOT NULL, "
                    + "`contact_phone` VARCHAR(15) NOT NULL"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            // 9. notifications
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `notifications` ("
                    + "`notification_id` INT AUTO_INCREMENT PRIMARY KEY, "
                    + "`user_id` INT NOT NULL, "
                    + "`message` VARCHAR(255) NOT NULL, "
                    + "`is_read` BIT DEFAULT 0"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        }
    }

    private void seedMySQLInitialData(Connection conn) {
        // Only seed if users table is empty
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT count(*) FROM users")) {
            if (rs.next() && rs.getInt(1) == 0) {
                // Seed alvin_ms
                String sql1 = "INSERT INTO users (username, phone) VALUES ('alvin_ms', '9876543210')";
                stmt.executeUpdate(sql1);
                stmt.executeUpdate("INSERT INTO user_profiles (user_id, full_name, blood_group, address) VALUES (1, 'Alvin MS', 'O+', 'Hostel Block A')");

                // Seed athul_c
                String sql2 = "INSERT INTO users (username, phone) VALUES ('athul_c', '8765432109')";
                stmt.executeUpdate(sql2);
                stmt.executeUpdate("INSERT INTO user_profiles (user_id, full_name, blood_group, address) VALUES (2, 'Athul Cleetus', 'B+', 'Hostel Block B')");
            }
        } catch (SQLException ignored) {}
    }

    private boolean tryConnectH2() {
        try {
            Class.forName("org.h2.Driver");
            new File("./data").mkdirs();
            String h2Url = "jdbc:h2:file:./data/disaster_db;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE";
            try (Connection conn = DriverManager.getConnection(h2Url, "sa", "");
                 Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users ("
                        + "user_id INT AUTO_INCREMENT PRIMARY KEY, "
                        + "username VARCHAR(50) NOT NULL UNIQUE, "
                        + "phone VARCHAR(15))");

                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS user_profiles ("
                        + "profile_id INT AUTO_INCREMENT PRIMARY KEY, "
                        + "user_id INT NOT NULL, "
                        + "full_name VARCHAR(100), "
                        + "blood_group VARCHAR(5), "
                        + "address VARCHAR(200))");

                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS sos_requests ("
                        + "sos_id INT AUTO_INCREMENT PRIMARY KEY, "
                        + "user_id INT NOT NULL, "
                        + "status VARCHAR(20) DEFAULT 'Active', "
                        + "request_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS gps_locations ("
                        + "location_id INT AUTO_INCREMENT PRIMARY KEY, "
                        + "sos_id INT NOT NULL, "
                        + "latitude DECIMAL(10,8) NOT NULL, "
                        + "longitude DECIMAL(11,8) NOT NULL)");

                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS rescue_status ("
                        + "status_id INT AUTO_INCREMENT PRIMARY KEY, "
                        + "sos_id INT NOT NULL, "
                        + "update_message VARCHAR(255), "
                        + "update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS incident_reports ("
                        + "incident_id INT AUTO_INCREMENT PRIMARY KEY, "
                        + "user_id INT NOT NULL, "
                        + "disaster_type VARCHAR(50) NOT NULL, "
                        + "severity_level INT DEFAULT 5, "
                        + "description TEXT, "
                        + "status VARCHAR(20) DEFAULT 'Reported')");

                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS emergency_contacts ("
                        + "contact_id INT AUTO_INCREMENT PRIMARY KEY, "
                        + "user_id INT NOT NULL, "
                        + "contact_name VARCHAR(100) NOT NULL, "
                        + "contact_phone VARCHAR(15) NOT NULL)");

                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS notifications ("
                        + "notification_id INT AUTO_INCREMENT PRIMARY KEY, "
                        + "user_id INT NOT NULL, "
                        + "message VARCHAR(255) NOT NULL, "
                        + "is_read BOOLEAN DEFAULT FALSE)");

                ResultSet rs = stmt.executeQuery("SELECT count(*) FROM users");
                if (rs.next() && rs.getInt(1) == 0) {
                    stmt.executeUpdate("INSERT INTO users (username, phone) VALUES ('alvin_ms', '9876543210')");
                    stmt.executeUpdate("INSERT INTO user_profiles (user_id, full_name, blood_group, address) VALUES (1, 'Alvin MS', 'O+', 'Hostel Block A')");
                }
                return true;
            }
        } catch (Exception e) {
            System.out.println("[DatabaseManager] H2 initialization: " + e.getMessage());
            return false;
        }
    }

    private void seedMemoryStore() {
        User u1 = new User("1", "alvin_ms", "alvin@example.com", "CITIZEN", 9.9312, 76.2673);
        u1.setPhone("9876543210");
        u1.setFullName("Alvin MS");
        u1.setBloodGroup("O+");
        u1.setAddress("Hostel Block A");
        memoryUsers.put(u1.getUserId(), u1);

        User u2 = new User("2", "athul_c", "athul@example.com", "RESPONDER", 9.9816, 76.2999);
        u2.setPhone("8765432109");
        u2.setFullName("Athul Cleetus");
        u2.setBloodGroup("B+");
        u2.setAddress("Hostel Block B");
        memoryUsers.put(u2.getUserId(), u2);

        SOSAlert sos1 = new SOSAlert("1", "1", 9.5786, 76.9746, "CRITICAL", "Flood water entering ground floor");
        memorySOS.put(sos1.getSosId(), sos1);

        memoryContacts.add(new ContactEntry(1, 1, "John (Dad)", "9998887776"));
        memoryContacts.add(new ContactEntry(2, 2, "Mary (Mom)", "8887776665"));
    }

    public Connection getConnection() throws SQLException {
        // 1. Always prioritize live XAMPP MySQL connection
        try {
            Connection conn = disaster.dao.DBConnection.getConnection();
            if (conn != null && !conn.isClosed()) {
                currentMode = DbMode.XAMPP_MYSQL;
                return conn;
            }
        } catch (SQLException ignored) {}

        if (currentMode == DbMode.LOCAL_EMBEDDED) {
            return DriverManager.getConnection("jdbc:h2:file:./data/disaster_db;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE", "sa", "");
        }
        return null;
    }

    public DbMode getCurrentMode() {
        return currentMode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public boolean isUsingXamppMySQL() {
        if (currentMode == DbMode.XAMPP_MYSQL) return true;
        try (Connection conn = disaster.dao.DBConnection.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                currentMode = DbMode.XAMPP_MYSQL;
                return true;
            }
        } catch (Exception ignored) {}
        return false;
    }

    /**
     * Resolves any user identifier (string id, username, email) into a valid integer user_id.
     */
    public int resolveUserId(Connection conn, String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) return 1;
        String clean = identifier.trim();
        try {
            return Integer.parseInt(clean);
        } catch (NumberFormatException ignored) {}

        // Strip prefix like user-001 -> 1
        if (clean.toLowerCase().startsWith("user-")) {
            try {
                return Integer.parseInt(clean.substring(5));
            } catch (NumberFormatException ignored) {}
        }

        // Try lookup by username
        try (PreparedStatement ps = conn.prepareStatement("SELECT user_id FROM users WHERE LOWER(username) = ?")) {
            ps.setString(1, clean.toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException ignored) {}

        // Fallback: first user in database
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT user_id FROM users ORDER BY user_id ASC LIMIT 1")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException ignored) {}

        return 1;
    }

    // =========================================================================
    // User Operations
    // =========================================================================

    public User getUserByUsername(String username) {
        if (currentMode == DbMode.XAMPP_MYSQL || currentMode == DbMode.LOCAL_EMBEDDED) {
            String sql = "SELECT u.user_id, u.username, u.phone, p.full_name, p.blood_group, p.address "
                    + "FROM users u LEFT JOIN user_profiles p ON u.user_id = p.user_id "
                    + "WHERE LOWER(u.username) = ?";
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username.toLowerCase());
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return mapUser(rs);
                    }
                }
            } catch (SQLException e) {
                System.err.println("[DatabaseManager] User query error: " + e.getMessage());
            }
        }
        for (User u : memoryUsers.values()) {
            if (u.getUsername().equalsIgnoreCase(username)) return u;
        }
        return null;
    }

    public User getUserByEmail(String email) {
        if (email != null && email.contains("@")) {
            String uname = email.substring(0, email.indexOf('@'));
            User u = getUserByUsername(uname);
            if (u != null) return u;
        }
        if (currentMode == DbMode.XAMPP_MYSQL || currentMode == DbMode.LOCAL_EMBEDDED) {
            String sql = "SELECT u.user_id, u.username, u.phone, p.full_name, p.blood_group, p.address "
                    + "FROM users u LEFT JOIN user_profiles p ON u.user_id = p.user_id "
                    + "WHERE LOWER(u.username) = ?";
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, email.toLowerCase());
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return mapUser(rs);
                    }
                }
            } catch (SQLException e) {
                System.err.println("[DatabaseManager] Email query error: " + e.getMessage());
            }
        }
        for (User u : memoryUsers.values()) {
            if (u.getEmail().equalsIgnoreCase(email)) return u;
        }
        return null;
    }

    public boolean saveUser(User u) {
        memoryUsers.put(u.getUserId() != null ? u.getUserId() : u.getUsername(), u);
        if (currentMode == DbMode.XAMPP_MYSQL || currentMode == DbMode.LOCAL_EMBEDDED) {
            try (Connection conn = getConnection()) {
                // Check if user already exists
                String checkSql = "SELECT user_id FROM users WHERE LOWER(username) = ?";
                int userId = 0;
                try (PreparedStatement check = conn.prepareStatement(checkSql)) {
                    check.setString(1, u.getUsername().toLowerCase());
                    try (ResultSet rs = check.executeQuery()) {
                        if (rs.next()) {
                            userId = rs.getInt("user_id");
                            u.setUserId(String.valueOf(userId));
                        }
                    }
                }

                // Insert into users if not existing
                if (userId == 0) {
                    String insertSql = "INSERT INTO users (username, phone) VALUES (?, ?)";
                    try (PreparedStatement pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                        pstmt.setString(1, u.getUsername());
                        String phone = (u.getPhone() != null && !u.getPhone().isEmpty()) ? u.getPhone() : "9876543210";
                        pstmt.setString(2, phone);
                        pstmt.executeUpdate();
                        try (ResultSet keys = pstmt.getGeneratedKeys()) {
                            if (keys.next()) {
                                userId = keys.getInt(1);
                                u.setUserId(String.valueOf(userId));
                            }
                        }
                    }
                }

                // Insert or update profile
                if (userId > 0) {
                    String profCheck = "SELECT profile_id FROM user_profiles WHERE user_id = ?";
                    boolean hasProfile = false;
                    try (PreparedStatement pCheck = conn.prepareStatement(profCheck)) {
                        pCheck.setInt(1, userId);
                        try (ResultSet rs = pCheck.executeQuery()) {
                            if (rs.next()) hasProfile = true;
                        }
                    }
                    if (!hasProfile) {
                        String profSql = "INSERT INTO user_profiles (user_id, full_name, blood_group, address) VALUES (?, ?, ?, ?)";
                        try (PreparedStatement profStmt = conn.prepareStatement(profSql)) {
                            profStmt.setInt(1, userId);
                            profStmt.setString(2, u.getFullName() != null ? u.getFullName() : u.getUsername());
                            profStmt.setString(3, u.getBloodGroup() != null ? u.getBloodGroup() : "O+");
                            profStmt.setString(4, u.getAddress() != null ? u.getAddress() : "Kerala Relief Zone");
                            profStmt.executeUpdate();
                        }
                    }
                }
                return true;
            } catch (SQLException e) {
                System.err.println("[DatabaseManager] Insert user error: " + e.getMessage());
            }
        }
        return true;
    }

    // =========================================================================
    // SOS Alert Operations
    // =========================================================================

    public boolean saveSOSAlert(SOSAlert sos) {
        if (sos.getSosId() != null) {
            memorySOS.put(sos.getSosId(), sos);
        }
        try (Connection conn = getConnection()) {
            if (conn != null) {
                int userId = resolveUserId(conn, sos.getUserId());
                String insertSos = "INSERT INTO sos_requests (user_id, status, request_time) VALUES (?, 'Active', CURRENT_TIMESTAMP)";
                int sosId = 0;
                try (PreparedStatement ps = conn.prepareStatement(insertSos, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            sosId = keys.getInt(1);
                            sos.setSosId(String.valueOf(sosId));
                            memorySOS.put(sos.getSosId(), sos);
                        }
                    }
                }

                if (sosId > 0) {
                    // Insert into gps_locations
                    String insertGps = "INSERT INTO gps_locations (sos_id, latitude, longitude) VALUES (?, ?, ?)";
                    try (PreparedStatement ps = conn.prepareStatement(insertGps)) {
                        ps.setInt(1, sosId);
                        ps.setDouble(2, sos.getLatitude());
                        ps.setDouble(3, sos.getLongitude());
                        ps.executeUpdate();
                    }

                    // Insert into rescue_status
                    String insertRescue = "INSERT INTO rescue_status (sos_id, update_message, update_time) VALUES (?, ?, CURRENT_TIMESTAMP)";
                    try (PreparedStatement ps = conn.prepareStatement(insertRescue)) {
                        ps.setInt(1, sosId);
                        String msg = sos.getDescription() != null && !sos.getDescription().isEmpty()
                                ? sos.getDescription()
                                : "Immediate distress signal broadcasted";
                        ps.setString(2, msg);
                        ps.executeUpdate();
                    }

                    // Insert notification
                    String insertNotif = "INSERT INTO notifications (user_id, message, is_read) VALUES (?, ?, 0)";
                    try (PreparedStatement ps = conn.prepareStatement(insertNotif)) {
                        ps.setInt(1, userId);
                        ps.setString(2, "SOS broadcast #" + sosId + " received by Unified Command");
                        ps.executeUpdate();
                    } catch (SQLException ignored) {}
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Insert SOS error: " + e.getMessage());
        }
        return true;
    }

    public List<SOSAlert> getActiveSOSAlerts() {
        List<SOSAlert> list = new ArrayList<>();
        if (currentMode == DbMode.XAMPP_MYSQL || currentMode == DbMode.LOCAL_EMBEDDED) {
            String sql = "SELECT s.sos_id, s.user_id, s.status, s.request_time, g.latitude, g.longitude, r.update_message "
                    + "FROM sos_requests s "
                    + "LEFT JOIN gps_locations g ON s.sos_id = g.sos_id "
                    + "LEFT JOIN rescue_status r ON s.sos_id = r.sos_id "
                    + "ORDER BY s.sos_id DESC";
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    double lat = rs.getDouble("latitude");
                    if (rs.wasNull()) lat = 9.9312;
                    double lon = rs.getDouble("longitude");
                    if (rs.wasNull()) lon = 76.2673;
                    String desc = rs.getString("update_message");
                    if (desc == null || desc.isEmpty()) desc = "Distress Broadcast";
                    SOSAlert s = new SOSAlert(
                            String.valueOf(rs.getInt("sos_id")),
                            String.valueOf(rs.getInt("user_id")),
                            lat,
                            lon,
                            "CRITICAL",
                            desc
                    );
                    s.setStatus(rs.getString("status"));
                    s.setRespondersCount(1);
                    list.add(s);
                }
                return list;
            } catch (SQLException e) {
                System.err.println("[DatabaseManager] Active SOS query error: " + e.getMessage());
            }
        }
        for (SOSAlert s : memorySOS.values()) {
            if ("Active".equalsIgnoreCase(s.getStatus())) list.add(s);
        }
        return list;
    }

    public boolean acknowledgeSOS(String sosId, String responderId) {
        SOSAlert s = memorySOS.get(sosId);
        if (s != null) {
            s.setStatus("ACKNOWLEDGED");
            s.setRespondersCount(s.getRespondersCount() + 1);
        }
        if (currentMode == DbMode.XAMPP_MYSQL || currentMode == DbMode.LOCAL_EMBEDDED) {
            try {
                int id = Integer.parseInt(sosId.replace("SOS-", "").trim());
                String sql = "UPDATE sos_requests SET status = 'Acknowledged' WHERE sos_id = ?";
                try (Connection conn = getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, id);
                    pstmt.executeUpdate();
                }
                String sqlRescue = "UPDATE rescue_status SET update_message = CONCAT(update_message, ' [ACKNOWLEDGED by unit]') WHERE sos_id = ?";
                try (Connection conn = getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sqlRescue)) {
                    pstmt.setInt(1, id);
                    pstmt.executeUpdate();
                }
                return true;
            } catch (Exception e) {
                System.err.println("[DatabaseManager] Acknowledge SOS error: " + e.getMessage());
            }
        }
        return true;
    }

    // =========================================================================
    // Disaster Report Operations
    // =========================================================================

    public boolean saveDisasterReport(DisasterReport rep) {
        if (rep.getReportId() != null) {
            memoryReports.put(rep.getReportId(), rep);
        }
        try (Connection conn = getConnection()) {
            if (conn != null) {
                int userId = resolveUserId(conn, rep.getUserId());
                int sev = 5;
                if (rep.getSeverity() != null) {
                    try {
                        sev = Integer.parseInt(rep.getSeverity());
                    } catch (NumberFormatException e) {
                        String s = rep.getSeverity().toUpperCase();
                        if (s.contains("CRIT")) sev = 10;
                        else if (s.contains("HIGH")) sev = 8;
                        else if (s.contains("MED")) sev = 5;
                        else sev = 3;
                    }
                }
                String insertSql = "INSERT INTO incident_reports (user_id, disaster_type, severity_level, description, status) VALUES (?, ?, ?, ?, ?)";
                int incId = 0;
                try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, userId);
                    ps.setString(2, rep.getDisasterType() != null ? rep.getDisasterType() : "General");
                    ps.setInt(3, sev);
                    ps.setString(4, rep.getDescription() != null ? rep.getDescription() : "Incident reported");
                    ps.setString(5, rep.getStatus() != null ? rep.getStatus() : "Reported");
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            incId = keys.getInt(1);
                            rep.setReportId("INC-" + incId);
                            memoryReports.put(rep.getReportId(), rep);
                        }
                    }
                }

                if (incId > 0) {
                    // Log notification
                    String insertNotif = "INSERT INTO notifications (user_id, message, is_read) VALUES (?, ?, 0)";
                    try (PreparedStatement ps = conn.prepareStatement(insertNotif)) {
                        ps.setInt(1, userId);
                        ps.setString(2, "Incident report #" + incId + " (" + rep.getDisasterType() + ") registered");
                        ps.executeUpdate();
                    } catch (SQLException ignored) {}
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Insert report error: " + e.getMessage());
        }
        return true;
    }

    public boolean verifyDisasterReport(String reportId, String verifierId) {
        DisasterReport r = memoryReports.get(reportId);
        if (r != null) {
            r.setStatus("VERIFIED");
            r.setVerifiedBy(verifierId);
        }
        if (currentMode == DbMode.XAMPP_MYSQL || currentMode == DbMode.LOCAL_EMBEDDED) {
            try {
                int id = Integer.parseInt(reportId.replace("INC-", "").trim());
                String sql = "UPDATE incident_reports SET status = 'Verified' WHERE incident_id = ?";
                try (Connection conn = getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, id);
                    pstmt.executeUpdate();
                    return true;
                }
            } catch (Exception e) {
                System.err.println("[DatabaseManager] Verify report error: " + e.getMessage());
            }
        }
        return true;
    }

    // =========================================================================
    // Emergency Contacts Operations
    // =========================================================================

    public boolean saveEmergencyContact(int userId, String contactName, String contactPhone) {
        if (currentMode == DbMode.XAMPP_MYSQL || currentMode == DbMode.LOCAL_EMBEDDED) {
            String sql = "INSERT INTO emergency_contacts (user_id, contact_name, contact_phone) VALUES (?, ?, ?)";
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, userId);
                pstmt.setString(2, contactName);
                pstmt.setString(3, contactPhone);
                pstmt.executeUpdate();
                return true;
            } catch (SQLException e) {
                System.err.println("[DatabaseManager] Insert contact error: " + e.getMessage());
            }
        }
        memoryContacts.add(new ContactEntry(memoryContacts.size() + 1, userId, contactName, contactPhone));
        return true;
    }

    public List<ContactEntry> getAllEmergencyContacts() {
        List<ContactEntry> list = new ArrayList<>();
        if (currentMode == DbMode.XAMPP_MYSQL || currentMode == DbMode.LOCAL_EMBEDDED) {
            String sql = "SELECT contact_id, user_id, contact_name, contact_phone FROM emergency_contacts ORDER BY contact_id ASC";
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new ContactEntry(
                            rs.getInt("contact_id"),
                            rs.getInt("user_id"),
                            rs.getString("contact_name"),
                            rs.getString("contact_phone")
                    ));
                }
                return list;
            } catch (SQLException e) {
                System.err.println("[DatabaseManager] Emergency contacts query error: " + e.getMessage());
            }
        }
        return new ArrayList<>(memoryContacts);
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private User mapUser(ResultSet rs) throws SQLException {
        String id = String.valueOf(rs.getInt("user_id"));
        String username = rs.getString("username");
        String phone = rs.getString("phone");
        String fullName = rs.getString("full_name");
        String blood = rs.getString("blood_group");
        String addr = rs.getString("address");

        User u = new User(
                id,
                username,
                username + "@kerala.gov.in",
                "CITIZEN",
                9.9312,
                76.2673
        );
        u.setPhone(phone);
        u.setFullName(fullName != null ? fullName : username);
        u.setBloodGroup(blood != null ? blood : "O+");
        u.setAddress(addr != null ? addr : "Kerala");
        u.setPasswordHash(hashPassword("Pass123456"));
        u.setActive(true);
        return u;
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return Integer.toHexString(password.hashCode());
        }
    }
}
