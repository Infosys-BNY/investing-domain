export interface Holding {
  readonly symbol: string;
  securityName: string;
  quantity: number;
  currentPrice: number;
  priceChange?: number;
  priceChangePercent?: number;
  costBasis: number;
  totalCost: number;
  marketValue: number;
  unrealizedGainLoss: number;
  unrealizedGainLossPercent: number;
  portfolioPercent: number;
  sector: string;
  assetClass: AssetClass;
  hasAlerts?: boolean;
  taxLotCount?: number;
}

export enum AssetClass {
  EQUITY = 'Equity',
  FIXED_INCOME = 'Fixed Income',
  ALTERNATIVE = 'Alternative',
  CASH = 'Cash'
}

export interface TaxLot {
  purchaseDate: Date;
  quantity: number;
  costBasis: number;
  currentValue: number;
  gainLoss: number;
  gainLossPercent: number;
  holdingPeriod: HoldingPeriod;
  taxImpact?: number;
}

export enum HoldingPeriod {
  SHORT_TERM = 'Short-term',
  LONG_TERM = 'Long-term'
}

export interface PortfolioSummary {
  totalMarketValue: number;
  totalCostBasis: number;
  totalUnrealizedGainLoss: number;
  totalUnrealizedGainLossPercent: number;
  totalRealizedGainLossYTD?: number;
  numberOfHoldings: number;
  portfolioBeta?: number;
  dividendYield?: number;
}

export interface AccountInfo {
  accountId: string;
  clientId: string;
  clientName: string;
  accountType: string;
  marketValue: number;
  cashBalance?: number;
  asOfDate: Date;
}

export interface HoldingsResponse {
  accountInfo: AccountInfo;
  summary: PortfolioSummary;
  holdings: Holding[];
  pagination?: {
    totalRecords: number;
    currentPage: number;
    pageSize: number;
    totalPages: number;
  };
}

export type FilterType = 'all' | 'gains' | 'losses' | 'equities' | 'fixedIncome' | 'alternatives' | 'largePositions';

export interface HoldingFilter {
  filterType: FilterType;
  searchTerm?: string;
  gainLossMin?: number;
  gainLossMax?: number;
  marketValueMin?: number;
  marketValueMax?: number;
  sectors?: string[];
  holdingPeriod?: HoldingPeriod;
  dividendPaying?: boolean;
  esgRated?: boolean;
}
