import { Client } from './client.model';
import { PageInfo } from './page-info.model';

export interface ClientListResponse {
  clients: Client[];
  totalCount: number;
  pageInfo: PageInfo;
}
