import { Injectable, signal, computed } from '@angular/core';
import { Observable, of } from 'rxjs';
import { User } from '../models/user.model';
import { LoginCredentials } from '../models/login-credentials.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private userSignal = signal<User | null>(null);
  public user = this.userSignal.asReadonly();
  public isAuthenticated = computed(() => !!this.userSignal());
  
  login(credentials: LoginCredentials): Observable<User> {
    return of({
      id: '1',
      email: credentials.email,
      name: 'Mock User',
      role: 'advisor'
    });
  }
  
  logout(): void {
    this.userSignal.set(null);
  }
  
  setUser(user: User): void {
    this.userSignal.set(user);
  }
}
