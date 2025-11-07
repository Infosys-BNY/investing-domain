import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { 
  HoldingsResponse, 
  TaxLot, 
  HoldingPeriod
} from '../models';

/**
 * Holdings Service - Connected to Domain API
 * All mocks removed - using real backend data
 */
@Injectable({
  providedIn: 'root'
})
export class HoldingsService {
  private readonly api = inject(ApiService);

  /**
   * Get holdings for an account
   * Returns holdings list with accountInfo and summary
   */
  getHoldings(clientId: string, accountId: string): Observable<HoldingsResponse> {
    // Note: clientId parameter not used by backend, but kept for compatibility
    return this.api.get<HoldingsResponse>(`accounts/${accountId}/holdings`);
  }

  /**
   * Get tax lots for a specific holding
   * Note: Backend doesn't have tax lot endpoint yet - returns mock data
   * TODO: Implement backend tax lot endpoint
   */
  getTaxLots(accountId: string, symbol: string): Observable<TaxLot[]> {
    // Backend doesn't have tax lot endpoint yet
    // Return simplified mock data for now
    console.log(`Tax lots requested for ${symbol} in account ${accountId} - using mock data`);
    
    const mockTaxLots: TaxLot[] = [
      {
        purchaseDate: new Date('2022-01-15'),
        quantity: 100,
        costBasis: 100.00,
        currentValue: 10000.00,
        gainLoss: 0.00,
        gainLossPercent: 0.00,
        holdingPeriod: HoldingPeriod.LONG_TERM,
        taxImpact: 0.00
      }
    ];
    
    return new Observable(observer => {
      observer.next(mockTaxLots);
      observer.complete();
    });
  }

  /**
   * Export holdings to file
   * Note: Backend doesn't have export endpoint yet
   * TODO: Implement backend export endpoint
   */
  exportHoldings(accountId: string, format: 'xlsx' | 'csv' | 'pdf'): Observable<Blob> {
    // Backend doesn't have export endpoint yet
    // Return placeholder blob
    console.log(`Export requested for account ${accountId} in format: ${format} - not yet implemented`);
    return new Observable(observer => {
      observer.next(new Blob(['Export functionality not yet implemented'], { type: 'text/plain' }));
      observer.complete();
    });
  }
}
