package com.bny.investing.client;

import com.bny.investing.dto.AccountDto;
import com.bny.investing.dto.ClientDto;

import java.util.List;

public interface LfdClientService {
    List<ClientDto> getAdvisorClients(String advisorId);
    ClientDto getClientById(String clientId);
    List<AccountDto> getClientAccounts(String clientId);
}
