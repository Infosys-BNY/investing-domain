import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { 
  Holding, 
  HoldingsResponse, 
  TaxLot
} from '../models';
import { ApiService } from '../../../core/services/api.service';

@Injectable({
  providedIn: 'root'
})
export class HoldingsService {
  private readonly api = inject(ApiService);
  private useMockData = false;

  getHoldings(clientId: string, accountId: string): Observable<HoldingsResponse> {
    return this.api.get<HoldingsResponse>(`accounts/${accountId}/holdings`);
  }

  getTaxLots(accountId: string, symbol: string): Observable<TaxLot[]> {
    return this.api.get<TaxLot[]>(`accounts/${accountId}/holdings/${symbol}/taxlots`);
  }

  exportHoldings(accountId: string, format: 'xlsx' | 'csv' | 'pdf', holdings: Holding[]): Observable<Blob> {
    return this.api.post<Blob>('export/holdings', {
      accountId,
      format,
      holdings
    });
  }
}
