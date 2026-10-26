package disaster.dao;

import java.sql.*;

public class EmergencyContactDAO {

    public boolean saveContact(int userId, String contactName, String contactPhone, String relation) {
        if (contactName == null || contactName.trim().isEmpty() || contactPhone == null || contactPhone.trim().isEmpty()) {
            System.err.println("❌ [EmergencyContactDAO] FAILED: Contact name and phone are required!");
            return false;
        }

        String sql = "INSERT INTO emergency_contacts (user_id, contact_name, contact_phone, relation) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (conn == null) {
                System.err.println("❌ [EmergencyContactDAO] FAILED: Connection returned null!");
                return false;
            }

            pstmt.setInt(1, userId);
            pstmt.setString(2, contactName.trim());
            pstmt.setString(3, contactPhone.trim());
            pstmt.setString(4, relation != null ? relation.trim() : "Emergency Contact");

            int affectedRows = pstmt.executeUpdate();
            System.out.println("✓ [EmergencyContactDAO] Contact '" + contactName + "' saved for User #" + userId);
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("❌ [EmergencyContactDAO] SQL Exception while saving emergency contact!");
            e.printStackTrace();
            return false;
        }
    }

    public boolean saveContact(int userId, String contactName, String contactPhone) {
        return saveContact(userId, contactName, contactPhone, "Emergency Contact");
    }

    public boolean updateContact(String oldName, String oldPhone, String newName, String newPhone) {
        String sql = "UPDATE emergency_contacts SET contact_name = ?, contact_phone = ? WHERE contact_name = ? OR contact_phone = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (conn == null) return false;
            pstmt.setString(1, newName.trim());
            pstmt.setString(2, newPhone.trim());
            pstmt.setString(3, oldName.trim());
            pstmt.setString(4, oldPhone.trim());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ [EmergencyContactDAO] Update error: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteContact(String name, String phone) {
        String sql = "DELETE FROM emergency_contacts WHERE contact_name = ? OR contact_phone = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (conn == null) return false;
            pstmt.setString(1, name.trim());
            pstmt.setString(2, phone.trim());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ [EmergencyContactDAO] Delete error: " + e.getMessage());
            return false;
        }
    }
}
