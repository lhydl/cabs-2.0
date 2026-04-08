import ErrorComponent from './error.component';
import { Routes } from '@angular/router';
import { ServiceUnavailableComponent } from 'app/service-unavailable/service-unavailable.component';

export const errorRoute: Routes = [
  {
    path: 'service-unavailable',
    component: ServiceUnavailableComponent,
    title: 'Service Unavailable',
  },
  {
    path: 'error',
    component: ErrorComponent,
    title: 'Error page!',
  },
  {
    path: 'accessdenied',
    component: ErrorComponent,
    data: {
      errorMessage: 'You are not authorized to access this page.',
    },
    title: 'Error page!',
  },
  {
    path: '404',
    component: ErrorComponent,
    data: {
      errorMessage: 'The page does not exist.',
    },
    title: 'Error page!',
  },
  {
    path: '**',
    redirectTo: '/404',
  },
];
