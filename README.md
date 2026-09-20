# Smart Disaster Management System (SDMA)

An enterprise-grade, real-time Smart Disaster Management Portal engineered with Java Swing and multi-tiered database synchronization (MySQL / Embedded H2 / SDRP Backend). Built to empower rapid disaster response, citizen emergency SOS dispatch, volunteer coordination, logistics tracking, and administrative governance.

---

## 🌟 Key Features

### 1. Citizen & Emergency Operations Portal
- **🔴 Emergency SOS Dispatch**: Instant one-touch distress call broadcast with real-time GPS telemetry and triage urgency levels.
- **🗺️ Kerala Live Relief Map**: Interactive canvas rendering regional disaster pins, flood spill zones, safe shelter clusters, and live telemetry coordinates.
- **📋 Incident Reporting**: Citizen crowd-sourced incident submission with category tagging, location logging, and impact assessment.
- **📢 Real-Time Ground Bulletins**: Live ticker broadcasting official state meteorological alerts, dam discharge warnings, and evacuation notices.
- **🚨 State Emergency Hotlines**: 24/7 direct access to National Emergency (112), District Control (1077), State Ops Center (1070), Ambulance (108), and Fire & Rescue (101).

### 2. Dedicated Admin Panel
- **Authorized Credentials**: Protected administration access.
  - **Username**: `admin123`
  - **Password**: `admin@123`
- **Re-Edit & Operational Control**:
  - 🏕️ **Relief Shelter Homes**: Monitor occupancy, add shelter zones, edit capacities, contact coordinators, or reallocate resources.
  - 👥 **Certified Volunteers Roster**: Deploy field responders, update duty stations, and assign specialties (Paramedic, Boat Rescue, Comms Ham).
  - 📦 **Warehouse Supplies & Depot Logistics**: Track stockpile counts (Rescue Boats, Medical Kits, Packaged Water, Dewatering Pumps, Generators), adjust supplies, and update depot readiness.
  - ⚠️ **Disaster Alerts Bulletin**: Issue live warnings, set alert severity levels (Critical, High, Medium), and revoke expired notices.
  - 📢 **Ground Intelligence Bulletins**: Broadcast real-time ground bulletins directly to the portal ticker.

### 3. High-Reliability Dual Database Architecture
- **Primary Engine**: XAMPP MySQL (`disaster_db` on default port `3306`).
- **Resilience Engine**: Seamless automatic fallback to persistent local storage when XAMPP is offline.
- **REST Telemetry**: Synchronized with SDRP REST API service on port `8085`.

---

## 🚀 Getting Started

### Prerequisites
- **Java Development Kit (JDK)**: JDK 17, 21, or higher.
- **MySQL (Optional)**: XAMPP Control Panel with MySQL running on port 3306.

### Compiling and Running

#### 1. Compile the project
```bash
javac -cp "lib/*;src/main/java" -d out/production/smart_disaster_management_system $(Get-ChildItem -Recurse -Filter *.java src/main/java | Select-Object -ExpandProperty FullName)
```

#### 2. Run the application
```bash
java -cp "out/production/smart_disaster_management_system;lib/*" disaster.Main
```

---

## 🔐 Credentials Reference

| Role | Username | Password | Access Level |
| :--- | :--- | :--- | :--- |
| **System Administrator** | `admin123` | `admin@123` | Full Admin Panel & Live Re-edit Control |
| **Citizen Demo User** | `alvin_ms` | `Pass123456` | Citizen Responder Portal |
| **New Citizens** | *Self-registration* | *Configured on sign-up* | Full Resident Access & SOS Emergency |

---

## 📂 Project Architecture

```
smart disaster management system/
├── lib/
│   ├── mysql-connector-j.jar     # Official MySQL JDBC Driver
│   └── h2.jar                    # Embedded H2 Database Engine
├── src/main/java/disaster/
│   ├── Main.java                 # Main application entry point
│   ├── backend/
│   │   ├── DatabaseManager.java  # Dual-mode MySQL / H2 connection pool
│   │   └── SdrpServer.java       # Local telemetry & API listener
│   ├── dao/
│   │   ├── DBConnection.java     # Database connector utility
│   │   └── UserDAO.java          # User persistence & profile DAO
│   ├── model/
│   │   ├── DisasterReport.java   # Incident report entity
│   │   ├── SOSAlert.java         # SOS distress beacon model
│   │   └── User.java             # System user & credentials model
│   ├── service/
│   │   ├── AdminDataManager.java # Live synchronized state manager
│   │   ├── ApiClient.java        # REST API integration client
│   │   └── UserSession.java      # Current authenticated session state
│   └── ui/
│       ├── AdminFrame.java       # Dedicated Administrator Command Center
│       ├── AlertFrame.java       # Critical meteorological alerts
│       ├── BaseFrame.java        # Standardized UI layout & components
│       ├── DashboardFrame.java   # Main portal landing dashboard
│       ├── EmergencyContactsFrame.java # Helpline & safehouse registry
│       ├── GPSFrame.java         # Kerala relief map & GPS coordinate canvas
│       ├── IncidentReportFrame.java    # Citizen disaster reporting form
│       ├── InventoryFrame.java   # Warehouse supplies & depot registry
│       ├── LoginFrame.java       # Secure portal authentication
│       ├── ProfileFrame.java     # User profile & triage medical telemetry
│       ├── RegisterFrame.java    # New account registration
│       ├── SOSFrame.java         # High-priority emergency trigger
│       ├── UITheme.java          # Kerala SDMA unified design tokens
│       └── VolunteerFrame.java   # Certified responder deployment roster
├── .gitignore                    # Git ignore configuration
└── README.md                     # Documentation
```

---

## 📜 License
Developed for Emergency Disaster Response & Management.
