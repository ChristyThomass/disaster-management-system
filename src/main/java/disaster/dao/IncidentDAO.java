package disaster.dao;

import disaster.model.DisasterReport;
import java.io.File;
import java.sql.*;

public class IncidentDAO {

    public boolean addIncidentReport(DisasterReport report, File attachedFile) {
        if (report == null) {
            System.err.println("❌ [IncidentDAO] FAILED: DisasterReport is null!");
            return false;
        }

        String sql = "INSERT INTO incident_reports (user_id, hazard_type, landmark, details, latitude, longitude, media_path, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.err.println("❌ [IncidentDAO] FAILED: Connection returned null from DBConnection!");
                return false;
            }

            int uId = 1;
            if (report.getUserId() != null) {
                try {
                    uId = Integer.parseInt(report.getUserId());
                } catch (NumberFormatException ignored) {}
            }

            String hazardType = report.getHazardType() != null && !report.getHazardType().trim().isEmpty()
                    ? report.getHazardType().trim()
                    : (report.getDisasterType() != null ? report.getDisasterType().trim() : "General Hazard");

            String landmark = report.getLandmark() != null ? report.getLandmark().trim() : "";
            String details = report.getDetails() != null ? report.getDetails().trim()
                    : (report.getDescription() != null ? report.getDescription().trim() : "Incident reported");

            String mediaPath = "";
            if (attachedFile != null && attachedFile.exists()) {
                mediaPath = attachedFile.getAbsolutePath();
            } else if (report.getMediaPath() != null) {
                mediaPath = report.getMediaPath().trim();
            }

            String status = report.getStatus() != null && !report.getStatus().trim().isEmpty()
                    ? report.getStatus().trim()
                    : "REPORTED";

            int generatedIncId = -1;
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, uId);
                pstmt.setString(2, hazardType);
                pstmt.setString(3, landmark);
                pstmt.setString(4, details);
                pstmt.setDouble(5, report.getLatitude());
                pstmt.setDouble(6, report.getLongitude());
                pstmt.setString(7, mediaPath);
                pstmt.setString(8, status);

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    try (ResultSet keys = pstmt.getGeneratedKeys()) {
                        if (keys.next()) {
                            generatedIncId = keys.getInt(1);
                            report.setReportId("INC-" + generatedIncId);
                        }
                    }
                }
            }

            System.out.println("✓ [IncidentDAO] Incident report #" + generatedIncId + " saved successfully into disaster_db.incident_reports.");
            return true;

        } catch (SQLException e) {
            System.err.println("❌ [IncidentDAO] SQL Exception while inserting incident report!");
            System.err.println("   Message: " + e.getMessage());
            System.err.println("   SQL State: " + e.getSQLState());
            System.err.println("   Error Code: " + e.getErrorCode());
            e.printStackTrace();
            return false;
        }
    }
}
