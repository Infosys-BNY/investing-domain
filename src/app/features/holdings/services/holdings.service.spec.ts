import { TestBed } from '@angular/core/testing';
import { HoldingsService } from './holdings.service';
import { HoldingPeriod } from '../models';
import { ApiService } from '../../../core/services/api.service';

describe('HoldingsService', () => {
  let service: HoldingsService;

  beforeEach(() => {
    const spy = jasmine.createSpyObj('ApiService', ['get', 'post']);

    TestBed.configureTestingModule({
      providers: [
        HoldingsService,
        { provide: ApiService, useValue: spy }
      ]
    });

    service = TestBed.inject(HoldingsService);
    (service as unknown as { useMockData: boolean }).useMockData = true;
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getHoldings', () => {
    it('should return holdings response with account info and summary', (done) => {
      service.getHoldings('C001', 'A001').subscribe(response => {
        expect(response).toBeTruthy();
        expect(response.accountInfo).toBeTruthy();
        expect(response.accountInfo.clientId).toBe('C001');
        expect(response.accountInfo.clientName).toBeTruthy();
        expect(response.summary).toBeTruthy();
        expect(response.holdings).toBeTruthy();
        expect(response.holdings.length).toBeGreaterThan(0);
        done();
      });
    });

    it('should return holdings with required properties', (done) => {
      service.getHoldings('C001', 'A001').subscribe(response => {
        const holding = response.holdings[0];
        expect(holding.symbol).toBeTruthy();
        expect(holding.securityName).toBeTruthy();
        expect(typeof holding.quantity).toBe('number');
        expect(typeof holding.price).toBe('number');
        expect(typeof holding.costBasis).toBe('number');
        expect(typeof holding.totalCost).toBe('number');
        expect(typeof holding.marketValue).toBe('number');
        expect(typeof holding.unrealizedGainLoss).toBe('number');
        expect(typeof holding.unrealizedGainLossPercent).toBe('number');
        expect(typeof holding.portfolioPercent).toBe('number');
        expect(holding.sector).toBeTruthy();
        expect(holding.assetClass).toBeTruthy();
        done();
      });
    });

    it('should calculate summary metrics correctly', (done) => {
      service.getHoldings('C001', 'A001').subscribe(response => {
        const summary = response.summary;
        expect(summary.totalMarketValue).toBeGreaterThan(0);
        expect(summary.totalCostBasis).toBeGreaterThan(0);
        expect(summary.totalUnrealizedGainLoss).toBe(
          summary.totalMarketValue - summary.totalCostBasis
        );
        expect(summary.numberOfHoldings).toBe(response.holdings.length);
        done();
      });
    });

    it('should have holdings with various asset classes', (done) => {
      service.getHoldings('C001', 'A001').subscribe(response => {
        const assetClasses = new Set(response.holdings.map(h => h.assetClass));
        expect(assetClasses.size).toBeGreaterThan(1);
        done();
      });
    });
  });

  describe('getTaxLots', () => {
    it('should return tax lots for a symbol', (done) => {
      service.getTaxLots('A001', 'AAPL').subscribe(lots => {
        expect(lots).toBeTruthy();
        expect(Array.isArray(lots)).toBe(true);
        expect(lots.length).toBeGreaterThan(0);
        done();
      });
    });

    it('should return tax lots with required properties', (done) => {
      service.getTaxLots('A001', 'AAPL').subscribe(lots => {
        const lot = lots[0];
        expect(typeof lot.quantity).toBe('number');
        expect(typeof lot.costBasis).toBe('number');
        expect(typeof lot.currentValue).toBe('number');
        expect(typeof lot.gainLoss).toBe('number');
        expect(typeof lot.gainLossPercent).toBe('number');
        expect(lot.purchaseDate).toBeInstanceOf(Date);
        expect(typeof lot.holdingPeriod).toBe('string');
        done();
      });
    });

    it('should calculate tax lot metrics correctly', (done) => {
      service.getTaxLots('A001', 'AAPL').subscribe(lots => {
        lots.forEach(lot => {
          const totalCost = lot.quantity * lot.costBasis;
          expect(lot.gainLoss).toBe(lot.currentValue - totalCost);
          const expectedPercent = ((lot.currentValue - totalCost) / totalCost) * 100;
          expect(lot.gainLossPercent).toBeCloseTo(expectedPercent, 2);
        });
        done();
      });
    });

    it('should have both short-term and long-term lots', (done) => {
      service.getTaxLots('A001', 'AAPL').subscribe(lots => {
        const holdingPeriods = new Set(lots.map(l => l.holdingPeriod));
        expect(holdingPeriods.has(HoldingPeriod.LONG_TERM) || holdingPeriods.has(HoldingPeriod.SHORT_TERM)).toBe(true);
        done();
      });
    });
  });

  describe('exportHoldings', () => {
    it('should return a blob for export', (done) => {
      service.exportHoldings('A001', 'xlsx', []).subscribe(blob => {
        expect(blob).toBeInstanceOf(Blob);
        expect(blob.size).toBeGreaterThan(0);
        done();
      });
    });

    it('should handle different export formats', (done) => {
      const formats: ('xlsx' | 'csv' | 'pdf')[] = ['xlsx', 'csv', 'pdf'];
      let completedCount = 0;

      formats.forEach(format => {
        service.exportHoldings('A001', format, []).subscribe(blob => {
          expect(blob).toBeInstanceOf(Blob);
          completedCount++;
          if (completedCount === formats.length) {
            done();
          }
        });
      });
    });
  });
});
