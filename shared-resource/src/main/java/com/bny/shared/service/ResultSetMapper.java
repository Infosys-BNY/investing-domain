package com.bny.shared.service;

import com.bny.shared.dto.response.ClientDto;
import com.bny.shared.dto.response.HoldingDto;
import com.bny.shared.dto.response.PortfolioSummaryDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class ResultSetMapper {
    
    public List<ClientDto> mapToClients(ResultSet rs) throws SQLException {
        List<ClientDto> clients = new ArrayList<>();
        
        while (rs.next()) {
            ClientDto client = ClientDto.builder()
                .clientId(rs.getString("client_id"))
                .clientName(rs.getString("client_name"))
                .advisorId(rs.getString("advisor_id"))
                .lastAccessed(getLocalDateTime(rs, "last_accessed"))
                .build();
            
            clients.add(client);
        }
        
        return clients;
    }
    
    public List<HoldingDto> mapToHoldings(ResultSet rs) throws SQLException {
        List<HoldingDto> holdings = new ArrayList<>();
        
        while (rs.next()) {
            HoldingDto holding = HoldingDto.builder()
                .symbol(rs.getString("symbol"))
                .securityName(rs.getString("security_name"))
                .quantity(getBigDecimal(rs, "quantity"))
                .currentPrice(getBigDecimal(rs, "current_price"))
                .costBasis(getBigDecimal(rs, "cost_basis"))
                .marketValue(getBigDecimal(rs, "market_value"))
                .unrealizedGainLoss(getBigDecimal(rs, "unrealized_gain_loss"))
                .unrealizedGainLossPercent(getBigDecimal(rs, "unrealized_gain_loss_percent"))
                .portfolioPercent(getBigDecimal(rs, "portfolio_percent"))
                .sector(rs.getString("sector"))
                .assetClass(rs.getString("asset_class"))
                .build();
            
            holdings.add(holding);
        }
        
        return holdings;
    }
    
    public PortfolioSummaryDto mapToPortfolioSummary(ResultSet rs) throws SQLException {
        if (rs.next()) {
            return PortfolioSummaryDto.builder()
                .totalMarketValue(getBigDecimal(rs, "total_market_value"))
                .totalCostBasis(getBigDecimal(rs, "total_cost_basis"))
                .totalUnrealizedGainLoss(getBigDecimal(rs, "total_unrealized_gain_loss"))
                .totalRealizedGainLossYTD(getBigDecimal(rs, "total_realized_gain_loss_ytd"))
                .numberOfHoldings(getInteger(rs, "number_of_holdings"))
                .portfolioBeta(getBigDecimal(rs, "portfolio_beta"))
                .dividendYield(getBigDecimal(rs, "dividend_yield"))
                .asOfDate(getLocalDateTime(rs, "as_of_date"))
                .build();
        }
        
        return null;
    }
    
    private BigDecimal getBigDecimal(ResultSet rs, String columnName) throws SQLException {
        BigDecimal value = rs.getBigDecimal(columnName);
        return rs.wasNull() ? null : value;
    }
    
    private Integer getInteger(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return rs.wasNull() ? null : value;
    }
    
    private LocalDateTime getLocalDateTime(ResultSet rs, String columnName) throws SQLException {
        java.sql.Timestamp timestamp = rs.getTimestamp(columnName);
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }
}
