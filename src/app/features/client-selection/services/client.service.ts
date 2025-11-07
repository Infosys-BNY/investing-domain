import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { Client, ClientListResponse, SpringPageResponse } from '../models';

/**
 * Client Service - Connected to Domain API
 * All mocks removed - using real backend data
 */
@Injectable()
export class ClientService {
  private readonly api = inject(ApiService);
  
  /**
   * Search clients by query string
   * Note: Backend doesn't have a search endpoint yet, so we fetch all and filter client-side
   */
  searchClients(query: string): Observable<Client[]> {
    // For now, get all clients and filter client-side
    // TODO: Add backend search endpoint for better performance
    return this.api.get<SpringPageResponse<Client>>(`advisor/ADV001/clients`)
      .pipe(
        map(response => response.content.filter(client =>
          client.clientName.toLowerCase().includes(query.toLowerCase()) ||
          client.clientId.toLowerCase().includes(query.toLowerCase())
        ))
      );
  }
  
  /**
   * Get paginated client list for an advisor
   * Maps Spring Page response to ClientListResponse
   */
  getClientList(advisorId: string, page = 0, pageSize = 50): Observable<ClientListResponse> {
    return this.api.get<SpringPageResponse<Client>>(`advisor/${advisorId}/clients`, { page, size: pageSize })
      .pipe(
        map(springPage => ({
          clients: springPage.content,
          totalCount: springPage.totalElements,
          pageInfo: {
            currentPage: (springPage.number ?? 0) + 1, // Spring pages are 0-indexed, UI is 1-indexed
            pageSize: springPage.size,
            totalPages: springPage.totalPages,
            totalRecords: springPage.totalElements
          }
        }))
      );
  }
  
  /**
   * Get recent clients for an advisor
   * Note: Backend doesn't have a recent-clients endpoint, so we return the first 5 clients
   */
  getRecentClients(advisorId: string): Observable<Client[]> {
    // Backend doesn't have a recent-clients endpoint yet
    // Return first 5 clients as "recent" for now
    return this.api.get<SpringPageResponse<Client>>(`advisor/${advisorId}/clients`, { page: 0, size: 5 })
      .pipe(
        map(response => response.content)
      );
  }
  
  /**
   * Log client access for audit trail
   * Note: Backend doesn't have audit endpoint yet - this is a no-op
   */
  logClientAccess(clientId: string): Observable<void> {
    // Backend doesn't have audit endpoint yet
    // Return empty observable for now
    // TODO: Implement audit logging endpoint
    return new Observable(observer => {
      console.log(`Client access logged: ${clientId}`);
      observer.next();
      observer.complete();
    });
  }
}
