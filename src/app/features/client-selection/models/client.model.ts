import { Account } from './account.model';

export interface Client {
  clientId: string;
  clientName: string;
  accounts: Account[];
}
