import { Component, OnInit, signal, computed, inject, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCardModule } from '@angular/material/card';
import { MatSortModule, Sort } from '@angular/material/sort';
import { ClientService } from '../services/client.service';
import { Client } from '../models';

@Component({
  selector: 'app-client-selection',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatTableModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatCardModule,
    MatSortModule
  ],
  templateUrl: './client-selection.component.html',
  styleUrl: './client-selection.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [ClientService]
})
export class ClientSelectionComponent implements OnInit {
  private readonly clientService = inject(ClientService);
  private readonly router = inject(Router);

  clients = signal<Client[]>([]);
  recentClients = signal<Client[]>([]);
  loading = signal(false);
  searchTerm = signal('');
  sortColumn = signal<string>('clientName');
  sortDirection = signal<'asc' | 'desc'>('asc');

  displayedColumns = ['clientName', 'accountId', 'accountType', 'marketValue', 'cashBalance', 'ytdPerformance', 'lastActivity', 'riskProfile', 'actions'];

  filteredAndSortedClients = computed(() => {
    let filtered = this.clients();
    
    const term = this.searchTerm().toLowerCase();
    if (term) {
      filtered = filtered.filter(client => 
        client.clientName.toLowerCase().includes(term) ||
        client.accounts.some(acc => acc.accountId.toLowerCase().includes(term))
      );
    }

    const sortCol = this.sortColumn();
    const sortDir = this.sortDirection();
    
    return filtered.sort((a, b) => {
      let compareValue = 0;
      
      if (sortCol === 'clientName') {
        compareValue = a.clientName.localeCompare(b.clientName);
      } else if (a.accounts[0] && b.accounts[0]) {
        const aAccount = a.accounts[0];
        const bAccount = b.accounts[0];
        
        switch (sortCol) {
          case 'marketValue':
            compareValue = aAccount.marketValue - bAccount.marketValue;
            break;
          case 'ytdPerformance':
            compareValue = aAccount.ytdPerformance - bAccount.ytdPerformance;
            break;
          case 'lastActivity':
            compareValue = new Date(aAccount.lastActivity).getTime() - new Date(bAccount.lastActivity).getTime();
            break;
        }
      }
      
      return sortDir === 'asc' ? compareValue : -compareValue;
    });
  });

  flattenedAccounts = computed(() => {
    return this.filteredAndSortedClients().flatMap(client =>
      client.accounts.map(account => ({
        ...account,
        clientId: client.clientId,
        clientName: client.clientName
      }))
    );
  });

  ngOnInit(): void {
    this.loadClients();
    this.loadRecentClients();
  }

  loadClients(): void {
    this.loading.set(true);
    this.clientService.getClientList('advisor-123').subscribe({
      next: (response) => {
        this.clients.set(response.clients);
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Error loading clients:', error);
        this.loading.set(false);
      }
    });
  }

  loadRecentClients(): void {
    this.clientService.getRecentClients('advisor-123').subscribe({
      next: (clients) => {
        this.recentClients.set(clients.slice(0, 5));
      },
      error: (error) => {
        console.error('Error loading recent clients:', error);
      }
    });
  }

  onSearchChange(value: string): void {
    this.searchTerm.set(value);
  }

  onSort(sort: Sort): void {
    this.sortColumn.set(sort.active);
    this.sortDirection.set(sort.direction as 'asc' | 'desc' || 'asc');
  }

  viewHoldings(clientId: string, accountId: string): void {
    this.clientService.logClientAccess(clientId).subscribe();
    this.router.navigate(['/holdings', clientId, accountId]);
  }

  viewRecentClientHoldings(client: Client): void {
    if (client.accounts.length > 0) {
      const account = client.accounts[0];
      this.viewHoldings(client.clientId, account.accountId);
    }
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0
    }).format(value);
  }

  formatPercentage(value: number): string {
    return `${value >= 0 ? '+' : ''}${value.toFixed(2)}%`;
  }

  formatDate(date: Date): string {
    return new Date(date).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  }

  trackByClientId(index: number, client: Client): string {
    return client.clientId;
  }
}
