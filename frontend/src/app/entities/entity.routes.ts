import { Routes } from '@angular/router';

const routes: Routes = [
  {
    path: 'patient',
    data: { pageTitle: 'Patients' },
    loadChildren: () => import('./patient/patient.routes'),
  },
  {
    path: 'appointment',
    data: { pageTitle: 'Appointments' },
    loadChildren: () => import('./appointment/appointment.routes'),
  },
  /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
];

export default routes;
