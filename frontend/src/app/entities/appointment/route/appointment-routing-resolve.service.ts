import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { of, EMPTY, Observable } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IAppointment } from '../appointment.model';
import { AppointmentService } from '../service/appointment.service';

export const appointmentResolve = (route: ActivatedRouteSnapshot): Observable<null | IAppointment> => {
  const id = route.params['id'];
  if (id) {
    return inject(AppointmentService)
      .find(id)
      .pipe(
        mergeMap((appointment: HttpResponse<IAppointment>) => {
          if (appointment.body) {
            return of(appointment.body);
          } else {
            inject(Router).navigate(['404']);
            return EMPTY;
          }
        }),
      );
  }
  return of(null);
};

export default appointmentResolve;
