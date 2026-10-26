package disaster.dao;

import disaster.model.SOSAlert;
import java.sql.*;

public class SOSDAO {

    public boolean saveSOSAlert(SOSAlert alert) {
        if (alert == null) {
            System.err.println("❌ [SOSDAO] FAILED: SOSAlert object is null!");
            return false;
        }

        String sql = "INSERT INTO sos_requests (user_id, latitude, longitude, status) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.err.println("❌ [SOSDAO] FAILED: Connection returned null!");
                return false;
            }

            int uId = 1;
            if (alert.getUserId() != null) {
                try {
                    uId = Integer.parseInt(alert.getUserId());
                } catch (NumberFormatException ignored) {}
            }

            String status = alert.getStatus() != null && !alert.getStatus().trim().isEmpty()
                    ? alert.getStatus().trim()
                    : "DISPATCHED";

            int generatedSosId = -1;
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, uId);
                pstmt.setDouble(2, alert.getLatitude());
                pstmt.setDouble(3, alert.getLongitude());
                pstmt.setString(4, status);

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    try (ResultSet keys = pstmt.getGeneratedKeys()) {
                        if (keys.next()) {
                            generatedSosId = keys.getInt(1);
                            alert.setSosId("SOS-" + generatedSosId);
                            alert.setId(generatedSosId);
                        }
                    }
                }
            }

            System.out.println("✓ [SOSDAO] SOS Alert #" + generatedSosId + " saved successfully into sos_requests for User #" + uId);
            return true;

        } catch (SQLException e) {
            System.err.println("❌ [SOSDAO] SQL Exception while saving SOS alert!");
            e.printStackTrace();
            return false;
        }
    }
}
