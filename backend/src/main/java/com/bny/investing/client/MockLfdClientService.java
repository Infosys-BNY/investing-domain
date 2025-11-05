package com.bny.investing.client;

import com.bny.investing.dto.AccountDto;
import com.bny.investing.dto.ClientDto;
import com.bny.investing.exception.ResourceNotFoundException;
import com.bny.investing.model.AccountType;
import com.bny.investing.model.RiskProfile;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
}
