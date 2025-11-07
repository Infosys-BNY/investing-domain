package com.bny.investing.client;

import com.bny.investing.dto.AccountDto;
import com.bny.investing.dto.ClientDto;
import com.bny.investing.dto.HoldingsResponseDto;
import com.bny.shared.dto.response.PortfolioSummaryDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LfdClientService {
    List<ClientDto> getAdvisorClients(String advisorId);
    ClientDto getClientById(String clientId);
    List<AccountDto> getClientAccounts(String clientId);
    AccountDto getAccountInfo(String accountId);
    HoldingsResponseDto getAccountHoldings(String accountId, Pageable pageable);
    PortfolioSummaryDto getPortfolioSummary(String accountId);
}
