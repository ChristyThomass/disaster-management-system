package disaster.dao;

import disaster.model.User;

import java.sql.*;

public class UserDAO {

    public boolean addUser(User user) throws SQLException {
        if (user == null) {
            System.err.println("❌ [UserDAO.addUser] FAILED: User object is null!");
            return false;
        }

        System.out.println(">>> [UserDAO.addUser] EXECUTING DATABASE INSERTION <<<");
        System.out.println("    Payload -> Username: '" + user.getUsername() + "'");
        System.out.println("    Payload -> Phone: '" + user.getPhone() + "'");
        System.out.println("    Payload -> Full Name: '" + user.getFullName() + "'");
        System.out.println("    Payload -> Email: '" + user.getEmail() + "'");

        // Sanitize phone for VARCHAR(15) column in disaster_db.users
        String rawPhone = user.getPhone();
        String cleanPhone = "9876543210";
        if (rawPhone != null && !rawPhone.trim().isEmpty()) {
            cleanPhone = rawPhone.replaceAll("[^0-9+]", "");
            if (cleanPhone.length() > 15) {
                cleanPhone = cleanPhone.substring(0, 15);
            }
        }

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.err.println("❌ [UserDAO.addUser] FAILED: Connection returned null from DBConnection!");
                return false;
            }

            // Verify auto-commit
            if (!conn.getAutoCommit()) {
                conn.setAutoCommit(true);
            }

            // 1. Check if user already exists; if so, generate a unique username so a new database record is created
            String desiredUsername = user.getUsername().trim();
            String checkSql = "SELECT user_id FROM users WHERE LOWER(username) = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, desiredUsername.toLowerCase());
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        String uniqueUsername = desiredUsername + "_" + (int)((System.currentTimeMillis() % 9000) + 1000);
                        System.out.println("ℹ [UserDAO.addUser] Username '" + desiredUsername + "' exists. Creating new unique account: " + uniqueUsername);
                        user.setUsername(uniqueUsername);
                    }
                }
            }

            // 2. Insert into users (username, phone)
            String sql = "INSERT INTO users (username, phone) VALUES (?, ?)";
            int generatedId = 0;
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, user.getUsername().trim());
                pstmt.setString(2, cleanPhone);
                int affectedRows = pstmt.executeUpdate();
                System.out.println("✓ [UserDAO.addUser] Inserted into 'users' (" + affectedRows + " row affected)");

                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        generatedId = keys.getInt(1);
                        user.setUserId(String.valueOf(generatedId));
                        System.out.println("✓ [UserDAO.addUser] Generated User ID: #" + generatedId);
                    }
                }
            }

            // 3. Insert into user_profiles
            if (generatedId > 0) {
                String profileSql = "INSERT INTO user_profiles (user_id, full_name, blood_group, address) VALUES (?, ?, ?, ?)";
                try (PreparedStatement profStmt = conn.prepareStatement(profileSql)) {
                    profStmt.setInt(1, generatedId);
                    String fullName = (user.getFullName() != null && !user.getFullName().trim().isEmpty())
                            ? user.getFullName().trim() : user.getUsername().trim();
                    String blood = (user.getBloodGroup() != null && !user.getBloodGroup().trim().isEmpty())
                            ? user.getBloodGroup().trim() : "O+";
                    String addr = (user.getAddress() != null && !user.getAddress().trim().isEmpty())
                            ? user.getAddress().trim() : "Kerala Disaster Relief Area";

                    profStmt.setString(2, fullName);
                    profStmt.setString(3, blood);
                    profStmt.setString(4, addr);
                    int profAffected = profStmt.executeUpdate();
                    System.out.println("✓ [UserDAO.addUser] Inserted into 'user_profiles' (" + profAffected + " row affected)");
                }
            }

            System.out.println(">>> [UserDAO.addUser] COMPLETED SUCCESSFULLY FOR USER: " + user.getUsername() + " <<<");
            return true;
        } catch (SQLException e) {
            System.err.println("❌ [UserDAO.addUser] SQL Exception during user creation!");
            System.err.println("   SQL State: " + e.getSQLState());
            System.err.println("   Error Code: " + e.getErrorCode());
            System.err.println("   Error Message: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private void ensureProfileExists(Connection conn, int userId, User user) {
        String checkSql = "SELECT profile_id FROM user_profiles WHERE user_id = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, userId);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (!rs.next()) {
                    String profSql = "INSERT INTO user_profiles (user_id, full_name, blood_group, address) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement profStmt = conn.prepareStatement(profSql)) {
                        profStmt.setInt(1, userId);
                        String fullName = user.getFullName() != null ? user.getFullName() : user.getUsername();
                        profStmt.setString(2, fullName);
                        profStmt.setString(3, user.getBloodGroup() != null ? user.getBloodGroup() : "O+");
                        profStmt.setString(4, user.getAddress() != null ? user.getAddress() : "Kerala Disaster Relief Area");
                        profStmt.executeUpdate();
                    }
                }
            }
        } catch (SQLException ex) {
            System.err.println("Notice on profile check: " + ex.getMessage());
        }
    }

    public boolean saveUser(User user) throws SQLException {
        return addUser(user);
    }

    public User getUserByUsername(String username) throws SQLException {
        String sql = "SELECT u.user_id, u.username, u.phone, p.full_name, p.blood_group, p.address "
                + "FROM users u LEFT JOIN user_profiles p ON u.user_id = p.user_id "
                + "WHERE LOWER(u.username) = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username.toLowerCase());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String id = String.valueOf(rs.getInt("user_id"));
                    String uname = rs.getString("username");
                    String phone = rs.getString("phone");
                    User u = new User(id, uname, uname + "@kerala.gov.in", "CITIZEN", 9.9312, 76.2673);
                    u.setPhone(phone);
                    return u;
                }
            }
        }
        return null;
    }

    public User getUserByEmail(String email) throws SQLException {
        String usernameGuess = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
        return getUserByUsername(usernameGuess);
    }
}

