import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'clients',
    pathMatch: 'full'
  },
  {
    path: 'clients',
    loadChildren: () => import('./features/client-selection/client-selection.routes')
      .then(m => m.CLIENT_SELECTION_ROUTES)
  },
  {
    path: 'holdings/:clientId/:accountId',
    loadComponent: () => import('./features/holdings/components/holdings-view.component')
      .then(m => m.HoldingsViewComponent)
  }
];
