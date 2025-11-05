package com.bny.investing.client;

import com.bny.investing.dto.*;
import com.bny.investing.exception.ResourceNotFoundException;
import com.bny.shared.dto.response.PortfolioSummaryDto;
import com.bny.investing.model.AccountType;
import com.bny.investing.model.AssetClass;
import com.bny.investing.model.RiskProfile;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(name = "app.mock.enabled", havingValue = "true", matchIfMissing = true)
public class MockLfdClientService implements LfdClientService {

    private final Map<String, ClientDto> mockClients = new HashMap<>();
    private final Map<String, List<AccountDto>> mockAccounts = new HashMap<>();

    public MockLfdClientService() {
        initializeMockData();
    }

    private void initializeMockData() {
        String advisorId = "advisor-001";
        
        AccountDto account1 = AccountDto.builder()
                .accountId("acc-001")
                .accountNumber("12345678")
                .accountType(AccountType.INDIVIDUAL)
                .accountName("Personal Investment Account")
                .marketValue(new BigDecimal("2450000.00"))
                .costBasis(new BigDecimal("2000000.00"))
                .unrealizedGainLoss(new BigDecimal("450000.00"))
                .ytdPerformance(new BigDecimal("12.5"))
                .cashBalance(new BigDecimal("50000.00"))
                .inceptionDate(LocalDate.of(2020, 1, 15))
                .lastActivity(LocalDateTime.now().minusDays(2))
                .build();

        AccountDto account2 = AccountDto.builder()
                .accountId("acc-002")
                .accountNumber("87654321")
                .accountType(AccountType.IRA)
                .accountName("Traditional IRA")
                .marketValue(new BigDecimal("1200000.00"))
                .costBasis(new BigDecimal("1100000.00"))
                .unrealizedGainLoss(new BigDecimal("100000.00"))
                .ytdPerformance(new BigDecimal("8.3"))
                .cashBalance(new BigDecimal("20000.00"))
                .inceptionDate(LocalDate.of(2018, 3, 20))
                .lastActivity(LocalDateTime.now().minusDays(5))
                .build();

        List<AccountDto> client1Accounts = Arrays.asList(account1, account2);
        mockAccounts.put("client-001", client1Accounts);

        ClientDto client1 = ClientDto.builder()
                .clientId("client-001")
                .clientName("John Smith")
                .accounts(client1Accounts)
                .taxId("123-45-6789")
                .riskProfile(RiskProfile.MODERATE)
                .totalMarketValue(new BigDecimal("3650000.00"))
                .totalCostBasis(new BigDecimal("3100000.00"))
                .totalUnrealizedGainLoss(new BigDecimal("550000.00"))
                .ytdPerformance(new BigDecimal("10.8"))
                .lastAccessed(LocalDateTime.now().minusHours(3))
                .advisorId(advisorId)
                .advisorName("Jane Advisor")
                .build();

        mockClients.put("client-001", client1);

        AccountDto account3 = AccountDto.builder()
                .accountId("acc-003")
                .accountNumber("11223344")
                .accountType(AccountType.JOINT)
                .accountName("Joint Investment Account")
                .marketValue(new BigDecimal("5000000.00"))
                .costBasis(new BigDecimal("4500000.00"))
                .unrealizedGainLoss(new BigDecimal("500000.00"))
                .ytdPerformance(new BigDecimal("15.2"))
                .cashBalance(new BigDecimal("100000.00"))
                .inceptionDate(LocalDate.of(2019, 6, 10))
                .lastActivity(LocalDateTime.now().minusDays(1))
                .build();

        List<AccountDto> client2Accounts = List.of(account3);
        mockAccounts.put("client-002", client2Accounts);

        ClientDto client2 = ClientDto.builder()
                .clientId("client-002")
                .clientName("Sarah Johnson")
                .accounts(client2Accounts)
                .taxId("987-65-4321")
                .riskProfile(RiskProfile.AGGRESSIVE)
                .totalMarketValue(new BigDecimal("5000000.00"))
                .totalCostBasis(new BigDecimal("4500000.00"))
                .totalUnrealizedGainLoss(new BigDecimal("500000.00"))
                .ytdPerformance(new BigDecimal("15.2"))
                .lastAccessed(LocalDateTime.now().minusDays(1))
                .advisorId(advisorId)
                .advisorName("Jane Advisor")
                .build();

        mockClients.put("client-002", client2);

        AccountDto account4 = AccountDto.builder()
                .accountId("acc-004")
                .accountNumber("55667788")
                .accountType(AccountType.TRUST)
                .accountName("Family Trust")
                .marketValue(new BigDecimal("10000000.00"))
                .costBasis(new BigDecimal("9000000.00"))
                .unrealizedGainLoss(new BigDecimal("1000000.00"))
                .ytdPerformance(new BigDecimal("6.5"))
                .cashBalance(new BigDecimal("500000.00"))
                .inceptionDate(LocalDate.of(2015, 9, 1))
                .lastActivity(LocalDateTime.now().minusDays(7))
                .build();

        List<AccountDto> client3Accounts = List.of(account4);
        mockAccounts.put("client-003", client3Accounts);

        ClientDto client3 = ClientDto.builder()
                .clientId("client-003")
                .clientName("Michael Williams")
                .accounts(client3Accounts)
                .taxId("456-78-9012")
                .riskProfile(RiskProfile.CONSERVATIVE)
                .totalMarketValue(new BigDecimal("10000000.00"))
                .totalCostBasis(new BigDecimal("9000000.00"))
                .totalUnrealizedGainLoss(new BigDecimal("1000000.00"))
                .ytdPerformance(new BigDecimal("6.5"))
                .lastAccessed(LocalDateTime.now().minusDays(10))
                .advisorId(advisorId)
                .advisorName("Jane Advisor")
                .build();

        mockClients.put("client-003", client3);
    }

    @Override
    public List<ClientDto> getAdvisorClients(String advisorId) {
        return mockClients.values().stream()
                .filter(client -> client.getAdvisorId().equals(advisorId))
                .collect(Collectors.toList());
    }

    @Override
    public ClientDto getClientById(String clientId) {
        return Optional.ofNullable(mockClients.get(clientId))
                .orElseThrow(() -> new ResourceNotFoundException("Client not found: " + clientId));
    }

    @Override
    public List<AccountDto> getClientAccounts(String clientId) {
        return Optional.ofNullable(mockAccounts.get(clientId))
                .orElseThrow(() -> new ResourceNotFoundException("Accounts not found for client: " + clientId));
    }

    @Override
    public HoldingsResponseDto getAccountHoldings(String accountId, Pageable pageable) {
        List<HoldingDto> allHoldings = generateMockHoldings(accountId);
        
        int start = pageable.getPageNumber() * pageable.getPageSize();
        int end = Math.min(start + pageable.getPageSize(), allHoldings.size());
        List<HoldingDto> pagedHoldings = allHoldings.subList(start, end);
        
        AccountDto accountInfo = getAccountInfo(accountId);
        PortfolioSummaryDto summary = calculatePortfolioSummary(allHoldings);
        
        return HoldingsResponseDto.builder()
                .accountInfo(accountInfo)
                .summary(summary)
                .holdings(pagedHoldings)
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .totalElements(allHoldings.size())
                .totalPages((int) Math.ceil((double) allHoldings.size() / pageable.getPageSize()))
                .build();
    }
    
    @Override
    public PortfolioSummaryDto getPortfolioSummary(String accountId) {
        List<HoldingDto> holdings = generateMockHoldings(accountId);
        return calculatePortfolioSummary(holdings);
    }
    
    private List<HoldingDto> generateMockHoldings(String accountId) {
        List<HoldingDto> holdings = new ArrayList<>();
        
        holdings.add(HoldingDto.builder()
                .symbol("AAPL")
                .securityName("Apple Inc.")
                .quantity(BigDecimal.valueOf(500))
                .currentPrice(BigDecimal.valueOf(175.50))
                .priceChange(BigDecimal.valueOf(2.30))
                .priceChangePercent(BigDecimal.valueOf(1.33))
                .costBasis(BigDecimal.valueOf(150.00))
                .totalCost(BigDecimal.valueOf(75000))
                .marketValue(BigDecimal.valueOf(87750))
                .unrealizedGainLoss(BigDecimal.valueOf(12750))
                .unrealizedGainLossPercent(BigDecimal.valueOf(17.0))
                .portfolioPercent(BigDecimal.valueOf(35.1))
                .sector("Technology")
                .assetClass(AssetClass.EQUITY)
                .hasAlerts(false)
                .taxLotCount(3)
                .build());
        
        holdings.add(HoldingDto.builder()
                .symbol("MSFT")
                .securityName("Microsoft Corporation")
                .quantity(BigDecimal.valueOf(300))
                .currentPrice(BigDecimal.valueOf(380.25))
                .priceChange(BigDecimal.valueOf(-1.75))
                .priceChangePercent(BigDecimal.valueOf(-0.46))
                .costBasis(BigDecimal.valueOf(320.00))
                .totalCost(BigDecimal.valueOf(96000))
                .marketValue(BigDecimal.valueOf(114075))
                .unrealizedGainLoss(BigDecimal.valueOf(18075))
                .unrealizedGainLossPercent(BigDecimal.valueOf(18.83))
                .portfolioPercent(BigDecimal.valueOf(45.6))
                .sector("Technology")
                .assetClass(AssetClass.EQUITY)
                .hasAlerts(false)
                .taxLotCount(2)
                .build());
        
        holdings.add(HoldingDto.builder()
                .symbol("BND")
                .securityName("Vanguard Total Bond Market ETF")
                .quantity(BigDecimal.valueOf(600))
                .currentPrice(BigDecimal.valueOf(75.80))
                .priceChange(BigDecimal.valueOf(0.15))
                .priceChangePercent(BigDecimal.valueOf(0.20))
                .costBasis(BigDecimal.valueOf(78.00))
                .totalCost(BigDecimal.valueOf(46800))
                .marketValue(BigDecimal.valueOf(45480))
                .unrealizedGainLoss(BigDecimal.valueOf(-1320))
                .unrealizedGainLossPercent(BigDecimal.valueOf(-2.82))
                .portfolioPercent(BigDecimal.valueOf(18.2))
                .sector("Fixed Income")
                .assetClass(AssetClass.FIXED_INCOME)
                .hasAlerts(false)
                .taxLotCount(1)
                .build());
        
        holdings.add(HoldingDto.builder()
                .symbol("CASH")
                .securityName("Cash")
                .quantity(BigDecimal.valueOf(2695))
                .currentPrice(BigDecimal.ONE)
                .priceChange(BigDecimal.ZERO)
                .priceChangePercent(BigDecimal.ZERO)
                .costBasis(BigDecimal.ONE)
                .totalCost(BigDecimal.valueOf(2695))
                .marketValue(BigDecimal.valueOf(2695))
                .unrealizedGainLoss(BigDecimal.ZERO)
                .unrealizedGainLossPercent(BigDecimal.ZERO)
                .portfolioPercent(BigDecimal.valueOf(1.1))
                .sector("Cash")
                .assetClass(AssetClass.CASH)
                .hasAlerts(false)
                .taxLotCount(0)
                .build());
        
        return holdings;
    }
    
    private AccountDto getAccountInfo(String accountId) {
        return AccountDto.builder()
                .accountId(accountId)
                .accountNumber("12345678")
                .accountType(AccountType.INDIVIDUAL)
                .accountName("Personal Investment Account")
                .marketValue(BigDecimal.valueOf(250000))
                .costBasis(BigDecimal.valueOf(218800))
                .unrealizedGainLoss(BigDecimal.valueOf(31200))
                .ytdPerformance(BigDecimal.valueOf(12.5))
                .cashBalance(BigDecimal.valueOf(2695))
                .inceptionDate(LocalDate.parse("2020-01-15"))
                .lastActivity(LocalDateTime.now().minusDays(2))
                .build();
    }
    
    private PortfolioSummaryDto calculatePortfolioSummary(List<HoldingDto> holdings) {
        BigDecimal totalMarketValue = holdings.stream()
                .map(HoldingDto::getMarketValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalCostBasis = holdings.stream()
                .map(HoldingDto::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalUnrealizedGainLoss = holdings.stream()
                .map(HoldingDto::getUnrealizedGainLoss)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return PortfolioSummaryDto.builder()
                .totalMarketValue(totalMarketValue)
                .totalCostBasis(totalCostBasis)
                .totalUnrealizedGainLoss(totalUnrealizedGainLoss)
                .totalRealizedGainLossYTD(BigDecimal.valueOf(5420.75))
                .numberOfHoldings(holdings.size())
                .portfolioBeta(BigDecimal.valueOf(0.92))
                .dividendYield(BigDecimal.valueOf(1.85))
                .asOfDate(LocalDateTime.now())
                .build();
    }
}
