import { Component, OnInit, OnDestroy, computed, signal, ChangeDetectionStrategy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatMenuModule } from '@angular/material/menu';
import { MatChipsModule } from '@angular/material/chips';
import { FormsModule } from '@angular/forms';
import { HoldingsService } from '../services';
import { 
  Holding, 
  HoldingsResponse, 
  FilterType, 
  TaxLot,
  AccountInfo,
  PortfolioSummary 
} from '../models';

@Component({
  selector: 'app-holdings-view',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatTableModule,
    MatSortModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatMenuModule,
    MatChipsModule
  ],
  templateUrl: './holdings-view.component.html',
  styleUrl: './holdings-view.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class HoldingsViewComponent implements OnInit, OnDestroy {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly holdingsService = inject(HoldingsService);

  loading = signal<boolean>(true);
  accountInfo = signal<AccountInfo | null>(null);
  summary = signal<PortfolioSummary | null>(null);
  holdings = signal<Holding[]>([]);
  searchTerm = signal<string>('');
  activeFilter = signal<FilterType>('all');
  sortColumn = signal<string>('symbol');
  sortDirection = signal<'asc' | 'desc'>('asc');
  expandedSymbol = signal<string | null>(null);
  taxLots = signal<TaxLot[]>([]);
  loadingTaxLots = signal<boolean>(false);
  exporting = signal<boolean>(false);
  lastUpdated = signal<Date>(new Date());
  
  private priceUpdateInterval?: ReturnType<typeof setInterval>;
  private clientId?: string;
  private accountId?: string;

  displayedColumns: string[] = [
    'symbol',
    'securityName',
    'quantity',
    'price',
    'costBasis',
    'totalCost',
    'marketValue',
    'unrealizedGainLoss',
    'unrealizedGainLossPercent',
    'portfolioPercent',
    'sector',
    'assetClass'
  ];

  filteredAndSortedHoldings = computed(() => {
    let filtered = this.holdings();
    const filter = this.activeFilter();
    const searchTerm = this.searchTerm().toLowerCase();

    if (searchTerm) {
      filtered = filtered.filter(h => 
        h.symbol.toLowerCase().includes(searchTerm) ||
        h.securityName.toLowerCase().includes(searchTerm)
      );
    }

    switch (filter) {
      case 'gains':
        filtered = filtered.filter(h => h.unrealizedGainLoss > 0);
        break;
      case 'losses':
        filtered = filtered.filter(h => h.unrealizedGainLoss < 0);
        break;
      case 'equities':
        filtered = filtered.filter(h => h.assetClass === 'Equity');
        break;
      case 'fixedIncome':
        filtered = filtered.filter(h => h.assetClass === 'Fixed Income');
        break;
      case 'alternatives':
        filtered = filtered.filter(h => h.assetClass === 'Alternative');
        break;
      case 'largePositions':
        filtered = filtered.filter(h => h.portfolioPercent > 5);
        break;
    }

    const column = this.sortColumn();
    const direction = this.sortDirection();

    return filtered.sort((a, b) => {
      const aValue = this.getPropertyValue(a, column);
      const bValue = this.getPropertyValue(b, column);

      if (aValue === bValue) return 0;
      
      const comparison = (aValue as number | string) < (bValue as number | string) ? -1 : 1;
      return direction === 'asc' ? comparison : -comparison;
    });
  });

  topGainers = computed(() => {
    return [...this.holdings()]
      .sort((a, b) => b.unrealizedGainLossPercent - a.unrealizedGainLossPercent)
      .slice(0, 3);
  });

  topLosers = computed(() => {
    return [...this.holdings()]
      .sort((a, b) => a.unrealizedGainLossPercent - b.unrealizedGainLossPercent)
      .slice(0, 3);
  });

  concentratedPositions = computed(() => {
    return this.holdings().filter(h => h.portfolioPercent > 10);
  });

  ngOnInit(): void {
    this.clientId = this.route.snapshot.paramMap.get('clientId') || undefined;
    this.accountId = this.route.snapshot.paramMap.get('accountId') || undefined;

    if (this.clientId && this.accountId) {
      this.loadHoldings(this.clientId, this.accountId);
      this.startPriceUpdates();
    }
  }

  ngOnDestroy(): void {
    this.stopPriceUpdates();
  }

  loadHoldings(clientId: string, accountId: string): void {
    this.loading.set(true);
    this.holdingsService.getHoldings(clientId, accountId).subscribe({
      next: (response: HoldingsResponse) => {
        this.accountInfo.set(response.accountInfo);
        this.summary.set(response.summary);
        this.holdings.set(response.holdings);
        this.lastUpdated.set(new Date());
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Error loading holdings:', error);
        this.loading.set(false);
      }
    });
  }

  onSort(sort: Sort): void {
    this.sortColumn.set(sort.active);
    this.sortDirection.set(sort.direction as 'asc' | 'desc' || 'asc');
  }

  onFilterChange(event: { value: FilterType }): void {
    this.activeFilter.set(event.value);
  }

  applyFilter(filter: FilterType): void {
    this.activeFilter.set(filter);
  }

  toggleTaxLots(symbol: string): void {
    const expanded = this.expandedSymbol();
    
    if (expanded === symbol) {
      this.expandedSymbol.set(null);
      this.taxLots.set([]);
    } else {
      this.expandedSymbol.set(symbol);
      this.loadTaxLots(symbol);
    }
  }

  loadTaxLots(symbol: string): void {
    const accountId = this.accountInfo()?.accountId;
    if (!accountId) return;

    this.loadingTaxLots.set(true);
    this.holdingsService.getTaxLots(accountId, symbol).subscribe({
      next: (lots: TaxLot[]) => {
        this.taxLots.set(lots);
        this.loadingTaxLots.set(false);
      },
      error: (error) => {
        console.error('Error loading tax lots:', error);
        this.loadingTaxLots.set(false);
      }
    });
  }

  exportHoldings(format: 'xlsx' | 'csv' | 'pdf'): void {
    const accountId = this.accountInfo()?.accountId;
    if (!accountId) return;

    this.exporting.set(true);
    this.holdingsService.exportHoldings(accountId, format, this.filteredAndSortedHoldings()).subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `holdings-${accountId}.${format}`;
        link.click();
        window.URL.revokeObjectURL(url);
        this.exporting.set(false);
      },
      error: (error) => {
        console.error('Error exporting holdings:', error);
        this.exporting.set(false);
      }
    });
  }

  navigateBack(): void {
    this.router.navigate(['/clients']);
  }

  refreshPrices(): void {
    if (this.clientId && this.accountId) {
      this.loadHoldings(this.clientId, this.accountId);
    }
  }

  private startPriceUpdates(): void {
    this.priceUpdateInterval = setInterval(() => {
      if (this.isMarketHours() && this.clientId && this.accountId) {
        this.loadHoldings(this.clientId, this.accountId);
      }
    }, 15000);
  }

  private stopPriceUpdates(): void {
    if (this.priceUpdateInterval) {
      clearInterval(this.priceUpdateInterval);
    }
  }

  private isMarketHours(): boolean {
    const now = new Date();
    const dayOfWeek = now.getDay();
    const hours = now.getHours();
    const minutes = now.getMinutes();
    const currentTime = hours * 60 + minutes;
    
    const isWeekday = dayOfWeek >= 1 && dayOfWeek <= 5;
    const marketOpen = 9 * 60 + 30;
    const marketClose = 16 * 60;
    
    return isWeekday && currentTime >= marketOpen && currentTime < marketClose;
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }).format(value);
  }

  formatCurrencyWithSign(value: number): string {
    const formatted = this.formatCurrency(Math.abs(value));
    return value >= 0 ? `+${formatted}` : `-${formatted}`;
  }

  formatPercentage(value: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'percent',
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }).format(value / 100);
  }

  formatNumber(value: number): string {
    return new Intl.NumberFormat('en-US', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }).format(value);
  }

  formatDate(date: Date): string {
    return new Intl.DateTimeFormat('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    }).format(new Date(date));
  }

  getGainLossClass(value: number): string {
    if (value > 0) return 'positive-performance';
    if (value < 0) return 'negative-performance';
    return 'neutral-performance';
  }

  isTopGainer(symbol: string): boolean {
    return this.topGainers().some(h => h.symbol === symbol);
  }

  isTopLoser(symbol: string): boolean {
    return this.topLosers().some(h => h.symbol === symbol);
  }

  isConcentratedPosition(portfolioPercent: number): boolean {
    return portfolioPercent > 10;
  }

  private getPropertyValue(obj: unknown, path: string): unknown {
    return path.split('.').reduce((o: unknown, p: string) => (o as Record<string, unknown>)?.[p], obj);
  }
}
