import { ASC } from 'app/config/navigation.constants';
import { AppointmentComponent } from './list/appointment.component';
import { AppointmentDetailComponent } from './detail/appointment-detail.component';
import AppointmentResolve from './route/appointment-routing-resolve.service';
import { AppointmentUpdateComponent } from './update/appointment-update.component';
import { Routes } from '@angular/router';
import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ApptHealthGuard } from 'app/shared/health/appt-health.guard';

const appointmentRoute: Routes = [
  {
    path: '',
    component: AppointmentComponent,
    data: {
      defaultSort: 'id,' + ASC,
    },
    canActivate: [UserRouteAccessService, ApptHealthGuard],
  },
  {
    path: ':id/view',
    component: AppointmentDetailComponent,
    resolve: {
      appointment: AppointmentResolve,
    },
    canActivate: [UserRouteAccessService, ApptHealthGuard],
  },
  {
    path: 'new',
    component: AppointmentUpdateComponent,
    resolve: {
      appointment: AppointmentResolve,
    },
    canActivate: [UserRouteAccessService, ApptHealthGuard],
  },
  {
    path: ':id/edit',
    component: AppointmentUpdateComponent,
    resolve: {
      appointment: AppointmentResolve,
    },
    canActivate: [UserRouteAccessService, ApptHealthGuard],
  },
];

export default appointmentRoute;
