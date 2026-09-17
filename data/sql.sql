CREATE DATABASE  IF NOT EXISTS `java` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `java`;
-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: java
-- ------------------------------------------------------
-- Server version	8.0.44

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
-- Table structure for table `accounts`
--

DROP TABLE IF EXISTS `accounts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `accounts` (
  `account_id` bigint NOT NULL AUTO_INCREMENT,
  `employee_id` bigint NOT NULL,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `role_id` int DEFAULT NULL,
  `status` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '1',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`account_id`),
  UNIQUE KEY `username_UNIQUE` (`username`),
  KEY `role_id` (`role_id`),
  KEY `accounts_employee` (`employee_id`),
  CONSTRAINT `accounts_employee` FOREIGN KEY (`employee_id`) REFERENCES `employees` (`employee_id`),
  CONSTRAINT `accounts_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `roles` (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `accounts`
--

LOCK TABLES `accounts` WRITE;
/*!40000 ALTER TABLE `accounts` DISABLE KEYS */;
INSERT INTO `accounts` VALUES (1,1,'admin','$2a$10$STq1cR0NYrP0XbXwl0bhju4NDnKgQuj0HpVFamScrP9AiQoIOEEnO',1,'1','2026-09-16 14:04:26','2026-09-16 21:36:34'),(2,2,'user_01','$2a$10$xnlG750aErLwHDWd4BfNx.QUYFeb4nDRoLkGp6Zf2iePsaunf/ZNW',2,'1','2026-09-16 22:33:14','2026-09-16 22:33:45');
/*!40000 ALTER TABLE `accounts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `actions`
--

DROP TABLE IF EXISTS `actions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `actions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `details` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `actions`
--

LOCK TABLES `actions` WRITE;
/*!40000 ALTER TABLE `actions` DISABLE KEYS */;
INSERT INTO `actions` VALUES (1,'VIEW','Xem dữ liệu'),(2,'ADD','Thêm mới'),(3,'UPDATE','Cập nhật'),(4,'DELETE','Xóa dữ liệu');
/*!40000 ALTER TABLE `actions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cash_ledger`
--

DROP TABLE IF EXISTS `cash_ledger`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cash_ledger` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `type` enum('SALE','CAPITAL_IN','CAPITAL_OUT','PURCHASE') NOT NULL COMMENT 'Loại giao dịch',
  `reference_id` bigint DEFAULT NULL COMMENT 'ID đơn hàng / phiếu nhập / giao dịch admin',
  `amount` decimal(15,2) NOT NULL COMMENT 'Số tiền (+ vào, - ra)',
  `description` varchar(255) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cash_ledger`
--

LOCK TABLES `cash_ledger` WRITE;
/*!40000 ALTER TABLE `cash_ledger` DISABLE KEYS */;
INSERT INTO `cash_ledger` VALUES (1,'PURCHASE',1,-291600000.00,'Thanh toán phiếu nhập #1','2026-09-06 02:00:00'),(2,'PURCHASE',2,-324700000.00,'Thanh toán phiếu nhập #2','2026-09-07 02:00:00'),(3,'PURCHASE',3,-295200000.00,'Thanh toán phiếu nhập #3','2026-09-08 02:00:00'),(4,'PURCHASE',4,-326850000.00,'Thanh toán phiếu nhập #4','2026-09-09 02:00:00'),(5,'PURCHASE',5,-292550000.00,'Thanh toán phiếu nhập #5','2026-09-10 02:00:00'),(6,'PURCHASE',6,-424900000.00,'Thanh toán phiếu nhập #6','2026-09-11 02:00:00'),(7,'PURCHASE',7,-221300000.00,'Thanh toán phiếu nhập #7','2026-09-12 02:00:00'),(8,'PURCHASE',8,-417500000.00,'Thanh toán phiếu nhập #8','2026-09-13 02:00:00'),(9,'PURCHASE',9,-345500000.00,'Thanh toán phiếu nhập #9','2026-09-14 02:00:00'),(10,'PURCHASE',10,-348400000.00,'Thanh toán phiếu nhập #10','2026-09-15 02:00:00'),(11,'SALE',1,16240000.00,'Bán hàng - Order #1','2026-09-16 15:30:50'),(12,'PURCHASE',11,-50000000.00,'Thanh toán phiếu nhập #11','2026-09-16 15:43:28');
/*!40000 ALTER TABLE `cash_ledger` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `categories`
--

DROP TABLE IF EXISTS `categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categories` (
  `category_id` int NOT NULL AUTO_INCREMENT,
  `category_code` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`category_id`),
  UNIQUE KEY `category_code_UNIQUE` (`category_code`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categories`
--

LOCK TABLES `categories` WRITE;
/*!40000 ALTER TABLE `categories` DISABLE KEYS */;
INSERT INTO `categories` VALUES (1,'LAP','Laptop','2026-09-16 14:04:26','2026-09-16 14:04:26'),(2,'MON','Màn hình','2026-09-16 14:04:26','2026-09-16 14:04:26'),(3,'KBD','Bàn phím','2026-09-16 14:04:26','2026-09-16 14:04:26'),(4,'MOU','Chuột','2026-09-16 14:04:26','2026-09-16 14:04:26'),(5,'HDP','Tai nghe','2026-09-16 14:04:26','2026-09-16 14:04:26'),(6,'STO','Lưu trữ','2026-09-16 14:04:26','2026-09-16 14:04:26'),(7,'RAM','RAM','2026-09-16 14:04:26','2026-09-16 14:04:26'),(8,'PHN','Điện thoại','2026-09-16 14:04:26','2026-09-16 14:04:26'),(9,'TAB','Máy tính bảng','2026-09-16 14:04:26','2026-09-16 14:04:26'),(10,'ACC','Phụ kiện','2026-09-16 14:04:26','2026-09-16 14:04:26');
/*!40000 ALTER TABLE `categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `costs`
--

DROP TABLE IF EXISTS `costs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `costs` (
  `cost_id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `profit` decimal(15,2) DEFAULT '0.00',
  `fixed_cost` decimal(15,2) DEFAULT '0.00',
  `status` tinyint(1) DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`cost_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `costs`
--

LOCK TABLES `costs` WRITE;
/*!40000 ALTER TABLE `costs` DISABLE KEYS */;
/*!40000 ALTER TABLE `costs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `customer`
--

DROP TABLE IF EXISTS `customer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customer` (
  `customer_id` bigint NOT NULL AUTO_INCREMENT,
  `customer_name` varchar(45) DEFAULT NULL,
  `phone_number` varchar(10) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`customer_id`),
  UNIQUE KEY `phone_number_UNIQUE` (`phone_number`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customer`
--

LOCK TABLES `customer` WRITE;
/*!40000 ALTER TABLE `customer` DISABLE KEYS */;
INSERT INTO `customer` VALUES (1,'Elon Musk','0987654321','2026-09-16 15:27:29','2026-09-16 15:30:50');
/*!40000 ALTER TABLE `customer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `employees`
--

DROP TABLE IF EXISTS `employees`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `employees` (
  `employee_id` bigint NOT NULL AUTO_INCREMENT,
  `employee_name` varchar(45) NOT NULL,
  `phone_number` varchar(10) NOT NULL,
  `salary` decimal(9,0) DEFAULT '0',
  `status` varchar(45) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`employee_id`),
  UNIQUE KEY `phone_number_UNIQUE` (`phone_number`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `employees`
--

LOCK TABLES `employees` WRITE;
/*!40000 ALTER TABLE `employees` DISABLE KEYS */;
INSERT INTO `employees` VALUES (1,'Quản trị viên','0909000001',0,'ACTIVE','2026-09-16 14:04:26','2026-09-16 14:57:51'),(2,'Nguyễn Văn Đông','0987654321',10000000,'ACTIVE','2026-09-16 15:26:31','2026-09-16 15:26:31'),(3,'Lê Quang Điệp','0987654324',10000000,'ACTIVE','2026-09-16 15:26:52','2026-09-16 15:26:52');
/*!40000 ALTER TABLE `employees` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `item_serials`
--

DROP TABLE IF EXISTS `item_serials`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `item_serials` (
  `receipt_item_id` bigint NOT NULL,
  `serial` varchar(45) NOT NULL,
  `is_used` tinyint NOT NULL DEFAULT '0',
  UNIQUE KEY `serial_UNIQUE` (`serial`),
  KEY `product_idx` (`receipt_item_id`),
  CONSTRAINT `product` FOREIGN KEY (`receipt_item_id`) REFERENCES `receipt_items` (`receipt_item_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `item_serials`
--

LOCK TABLES `item_serials` WRITE;
/*!40000 ALTER TABLE `item_serials` DISABLE KEYS */;
INSERT INTO `item_serials` VALUES (21,'ACERASPIRELITE15-001',0),(21,'ACERASPIRELITE15-002',0),(21,'ACERASPIRELITE15-003',0),(21,'ACERASPIRELITE15-004',0),(21,'ACERASPIRELITE15-005',0),(24,'ACERKA242Y-001',0),(24,'ACERKA242Y-002',0),(24,'ACERKA242Y-003',0),(24,'ACERKA242Y-004',0),(24,'ACERKA242Y-005',0),(23,'ACERNITROV15-001',0),(23,'ACERNITROV15-002',0),(23,'ACERNITROV15-003',0),(23,'ACERNITROV15-004',0),(23,'ACERNITROV15-005',0),(25,'ACEROMW010-001',0),(25,'ACEROMW010-002',0),(25,'ACEROMW010-003',0),(25,'ACEROMW010-004',0),(25,'ACEROMW010-005',0),(22,'ACERSWIFTGO14-001',0),(22,'ACERSWIFTGO14-002',0),(22,'ACERSWIFTGO14-003',0),(22,'ACERSWIFTGO14-004',0),(22,'ACERSWIFTGO14-005',0),(30,'APLAIRPODS4-001',0),(30,'APLAIRPODS4-002',0),(30,'APLAIRPODS4-003',0),(30,'APLAIRPODS4-004',0),(30,'APLAIRPODS4-005',0),(28,'APLIP15128-001',0),(28,'APLIP15128-002',0),(28,'APLIP15128-003',0),(28,'APLIP15128-004',0),(28,'APLIP15128-005',0),(29,'APLIPAD1064-001',0),(29,'APLIPAD1064-002',0),(29,'APLIPAD1064-003',0),(29,'APLIPAD1064-004',0),(29,'APLIPAD1064-005',0),(26,'APLMBA13M3-001',0),(26,'APLMBA13M3-002',0),(26,'APLMBA13M3-003',0),(26,'APLMBA13M3-004',0),(26,'APLMBA13M3-005',0),(27,'APLMBA15M3-001',0),(27,'APLMBA15M3-002',0),(27,'APLMBA15M3-003',0),(27,'APLMBA15M3-004',0),(27,'APLMBA15M3-005',0),(17,'ASUSTUFF15-001',0),(17,'ASUSTUFF15-002',0),(17,'ASUSTUFF15-003',0),(17,'ASUSTUFF15-004',0),(17,'ASUSTUFF15-005',0),(20,'ASUSTUFM3-001',0),(20,'ASUSTUFM3-002',0),(20,'ASUSTUFM3-003',0),(20,'ASUSTUFM3-004',0),(20,'ASUSTUFM3-005',0),(19,'ASUSVA24EHF-001',0),(19,'ASUSVA24EHF-002',0),(19,'ASUSVA24EHF-003',0),(19,'ASUSVA24EHF-004',0),(19,'ASUSVA24EHF-005',0),(16,'ASUSVIVO15X1504-001',0),(16,'ASUSVIVO15X1504-002',0),(16,'ASUSVIVO15X1504-003',0),(16,'ASUSVIVO15X1504-004',0),(16,'ASUSVIVO15X1504-005',0),(18,'ASUSZEN14UX3405-001',0),(18,'ASUSZEN14UX3405-002',0),(18,'ASUSZEN14UX3405-003',0),(18,'ASUSZEN14UX3405-004',0),(18,'ASUSZEN14UX3405-005',0),(1,'DELLINS153530-001',1),(1,'DELLINS153530-002',0),(1,'DELLINS153530-003',0),(1,'DELLINS153530-004',0),(1,'DELLINS153530-005',0),(3,'DELLLAT5450-001',0),(3,'DELLLAT5450-002',0),(3,'DELLLAT5450-003',0),(3,'DELLLAT5450-004',0),(3,'DELLLAT5450-005',0),(4,'DELLP2425H-001',0),(4,'DELLP2425H-002',0),(4,'DELLP2425H-003',0),(4,'DELLP2425H-004',0),(4,'DELLP2425H-005',0),(53,'DELLTEST-001',0),(53,'DELLTEST-002',0),(53,'DELLTEST-003',0),(53,'DELLTEST-004',0),(53,'DELLTEST-005',0),(53,'DELLTEST-006',0),(53,'DELLTEST-007',0),(53,'DELLTEST-008',0),(53,'DELLTEST-009',0),(53,'DELLTEST-010',0),(2,'DELLVOS143430-001',0),(2,'DELLVOS143430-002',0),(2,'DELLVOS143430-003',0),(2,'DELLVOS143430-004',0),(2,'DELLVOS143430-005',0),(5,'DELLWM126-001',0),(5,'DELLWM126-002',0),(5,'DELLWM126-003',0),(5,'DELLWM126-004',0),(5,'DELLWM126-005',0),(11,'HP15FD0306TU-001',0),(11,'HP15FD0306TU-002',0),(11,'HP15FD0306TU-003',0),(11,'HP15FD0306TU-004',0),(11,'HP15FD0306TU-005',0),(15,'HP430MULTI-001',0),(15,'HP430MULTI-002',0),(15,'HP430MULTI-003',0),(15,'HP430MULTI-004',0),(15,'HP430MULTI-005',0),(14,'HPM24F-001',0),(14,'HPM24F-002',0),(14,'HPM24F-003',0),(14,'HPM24F-004',0),(14,'HPM24F-005',0),(12,'HPPAV15EG3095TU-001',0),(12,'HPPAV15EG3095TU-002',0),(12,'HPPAV15EG3095TU-003',0),(12,'HPPAV15EG3095TU-004',0),(12,'HPPAV15EG3095TU-005',0),(13,'HPPRO440G11-001',0),(13,'HPPRO440G11-002',0),(13,'HPPRO440G11-003',0),(13,'HPPRO440G11-004',0),(13,'HPPRO440G11-005',0),(45,'KSTDTX128-001',0),(45,'KSTDTX128-002',0),(45,'KSTDTX128-003',0),(45,'KSTDTX128-004',0),(45,'KSTDTX128-005',0),(43,'KSTFURY163200-001',0),(43,'KSTFURY163200-002',0),(43,'KSTFURY163200-003',0),(43,'KSTFURY163200-004',0),(43,'KSTFURY163200-005',0),(44,'KSTFURY325600-001',0),(44,'KSTFURY325600-002',0),(44,'KSTFURY325600-003',0),(44,'KSTFURY325600-004',0),(44,'KSTFURY325600-005',0),(42,'KSTKC30001TB-001',0),(42,'KSTKC30001TB-002',0),(42,'KSTKC30001TB-003',0),(42,'KSTKC30001TB-004',0),(42,'KSTKC30001TB-005',0),(41,'KSTNV21TB-001',0),(41,'KSTNV21TB-002',0),(41,'KSTNV21TB-003',0),(41,'KSTNV21TB-004',0),(41,'KSTNV21TB-005',0),(10,'LENGX30M39704-001',0),(10,'LENGX30M39704-002',0),(10,'LENGX30M39704-003',0),(10,'LENGX30M39704-004',0),(10,'LENGX30M39704-005',0),(7,'LENIDEA514-001',0),(7,'LENIDEA514-002',0),(7,'LENIDEA514-003',0),(7,'LENIDEA514-004',0),(7,'LENIDEA514-005',0),(9,'LENL24I40-001',0),(9,'LENL24I40-002',0),(9,'LENL24I40-003',0),(9,'LENL24I40-004',0),(9,'LENL24I40-005',0),(6,'LENLOQ15IRX9-001',1),(6,'LENLOQ15IRX9-002',0),(6,'LENLOQ15IRX9-003',0),(6,'LENLOQ15IRX9-004',0),(6,'LENLOQ15IRX9-005',0),(8,'LENTHINKE14-001',0),(8,'LENTHINKE14-002',0),(8,'LENTHINKE14-003',0),(8,'LENTHINKE14-004',0),(8,'LENTHINKE14-005',0),(40,'LOGIG435-001',0),(40,'LOGIG435-002',0),(40,'LOGIG435-003',0),(40,'LOGIG435-004',0),(40,'LOGIG435-005',0),(37,'LOGIG502X-001',0),(37,'LOGIG502X-002',0),(37,'LOGIG502X-003',0),(37,'LOGIG502X-004',0),(37,'LOGIG502X-005',0),(38,'LOGIK380S-001',0),(38,'LOGIK380S-002',0),(38,'LOGIK380S-003',0),(38,'LOGIK380S-004',0),(38,'LOGIK380S-005',0),(39,'LOGIMXKEYSS-001',0),(39,'LOGIMXKEYSS-002',0),(39,'LOGIMXKEYSS-003',0),(39,'LOGIMXKEYSS-004',0),(39,'LOGIMXKEYSS-005',0),(36,'LOGIMXMASTER3S-001',0),(36,'LOGIMXMASTER3S-002',0),(36,'LOGIMXMASTER3S-003',0),(36,'LOGIMXMASTER3S-004',0),(36,'LOGIMXMASTER3S-005',0),(50,'MSICLUTCHGM41-001',0),(50,'MSICLUTCHGM41-002',0),(50,'MSICLUTCHGM41-003',0),(50,'MSICLUTCHGM41-004',0),(50,'MSICLUTCHGM41-005',0),(46,'MSICYBORG15A13V-001',0),(46,'MSICYBORG15A13V-002',0),(46,'MSICYBORG15A13V-003',0),(46,'MSICYBORG15A13V-004',0),(46,'MSICYBORG15A13V-005',0),(49,'MSIG274QPF-001',0),(49,'MSIG274QPF-002',0),(49,'MSIG274QPF-003',0),(49,'MSIG274QPF-004',0),(49,'MSIG274QPF-005',0),(47,'MSIKATANA15B13V-001',0),(47,'MSIKATANA15B13V-002',0),(47,'MSIKATANA15B13V-003',0),(47,'MSIKATANA15B13V-004',0),(47,'MSIKATANA15B13V-005',0),(48,'MSIMODERN14C13M-001',0),(48,'MSIMODERN14C13M-002',0),(48,'MSIMODERN14C13M-003',0),(48,'MSIMODERN14C13M-004',0),(48,'MSIMODERN14C13M-005',0),(54,'SN-001',0),(54,'SN-002',0),(54,'SN-003',0),(54,'SN-004',0),(54,'SN-005',0),(54,'SN-006',0),(54,'SN-007',0),(54,'SN-008',0),(54,'SN-009',0),(54,'SN-010',0),(32,'SSA55256-001',0),(32,'SSA55256-002',0),(32,'SSA55256-003',0),(32,'SSA55256-004',0),(32,'SSA55256-005',0),(34,'SSODYSSEYG5-001',0),(34,'SSODYSSEYG5-002',0),(34,'SSODYSSEYG5-003',0),(34,'SSODYSSEYG5-004',0),(34,'SSODYSSEYG5-005',0),(31,'SSS24256-001',0),(31,'SSS24256-002',0),(31,'SSS24256-003',0),(31,'SSS24256-004',0),(31,'SSS24256-005',0),(35,'SST71TB-001',0),(35,'SST71TB-002',0),(35,'SST71TB-003',0),(35,'SST71TB-004',0),(35,'SST71TB-005',0),(33,'SSTABS9FE-001',0),(33,'SSTABS9FE-002',0),(33,'SSTABS9FE-003',0),(33,'SSTABS9FE-004',0),(33,'SSTABS9FE-005',0);
/*!40000 ALTER TABLE `item_serials` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_item_serials_before_insert` BEFORE INSERT ON `item_serials` FOR EACH ROW BEGIN
    DECLARE v_status TINYINT DEFAULT 0;
    SELECT COALESCE(MAX(r.status), 0)
      INTO v_status
      FROM receipt_items ri
      JOIN receipts r ON r.receipt_id = ri.receipt_id
     WHERE ri.receipt_item_id = NEW.receipt_item_id;

    IF v_status = 1 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Khong duoc them serial vao phieu nhap da hoan tat.';
    END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_item_serials_before_update` BEFORE UPDATE ON `item_serials` FOR EACH ROW BEGIN
    DECLARE v_old_status TINYINT DEFAULT 0;
    DECLARE v_new_status TINYINT DEFAULT 0;

    -- Cho phép đổi is_used khi bán/hoàn hàng, nhưng không cho đổi serial hoặc receipt_item_id
    -- của một phiếu đã hoàn tất.
    IF NOT (NEW.serial <=> OLD.serial)
       OR NOT (NEW.receipt_item_id <=> OLD.receipt_item_id) THEN

        SELECT COALESCE(MAX(r.status), 0)
          INTO v_old_status
          FROM receipt_items ri
          JOIN receipts r ON r.receipt_id = ri.receipt_id
         WHERE ri.receipt_item_id = OLD.receipt_item_id;

        SELECT COALESCE(MAX(r.status), 0)
          INTO v_new_status
          FROM receipt_items ri
          JOIN receipts r ON r.receipt_id = ri.receipt_id
         WHERE ri.receipt_item_id = NEW.receipt_item_id;

        IF v_old_status = 1 OR v_new_status = 1 THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Khong duoc doi serial/receipt_item cua phieu nhap da hoan tat.';
        END IF;
    END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_item_serials_before_delete` BEFORE DELETE ON `item_serials` FOR EACH ROW BEGIN
    DECLARE v_status TINYINT DEFAULT 0;
    SELECT COALESCE(MAX(r.status), 0)
      INTO v_status
      FROM receipt_items ri
      JOIN receipts r ON r.receipt_id = ri.receipt_id
     WHERE ri.receipt_item_id = OLD.receipt_item_id;

    IF v_status = 1 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Khong duoc xoa serial cua phieu nhap da hoan tat.';
    END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `module_actions`
--

DROP TABLE IF EXISTS `module_actions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `module_actions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `module_id` int DEFAULT NULL,
  `action_id` int DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `module_id` (`module_id`),
  KEY `action_id` (`action_id`),
  CONSTRAINT `module_actions_ibfk_1` FOREIGN KEY (`module_id`) REFERENCES `modules` (`id`) ON DELETE CASCADE,
  CONSTRAINT `module_actions_ibfk_2` FOREIGN KEY (`action_id`) REFERENCES `actions` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=64 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `module_actions`
--

LOCK TABLES `module_actions` WRITE;
/*!40000 ALTER TABLE `module_actions` DISABLE KEYS */;
INSERT INTO `module_actions` VALUES (1,1,1,'2026-09-16 14:04:26','2026-09-16 21:50:08'),(2,1,2,'2026-09-16 14:04:26','2026-09-16 21:50:08'),(3,1,3,'2026-09-16 14:04:26','2026-09-16 21:50:08'),(4,1,4,'2026-09-16 14:04:26','2026-09-16 21:50:08'),(5,2,1,'2026-09-16 14:04:26','2026-09-16 21:50:08'),(6,2,2,'2026-09-16 14:04:26','2026-09-16 21:50:08'),(7,2,3,'2026-09-16 14:04:26','2026-09-16 21:50:08'),(8,2,4,'2026-09-16 14:04:26','2026-09-16 21:50:08'),(9,3,1,'2026-09-16 14:04:26','2026-09-16 21:50:08'),(10,3,2,'2026-09-16 14:04:26','2026-09-16 21:50:08'),(11,3,3,'2026-09-16 14:04:26','2026-09-16 21:50:08'),(12,3,4,'2026-09-16 14:04:26','2026-09-16 21:50:08'),(13,4,1,'2026-09-16 14:04:26','2026-09-16 21:50:08'),(14,4,2,'2026-09-16 14:04:26','2026-09-16 21:50:08'),(15,4,3,'2026-09-16 14:04:26','2026-09-16 21:50:08'),(16,4,4,'2026-09-16 14:04:26','2026-09-16 21:50:08'),(17,5,1,'2026-09-16 14:04:26','2026-09-16 21:50:08'),(18,5,2,'2026-09-16 14:04:26','2026-09-16 21:50:08'),(19,5,3,'2026-09-16 14:04:26','2026-09-16 21:50:08'),(20,5,4,'2026-09-16 14:04:26','2026-09-16 21:50:08'),(21,6,1,'2026-09-16 14:04:26','2026-09-16 21:50:08'),(22,6,2,'2026-09-16 14:04:26','2026-09-16 21:50:08'),(23,6,3,'2026-09-16 14:04:26','2026-09-16 21:50:08'),(24,6,4,'2026-09-16 14:04:26','2026-09-16 21:50:08'),(25,7,1,'2026-09-16 14:04:26','2026-09-16 21:50:08'),(26,7,2,'2026-09-16 14:04:26','2026-09-16 21:50:08'),(27,7,3,'2026-09-16 14:04:26','2026-09-16 21:50:08'),(28,7,4,'2026-09-16 14:04:26','2026-09-16 21:50:08'),(29,8,1,'2026-09-16 14:04:26','2026-09-16 21:50:08'),(30,8,2,'2026-09-16 14:04:26','2026-09-16 21:50:08'),(31,8,3,'2026-09-16 14:04:26','2026-09-16 21:50:08'),(32,8,4,'2026-09-16 14:04:26','2026-09-16 21:50:08'),(33,9,1,'2026-09-16 14:04:26','2026-09-16 21:50:08'),(34,9,2,'2026-09-16 14:04:26','2026-09-16 21:50:08'),(35,9,3,'2026-09-16 14:04:26','2026-09-16 21:50:08'),(36,9,4,'2026-09-16 14:04:26','2026-09-16 21:50:08'),(37,10,1,'2026-09-16 14:04:26','2026-09-16 21:50:08'),(38,10,2,'2026-09-16 14:04:26','2026-09-16 21:50:08'),(39,10,3,'2026-09-16 14:04:26','2026-09-16 21:50:08'),(40,10,4,'2026-09-16 14:04:26','2026-09-16 21:50:08'),(41,11,1,'2026-09-16 14:04:26','2026-09-16 21:50:08'),(42,11,2,'2026-09-16 14:04:26','2026-09-16 21:50:08'),(43,11,3,'2026-09-16 14:04:26','2026-09-16 21:50:08'),(44,11,4,'2026-09-16 14:04:26','2026-09-16 21:50:08'),(45,12,1,'2026-09-16 14:04:26','2026-09-16 21:50:08'),(46,12,2,'2026-09-16 14:04:26','2026-09-16 21:50:08'),(47,12,3,'2026-09-16 14:04:26','2026-09-16 21:50:08'),(48,12,4,'2026-09-16 14:04:26','2026-09-16 21:50:08'),(49,13,1,'2026-09-16 14:04:26','2026-09-16 21:50:08'),(50,13,2,'2026-09-16 14:04:26','2026-09-16 21:50:08'),(51,13,3,'2026-09-16 14:04:26','2026-09-16 21:50:08'),(52,13,4,'2026-09-16 14:04:26','2026-09-16 21:50:08');
/*!40000 ALTER TABLE `module_actions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `module_groups`
--

DROP TABLE IF EXISTS `module_groups`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `module_groups` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `module_groups`
--

LOCK TABLES `module_groups` WRITE;
/*!40000 ALTER TABLE `module_groups` DISABLE KEYS */;
INSERT INTO `module_groups` VALUES (1,'Quản Lý Chung'),(2,'Quản Lý Sản Phẩm'),(3,'Quản Lý Đơn Hàng'),(4,'Quản Lý Tài Khoản'),(5,'Báo Cáo & Thống Kê');
/*!40000 ALTER TABLE `module_groups` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `modules`
--

DROP TABLE IF EXISTS `modules`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `modules` (
  `id` int NOT NULL AUTO_INCREMENT,
  `code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `module_groups_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_module_group` (`module_groups_id`),
  CONSTRAINT `fk_module_group` FOREIGN KEY (`module_groups_id`) REFERENCES `module_groups` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `modules`
--

LOCK TABLES `modules` WRITE;
/*!40000 ALTER TABLE `modules` DISABLE KEYS */;
INSERT INTO `modules` VALUES (1,'CATEGORY','Phân loại sản phẩm',1),(2,'SUPPLIER','Nhà cung cấp',1),(3,'PROMOTION','Khuyến mãi',1),(4,'EMPLOYEE','Nhân viên',1),(5,'WARRANTY','Bảo hành',1),(6,'STORE','Cửa Hàng',2),(7,'WAREHOUSE','Tồn Kho',2),(8,'RECEIPT','Phiếu Nhập',2),(9,'ORDER','Đơn hàng',3),(10,'HISTORY','Lịch Sử Giao Dịch',3),(11,'ACCOUNT','Tài khoản',4),(12,'PERMISSION','Phân quyền',4),(13,'REPORT','Báo cáo - thống kê',5);
/*!40000 ALTER TABLE `modules` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_items`
--

DROP TABLE IF EXISTS `order_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_items` (
  `order_item_id` int NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  `original_price` decimal(15,2) NOT NULL,
  `unit_price` decimal(15,2) NOT NULL,
  `receipt_id` bigint NOT NULL,
  `product_serial` varchar(45) NOT NULL,
  PRIMARY KEY (`order_item_id`),
  KEY `order_id` (`order_id`),
  KEY `product_id` (`product_id`),
  KEY `product_receipt_id_idx` (`receipt_id`),
  KEY `product_serial_idx` (`product_serial`),
  CONSTRAINT `order_items_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`),
  CONSTRAINT `order_items_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`),
  CONSTRAINT `product_receipt_id` FOREIGN KEY (`receipt_id`) REFERENCES `receipts` (`receipt_id`),
  CONSTRAINT `product_serial` FOREIGN KEY (`product_serial`) REFERENCES `item_serials` (`serial`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_items`
--

LOCK TABLES `order_items` WRITE;
/*!40000 ALTER TABLE `order_items` DISABLE KEYS */;
INSERT INTO `order_items` VALUES (1,1,1,16240000.00,16240000.00,1,'DELLINS153530-001'),(2,1,6,0.00,0.00,2,'LENLOQ15IRX9-001');
/*!40000 ALTER TABLE `order_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `order_id` bigint NOT NULL AUTO_INCREMENT,
  `customer_id` bigint DEFAULT NULL,
  `promotion_id` int DEFAULT NULL,
  `total` decimal(15,2) DEFAULT NULL,
  `payment_method` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_user` bigint DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`order_id`),
  KEY `promotion_id` (`promotion_id`),
  KEY `order_customer_idx` (`customer_id`),
  KEY `order_user_idx` (`created_user`),
  CONSTRAINT `order_customer` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`customer_id`),
  CONSTRAINT `order_user` FOREIGN KEY (`created_user`) REFERENCES `accounts` (`account_id`),
  CONSTRAINT `orders_ibfk_2` FOREIGN KEY (`promotion_id`) REFERENCES `promotions` (`promotion_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` VALUES (1,1,2,16240000.00,'Tiền Mặt',1,'2026-09-16 22:27:17');
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_images`
--

DROP TABLE IF EXISTS `product_images`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_images` (
  `product_image_id` bigint NOT NULL AUTO_INCREMENT,
  `product_id` bigint NOT NULL,
  `image_url` longblob NOT NULL,
  PRIMARY KEY (`product_image_id`),
  KEY `product_id` (`product_id`),
  CONSTRAINT `product_images_ibfk_1` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_images`
--

LOCK TABLES `product_images` WRITE;
/*!40000 ALTER TABLE `product_images` DISABLE KEYS */;
/*!40000 ALTER TABLE `product_images` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `products`
--

DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products` (
  `product_id` bigint NOT NULL AUTO_INCREMENT,
  `product_code` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `category_id` int DEFAULT NULL,
  `stock` int DEFAULT '0',
  `unit_id` bigint DEFAULT NULL,
  `avg_unit_price` decimal(15,2) DEFAULT '0.00',
  `profit` decimal(5,2) DEFAULT NULL,
  `sell_price` decimal(15,2) DEFAULT '0.00',
  `is_on_sale` tinyint(1) DEFAULT '0',
  `is_active` tinyint DEFAULT '1',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`product_id`),
  UNIQUE KEY `product_code_UNIQUE` (`product_code`),
  KEY `category_id` (`category_id`),
  CONSTRAINT `products_ibfk_1` FOREIGN KEY (`category_id`) REFERENCES `categories` (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=51 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `products`
--

LOCK TABLES `products` WRITE;
/*!40000 ALTER TABLE `products` DISABLE KEYS */;
INSERT INTO `products` VALUES (1,'DELL-INS15-3530','Dell Inspiron 15 3530',1,14,1,14500000.00,0.12,14500000.00,1,1,'2026-09-16 14:04:26','2026-09-16 22:43:28'),(2,'DELL-VOS14-3430','Dell Vostro 14 3430',1,15,1,15800000.00,0.12,15800000.00,1,1,'2026-09-16 14:04:26','2026-09-16 22:43:28'),(3,'DELL-LAT5450','Dell Latitude 5450',1,5,1,23500000.00,0.10,23500000.00,0,1,'2026-09-16 14:04:26','2026-09-16 21:07:44'),(4,'DELL-P2425H','Dell P2425H 24 inch',2,5,1,4200000.00,0.15,4200000.00,0,1,'2026-09-16 14:04:26','2026-09-16 21:07:44'),(5,'DELL-WM126','Dell Wireless Mouse WM126',4,5,1,320000.00,0.20,320000.00,0,1,'2026-09-16 14:04:26','2026-09-16 21:07:44'),(6,'LEN-LOQ15IRX9','Lenovo LOQ 15IRX9',1,4,1,21500000.00,0.12,21500000.00,0,1,'2026-09-16 14:04:26','2026-09-16 22:30:50'),(7,'LEN-IDEA5-14','Lenovo IdeaPad Slim 5 14',1,5,1,16900000.00,0.12,16900000.00,0,1,'2026-09-16 14:04:26','2026-09-16 21:07:44'),(8,'LEN-THINK-E14','Lenovo ThinkPad E14 Gen 6',1,5,1,22600000.00,0.10,22600000.00,0,1,'2026-09-16 14:04:26','2026-09-16 21:07:44'),(9,'LEN-L24I40','Lenovo L24i-40 23.8 inch',2,5,1,3650000.00,0.15,3650000.00,0,1,'2026-09-16 14:04:26','2026-09-16 21:07:44'),(10,'LEN-GX30M39704','Lenovo 300 Wireless Mouse',4,5,1,290000.00,0.20,290000.00,0,1,'2026-09-16 14:04:26','2026-09-16 21:07:44'),(11,'HP-15-FD0306TU','HP 15 fd0306TU',1,5,1,13900000.00,0.12,13900000.00,0,1,'2026-09-16 14:04:26','2026-09-16 21:07:44'),(12,'HP-PAV15-EG3095TU','HP Pavilion 15 eg3095TU',1,5,1,18400000.00,0.12,18400000.00,0,1,'2026-09-16 14:04:26','2026-09-16 21:07:44'),(13,'HP-PRO440-G11','HP ProBook 440 G11',1,5,1,22800000.00,0.10,22800000.00,0,1,'2026-09-16 14:04:26','2026-09-16 21:07:44'),(14,'HP-M24F','HP M24f 23.8 inch',2,5,1,3350000.00,0.15,3350000.00,0,1,'2026-09-16 14:04:26','2026-09-16 21:07:44'),(15,'HP-430-MULTI','HP 430 Multi-Device Mouse',4,5,1,590000.00,0.20,590000.00,0,1,'2026-09-16 14:04:26','2026-09-16 21:07:44'),(16,'ASUS-VIVO15-X1504','ASUS Vivobook 15 X1504',1,5,1,14200000.00,0.12,14200000.00,0,1,'2026-09-16 14:04:26','2026-09-16 21:07:44'),(17,'ASUS-TUF-F15','ASUS TUF Gaming F15',1,5,1,21900000.00,0.12,21900000.00,0,1,'2026-09-16 14:04:26','2026-09-16 21:07:44'),(18,'ASUS-ZEN14-UX3405','ASUS Zenbook 14 OLED UX3405',1,5,1,25900000.00,0.10,25900000.00,0,1,'2026-09-16 14:04:26','2026-09-16 21:07:44'),(19,'ASUS-VA24EHF','ASUS VA24EHF 23.8 inch',2,5,1,2950000.00,0.15,2950000.00,0,1,'2026-09-16 14:04:26','2026-09-16 21:07:44'),(20,'ASUS-TUF-M3','ASUS TUF Gaming M3 Mouse',4,5,1,420000.00,0.20,420000.00,0,1,'2026-09-16 14:04:26','2026-09-16 21:07:44'),(21,'ACER-ASPIRE-LITE15','Acer Aspire Lite 15',1,5,1,12500000.00,0.12,12500000.00,0,1,'2026-09-16 14:04:26','2026-09-16 21:07:44'),(22,'ACER-SWIFT-GO14','Acer Swift Go 14',1,5,1,20500000.00,0.12,20500000.00,0,1,'2026-09-16 14:04:26','2026-09-16 21:07:44'),(23,'ACER-NITRO-V15','Acer Nitro V 15',1,5,1,22500000.00,0.12,22500000.00,0,1,'2026-09-16 14:04:26','2026-09-16 21:07:44'),(24,'ACER-KA242Y','Acer KA242Y 23.8 inch',2,5,1,2750000.00,0.15,2750000.00,0,1,'2026-09-16 14:04:26','2026-09-16 21:07:44'),(25,'ACER-OMW010','Acer Wireless Mouse OMW010',4,5,1,260000.00,0.20,260000.00,0,1,'2026-09-16 14:04:26','2026-09-16 21:07:44'),(26,'APL-MBA13-M3','MacBook Air 13 M3',1,5,1,24900000.00,0.08,24900000.00,0,1,'2026-09-16 14:04:26','2026-09-16 21:07:44'),(27,'APL-MBA15-M3','MacBook Air 15 M3',1,5,1,29900000.00,0.08,29900000.00,0,1,'2026-09-16 14:04:26','2026-09-16 21:07:44'),(28,'APL-IP15-128','iPhone 15 128GB',8,5,1,17900000.00,0.08,17900000.00,0,1,'2026-09-16 14:04:26','2026-09-16 21:07:44'),(29,'APL-IPAD10-64','iPad Gen 10 64GB',9,5,1,8990000.00,0.10,8990000.00,0,1,'2026-09-16 14:04:26','2026-09-16 21:07:44'),(30,'APL-AIRPODS4','Apple AirPods 4',5,5,1,3290000.00,0.15,3290000.00,0,1,'2026-09-16 14:04:26','2026-09-16 21:07:44'),(31,'SS-S24-256','Samsung Galaxy S24 256GB',8,5,1,17400000.00,0.10,17400000.00,0,1,'2026-09-16 14:04:26','2026-09-16 21:07:44'),(32,'SS-A55-256','Samsung Galaxy A55 256GB',8,5,1,8790000.00,0.12,8790000.00,0,1,'2026-09-16 14:04:26','2026-09-16 21:07:44'),(33,'SS-TABS9FE','Samsung Galaxy Tab S9 FE',9,5,1,9990000.00,0.12,9990000.00,0,1,'2026-09-16 14:04:26','2026-09-16 21:07:44'),(34,'SS-ODYSSEY-G5','Samsung Odyssey G5 27 inch',2,5,1,5890000.00,0.15,5890000.00,0,1,'2026-09-16 14:04:26','2026-09-16 21:07:44'),(35,'SS-T7-1TB','Samsung Portable SSD T7 1TB',6,5,1,2190000.00,0.18,2190000.00,0,1,'2026-09-16 14:04:26','2026-09-16 21:07:44'),(36,'LOGI-MXMASTER3S','Logitech MX Master 3S',4,5,1,2190000.00,0.18,2190000.00,0,1,'2026-09-16 14:04:26','2026-09-16 21:07:44'),(37,'LOGI-G502X','Logitech G502 X',4,5,1,1590000.00,0.18,1590000.00,0,1,'2026-09-16 14:04:26','2026-09-16 21:07:44'),(38,'LOGI-K380S','Logitech Pebble Keys 2 K380s',3,5,1,890000.00,0.20,890000.00,0,1,'2026-09-16 14:04:26','2026-09-16 21:07:44'),(39,'LOGI-MXKEYS-S','Logitech MX Keys S',3,5,1,2390000.00,0.18,2390000.00,0,1,'2026-09-16 14:04:26','2026-09-16 21:07:44'),(40,'LOGI-G435','Logitech G435 Lightspeed',5,5,1,1290000.00,0.20,1290000.00,0,1,'2026-09-16 14:04:26','2026-09-16 21:07:44'),(41,'KST-NV2-1TB','Kingston NV2 NVMe 1TB',6,5,1,1450000.00,0.18,1450000.00,0,1,'2026-09-16 14:04:26','2026-09-16 21:07:44'),(42,'KST-KC3000-1TB','Kingston KC3000 NVMe 1TB',6,5,1,2190000.00,0.18,2190000.00,0,1,'2026-09-16 14:04:26','2026-09-16 21:07:44'),(43,'KST-FURY16-3200','Kingston Fury Beast 16GB DDR4',7,5,1,850000.00,0.20,850000.00,0,1,'2026-09-16 14:04:26','2026-09-16 21:07:44'),(44,'KST-FURY32-5600','Kingston Fury Beast 32GB DDR5',7,5,1,2190000.00,0.20,2190000.00,0,1,'2026-09-16 14:04:26','2026-09-16 21:07:44'),(45,'KST-DTX-128','Kingston DataTraveler Exodia 128GB',6,5,1,230000.00,0.25,230000.00,0,1,'2026-09-16 14:04:26','2026-09-16 21:07:44'),(46,'MSI-CYBORG15-A13V','MSI Cyborg 15 A13V',1,5,1,22900000.00,0.12,22900000.00,0,1,'2026-09-16 14:04:26','2026-09-16 21:07:44'),(47,'MSI-KATANA15-B13V','MSI Katana 15 B13V',1,5,1,25900000.00,0.12,25900000.00,0,1,'2026-09-16 14:04:26','2026-09-16 21:07:44'),(48,'MSI-MODERN14-C13M','MSI Modern 14 C13M',1,5,1,13900000.00,0.12,13900000.00,0,1,'2026-09-16 14:04:26','2026-09-16 21:07:44'),(49,'MSI-G274QPF','MSI G274QPF E2 27 inch',2,5,1,5990000.00,0.15,5990000.00,0,1,'2026-09-16 14:04:26','2026-09-16 21:07:44'),(50,'MSI-CLUTCH-GM41','MSI Clutch GM41 Lightweight',4,5,1,990000.00,0.20,990000.00,0,1,'2026-09-16 14:04:26','2026-09-16 21:07:44');
/*!40000 ALTER TABLE `products` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `promotion_items`
--

DROP TABLE IF EXISTS `promotion_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `promotion_items` (
  `promotion_id` int NOT NULL,
  `product_id` bigint NOT NULL,
  `gifted_product_id` bigint DEFAULT NULL,
  `discount_percent` int DEFAULT '0',
  KEY `promotion_id` (`promotion_id`),
  KEY `PRODUCT_ID_idx` (`product_id`),
  KEY `promotion_product_idx` (`gifted_product_id`),
  CONSTRAINT `PRODUCT_ID` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`) ON DELETE CASCADE,
  CONSTRAINT `promotion_items_ibfk_1` FOREIGN KEY (`promotion_id`) REFERENCES `promotions` (`promotion_id`) ON DELETE CASCADE,
  CONSTRAINT `promotion_product` FOREIGN KEY (`gifted_product_id`) REFERENCES `products` (`product_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `promotion_items`
--

LOCK TABLES `promotion_items` WRITE;
/*!40000 ALTER TABLE `promotion_items` DISABLE KEYS */;
INSERT INTO `promotion_items` VALUES (2,1,6,0),(1,2,NULL,55);
/*!40000 ALTER TABLE `promotion_items` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_promotion_item_after_insert` AFTER INSERT ON `promotion_items` FOR EACH ROW BEGIN
    UPDATE products p
    SET p.is_on_sale = CASE WHEN EXISTS (
        SELECT 1
        FROM promotion_items pi2
        JOIN promotions pr2 ON pr2.promotion_id = pi2.promotion_id
        WHERE pi2.product_id = NEW.product_id
          AND pr2.status = 1
    ) THEN 1 ELSE 0 END
    WHERE p.product_id = NEW.product_id;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_promotion_item_after_update` AFTER UPDATE ON `promotion_items` FOR EACH ROW BEGIN
    UPDATE products p
    SET p.is_on_sale = CASE WHEN EXISTS (
        SELECT 1
        FROM promotion_items pi2
        JOIN promotions pr2 ON pr2.promotion_id = pi2.promotion_id
        WHERE pi2.product_id = p.product_id
          AND pr2.status = 1
    ) THEN 1 ELSE 0 END
    WHERE p.product_id IN (OLD.product_id, NEW.product_id);
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_promotion_item_after_delete` AFTER DELETE ON `promotion_items` FOR EACH ROW BEGIN
    UPDATE products p
    SET p.is_on_sale = CASE WHEN EXISTS (
        SELECT 1
        FROM promotion_items pi2
        JOIN promotions pr2 ON pr2.promotion_id = pi2.promotion_id
        WHERE pi2.product_id = OLD.product_id
          AND pr2.status = 1
    ) THEN 1 ELSE 0 END
    WHERE p.product_id = OLD.product_id;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `promotions`
--

DROP TABLE IF EXISTS `promotions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `promotions` (
  `promotion_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `promotion_type` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `min_paid` decimal(15,2) DEFAULT NULL,
  `discount_on_bill` int DEFAULT '0',
  `available_time` int DEFAULT NULL,
  `is_auto` tinyint(1) DEFAULT '0',
  `start_at` datetime DEFAULT NULL,
  `end_at` datetime DEFAULT NULL,
  `status` tinyint(1) DEFAULT '1',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`promotion_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `promotions`
--

LOCK TABLES `promotions` WRITE;
/*!40000 ALTER TABLE `promotions` DISABLE KEYS */;
INSERT INTO `promotions` VALUES (1,'sale cuối tuần','PERCENT',0.00,0,0,1,'2026-09-16 00:00:00','2026-12-28 00:00:00',1,'2026-09-16 22:23:28','2026-09-16 22:36:21'),(2,'tặng kèm','GIFT',0.00,0,NULL,1,'2026-09-16 00:00:00','2026-12-11 00:00:00',1,'2026-09-16 22:24:18','2026-09-16 22:24:18'),(3,'Giảm theo BILL','BILL',1000000.00,10,0,1,'2026-09-16 00:00:00','2027-09-05 00:00:00',1,'2026-09-16 22:25:52','2026-09-16 22:36:12');
/*!40000 ALTER TABLE `promotions` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_promotion_status_update` AFTER UPDATE ON `promotions` FOR EACH ROW BEGIN
    IF NOT (NEW.status <=> OLD.status) THEN
        UPDATE products p
        JOIN (
            SELECT DISTINCT product_id
            FROM promotion_items
            WHERE promotion_id = NEW.promotion_id
        ) affected ON affected.product_id = p.product_id
        SET p.is_on_sale = CASE WHEN EXISTS (
            SELECT 1
            FROM promotion_items pi2
            JOIN promotions pr2 ON pr2.promotion_id = pi2.promotion_id
            WHERE pi2.product_id = p.product_id
              AND pr2.status = 1
        ) THEN 1 ELSE 0 END;
    END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_promotion_before_delete` BEFORE DELETE ON `promotions` FOR EACH ROW BEGIN
    -- FK cascade trên promotion_items không đảm bảo child trigger chạy ở MySQL 8.0,
    -- nên phải cập nhật is_on_sale trước khi xóa promotion cha.
    UPDATE products p
    JOIN (
        SELECT DISTINCT product_id
        FROM promotion_items
        WHERE promotion_id = OLD.promotion_id
    ) affected ON affected.product_id = p.product_id
    SET p.is_on_sale = CASE WHEN EXISTS (
        SELECT 1
        FROM promotion_items pi2
        JOIN promotions pr2 ON pr2.promotion_id = pi2.promotion_id
        WHERE pi2.product_id = p.product_id
          AND pi2.promotion_id <> OLD.promotion_id
          AND pr2.status = 1
    ) THEN 1 ELSE 0 END;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `rbac`
--

DROP TABLE IF EXISTS `rbac`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rbac` (
  `role_id` int NOT NULL,
  `module_action_id` int NOT NULL,
  `status` tinyint(1) DEFAULT '1',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`role_id`,`module_action_id`),
  KEY `permission_id` (`module_action_id`),
  CONSTRAINT `rbac_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `roles` (`role_id`),
  CONSTRAINT `rbac_ibfk_2` FOREIGN KEY (`module_action_id`) REFERENCES `module_actions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rbac`
--

LOCK TABLES `rbac` WRITE;
/*!40000 ALTER TABLE `rbac` DISABLE KEYS */;
INSERT INTO `rbac` VALUES (1,1,1,'2026-09-16 21:51:58'),(1,2,1,'2026-09-16 21:51:58'),(1,3,1,'2026-09-16 21:51:58'),(1,4,1,'2026-09-16 21:51:58'),(1,5,1,'2026-09-16 21:51:58'),(1,6,1,'2026-09-16 21:51:58'),(1,7,1,'2026-09-16 21:51:58'),(1,8,1,'2026-09-16 21:51:58'),(1,9,1,'2026-09-16 21:51:58'),(1,10,1,'2026-09-16 21:51:58'),(1,11,1,'2026-09-16 21:51:58'),(1,12,1,'2026-09-16 21:51:58'),(1,13,1,'2026-09-16 21:51:58'),(1,14,1,'2026-09-16 21:51:58'),(1,15,1,'2026-09-16 21:51:58'),(1,16,1,'2026-09-16 21:51:58'),(1,17,1,'2026-09-16 21:51:58'),(1,18,1,'2026-09-16 21:51:58'),(1,19,1,'2026-09-16 21:51:58'),(1,20,1,'2026-09-16 21:51:58'),(1,21,1,'2026-09-16 21:51:58'),(1,22,1,'2026-09-16 21:51:58'),(1,23,1,'2026-09-16 21:51:58'),(1,24,1,'2026-09-16 21:51:58'),(1,25,1,'2026-09-16 21:51:58'),(1,26,1,'2026-09-16 21:51:58'),(1,27,1,'2026-09-16 21:51:58'),(1,28,1,'2026-09-16 21:51:58'),(1,29,1,'2026-09-16 21:51:58'),(1,30,1,'2026-09-16 21:51:58'),(1,31,1,'2026-09-16 21:51:58'),(1,32,1,'2026-09-16 21:51:58'),(1,33,1,'2026-09-16 21:51:58'),(1,34,1,'2026-09-16 21:51:58'),(1,35,1,'2026-09-16 21:51:58'),(1,36,1,'2026-09-16 21:51:58'),(1,37,1,'2026-09-16 21:51:58'),(1,38,1,'2026-09-16 21:51:58'),(1,39,1,'2026-09-16 21:51:58'),(1,40,1,'2026-09-16 21:51:58'),(1,41,1,'2026-09-16 21:51:58'),(1,42,1,'2026-09-16 21:51:58'),(1,43,1,'2026-09-16 21:51:58'),(1,44,1,'2026-09-16 21:51:58'),(1,45,1,'2026-09-16 21:51:58'),(1,46,1,'2026-09-16 21:51:58'),(1,47,1,'2026-09-16 21:51:58'),(1,48,1,'2026-09-16 21:51:58'),(1,49,1,'2026-09-16 21:51:58'),(1,50,1,'2026-09-16 21:51:58'),(1,51,1,'2026-09-16 21:51:58'),(1,52,1,'2026-09-16 21:51:58'),(2,1,1,'2026-09-16 22:33:38'),(2,2,1,'2026-09-16 22:33:38'),(2,3,0,'2026-09-16 22:33:38'),(2,4,0,'2026-09-16 22:33:38'),(2,5,1,'2026-09-16 22:33:38'),(2,6,1,'2026-09-16 22:33:38'),(2,7,0,'2026-09-16 22:33:38'),(2,8,0,'2026-09-16 22:33:38'),(2,9,1,'2026-09-16 22:33:38'),(2,10,1,'2026-09-16 22:33:38'),(2,11,0,'2026-09-16 22:33:38'),(2,12,0,'2026-09-16 22:33:38'),(2,13,1,'2026-09-16 22:33:38'),(2,14,1,'2026-09-16 22:33:38'),(2,15,0,'2026-09-16 22:33:38'),(2,16,0,'2026-09-16 22:33:38'),(2,17,1,'2026-09-16 22:33:38'),(2,18,1,'2026-09-16 22:33:38'),(2,19,0,'2026-09-16 22:33:38'),(2,20,0,'2026-09-16 22:33:38'),(2,21,1,'2026-09-16 22:33:38'),(2,22,1,'2026-09-16 22:33:38'),(2,23,0,'2026-09-16 22:33:38'),(2,24,0,'2026-09-16 22:33:38'),(2,25,1,'2026-09-16 22:33:38'),(2,26,1,'2026-09-16 22:33:38'),(2,27,0,'2026-09-16 22:33:38'),(2,28,0,'2026-09-16 22:33:38'),(2,29,1,'2026-09-16 22:33:38'),(2,30,1,'2026-09-16 22:33:38'),(2,31,0,'2026-09-16 22:33:38'),(2,32,0,'2026-09-16 22:33:38'),(2,33,1,'2026-09-16 22:33:38'),(2,34,1,'2026-09-16 22:33:38'),(2,35,0,'2026-09-16 22:33:38'),(2,36,0,'2026-09-16 22:33:38'),(2,37,1,'2026-09-16 22:33:38'),(2,38,1,'2026-09-16 22:33:38'),(2,39,0,'2026-09-16 22:33:38'),(2,40,0,'2026-09-16 22:33:38'),(2,41,1,'2026-09-16 22:33:38'),(2,42,1,'2026-09-16 22:33:38'),(2,43,0,'2026-09-16 22:33:38'),(2,44,0,'2026-09-16 22:33:38'),(2,45,1,'2026-09-16 22:33:38'),(2,46,1,'2026-09-16 22:33:38'),(2,47,0,'2026-09-16 22:33:38'),(2,48,0,'2026-09-16 22:33:38'),(2,49,1,'2026-09-16 22:33:38'),(2,50,1,'2026-09-16 22:33:38'),(2,51,0,'2026-09-16 22:33:38'),(2,52,0,'2026-09-16 22:33:38');
/*!40000 ALTER TABLE `rbac` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `receipt_items`
--

DROP TABLE IF EXISTS `receipt_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `receipt_items` (
  `receipt_item_id` bigint NOT NULL AUTO_INCREMENT,
  `receipt_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  `unit_price` decimal(15,2) NOT NULL,
  `warranty_time` int NOT NULL,
  `is_active` tinyint DEFAULT '1',
  `quantity` int NOT NULL DEFAULT '1',
  PRIMARY KEY (`receipt_item_id`),
  KEY `receipt_id` (`receipt_id`),
  KEY `product_id` (`product_id`),
  CONSTRAINT `receipt_items_ibfk_1` FOREIGN KEY (`receipt_id`) REFERENCES `receipts` (`receipt_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `receipt_items_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=55 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `receipt_items`
--

LOCK TABLES `receipt_items` WRITE;
/*!40000 ALTER TABLE `receipt_items` DISABLE KEYS */;
INSERT INTO `receipt_items` VALUES (1,1,1,14500000.00,24,1,5),(2,1,2,15800000.00,24,1,5),(3,1,3,23500000.00,36,1,5),(4,1,4,4200000.00,36,1,5),(5,1,5,320000.00,12,1,5),(6,2,6,21500000.00,24,1,5),(7,2,7,16900000.00,24,1,5),(8,2,8,22600000.00,36,1,5),(9,2,9,3650000.00,36,1,5),(10,2,10,290000.00,12,1,5),(11,3,11,13900000.00,24,1,5),(12,3,12,18400000.00,24,1,5),(13,3,13,22800000.00,36,1,5),(14,3,14,3350000.00,36,1,5),(15,3,15,590000.00,12,1,5),(16,4,16,14200000.00,24,1,5),(17,4,17,21900000.00,24,1,5),(18,4,18,25900000.00,24,1,5),(19,4,19,2950000.00,36,1,5),(20,4,20,420000.00,12,1,5),(21,5,21,12500000.00,24,1,5),(22,5,22,20500000.00,24,1,5),(23,5,23,22500000.00,24,1,5),(24,5,24,2750000.00,36,1,5),(25,5,25,260000.00,12,1,5),(26,6,26,24900000.00,12,1,5),(27,6,27,29900000.00,12,1,5),(28,6,28,17900000.00,12,1,5),(29,6,29,8990000.00,12,1,5),(30,6,30,3290000.00,12,1,5),(31,7,31,17400000.00,12,1,5),(32,7,32,8790000.00,12,1,5),(33,7,33,9990000.00,12,1,5),(34,7,34,5890000.00,24,1,5),(35,7,35,2190000.00,36,1,5),(36,8,36,2190000.00,12,1,5),(37,8,37,1590000.00,24,1,5),(38,8,38,890000.00,12,1,5),(39,8,39,2390000.00,12,1,5),(40,8,40,1290000.00,24,1,5),(41,9,41,1450000.00,36,1,5),(42,9,42,2190000.00,60,1,5),(43,9,43,850000.00,36,1,5),(44,9,44,2190000.00,36,1,5),(45,9,45,230000.00,60,1,5),(46,10,46,22900000.00,24,1,5),(47,10,47,25900000.00,24,1,5),(48,10,48,13900000.00,24,1,5),(49,10,49,5990000.00,36,1,5),(50,10,50,990000.00,12,1,5),(53,11,1,20000000.00,12,1,10),(54,11,2,30000000.00,24,1,10);
/*!40000 ALTER TABLE `receipt_items` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_receipt_items_before_insert` BEFORE INSERT ON `receipt_items` FOR EACH ROW BEGIN
    DECLARE v_status TINYINT DEFAULT 0;
    SELECT COALESCE(MAX(status), 0)
      INTO v_status
      FROM receipts
     WHERE receipt_id = NEW.receipt_id;

    IF v_status = 1 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Khong duoc them chi tiet vao phieu nhap da hoan tat.';
    END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_receipt_items_before_update` BEFORE UPDATE ON `receipt_items` FOR EACH ROW BEGIN
    DECLARE v_old_status TINYINT DEFAULT 0;
    DECLARE v_new_status TINYINT DEFAULT 0;

    SELECT COALESCE(MAX(status), 0)
      INTO v_old_status
      FROM receipts
     WHERE receipt_id = OLD.receipt_id;

    SELECT COALESCE(MAX(status), 0)
      INTO v_new_status
      FROM receipts
     WHERE receipt_id = NEW.receipt_id;

    IF v_old_status = 1 OR v_new_status = 1 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Khong duoc sua chi tiet cua phieu nhap da hoan tat.';
    END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_receipt_items_before_delete` BEFORE DELETE ON `receipt_items` FOR EACH ROW BEGIN
    DECLARE v_status TINYINT DEFAULT 0;
    SELECT COALESCE(MAX(r.status), 0)
      INTO v_status
      FROM receipts r
     WHERE r.receipt_id = OLD.receipt_id;

    IF v_status = 1 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Khong duoc xoa chi tiet cua phieu nhap da hoan tat.';
    END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `receipts`
--

DROP TABLE IF EXISTS `receipts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `receipts` (
  `receipt_id` bigint NOT NULL AUTO_INCREMENT,
  `supplier_id` int NOT NULL,
  `total` decimal(15,2) DEFAULT NULL,
  `status` tinyint DEFAULT '0',
  `created_user` bigint DEFAULT NULL,
  `updated_user` bigint DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`receipt_id`),
  KEY `fk_supplier_id_idx` (`supplier_id`),
  KEY `receipt_user_idx` (`created_user`),
  KEY `receipt_updated_user_idx` (`updated_user`),
  CONSTRAINT `fk_supplier_id` FOREIGN KEY (`supplier_id`) REFERENCES `suppliers` (`supplier_id`),
  CONSTRAINT `receipt_updated_user` FOREIGN KEY (`updated_user`) REFERENCES `employees` (`employee_id`),
  CONSTRAINT `receipt_user` FOREIGN KEY (`created_user`) REFERENCES `employees` (`employee_id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `receipts`
--

LOCK TABLES `receipts` WRITE;
/*!40000 ALTER TABLE `receipts` DISABLE KEYS */;
INSERT INTO `receipts` VALUES (1,1,291600000.00,1,1,1,'2026-09-06 09:00:00','2026-09-16 14:04:26'),(2,2,324700000.00,1,1,1,'2026-09-07 09:00:00','2026-09-16 14:04:26'),(3,3,295200000.00,1,1,1,'2026-09-08 09:00:00','2026-09-16 14:04:26'),(4,4,326850000.00,1,1,1,'2026-09-09 09:00:00','2026-09-16 14:04:26'),(5,5,292550000.00,1,1,1,'2026-09-10 09:00:00','2026-09-16 14:04:26'),(6,6,424900000.00,1,1,1,'2026-09-11 09:00:00','2026-09-16 14:04:26'),(7,7,221300000.00,1,1,1,'2026-09-12 09:00:00','2026-09-16 14:04:26'),(8,8,41750000.00,1,1,1,'2026-09-13 09:00:00','2026-09-16 14:04:26'),(9,9,34550000.00,1,1,1,'2026-09-14 09:00:00','2026-09-16 14:04:26'),(10,10,348400000.00,1,1,1,'2026-09-15 09:00:00','2026-09-16 14:04:26'),(11,1,500000000.00,1,1,1,'2026-09-16 22:42:49','2026-09-16 22:43:28');
/*!40000 ALTER TABLE `receipts` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_receipts_before_status_update` BEFORE UPDATE ON `receipts` FOR EACH ROW BEGIN
    -- Hoàn tất phiếu: kiểm tra toàn bộ dữ liệu trước khi cho status -> 1.
    IF NEW.status = 1 AND COALESCE(OLD.status, 0) <> 1 THEN
        IF NOT EXISTS (
            SELECT 1
            FROM receipt_items ri
            WHERE ri.receipt_id = NEW.receipt_id
              AND ri.is_active = 1
        ) THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Khong the hoan tat phieu nhap: phieu chua co san pham.';
        END IF;

        IF EXISTS (
            SELECT 1
            FROM receipt_items ri
            WHERE ri.receipt_id = NEW.receipt_id
              AND ri.is_active = 1
              AND (ri.quantity <= 0 OR ri.unit_price < 0)
        ) THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Khong the hoan tat phieu nhap: quantity/gia nhap khong hop le.';
        END IF;

        IF EXISTS (
            SELECT 1
            FROM receipt_items ri
            LEFT JOIN supplier_products sp
              ON sp.supplier_id = NEW.supplier_id
             AND sp.product_id = ri.product_id
            WHERE ri.receipt_id = NEW.receipt_id
              AND ri.is_active = 1
              AND sp.supplier_product_id IS NULL
        ) THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Khong the hoan tat phieu nhap: san pham khong thuoc nha cung cap cua phieu.';
        END IF;

        IF EXISTS (
            SELECT 1
            FROM receipt_items ri
            LEFT JOIN (
                SELECT receipt_item_id, COUNT(*) AS serial_count
                FROM item_serials
                GROUP BY receipt_item_id
            ) s ON s.receipt_item_id = ri.receipt_item_id
            WHERE ri.receipt_id = NEW.receipt_id
              AND ri.is_active = 1
              AND COALESCE(s.serial_count, 0) <> ri.quantity
        ) THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Khong the hoan tat phieu nhap: so serial phai bang quantity cua tung dong.';
        END IF;

        -- Không tin total từ client: luôn tính lại từ chi tiết active.
        SET NEW.total = (
            SELECT COALESCE(SUM(ri.quantity * ri.unit_price), 0)
            FROM receipt_items ri
            WHERE ri.receipt_id = NEW.receipt_id
              AND ri.is_active = 1
        );
    END IF;

    -- Phiếu đã hoàn tất: không cho đổi NCC/total trực tiếp khi vẫn giữ status = 1.
    IF OLD.status = 1 AND NEW.status = 1 THEN
        IF NOT (NEW.supplier_id <=> OLD.supplier_id)
           OR NOT (NEW.total <=> OLD.total) THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Phieu nhap da hoan tat: khong duoc sua nha cung cap hoac total truc tiep.';
        END IF;
    END IF;

    -- Hủy phiếu đã hoàn tất: chỉ cho phép nếu chưa serial nào được dùng/bán.
    IF OLD.status = 1 AND COALESCE(NEW.status, 0) <> 1 THEN
        IF EXISTS (
            SELECT 1
            FROM receipt_items ri
            JOIN item_serials s ON s.receipt_item_id = ri.receipt_item_id
            WHERE ri.receipt_id = OLD.receipt_id
              AND ri.is_active = 1
              AND s.is_used = 1
        ) THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Khong the huy phieu nhap: da co serial cua phieu duoc su dung/ban.';
        END IF;

        IF EXISTS (
            SELECT 1
            FROM products p
            JOIN (
                SELECT product_id, SUM(quantity) AS qty
                FROM receipt_items
                WHERE receipt_id = OLD.receipt_id
                  AND is_active = 1
                GROUP BY product_id
            ) x ON x.product_id = p.product_id
            WHERE p.stock < x.qty
        ) THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Khong the huy phieu nhap: ton kho hien tai khong du de hoan kho.';
        END IF;
    END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `update_stock_on_receipt_complete` AFTER UPDATE ON `receipts` FOR EACH ROW BEGIN
    IF NEW.status = 1 AND COALESCE(OLD.status, 0) <> 1 THEN
        UPDATE products p
        JOIN (
            SELECT product_id, SUM(quantity) AS qty
            FROM receipt_items
            WHERE receipt_id = NEW.receipt_id
              AND is_active = 1
            GROUP BY product_id
        ) x ON x.product_id = p.product_id
        SET p.stock = p.stock + x.qty;

    ELSEIF OLD.status = 1 AND COALESCE(NEW.status, 0) <> 1 THEN
        UPDATE products p
        JOIN (
            SELECT product_id, SUM(quantity) AS qty
            FROM receipt_items
            WHERE receipt_id = OLD.receipt_id
              AND is_active = 1
            GROUP BY product_id
        ) x ON x.product_id = p.product_id
        SET p.stock = p.stock - x.qty;
    END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_receipts_before_delete` BEFORE DELETE ON `receipts` FOR EACH ROW BEGIN
    IF OLD.status = 1 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Khong duoc xoa phieu nhap da hoan tat. Hay huy phieu truoc.';
    END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `roles` (
  `role_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `roles`
--

LOCK TABLES `roles` WRITE;
/*!40000 ALTER TABLE `roles` DISABLE KEYS */;
INSERT INTO `roles` VALUES (1,'ADMIN','2026-09-16 14:04:26','2026-09-16 14:04:26'),(2,'Nhân Viên Bán Hàng','2026-09-16 22:33:31','2026-09-16 22:33:31');
/*!40000 ALTER TABLE `roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `supplier_products`
--

DROP TABLE IF EXISTS `supplier_products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `supplier_products` (
  `supplier_product_id` bigint NOT NULL AUTO_INCREMENT,
  `supplier_id` int NOT NULL,
  `product_id` bigint NOT NULL,
  PRIMARY KEY (`supplier_product_id`),
  KEY `supplier_id` (`supplier_id`),
  KEY `product_id` (`product_id`),
  CONSTRAINT `supplier_products_ibfk_1` FOREIGN KEY (`supplier_id`) REFERENCES `suppliers` (`supplier_id`),
  CONSTRAINT `supplier_products_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=52 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `supplier_products`
--

LOCK TABLES `supplier_products` WRITE;
/*!40000 ALTER TABLE `supplier_products` DISABLE KEYS */;
INSERT INTO `supplier_products` VALUES (1,1,1),(2,1,2),(3,1,3),(4,1,4),(5,1,5),(6,2,6),(7,2,7),(8,2,8),(9,2,9),(10,2,10),(11,3,11),(12,3,12),(13,3,13),(14,3,14),(15,3,15),(16,4,16),(17,4,17),(18,4,18),(19,4,19),(20,4,20),(21,5,21),(22,5,22),(23,5,23),(24,5,24),(25,5,25),(26,6,26),(27,6,27),(28,6,28),(29,6,29),(30,6,30),(31,7,31),(32,7,32),(33,7,33),(34,7,34),(35,7,35),(36,8,36),(37,8,37),(38,8,38),(39,8,39),(40,8,40),(41,9,41),(42,9,42),(43,9,43),(44,9,44),(45,9,45),(46,10,46),(47,10,47),(48,10,48),(49,10,49),(50,10,50),(51,11,1);
/*!40000 ALTER TABLE `supplier_products` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `suppliers`
--

DROP TABLE IF EXISTS `suppliers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `suppliers` (
  `supplier_id` int NOT NULL AUTO_INCREMENT,
  `supplier_code` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phone_number` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`supplier_id`),
  UNIQUE KEY `phone_number_UNIQUE` (`phone_number`),
  UNIQUE KEY `supplier_code_UNIQUE` (`supplier_code`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `suppliers`
--

LOCK TABLES `suppliers` WRITE;
/*!40000 ALTER TABLE `suppliers` DISABLE KEYS */;
INSERT INTO `suppliers` VALUES (1,'NCC001','Dell Technologies Việt Nam','0901000001','TP. Hồ Chí Minh'),(2,'NCC002','Lenovo Việt Nam','0901000002','TP. Hồ Chí Minh'),(3,'NCC003','HP Việt Nam','0901000003','TP. Hồ Chí Minh'),(4,'NCC004','ASUS Việt Nam','0901000004','TP. Hồ Chí Minh'),(5,'NCC005','Acer Việt Nam','0901000005','TP. Hồ Chí Minh'),(6,'NCC006','Apple Distribution Việt Nam','0901000006','TP. Hồ Chí Minh'),(7,'NCC007','Samsung Electronics Việt Nam','0901000007','TP. Hồ Chí Minh'),(8,'NCC008','Logitech Distribution Việt Nam','0901000008','TP. Hồ Chí Minh'),(9,'NCC009','Kingston Technology Việt Nam','0901000009','TP. Hồ Chí Minh'),(10,'NCC010','MSI Việt Nam','0901000010','TP. Hồ Chí Minh'),(11,'NCC011','DOTA2','0987657461','275 An Dương Vương'),(12,'NCC012','TNHH 1 Thành Viên','0987654326','Hà Nội');
/*!40000 ALTER TABLE `suppliers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Temporary view structure for view `vw_active_promotions`
--

DROP TABLE IF EXISTS `vw_active_promotions`;
/*!50001 DROP VIEW IF EXISTS `vw_active_promotions`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `vw_active_promotions` AS SELECT 
 1 AS `product_id`,
 1 AS `product_name`,
 1 AS `p_sell_price`,
 1 AS `profit_rate`,
 1 AS `promotion_id`,
 1 AS `promotion_name`,
 1 AS `is_auto`,
 1 AS `start_at`,
 1 AS `end_at`,
 1 AS `discount_percent`,
 1 AS `sell_price`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `vw_cash_balance`
--

DROP TABLE IF EXISTS `vw_cash_balance`;
/*!50001 DROP VIEW IF EXISTS `vw_cash_balance`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `vw_cash_balance` AS SELECT 
 1 AS `cash_balance`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `vw_daily_revenue`
--

DROP TABLE IF EXISTS `vw_daily_revenue`;
/*!50001 DROP VIEW IF EXISTS `vw_daily_revenue`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `vw_daily_revenue` AS SELECT 
 1 AS `date`,
 1 AS `revenue`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `vw_product_sell_price`
--

DROP TABLE IF EXISTS `vw_product_sell_price`;
/*!50001 DROP VIEW IF EXISTS `vw_product_sell_price`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `vw_product_sell_price` AS SELECT 
 1 AS `product_id`,
 1 AS `name`,
 1 AS `product_unit_price`,
 1 AS `profit_rate`,
 1 AS `discount_percent`,
 1 AS `sell_price`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `vw_receipt_items`
--

DROP TABLE IF EXISTS `vw_receipt_items`;
/*!50001 DROP VIEW IF EXISTS `vw_receipt_items`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `vw_receipt_items` AS SELECT 
 1 AS `receipt_item_id`,
 1 AS `receipt_id`,
 1 AS `product_id`,
 1 AS `quantity`,
 1 AS `unit_price`,
 1 AS `warranty_time`,
 1 AS `stock`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `vw_total_cost`
--

DROP TABLE IF EXISTS `vw_total_cost`;
/*!50001 DROP VIEW IF EXISTS `vw_total_cost`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `vw_total_cost` AS SELECT 
 1 AS `total_cost`*/;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `warranty_card`
--

DROP TABLE IF EXISTS `warranty_card`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `warranty_card` (
  `warranty_id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `receive_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `return_date` date NOT NULL,
  `product_id` bigint DEFAULT NULL,
  `product_serial` varchar(45) DEFAULT NULL,
  `details` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `status` tinyint DEFAULT '1',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_user` bigint NOT NULL,
  PRIMARY KEY (`warranty_id`),
  KEY `user_idx` (`created_user`),
  KEY `warranty_product_idx` (`order_id`),
  KEY `idx_warranty_order` (`order_id`),
  KEY `idx_warranty_product` (`product_id`),
  KEY `idx_warranty_status` (`status`),
  CONSTRAINT `user` FOREIGN KEY (`created_user`) REFERENCES `employees` (`employee_id`),
  CONSTRAINT `warranty_card_product_fk` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`) ON DELETE RESTRICT,
  CONSTRAINT `warranty_product` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `warranty_card`
--

LOCK TABLES `warranty_card` WRITE;
/*!40000 ALTER TABLE `warranty_card` DISABLE KEYS */;
/*!40000 ALTER TABLE `warranty_card` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'java'
--
/*!50106 SET @save_time_zone= @@TIME_ZONE */ ;
/*!50106 DROP EVENT IF EXISTS `ev_auto_promotion_status` */;
DELIMITER ;;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;;
/*!50003 SET character_set_client  = utf8mb3 */ ;;
/*!50003 SET character_set_results = utf8mb3 */ ;;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;;
/*!50003 SET @saved_time_zone      = @@time_zone */ ;;
/*!50003 SET time_zone             = '+00:00' */ ;;
/*!50106 CREATE*/ /*!50117 DEFINER=`root`@`localhost`*/ /*!50106 EVENT `ev_auto_promotion_status` ON SCHEDULE EVERY 1 MINUTE STARTS '2026-09-16 14:05:25' ON COMPLETION PRESERVE ENABLE DO BEGIN
    UPDATE promotions
    SET status = 1
    WHERE is_auto = 1
      AND start_at <= NOW()
      AND end_at > NOW()
      AND status <> 1;

    UPDATE promotions
    SET status = 0
    WHERE is_auto = 1
      AND end_at <= NOW()
      AND status <> 0;
END */ ;;
/*!50003 SET time_zone             = @saved_time_zone */ ;;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;;
/*!50003 SET character_set_client  = @saved_cs_client */ ;;
/*!50003 SET character_set_results = @saved_cs_results */ ;;
/*!50003 SET collation_connection  = @saved_col_connection */ ;;
DELIMITER ;
/*!50106 SET TIME_ZONE= @save_time_zone */ ;

--
-- Dumping routines for database 'java'
--
/*!50003 DROP PROCEDURE IF EXISTS `sp_recalculate_avg_price` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_recalculate_avg_price`(IN p_product_id BIGINT)
BEGIN
    DECLARE total_qty INT DEFAULT 0;
    DECLARE total_value DECIMAL(18,2) DEFAULT 0;

    SELECT
        COALESCE(SUM(ri.quantity), 0),
        COALESCE(SUM(ri.quantity * ri.unit_price), 0)
    INTO total_qty, total_value
    FROM receipt_items ri
    JOIN receipts r ON r.receipt_id = ri.receipt_id
    WHERE ri.product_id = p_product_id
      AND ri.is_active = 1
      AND r.status = 1;

    IF total_qty > 0 THEN
        UPDATE products
        SET avg_unit_price = total_value / total_qty
        WHERE product_id = p_product_id;
    ELSE
        UPDATE products
        SET avg_unit_price = 0
        WHERE product_id = p_product_id;
    END IF;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Final view structure for view `vw_active_promotions`
--

/*!50001 DROP VIEW IF EXISTS `vw_active_promotions`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY INVOKER */
/*!50001 VIEW `vw_active_promotions` AS select `p`.`product_id` AS `product_id`,`p`.`name` AS `product_name`,`p`.`sell_price` AS `p_sell_price`,`p`.`profit` AS `profit_rate`,`pr`.`promotion_id` AS `promotion_id`,`pr`.`name` AS `promotion_name`,`pr`.`is_auto` AS `is_auto`,`pr`.`start_at` AS `start_at`,`pr`.`end_at` AS `end_at`,`pi`.`discount_percent` AS `discount_percent`,((`p`.`sell_price` * (1 + coalesce(`p`.`profit`,0))) * (1 - (`pi`.`discount_percent` / 100))) AS `sell_price` from ((`promotions` `pr` join `promotion_items` `pi` on((`pi`.`promotion_id` = `pr`.`promotion_id`))) join `products` `p` on((`p`.`product_id` = `pi`.`product_id`))) where ((`pr`.`status` = 1) and (`pi`.`discount_percent` > 0)) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `vw_cash_balance`
--

/*!50001 DROP VIEW IF EXISTS `vw_cash_balance`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY INVOKER */
/*!50001 VIEW `vw_cash_balance` AS select coalesce(sum(`cash_ledger`.`amount`),0) AS `cash_balance` from `cash_ledger` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `vw_daily_revenue`
--

/*!50001 DROP VIEW IF EXISTS `vw_daily_revenue`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY INVOKER */
/*!50001 VIEW `vw_daily_revenue` AS select cast(`cash_ledger`.`created_at` as date) AS `date`,sum(`cash_ledger`.`amount`) AS `revenue` from `cash_ledger` where (`cash_ledger`.`type` = 'SALE') group by cast(`cash_ledger`.`created_at` as date) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `vw_product_sell_price`
--

/*!50001 DROP VIEW IF EXISTS `vw_product_sell_price`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY INVOKER */
/*!50001 VIEW `vw_product_sell_price` AS select `p`.`product_id` AS `product_id`,`p`.`name` AS `name`,(`p`.`sell_price` * (1 + coalesce(`p`.`profit`,0))) AS `product_unit_price`,`p`.`profit` AS `profit_rate`,coalesce(max((case when (`pr`.`status` = 1) then `pi`.`discount_percent` else 0 end)),0) AS `discount_percent`,((`p`.`sell_price` * (1 + coalesce(`p`.`profit`,0))) * (1 - (coalesce(max((case when (`pr`.`status` = 1) then `pi`.`discount_percent` else 0 end)),0) / 100))) AS `sell_price` from ((`products` `p` left join `promotion_items` `pi` on((`pi`.`product_id` = `p`.`product_id`))) left join `promotions` `pr` on((`pr`.`promotion_id` = `pi`.`promotion_id`))) group by `p`.`product_id`,`p`.`name`,`p`.`sell_price`,`p`.`profit` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `vw_receipt_items`
--

/*!50001 DROP VIEW IF EXISTS `vw_receipt_items`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY INVOKER */
/*!50001 VIEW `vw_receipt_items` AS select `r`.`receipt_item_id` AS `receipt_item_id`,`r`.`receipt_id` AS `receipt_id`,`r`.`product_id` AS `product_id`,`r`.`quantity` AS `quantity`,`r`.`unit_price` AS `unit_price`,`r`.`warranty_time` AS `warranty_time`,count((case when ((`s`.`is_used` = 0) and (`re`.`status` = 1)) then 1 end)) AS `stock` from ((`receipt_items` `r` left join `item_serials` `s` on((`s`.`receipt_item_id` = `r`.`receipt_item_id`))) left join `receipts` `re` on((`re`.`receipt_id` = `r`.`receipt_id`))) group by `r`.`receipt_item_id`,`r`.`receipt_id`,`r`.`product_id`,`r`.`quantity`,`r`.`unit_price`,`r`.`warranty_time` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `vw_total_cost`
--

/*!50001 DROP VIEW IF EXISTS `vw_total_cost`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY INVOKER */
/*!50001 VIEW `vw_total_cost` AS select coalesce(abs(sum(`cash_ledger`.`amount`)),0) AS `total_cost` from `cash_ledger` where (`cash_ledger`.`type` in ('PURCHASE','CAPITAL_OUT')) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-09-17 14:36:26
