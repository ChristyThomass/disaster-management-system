-- ============================================================================
-- disaster_db  --  Clean 5-Table Schema
-- Matches: UserDAO, EmergencyContactDAO, VolunteerDAO, IncidentDAO, SOSDAO
-- Run this in phpMyAdmin to reset the schema from scratch.
-- ============================================================================

SET FOREIGN_KEY_CHECKS = 0;

USE `disaster_db`;

-- Drop legacy tables
DROP TABLE IF EXISTS `emergency_contacts`;
DROP TABLE IF EXISTS `gps_locations`;
DROP TABLE IF EXISTS `incident_media`;
DROP TABLE IF EXISTS `incident_reports`;
DROP TABLE IF EXISTS `notifications`;
DROP TABLE IF EXISTS `rescue_status`;
DROP TABLE IF EXISTS `sos_requests`;
DROP TABLE IF EXISTS `users`;
DROP TABLE IF EXISTS `user_profiles`;
DROP TABLE IF EXISTS `volunteers`;
DROP TABLE IF EXISTS `volunteer_tasks`;
DROP TABLE IF EXISTS `sos_alerts`;
DROP TABLE IF EXISTS `sos_responses`;
DROP TABLE IF EXISTS `disaster_reports`;
DROP TABLE IF EXISTS `alerts`;

-- 1. users
CREATE TABLE `users` (
    `id`         INT AUTO_INCREMENT PRIMARY KEY,
    `full_name`  VARCHAR(100)  NOT NULL,
    `email`      VARCHAR(100)  NOT NULL UNIQUE,
    `phone`      VARCHAR(20),
    `password`   VARCHAR(255)  NOT NULL,
    `role`       VARCHAR(30)   NOT NULL DEFAULT 'CITIZEN',
    `created_at` TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. emergency_contacts
CREATE TABLE `emergency_contacts` (
    `id`            INT AUTO_INCREMENT PRIMARY KEY,
    `user_id`       INT          NOT NULL,
    `contact_name`  VARCHAR(100) NOT NULL,
    `contact_phone` VARCHAR(20)  NOT NULL,
    `relation`      VARCHAR(50),
    CONSTRAINT `fk_ec_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. volunteers
CREATE TABLE `volunteers` (
    `id`              INT AUTO_INCREMENT PRIMARY KEY,
    `user_id`         INT          NOT NULL,
    `district`        VARCHAR(50),
    `skill`           VARCHAR(100),
    `id_proof_status` VARCHAR(50)  NOT NULL DEFAULT 'UNVERIFIED',
    `approval_status` VARCHAR(30)  NOT NULL DEFAULT 'PENDING',
    CONSTRAINT `fk_vol_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. incident_reports
CREATE TABLE `incident_reports` (
    `id`          INT AUTO_INCREMENT PRIMARY KEY,
    `user_id`     INT          NOT NULL,
    `hazard_type` VARCHAR(50)  NOT NULL,
    `landmark`    VARCHAR(150),
    `details`     TEXT,
    `latitude`    DECIMAL(10,8),
    `longitude`   DECIMAL(11,8),
    `media_path`  VARCHAR(255),
    `status`      VARCHAR(30)  NOT NULL DEFAULT 'REPORTED',
    `created_at`  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_inc_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. sos_requests
CREATE TABLE `sos_requests` (
    `id`         INT AUTO_INCREMENT PRIMARY KEY,
    `user_id`    INT            NOT NULL,
    `latitude`   DECIMAL(10,8)  NOT NULL,
    `longitude`  DECIMAL(11,8)  NOT NULL,
    `status`     VARCHAR(30)    NOT NULL DEFAULT 'DISPATCHED',
    `created_at` TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_sos_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;

-- Seed data (only for dev/test)
INSERT INTO `users` (full_name, email, phone, password, role) VALUES
    ('Alvin MS',      'alvin@disaster.gov.in', '9876543210', 'Pass123456', 'CITIZEN'),
    ('Athul Cleetus', 'athul@disaster.gov.in', '8765432109', 'Pass123456', 'VOLUNTEER');

