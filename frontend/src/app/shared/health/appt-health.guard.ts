import { HttpErrorResponse } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { CanActivate, Router } from "@angular/router";
import { AppointmentService } from "app/entities/appointment/service/appointment.service";
import { catchError, map, Observable, of } from "rxjs";

@Injectable({
    providedIn: 'root'
})
export class ApptHealthGuard implements CanActivate {

    constructor(
        private appointmentService: AppointmentService,
        private router: Router
    ) { }

    canActivate(): Observable<boolean> {
        return this.appointmentService.getHealth().pipe(
            map(() => true), // service is healthy
            catchError((err: HttpErrorResponse) => {
                if (err.status === 503) {
                    this.router.navigate(['/service-unavailable'], { replaceUrl: true });
                }
                return of(false);
            })
        );
    }
}