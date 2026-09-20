-- ============================================================================
-- SMART DISASTER MANAGEMENT SYSTEM (SDRP) - XAMPP MySQL / MariaDB Schema
-- Compatible with XAMPP MySQL (Port 3306) & phpMyAdmin
-- ============================================================================

CREATE DATABASE IF NOT EXISTS `sdrp_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `sdrp_db`;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS `users` (
    `user_id` VARCHAR(36) PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `email` VARCHAR(100) NOT NULL UNIQUE,
    `phone` VARCHAR(20) DEFAULT NULL,
    `password_hash` VARCHAR(255) NOT NULL,
    `user_type` VARCHAR(20) NOT NULL DEFAULT 'VICTIM',
    `latitude` DOUBLE DEFAULT 0.0,
    `longitude` DOUBLE DEFAULT 0.0,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `is_active` BOOLEAN DEFAULT TRUE,
    INDEX `idx_users_username` (`username`),
    INDEX `idx_users_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. User Profiles Table
CREATE TABLE IF NOT EXISTS `user_profiles` (
    `profile_id` VARCHAR(36) PRIMARY KEY,
    `user_id` VARCHAR(36) NOT NULL,
    `full_name` VARCHAR(100) DEFAULT NULL,
    `blood_group` VARCHAR(10) DEFAULT NULL,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. SOS Alerts Table
CREATE TABLE IF NOT EXISTS `sos_alerts` (
    `sos_id` VARCHAR(36) PRIMARY KEY,
    `user_id` VARCHAR(36) NOT NULL,
    `latitude` DOUBLE NOT NULL,
    `longitude` DOUBLE NOT NULL,
    `urgency_level` VARCHAR(20) NOT NULL DEFAULT 'CRITICAL',
    `description` TEXT NOT NULL,
    `status` VARCHAR(20) DEFAULT 'ACTIVE',
    `responders_count` INT DEFAULT 0,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_sos_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. SOS Responses Table
CREATE TABLE IF NOT EXISTS `sos_responses` (
    `response_id` VARCHAR(36) PRIMARY KEY,
    `sos_id` VARCHAR(36) NOT NULL,
    `responder_id` VARCHAR(36) NOT NULL,
    `status` VARCHAR(20) DEFAULT 'RESPONDING',
    `responded_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`sos_id`) REFERENCES `sos_alerts`(`sos_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Disaster Incident Reports Table
CREATE TABLE IF NOT EXISTS `disaster_reports` (
    `report_id` VARCHAR(36) PRIMARY KEY,
    `user_id` VARCHAR(36) NOT NULL,
    `disaster_type` VARCHAR(30) NOT NULL,
    `latitude` DOUBLE NOT NULL,
    `longitude` DOUBLE NOT NULL,
    `severity` VARCHAR(20) NOT NULL,
    `description` TEXT NOT NULL,
    `status` VARCHAR(20) DEFAULT 'PENDING',
    `affected_people` INT DEFAULT 0,
    `verified_by` VARCHAR(36) DEFAULT NULL,
    `verified_at` TIMESTAMP NULL DEFAULT NULL,
    `reported_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Alerts Broadcast Table
CREATE TABLE IF NOT EXISTS `alerts` (
    `alert_id` VARCHAR(36) PRIMARY KEY,
    `type` VARCHAR(30) NOT NULL,
    `message` TEXT NOT NULL,
    `priority` VARCHAR(20) NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `is_acknowledged` BOOLEAN DEFAULT FALSE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Initial Seed Data
INSERT IGNORE INTO `users` (`user_id`, `username`, `email`, `phone`, `password_hash`, `user_type`, `latitude`, `longitude`, `is_active`)
VALUES
('user-001', 'john_responder', 'john@example.com', '+91 98470 12345', '8d969eef6ecad3c29a3a873fba5b4cd9fdb20ac89f3ccc36fafeb4f0f79c1be', 'RESPONDER', 9.9312, 76.2673, 1),
('user-002', 'sarah_victim', 'sarah@example.com', '+91 98470 54321', '8d969eef6ecad3c29a3a873fba5b4cd9fdb20ac89f3ccc36fafeb4f0f79c1be', 'VICTIM', 9.9816, 76.2999, 1),
('user-003', 'admin_user', 'admin@example.com', '+91 98470 99999', '8d969eef6ecad3c29a3a873fba5b4cd9fdb20ac89f3ccc36fafeb4f0f79c1be', 'ADMIN', 9.9312, 76.2673, 1);

INSERT IGNORE INTO `user_profiles` (`profile_id`, `user_id`, `full_name`, `blood_group`)
VALUES
('prof-001', 'user-001', 'Officer John Smith', 'O+'),
('prof-002', 'user-002', 'Sarah Varghese', 'B+');

INSERT IGNORE INTO `sos_alerts` (`sos_id`, `user_id`, `latitude`, `longitude`, `urgency_level`, `description`, `status`, `responders_count`)
VALUES
('sos-001', 'user-002', 9.9650, 76.2420, 'CRITICAL', 'Person trapped in flooded area near Kochi port', 'ACTIVE', 1),
('sos-002', 'user-001', 10.0261, 76.3125, 'HIGH', 'Medical evacuation needed at Aluva relief center', 'ACTIVE', 0);

INSERT IGNORE INTO `disaster_reports` (`report_id`, `user_id`, `disaster_type`, `latitude`, `longitude`, `severity`, `description`, `status`, `affected_people`)
VALUES
('report-001', 'user-002', 'FLOOD', 9.9816, 76.2999, 'HIGH', 'Heavy river inundation along Periyar basin', 'PENDING', 240);
