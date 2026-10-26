package disaster.service;

import java.util.*;

/**
 * Standardized data catalog for Kerala Districts and Multi-Disciplinary
 * Emergency Volunteer Skillsets and Roles.
 */
public class VolunteerSkillsData {

    public static final String[] KERALA_DISTRICTS = {
            "Alappuzha",
            "Ernakulam",
            "Idukki",
            "Kannur",
            "Kasaragod",
            "Kollam",
            "Kottayam",
            "Kozhikode",
            "Malappuram",
            "Palakkad",
            "Pathanamthitta",
            "Thiruvananthapuram",
            "Thrissur",
            "Wayanad"
    };

    public static final String[] CATEGORIES = {
            "Medical & Health",
            "Search, Rescue & Field Operations",
            "Logistics, Transport & Engineering",
            "Communications & Administration",
            "Security & Traffic Control",
            "Community Care & Specialized Support",
            "Trades, Repair & Sanitation",
            "Planning, Data & Admin"
    };

    private static final Map<String, List<String>> SKILLS_MAP = new LinkedHashMap<>();

    static {
        SKILLS_MAP.put("Medical & Health", Arrays.asList(
                "Basic First Aid",
                "CPR / AED Certified",
                "EMT / Paramedic",
                "Registered Nurse (RN) / Doctor",
                "Psychological First Aid / Crisis Counseling",
                "Triage Management"
        ));

        SKILLS_MAP.put("Search, Rescue & Field Operations", Arrays.asList(
                "Urban Search and Rescue (USAR)",
                "Swiftwater / Flood Rescue",
                "Firefighting Experience",
                "Drone / UAV Operator",
                "Debris Clearance / Chainsaw Operation",
                "Animal / Pet Rescue"
        ));

        SKILLS_MAP.put("Logistics, Transport & Engineering", Arrays.asList(
                "Heavy Machinery Operator (Excavator, Crane)",
                "Commercial Driver's License (CDL) / Truck Driving",
                "Supply Chain / Inventory Management",
                "Structural Engineering / Damage Assessment",
                "Shelter Setup and Management",
                "Food & Water Distribution"
        ));

        SKILLS_MAP.put("Communications & Administration", Arrays.asList(
                "Ham / Amateur Radio Operator",
                "Multilingual / Translator",
                "IT / Emergency Network Setup",
                "Dispatch / Call Center Operations",
                "Volunteer Coordination"
        ));

        SKILLS_MAP.put("Security & Traffic Control", Arrays.asList(
                "Crowd Control Management",
                "Traffic Routing / Flagger",
                "Security Officer / Law Enforcement Background",
                "Evacuation Coordination"
        ));

        SKILLS_MAP.put("Community Care & Specialized Support", Arrays.asList(
                "Childcare / Pediatric Support",
                "Elderly Care Assistance",
                "Special Needs / Disability Support",
                "Sign Language Interpreter",
                "Mass Feeding / Commercial Cooking"
        ));

        SKILLS_MAP.put("Trades, Repair & Sanitation", Arrays.asList(
                "Electrician / Power Restoration",
                "Plumber / Water Sanitation",
                "Generator Maintenance & Repair",
                "Off-Road (4x4) Vehicle Operation",
                "Boat / Watercraft Operation",
                "Carpentry / Temporary Shelter Construction"
        ));

        SKILLS_MAP.put("Planning, Data & Admin", Arrays.asList(
                "GIS / Mapping Specialist (for tracking SOS clusters)",
                "Data Entry & Record Keeping",
                "Hazmat (Hazardous Materials) Handling",
                "Legal Assistance / Claims Help"
        ));
    }

    public static List<String> getSkillsForCategory(String category) {
        List<String> list = SKILLS_MAP.get(category);
        if (list == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(list);
    }

    public static Map<String, List<String>> getAllSkillsMap() {
        return Collections.unmodifiableMap(SKILLS_MAP);
    }

    public static class SkillEntry {
        private final String category;
        private final String skillName;

        public SkillEntry(String category, String skillName) {
            this.category = category;
            this.skillName = skillName;
        }

        public String getCategory() { return category; }
        public String getSkillName() { return skillName; }
    }

    public static List<SkillEntry> getAllSkillsFlattened() {
        List<SkillEntry> entries = new ArrayList<>();
        for (Map.Entry<String, List<String>> e : SKILLS_MAP.entrySet()) {
            for (String skill : e.getValue()) {
                entries.add(new SkillEntry(e.getKey(), skill));
            }
        }
        return entries;
    }
}
