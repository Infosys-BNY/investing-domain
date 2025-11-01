import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { of, delay } from 'rxjs';
import { HoldingsViewComponent } from './holdings-view.component';
import { HoldingsService } from '../services/holdings.service';
import { HoldingsResponse, TaxLot, AssetClass, HoldingPeriod } from '../models';

describe('HoldingsViewComponent', () => {
  let component: HoldingsViewComponent;
  let fixture: ComponentFixture<HoldingsViewComponent>;
  let mockHoldingsService: jasmine.SpyObj<HoldingsService>;
  let mockRouter: jasmine.SpyObj<Router>;
  let mockActivatedRoute: {
    snapshot: {
      paramMap: {
        get: jasmine.Spy;
      };
    };
  };

  const mockHoldingsResponse: HoldingsResponse = {
    accountInfo: {
      clientId: 'C001',
      clientName: 'Test Client',
      accountId: 'A001',
      accountType: 'Individual',
      totalPortfolioValue: 1000000,
      asOfDate: new Date('2024-01-15')
    },
    summary: {
      totalMarketValue: 1000000,
      totalCostBasis: 800000,
      totalUnrealizedGainLoss: 200000,
      totalUnrealizedGainLossPercent: 25,
      numberOfHoldings: 3
    },
    holdings: [
      {
        symbol: 'AAPL',
        securityName: 'Apple Inc.',
        quantity: 100,
        price: 150,
        costBasis: 120,
        totalCost: 12000,
        marketValue: 15000,
        unrealizedGainLoss: 3000,
        unrealizedGainLossPercent: 25,
        portfolioPercent: 1.5,
        sector: 'Technology',
        assetClass: AssetClass.EQUITY
      },
      {
        symbol: 'MSFT',
        securityName: 'Microsoft Corporation',
        quantity: 50,
        price: 300,
        costBasis: 250,
        totalCost: 12500,
        marketValue: 15000,
        unrealizedGainLoss: 2500,
        unrealizedGainLossPercent: 20,
        portfolioPercent: 1.5,
        sector: 'Technology',
        assetClass: AssetClass.EQUITY
      },
      {
        symbol: 'BND',
        securityName: 'Vanguard Total Bond Market ETF',
        quantity: 200,
        price: 80,
        costBasis: 85,
        totalCost: 17000,
        marketValue: 16000,
        unrealizedGainLoss: -1000,
        unrealizedGainLossPercent: -5.88,
        portfolioPercent: 1.6,
        sector: 'Fixed Income',
        assetClass: AssetClass.FIXED_INCOME
      }
    ]
  };

  const mockTaxLots: TaxLot[] = [
    {
      purchaseDate: new Date('2023-01-15'),
      quantity: 50,
      costBasis: 120,
      currentValue: 7500,
      gainLoss: 1500,
      gainLossPercent: 25,
      holdingPeriod: HoldingPeriod.LONG_TERM
    }
  ];

  beforeEach(async () => {
    mockHoldingsService = jasmine.createSpyObj('HoldingsService', [
      'getHoldings',
      'getTaxLots',
      'exportHoldings'
    ]);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);
    mockActivatedRoute = {
      snapshot: {
        paramMap: {
          get: jasmine.createSpy('get').and.callFake((key: string) => {
            if (key === 'clientId') return 'C001';
            if (key === 'accountId') return 'A001';
            return null;
          })
        }
      }
    };

    mockHoldingsService.getHoldings.and.returnValue(of(mockHoldingsResponse).pipe(delay(100)));
    mockHoldingsService.getTaxLots.and.returnValue(of(mockTaxLots).pipe(delay(100)));
    mockHoldingsService.exportHoldings.and.returnValue(
      of(new Blob(['test'], { type: 'text/plain' })).pipe(delay(100))
    );

    await TestBed.configureTestingModule({
      imports: [HoldingsViewComponent],
      providers: [
        { provide: HoldingsService, useValue: mockHoldingsService },
        { provide: Router, useValue: mockRouter },
        { provide: ActivatedRoute, useValue: mockActivatedRoute }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(HoldingsViewComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Initialization', () => {
    it('should load holdings on init', (done) => {
      fixture.detectChanges();
      
      setTimeout(() => {
        expect(mockHoldingsService.getHoldings).toHaveBeenCalledWith('C001', 'A001');
        expect(component.holdings().length).toBe(3);
        expect(component.loading()).toBe(false);
        done();
      }, 150);
    });

    it('should set account info and summary from response', (done) => {
      fixture.detectChanges();
      
      setTimeout(() => {
        expect(component.accountInfo()).toEqual(mockHoldingsResponse.accountInfo);
        expect(component.summary()).toEqual(mockHoldingsResponse.summary);
        done();
      }, 150);
    });

    it('should handle missing route params gracefully', () => {
      mockActivatedRoute.snapshot.paramMap.get.and.returnValue(null);
      fixture.detectChanges();
      expect(mockHoldingsService.getHoldings).not.toHaveBeenCalled();
    });
  });

  describe('Filtering', () => {
    beforeEach((done) => {
      fixture.detectChanges();
      setTimeout(() => done(), 150);
    });

    it('should filter gains only', () => {
      component.applyFilter('gains');
      const filtered = component.filteredAndSortedHoldings();
      expect(filtered.length).toBe(2);
      expect(filtered.every(h => h.unrealizedGainLoss > 0)).toBe(true);
    });

    it('should filter losses only', () => {
      component.applyFilter('losses');
      const filtered = component.filteredAndSortedHoldings();
      expect(filtered.length).toBe(1);
      expect(filtered.every(h => h.unrealizedGainLoss < 0)).toBe(true);
    });

    it('should filter equities', () => {
      component.applyFilter('equities');
      const filtered = component.filteredAndSortedHoldings();
      expect(filtered.length).toBe(2);
      expect(filtered.every(h => h.assetClass === AssetClass.EQUITY)).toBe(true);
    });

    it('should filter fixed income', () => {
      component.applyFilter('fixedIncome');
      const filtered = component.filteredAndSortedHoldings();
      expect(filtered.length).toBe(1);
      expect(filtered.every(h => h.assetClass === AssetClass.FIXED_INCOME)).toBe(true);
    });

    it('should filter by search term', () => {
      component.searchTerm.set('AAPL');
      const filtered = component.filteredAndSortedHoldings();
      expect(filtered.length).toBe(1);
      expect(filtered[0].symbol).toBe('AAPL');
    });

    it('should search by security name', () => {
      component.searchTerm.set('Apple');
      const filtered = component.filteredAndSortedHoldings();
      expect(filtered.length).toBe(1);
      expect(filtered[0].securityName).toContain('Apple');
    });

    it('should be case insensitive in search', () => {
      component.searchTerm.set('aapl');
      const filtered = component.filteredAndSortedHoldings();
      expect(filtered.length).toBe(1);
    });
  });

  describe('Sorting', () => {
    beforeEach((done) => {
      fixture.detectChanges();
      setTimeout(() => done(), 150);
    });

    it('should sort by symbol ascending', () => {
      component.onSort({ active: 'symbol', direction: 'asc' });
      const sorted = component.filteredAndSortedHoldings();
      expect(sorted[0].symbol).toBe('AAPL');
      expect(sorted[2].symbol).toBe('MSFT');
    });

    it('should sort by symbol descending', () => {
      component.onSort({ active: 'symbol', direction: 'desc' });
      const sorted = component.filteredAndSortedHoldings();
      expect(sorted[0].symbol).toBe('MSFT');
      expect(sorted[2].symbol).toBe('AAPL');
    });

    it('should sort by market value', () => {
      component.onSort({ active: 'marketValue', direction: 'desc' });
      const sorted = component.filteredAndSortedHoldings();
      expect(sorted[0].marketValue).toBeGreaterThanOrEqual(sorted[1].marketValue);
    });

    it('should sort by unrealized gain/loss percent', () => {
      component.onSort({ active: 'unrealizedGainLossPercent', direction: 'desc' });
      const sorted = component.filteredAndSortedHoldings();
      expect(sorted[0].unrealizedGainLossPercent).toBe(25);
      expect(sorted[2].unrealizedGainLossPercent).toBe(-5.88);
    });
  });

  describe('Tax Lots', () => {
    beforeEach((done) => {
      fixture.detectChanges();
      setTimeout(() => done(), 150);
    });

    it('should toggle tax lots expansion', (done) => {
      component.toggleTaxLots('AAPL');
      expect(component.expandedSymbol()).toBe('AAPL');
      expect(component.loadingTaxLots()).toBe(true);

      setTimeout(() => {
        expect(mockHoldingsService.getTaxLots).toHaveBeenCalledWith('A001', 'AAPL');
        expect(component.taxLots().length).toBeGreaterThan(0);
        expect(component.loadingTaxLots()).toBe(false);
        done();
      }, 150);
    });

    it('should collapse tax lots when toggling same symbol', () => {
      component.expandedSymbol.set('AAPL');
      component.toggleTaxLots('AAPL');
      expect(component.expandedSymbol()).toBeNull();
      expect(component.taxLots().length).toBe(0);
    });

    it('should switch to different symbol tax lots', (done) => {
      component.expandedSymbol.set('AAPL');
      component.toggleTaxLots('MSFT');
      
      setTimeout(() => {
        expect(component.expandedSymbol()).toBe('MSFT');
        expect(mockHoldingsService.getTaxLots).toHaveBeenCalledWith('A001', 'MSFT');
        done();
      }, 150);
    });
  });

  describe('Export', () => {
    beforeEach((done) => {
      fixture.detectChanges();
      setTimeout(() => done(), 150);
    });

    it('should export holdings to xlsx', (done) => {
      spyOn(window.URL, 'createObjectURL').and.returnValue('blob:test');
      spyOn(window.URL, 'revokeObjectURL');
      
      component.exportHoldings('xlsx');
      expect(component.exporting()).toBe(true);

      setTimeout(() => {
        expect(mockHoldingsService.exportHoldings).toHaveBeenCalledWith(
          'A001',
          'xlsx',
          component.filteredAndSortedHoldings()
        );
        expect(component.exporting()).toBe(false);
        done();
      }, 150);
    });

    it('should not export if no account info', () => {
      component.accountInfo.set(null);
      component.exportHoldings('csv');
      expect(mockHoldingsService.exportHoldings).not.toHaveBeenCalled();
    });
  });

  describe('Navigation', () => {
    it('should navigate back to clients', () => {
      component.navigateBack();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/clients']);
    });
  });

  describe('Computed Properties', () => {
    beforeEach((done) => {
      fixture.detectChanges();
      setTimeout(() => done(), 150);
    });

    it('should calculate top gainers', () => {
      const topGainers = component.topGainers();
      expect(topGainers.length).toBeLessThanOrEqual(3);
      expect(topGainers[0].unrealizedGainLossPercent).toBeGreaterThanOrEqual(
        topGainers[topGainers.length - 1].unrealizedGainLossPercent
      );
    });

    it('should calculate top losers', () => {
      const topLosers = component.topLosers();
      expect(topLosers.length).toBeLessThanOrEqual(3);
      expect(topLosers[0].unrealizedGainLossPercent).toBeLessThanOrEqual(
        topLosers[topLosers.length - 1].unrealizedGainLossPercent
      );
    });

    it('should identify concentrated positions', () => {
      const concentrated = component.concentratedPositions();
      expect(concentrated.every(h => h.portfolioPercent > 10)).toBe(true);
    });
  });

  describe('Formatting', () => {
    it('should format currency', () => {
      expect(component.formatCurrency(1000)).toBe('$1,000.00');
      expect(component.formatCurrency(1234567.89)).toBe('$1,234,567.89');
    });

    it('should format currency with sign', () => {
      expect(component.formatCurrencyWithSign(1000)).toBe('+$1,000.00');
      expect(component.formatCurrencyWithSign(-1000)).toBe('-$1,000.00');
    });

    it('should format percentage', () => {
      expect(component.formatPercentage(25)).toBe('25.00%');
      expect(component.formatPercentage(-5.88)).toBe('-5.88%');
    });

    it('should format number', () => {
      expect(component.formatNumber(1234.5678)).toBe('1,234.57');
    });

    it('should return correct gain/loss class', () => {
      expect(component.getGainLossClass(100)).toBe('positive-performance');
      expect(component.getGainLossClass(-100)).toBe('negative-performance');
      expect(component.getGainLossClass(0)).toBe('neutral-performance');
    });
  });

  describe('Cleanup', () => {
    it('should stop price updates on destroy', () => {
      fixture.detectChanges();
      spyOn(window, 'clearInterval');
      component.ngOnDestroy();
      expect(clearInterval).toHaveBeenCalled();
    });
  });
});
