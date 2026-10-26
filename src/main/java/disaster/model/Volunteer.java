package disaster.model;

public class Volunteer {
    private int id;
    private int userId;
    private String district;
    private String skill;
    private String idProofStatus;
    private String approvalStatus;

    public Volunteer() {
        this.idProofStatus = "UNVERIFIED";
        this.approvalStatus = "PENDING";
    }

    public Volunteer(int userId, String district, String skill) {
        this.userId = userId;
        this.district = district;
        this.skill = skill;
        this.idProofStatus = "UNVERIFIED";
        this.approvalStatus = "PENDING";
    }

    public Volunteer(int id, int userId, String district, String skill, String idProofStatus, String approvalStatus) {
        this.id = id;
        this.userId = userId;
        this.district = district;
        this.skill = skill;
        this.idProofStatus = idProofStatus != null ? idProofStatus : "UNVERIFIED";
        this.approvalStatus = approvalStatus != null ? approvalStatus : "PENDING";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getSkill() { return skill; }
    public void setSkill(String skill) { this.skill = skill; }

    public String getIdProofStatus() { return idProofStatus; }
    public void setIdProofStatus(String idProofStatus) { this.idProofStatus = idProofStatus; }

    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }

    @Override
    public String toString() {
        return "Volunteer{" +
                "id=" + id +
                ", userId=" + userId +
                ", district='" + district + '\'' +
                ", skill='" + skill + '\'' +
                ", idProofStatus='" + idProofStatus + '\'' +
                ", approvalStatus='" + approvalStatus + '\'' +
                '}';
    }
}
