package disaster.model;

import java.time.LocalDateTime;

public class DisasterReport {
    private String reportId;
    private String userId;
    private String disasterType;
    private double latitude;
    private double longitude;
    private String severity;
    private String description;
    private LocalDateTime reportedAt;
    private String status;
    private int affectedPeople;
    private String verifiedBy;

    public DisasterReport() {
        this.reportedAt = LocalDateTime.now();
        this.status = "PENDING";
    }

    public DisasterReport(String reportId, String userId, String disasterType, double latitude, double longitude, String severity, String description, int affectedPeople) {
        this.reportId = reportId;
        this.userId = userId;
        this.disasterType = disasterType;
        this.latitude = latitude;
        this.longitude = longitude;
        this.severity = severity;
        this.description = description;
        this.affectedPeople = affectedPeople;
        this.reportedAt = LocalDateTime.now();
        this.status = "PENDING";
    }

    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getDisasterType() { return disasterType; }
    public void setDisasterType(String disasterType) { this.disasterType = disasterType; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getReportedAt() { return reportedAt; }
    public void setReportedAt(LocalDateTime reportedAt) { this.reportedAt = reportedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getAffectedPeople() { return affectedPeople; }
    public void setAffectedPeople(int affectedPeople) { this.affectedPeople = affectedPeople; }

    public String getVerifiedBy() { return verifiedBy; }
    public void setVerifiedBy(String verifiedBy) { this.verifiedBy = verifiedBy; }
}
