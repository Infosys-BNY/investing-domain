import { Injectable, inject } from '@angular/core';
import { Observable, of, delay, map } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { Client, ClientListResponse, PaginatedResponse } from '../models';
import { MOCK_CLIENT_LIST_RESPONSE } from './mock-client-data';

@Injectable()
export class ClientService {
  private readonly api = inject(ApiService);
  private readonly useMockData = false;
  
  private transformPaginatedResponse(response: PaginatedResponse<Client>): ClientListResponse {
    return {
      clients: response.content || [],
      totalCount: response.totalElements || 0,
      pageInfo: {
        currentPage: response.page || 0,
        totalPages: response.totalPages || 0,
        pageSize: response.size || 50
      }
    };
  }
  
  searchClients(advisorId: string, query: string): Observable<Client[]> {
    if (this.useMockData) {
      return of(MOCK_CLIENT_LIST_RESPONSE.clients).pipe(
        map(clients => clients.filter(client =>
          client.clientName.toLowerCase().includes(query.toLowerCase()) ||
          client.accounts.some(acc => acc.accountId.toLowerCase().includes(query.toLowerCase()))
        )),
        delay(300)
      );
    }
    const requestBody = {
      advisorId,
      clientName: query,
      page: 0,
      size: 50
    };
    return this.api.post<PaginatedResponse<Client>>('clients/search', requestBody)
      .pipe(
        map(response => this.transformPaginatedResponse(response).clients)
      );
  }

  getClientList(advisorId: string, page = 0, size = 50): Observable<ClientListResponse> {
    if (this.useMockData) {
      return of(MOCK_CLIENT_LIST_RESPONSE).pipe(delay(500));
    }
    return this.api.get<PaginatedResponse<Client>>(`advisor/${advisorId}/clients`, { page, size })
      .pipe(
        map(response => this.transformPaginatedResponse(response))
      );
  }

  getRecentClients(advisorId: string): Observable<Client[]> {
    return this.getClientList(advisorId, 0, 5).pipe(
      map(response => response.clients.slice(0, 5))
    );
  }
  
  logClientAccess(clientId: string): Observable<void> {
    if (this.useMockData) {
      return of(void 0).pipe(delay(100));
    }
    return this.api.post<void>('audit/client-access', { clientId, timestamp: new Date().toISOString() });
  }
}
