import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ClientSelectionComponent } from './client-selection.component';
import { ClientService } from '../services/client.service';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { MOCK_CLIENT_LIST_RESPONSE, MOCK_RECENT_CLIENTS } from '../services/mock-client-data';
import { Sort } from '@angular/material/sort';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

describe('ClientSelectionComponent', () => {
  let component: ClientSelectionComponent;
  let fixture: ComponentFixture<ClientSelectionComponent>;
  let clientServiceSpy: jasmine.SpyObj<ClientService>;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    const clientSpy = jasmine.createSpyObj('ClientService', ['getClientList', 'getRecentClients', 'logClientAccess']);
    const routerSpyObj = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [ClientSelectionComponent, NoopAnimationsModule],
      providers: [
        { provide: ClientService, useValue: clientSpy },
        { provide: Router, useValue: routerSpyObj }
      ]
    }).compileComponents();

    clientServiceSpy = TestBed.inject(ClientService) as jasmine.SpyObj<ClientService>;
    routerSpy = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    
    clientServiceSpy.getClientList.and.returnValue(of(MOCK_CLIENT_LIST_RESPONSE));
    clientServiceSpy.getRecentClients.and.returnValue(of(MOCK_RECENT_CLIENTS));
    clientServiceSpy.logClientAccess.and.returnValue(of(void 0));

    fixture = TestBed.createComponent(ClientSelectionComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('ngOnInit', () => {
    it('should load clients on initialization', () => {
      fixture.detectChanges();
      
      expect(clientServiceSpy.getClientList).toHaveBeenCalledWith('advisor-123');
      expect(component.clients().length).toBe(MOCK_CLIENT_LIST_RESPONSE.clients.length);
    });

    it('should load recent clients on initialization', () => {
      fixture.detectChanges();
      
      expect(clientServiceSpy.getRecentClients).toHaveBeenCalledWith('advisor-123');
      expect(component.recentClients().length).toBe(5);
    });

    it('should set loading state during data fetch', () => {
      expect(component.loading()).toBe(false);
      
      component.ngOnInit();
      
      expect(component.loading()).toBe(true);
    });
  });

  describe('Search functionality', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should update searchTerm signal when onSearchChange is called', () => {
      const searchValue = 'Smith';
      
      component.onSearchChange(searchValue);
      
      expect(component.searchTerm()).toBe(searchValue);
    });

    it('should filter clients by name', () => {
      component.onSearchChange('Smith');
      
      const filtered = component.filteredAndSortedClients();
      
      expect(filtered.length).toBeGreaterThan(0);
      expect(filtered[0].clientName).toContain('Smith');
    });

    it('should filter clients by account ID', () => {
      component.onSearchChange('ACC-1001');
      
      const filtered = component.filteredAndSortedClients();
      
      expect(filtered.length).toBeGreaterThan(0);
      expect(filtered[0].accounts[0].accountId).toBe('ACC-1001');
    });

    it('should be case insensitive', () => {
      component.onSearchChange('smith');
      
      const filtered = component.filteredAndSortedClients();
      
      expect(filtered.length).toBeGreaterThan(0);
    });

    it('should return empty array when no matches', () => {
      component.onSearchChange('NonExistentClient12345');
      
      const filtered = component.filteredAndSortedClients();
      
      expect(filtered.length).toBe(0);
    });

    it('should clear search when empty string is provided', () => {
      component.onSearchChange('Smith');
      expect(component.filteredAndSortedClients().length).toBeLessThan(component.clients().length);
      
      component.onSearchChange('');
      
      expect(component.searchTerm()).toBe('');
      expect(component.filteredAndSortedClients().length).toBe(component.clients().length);
    });
  });

  describe('Sorting functionality', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should sort by client name ascending', () => {
      const sort: Sort = { active: 'clientName', direction: 'asc' };
      
      component.onSort(sort);
      
      const sorted = component.filteredAndSortedClients();
      expect(sorted[0].clientName.localeCompare(sorted[1].clientName)).toBeLessThanOrEqual(0);
    });

    it('should sort by client name descending', () => {
      const sort: Sort = { active: 'clientName', direction: 'desc' };
      
      component.onSort(sort);
      
      const sorted = component.filteredAndSortedClients();
      expect(sorted[0].clientName.localeCompare(sorted[1].clientName)).toBeGreaterThanOrEqual(0);
    });

    it('should sort by market value ascending', () => {
      const sort: Sort = { active: 'marketValue', direction: 'asc' };
      
      component.onSort(sort);
      
      const sorted = component.filteredAndSortedClients();
      expect(sorted[0].accounts[0].marketValue).toBeLessThanOrEqual(sorted[1].accounts[0].marketValue);
    });

    it('should sort by market value descending', () => {
      const sort: Sort = { active: 'marketValue', direction: 'desc' };
      
      component.onSort(sort);
      
      const sorted = component.filteredAndSortedClients();
      expect(sorted[0].accounts[0].marketValue).toBeGreaterThanOrEqual(sorted[1].accounts[0].marketValue);
    });

    it('should sort by YTD performance', () => {
      const sort: Sort = { active: 'ytdPerformance', direction: 'asc' };
      
      component.onSort(sort);
      
      const sorted = component.filteredAndSortedClients();
      expect(sorted[0].accounts[0].ytdPerformance).toBeLessThanOrEqual(sorted[1].accounts[0].ytdPerformance);
    });

    it('should sort by last activity date', () => {
      const sort: Sort = { active: 'lastActivity', direction: 'asc' };
      
      component.onSort(sort);
      
      const sorted = component.filteredAndSortedClients();
      const date1 = new Date(sorted[0].accounts[0].lastActivity).getTime();
      const date2 = new Date(sorted[1].accounts[0].lastActivity).getTime();
      expect(date1).toBeLessThanOrEqual(date2);
    });

    it('should update sortColumn and sortDirection signals', () => {
      const sort: Sort = { active: 'marketValue', direction: 'desc' };
      
      component.onSort(sort);
      
      expect(component.sortColumn()).toBe('marketValue');
      expect(component.sortDirection()).toBe('desc');
    });
  });

  describe('Navigation', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should navigate to holdings when viewHoldings is called', () => {
      const clientId = 'CLT-001';
      const accountId = 'ACC-1001';
      
      component.viewHoldings(clientId, accountId);
      
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/holdings', clientId, accountId]);
    });

    it('should log client access when viewHoldings is called', () => {
      const clientId = 'CLT-001';
      const accountId = 'ACC-1001';
      
      component.viewHoldings(clientId, accountId);
      
      expect(clientServiceSpy.logClientAccess).toHaveBeenCalledWith(clientId);
    });

    it('should navigate to holdings for recent client', () => {
      const client = MOCK_RECENT_CLIENTS[0];
      
      component.viewRecentClientHoldings(client);
      
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/holdings', client.clientId, client.accounts[0].accountId]);
      expect(clientServiceSpy.logClientAccess).toHaveBeenCalledWith(client.clientId);
    });

    it('should not navigate if client has no accounts', () => {
      const clientWithNoAccounts = { ...MOCK_RECENT_CLIENTS[0], accounts: [] };
      
      component.viewRecentClientHoldings(clientWithNoAccounts);
      
      expect(routerSpy.navigate).not.toHaveBeenCalled();
    });
  });

  describe('Formatting functions', () => {
    it('should format currency correctly', () => {
      const result = component.formatCurrency(1234567);
      
      expect(result).toBe('$1,234,567');
    });

    it('should format percentage correctly with positive value', () => {
      const result = component.formatPercentage(5.25);
      
      expect(result).toBe('+5.25%');
    });

    it('should format percentage correctly with negative value', () => {
      const result = component.formatPercentage(-2.5);
      
      expect(result).toBe('-2.50%');
    });

    it('should format date correctly', () => {
      const date = new Date('2024-10-30');
      const result = component.formatDate(date);
      
      expect(result).toContain('Oct');
      expect(result).toContain('30');
      expect(result).toContain('2024');
    });
  });

  describe('flattenedAccounts computed signal', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should flatten all client accounts', () => {
      const flattened = component.flattenedAccounts();
      
      expect(flattened.length).toBe(MOCK_CLIENT_LIST_RESPONSE.clients.length);
      flattened.forEach(account => {
        expect(account.clientId).toBeDefined();
        expect(account.clientName).toBeDefined();
        expect(account.accountId).toBeDefined();
      });
    });

    it('should include client information in flattened accounts', () => {
      const flattened = component.flattenedAccounts();
      const firstAccount = flattened[0];
      
      expect(firstAccount.clientId).toBe(MOCK_CLIENT_LIST_RESPONSE.clients[0].clientId);
      expect(firstAccount.clientName).toBe(MOCK_CLIENT_LIST_RESPONSE.clients[0].clientName);
    });
  });

  describe('Error handling', () => {
    it('should handle error when loading clients fails', () => {
      clientServiceSpy.getClientList.and.returnValue(throwError(() => new Error('API Error')));
      
      component.loadClients();
      
      expect(component.loading()).toBe(false);
    });

    it('should handle error when loading recent clients fails', () => {
      clientServiceSpy.getRecentClients.and.returnValue(throwError(() => new Error('API Error')));
      
      component.loadRecentClients();
      
      expect(component.recentClients().length).toBe(0);
    });
  });

  describe('trackByClientId', () => {
    it('should return clientId for tracking', () => {
      const client = MOCK_RECENT_CLIENTS[0];
      
      const result = component.trackByClientId(0, client);
      
      expect(result).toBe(client.clientId);
    });
  });
});
