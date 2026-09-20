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

        // Schema matching disaster_db.incident_reports: (user_id, disaster_type, severity_level, description, status)
        String sql = "INSERT INTO incident_reports (user_id, disaster_type, severity_level, description, status) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.err.println("❌ [IncidentDAO] FAILED: Connection returned null from DBConnection!");
                return false;
            }

            conn.setAutoCommit(false);
            int generatedIncId = -1;

            int uId = 1;
            if (report.getUserId() != null) {
                try {
                    uId = Integer.parseInt(report.getUserId());
                } catch (NumberFormatException ignored) {}
            }

            int sevLevel = 5;
            if (report.getSeverity() != null) {
                try {
                    sevLevel = Integer.parseInt(report.getSeverity().trim());
                } catch (NumberFormatException e) {
                    String s = report.getSeverity().toUpperCase();
                    if (s.contains("CRIT")) sevLevel = 10;
                    else if (s.contains("HIGH")) sevLevel = 8;
                    else if (s.contains("MED")) sevLevel = 5;
                    else sevLevel = 3;
                }
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, uId);
                pstmt.setString(2, report.getDisasterType() != null ? report.getDisasterType() : "General");
                pstmt.setInt(3, sevLevel);
                pstmt.setString(4, report.getDescription() != null ? report.getDescription() : "Incident reported");
                pstmt.setString(5, report.getStatus() != null ? report.getStatus() : "Reported");

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

            // Insert into incident_media if attachment present
            if (generatedIncId > 0 && attachedFile != null && attachedFile.exists()) {
                String mediaSql = "INSERT INTO incident_media (incident_id, file_path) VALUES (?, ?)";
                try (PreparedStatement mediaStmt = conn.prepareStatement(mediaSql)) {
                    mediaStmt.setInt(1, generatedIncId);
                    mediaStmt.setString(2, attachedFile.getAbsolutePath());
                    mediaStmt.executeUpdate();
                } catch (SQLException mediaEx) {
                    System.err.println("⚠️ [IncidentDAO] Notice inserting incident media: " + mediaEx.getMessage());
                }
            }

            conn.commit();
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
