import { Injectable, inject } from '@angular/core';
import { Observable, of, delay, map } from 'rxjs';
import { 
  Holding, 
  HoldingsResponse, 
  TaxLot, 
  AssetClass, 
  HoldingPeriod,
  AccountInfo,
  PortfolioSummary
} from '../models';
import { ApiService } from '../../../core/services/api.service';

interface BackendHoldingDto {
  symbol: string;
  securityName: string;
  quantity: number;
  price: number;
  priceChange?: number;
  priceChangePercent?: number;
  costBasis: number;
  totalCost: number;
  marketValue: number;
  unrealizedGainLoss: number;
  unrealizedGainLossPercent: number;
  portfolioPercent: number;
  sector: string;
  assetClass: string;
  hasAlerts?: boolean;
  taxLotCount?: number;
}

interface BackendAccountDto {
  accountId: string;
  clientName: string;
  accountType: string;
  totalCashPosition?: number;
  asOfDate?: string;
}

interface BackendPortfolioSummaryDto {
  totalMarketValue: number;
  totalCostBasis: number;
  totalUnrealizedGainLoss: number;
  totalUnrealizedGainLossPercent: number;
  totalRealizedGainLossYTD?: number;
  numberOfHoldings: number;
  portfolioBeta?: number;
  dividendYield?: number;
}

interface BackendHoldingsResponse {
  accountInfo: BackendAccountDto;
  summary: BackendPortfolioSummaryDto;
  holdings: BackendHoldingDto[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

@Injectable({
  providedIn: 'root'
})
export class HoldingsService {
  private readonly api = inject(ApiService);
  private useMockData = false;

  getHoldings(clientId: string, accountId: string): Observable<HoldingsResponse> {
    if (this.useMockData) {
      return of(this.createMockHoldingsResponse(clientId, accountId)).pipe(delay(500));
    }
    return this.api.get<BackendHoldingsResponse>(`accounts/${accountId}/holdings`, { page: 0, size: 1000 }).pipe(
      map(response => this.transformBackendResponse(response, clientId))
    );
  }

  private transformBackendResponse(response: BackendHoldingsResponse, clientId: string): HoldingsResponse {
    const accountInfo: AccountInfo = {
      accountId: response.accountInfo?.accountId || '',
      clientId: clientId,
      clientName: response.accountInfo?.clientName || '',
      accountType: response.accountInfo?.accountType || '',
      totalPortfolioValue: response.summary?.totalMarketValue || 0,
      totalCashPosition: response.accountInfo?.totalCashPosition || 0,
      asOfDate: response.accountInfo?.asOfDate ? new Date(response.accountInfo.asOfDate) : new Date()
    };

    const summary: PortfolioSummary = {
      totalMarketValue: response.summary?.totalMarketValue || 0,
      totalCostBasis: response.summary?.totalCostBasis || 0,
      totalUnrealizedGainLoss: response.summary?.totalUnrealizedGainLoss || 0,
      totalUnrealizedGainLossPercent: response.summary?.totalUnrealizedGainLossPercent || 0,
      totalRealizedGainLossYTD: response.summary?.totalRealizedGainLossYTD,
      numberOfHoldings: response.summary?.numberOfHoldings || response.holdings?.length || 0,
      portfolioBeta: response.summary?.portfolioBeta,
      dividendYield: response.summary?.dividendYield
    };

    const holdings: Holding[] = (response.holdings || []).map((h: BackendHoldingDto) => ({
      symbol: h.symbol,
      securityName: h.securityName,
      quantity: h.quantity,
      price: h.price,
      priceChange: h.priceChange,
      priceChangePercent: h.priceChangePercent,
      costBasis: h.costBasis,
      totalCost: h.totalCost,
      marketValue: h.marketValue,
      unrealizedGainLoss: h.unrealizedGainLoss,
      unrealizedGainLossPercent: h.unrealizedGainLossPercent,
      portfolioPercent: h.portfolioPercent,
      sector: h.sector,
      assetClass: h.assetClass as AssetClass,
      hasAlerts: h.hasAlerts,
      taxLotCount: h.taxLotCount
    }));

    return {
      accountInfo,
      summary,
      holdings,
      pagination: response.totalElements !== undefined ? {
        totalRecords: response.totalElements,
        currentPage: response.page || 0,
        pageSize: response.size || holdings.length,
        totalPages: response.totalPages || 1
      } : undefined
    };
  }

  getTaxLots(accountId: string, symbol: string): Observable<TaxLot[]> {
    if (this.useMockData) {
      return of(this.createMockTaxLots(symbol)).pipe(delay(500));
    }
    return of(this.createMockTaxLots(symbol));
  }

  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  exportHoldings(_accountId: string, _format: 'xlsx' | 'csv' | 'pdf', _holdings: Holding[]): Observable<Blob> {
    if (this.useMockData) {
      return of(new Blob(['Mock export data'], { type: 'text/plain' })).pipe(delay(1000));
    }
    return of(new Blob(['Mock export data'], { type: 'text/plain' }));
  }

  private createMockHoldingsResponse(clientId: string, accountId: string): HoldingsResponse {
    const accountInfo: AccountInfo = {
      accountId: accountId,
      clientId: clientId,
      clientName: this.getClientName(clientId),
      accountType: 'Individual',
      totalPortfolioValue: 1245000.00,
      totalCashPosition: 45000.00,
      asOfDate: new Date()
    };

    const holdings: Holding[] = [
      {
        symbol: 'AAPL',
        securityName: 'Apple Inc.',
        quantity: 500,
        price: 178.50,
        priceChange: 2.50,
        priceChangePercent: 1.42,
        costBasis: 145.00,
        totalCost: 72500.00,
        marketValue: 89250.00,
        unrealizedGainLoss: 16750.00,
        unrealizedGainLossPercent: 23.10,
        portfolioPercent: 7.17,
        sector: 'Technology',
        assetClass: AssetClass.EQUITY,
        hasAlerts: false,
        taxLotCount: 3
      },
      {
        symbol: 'MSFT',
        securityName: 'Microsoft Corporation',
        quantity: 300,
        price: 378.25,
        priceChange: -1.75,
        priceChangePercent: -0.46,
        costBasis: 340.00,
        totalCost: 102000.00,
        marketValue: 113475.00,
        unrealizedGainLoss: 11475.00,
        unrealizedGainLossPercent: 11.25,
        portfolioPercent: 9.11,
        sector: 'Technology',
        assetClass: AssetClass.EQUITY,
        hasAlerts: false,
        taxLotCount: 2
      },
      {
        symbol: 'GOOGL',
        securityName: 'Alphabet Inc. Class A',
        quantity: 400,
        price: 142.80,
        priceChange: 3.20,
        priceChangePercent: 2.29,
        costBasis: 128.50,
        totalCost: 51400.00,
        marketValue: 57120.00,
        unrealizedGainLoss: 5720.00,
        unrealizedGainLossPercent: 11.13,
        portfolioPercent: 4.59,
        sector: 'Technology',
        assetClass: AssetClass.EQUITY,
        hasAlerts: false,
        taxLotCount: 2
      },
      {
        symbol: 'AMZN',
        securityName: 'Amazon.com Inc.',
        quantity: 250,
        price: 178.35,
        priceChange: 1.85,
        priceChangePercent: 1.05,
        costBasis: 165.00,
        totalCost: 41250.00,
        marketValue: 44587.50,
        unrealizedGainLoss: 3337.50,
        unrealizedGainLossPercent: 8.09,
        portfolioPercent: 3.58,
        sector: 'Consumer Discretionary',
        assetClass: AssetClass.EQUITY,
        hasAlerts: false,
        taxLotCount: 1
      },
      {
        symbol: 'TSLA',
        securityName: 'Tesla Inc.',
        quantity: 150,
        price: 242.50,
        priceChange: -5.25,
        priceChangePercent: -2.12,
        costBasis: 285.00,
        totalCost: 42750.00,
        marketValue: 36375.00,
        unrealizedGainLoss: -6375.00,
        unrealizedGainLossPercent: -14.91,
        portfolioPercent: 2.92,
        sector: 'Consumer Discretionary',
        assetClass: AssetClass.EQUITY,
        hasAlerts: true,
        taxLotCount: 2
      },
      {
        symbol: 'JPM',
        securityName: 'JPMorgan Chase & Co.',
        quantity: 600,
        price: 158.75,
        priceChange: 0.50,
        priceChangePercent: 0.32,
        costBasis: 142.00,
        totalCost: 85200.00,
        marketValue: 95250.00,
        unrealizedGainLoss: 10050.00,
        unrealizedGainLossPercent: 11.80,
        portfolioPercent: 7.65,
        sector: 'Financials',
        assetClass: AssetClass.EQUITY,
        hasAlerts: false,
        taxLotCount: 3
      },
      {
        symbol: 'JNJ',
        securityName: 'Johnson & Johnson',
        quantity: 400,
        price: 156.80,
        priceChange: 1.20,
        priceChangePercent: 0.77,
        costBasis: 158.50,
        totalCost: 63400.00,
        marketValue: 62720.00,
        unrealizedGainLoss: -680.00,
        unrealizedGainLossPercent: -1.07,
        portfolioPercent: 5.04,
        sector: 'Healthcare',
        assetClass: AssetClass.EQUITY,
        hasAlerts: false,
        taxLotCount: 2
      },
      {
        symbol: 'XOM',
        securityName: 'Exxon Mobil Corporation',
        quantity: 800,
        price: 112.45,
        priceChange: -0.85,
        priceChangePercent: -0.75,
        costBasis: 98.00,
        totalCost: 78400.00,
        marketValue: 89960.00,
        unrealizedGainLoss: 11560.00,
        unrealizedGainLossPercent: 14.74,
        portfolioPercent: 7.22,
        sector: 'Energy',
        assetClass: AssetClass.EQUITY,
        hasAlerts: false,
        taxLotCount: 4
      },
      {
        symbol: 'BND',
        securityName: 'Vanguard Total Bond Market ETF',
        quantity: 2000,
        price: 74.85,
        priceChange: 0.15,
        priceChangePercent: 0.20,
        costBasis: 76.50,
        totalCost: 153000.00,
        marketValue: 149700.00,
        unrealizedGainLoss: -3300.00,
        unrealizedGainLossPercent: -2.16,
        portfolioPercent: 12.02,
        sector: 'Fixed Income',
        assetClass: AssetClass.FIXED_INCOME,
        hasAlerts: false,
        taxLotCount: 5
      },
      {
        symbol: 'VNQ',
        securityName: 'Vanguard Real Estate ETF',
        quantity: 1000,
        price: 87.25,
        priceChange: 0.75,
        priceChangePercent: 0.87,
        costBasis: 92.00,
        totalCost: 92000.00,
        marketValue: 87250.00,
        unrealizedGainLoss: -4750.00,
        unrealizedGainLossPercent: -5.16,
        portfolioPercent: 7.01,
        sector: 'Real Estate',
        assetClass: AssetClass.EQUITY,
        hasAlerts: false,
        taxLotCount: 2
      },
      {
        symbol: 'GLD',
        securityName: 'SPDR Gold Trust',
        quantity: 500,
        price: 188.50,
        priceChange: 2.10,
        priceChangePercent: 1.13,
        costBasis: 175.00,
        totalCost: 87500.00,
        marketValue: 94250.00,
        unrealizedGainLoss: 6750.00,
        unrealizedGainLossPercent: 7.71,
        portfolioPercent: 7.57,
        sector: 'Commodities',
        assetClass: AssetClass.ALTERNATIVE,
        hasAlerts: false,
        taxLotCount: 3
      },
      {
        symbol: 'VOO',
        securityName: 'Vanguard S&P 500 ETF',
        quantity: 600,
        price: 445.75,
        priceChange: 3.25,
        priceChangePercent: 0.73,
        costBasis: 420.00,
        totalCost: 252000.00,
        marketValue: 267450.00,
        unrealizedGainLoss: 15450.00,
        unrealizedGainLossPercent: 6.13,
        portfolioPercent: 21.48,
        sector: 'Diversified',
        assetClass: AssetClass.EQUITY,
        hasAlerts: true,
        taxLotCount: 4
      },
      {
        symbol: 'BAC',
        securityName: 'Bank of America Corp.',
        quantity: 1200,
        price: 32.85,
        priceChange: 0.35,
        priceChangePercent: 1.08,
        costBasis: 35.50,
        totalCost: 42600.00,
        marketValue: 39420.00,
        unrealizedGainLoss: -3180.00,
        unrealizedGainLossPercent: -7.46,
        portfolioPercent: 3.17,
        sector: 'Financials',
        assetClass: AssetClass.EQUITY,
        hasAlerts: false,
        taxLotCount: 2
      },
      {
        symbol: 'DIS',
        securityName: 'The Walt Disney Company',
        quantity: 350,
        price: 95.40,
        priceChange: -1.20,
        priceChangePercent: -1.24,
        costBasis: 105.00,
        totalCost: 36750.00,
        marketValue: 33390.00,
        unrealizedGainLoss: -3360.00,
        unrealizedGainLossPercent: -9.14,
        portfolioPercent: 2.68,
        sector: 'Communication Services',
        assetClass: AssetClass.EQUITY,
        hasAlerts: false,
        taxLotCount: 1
      }
    ];

    const totalMarketValue = holdings.reduce((sum, h) => sum + h.marketValue, 0);
    const totalCostBasis = holdings.reduce((sum, h) => sum + h.totalCost, 0);
    const totalUnrealizedGainLoss = totalMarketValue - totalCostBasis;
    const totalUnrealizedGainLossPercent = (totalUnrealizedGainLoss / totalCostBasis) * 100;

    const summary: PortfolioSummary = {
      totalMarketValue: totalMarketValue,
      totalCostBasis: totalCostBasis,
      totalUnrealizedGainLoss: totalUnrealizedGainLoss,
      totalUnrealizedGainLossPercent: totalUnrealizedGainLossPercent,
      totalRealizedGainLossYTD: 12500.00,
      numberOfHoldings: holdings.length,
      portfolioBeta: 1.08,
      dividendYield: 2.35
    };

    return {
      accountInfo,
      summary,
      holdings
    };
  }

  private createMockTaxLots(symbol: string): TaxLot[] {
    const taxLots: TaxLot[] = [];

    if (symbol === 'AAPL') {
      taxLots.push(
        {
          purchaseDate: new Date('2022-03-15'),
          quantity: 200,
          costBasis: 150.00,
          currentValue: 35700.00,
          gainLoss: 5700.00,
          gainLossPercent: 19.00,
          holdingPeriod: HoldingPeriod.LONG_TERM,
          taxImpact: 855.00
        },
        {
          purchaseDate: new Date('2023-06-20'),
          quantity: 150,
          costBasis: 142.00,
          currentValue: 26775.00,
          gainLoss: 5475.00,
          gainLossPercent: 25.70,
          holdingPeriod: HoldingPeriod.LONG_TERM,
          taxImpact: 821.25
        },
        {
          purchaseDate: new Date('2024-01-10'),
          quantity: 150,
          costBasis: 143.00,
          currentValue: 26775.00,
          gainLoss: 5325.00,
          gainLossPercent: 24.83,
          holdingPeriod: HoldingPeriod.SHORT_TERM,
          taxImpact: 1863.75
        }
      );
    } else if (symbol === 'MSFT') {
      taxLots.push(
        {
          purchaseDate: new Date('2021-09-01'),
          quantity: 200,
          costBasis: 335.00,
          currentValue: 75650.00,
          gainLoss: 8650.00,
          gainLossPercent: 12.91,
          holdingPeriod: HoldingPeriod.LONG_TERM,
          taxImpact: 1297.50
        },
        {
          purchaseDate: new Date('2023-11-15'),
          quantity: 100,
          costBasis: 350.00,
          currentValue: 37825.00,
          gainLoss: 2825.00,
          gainLossPercent: 8.07,
          holdingPeriod: HoldingPeriod.LONG_TERM,
          taxImpact: 423.75
        }
      );
    } else if (symbol === 'TSLA') {
      taxLots.push(
        {
          purchaseDate: new Date('2022-08-20'),
          quantity: 100,
          costBasis: 280.00,
          currentValue: 24250.00,
          gainLoss: -3750.00,
          gainLossPercent: -13.39,
          holdingPeriod: HoldingPeriod.LONG_TERM,
          taxImpact: -562.50
        },
        {
          purchaseDate: new Date('2023-12-05'),
          quantity: 50,
          costBasis: 295.00,
          currentValue: 12125.00,
          gainLoss: -2625.00,
          gainLossPercent: -17.80,
          holdingPeriod: HoldingPeriod.LONG_TERM,
          taxImpact: -393.75
        }
      );
    } else {
      taxLots.push(
        {
          purchaseDate: new Date('2022-01-01'),
          quantity: 100,
          costBasis: 100.00,
          currentValue: 10000.00,
          gainLoss: 0.00,
          gainLossPercent: 0.00,
          holdingPeriod: HoldingPeriod.LONG_TERM,
          taxImpact: 0.00
        }
      );
    }

    return taxLots;
  }

  private getClientName(clientId: string): string {
    const clientNames: Record<string, string> = {
      'CLT-001': 'John Smith',
      'CLT-002': 'Sarah Johnson',
      'CLT-003': 'Michael Chen',
      'CLT-004': 'Davis, Patricia',
      'CLT-005': 'Miller, James',
      'CLT-006': 'Wilson, Jennifer',
      'CLT-007': 'Brown, Michael',
      'CLT-008': 'Taylor, Sarah',
      'CLT-009': 'Anderson, David',
      'CLT-010': 'Martinez, Linda'
    };
    return clientNames[clientId] || 'Unknown Client';
  }
}
