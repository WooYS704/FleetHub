-- ====================================================================
-- PROJECT SPECIFICATIONS DATABASE SCRIPT: transit_db.sql
-- Description: Creates 4 relational tables for the Transit System.
-- Includes support for inheritance mapping, soft delete, and analytics.
-- ====================================================================

CREATE DATABASE IF NOT EXISTS transit_db;
USE transit_db;

-- Drop tables if they exist to allow clean re-running of the script
DROP TABLE IF EXISTS operational_logs;
DROP TABLE IF EXISTS train_details;
DROP TABLE IF EXISTS bus_details;
DROP TABLE IF EXISTS vehicles;

-- --------------------------------------------------------------------
-- TABLE 1: Base Vehicle Table
-- Handles core properties shared across subclasses and the Soft Delete flag.
-- --------------------------------------------------------------------
CREATE TABLE vehicles (
    vehicle_id VARCHAR(50) NOT NULL,
    type VARCHAR(20) NOT NULL,
    capacity INT NOT NULL,
    status VARCHAR(50) NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (vehicle_id)
) ENGINE=InnoDB;

-- --------------------------------------------------------------------
-- TABLE 2: Bus Subclass Details
-- Maps Bus-specific fields back to the parent vehicles table.
-- --------------------------------------------------------------------
CREATE TABLE bus_details (
    vehicle_id VARCHAR(50) NOT NULL,
    route_number VARCHAR(50) NOT NULL,
    driver_name VARCHAR(50) NOT NULL,
    PRIMARY KEY (vehicle_id),
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id) 
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- --------------------------------------------------------------------
-- TABLE 3: Train Subclass Details
-- Maps Train-specific fields back to the parent vehicles table.
-- --------------------------------------------------------------------
CREATE TABLE train_details (
    vehicle_id VARCHAR(50) NOT NULL,
    line_name VARCHAR(50) NOT NULL,
    coach_count INT NOT NULL,
    PRIMARY KEY (vehicle_id),
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id) 
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- --------------------------------------------------------------------
-- TABLE 4: Operational Metrics Logs
-- Satisfies the 4th table requirement and feeds the Calculation Analytics Module.
-- --------------------------------------------------------------------
CREATE TABLE operational_logs (
    log_id INT AUTO_INCREMENT NOT NULL,
    vehicle_id VARCHAR(50) NOT NULL,
    efficiency_score INT NOT NULL, 
    log_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (log_id),
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id) 
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;


-- ====================================================================
-- SAMPLE SEED DATA INSERTIONS (For Testing & Lecturer Evaluation)
-- ====================================================================

-- Insert Sample Buses
INSERT INTO vehicles (vehicle_id, type, capacity, status, is_deleted) 
VALUES ('B-101', 'Bus', 45, 'Active', FALSE);
INSERT INTO bus_details (vehicle_id, route_number, driver_name) 
VALUES ('B-101', 'Route 14', 'John Smith');
INSERT INTO operational_logs (vehicle_id, efficiency_score) 
VALUES ('B-101', 85);

INSERT INTO vehicles (vehicle_id, type, capacity, status, is_deleted) 
VALUES ('B-102', 'Bus', 30, 'Under Maintenance', FALSE);
INSERT INTO bus_details (vehicle_id, route_number, driver_name) 
VALUES ('B-102', 'Route 404', 'Sarah Jenkins');
INSERT INTO operational_logs (vehicle_id, efficiency_score) 
VALUES ('B-102', 40);

-- Insert Sample Trains
INSERT INTO vehicles (vehicle_id, type, capacity, status, is_deleted) 
VALUES ('T-202', 'Train', 240, 'Active', FALSE);
INSERT INTO train_details (vehicle_id, line_name, coach_count) 
VALUES ('T-202', 'Blue Line', 6);
INSERT INTO operational_logs (vehicle_id, efficiency_score) 
VALUES ('T-202', 92);

INSERT INTO vehicles (vehicle_id, type, capacity, status, is_deleted) 
VALUES ('T-203', 'Train', 320, 'Active', FALSE);
INSERT INTO train_details (vehicle_id, line_name, coach_count) 
VALUES ('T-203', 'Red Line', 8);
INSERT INTO operational_logs (vehicle_id, efficiency_score) 
VALUES ('T-203', 95);

-- Insert a Pre-Soft-Deleted Entry (Will be safely hidden from display list view)
INSERT INTO vehicles (vehicle_id, type, capacity, status, is_deleted) 
VALUES ('B-999', 'Bus', 50, 'Archived', TRUE);
INSERT INTO bus_details (vehicle_id, route_number, driver_name) 
VALUES ('B-999', 'Route Old', 'Retired Driver');
INSERT INTO operational_logs (vehicle_id, efficiency_score) 
VALUES ('B-999', 0);

-- ====================================================================
-- Verify Data Structure Script (Optional test block)
-- ====================================================================
-- SELECT * FROM vehicles WHERE is_deleted = FALSE;