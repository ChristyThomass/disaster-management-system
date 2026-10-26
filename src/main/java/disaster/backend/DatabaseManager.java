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
                    + "`id` INT AUTO_INCREMENT PRIMARY KEY, "
                    + "`full_name` VARCHAR(100) NOT NULL, "
                    + "`email` VARCHAR(100) NOT NULL UNIQUE, "
                    + "`phone` VARCHAR(20), "
                    + "`password` VARCHAR(255) NOT NULL, "
                    + "`role` VARCHAR(30) NOT NULL DEFAULT 'CITIZEN', "
                    + "`created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            // 2. emergency_contacts
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `emergency_contacts` ("
                    + "`id` INT AUTO_INCREMENT PRIMARY KEY, "
                    + "`user_id` INT NOT NULL, "
                    + "`contact_name` VARCHAR(100) NOT NULL, "
                    + "`contact_phone` VARCHAR(20) NOT NULL, "
                    + "`relation` VARCHAR(50)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            // 3. volunteers
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `volunteers` ("
                    + "`id` INT AUTO_INCREMENT PRIMARY KEY, "
                    + "`user_id` INT NOT NULL, "
                    + "`district` VARCHAR(50), "
                    + "`skill` VARCHAR(100), "
                    + "`id_proof_status` VARCHAR(50) DEFAULT 'UNVERIFIED', "
                    + "`approval_status` VARCHAR(30) NOT NULL DEFAULT 'PENDING'"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            // 4. incident_reports
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `incident_reports` ("
                    + "`id` INT AUTO_INCREMENT PRIMARY KEY, "
                    + "`user_id` INT NOT NULL, "
                    + "`hazard_type` VARCHAR(50) NOT NULL, "
                    + "`landmark` VARCHAR(150), "
                    + "`details` TEXT, "
                    + "`latitude` DECIMAL(10,8), "
                    + "`longitude` DECIMAL(11,8), "
                    + "`media_path` VARCHAR(255), "
                    + "`status` VARCHAR(30) NOT NULL DEFAULT 'REPORTED', "
                    + "`created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            // 5. sos_requests
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `sos_requests` ("
                    + "`id` INT AUTO_INCREMENT PRIMARY KEY, "
                    + "`user_id` INT NOT NULL, "
                    + "`latitude` DECIMAL(10,8) NOT NULL, "
                    + "`longitude` DECIMAL(11,8) NOT NULL, "
                    + "`status` VARCHAR(30) NOT NULL DEFAULT 'DISPATCHED', "
                    + "`created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        }
    }

    private void seedMySQLInitialData(Connection conn) {
        // Only seed if users table is empty
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT count(*) FROM users")) {
            if (rs.next() && rs.getInt(1) == 0) {
                // Seed alvin_ms
                String sql1 = "INSERT INTO users (full_name, email, phone, password, role) VALUES ('Alvin MS', 'alvin@disaster.gov.in', '9876543210', 'Pass123456', 'CITIZEN')";
                stmt.executeUpdate(sql1);

                // Seed athul_c
                String sql2 = "INSERT INTO users (full_name, email, phone, password, role) VALUES ('Athul Cleetus', 'athul@disaster.gov.in', '8765432109', 'Pass123456', 'VOLUNTEER')";
                stmt.executeUpdate(sql2);
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
                        + "id INT AUTO_INCREMENT PRIMARY KEY, "
                        + "full_name VARCHAR(100) NOT NULL, "
                        + "email VARCHAR(100) NOT NULL UNIQUE, "
                        + "phone VARCHAR(20), "
                        + "password VARCHAR(255) NOT NULL, "
                        + "role VARCHAR(30) DEFAULT 'CITIZEN', "
                        + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS emergency_contacts ("
                        + "id INT AUTO_INCREMENT PRIMARY KEY, "
                        + "user_id INT NOT NULL, "
                        + "contact_name VARCHAR(100) NOT NULL, "
                        + "contact_phone VARCHAR(20) NOT NULL, "
                        + "relation VARCHAR(50))");

                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS volunteers ("
                        + "id INT AUTO_INCREMENT PRIMARY KEY, "
                        + "user_id INT NOT NULL, "
                        + "district VARCHAR(50), "
                        + "skill VARCHAR(100), "
                        + "id_proof_status VARCHAR(50) DEFAULT 'UNVERIFIED', "
                        + "approval_status VARCHAR(30) DEFAULT 'PENDING')");

                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS incident_reports ("
                        + "id INT AUTO_INCREMENT PRIMARY KEY, "
                        + "user_id INT NOT NULL, "
                        + "hazard_type VARCHAR(50) NOT NULL, "
                        + "landmark VARCHAR(150), "
                        + "details TEXT, "
                        + "latitude DECIMAL(10,8), "
                        + "longitude DECIMAL(11,8), "
                        + "media_path VARCHAR(255), "
                        + "status VARCHAR(30) DEFAULT 'REPORTED', "
                        + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS sos_requests ("
                        + "id INT AUTO_INCREMENT PRIMARY KEY, "
                        + "user_id INT NOT NULL, "
                        + "latitude DECIMAL(10,8) NOT NULL, "
                        + "longitude DECIMAL(11,8) NOT NULL, "
                        + "status VARCHAR(30) DEFAULT 'DISPATCHED', "
                        + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

                ResultSet rs = stmt.executeQuery("SELECT count(*) FROM users");
                if (rs.next() && rs.getInt(1) == 0) {
                    stmt.executeUpdate("INSERT INTO users (full_name, email, phone, password, role) VALUES ('Alvin MS', 'alvin@disaster.gov.in', '9876543210', 'Pass123456', 'CITIZEN')");
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

        // Try lookup by username or email
        try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM users WHERE LOWER(email) = ? OR LOWER(full_name) = ?")) {
            ps.setString(1, clean.toLowerCase());
            ps.setString(2, clean.toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException ignored) {}

        // Fallback: first user in database
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id FROM users ORDER BY id ASC LIMIT 1")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException ignored) {}

        return 1;
    }

    // =========================================================================
    // User Operations
    // =========================================================================

    public User getUserByUsername(String username) {
        if (currentMode == DbMode.XAMPP_MYSQL || currentMode == DbMode.LOCAL_EMBEDDED) {
            String sql = "SELECT id, full_name, email, phone, role, password FROM users WHERE LOWER(email) = ? OR LOWER(full_name) = ?";
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username.toLowerCase());
                pstmt.setString(2, username.toLowerCase());
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
            if (u.getUsername().equalsIgnoreCase(username) || (u.getEmail() != null && u.getEmail().equalsIgnoreCase(username))) return u;
        }
        return null;
    }

    public User getUserByEmail(String email) {
        if (currentMode == DbMode.XAMPP_MYSQL || currentMode == DbMode.LOCAL_EMBEDDED) {
            String sql = "SELECT id, full_name, email, phone, role, password FROM users WHERE LOWER(email) = ?";
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
                String checkSql = "SELECT id FROM users WHERE LOWER(email) = ?";
                int userId = 0;
                try (PreparedStatement check = conn.prepareStatement(checkSql)) {
                    String em = (u.getEmail() != null && !u.getEmail().isEmpty()) ? u.getEmail().toLowerCase() : (u.getUsername().toLowerCase() + "@disaster.gov.in");
                    check.setString(1, em);
                    try (ResultSet rs = check.executeQuery()) {
                        if (rs.next()) {
                            userId = rs.getInt("id");
                            u.setUserId(String.valueOf(userId));
                            u.setId(userId);
                        }
                    }
                }

                // Insert into users if not existing
                if (userId == 0) {
                    String insertSql = "INSERT INTO users (full_name, email, phone, password, role) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                        pstmt.setString(1, u.getFullName() != null ? u.getFullName() : u.getUsername());
                        pstmt.setString(2, u.getEmail() != null ? u.getEmail() : (u.getUsername() + "@disaster.gov.in"));
                        String phone = (u.getPhone() != null && !u.getPhone().isEmpty()) ? u.getPhone() : "9876543210";
                        pstmt.setString(3, phone);
                        pstmt.setString(4, u.getPassword() != null ? u.getPassword() : "Pass123456");
                        pstmt.setString(5, u.getRole() != null ? u.getRole() : "CITIZEN");
                        pstmt.executeUpdate();
                        try (ResultSet keys = pstmt.getGeneratedKeys()) {
                            if (keys.next()) {
                                userId = keys.getInt(1);
                                u.setUserId(String.valueOf(userId));
                                u.setId(userId);
                            }
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
                String insertSos = "INSERT INTO sos_requests (user_id, latitude, longitude, status) VALUES (?, ?, ?, ?)";
                int sosId = 0;
                try (PreparedStatement ps = conn.prepareStatement(insertSos, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, userId);
                    ps.setDouble(2, sos.getLatitude());
                    ps.setDouble(3, sos.getLongitude());
                    ps.setString(4, sos.getStatus() != null ? sos.getStatus() : "DISPATCHED");
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            sosId = keys.getInt(1);
                            sos.setSosId(String.valueOf(sosId));
                            sos.setId(sosId);
                            memorySOS.put(sos.getSosId(), sos);
                        }
                    }
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
            String sql = "SELECT id, user_id, latitude, longitude, status, created_at FROM sos_requests ORDER BY id DESC";
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    double lat = rs.getDouble("latitude");
                    if (rs.wasNull()) lat = 9.9312;
                    double lon = rs.getDouble("longitude");
                    if (rs.wasNull()) lon = 76.2673;
                    SOSAlert s = new SOSAlert(
                            String.valueOf(rs.getInt("id")),
                            String.valueOf(rs.getInt("user_id")),
                            lat,
                            lon,
                            "CRITICAL",
                            "Emergency SOS Distress"
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
            if ("Active".equalsIgnoreCase(s.getStatus()) || "DISPATCHED".equalsIgnoreCase(s.getStatus())) list.add(s);
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
                String sql = "UPDATE sos_requests SET status = 'Acknowledged' WHERE id = ?";
                try (Connection conn = getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
                String insertSql = "INSERT INTO incident_reports (user_id, hazard_type, landmark, details, latitude, longitude, media_path, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                int incId = 0;
                try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, userId);
                    ps.setString(2, rep.getHazardType() != null ? rep.getHazardType() : (rep.getDisasterType() != null ? rep.getDisasterType() : "General"));
                    ps.setString(3, rep.getLandmark() != null ? rep.getLandmark() : "");
                    ps.setString(4, rep.getDetails() != null ? rep.getDetails() : (rep.getDescription() != null ? rep.getDescription() : "Incident reported"));
                    ps.setDouble(5, rep.getLatitude());
                    ps.setDouble(6, rep.getLongitude());
                    ps.setString(7, rep.getMediaPath() != null ? rep.getMediaPath() : "");
                    ps.setString(8, rep.getStatus() != null ? rep.getStatus() : "REPORTED");
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            incId = keys.getInt(1);
                            rep.setReportId("INC-" + incId);
                            memoryReports.put(rep.getReportId(), rep);
                        }
                    }
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
                String sql = "UPDATE incident_reports SET status = 'Verified' WHERE id = ?";
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
            String sql = "SELECT id, user_id, contact_name, contact_phone FROM emergency_contacts ORDER BY id ASC";
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new ContactEntry(
                            rs.getInt("id"),
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

    public boolean updateEmergencyContact(String oldName, String oldPhone, String newName, String newPhone) {
        if (currentMode == DbMode.XAMPP_MYSQL || currentMode == DbMode.LOCAL_EMBEDDED) {
            String sql = "UPDATE emergency_contacts SET contact_name = ?, contact_phone = ? WHERE contact_name = ? OR contact_phone = ?";
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, newName);
                pstmt.setString(2, newPhone);
                pstmt.setString(3, oldName);
                pstmt.setString(4, oldPhone);
                int count = pstmt.executeUpdate();
                if (count > 0) return true;
            } catch (SQLException e) {
                System.err.println("[DatabaseManager] Update contact error: " + e.getMessage());
            }
        }
        for (ContactEntry c : memoryContacts) {
            if (c.contactName.equalsIgnoreCase(oldName) || c.contactPhone.equals(oldPhone)) {
                c.contactName = newName;
                c.contactPhone = newPhone;
                return true;
            }
        }
        return false;
    }

    public boolean deleteEmergencyContact(String name, String phone) {
        if (currentMode == DbMode.XAMPP_MYSQL || currentMode == DbMode.LOCAL_EMBEDDED) {
            String sql = "DELETE FROM emergency_contacts WHERE contact_name = ? OR contact_phone = ?";
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.setString(2, phone);
                int count = pstmt.executeUpdate();
                if (count > 0) return true;
            } catch (SQLException e) {
                System.err.println("[DatabaseManager] Delete contact error: " + e.getMessage());
            }
        }
        return memoryContacts.removeIf(c -> c.contactName.equalsIgnoreCase(name) || c.contactPhone.equals(phone));
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    public static String cleanEmail(String email) {
        if (email == null) return "";
        email = email.trim();
        if (email.contains("@")) {
            int firstAt = email.indexOf('@');
            int secondAt = email.indexOf('@', firstAt + 1);
            if (secondAt > 0) {
                email = email.substring(0, secondAt);
            }
        }
        if (email.endsWith("@kerala.gov.in") && email.length() > "@kerala.gov.in".length()) {
            String sub = email.substring(0, email.length() - "@kerala.gov.in".length());
            if (sub.contains("@")) email = sub;
        }
        if (email.endsWith("@kerala.gov.com") && email.length() > "@kerala.gov.com".length()) {
            String sub = email.substring(0, email.length() - "@kerala.gov.com".length());
            if (sub.contains("@")) email = sub;
        }
        return email;
    }

    private User mapUser(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String fullName = rs.getString("full_name");
        String email = rs.getString("email");
        String phone = rs.getString("phone");
        String role = rs.getString("role");
        String password = rs.getString("password");

        User u = new User(String.valueOf(id), fullName, email, role != null ? role : "CITIZEN", 9.9312, 76.2673);
        u.setId(id);
        u.setFullName(fullName != null ? fullName : "");
        u.setEmail(email != null ? email : "");
        u.setPhone(phone != null ? phone : "");
        u.setRole(role != null ? role : "CITIZEN");
        u.setPassword(password != null ? password : "");
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
