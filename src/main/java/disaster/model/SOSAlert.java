package disaster.model;

import java.time.LocalDateTime;

public class SOSAlert {
    private String sosId;
    private String userId;
    private double latitude;
    private double longitude;
    private String urgencyLevel;
    private String description;
    private LocalDateTime createdAt;
    private String status;
    private int respondersCount;

    public SOSAlert() {
        this.createdAt = LocalDateTime.now();
        this.status = "ACTIVE";
        this.respondersCount = 0;
    }

    public SOSAlert(String sosId, String userId, double latitude, double longitude, String urgencyLevel, String description) {
        this.sosId = sosId;
        this.userId = userId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.urgencyLevel = urgencyLevel != null ? urgencyLevel : "CRITICAL";
        this.description = description;
        this.createdAt = LocalDateTime.now();
        this.status = "ACTIVE";
        this.respondersCount = 0;
    }

    public String getSosId() { return sosId; }
    public void setSosId(String sosId) { this.sosId = sosId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getUrgencyLevel() { return urgencyLevel; }
    public void setUrgencyLevel(String urgencyLevel) { this.urgencyLevel = urgencyLevel; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getRespondersCount() { return respondersCount; }
    public void setRespondersCount(int respondersCount) { this.respondersCount = respondersCount; }
}
