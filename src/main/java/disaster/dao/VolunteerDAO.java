package disaster.dao;

import disaster.model.Volunteer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VolunteerDAO {

    public boolean registerVolunteer(int userId, String district, String skill, String idProofStatus, String approvalStatus) {
        String sql = "INSERT INTO volunteers (user_id, district, skill, id_proof_status, approval_status) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (conn == null) {
                System.err.println("❌ [VolunteerDAO] FAILED: Connection returned null!");
                return false;
            }

            pstmt.setInt(1, userId);
            pstmt.setString(2, district != null ? district.trim() : "Kerala State");
            pstmt.setString(3, skill != null ? skill.trim() : "General Relief");
            pstmt.setString(4, idProofStatus != null ? idProofStatus.trim() : "UNVERIFIED");
            pstmt.setString(5, approvalStatus != null ? approvalStatus.trim() : "PENDING");

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        int genId = keys.getInt(1);
                        System.out.println("✓ [VolunteerDAO] Volunteer record #" + genId + " created for User #" + userId);
                    }
                }
            }
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("❌ [VolunteerDAO] SQL Exception while registering volunteer: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean registerVolunteer(int userId, String district, String skill) {
        return registerVolunteer(userId, district, skill, "UNVERIFIED", "PENDING");
    }

    public boolean saveVolunteer(Volunteer v) {
        if (v == null) return false;
        return registerVolunteer(v.getUserId(), v.getDistrict(), v.getSkill(), v.getIdProofStatus(), v.getApprovalStatus());
    }

    public boolean updateApprovalStatus(int id, String approvalStatus) {
        String sql = "UPDATE volunteers SET approval_status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (conn == null) return false;
            pstmt.setString(1, approvalStatus != null ? approvalStatus.trim() : "PENDING");
            pstmt.setInt(2, id);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("❌ [VolunteerDAO] SQL Exception updating approval status: " + e.getMessage());
            return false;
        }
    }

    public List<Volunteer> getVolunteersByUserId(int userId) {
        List<Volunteer> list = new ArrayList<>();
        String sql = "SELECT id, user_id, district, skill, id_proof_status, approval_status FROM volunteers WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (conn == null) return list;
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Volunteer(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getString("district"),
                            rs.getString("skill"),
                            rs.getString("id_proof_status"),
                            rs.getString("approval_status")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ [VolunteerDAO] Error fetching volunteers by user_id: " + e.getMessage());
        }
        return list;
    }
}
