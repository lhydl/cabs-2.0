import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot } from '@angular/router';
import { Observable, catchError, map, of } from 'rxjs';

import { AppointmentService } from 'app/entities/appointment/service/appointment.service';
import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class ApptHealthGuard implements CanActivate {
  constructor(
    private appointmentService: AppointmentService,
    private router: Router,
  ) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> {
    return this.appointmentService.getHealth().pipe(
      map(() => true),
      catchError((err: HttpErrorResponse) => {
        if (err.status === 503 || err.status === 504) {
          this.router.navigate(['/service-unavailable'], {
            queryParams: { returnUrl: state.url },
            replaceUrl: true,
          });
        }
        return of(false);
      }),
    );
  }
}
