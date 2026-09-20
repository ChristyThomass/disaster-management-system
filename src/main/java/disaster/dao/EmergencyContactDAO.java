package disaster.dao;

import java.sql.*;

public class EmergencyContactDAO {

    public boolean saveContact(int userId, String contactName, String contactPhone) {
        if (contactName == null || contactName.trim().isEmpty() || contactPhone == null || contactPhone.trim().isEmpty()) {
            System.err.println("❌ [EmergencyContactDAO] FAILED: Contact name and phone are required!");
            return false;
        }

        String sql = "INSERT INTO emergency_contacts (user_id, contact_name, contact_phone) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (conn == null) {
                System.err.println("❌ [EmergencyContactDAO] FAILED: Connection returned null!");
                return false;
            }

            pstmt.setInt(1, userId);
            pstmt.setString(2, contactName.trim());
            pstmt.setString(3, contactPhone.trim());

            int affectedRows = pstmt.executeUpdate();
            System.out.println("✓ [EmergencyContactDAO] Contact '" + contactName + "' saved for User #" + userId);
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("❌ [EmergencyContactDAO] SQL Exception while saving emergency contact!");
            e.printStackTrace();
            return false;
        }
    }
}
