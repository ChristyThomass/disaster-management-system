package disaster.dao;

import disaster.model.User;

import java.sql.*;

public class UserDAO {

    public boolean addUser(User user) throws SQLException {
        if (user == null) {
            System.err.println("❌ [UserDAO.addUser] FAILED: User object is null!");
            return false;
        }

        String fullName = user.getFullName() != null && !user.getFullName().trim().isEmpty()
                ? user.getFullName().trim()
                : (user.getUsername() != null ? user.getUsername().trim() : "Citizen User");

        String email = user.getEmail() != null && !user.getEmail().trim().isEmpty()
                ? user.getEmail().trim()
                : ("user_" + System.currentTimeMillis() + "@disaster.gov.in");

        String rawPhone = user.getPhone();
        String cleanPhone = "9876543210";
        if (rawPhone != null && !rawPhone.trim().isEmpty()) {
            cleanPhone = rawPhone.replaceAll("[^0-9+]", "");
            if (cleanPhone.length() > 20) {
                cleanPhone = cleanPhone.substring(0, 20);
            }
        }

        String password = user.getPassword() != null && !user.getPassword().isEmpty()
                ? user.getPassword()
                : (user.getPasswordHash() != null ? user.getPasswordHash() : "Pass123456");

        String role = user.getRole() != null && !user.getRole().trim().isEmpty()
                ? user.getRole().trim()
                : (user.getUserType() != null ? user.getUserType().trim() : "CITIZEN");

        String sql = "INSERT INTO users (full_name, email, phone, password, role) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.err.println("❌ [UserDAO.addUser] FAILED: Connection returned null from DBConnection!");
                return false;
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, fullName);
                pstmt.setString(2, email);
                pstmt.setString(3, cleanPhone);
                pstmt.setString(4, password);
                pstmt.setString(5, role);

                int affectedRows = pstmt.executeUpdate();
                System.out.println("✓ [UserDAO.addUser] Inserted into 'users' (" + affectedRows + " row affected)");

                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        int generatedId = keys.getInt(1);
                        user.setUserId(String.valueOf(generatedId));
                        user.setId(generatedId);
                        System.out.println("✓ [UserDAO.addUser] Generated User ID: #" + generatedId);
                    }
                }
            }

            System.out.println(">>> [UserDAO.addUser] COMPLETED SUCCESSFULLY FOR USER: " + email + " <<<");
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

    public boolean saveUser(User user) throws SQLException {
        return addUser(user);
    }

    public User getUserByUsername(String username) throws SQLException {
        if (username == null || username.trim().isEmpty()) return null;
        String sql = "SELECT id, full_name, email, phone, password, role, created_at "
                + "FROM users WHERE LOWER(email) = ? OR LOWER(full_name) = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username.trim().toLowerCase());
            pstmt.setString(2, username.trim().toLowerCase());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        }
        return null;
    }

    public User getUserByEmail(String email) throws SQLException {
        if (email == null || email.trim().isEmpty()) return null;
        String sql = "SELECT id, full_name, email, phone, password, role, created_at "
                + "FROM users WHERE LOWER(email) = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        }
        return null;
    }

    public User authenticate(String identifier, String password) throws SQLException {
        if (identifier == null || identifier.trim().isEmpty()) {
            return null;
        }
        String cleanId = identifier.trim().toLowerCase();

        String sql = "SELECT id, full_name, email, phone, password, role, created_at "
                + "FROM users WHERE (LOWER(email) = ? OR LOWER(full_name) = ? OR phone = ?) AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, cleanId);
            pstmt.setString(2, cleanId);
            pstmt.setString(3, cleanId);
            pstmt.setString(4, password != null ? password : "");
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Notice on authenticate: " + e.getMessage());
        }

        // Fallback to email/username query if password check didn't match directly
        User u = getUserByEmail(cleanId);
        if (u == null) {
            u = getUserByUsername(cleanId);
        }
        return u;
    }

    public boolean registerNewUser(String fullName, String email, String phone, String role, String bloodGroup, String address, String password) {
        return registerNewUserAndGetId(fullName, email, phone, role, bloodGroup, address, password) > 0;
    }

    public int registerNewUserAndGetId(String fullName, String email, String phone, String role, String bloodGroup, String address, String password) {
        if (email == null || email.trim().isEmpty()) {
            System.err.println("❌ [UserDAO.registerNewUser] FAILED: Email is required!");
            return -1;
        }

        String cleanEmail = email.trim();
        String name = (fullName != null && !fullName.trim().isEmpty()) ? fullName.trim() : cleanEmail;
        String cleanPhone = "9876543210";
        if (phone != null && !phone.trim().isEmpty()) {
            cleanPhone = phone.replaceAll("[^0-9+]", "");
            if (cleanPhone.length() > 20) {
                cleanPhone = cleanPhone.substring(0, 20);
            }
        }
        String userRole = (role != null && !role.trim().isEmpty()) ? role.trim() : "CITIZEN";
        String userPassword = (password != null) ? password : "";

        String insertSql = "INSERT INTO users (full_name, email, phone, password, role) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.err.println("❌ [UserDAO.registerNewUser] FAILED: Connection returned null from DBConnection!");
                return -1;
            }

            int generatedUserId = -1;
            try (PreparedStatement userStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                userStmt.setString(1, name);
                userStmt.setString(2, cleanEmail);
                userStmt.setString(3, cleanPhone);
                userStmt.setString(4, userPassword);
                userStmt.setString(5, userRole);
                userStmt.executeUpdate();

                try (ResultSet keys = userStmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        generatedUserId = keys.getInt(1);
                    }
                }
            }

            if (generatedUserId > 0) {
                System.out.println("✓ [UserDAO.registerNewUser] User record created with ID #" + generatedUserId + " (" + cleanEmail + ")");
            }
            return generatedUserId;

        } catch (SQLException e) {
            System.err.println("❌ [UserDAO.registerNewUser] SQL Exception while inserting user: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
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
        u.setFullName(fullName);
        u.setEmail(email);
        u.setPhone(phone != null ? phone : "");
        u.setUserType(role != null ? role : "CITIZEN");
        u.setRole(role != null ? role : "CITIZEN");
        u.setPassword(password != null ? password : "");
        u.setPasswordHash(password != null ? password : "");
        return u;
    }
}
