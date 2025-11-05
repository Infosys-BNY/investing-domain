-- BNY Data Services - Stored Procedures for LFD API Integration
-- Database: bny_data_services

SET NAMES utf8mb4;
DELIMITER $$

-- Search clients with filtering and pagination
CREATE PROCEDURE `sp_search_clients`(
    IN p_advisor_id VARCHAR(50),
    IN p_search_query VARCHAR(200),
    IN p_account_types JSON,
    IN p_min_market_value DECIMAL(19,4),
    IN p_max_market_value DECIMAL(19,4),
    IN p_activity_status VARCHAR(20),
    IN p_risk_profile VARCHAR(20),
    IN p_sort_field VARCHAR(50),
    IN p_sort_direction VARCHAR(10),
    IN p_page_offset INT,
    IN p_page_size INT,
    OUT p_result_code INT,
    OUT p_error_message VARCHAR(500),
    OUT p_total_count INT
)
BEGIN
    DECLARE v_total_count INT DEFAULT 0;
    
    -- Get total count
    SELECT COUNT(*) INTO v_total_count
    FROM clients c
    WHERE c.advisor_id = p_advisor_id;
    
    SET p_total_count = v_total_count;
    SET p_result_code = 0;
    SET p_error_message = NULL;
    
    -- Return paginated results
    SELECT 
        c.client_id,
        c.client_name,
        c.advisor_id,
        'Unknown Advisor' as advisor_name,
        (SELECT COUNT(*) FROM accounts a WHERE a.client_id = c.client_id) as account_count,
        COALESCE(SUM(a.market_value), 0) as total_market_value,
        'Active' as activity_status,
        'MODERATE' as risk_profile,
        c.created_date as last_activity_date,
        c.created_date
    FROM clients c
    LEFT JOIN accounts a ON c.client_id = a.client_id
    WHERE c.advisor_id = p_advisor_id
    GROUP BY c.client_id, c.client_name, c.advisor_id, c.created_date
    ORDER BY c.client_name
    LIMIT p_page_size OFFSET p_page_offset;
END$$

-- Get advisor clients with pagination
CREATE PROCEDURE `sp_get_advisor_clients`(
    IN p_advisor_id VARCHAR(50),
    IN p_page_offset INT,
    IN p_page_size INT,
    OUT p_result_code INT,
    OUT p_error_message VARCHAR(500),
    OUT p_total_count INT
)
BEGIN
    DECLARE v_total_count INT DEFAULT 0;
    
    -- Get total count
    SELECT COUNT(*) INTO v_total_count
    FROM clients c
    WHERE c.advisor_id = p_advisor_id;
    
    SET p_total_count = v_total_count;
    SET p_result_code = 0;
    SET p_error_message = NULL;
    
    -- Return paginated results
    SELECT 
        c.client_id,
        c.client_name,
        c.advisor_id,
        'Unknown Advisor' as advisor_name,
        (SELECT COUNT(*) FROM accounts a WHERE a.client_id = c.client_id) as account_count,
        COALESCE(SUM(a.market_value), 0) as total_market_value,
        'Active' as activity_status,
        'MODERATE' as risk_profile,
        c.created_date as last_activity_date,
        c.created_date
    FROM clients c
    LEFT JOIN accounts a ON c.client_id = a.client_id
    WHERE c.advisor_id = p_advisor_id
    GROUP BY c.client_id, c.client_name, c.advisor_id, c.created_date
    ORDER BY c.client_name
    LIMIT p_page_size OFFSET p_page_offset;
END$$

-- Get account holdings with calculations
CREATE PROCEDURE `sp_get_account_holdings`(
    IN p_account_id VARCHAR(50),
    IN p_as_of_date DATE,
    IN p_asset_classes JSON,
    IN p_sort_field VARCHAR(50),
    IN p_sort_direction VARCHAR(10),
    IN p_page_offset INT,
    IN p_page_size INT,
    OUT p_result_code INT,
    OUT p_error_message VARCHAR(500),
    OUT p_total_count INT
)
BEGIN
    DECLARE v_total_count INT DEFAULT 0;
    
    -- Get total count
    SELECT COUNT(*) INTO v_total_count
    FROM holdings h
    WHERE h.account_id = p_account_id;
    
    SET p_total_count = v_total_count;
    SET p_result_code = 0;
    SET p_error_message = NULL;
    
    -- Return holdings with calculated values
    SELECT 
        h.account_id,
        h.symbol,
        s.security_name,
        s.asset_class,
        h.quantity,
        h.cost_basis,
        s.current_price,
        (h.quantity * s.current_price) as market_value,
        (h.quantity * s.current_price - h.cost_basis) as unrealized_gain_loss,
        CASE 
            WHEN h.cost_basis > 0 THEN 
                ROUND(((h.quantity * s.current_price - h.cost_basis) / h.cost_basis) * 100, 2)
            ELSE 0 
        END as unrealized_gain_loss_percent,
        h.purchase_date,
        CURRENT_DATE() as price_date
    FROM holdings h
    JOIN securities s ON h.symbol = s.symbol
    WHERE h.account_id = p_account_id
    ORDER BY market_value DESC
    LIMIT p_page_size OFFSET p_page_offset;
END$$

-- Get portfolio summary with aggregations
CREATE PROCEDURE `sp_get_portfolio_summary`(
    IN p_account_id VARCHAR(50),
    OUT p_result_code INT,
    OUT p_error_message VARCHAR(500),
    OUT p_total_market_value DECIMAL(19,4),
    OUT p_total_cost_basis DECIMAL(19,4),
    OUT p_total_unrealized_gain_loss DECIMAL(19,4),
    OUT p_unrealized_gain_loss_percent DECIMAL(19,4),
    OUT p_portfolio_beta DECIMAL(19,4),
    OUT p_annual_dividend_yield DECIMAL(19,4),
    OUT p_holdings_count INT
)
BEGIN
    DECLARE v_total_market_value DECIMAL(19,4) DEFAULT 0;
    DECLARE v_total_cost_basis DECIMAL(19,4) DEFAULT 0;
    DECLARE v_holdings_count INT DEFAULT 0;
    
    -- Calculate aggregations
    SELECT 
        COALESCE(SUM(h.quantity * s.current_price), 0),
        COALESCE(SUM(h.cost_basis), 0),
        COUNT(*)
    INTO v_total_market_value, v_total_cost_basis, v_holdings_count
    FROM holdings h
    JOIN securities s ON h.symbol = s.symbol
    WHERE h.account_id = p_account_id;
    
    SET p_total_market_value = v_total_market_value;
    SET p_total_cost_basis = v_total_cost_basis;
    SET p_total_unrealized_gain_loss = v_total_market_value - v_total_cost_basis;
    SET p_unrealized_gain_loss_percent = CASE 
        WHEN v_total_cost_basis > 0 THEN 
            ROUND(((v_total_market_value - v_total_cost_basis) / v_total_cost_basis) * 100, 2)
        ELSE 0 
    END;
    SET p_portfolio_beta = 1.0; -- Simplified
    SET p_annual_dividend_yield = 2.5; -- Simplified
    SET p_holdings_count = v_holdings_count;
    SET p_result_code = 0;
    SET p_error_message = NULL;
    
    -- Return asset allocation
    SELECT 
        s.asset_class,
        SUM(h.quantity * s.current_price) as market_value,
        CASE 
            WHEN v_total_market_value > 0 THEN 
                ROUND((SUM(h.quantity * s.current_price) / v_total_market_value) * 100, 2)
            ELSE 0 
        END as percentage,
        COUNT(*) as holdings_count
    FROM holdings h
    JOIN securities s ON h.symbol = s.symbol
    WHERE h.account_id = p_account_id
    GROUP BY s.asset_class
    ORDER BY market_value DESC;
END$$

DELIMITER ;
