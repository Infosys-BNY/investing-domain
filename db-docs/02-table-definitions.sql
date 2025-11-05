-- BNY Data Services - Table Definitions
-- Database: bny_data_services

SET NAMES utf8mb4;

-- Clients table
CREATE TABLE `clients` (
  `client_id` VARCHAR(50) NOT NULL,
  `client_name` VARCHAR(200) NOT NULL,
  `advisor_id` VARCHAR(50) NOT NULL,
  `tax_id` VARCHAR(20) NULL,
  `created_date` DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
  `last_updated` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`client_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Securities table
CREATE TABLE `securities` (
  `symbol` VARCHAR(20) NOT NULL,
  `security_name` VARCHAR(200) NOT NULL,
  `sector` VARCHAR(100) NULL,
  `asset_class` VARCHAR(20) NOT NULL,
  `current_price` DECIMAL(19,4) NOT NULL,
  `price_change` DECIMAL(19,4) NULL,
  `price_change_percent` DECIMAL(19,4) NULL,
  `last_price_update` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`symbol`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Accounts table
CREATE TABLE `accounts` (
  `account_id` VARCHAR(50) NOT NULL,
  `client_id` VARCHAR(50) NOT NULL,
  `account_number` VARCHAR(50) NOT NULL,
  `account_type` VARCHAR(20) NOT NULL,
  `market_value` DECIMAL(19,4) NOT NULL DEFAULT 0,
  `cash_balance` DECIMAL(19,4) NOT NULL DEFAULT 0,
  `ytd_performance` DECIMAL(19,4) NULL,
  `risk_profile` VARCHAR(20) NULL,
  `created_date` DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
  `last_updated` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`account_id`),
  FOREIGN KEY (`client_id`) REFERENCES `clients`(`client_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Holdings table
CREATE TABLE `holdings` (
  `holding_id` VARCHAR(50) NOT NULL,
  `account_id` VARCHAR(50) NOT NULL,
  `symbol` VARCHAR(20) NOT NULL,
  `quantity` DECIMAL(19,4) NOT NULL,
  `cost_basis` DECIMAL(19,4) NOT NULL,
  `purchase_date` DATETIME NULL,
  `last_updated` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`holding_id`),
  FOREIGN KEY (`account_id`) REFERENCES `accounts`(`account_id`),
  FOREIGN KEY (`symbol`) REFERENCES `securities`(`symbol`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
