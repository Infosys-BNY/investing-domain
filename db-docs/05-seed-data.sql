-- BNY Data Services - Seed Data for Demo
-- Database: bny_data_services

SET NAMES utf8mb4;

-- Securities (minimal set for demo)
INSERT INTO `securities` (`symbol`, `security_name`, `sector`, `asset_class`, `current_price`, `price_change`, `price_change_percent`, `last_price_update`) VALUES
('AAPL', 'Apple Inc.', 'Technology', 'EQUITY', 178.25, 2.15, 1.22, NOW()),
('MSFT', 'Microsoft Corporation', 'Technology', 'EQUITY', 415.80, 3.45, 0.84, NOW()),
('GOOGL', 'Alphabet Inc.', 'Technology', 'EQUITY', 145.60, -1.20, -0.82, NOW()),
('JNJ', 'Johnson & Johnson', 'Healthcare', 'EQUITY', 165.40, 0.80, 0.49, NOW()),
('BRK.B', 'Berkshire Hathaway Inc.', 'Financial Services', 'EQUITY', 365.20, 0.85, 0.23, NOW()),
('BND', 'Vanguard Total Bond Market ETF', 'Fixed Income', 'FIXED_INCOME', 72.50, -0.10, -0.14, NOW());

-- Clients (3 clients for demo)
INSERT INTO `clients` (`client_id`, `client_name`, `advisor_id`, `tax_id`, `created_date`, `last_updated`) VALUES
('CLT001', 'Smith, John', 'ADV001', '1234', '2021-01-15 10:30:00', NOW()),
('CLT002', 'Johnson, Mary', 'ADV002', '5678', '2020-03-22 14:45:00', NOW()),
('CLT003', 'Davis Corporation', 'ADV001', '9876', '2019-07-10 09:15:00', NOW());

-- Accounts (6 accounts - 2 per client)
INSERT INTO `accounts` (`account_id`, `client_id`, `account_number`, `account_type`, `market_value`, `cash_balance`, `ytd_performance`, `risk_profile`, `created_date`, `last_updated`) VALUES
('ACC001', 'CLT001', 'ACC-001', 'INDIVIDUAL', 1250000.00, 25000.00, 12.5, 'MODERATE', '2021-01-15 10:30:00', NOW()),
('ACC002', 'CLT001', 'ACC-002', 'JOINT', 850000.00, 15000.00, 10.2, 'CONSERVATIVE', '2021-06-20 11:00:00', NOW()),
('ACC003', 'CLT002', 'ACC-003', 'INDIVIDUAL', 2100000.00, 40000.00, 15.8, 'AGGRESSIVE', '2020-03-22 14:45:00', NOW()),
('ACC004', 'CLT002', 'ACC-004', 'IRA', 650000.00, 12000.00, 8.5, 'MODERATE', '2020-09-10 16:20:00', NOW()),
('ACC005', 'CLT003', 'ACC-005', 'CORPORATE', 3200000.00, 75000.00, 11.3, 'MODERATE', '2019-07-10 09:15:00', NOW()),
('ACC006', 'CLT003', 'ACC-006', 'CORPORATE', 1800000.00, 35000.00, 9.7, 'CONSERVATIVE', '2020-02-15 13:45:00', NOW());

-- Holdings (15 holdings spread across accounts)
INSERT INTO `holdings` (`holding_id`, `account_id`, `symbol`, `quantity`, `cost_basis`, `purchase_date`, `last_updated`) VALUES
-- Account ACC001
('HLD001', 'ACC001', 'AAPL', 500.000, 85000.00, '2021-01-15 09:30:00', NOW()),
('HLD002', 'ACC001', 'MSFT', 200.000, 75000.00, '2021-03-22 10:45:00', NOW()),
('HLD003', 'ACC001', 'GOOGL', 300.000, 42000.00, '2021-02-10 11:20:00', NOW()),
-- Account ACC002
('HLD004', 'ACC002', 'AAPL', 200.000, 34000.00, '2021-06-20 10:15:00', NOW()),
('HLD005', 'ACC002', 'JNJ', 400.000, 60000.00, '2021-04-05 14:30:00', NOW()),
('HLD006', 'ACC002', 'BND', 1000.000, 72000.00, '2021-05-12 09:45:00', NOW()),
-- Account ACC003
('HLD007', 'ACC003', 'MSFT', 400.000, 150000.00, '2020-03-22 10:45:00', NOW()),
('HLD008', 'ACC003', 'GOOGL', 600.000, 84000.00, '2020-06-15 15:20:00', NOW()),
('HLD009', 'ACC003', 'BRK.B', 300.000, 105000.00, '2020-09-10 11:00:00', NOW()),
-- Account ACC004
('HLD010', 'ACC004', 'AAPL', 150.000, 25500.00, '2020-09-10 13:45:00', NOW()),
('HLD011', 'ACC004', 'BND', 2000.000, 144000.00, '2020-11-20 10:30:00', NOW()),
('HLD012', 'ACC004', 'JNJ', 250.000, 37500.00, '2020-12-05 14:15:00', NOW()),
-- Account ACC005
('HLD013', 'ACC005', 'MSFT', 500.000, 187500.00, '2019-07-10 09:15:00', NOW()),
('HLD014', 'ACC005', 'GOOGL', 800.000, 112000.00, '2019-10-25 16:40:00', NOW()),
('HLD015', 'ACC006', 'BRK.B', 400.000, 140000.00, '2020-02-15 13:45:00', NOW());
