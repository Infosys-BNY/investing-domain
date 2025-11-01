import { Client, ClientListResponse, AccountType, RiskProfile } from '../models';

export const MOCK_CLIENTS: Client[] = [
  {
    clientId: 'CLT-001',
    clientName: 'Smith, John',
    accounts: [
      {
        accountId: 'ACC-1001',
        accountType: AccountType.INDIVIDUAL,
        marketValue: 1200000,
        cashBalance: 50000,
        ytdPerformance: 5.2,
        lastActivity: new Date('2024-10-30T14:30:00Z'),
        riskProfile: RiskProfile.MODERATE
      }
    ]
  },
  {
    clientId: 'CLT-002',
    clientName: 'Johnson, Mary',
    accounts: [
      {
        accountId: 'ACC-1002',
        accountType: AccountType.JOINT,
        marketValue: 850000,
        cashBalance: 35000,
        ytdPerformance: 2.1,
        lastActivity: new Date('2024-10-28T09:15:00Z'),
        riskProfile: RiskProfile.CONSERVATIVE
      }
    ]
  },
  {
    clientId: 'CLT-003',
    clientName: 'Williams, Robert',
    accounts: [
      {
        accountId: 'ACC-1003',
        accountType: AccountType.IRA,
        marketValue: 2500000,
        cashBalance: 100000,
        ytdPerformance: 8.7,
        lastActivity: new Date('2024-10-29T16:45:00Z'),
        riskProfile: RiskProfile.AGGRESSIVE
      }
    ]
  },
  {
    clientId: 'CLT-004',
    clientName: 'Davis, Patricia',
    accounts: [
      {
        accountId: 'ACC-1004',
        accountType: AccountType.TRUST,
        marketValue: 1750000,
        cashBalance: 75000,
        ytdPerformance: 4.3,
        lastActivity: new Date('2024-10-27T11:20:00Z'),
        riskProfile: RiskProfile.MODERATE
      }
    ]
  },
  {
    clientId: 'CLT-005',
    clientName: 'Miller, James',
    accounts: [
      {
        accountId: 'ACC-1005',
        accountType: AccountType.CORPORATE,
        marketValue: 3200000,
        cashBalance: 150000,
        ytdPerformance: 6.9,
        lastActivity: new Date('2024-10-31T08:00:00Z'),
        riskProfile: RiskProfile.AGGRESSIVE
      }
    ]
  },
  {
    clientId: 'CLT-006',
    clientName: 'Wilson, Jennifer',
    accounts: [
      {
        accountId: 'ACC-1006',
        accountType: AccountType.INDIVIDUAL,
        marketValue: 450000,
        cashBalance: 20000,
        ytdPerformance: -1.2,
        lastActivity: new Date('2024-10-25T14:30:00Z'),
        riskProfile: RiskProfile.CONSERVATIVE
      }
    ]
  },
  {
    clientId: 'CLT-007',
    clientName: 'Brown, Michael',
    accounts: [
      {
        accountId: 'ACC-1007',
        accountType: AccountType.JOINT,
        marketValue: 980000,
        cashBalance: 45000,
        ytdPerformance: 3.5,
        lastActivity: new Date('2024-10-26T10:15:00Z'),
        riskProfile: RiskProfile.MODERATE
      }
    ]
  },
  {
    clientId: 'CLT-008',
    clientName: 'Taylor, Sarah',
    accounts: [
      {
        accountId: 'ACC-1008',
        accountType: AccountType.IRA,
        marketValue: 1650000,
        cashBalance: 60000,
        ytdPerformance: 7.1,
        lastActivity: new Date('2024-10-29T15:45:00Z'),
        riskProfile: RiskProfile.AGGRESSIVE
      }
    ]
  },
  {
    clientId: 'CLT-009',
    clientName: 'Anderson, David',
    accounts: [
      {
        accountId: 'ACC-1009',
        accountType: AccountType.INDIVIDUAL,
        marketValue: 725000,
        cashBalance: 30000,
        ytdPerformance: 1.8,
        lastActivity: new Date('2024-10-24T13:20:00Z'),
        riskProfile: RiskProfile.CONSERVATIVE
      }
    ]
  },
  {
    clientId: 'CLT-010',
    clientName: 'Martinez, Linda',
    accounts: [
      {
        accountId: 'ACC-1010',
        accountType: AccountType.TRUST,
        marketValue: 2100000,
        cashBalance: 90000,
        ytdPerformance: 5.6,
        lastActivity: new Date('2024-10-30T09:30:00Z'),
        riskProfile: RiskProfile.MODERATE
      }
    ]
  }
];

export const MOCK_CLIENT_LIST_RESPONSE: ClientListResponse = {
  clients: MOCK_CLIENTS,
  totalCount: MOCK_CLIENTS.length,
  pageInfo: {
    currentPage: 1,
    totalPages: 1,
    pageSize: 50
  }
};

export const MOCK_RECENT_CLIENTS: Client[] = MOCK_CLIENTS.slice(0, 5);
