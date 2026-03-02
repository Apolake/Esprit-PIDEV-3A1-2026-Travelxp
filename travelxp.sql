-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Mar 02, 2026 at 11:14 AM
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
-- Database: `travelxp`
--

-- --------------------------------------------------------

--
-- Table structure for table `activities`
--

CREATE TABLE `activities` (
  `id` bigint(20) NOT NULL,
  `trip_id` bigint(20) DEFAULT NULL,
  `title` varchar(255) NOT NULL,
  `type` varchar(100) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `activity_date` date DEFAULT NULL,
  `start_time` time DEFAULT NULL,
  `end_time` time DEFAULT NULL,
  `location_name` varchar(255) DEFAULT NULL,
  `transport_type` varchar(100) DEFAULT NULL,
  `cost_amount` double DEFAULT 0,
  `currency` varchar(10) DEFAULT 'USD',
  `xp_earned` int(11) DEFAULT 0,
  `status` varchar(50) DEFAULT 'PLANNED',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `activities`
--

INSERT INTO `activities` (`id`, `trip_id`, `title`, `type`, `description`, `activity_date`, `start_time`, `end_time`, `location_name`, `transport_type`, `cost_amount`, `currency`, `xp_earned`, `status`, `created_at`, `updated_at`) VALUES
(2, 2, 'Beach Party', 'Swimming', NULL, '2026-02-28', NULL, NULL, NULL, NULL, 50, NULL, NULL, 'PLANNED', '2026-02-23 21:55:50', '2026-02-24 09:15:01'),
(6, 2, 'okd', 'jjj', NULL, '2026-02-25', NULL, NULL, NULL, NULL, 655, NULL, NULL, 'PLANNED', '2026-02-24 09:14:56', '2026-02-24 09:14:56'),
(7, 8, 'okd', 'jjj', NULL, '2026-02-25', NULL, NULL, NULL, NULL, 655, NULL, NULL, 'DONE', '2026-02-24 09:19:56', '2026-02-24 09:19:56'),
(8, 8, 'Beach Party', 'Swimming', NULL, '2026-02-28', NULL, NULL, NULL, NULL, 50, NULL, NULL, 'DONE', '2026-02-24 09:19:56', '2026-02-24 09:19:56');

-- --------------------------------------------------------

--
-- Table structure for table `booking`
--

CREATE TABLE `booking` (
  `booking_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `trip_id` int(11) DEFAULT NULL,
  `service_id` int(11) DEFAULT NULL,
  `booking_date` date DEFAULT NULL,
  `booking_status` varchar(50) DEFAULT 'PENDING',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `duration` int(11) DEFAULT 1,
  `total_price` decimal(10,2) DEFAULT 0.00,
  `property_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `booking`
--

INSERT INTO `booking` (`booking_id`, `user_id`, `trip_id`, `service_id`, `booking_date`, `booking_status`, `created_at`, `duration`, `total_price`, `property_id`) VALUES
(1, 4, 0, 0, '2026-02-23', 'CANCELLED', '2026-02-23 19:56:24', 2, 0.00, NULL),
(2, 4, 0, 0, '2026-02-23', 'CANCELLED', '2026-02-23 20:02:44', 3, 0.00, NULL),
(3, 4, 0, 0, '2026-02-23', 'CANCELLED', '2026-02-23 20:13:41', 1, 270.00, NULL),
(4, 4, 0, 0, '2027-02-11', 'CANCELLED', '2026-02-23 20:14:02', 3, 405.00, NULL),
(5, 4, 0, 0, '2026-02-23', 'CANCELLED', '2026-02-23 20:21:37', 3, 500.00, NULL),
(6, 4, 0, 0, '2026-02-23', 'CANCELLED', '2026-02-23 20:36:53', 2, 360.00, 4),
(7, 4, 0, 0, '2026-02-23', 'CANCELLED', '2026-02-23 22:06:57', 2, 644.00, 6),
(8, 4, 0, 0, '2026-02-24', 'CONFIRMED', '2026-02-24 08:47:25', 100, 19980.00, 4);

-- --------------------------------------------------------

--
-- Table structure for table `booking_services`
--

CREATE TABLE `booking_services` (
  `booking_id` int(11) NOT NULL,
  `service_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `booking_services`
--

INSERT INTO `booking_services` (`booking_id`, `service_id`) VALUES
(6, 1),
(7, 1),
(7, 2),
(8, 1),
(8, 2);

-- --------------------------------------------------------

--
-- Table structure for table `comments`
--

CREATE TABLE `comments` (
  `id` int(11) NOT NULL,
  `feedback_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `comment_text` text NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `comments`
--

INSERT INTO `comments` (`id`, `feedback_id`, `user_id`, `comment_text`, `created_at`) VALUES
(2, 2, 4, 'haha', '2026-02-23 20:05:49'),
(3, 2, 4, 'commznt13454 ', '2026-02-24 07:42:36');

-- --------------------------------------------------------

--
-- Table structure for table `feedback`
--

CREATE TABLE `feedback` (
  `id` int(11) NOT NULL,
  `fcontent` text NOT NULL,
  `user_id` int(11) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `feedback`
--

INSERT INTO `feedback` (`id`, `fcontent`, `user_id`, `created_at`) VALUES
(2, 'test', 3, '2026-02-23 18:41:25'),
(3, 'teasst', 4, '2026-02-23 20:05:37'),
(4, 'test 123455', 4, '2026-02-24 07:40:23');

-- --------------------------------------------------------

--
-- Table structure for table `gamification`
--

CREATE TABLE `gamification` (
  `user_id` int(11) NOT NULL,
  `xp` int(11) DEFAULT 0,
  `level` int(11) DEFAULT 1,
  `title` varchar(50) DEFAULT 'Novice'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `gamification`
--

INSERT INTO `gamification` (`user_id`, `xp`, `level`, `title`) VALUES
(2, 155, 3, 'Explorer'),
(3, 730, 9, 'Adventurer'),
(4, 7490, 76, 'Beyond Limits'),
(5, 100, 5, 'Traveler'),
(6, 90, 2, 'Novice'),
(7, 0, 1, 'Novice');

-- --------------------------------------------------------

--
-- Table structure for table `offer`
--

CREATE TABLE `offer` (
  `id` bigint(20) NOT NULL,
  `property_id` bigint(20) NOT NULL,
  `title` varchar(255) NOT NULL,
  `description` text DEFAULT NULL,
  `discount_percentage` decimal(5,2) DEFAULT NULL,
  `start_date` date DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `offer`
--

INSERT INTO `offer` (`id`, `property_id`, `title`, `description`, `discount_percentage`, `start_date`, `end_date`, `is_active`, `created_at`) VALUES
(1, 4, 'Sale', 'good', 10.00, '2026-02-17', '2026-02-28', 1, '2026-02-23 19:01:37');

-- --------------------------------------------------------

--
-- Table structure for table `property`
--

CREATE TABLE `property` (
  `id` bigint(20) NOT NULL,
  `owner_id` int(11) NOT NULL,
  `title` varchar(255) NOT NULL,
  `description` text DEFAULT NULL,
  `property_type` varchar(50) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `city` varchar(100) DEFAULT NULL,
  `country` varchar(100) DEFAULT NULL,
  `bedrooms` int(11) DEFAULT NULL,
  `bathrooms` int(11) DEFAULT NULL,
  `max_guests` int(11) DEFAULT NULL,
  `price_per_night` decimal(10,2) DEFAULT NULL,
  `images` text DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `property`
--

INSERT INTO `property` (`id`, `owner_id`, `title`, `description`, `property_type`, `address`, `city`, `country`, `bedrooms`, `bathrooms`, `max_guests`, `price_per_night`, `images`, `is_active`, `created_at`) VALUES
(4, 2, 'Duplex', 'Big Duplex with all commodities', 'House', 'Cite Riadh', 'Tunis', 'Tunisia', 2, 1, 2, 150.00, 'uploads\\image_1.jpg', 1, '2026-02-23 19:55:38'),
(5, 3, 'Great Villa with Swimming pool', 'Great Villa with Swimming pool', 'Villa', '1375 Frankfurt', 'Frankfurt', 'Germany', 5, 3, 10, 250.00, 'uploads\\image_2.jpg', 1, '2026-02-23 20:20:33'),
(6, 5, 'Duplex with Sauna', 'Duplex with Sauna', 'Duplex', 'Next to Esprit', 'Tunis', 'Tunisia', 2, 3, 5, 250.00, 'uploads\\image_4.jpg', 1, '2026-02-23 22:05:19');

-- --------------------------------------------------------

--
-- Table structure for table `service`
--

CREATE TABLE `service` (
  `service_id` int(11) NOT NULL,
  `provider_name` varchar(255) NOT NULL,
  `service_type` varchar(100) DEFAULT NULL,
  `price` double DEFAULT NULL,
  `eco_friendly` tinyint(1) DEFAULT NULL,
  `xp_reward` int(11) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `service`
--

INSERT INTO `service` (`service_id`, `provider_name`, `service_type`, `price`, `eco_friendly`, `xp_reward`, `created_at`) VALUES
(1, 'Cleaning Company', 'Room Cleaning', 50, 1, 50, '2026-02-23 20:33:12'),
(2, 'Clothes Cleaning Company', 'Clothing', 22, 1, 50, '2026-02-23 22:06:23');

-- --------------------------------------------------------

--
-- Table structure for table `trips`
--

CREATE TABLE `trips` (
  `id` bigint(20) NOT NULL,
  `user_id` int(11) DEFAULT NULL,
  `trip_name` varchar(255) NOT NULL,
  `origin` varchar(255) DEFAULT NULL,
  `destination` varchar(255) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `start_date` date NOT NULL,
  `end_date` date NOT NULL,
  `status` varchar(50) DEFAULT 'PLANNED',
  `budget_amount` double DEFAULT 0,
  `currency` varchar(10) DEFAULT 'USD',
  `total_expenses` double DEFAULT 0,
  `total_xp_earned` int(11) DEFAULT 0,
  `notes` text DEFAULT NULL,
  `cover_image_url` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `parent_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `trips`
--

INSERT INTO `trips` (`id`, `user_id`, `trip_name`, `origin`, `destination`, `description`, `start_date`, `end_date`, `status`, `budget_amount`, `currency`, `total_expenses`, `total_xp_earned`, `notes`, `cover_image_url`, `created_at`, `updated_at`, `parent_id`) VALUES
(1, 3, 'new trip', 'Paris', 'Tunis', NULL, '2026-02-24', '2026-02-27', 'PLANNED', 500, NULL, 0, 0, NULL, NULL, '2026-02-23 21:39:11', '2026-02-23 21:39:11', NULL),
(2, NULL, 'new trip2', 'Germany', 'Tunis', NULL, '2026-02-24', '2026-02-27', 'PLANNED', 500, NULL, 0, 0, NULL, NULL, '2026-02-23 21:51:41', '2026-02-23 21:51:41', NULL),
(6, NULL, 'hhhhhhhh', 'libya', 'tunisia', NULL, '2026-02-25', '2026-02-26', 'PLANNED', 2, NULL, 0, 0, NULL, NULL, '2026-02-24 09:12:43', '2026-02-24 09:13:31', NULL),
(7, NULL, 'kkkkkkkkk', 'libya', 'tunisia', NULL, '2026-02-25', '2026-02-26', 'PLANNED', 2, NULL, 0, 0, NULL, NULL, '2026-02-24 09:13:15', '2026-02-24 09:13:15', NULL),
(8, 4, 'new trip2', 'Germany', 'Tunis', NULL, '2026-02-24', '2026-02-27', 'PLANNED', 500, NULL, 1205, 0, NULL, NULL, '2026-02-24 09:19:56', '2026-02-24 09:19:56', 2);

-- --------------------------------------------------------

--
-- Table structure for table `trip_activity_participants`
--

CREATE TABLE `trip_activity_participants` (
  `activity_id` bigint(20) NOT NULL,
  `user_id` int(11) NOT NULL,
  `joined_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `trip_activity_participants`
--

INSERT INTO `trip_activity_participants` (`activity_id`, `user_id`, `joined_at`) VALUES
(2, 4, '2026-02-24 09:09:26'),
(6, 4, '2026-02-24 09:19:56');

-- --------------------------------------------------------

--
-- Table structure for table `trip_milestones`
--

CREATE TABLE `trip_milestones` (
  `id` bigint(20) NOT NULL,
  `trip_id` bigint(20) DEFAULT NULL,
  `title` varchar(255) NOT NULL,
  `description` text DEFAULT NULL,
  `milestone_date` date DEFAULT NULL,
  `status` varchar(50) DEFAULT 'PLANNED',
  `xp_earned` int(11) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `trip_participants`
--

CREATE TABLE `trip_participants` (
  `trip_id` bigint(20) NOT NULL,
  `user_id` int(11) NOT NULL,
  `joined_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `trip_participants`
--

INSERT INTO `trip_participants` (`trip_id`, `user_id`, `joined_at`) VALUES
(2, 4, '2026-02-24 09:09:26');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `birthday` date NOT NULL,
  `bio` text DEFAULT NULL,
  `profile_image` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `role` enum('USER','ADMIN') DEFAULT 'USER',
  `balance` decimal(10,2) DEFAULT 0.00,
  `face_registered` tinyint(1) NOT NULL DEFAULT 0,
  `totp_enabled` tinyint(1) NOT NULL DEFAULT 0,
  `totp_secret` varchar(64) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `username`, `email`, `password_hash`, `birthday`, `bio`, `profile_image`, `created_at`, `updated_at`, `role`, `balance`, `face_registered`, `totp_enabled`, `totp_secret`) VALUES
(2, 'reaperxd', 'reaper@gmail.com', '$2a$12$Mrhuj2urJxJ2B4xSDjn51OUdw3vewlAyc41xKAWaBeFpgjXlbGnQe', '2008-02-18', 'OMG', 'uploads\\image_7.png', '2026-02-18 22:01:10', '2026-03-02 09:12:34', 'USER', 0.00, 1, 0, NULL),
(3, 'admin', 'admin@gmail.com', '$2a$12$Bbbzf9nb7b8XHsLjXEZ9euK2YSLohLrAYOdY8A885evHAaviycasS', '2003-05-18', 'its me', 'uploads\\image_5.png', '2026-02-20 19:30:28', '2026-02-23 22:20:00', 'ADMIN', 0.00, 0, 0, NULL),
(4, 'yassine', 'yassine@gmail.com', '$2a$12$lsCe772z4trtu3VFqRdFROXh4S08mYJOCW5.OOjmNfAqeO9uCYn8C', '2003-05-18', 'real', 'uploads\\image_6.png', '2026-02-20 19:31:38', '2026-03-02 09:36:15', 'USER', 5745.00, 1, 1, 'E3JNLSUKQLH6AKGVT4QNMASJJRISHSUG'),
(5, 'test', 'test@gmail.com', '$2a$12$mtPVwj2Cr9bzW0NHNrFHB.wWqeSKpCmIQSV3fZP47vkf5igezwI6i', '2004-02-24', 'test', 'uploads\\image_3.png', '2026-02-23 22:04:24', '2026-02-23 22:04:24', 'USER', 0.00, 0, 0, NULL),
(6, 'dhiaadmin', 'raddaouidhia135@gmail.com', '$2a$12$0P6k4lpDgOqJdemIL5j1peD8MkwsqzFVQ4mEu73T5hkIeakdRoKIe', '2004-11-12', '', 'uploads\\image_8.jpg', '2026-02-24 08:13:51', '2026-02-24 08:15:40', 'ADMIN', 0.00, 0, 0, NULL),
(7, 'yassineraddadi', 'yassine.raddad.1@gmail.com', '$2a$12$uXexhVql0F8wq2R8SaViz.cvJFEviGEWLzV1hjWZG33CC/Ro7KRZ.', '2006-02-15', '', 'uploads\\image_9.jpg', '2026-02-24 08:17:25', '2026-02-24 08:17:25', 'USER', 0.00, 0, 0, NULL);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `activities`
--
ALTER TABLE `activities`
  ADD PRIMARY KEY (`id`),
  ADD KEY `trip_id` (`trip_id`);

--
-- Indexes for table `booking`
--
ALTER TABLE `booking`
  ADD PRIMARY KEY (`booking_id`),
  ADD KEY `user_id` (`user_id`),
  ADD KEY `property_id` (`property_id`);

--
-- Indexes for table `booking_services`
--
ALTER TABLE `booking_services`
  ADD PRIMARY KEY (`booking_id`,`service_id`),
  ADD KEY `service_id` (`service_id`);

--
-- Indexes for table `comments`
--
ALTER TABLE `comments`
  ADD PRIMARY KEY (`id`),
  ADD KEY `feedback_id` (`feedback_id`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `feedback`
--
ALTER TABLE `feedback`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `gamification`
--
ALTER TABLE `gamification`
  ADD PRIMARY KEY (`user_id`);

--
-- Indexes for table `offer`
--
ALTER TABLE `offer`
  ADD PRIMARY KEY (`id`),
  ADD KEY `property_id` (`property_id`);

--
-- Indexes for table `property`
--
ALTER TABLE `property`
  ADD PRIMARY KEY (`id`),
  ADD KEY `owner_id` (`owner_id`);

--
-- Indexes for table `service`
--
ALTER TABLE `service`
  ADD PRIMARY KEY (`service_id`);

--
-- Indexes for table `trips`
--
ALTER TABLE `trips`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`),
  ADD KEY `fk_parent_trip` (`parent_id`);

--
-- Indexes for table `trip_activity_participants`
--
ALTER TABLE `trip_activity_participants`
  ADD PRIMARY KEY (`activity_id`,`user_id`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `trip_milestones`
--
ALTER TABLE `trip_milestones`
  ADD PRIMARY KEY (`id`),
  ADD KEY `trip_id` (`trip_id`);

--
-- Indexes for table `trip_participants`
--
ALTER TABLE `trip_participants`
  ADD PRIMARY KEY (`trip_id`,`user_id`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username` (`username`),
  ADD UNIQUE KEY `email` (`email`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `activities`
--
ALTER TABLE `activities`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `booking`
--
ALTER TABLE `booking`
  MODIFY `booking_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `comments`
--
ALTER TABLE `comments`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `feedback`
--
ALTER TABLE `feedback`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `offer`
--
ALTER TABLE `offer`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `property`
--
ALTER TABLE `property`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT for table `service`
--
ALTER TABLE `service`
  MODIFY `service_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `trips`
--
ALTER TABLE `trips`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `trip_milestones`
--
ALTER TABLE `trip_milestones`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `activities`
--
ALTER TABLE `activities`
  ADD CONSTRAINT `activities_ibfk_1` FOREIGN KEY (`trip_id`) REFERENCES `trips` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `booking`
--
ALTER TABLE `booking`
  ADD CONSTRAINT `booking_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `booking_ibfk_2` FOREIGN KEY (`property_id`) REFERENCES `property` (`id`) ON DELETE SET NULL;

--
-- Constraints for table `booking_services`
--
ALTER TABLE `booking_services`
  ADD CONSTRAINT `booking_services_ibfk_1` FOREIGN KEY (`booking_id`) REFERENCES `booking` (`booking_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `booking_services_ibfk_2` FOREIGN KEY (`service_id`) REFERENCES `service` (`service_id`) ON DELETE CASCADE;

--
-- Constraints for table `comments`
--
ALTER TABLE `comments`
  ADD CONSTRAINT `comments_ibfk_1` FOREIGN KEY (`feedback_id`) REFERENCES `feedback` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `comments_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `feedback`
--
ALTER TABLE `feedback`
  ADD CONSTRAINT `feedback_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `gamification`
--
ALTER TABLE `gamification`
  ADD CONSTRAINT `gamification_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `offer`
--
ALTER TABLE `offer`
  ADD CONSTRAINT `offer_ibfk_1` FOREIGN KEY (`property_id`) REFERENCES `property` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `property`
--
ALTER TABLE `property`
  ADD CONSTRAINT `property_ibfk_1` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `trips`
--
ALTER TABLE `trips`
  ADD CONSTRAINT `fk_parent_trip` FOREIGN KEY (`parent_id`) REFERENCES `trips` (`id`) ON DELETE SET NULL,
  ADD CONSTRAINT `trips_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL;

--
-- Constraints for table `trip_activity_participants`
--
ALTER TABLE `trip_activity_participants`
  ADD CONSTRAINT `trip_activity_participants_ibfk_1` FOREIGN KEY (`activity_id`) REFERENCES `activities` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `trip_activity_participants_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `trip_milestones`
--
ALTER TABLE `trip_milestones`
  ADD CONSTRAINT `trip_milestones_ibfk_1` FOREIGN KEY (`trip_id`) REFERENCES `trips` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `trip_participants`
--
ALTER TABLE `trip_participants`
  ADD CONSTRAINT `trip_participants_ibfk_1` FOREIGN KEY (`trip_id`) REFERENCES `trips` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `trip_participants_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
