-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Feb 16, 2026 at 09:28 AM
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
-- Table structure for table `achievements`
--

CREATE TABLE `achievements` (
  `id` bigint(20) NOT NULL,
  `name` varchar(100) NOT NULL,
  `description` text DEFAULT NULL,
  `icon` varchar(255) DEFAULT NULL,
  `xp_reward` int(11) DEFAULT 0,
  `achievement_type` varchar(50) NOT NULL,
  `requirement_value` int(11) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `achievements`
--

INSERT INTO `achievements` (`id`, `name`, `description`, `icon`, `xp_reward`, `achievement_type`, `requirement_value`, `created_at`) VALUES
(1, 'First Booking', 'Complete your first booking', '🎉', 50, 'BOOKING', 1, '2026-02-03 10:13:25'),
(2, 'Frequent Traveler', 'Complete 5 bookings', '🏆', 100, 'BOOKING', 5, '2026-02-03 10:13:25'),
(3, 'Booking Expert', 'Complete 10 bookings', '💎', 200, 'BOOKING', 10, '2026-02-03 10:13:25'),
(4, 'Review Writer', 'Write your first review', '✍️', 25, 'REVIEW', 1, '2026-02-03 10:13:25'),
(5, 'Critic', 'Write 10 reviews', '📝', 150, 'REVIEW', 10, '2026-02-03 10:13:25'),
(6, 'Property Owner', 'List your first property', '🏠', 75, 'PROPERTY', 1, '2026-02-03 10:13:25'),
(7, 'Super Host', 'Receive 10 five-star reviews', '⭐', 300, 'HOST', 10, '2026-02-03 10:13:25'),
(8, 'Early Bird', 'Book 3 months in advance', '🐦', 50, 'SPECIAL', 1, '2026-02-03 10:13:25'),
(9, 'Last Minute', 'Book within 24 hours of check-in', '⚡', 50, 'SPECIAL', 1, '2026-02-03 10:13:25'),
(10, 'Weekend Warrior', 'Complete 5 weekend bookings', '🎊', 100, 'SPECIAL', 5, '2026-02-03 10:13:25');

-- --------------------------------------------------------

--
-- Table structure for table `activities`
--

CREATE TABLE `activities` (
  `id` bigint(20) NOT NULL,
  `trip_id` bigint(20) NOT NULL,
  `title` varchar(150) NOT NULL,
  `description` text DEFAULT NULL,
  `activity_date` date NOT NULL,
  `location` varchar(255) DEFAULT NULL,
  `cost` decimal(10,2) DEFAULT 0.00,
  `xp_reward` int(11) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `bookings`
--

CREATE TABLE `bookings` (
  `id` bigint(20) NOT NULL,
  `property_id` bigint(20) NOT NULL,
  `guest_id` bigint(20) NOT NULL,
  `check_in_date` date NOT NULL,
  `check_out_date` date NOT NULL,
  `total_price` decimal(10,2) NOT NULL,
  `number_of_guests` int(11) NOT NULL,
  `status` varchar(20) DEFAULT 'PENDING',
  `special_requests` text DEFAULT NULL,
  `xp_earned` int(11) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `bookings`
--

INSERT INTO `bookings` (`id`, `property_id`, `guest_id`, `check_in_date`, `check_out_date`, `total_price`, `number_of_guests`, `status`, `special_requests`, `xp_earned`, `created_at`, `updated_at`) VALUES
(1, 1, 2, '2026-02-19', '2026-02-28', 1080.00, 1, 'CONFIRMED', '', 108, '2026-02-10 08:17:26', '2026-02-10 08:17:26'),
(2, 1, 5, '2026-02-10', '2026-02-19', 1080.00, 1, 'CONFIRMED', '', 108, '2026-02-10 08:37:55', '2026-02-10 08:37:55'),
(3, 2, 2, '2026-02-18', '2026-02-28', 2500.00, 3, 'CONFIRMED', '', 250, '2026-02-11 08:41:00', '2026-02-11 08:41:00');

-- --------------------------------------------------------

--
-- Table structure for table `booking_services`
--

CREATE TABLE `booking_services` (
  `id` bigint(20) NOT NULL,
  `booking_id` bigint(20) NOT NULL,
  `service_id` bigint(20) NOT NULL,
  `quantity` int(11) DEFAULT 1,
  `price_at_booking` decimal(10,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `offers`
--

CREATE TABLE `offers` (
  `id` bigint(20) NOT NULL,
  `property_id` bigint(20) NOT NULL,
  `title` varchar(150) NOT NULL,
  `description` text DEFAULT NULL,
  `discount_percentage` decimal(5,2) NOT NULL,
  `start_date` date NOT NULL,
  `end_date` date NOT NULL,
  `is_active` tinyint(1) DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `properties`
--

CREATE TABLE `properties` (
  `id` bigint(20) NOT NULL,
  `owner_id` bigint(20) NOT NULL,
  `title` varchar(200) NOT NULL,
  `description` text DEFAULT NULL,
  `property_type` varchar(50) NOT NULL,
  `address` varchar(255) NOT NULL,
  `city` varchar(100) NOT NULL,
  `country` varchar(100) NOT NULL,
  `latitude` decimal(10,8) DEFAULT NULL,
  `longitude` decimal(11,8) DEFAULT NULL,
  `bedrooms` int(11) NOT NULL,
  `bathrooms` int(11) NOT NULL,
  `max_guests` int(11) NOT NULL,
  `price_per_night` decimal(10,2) NOT NULL,
  `amenities` text DEFAULT NULL,
  `images` text DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `properties`
--

INSERT INTO `properties` (`id`, `owner_id`, `title`, `description`, `property_type`, `address`, `city`, `country`, `latitude`, `longitude`, `bedrooms`, `bathrooms`, `max_guests`, `price_per_night`, `amenities`, `images`, `is_active`, `created_at`, `updated_at`) VALUES
(1, 2, 'Ocean View Apartment', 'Apartment with a stunning sea view', 'APARTMENT', '123 Beach Road', 'Djerba', 'Tunisia', NULL, NULL, 2, 1, 4, 120.00, NULL, NULL, 1, '2026-02-10 09:13:02', '2026-02-10 09:13:02'),
(2, 3, 'Desert Oasis Villa', 'Luxury villa near the desert', 'VILLA', '45 Sahara Street', 'Douz', 'Tunisia', NULL, NULL, 3, 2, 6, 250.00, NULL, NULL, 1, '2026-02-10 09:13:02', '2026-02-10 09:13:02');

-- --------------------------------------------------------

--
-- Table structure for table `reviews`
--

CREATE TABLE `reviews` (
  `id` bigint(20) NOT NULL,
  `booking_id` bigint(20) NOT NULL,
  `reviewer_id` bigint(20) NOT NULL,
  `property_id` bigint(20) NOT NULL,
  `rating` int(11) NOT NULL CHECK (`rating` >= 1 and `rating` <= 5),
  `comment` text DEFAULT NULL,
  `xp_earned` int(11) DEFAULT 10,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `review_comments`
--

CREATE TABLE `review_comments` (
  `id` bigint(20) NOT NULL,
  `review_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `comment` text NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `services`
--

CREATE TABLE `services` (
  `id` bigint(20) NOT NULL,
  `name` varchar(100) NOT NULL,
  `description` text DEFAULT NULL,
  `price` decimal(10,2) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `trips`
--

CREATE TABLE `trips` (
  `id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `trip_name` varchar(150) NOT NULL,
  `start_date` date NOT NULL,
  `end_date` date NOT NULL,
  `total_xp_earned` int(11) DEFAULT 0,
  `status` varchar(20) DEFAULT 'PLANNED',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `trip_bookings`
--

CREATE TABLE `trip_bookings` (
  `id` bigint(20) NOT NULL,
  `trip_id` bigint(20) NOT NULL,
  `booking_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `trip_milestones`
--

CREATE TABLE `trip_milestones` (
  `id` bigint(20) NOT NULL,
  `trip_id` bigint(20) NOT NULL,
  `title` varchar(100) NOT NULL,
  `description` text DEFAULT NULL,
  `xp_reward` int(11) DEFAULT 0,
  `completed` tinyint(1) DEFAULT 0,
  `completed_at` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` bigint(20) NOT NULL,
  `username` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `full_name` varchar(100) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `profile_picture` varchar(255) DEFAULT NULL,
  `bio` text DEFAULT NULL,
  `experience_points` int(11) DEFAULT 0,
  `level_id` bigint(20) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `username`, `email`, `password`, `full_name`, `phone`, `profile_picture`, `bio`, `experience_points`, `level_id`, `created_at`, `updated_at`) VALUES
(1, 'yassinerd', 'yassine.raddadi.1@gmail.com', '53959532', 'Yassine Raddadi', NULL, NULL, 'hahaha', 2000, 1, '2026-02-04 10:59:01', '2026-02-10 07:40:59'),
(2, 'test', 'test@test.com', '1234', 'test test', NULL, NULL, NULL, 508, 3, '2026-02-10 07:48:58', '2026-02-11 08:41:01'),
(3, 'testuser1', 'test1@travelxp.com', 'password123', 'Test User One', NULL, NULL, NULL, 0, 1, '2026-02-10 09:13:02', '2026-02-10 09:13:02'),
(4, 'testuser2', 'test2@travelxp.com', 'password123', 'Test User Two', NULL, NULL, NULL, 0, 1, '2026-02-10 09:13:02', '2026-02-10 09:13:02'),
(5, 'test2', 'test2', '1234', 'test2 test2', NULL, NULL, NULL, 208, 2, '2026-02-10 08:20:38', '2026-02-10 08:37:55'),
(6, '', '', '', '', NULL, NULL, NULL, 0, 1, '2026-02-10 08:39:45', '2026-02-10 08:39:45');

-- --------------------------------------------------------

--
-- Table structure for table `user_achievements`
--

CREATE TABLE `user_achievements` (
  `id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `achievement_id` bigint(20) NOT NULL,
  `earned_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `user_achievements`
--

INSERT INTO `user_achievements` (`id`, `user_id`, `achievement_id`, `earned_at`) VALUES
(1, 2, 1, '2026-02-10 08:17:26'),
(2, 5, 1, '2026-02-10 08:37:55');

-- --------------------------------------------------------

--
-- Table structure for table `user_levels`
--

CREATE TABLE `user_levels` (
  `id` bigint(20) NOT NULL,
  `level_name` varchar(50) NOT NULL,
  `level_number` int(11) NOT NULL,
  `xp_required` int(11) NOT NULL,
  `badge_icon` varchar(255) DEFAULT NULL,
  `benefits` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `user_levels`
--

INSERT INTO `user_levels` (`id`, `level_name`, `level_number`, `xp_required`, `badge_icon`, `benefits`, `created_at`) VALUES
(1, 'Novice Traveler', 1, 0, '🌱', 'Welcome to TravelXP! Start your journey.', '2026-02-03 10:13:25'),
(2, 'Explorer', 2, 100, '🗺️', 'Unlock 5% discount on bookings', '2026-02-03 10:13:25'),
(3, 'Adventurer', 3, 300, '🎒', 'Unlock 10% discount on bookings', '2026-02-03 10:13:25'),
(4, 'Globetrotter', 4, 600, '✈️', 'Priority customer support + 15% discount', '2026-02-03 10:13:25'),
(5, 'World Wanderer', 5, 1000, '🌍', 'Exclusive properties access + 20% discount', '2026-02-03 10:13:25'),
(6, 'Travel Master', 6, 1500, '👑', 'VIP status + 25% discount + Early access', '2026-02-03 10:13:25'),
(7, 'Legend', 7, 2500, '⭐', 'All benefits + Free cancellation + Concierge service', '2026-02-03 10:13:25');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `achievements`
--
ALTER TABLE `achievements`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `activities`
--
ALTER TABLE `activities`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_activity_trip` (`trip_id`),
  ADD KEY `idx_activity_date` (`activity_date`);

--
-- Indexes for table `bookings`
--
ALTER TABLE `bookings`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_property` (`property_id`),
  ADD KEY `idx_guest` (`guest_id`),
  ADD KEY `idx_status` (`status`),
  ADD KEY `idx_dates` (`check_in_date`,`check_out_date`);

--
-- Indexes for table `booking_services`
--
ALTER TABLE `booking_services`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_booking_service` (`booking_id`,`service_id`),
  ADD KEY `idx_booking_service_booking` (`booking_id`),
  ADD KEY `idx_booking_service_service` (`service_id`);

--
-- Indexes for table `offers`
--
ALTER TABLE `offers`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_offer_property` (`property_id`),
  ADD KEY `idx_offer_dates` (`start_date`,`end_date`);

--
-- Indexes for table `properties`
--
ALTER TABLE `properties`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_owner` (`owner_id`),
  ADD KEY `idx_city` (`city`),
  ADD KEY `idx_price` (`price_per_night`);

--
-- Indexes for table `reviews`
--
ALTER TABLE `reviews`
  ADD PRIMARY KEY (`id`),
  ADD KEY `booking_id` (`booking_id`),
  ADD KEY `idx_property` (`property_id`),
  ADD KEY `idx_reviewer` (`reviewer_id`),
  ADD KEY `idx_rating` (`rating`);

--
-- Indexes for table `review_comments`
--
ALTER TABLE `review_comments`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_review_comment_review` (`review_id`),
  ADD KEY `idx_review_comment_user` (`user_id`);

--
-- Indexes for table `services`
--
ALTER TABLE `services`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `trips`
--
ALTER TABLE `trips`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_trip_user` (`user_id`),
  ADD KEY `idx_trip_dates` (`start_date`,`end_date`);

--
-- Indexes for table `trip_bookings`
--
ALTER TABLE `trip_bookings`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_trip_booking` (`trip_id`,`booking_id`),
  ADD KEY `idx_trip` (`trip_id`),
  ADD KEY `idx_booking` (`booking_id`);

--
-- Indexes for table `trip_milestones`
--
ALTER TABLE `trip_milestones`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_trip_milestone` (`trip_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username` (`username`),
  ADD UNIQUE KEY `email` (`email`),
  ADD KEY `idx_username` (`username`),
  ADD KEY `idx_email` (`email`),
  ADD KEY `fk_user_level` (`level_id`);

--
-- Indexes for table `user_achievements`
--
ALTER TABLE `user_achievements`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_user_achievement` (`user_id`,`achievement_id`),
  ADD KEY `idx_user` (`user_id`),
  ADD KEY `idx_achievement` (`achievement_id`);

--
-- Indexes for table `user_levels`
--
ALTER TABLE `user_levels`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `level_number` (`level_number`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `achievements`
--
ALTER TABLE `achievements`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `activities`
--
ALTER TABLE `activities`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `bookings`
--
ALTER TABLE `bookings`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `booking_services`
--
ALTER TABLE `booking_services`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `offers`
--
ALTER TABLE `offers`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `properties`
--
ALTER TABLE `properties`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `reviews`
--
ALTER TABLE `reviews`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `review_comments`
--
ALTER TABLE `review_comments`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `services`
--
ALTER TABLE `services`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `trips`
--
ALTER TABLE `trips`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `trip_bookings`
--
ALTER TABLE `trip_bookings`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `trip_milestones`
--
ALTER TABLE `trip_milestones`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT for table `user_achievements`
--
ALTER TABLE `user_achievements`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `user_levels`
--
ALTER TABLE `user_levels`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `activities`
--
ALTER TABLE `activities`
  ADD CONSTRAINT `activities_ibfk_1` FOREIGN KEY (`trip_id`) REFERENCES `trips` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `bookings`
--
ALTER TABLE `bookings`
  ADD CONSTRAINT `bookings_ibfk_1` FOREIGN KEY (`property_id`) REFERENCES `properties` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `bookings_ibfk_2` FOREIGN KEY (`guest_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `booking_services`
--
ALTER TABLE `booking_services`
  ADD CONSTRAINT `booking_services_ibfk_1` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `booking_services_ibfk_2` FOREIGN KEY (`service_id`) REFERENCES `services` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `offers`
--
ALTER TABLE `offers`
  ADD CONSTRAINT `offers_ibfk_1` FOREIGN KEY (`property_id`) REFERENCES `properties` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `properties`
--
ALTER TABLE `properties`
  ADD CONSTRAINT `properties_ibfk_1` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `reviews`
--
ALTER TABLE `reviews`
  ADD CONSTRAINT `reviews_ibfk_1` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `reviews_ibfk_2` FOREIGN KEY (`reviewer_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `reviews_ibfk_3` FOREIGN KEY (`property_id`) REFERENCES `properties` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `review_comments`
--
ALTER TABLE `review_comments`
  ADD CONSTRAINT `review_comments_ibfk_1` FOREIGN KEY (`review_id`) REFERENCES `reviews` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `review_comments_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `trips`
--
ALTER TABLE `trips`
  ADD CONSTRAINT `trips_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `trip_bookings`
--
ALTER TABLE `trip_bookings`
  ADD CONSTRAINT `trip_bookings_ibfk_1` FOREIGN KEY (`trip_id`) REFERENCES `trips` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `trip_bookings_ibfk_2` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `trip_milestones`
--
ALTER TABLE `trip_milestones`
  ADD CONSTRAINT `trip_milestones_ibfk_1` FOREIGN KEY (`trip_id`) REFERENCES `trips` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `users`
--
ALTER TABLE `users`
  ADD CONSTRAINT `fk_user_level` FOREIGN KEY (`level_id`) REFERENCES `user_levels` (`id`);

--
-- Constraints for table `user_achievements`
--
ALTER TABLE `user_achievements`
  ADD CONSTRAINT `user_achievements_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `user_achievements_ibfk_2` FOREIGN KEY (`achievement_id`) REFERENCES `achievements` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
