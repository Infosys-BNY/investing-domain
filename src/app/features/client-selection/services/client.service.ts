import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { Client, ClientListResponse } from '../models';

@Injectable()
export class ClientService {
  private readonly api = inject(ApiService);
  private readonly useMockData = false;
  
  searchClients(query: string): Observable<Client[]> {
    return this.api.post<ClientListResponse>('clients/search', { query })
      .pipe(
        map(response => response.clients)
      );
  }
  
  getClientList(advisorId: string, page = 1, pageSize = 50): Observable<ClientListResponse> {
    return this.api.get<ClientListResponse>(`advisor/${advisorId}/clients`, { page, pageSize });
  }
  
  getRecentClients(advisorId: string): Observable<Client[]> {
    return this.api.get<Client[]>(`advisor/${advisorId}/recent-clients`);
  }
  
  logClientAccess(clientId: string): Observable<void> {
    return this.api.post<void>('audit/client-access', { clientId, timestamp: new Date().toISOString() });
  }
}
