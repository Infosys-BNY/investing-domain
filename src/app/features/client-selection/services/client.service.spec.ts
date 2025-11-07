import { TestBed } from '@angular/core/testing';
import { ClientService } from './client.service';
import { ApiService } from '../../../core/services/api.service';
import { of } from 'rxjs';
import { MOCK_CLIENT_LIST_RESPONSE } from './mock-client-data';

describe('ClientService', () => {
  let service: ClientService;
  let apiServiceSpy: jasmine.SpyObj<ApiService>;

  beforeEach(() => {
    const spy = jasmine.createSpyObj('ApiService', ['get', 'post']);

    TestBed.configureTestingModule({
      providers: [
        ClientService,
        { provide: ApiService, useValue: spy }
      ]
    });

    service = TestBed.inject(ClientService);
    apiServiceSpy = TestBed.inject(ApiService) as jasmine.SpyObj<ApiService>;
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('searchClients', () => {
    beforeEach(() => {
      (service as unknown as { useMockData: boolean }).useMockData = true;
    });

    it('should return filtered clients from mock data when useMockData is true', (done) => {
      const advisorId = 'advisor-123';
      const searchQuery = 'Smith';
      
      service.searchClients(advisorId, searchQuery).subscribe(clients => {
        expect(clients.length).toBeGreaterThan(0);
        expect(clients[0].clientName).toContain('Smith');
        done();
      });
    });

    it('should filter by account ID when searching', (done) => {
      const advisorId = 'advisor-123';
      const searchQuery = 'ACC-1001';
      
      service.searchClients(advisorId, searchQuery).subscribe(clients => {
        expect(clients.length).toBeGreaterThan(0);
        expect(clients[0].accounts[0].accountId).toBe('ACC-1001');
        done();
      });
    });

    it('should return empty array when no matches found', (done) => {
      const advisorId = 'advisor-123';
      const searchQuery = 'NonExistentClient12345';
      
      service.searchClients(advisorId, searchQuery).subscribe(clients => {
        expect(clients.length).toBe(0);
        done();
      });
    });

    it('should be case insensitive when searching', (done) => {
      const advisorId = 'advisor-123';
      const searchQuery = 'smith';
      
      service.searchClients(advisorId, searchQuery).subscribe(clients => {
        expect(clients.length).toBeGreaterThan(0);
        expect(clients[0].clientName.toLowerCase()).toContain('smith');
        done();
      });
    });
  });

  describe('getClientList', () => {
    beforeEach(() => {
      (service as unknown as { useMockData: boolean }).useMockData = true;
    });

    it('should return client list response from mock data', (done) => {
      const advisorId = 'advisor-123';
      
      service.getClientList(advisorId).subscribe(response => {
        expect(response).toBeTruthy();
        expect(response.clients).toBeDefined();
        expect(response.clients.length).toBe(MOCK_CLIENT_LIST_RESPONSE.clients.length);
        expect(response.totalCount).toBe(MOCK_CLIENT_LIST_RESPONSE.totalCount);
        expect(response.pageInfo).toBeDefined();
        done();
      });
    });

    it('should use default page and size parameters', (done) => {
      const advisorId = 'advisor-123';
      
      service.getClientList(advisorId).subscribe(response => {
        expect(response.pageInfo.currentPage).toBeDefined();
        expect(response.pageInfo.pageSize).toBeDefined();
        done();
      });
    });

    it('should accept custom page and pageSize parameters', (done) => {
      const advisorId = 'advisor-123';
      
      service.getClientList(advisorId, 2, 25).subscribe(response => {
        expect(response).toBeTruthy();
        done();
      });
    });
  });

  describe('getRecentClients', () => {
    it('should return recent clients by calling getClientList', (done) => {
      const advisorId = 'advisor-123';
      const mockResponse = { 
        clients: MOCK_CLIENT_LIST_RESPONSE.clients.slice(0, 5),
        totalCount: 100,
        pageInfo: { currentPage: 0, totalPages: 20, pageSize: 5 }
      };
      
      spyOn(service, 'getClientList').and.returnValue(of(mockResponse));
      
      service.getRecentClients(advisorId).subscribe(clients => {
        expect(service.getClientList).toHaveBeenCalledWith(advisorId, 0, 5);
        expect(clients.length).toBeLessThanOrEqual(5);
        done();
      });
    });

    it('should return array of Client objects', (done) => {
      const advisorId = 'advisor-123';
      const mockResponse = { 
        clients: MOCK_CLIENT_LIST_RESPONSE.clients.slice(0, 5),
        totalCount: 100,
        pageInfo: { currentPage: 0, totalPages: 20, pageSize: 5 }
      };
      
      spyOn(service, 'getClientList').and.returnValue(of(mockResponse));
      
      service.getRecentClients(advisorId).subscribe(clients => {
        clients.forEach(client => {
          expect(client.clientId).toBeDefined();
          expect(client.clientName).toBeDefined();
          expect(client.accounts).toBeDefined();
          expect(Array.isArray(client.accounts)).toBe(true);
        });
        done();
      });
    });
  });

  describe('logClientAccess', () => {
    beforeEach(() => {
      (service as unknown as { useMockData: boolean }).useMockData = true;
    });

    it('should complete successfully with mock data', (done) => {
      const clientId = 'CLT-001';
      
      service.logClientAccess(clientId).subscribe({
        next: () => {
          expect(true).toBe(true);
          done();
        },
        error: () => {
          fail('Should not error');
        }
      });
    });

    it('should handle multiple calls', (done) => {
      const clientId1 = 'CLT-001';
      const clientId2 = 'CLT-002';
      
      let callCount = 0;
      
      service.logClientAccess(clientId1).subscribe(() => {
        callCount++;
        if (callCount === 2) {
          expect(callCount).toBe(2);
          done();
        }
      });
      
      service.logClientAccess(clientId2).subscribe(() => {
        callCount++;
        if (callCount === 2) {
          expect(callCount).toBe(2);
          done();
        }
      });
    });
  });

  describe('with useMockData = false', () => {
    beforeEach(() => {
      (service as unknown as { useMockData: boolean }).useMockData = false;
    });

    it('should call API service for searchClients', (done) => {
      const advisorId = 'advisor-123';
      const query = 'test';
      const mockResponse = { 
        content: MOCK_CLIENT_LIST_RESPONSE.clients, 
        totalElements: 10, 
        page: 0, 
        size: 50,
        totalPages: 1
      };
      apiServiceSpy.post.and.returnValue(of(mockResponse));

      service.searchClients(advisorId, query).subscribe(clients => {
        expect(apiServiceSpy.post).toHaveBeenCalledWith('clients/search', {
          advisorId,
          clientName: query,
          page: 0,
          size: 50
        });
        expect(clients).toEqual(mockResponse.content);
        done();
      });
    });

    it('should call API service for getClientList', (done) => {
      const advisorId = 'advisor-123';
      const mockResponse = {
        content: MOCK_CLIENT_LIST_RESPONSE.clients,
        totalElements: MOCK_CLIENT_LIST_RESPONSE.totalCount,
        page: 0,
        size: 50,
        totalPages: 1
      };
      apiServiceSpy.get.and.returnValue(of(mockResponse));

      service.getClientList(advisorId).subscribe(response => {
        expect(apiServiceSpy.get).toHaveBeenCalledWith(`advisor/${advisorId}/clients`, { page: 0, size: 50 });
        expect(response.clients).toEqual(mockResponse.content);
        expect(response.totalCount).toEqual(mockResponse.totalElements);
        done();
      });
    });

    it('should call API service for getRecentClients', (done) => {
      const advisorId = 'advisor-123';
      const mockResponse = {
        content: MOCK_CLIENT_LIST_RESPONSE.clients.slice(0, 5),
        totalElements: 100,
        page: 0,
        size: 5,
        totalPages: 20
      };
      apiServiceSpy.get.and.returnValue(of(mockResponse));

      service.getRecentClients(advisorId).subscribe(clients => {
        expect(apiServiceSpy.get).toHaveBeenCalledWith(`advisor/${advisorId}/clients`, { page: 0, size: 5 });
        expect(clients.length).toBeLessThanOrEqual(5);
        done();
      });
    });

    it('should call API service for logClientAccess', (done) => {
      const clientId = 'CLT-001';
      apiServiceSpy.post.and.returnValue(of(void 0));

      service.logClientAccess(clientId).subscribe(() => {
        expect(apiServiceSpy.post).toHaveBeenCalled();
        const callArgs = apiServiceSpy.post.calls.mostRecent().args;
        expect(callArgs[0]).toBe('audit/client-access');
        expect(callArgs[1]).toEqual(jasmine.objectContaining({ clientId }));
        done();
      });
    });
  });
});
