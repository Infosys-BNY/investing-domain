import { AccountType } from './account-type.enum';
import { RiskProfile } from './risk-profile.enum';

export interface Account {
  accountId: string;
  accountType: AccountType;
  marketValue: number;
  cashBalance: number;
  ytdPerformance: number;
  lastActivity: Date;
  riskProfile: RiskProfile;
}
