package disaster.dao;

import disaster.model.SOSAlert;
import java.sql.*;

public class SOSDAO {

    public boolean saveSOSAlert(SOSAlert alert) {
        if (alert == null) {
            System.err.println("❌ [SOSDAO] FAILED: SOSAlert object is null!");
            return false;
        }

        String sql = "INSERT INTO sos_requests (user_id, latitude, longitude, severity, emergency_nature, status) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.err.println("❌ [SOSDAO] FAILED: Connection returned null!");
                return false;
            }

            conn.setAutoCommit(false);
            int generatedSosId = -1;

            int uId = 1;
            if (alert.getUserId() != null) {
                try {
                    uId = Integer.parseInt(alert.getUserId());
                } catch (NumberFormatException ignored) {}
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, uId);
                pstmt.setDouble(2, alert.getLatitude());
                pstmt.setDouble(3, alert.getLongitude());
                pstmt.setString(4, alert.getUrgencyLevel() != null ? alert.getUrgencyLevel() : "CRITICAL");
                pstmt.setString(5, alert.getDescription() != null ? alert.getDescription() : "Emergency SOS");
                pstmt.setString(6, "ACTIVE");

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    try (ResultSet keys = pstmt.getGeneratedKeys()) {
                        if (keys.next()) {
                            generatedSosId = keys.getInt(1);
                            alert.setSosId("SOS-" + generatedSosId);
                        }
                    }
                }
            }

            if (generatedSosId > 0) {
                // Record GPS position
                String gpsSql = "INSERT INTO gps_locations (user_id, latitude, longitude) VALUES (?, ?, ?)";
                try (PreparedStatement gpsStmt = conn.prepareStatement(gpsSql)) {
                    gpsStmt.setInt(1, uId);
                    gpsStmt.setDouble(2, alert.getLatitude());
                    gpsStmt.setDouble(3, alert.getLongitude());
                    gpsStmt.executeUpdate();
                } catch (SQLException ignored) {}

                // Dispatch rescue team status
                String rescueSql = "INSERT INTO rescue_status (sos_id, status, assigned_team) VALUES (?, ?, ?)";
                try (PreparedStatement rescueStmt = conn.prepareStatement(rescueSql)) {
                    rescueStmt.setInt(1, generatedSosId);
                    rescueStmt.setString(2, "DISPATCHED");
                    rescueStmt.setString(3, "NDRF 04 Battalion & Kerala Fire Force");
                    rescueStmt.executeUpdate();
                } catch (SQLException ignored) {}
            }

            conn.commit();
            System.out.println("✓ [SOSDAO] SOS Alert #" + generatedSosId + " saved successfully for User #" + uId);
            return true;

        } catch (SQLException e) {
            System.err.println("❌ [SOSDAO] SQL Exception while saving SOS alert!");
            e.printStackTrace();
            return false;
        }
    }
}
