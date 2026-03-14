import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import { AppointmentComponent } from './list/appointment.component';
import { AppointmentDetailComponent } from './detail/appointment-detail.component';
import { AppointmentUpdateComponent } from './update/appointment-update.component';
import AppointmentResolve from './route/appointment-routing-resolve.service';

const appointmentRoute: Routes = [
  {
    path: '',
    component: AppointmentComponent,
    data: {
      defaultSort: 'id,' + ASC,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: AppointmentDetailComponent,
    resolve: {
      appointment: AppointmentResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: AppointmentUpdateComponent,
    resolve: {
      appointment: AppointmentResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: AppointmentUpdateComponent,
    resolve: {
      appointment: AppointmentResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default appointmentRoute;
