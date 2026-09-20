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
        private String name;
        private String skill;
        private String sector;
        private String phone;
        private String status;

        public Volunteer(int id, String name, String skill, String sector, String phone, String status) {
            this.id = id;
            this.name = name;
            this.skill = skill;
            this.sector = sector;
            this.phone = phone;
            this.status = status;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSkill() { return skill; }
        public void setSkill(String skill) { this.skill = skill; }
        public String getSector() { return sector; }
        public void setSector(String sector) { this.sector = sector; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
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
    private final List<InventoryItem> inventoryItems = new CopyOnWriteArrayList<>();
    private final List<ShelterHome> shelterHomes = new CopyOnWriteArrayList<>();
    private final List<DisasterAlert> alerts = new CopyOnWriteArrayList<>();
    private final List<GroundBulletin> bulletins = new CopyOnWriteArrayList<>();
    private final List<Runnable> changeListeners = new CopyOnWriteArrayList<>();

    private int nextVolunteerId = 101;
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
        // 1. Volunteers
        volunteers.add(new Volunteer(nextVolunteerId++, "Anil Kumar", "Boat Rescue & Deep Dive", "Alappuzha (Camp #1)", "+91 98471 23450", "Deployed"));
        volunteers.add(new Volunteer(nextVolunteerId++, "Meera Nair", "Trauma Care Paramedic", "Wayanad (Meppadi Camp)", "+91 94462 89012", "On Duty"));
        volunteers.add(new Volunteer(nextVolunteerId++, "Faisal Rahman", "Logistics & Food Distribution", "Kuttanad Relief Depot", "+91 98953 45671", "Active"));
        volunteers.add(new Volunteer(nextVolunteerId++, "Deepa Thomas", "Emergency Communication Ham", "Idukki District Control", "+91 97450 11223", "Deployed"));

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

    public synchronized void addVolunteer(String name, String skill, String sector, String phone, String status) {
        volunteers.add(new Volunteer(nextVolunteerId++, name, skill, sector, phone, status));
        notifyListeners();
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
