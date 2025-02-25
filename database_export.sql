-- MySQL dump 10.13  Distrib 8.0.40, for Win64 (x86_64)
--
-- Host: localhost    Database: interactive_map
-- ------------------------------------------------------
-- Server version	8.0.40

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `electrical_lines`
--

DROP TABLE IF EXISTS `electrical_lines`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `electrical_lines` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `line_geometry` text NOT NULL,
  `description` text,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `approval_status` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_electrical_line_admin` (`admin_id`),
  CONSTRAINT `fk_electrical_line_admin` FOREIGN KEY (`admin_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `electrical_lines`
--

LOCK TABLES `electrical_lines` WRITE;
/*!40000 ALTER TABLE `electrical_lines` DISABLE KEYS */;
INSERT INTO `electrical_lines` VALUES (1,5,'Main Current','[[37.79021773112666,-122.46174573898317],[37.790701007817766,-122.46186375617982],[37.79084514235931,-122.46123075485231]]','Black canle using 2/4','2025-02-24 08:57:50','PENDING'),(2,5,'Main','[[37.79193476210032,-122.46387004852296],[37.79158290894305,-122.46320486068727],[37.79154051688329,-122.4630117416382],[37.7917270417643,-122.46268451213838]]','Black tube 4 ft uner','2025-02-24 21:10:05','APPROVED'),(3,5,'Main','[[37.79200258902236,-122.463875412941],[37.79224846109275,-122.46325850486757],[37.791972914751625,-122.46276497840883]]','123','2025-02-24 21:11:57','PENDING');
/*!40000 ALTER TABLE `electrical_lines` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `saved_locations`
--

DROP TABLE IF EXISTS `saved_locations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `saved_locations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `location` varchar(255) NOT NULL,
  `description` text,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `approval_status` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_saved_location_admin` (`admin_id`),
  CONSTRAINT `fk_saved_location_admin` FOREIGN KEY (`admin_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `saved_locations`
--

LOCK TABLES `saved_locations` WRITE;
/*!40000 ALTER TABLE `saved_locations` DISABLE KEYS */;
INSERT INTO `saved_locations` VALUES (1,5,'Lot1','[37.768221057589166,-122.40061283111574]','Main field ','2025-02-24 08:47:20','PENDING');
/*!40000 ALTER TABLE `saved_locations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tree_permissions`
--

DROP TABLE IF EXISTS `tree_permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tree_permissions` (
  `tree_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`tree_id`,`user_id`),
  KEY `FKl3mv4svnbbqcyvh0n719xmnvx` (`user_id`),
  CONSTRAINT `FKl3mv4svnbbqcyvh0n719xmnvx` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKomow9sujmwarly5tvpd3fnc1` FOREIGN KEY (`tree_id`) REFERENCES `trees` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tree_permissions`
--

LOCK TABLES `tree_permissions` WRITE;
/*!40000 ALTER TABLE `tree_permissions` DISABLE KEYS */;
/*!40000 ALTER TABLE `tree_permissions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `trees`
--

DROP TABLE IF EXISTS `trees`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `trees` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `species` varchar(255) NOT NULL,
  `health_status` varchar(255) NOT NULL,
  `height` float DEFAULT NULL,
  `next_fertilization_date` date DEFAULT NULL,
  `owner_id` bigint NOT NULL,
  `user_has_permission` tinyint(1) DEFAULT '0',
  `last_watering_date` date DEFAULT NULL,
  `next_watering_date` date DEFAULT NULL,
  `last_fertilization_date` date DEFAULT NULL,
  `soil_moisture_level` float DEFAULT NULL,
  `temperature` float DEFAULT NULL,
  `humidity` float DEFAULT NULL,
  `planting_date` date DEFAULT NULL,
  `latitude` double NOT NULL,
  `longitude` double NOT NULL,
  `health_note` text,
  `approval_status` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `owner_id` (`owner_id`),
  CONSTRAINT `trees_ibfk_1` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trees`
--

LOCK TABLES `trees` WRITE;
/*!40000 ALTER TABLE `trees` DISABLE KEYS */;
INSERT INTO `trees` VALUES (1,'Mango Tree','Healthy',5.5,'2024-02-28',1,1,'2025-02-10','2025-02-24','2024-01-01',30.5,51,50,'2020-01-01',37.79978771668936,-122.47048974037172,NULL,'PENDING'),(2,'Avacado','Healthy',3.51779,'2025-05-01',5,1,'2025-02-17','2025-02-24','2025-02-03',0.324,53,87,'2025-02-17',37.800312472885736,-122.471981048584,NULL,'PENDING'),(3,'Avacado','Bad',3.60138,'2025-05-22',5,1,'2025-02-22','2025-03-01','2025-02-22',0.324,52,90,'2025-02-22',37.7885441845406,-122.46775388717653,'','PENDING'),(4,'Apple','bad',37.1151,'2025-05-12',5,1,'2025-02-10','2025-02-17','2025-02-17',0.324,53,91,'2025-02-03',37.79978771668936,-122.47048974037172,NULL,'PENDING'),(6,'Apple','Good',3.51096,'2025-04-22',5,1,'2025-02-19','2025-03-05','2025-02-20',0.143,52,50,'2025-02-22',33.96982142357065,-117.2714138031006,NULL,'PENDING'),(7,'avacado','Healthy',3.5,'2025-06-12',5,1,'2025-02-22','2025-03-01',NULL,0.142,78,23,NULL,33.50130117085165,-117.23461389541627,NULL,'PENDING'),(8,'Apple','bad',NULL,NULL,10,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,37.7908961545805,-122.46954023838045,NULL,'PENDING'),(9,'Avacado','Good',3.50274,'2025-05-25',5,1,'2025-02-20','2025-02-27','2025-02-03',0.142,78,21,'2025-02-22',33.49979814758526,-117.23321914672853,NULL,'PENDING'),(10,'apple','bad',NULL,NULL,5,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,52.72672167856861,-363.8120412826538,NULL,'PENDING'),(11,'Apple','Good',3.51644,'2025-05-22',5,1,'2025-02-22','2025-03-01','2025-02-22',0.324,52,90,'2025-02-22',37.788598448521356,-122.46784508228303,NULL,'DENIED'),(12,'apple','Good',3.58347,'2025-05-23',5,1,'2025-02-22','2025-03-01','2025-02-22',0.323,48,88,'2025-02-18',37.78934033559422,-122.46824741363527,'','APPROVED'),(13,'apple','Good',3.5,'2025-05-25',8,0,NULL,'2025-03-03',NULL,0.324,60,90,NULL,37.7928970507318,-122.47235655784608,NULL,'DENIED'),(14,'Avacado','Good',3.5,'2025-05-25',5,1,NULL,'2025-03-03',NULL,0.324,52,91,NULL,37.791303120899194,-122.47614383697511,NULL,'PENDING'),(15,'Apple','Good',3.64375,'2025-05-15',5,1,'2025-02-17','2025-02-24','2025-02-17',0.323,48,88,'2025-02-03',37.79157866973817,-122.47426092624666,'I broke it','APPROVED'),(16,'Apple','Good',3.5,'2025-05-24',8,0,NULL,'2025-03-03',NULL,0.324,52,90,NULL,37.79310038739155,-122.45270133018495,NULL,'PENDING'),(17,'apple','Good',3.5,'2025-05-25',8,0,NULL,'2025-03-03',NULL,0.324,60,90,NULL,37.79157866973817,-122.46301710605623,NULL,'PENDING'),(18,'apple','Good',3.55476,'2025-05-21',5,1,'2025-02-21','2025-02-28','2025-02-22',0.3,48,86,'2025-02-04',37.80178173153214,-122.43395805358888,NULL,'APPROVED');
/*!40000 ALTER TABLE `trees` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `admin_code` varchar(255) DEFAULT NULL,
  `admin_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`),
  UNIQUE KEY `admin_code` (`admin_code`),
  KEY `FKfji1yi89tedju7pajxn8paefw` (`admin_id`),
  CONSTRAINT `FKfji1yi89tedju7pajxn8paefw` FOREIGN KEY (`admin_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'admin','admin123','ROLE_ADMIN','lugo.gustavo76@yahoo.com',NULL,NULL),(3,'user1','password123','ROLE_USER','user1@example.com',NULL,NULL),(4,'admin1','adminpass','ROLE_ADMIN','admin1@example.com',NULL,NULL),(5,'admin2','password123','ROLE_ADMIN','admin2@example.com','9e6ee0b9-456e-4150-af95-c61f106dd233',NULL),(8,'user2','password123','ROLE_USER','user2@example.com',NULL,5),(10,'Mguar243','123321','ROLE_ADMIN','cool.dude@gmail.net','da987312-dad2-42ea-8771-223546cb2e26',NULL),(11,'Buddy1025','321123','ROLE_USER','ZZZ.fanatic@gmail.net',NULL,10);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `water_lines`
--

DROP TABLE IF EXISTS `water_lines`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `water_lines` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `line_geometry` text NOT NULL,
  `description` text,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `approval_status` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_water_line_admin` (`admin_id`),
  CONSTRAINT `fk_water_line_admin` FOREIGN KEY (`admin_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `water_lines`
--

LOCK TABLES `water_lines` WRITE;
/*!40000 ALTER TABLE `water_lines` DISABLE KEYS */;
INSERT INTO `water_lines` VALUES (1,5,'Main line','[[37.79126906585198,-122.46060848236085],[37.79114626956266,-122.46140241622926],[37.790904633068344,-122.46220707893373],[37.790353529335476,-122.4621105194092]]','connected to meter','2025-02-24 08:46:33','APPROVED'),(2,5,'Mainline','[[37.79191356617443,-122.4638968706131],[37.79156171291622,-122.46321022510529],[37.79150660321799,-122.46299564838411],[37.79171432417373,-122.4626523256302]]','the water line 2/3 white 3ft unnderground','2025-02-24 21:09:25','APPROVED'),(3,5,'Main','[[37.79196019720338,-122.46385395526887],[37.79220606941485,-122.46321022510529],[37.79189660942932,-122.46282398700716]]','1','2025-02-24 21:11:30','PENDING');
/*!40000 ALTER TABLE `water_lines` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-02-25  1:12:37
