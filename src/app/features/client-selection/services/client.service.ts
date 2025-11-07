import { Injectable, inject } from '@angular/core';
import { Observable, of, delay, map } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { Client, ClientListResponse } from '../models';
import { MOCK_CLIENT_LIST_RESPONSE, MOCK_RECENT_CLIENTS } from './mock-client-data';

@Injectable()
export class ClientService {
  private readonly api = inject(ApiService);
  private readonly useMockData = false;
  
  searchClients(query: string): Observable<Client[]> {
    if (this.useMockData) {
      return of(MOCK_CLIENT_LIST_RESPONSE.clients).pipe(
        map(clients => clients.filter(client =>
          client.clientName.toLowerCase().includes(query.toLowerCase()) ||
          client.accounts.some(acc => acc.accountId.toLowerCase().includes(query.toLowerCase()))
        )),
        delay(300)
      );
    }
    return this.api.get<ClientListResponse>(`clients/search`, { query })
      .pipe(
        map(response => response.clients)
      );
  }
  
  getClientList(advisorId: string, page = 1, pageSize = 50): Observable<ClientListResponse> {
    if (this.useMockData) {
      return of(MOCK_CLIENT_LIST_RESPONSE).pipe(delay(500));
    }
    return this.api.get<ClientListResponse>(`advisor/${advisorId}/clients`, { page, pageSize });
  }
  
  getRecentClients(advisorId: string): Observable<Client[]> {
    if (this.useMockData) {
      return of(MOCK_RECENT_CLIENTS).pipe(delay(300));
    }
    return this.api.get<Client[]>(`advisor/${advisorId}/recent-clients`);
  }
  
  logClientAccess(clientId: string): Observable<void> {
    if (this.useMockData) {
      return of(void 0).pipe(delay(100));
    }
    return this.api.post<void>('audit/client-access', { clientId, timestamp: new Date().toISOString() });
  }
}
