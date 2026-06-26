========================================================================
         TRANSIT NETWORK OPERATIONS & ANALYTICS MANAGEMENT SYSTEM
========================================================================
Course Assignment: Object-Oriented Programming (OOP) Project
Architecture: Model-View-Controller (MVC) Framework
Backend Database: MySQL via XAMPP Local Server Server Configuration
Interface Type: Java Swing Desktop Graphical User Interface (GUI)

------------------------------------------------------------------------
1. PROJECT STRUCTURE OVERVIEW
------------------------------------------------------------------------
The system codebase strictly follows the architectural separation of concerns 
using the MVC design pattern across designated packages:

src/
├── model/
│   ├── Vehicle.java            <- Base model entity containing common properties
│   ├── Bus.java                <- Inherited subclass handling Route/Driver metrics
│   ├── Train.java              <- Inherited subclass handling Line/Coach metrics
│   └── DatabaseConnection.java <- Manages physical MySQL database connections
│
├── view/
│   ├── MainFrame.java          <- Builds Java Swing UI windows, forms, and grid tables
│   └── MainApp.java            <- System entry point running safely on the EDT thread
│
└── controller/
    └── VehicleController.java  <- Intercepts actions, validates input forms, and 
                                   handles database CRUD transaction queries safely.

Outside Source Folder:
├── transit_db.sql              <- Complete schema structural table creation script
└── README.txt                  <- System configuration documentation ledger

------------------------------------------------------------------------
2. PRE-REQUISITES & ENVIRONMENT SETUP
------------------------------------------------------------------------
Before compiling and running the application, ensure the following environment 
components are active:

1. Java Development Kit (JDK): JDK 11 or higher recommended.
2. XAMPP Server Module: Downloaded and installed (contains local Apache/MySQL tools).
3. MySQL Connector/J Driver: The `mysql-connector-j-8.x.x.jar` archive library file 
   must be linked into your IDE build classpath configuration.

------------------------------------------------------------------------
3. STEP-BY-STEP SETUP & EVALUATION INSTRUCTIONS
------------------------------------------------------------------------

STEP 1: LAUNCH THE LOCAL DATABASE ENVIRONMENT
1. Launch the 'XAMPP Control Panel' application desktop app interface.
2. Locate the row labeled 'MySQL' and click the 'Start' button.
3. Ensure the 'MySQL' indicator label highlights green on port '3306'.

STEP 2: IMPORT THE DATABASE SCHEMA (TABLE SCHEMAS)
1. Open your web browser and navigate to: http://localhost/phpmyadmin
2. Along the sidebar navigation tree, click 'New' to initialize a database container.
3. Define the database name exactly as: transit_db
4. Select the newly generated 'transit_db' from the list, then click the 'Import' tab.
5. Click 'Choose File', navigate to your submission directory, and pick 'transit_db.sql'.
6. Scroll down to the bottom right and click the 'Go' button.
7. Verification: The tool will parse 4 tables: `vehicles`, `bus_details`, 
   `train_details`, and `operational_logs` along with automated seed test records.

STEP 3: RUNNING THE JAVA APPLICATION ENGINE
1. Import the project root project folder space into your preferred IDE (Eclipse/IntelliJ).
2. Right-click your project directory -> Build Path -> Configure Build Path.
3. Under the 'Libraries' tab, choose 'Classpath', then select 'Add External JARs'.
4. Bind your downloaded 'mysql-connector-j-8.x.x.jar' driver library, then apply.
5. Open up the source package tracking down: view -> MainApp.java
6. Right-click 'MainApp.java' and run it as a standard Java Application.

------------------------------------------------------------------------
4. ADVANCED SYSTEM HIGHLIGHTS FOR EVALUATION
------------------------------------------------------------------------
• Fault-Tolerant Input Parsing: Try-catch validation locks intercept form queries, 
  gracefully displaying dialog alerts for empty fields or string format parsing 
  errors (e.g. typing characters into seat count fields) instead of crashing.
• Dual Inheritance Subclass Support: Switching the 'Vehicle Type' dropdown 
  automatically switches form layout field contextual tracking for Buses vs Trains.
• Production Soft Deletion: Clicking 'Soft Delete' on a selected record removes the 
  asset from the GUI inventory view table instantly. In the backend, its `is_deleted` 
  status flag changes to TRUE, preserving operational logging rows.
• Embedded Analytics Operations: Clicking 'Calculate Fleet Metrics' triggers active 
  SQL aggregate summaries highlighting total asset counts, global capacity volumes, 
  and real-time fleet load averages dynamically.
========================================================================