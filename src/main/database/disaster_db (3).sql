-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Sep 20, 2026 at 07:06 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `disaster_db`
--

-- --------------------------------------------------------

--
-- Table structure for table `emergency_contacts`
--

CREATE TABLE `emergency_contacts` (
  `contact_id` int(11) NOT NULL,
  `user_id` int(11) DEFAULT NULL,
  `contact_name` varchar(100) DEFAULT NULL,
  `contact_phone` varchar(15) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `emergency_contacts`
--

INSERT INTO `emergency_contacts` (`contact_id`, `user_id`, `contact_name`, `contact_phone`) VALUES
(1, 1, 'John (Dad)', '9998887776'),
(2, 2, 'Mary (Mom)', '8887776665');

-- --------------------------------------------------------

--
-- Table structure for table `gps_locations`
--

CREATE TABLE `gps_locations` (
  `location_id` int(11) NOT NULL,
  `sos_id` int(11) DEFAULT NULL,
  `latitude` decimal(10,8) DEFAULT NULL,
  `longitude` decimal(11,8) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `gps_locations`
--

INSERT INTO `gps_locations` (`location_id`, `sos_id`, `latitude`, `longitude`) VALUES
(1, 1, 9.57860000, 76.97460000),
(2, 2, 9.93120000, 76.26730000),
(3, 3, 9.93120000, 76.26730000),
(4, 4, 9.93120000, 76.26730000),
(5, 5, 9.93120000, 76.26730000);

-- --------------------------------------------------------

--
-- Table structure for table `incident_media`
--

CREATE TABLE `incident_media` (
  `media_id` int(11) NOT NULL,
  `incident_id` int(11) DEFAULT NULL,
  `file_path` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `incident_media`
--

INSERT INTO `incident_media` (`media_id`, `incident_id`, `file_path`) VALUES
(1, 1, '/images/landslide_01.jpg');

-- --------------------------------------------------------

--
-- Table structure for table `incident_reports`
--

CREATE TABLE `incident_reports` (
  `incident_id` int(11) NOT NULL,
  `user_id` int(11) DEFAULT NULL,
  `disaster_type` varchar(50) DEFAULT NULL,
  `severity_level` int(11) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `status` varchar(20) DEFAULT 'Reported',
  `latitude` decimal(10,8) DEFAULT NULL,
  `longitude` decimal(11,8) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `incident_reports`
--

INSERT INTO `incident_reports` (`incident_id`, `user_id`, `disaster_type`, `severity_level`, `description`, `status`, `latitude`, `longitude`) VALUES
(1, 2, 'Landslide', 8, 'Road blocked near college gate.', 'Reported', NULL, NULL),
(2, 2, 'Flood', 9, 'Water levels rising near MBCCET main gate.', 'Reported', NULL, NULL),
(3, 2, 'Flood', 9, 'Water levels rising near MBCCET main gate.', 'Reported', NULL, NULL),
(4, 7, 'Flood', 8, '9.9312° N, 76.2673° E (Kerala Relief Grid) - reessesese', 'Reported', NULL, NULL),
(5, 7, 'FLOOD', 8, '9.9312° N, 76.2673° E (Kerala Relief Grid) - reessesese', 'PENDING', NULL, NULL),
(6, 1, 'Flood', 8, '9.9312° N, 76.2673° E (Kerala Relief Grid) - eses4dtytuytu6y', 'Reported', NULL, NULL),
(7, 1, 'FLOOD', 8, '9.9312° N, 76.2673° E (Kerala Relief Grid) - eses4dtytuytu6y', 'PENDING', NULL, NULL),
(8, 1, 'Flood', 8, '9.9312° N, 76.2673° E (Kerala Relief Grid) - wsaffsdrfsdfds', 'Reported', NULL, NULL),
(9, 1, 'FLOOD', 8, '9.9312° N, 76.2673° E (Kerala Relief Grid) - wsaffsdrfsdfds', 'PENDING', NULL, NULL),
(10, 1, 'Flood', 8, '9.9312° N, 76.2673° E (Kerala Relief Grid) - saYGDGUysgyd', 'Reported', NULL, NULL),
(11, 1, 'FLOOD', 8, '9.9312° N, 76.2673° E (Kerala Relief Grid) - saYGDGUysgyd', 'PENDING', NULL, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `notifications`
--

CREATE TABLE `notifications` (
  `notification_id` int(11) NOT NULL,
  `user_id` int(11) DEFAULT NULL,
  `message` varchar(255) DEFAULT NULL,
  `is_read` tinyint(1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `notifications`
--

INSERT INTO `notifications` (`notification_id`, `user_id`, `message`, `is_read`) VALUES
(1, 1, 'Heavy rain alert issued for Idukki district.', 0),
(2, 2, 'Your landslide report has been received by authorities.', 0),
(3, 1, 'SOS broadcast #2 received by Unified Command', 0),
(4, 1, 'SOS broadcast #3 received by Unified Command', 0),
(5, 7, 'Incident report #4 (Flood) registered', 0),
(6, 7, 'Incident report #5 (FLOOD) registered', 0),
(7, 1, 'Incident report #6 (Flood) registered', 0),
(8, 1, 'Incident report #7 (FLOOD) registered', 0),
(9, 1, 'Incident report #8 (Flood) registered', 0),
(10, 1, 'Incident report #9 (FLOOD) registered', 0),
(11, 1, 'Incident report #11 (FLOOD) registered', 0),
(12, 1, 'SOS broadcast #4 received by Unified Command', 0),
(13, 1, 'SOS broadcast #5 received by Unified Command', 0);

-- --------------------------------------------------------

--
-- Table structure for table `rescue_status`
--

CREATE TABLE `rescue_status` (
  `status_id` int(11) NOT NULL,
  `sos_id` int(11) DEFAULT NULL,
  `update_message` varchar(255) DEFAULT NULL,
  `update_time` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `rescue_status`
--

INSERT INTO `rescue_status` (`status_id`, `sos_id`, `update_message`, `update_time`) VALUES
(1, 1, 'Rescue team dispatched from Kuttikkanam base.', '2026-09-17 12:45:52'),
(2, 2, 'Critical Medical Emergency', '2026-09-20 16:24:20'),
(3, 3, 'Critical Medical Emergency', '2026-09-20 16:24:20'),
(4, 4, 'Critical Medical Emergency', '2026-09-20 16:56:59'),
(5, 5, 'Critical Medical Emergency', '2026-09-20 16:56:59');

-- --------------------------------------------------------

--
-- Table structure for table `sos_requests`
--

CREATE TABLE `sos_requests` (
  `sos_id` int(11) NOT NULL,
  `user_id` int(11) DEFAULT NULL,
  `status` varchar(20) DEFAULT 'Active',
  `request_time` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `sos_requests`
--

INSERT INTO `sos_requests` (`sos_id`, `user_id`, `status`, `request_time`) VALUES
(1, 1, 'Active', '2026-09-17 12:28:26'),
(2, 1, 'Active', '2026-09-20 16:24:20'),
(3, 1, 'Active', '2026-09-20 16:24:20'),
(4, 1, 'Active', '2026-09-20 16:56:59'),
(5, 1, 'Active', '2026-09-20 16:56:59');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `user_id` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `phone` varchar(15) NOT NULL,
  `role` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`user_id`, `username`, `email`, `phone`, `role`, `password`) VALUES
(1, 'alvin_ms', '', '9876543210', '', ''),
(2, 'athul_c', '', '8765432109', '', ''),
(3, 'new_volunteer', '', '1122334455', '', ''),
(4, 'alvin_anil_varghese', '', '7337617901', '', ''),
(5, 'alvin', '', '7337617901', '', ''),
(6, 'test@gmail.com', 'test@gmail.com', '1234567890', 'VICTIM', '560090'),
(7, 'test1@gmail.com', 'test1@gmail.com', '7337617901', 'VICTIM', '123456789');

-- --------------------------------------------------------

--
-- Table structure for table `user_profiles`
--

CREATE TABLE `user_profiles` (
  `profile_id` int(11) NOT NULL,
  `user_id` int(11) DEFAULT NULL,
  `full_name` varchar(100) DEFAULT NULL,
  `blood_group` varchar(5) DEFAULT NULL,
  `address` varchar(200) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `user_profiles`
--

INSERT INTO `user_profiles` (`profile_id`, `user_id`, `full_name`, `blood_group`, `address`) VALUES
(1, 1, 'Alvin MS', 'O+', 'Hostel Block A'),
(2, 2, 'Athul Cleetus', 'B+', 'Hostel Block B'),
(4, 5, 'Alvin', 'O+', 'Kerala Disaster Relief Area'),
(5, 6, 'Test', 'A+', 'test adresss'),
(6, 7, 'test1', 'B-', 'ysgdtfsTADFT');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `emergency_contacts`
--
ALTER TABLE `emergency_contacts`
  ADD PRIMARY KEY (`contact_id`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `gps_locations`
--
ALTER TABLE `gps_locations`
  ADD PRIMARY KEY (`location_id`),
  ADD KEY `sos_id` (`sos_id`);

--
-- Indexes for table `incident_media`
--
ALTER TABLE `incident_media`
  ADD PRIMARY KEY (`media_id`),
  ADD KEY `incident_id` (`incident_id`);

--
-- Indexes for table `incident_reports`
--
ALTER TABLE `incident_reports`
  ADD PRIMARY KEY (`incident_id`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `notifications`
--
ALTER TABLE `notifications`
  ADD PRIMARY KEY (`notification_id`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `rescue_status`
--
ALTER TABLE `rescue_status`
  ADD PRIMARY KEY (`status_id`),
  ADD KEY `sos_id` (`sos_id`);

--
-- Indexes for table `sos_requests`
--
ALTER TABLE `sos_requests`
  ADD PRIMARY KEY (`sos_id`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`);

--
-- Indexes for table `user_profiles`
--
ALTER TABLE `user_profiles`
  ADD PRIMARY KEY (`profile_id`),
  ADD KEY `user_id` (`user_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `emergency_contacts`
--
ALTER TABLE `emergency_contacts`
  MODIFY `contact_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `gps_locations`
--
ALTER TABLE `gps_locations`
  MODIFY `location_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `incident_media`
--
ALTER TABLE `incident_media`
  MODIFY `media_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `incident_reports`
--
ALTER TABLE `incident_reports`
  MODIFY `incident_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT for table `notifications`
--
ALTER TABLE `notifications`
  MODIFY `notification_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- AUTO_INCREMENT for table `rescue_status`
--
ALTER TABLE `rescue_status`
  MODIFY `status_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `sos_requests`
--
ALTER TABLE `sos_requests`
  MODIFY `sos_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `user_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT for table `user_profiles`
--
ALTER TABLE `user_profiles`
  MODIFY `profile_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `emergency_contacts`
--
ALTER TABLE `emergency_contacts`
  ADD CONSTRAINT `emergency_contacts_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;

--
-- Constraints for table `gps_locations`
--
ALTER TABLE `gps_locations`
  ADD CONSTRAINT `gps_locations_ibfk_1` FOREIGN KEY (`sos_id`) REFERENCES `sos_requests` (`sos_id`) ON DELETE CASCADE;

--
-- Constraints for table `incident_media`
--
ALTER TABLE `incident_media`
  ADD CONSTRAINT `incident_media_ibfk_1` FOREIGN KEY (`incident_id`) REFERENCES `incident_reports` (`incident_id`) ON DELETE CASCADE;

--
-- Constraints for table `incident_reports`
--
ALTER TABLE `incident_reports`
  ADD CONSTRAINT `incident_reports_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;

--
-- Constraints for table `notifications`
--
ALTER TABLE `notifications`
  ADD CONSTRAINT `notifications_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;

--
-- Constraints for table `rescue_status`
--
ALTER TABLE `rescue_status`
  ADD CONSTRAINT `rescue_status_ibfk_1` FOREIGN KEY (`sos_id`) REFERENCES `sos_requests` (`sos_id`) ON DELETE CASCADE;

--
-- Constraints for table `sos_requests`
--
ALTER TABLE `sos_requests`
  ADD CONSTRAINT `sos_requests_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;

--
-- Constraints for table `user_profiles`
--
ALTER TABLE `user_profiles`
  ADD CONSTRAINT `user_profiles_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
