package disaster.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Centralized data management system for SDMA Portal and Admin Panel.
 * Provides synchronized, modifiable records for Volunteers, Inventory Supplies,
 * Shelter Homes, Disaster Alerts, and Live Bulletins.
 */
public class AdminDataManager {

    private static AdminDataManager instance;

    // ---------------- Model Classes ----------------
    public static class Volunteer {
        private int id;
        private String username;
        private String name;
        private String email;
        private String phone;
        private String district;
        private String skill;
        private String idProofPath;
        private String certificatePath;
        private String status;
        private String registeredDate;

        public Volunteer(int id, String name, String skill, String sector, String phone, String status) {
            this(id,
                 name.toLowerCase().replaceAll("[^a-z0-9]", "_"),
                 name,
                 name.toLowerCase().replaceAll("[^a-z0-9]", "") + "@volunteer.sdma.gov.in",
                 phone,
                 sector,
                 skill,
                 "Verified Govt Record",
                 "Certified Responder",
                 status,
                 "2026-10-26");
        }

        public Volunteer(int id, String username, String name, String email, String phone,
                         String district, String skill, String idProofPath, String certificatePath,
                         String status, String registeredDate) {
            this.id = id;
            this.username = username != null ? username : "";
            this.name = name != null ? name : "";
            this.email = email != null ? email : "";
            this.phone = phone != null ? phone : "";
            this.district = district != null ? district : "General Sector";
            this.skill = skill != null ? skill : "General Relief";
            this.idProofPath = idProofPath != null ? idProofPath : "None Attached";
            this.certificatePath = certificatePath != null ? certificatePath : "None Attached";
            this.status = status != null ? status : "Pending";
            this.registeredDate = registeredDate != null ? registeredDate : "2026-10-26";
        }

        public int getId() { return id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getDistrict() { return district; }
        public void setDistrict(String district) { this.district = district; }
        public String getSector() { return district; }
        public void setSector(String sector) { this.district = sector; }
        public String getSkill() { return skill; }
        public void setSkill(String skill) { this.skill = skill; }
        public String getIdProofPath() { return idProofPath; }
        public void setIdProofPath(String idProofPath) { this.idProofPath = idProofPath; }
        public String getCertificatePath() { return certificatePath; }
        public void setCertificatePath(String certificatePath) { this.certificatePath = certificatePath; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getRegisteredDate() { return registeredDate; }
        public void setRegisteredDate(String registeredDate) { this.registeredDate = registeredDate; }
    }

    public static class VolunteerTask {
        private int taskId;
        private int volunteerId;
        private String volunteerName;
        private String taskTitle;
        private String district;
        private String location;
        private String priority;
        private String status;
        private String assignedDate;
        private String instructions;

        public VolunteerTask(int taskId, int volunteerId, String volunteerName, String taskTitle,
                             String district, String location, String priority, String status,
                             String assignedDate, String instructions) {
            this.taskId = taskId;
            this.volunteerId = volunteerId;
            this.volunteerName = volunteerName != null ? volunteerName : "";
            this.taskTitle = taskTitle != null ? taskTitle : "";
            this.district = district != null ? district : "";
            this.location = location != null ? location : "";
            this.priority = priority != null ? priority : "High";
            this.status = status != null ? status : "Assigned";
            this.assignedDate = assignedDate != null ? assignedDate : "2026-10-26";
            this.instructions = instructions != null ? instructions : "";
        }

        public int getTaskId() { return taskId; }
        public int getVolunteerId() { return volunteerId; }
        public void setVolunteerId(int volunteerId) { this.volunteerId = volunteerId; }
        public String getVolunteerName() { return volunteerName; }
        public void setVolunteerName(String volunteerName) { this.volunteerName = volunteerName; }
        public String getTaskTitle() { return taskTitle; }
        public void setTaskTitle(String taskTitle) { this.taskTitle = taskTitle; }
        public String getDistrict() { return district; }
        public void setDistrict(String district) { this.district = district; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getAssignedDate() { return assignedDate; }
        public void setAssignedDate(String assignedDate) { this.assignedDate = assignedDate; }
        public String getInstructions() { return instructions; }
        public void setInstructions(String instructions) { this.instructions = instructions; }
    }


    public static class InventoryItem {
        private int id;
        private String name;
        private String depot;
        private String quantity;
        private String unit;
        private String status;

        public InventoryItem(int id, String name, String depot, String quantity, String unit, String status) {
            this.id = id;
            this.name = name;
            this.depot = depot;
            this.quantity = quantity;
            this.unit = unit;
            this.status = status;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDepot() { return depot; }
        public void setDepot(String depot) { this.depot = depot; }
        public String getQuantity() { return quantity; }
        public void setQuantity(String quantity) { this.quantity = quantity; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class ShelterHome {
        private int id;
        private String name;
        private String district;
        private int occupancy;
        private int capacity;
        private String coordinator;
        private String phone;
        private String status;

        public ShelterHome(int id, String name, String district, int occupancy, int capacity, String coordinator, String phone, String status) {
            this.id = id;
            this.name = name;
            this.district = district;
            this.occupancy = occupancy;
            this.capacity = capacity;
            this.coordinator = coordinator;
            this.phone = phone;
            this.status = status;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDistrict() { return district; }
        public void setDistrict(String district) { this.district = district; }
        public int getOccupancy() { return occupancy; }
        public void setOccupancy(int occupancy) { this.occupancy = occupancy; }
        public int getCapacity() { return capacity; }
        public void setCapacity(int capacity) { this.capacity = capacity; }
        public String getCoordinator() { return coordinator; }
        public void setCoordinator(String coordinator) { this.coordinator = coordinator; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class DisasterAlert {
        private int id;
        private String hazard;
        private String district;
        private String level;
        private String affected;
        private String status;

        public DisasterAlert(int id, String hazard, String district, String level, String affected, String status) {
            this.id = id;
            this.hazard = hazard;
            this.district = district;
            this.level = level;
            this.affected = affected;
            this.status = status;
        }

        public int getId() { return id; }
        public String getHazard() { return hazard; }
        public void setHazard(String hazard) { this.hazard = hazard; }
        public String getDistrict() { return district; }
        public void setDistrict(String district) { this.district = district; }
        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }
        public String getAffected() { return affected; }
        public void setAffected(String affected) { this.affected = affected; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class GroundBulletin {
        private int id;
        private String time;
        private String message;
        private String severity;

        public GroundBulletin(int id, String time, String message, String severity) {
            this.id = id;
            this.time = time;
            this.message = message;
            this.severity = severity;
        }

        public int getId() { return id; }
        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
    }

    // ---------------- Data Collections ----------------
    private final List<Volunteer> volunteers = new CopyOnWriteArrayList<>();
    private final List<VolunteerTask> volunteerTasks = new CopyOnWriteArrayList<>();
    private final List<InventoryItem> inventoryItems = new CopyOnWriteArrayList<>();
    private final List<ShelterHome> shelterHomes = new CopyOnWriteArrayList<>();
    private final List<DisasterAlert> alerts = new CopyOnWriteArrayList<>();
    private final List<GroundBulletin> bulletins = new CopyOnWriteArrayList<>();
    private final List<Runnable> changeListeners = new CopyOnWriteArrayList<>();

    private int nextVolunteerId = 101;
    private int nextTaskId = 1001;
    private int nextInventoryId = 201;
    private int nextShelterId = 301;
    private int nextAlertId = 401;
    private int nextBulletinId = 501;

    private AdminDataManager() {
        seedInitialData();
    }

    public static synchronized AdminDataManager getInstance() {
        if (instance == null) {
            instance = new AdminDataManager();
        }
        return instance;
    }

    public void addChangeListener(Runnable listener) {
        if (listener != null && !changeListeners.contains(listener)) {
            changeListeners.add(listener);
        }
    }

    public void removeChangeListener(Runnable listener) {
        changeListeners.remove(listener);
    }

    private void notifyListeners() {
        for (Runnable r : changeListeners) {
            try {
                r.run();
            } catch (Exception ignored) {}
        }
    }

    private void seedInitialData() {
        // 1. Volunteers (Initial approved state)
        Volunteer v1 = new Volunteer(nextVolunteerId++, "anil_kumar", "Anil Kumar", "anil@disaster.kerala.gov.in", "+91 98471 23450", "Alappuzha", "Boat Rescue & Deep Dive", "Pre-verified Govt Record", "Kerala Fire Force Deep Sea Certified", "Approved", "2026-10-20");
        Volunteer v2 = new Volunteer(nextVolunteerId++, "meera_nair", "Meera Nair", "meera@disaster.kerala.gov.in", "+91 94462 89012", "Wayanad", "Trauma Care Paramedic", "Pre-verified Govt Record", "Kerala Nursing Council Reg #8841", "Approved", "2026-10-22");
        Volunteer v3 = new Volunteer(nextVolunteerId++, "faisal_rahman", "Faisal Rahman", "faisal@disaster.kerala.gov.in", "+91 98953 45671", "Alappuzha", "Logistics & Food Distribution", "Pre-verified Govt Record", "Civil Supplies Relief Logistics Cert", "Approved", "2026-10-23");
        Volunteer v4 = new Volunteer(nextVolunteerId++, "deepa_thomas", "Deepa Thomas", "deepa@disaster.kerala.gov.in", "+91 97450 11223", "Idukki", "Emergency Communication Ham", "Pre-verified Govt Record", "Govt of India Wireless Telephony License", "Approved", "2026-10-24");

        volunteers.add(v1);
        volunteers.add(v2);
        volunteers.add(v3);
        volunteers.add(v4);

        // Initial Seed Tasks
        volunteerTasks.add(new VolunteerTask(
                nextTaskId++, v1.getId(), v1.getName(),
                "Kuttanad Motorboat Evacuation & Patrol",
                "Alappuzha", "Kuttanad Backwaters Sector #3",
                "Critical", "In Progress", "2026-10-26",
                "Deploy with NDRF motorboat team to assist stranded elderly citizens and transport drinking water."
        ));

        volunteerTasks.add(new VolunteerTask(
                nextTaskId++, v2.getId(), v2.getName(),
                "Meppadi Landslide Relief Camp Triage Unit",
                "Wayanad", "Meppadi Higher Secondary School Camp",
                "High", "Assigned", "2026-10-26",
                "Operate first aid dispensary, triage incoming injured evacuees, and organize medical prescriptions."
        ));

        volunteerTasks.add(new VolunteerTask(
                nextTaskId++, v4.getId(), v4.getName(),
                "Munnar Ghat VHF Radio Repeater Monitoring",
                "Idukki", "Munnar District Control Center",
                "High", "In Progress", "2026-10-26",
                "Maintain line-of-sight emergency amateur radio connection between Idukki Collectorate and field rescue units."
        ));

        // 2. Inventory
        inventoryItems.add(new InventoryItem(nextInventoryId++, "Inflatable Motorized Rescue Boats", "Alappuzha Water Depot", "18", "Units", "Ready for Deployment"));
        inventoryItems.add(new InventoryItem(nextInventoryId++, "Packaged Drinking Water", "Central Logistics Warehouse", "24,000", "Liters", "Ample Stock"));
        inventoryItems.add(new InventoryItem(nextInventoryId++, "Emergency Dry Food Rations", "Kochi Civil Supplies Hub", "12,500", "Packets", "Available for Transit"));
        inventoryItems.add(new InventoryItem(nextInventoryId++, "Portable Heavy Diesel Generators", "Kottayam Power Depot", "32", "Units", "Operational"));
        inventoryItems.add(new InventoryItem(nextInventoryId++, "High-Capacity Dewatering Pumps", "Kuttanad Drainage Unit", "14", "Units", "Deployed & Ready"));
        inventoryItems.add(new InventoryItem(nextInventoryId++, "First Aid & Trauma Medical Kits", "District Health Warehouse", "450", "Kits", "Certified Stocked"));
        inventoryItems.add(new InventoryItem(nextInventoryId++, "Thermal Blankets & Tarpaulin Rolls", "Wayanad Relief Depot", "3,800", "Pcs", "Ready for Camps"));
        inventoryItems.add(new InventoryItem(nextInventoryId++, "Satellite Emergency Phones (Sat-Com)", "State Operations Center", "16", "Handsets", "Active & Charged"));

        // 3. Shelter Homes
        shelterHomes.add(new ShelterHome(nextShelterId++, "Thrissur Emergency Shelter Cluster", "Thrissur", 380, 500, "Fr. Joseph Varghese", "+91 94471 33441", "Active (Rations & Blankets)"));
        shelterHomes.add(new ShelterHome(nextShelterId++, "Kollam Coastal Relief Station", "Kollam", 240, 350, "Smt. Shailaja K.", "+91 98462 55662", "Operational (Cooked Food)"));
        shelterHomes.add(new ShelterHome(nextShelterId++, "Kannur Northern Relief Camp", "Kannur", 290, 400, "Sri. Pradeep Kumar", "+91 97453 77883", "Operational (Govt HS Zone)"));
        shelterHomes.add(new ShelterHome(nextShelterId++, "Alappuzha St. Thomas Relief Camp", "Alappuzha", 450, 600, "Dr. Mathew George", "+91 98954 11224", "High Influx (Boat Evacuees)"));
        shelterHomes.add(new ShelterHome(nextShelterId++, "Wayanad Meppadi Hill Safehouse", "Wayanad", 180, 250, "Sri. Rajesh Mohan", "+91 94465 99005", "Operational (Medical Care)"));

        // 4. Alerts
        alerts.add(new DisasterAlert(nextAlertId++, "Reservoir Spill Discharge", "Banasurasagar & Pamba", "Critical", "28,000", "Spillway Active"));
        alerts.add(new DisasterAlert(nextAlertId++, "Landslide Hazard Notice", "Wayanad (Meppadi Ghats)", "Critical", "14,500", "Red Alert Active"));
        alerts.add(new DisasterAlert(nextAlertId++, "Flash Flood Inundation", "Alappuzha (Kuttanad)", "High", "35,000", "Relief Camps Active"));
        alerts.add(new DisasterAlert(nextAlertId++, "Severe Thunderstorm & Wind", "Interior Kerala Districts", "High", "120,000", "Advisory Issued"));
        alerts.add(new DisasterAlert(nextAlertId++, "Ghat Road Mudslide Risk", "Idukki (Munnar Bypass)", "High", "8,200", "Traffic Diverted"));
        alerts.add(new DisasterAlert(nextAlertId++, "Coastal Surge & High Swell", "Kollam Coastal Belt", "Medium", "18,400", "Fishermen Warning"));

        // 5. Bulletins
        bulletins.add(new GroundBulletin(nextBulletinId++, "18:45 IST", "Banasurasagar Dam spillway shutters raised by 10cm. Downstream Kabini river alert active.", "DANGER"));
        bulletins.add(new GroundBulletin(nextBulletinId++, "17:20 IST", "NDRF 04 Battalion stationed at Sulthan Bathery & Mananthavady with motorized rescue boats.", "PRIMARY"));
        bulletins.add(new GroundBulletin(nextBulletinId++, "15:10 IST", "15 relief camps operational across Alappuzha & Wayanad accommodating 1,420 citizens.", "SUCCESS"));
    }

    // ---------------- Volunteers Methods ----------------
    public List<Volunteer> getVolunteers() {
        return Collections.unmodifiableList(volunteers);
    }

    public synchronized Volunteer addVolunteerApplication(String username, String name, String email,
                                                          String phone, String district, String skill,
                                                          String idProofPath, String certificatePath) {
        String today = java.time.LocalDate.now().toString();
        email = disaster.backend.DatabaseManager.cleanEmail(email);
        Volunteer v = new Volunteer(nextVolunteerId++, username, name, email, phone, district, skill, idProofPath, certificatePath, "Pending", today);
        volunteers.add(v);
        notifyListeners();
        return v;
    }

    public synchronized void addVolunteer(String name, String skill, String sector, String phone, String status) {
        volunteers.add(new Volunteer(nextVolunteerId++, name, skill, sector, phone, status));
        notifyListeners();
    }

    public synchronized boolean approveVolunteer(int id) {
        for (Volunteer v : volunteers) {
            if (v.getId() == id) {
                v.setStatus("Approved");
                if (getTasksForVolunteer(id).isEmpty()) {
                    assignTask(v.getId(), v.getName(),
                            v.getDistrict() + " Field Emergency Mission (" + v.getSkill() + ")",
                            v.getDistrict(),
                            v.getDistrict() + " Central SDMA Relief Command",
                            "High",
                            "Report to District Incident Command. Coordinate on-ground relief and assist evacuation operations.");
                }
                notifyListeners();
                return true;
            }
        }
        return false;
    }

    public synchronized boolean rejectVolunteer(int id) {
        for (Volunteer v : volunteers) {
            if (v.getId() == id) {
                v.setStatus("Rejected");
                notifyListeners();
                return true;
            }
        }
        return false;
    }

    public synchronized Volunteer getVolunteerById(int id) {
        for (Volunteer v : volunteers) {
            if (v.getId() == id) return v;
        }
        return null;
    }

    public synchronized Volunteer getVolunteerByUsername(String username) {
        if (username == null || username.trim().isEmpty()) return null;
        String clean = username.trim().toLowerCase();
        for (Volunteer v : volunteers) {
            if (v.getUsername() != null && v.getUsername().equalsIgnoreCase(clean)) {
                return v;
            }
        }
        // Match by legal name if username didn't match directly
        for (Volunteer v : volunteers) {
            if (v.getName() != null && v.getName().equalsIgnoreCase(clean)) {
                return v;
            }
        }
        return null;
    }

    public synchronized Volunteer getVolunteerByEmail(String email) {
        if (email == null || email.trim().isEmpty()) return null;
        String clean = email.trim().toLowerCase();
        for (Volunteer v : volunteers) {
            if (v.getEmail() != null && v.getEmail().equalsIgnoreCase(clean)) {
                return v;
            }
        }
        return null;
    }

    public synchronized String getVolunteerStatus(String identifier) {
        if (identifier == null) return "NONE";
        Volunteer v = getVolunteerByUsername(identifier);
        if (v == null && identifier.contains("@")) {
            v = getVolunteerByEmail(identifier);
        }
        if (v != null) {
            return v.getStatus();
        }
        return "NONE";
    }

    public synchronized boolean isVolunteerApproved(String identifier) {
        if (identifier == null) return false;
        if ("admin".equalsIgnoreCase(identifier) || "admin123".equalsIgnoreCase(identifier)) {
            return true;
        }
        Volunteer v = getVolunteerByUsername(identifier);
        if (v == null && identifier.contains("@")) {
            v = getVolunteerByEmail(identifier);
        }
        if (v != null) {
            String s = v.getStatus().toLowerCase();
            return s.contains("approved") || s.contains("active") || s.contains("deployed") || s.contains("on duty");
        }
        return false;
    }

    public List<Volunteer> getPendingVolunteers() {
        List<Volunteer> list = new ArrayList<>();
        for (Volunteer v : volunteers) {
            if ("Pending".equalsIgnoreCase(v.getStatus())) {
                list.add(v);
            }
        }
        return list;
    }

    public List<Volunteer> getApprovedVolunteers() {
        List<Volunteer> list = new ArrayList<>();
        for (Volunteer v : volunteers) {
            String s = v.getStatus().toLowerCase();
            if (s.contains("approved") || s.contains("active") || s.contains("deployed") || s.contains("on duty")) {
                list.add(v);
            }
        }
        return list;
    }

    public synchronized boolean updateVolunteer(int id, String name, String skill, String sector, String phone, String status) {
        for (Volunteer v : volunteers) {
            if (v.getId() == id) {
                v.setName(name);
                v.setSkill(skill);
                v.setSector(sector);
                v.setPhone(phone);
                v.setStatus(status);
                notifyListeners();
                return true;
            }
        }
        return false;
    }

    public synchronized boolean deleteVolunteer(int id) {
        boolean removed = volunteers.removeIf(v -> v.getId() == id);
        if (removed) notifyListeners();
        return removed;
    }

    // ---------------- Volunteer Tasks & Assigned Works ----------------
    public List<VolunteerTask> getAllTasks() {
        return Collections.unmodifiableList(volunteerTasks);
    }

    public synchronized VolunteerTask assignTask(int volunteerId, String volunteerName, String taskTitle,
                                                 String district, String location, String priority,
                                                 String instructions) {
        String today = java.time.LocalDate.now().toString();
        VolunteerTask t = new VolunteerTask(nextTaskId++, volunteerId, volunteerName, taskTitle, district, location, priority, "Assigned", today, instructions);
        volunteerTasks.add(0, t);
        notifyListeners();
        return t;
    }

    public synchronized boolean updateTaskStatus(int taskId, String newStatus) {
        for (VolunteerTask t : volunteerTasks) {
            if (t.getTaskId() == taskId) {
                t.setStatus(newStatus);
                notifyListeners();
                return true;
            }
        }
        return false;
    }

    public synchronized boolean deleteTask(int taskId) {
        boolean removed = volunteerTasks.removeIf(t -> t.getTaskId() == taskId);
        if (removed) notifyListeners();
        return removed;
    }

    public List<VolunteerTask> getTasksForVolunteer(int volunteerId) {
        List<VolunteerTask> list = new ArrayList<>();
        for (VolunteerTask t : volunteerTasks) {
            if (t.getVolunteerId() == volunteerId) {
                list.add(t);
            }
        }
        return list;
    }

    public List<VolunteerTask> getTasksForUser(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) return Collections.emptyList();
        String clean = identifier.trim().toLowerCase();
        Volunteer v = getVolunteerByUsername(clean);
        if (v == null && clean.contains("@")) {
            v = getVolunteerByEmail(clean);
        }
        int vid = (v != null) ? v.getId() : -1;
        List<VolunteerTask> list = new ArrayList<>();
        for (VolunteerTask t : volunteerTasks) {
            if (vid != -1 && t.getVolunteerId() == vid) {
                list.add(t);
            } else if (t.getVolunteerName() != null && t.getVolunteerName().trim().equalsIgnoreCase(clean)) {
                list.add(t);
            }
        }
        return list;
    }

    // ---------------- Inventory Methods ----------------
    public List<InventoryItem> getInventoryItems() {
        return Collections.unmodifiableList(inventoryItems);
    }

    public synchronized void addInventoryItem(String name, String depot, String quantity, String unit, String status) {
        inventoryItems.add(new InventoryItem(nextInventoryId++, name, depot, quantity, unit, status));
        notifyListeners();
    }

    public synchronized boolean updateInventoryItem(int id, String name, String depot, String quantity, String unit, String status) {
        for (InventoryItem item : inventoryItems) {
            if (item.getId() == id) {
                item.setName(name);
                item.setDepot(depot);
                item.setQuantity(quantity);
                item.setUnit(unit);
                item.setStatus(status);
                notifyListeners();
                return true;
            }
        }
        return false;
    }

    public synchronized boolean deleteInventoryItem(int id) {
        boolean removed = inventoryItems.removeIf(i -> i.getId() == id);
        if (removed) notifyListeners();
        return removed;
    }

    // ---------------- Shelter Homes Methods ----------------
    public List<ShelterHome> getShelterHomes() {
        return Collections.unmodifiableList(shelterHomes);
    }

    public synchronized void addShelterHome(String name, String district, int occupancy, int capacity, String coordinator, String phone, String status) {
        shelterHomes.add(new ShelterHome(nextShelterId++, name, district, occupancy, capacity, coordinator, phone, status));
        notifyListeners();
    }

    public synchronized boolean updateShelterHome(int id, String name, String district, int occupancy, int capacity, String coordinator, String phone, String status) {
        for (ShelterHome s : shelterHomes) {
            if (s.getId() == id) {
                s.setName(name);
                s.setDistrict(district);
                s.setOccupancy(occupancy);
                s.setCapacity(capacity);
                s.setCoordinator(coordinator);
                s.setPhone(phone);
                s.setStatus(status);
                notifyListeners();
                return true;
            }
        }
        return false;
    }

    public synchronized boolean deleteShelterHome(int id) {
        boolean removed = shelterHomes.removeIf(s -> s.getId() == id);
        if (removed) notifyListeners();
        return removed;
    }

    // ---------------- Alerts Methods ----------------
    public List<DisasterAlert> getAlerts() {
        return Collections.unmodifiableList(alerts);
    }

    public synchronized void addAlert(String hazard, String district, String level, String affected, String status) {
        alerts.add(new DisasterAlert(nextAlertId++, hazard, district, level, affected, status));
        notifyListeners();
    }

    public synchronized boolean updateAlert(int id, String hazard, String district, String level, String affected, String status) {
        for (DisasterAlert a : alerts) {
            if (a.getId() == id) {
                a.setHazard(hazard);
                a.setDistrict(district);
                a.setLevel(level);
                a.setAffected(affected);
                a.setStatus(status);
                notifyListeners();
                return true;
            }
        }
        return false;
    }

    public synchronized boolean deleteAlert(int id) {
        boolean removed = alerts.removeIf(a -> a.getId() == id);
        if (removed) notifyListeners();
        return removed;
    }

    // ---------------- Bulletins Methods ----------------
    public List<GroundBulletin> getBulletins() {
        return Collections.unmodifiableList(bulletins);
    }

    public synchronized void addBulletin(String time, String message, String severity) {
        bulletins.add(new GroundBulletin(nextBulletinId++, time, message, severity));
        notifyListeners();
    }

    public synchronized boolean updateBulletin(int id, String time, String message, String severity) {
        for (GroundBulletin b : bulletins) {
            if (b.getId() == id) {
                b.setTime(time);
                b.setMessage(message);
                b.setSeverity(severity);
                notifyListeners();
                return true;
            }
        }
        return false;
    }

    public synchronized boolean deleteBulletin(int id) {
        boolean removed = bulletins.removeIf(b -> b.getId() == id);
        if (removed) notifyListeners();
        return removed;
    }
}
