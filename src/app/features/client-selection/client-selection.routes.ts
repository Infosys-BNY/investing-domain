import { Routes } from '@angular/router';

export const CLIENT_SELECTION_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./components/client-selection.component')
      .then(m => m.ClientSelectionComponent)
  }
];
