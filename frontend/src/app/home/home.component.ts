import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { AccountService } from 'app/core/auth/account.service';
import { Account } from 'app/core/auth/account.model';
import { IAppointment } from 'app/entities/appointment/appointment.model';
import { AppointmentService, EntityArrayResponseType } from 'app/entities/appointment/service/appointment.service';
import dayjs from 'dayjs';
import HasAnyAuthorityDirective from 'app/shared/auth/has-any-authority.directive';

import { MatDividerModule } from '@angular/material/divider';
import { HttpParams } from '@angular/common/http';

@Component({
  standalone: true,
  selector: 'jhi-home',
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss',
  imports: [SharedModule, RouterModule, MatDividerModule, HasAnyAuthorityDirective],
})
export default class HomeComponent implements OnInit, OnDestroy {
  account: Account | null = null;
  isAdmin: boolean = false;
  isUser: boolean = false;
  appointments?: IAppointment[] = [];
  userTodaysAppointments?: IAppointment[] = [];
  currentAppointment?: IAppointment;
  nextAppointment?: IAppointment;
  numPeopleInFront?: number;
  lastUpdatedTime?: string;
  today = dayjs().format('DD MMM YYYY');
  userQueueNum?: number;
  updateAppointmentsIntervalId: any;

  private readonly destroy$ = new Subject<void>();

  constructor(
    private accountService: AccountService,
    protected appointmentService: AppointmentService,
    private router: Router,
  ) {}

  // NOTE: USE *jhiHasAnyAuthority in HTML! I'VE REMOVED USER ROLE FOR admin
  ngOnInit(): void {
    this.accountService
      .getAuthenticationState()
      .pipe(takeUntil(this.destroy$))
      .subscribe(account => {
        this.account = account;
        this.isAdmin = this.accountService.hasAnyAuthority('ROLE_ADMIN');
        this.isUser = this.accountService.hasAnyAuthority('ROLE_USER');

        if (this.account) {
          this.getTodaysAppointments();
          if (this.isUser) {
            // Real-time queue update for users, polling every 5 seconds
            this.updateAppointmentsIntervalId = setInterval(() => {
              this.getTodaysAppointments();
            }, 5000);
          }
        } else {
          if (this.updateAppointmentsIntervalId) {
            clearInterval(this.updateAppointmentsIntervalId);
          }
        }
      });
  }

  login(): void {
    this.router.navigate(['/login']);
  }

  getTodaysAppointments(): void {
    this.appointmentService.getTodaysAppointments().subscribe((res: any) => {
      /* using appt id as q number */
      this.appointments = res;
      if (this.appointments) {
        if (!this.isAdmin) {
          this.userTodaysAppointments = this.appointments.filter(appointment => appointment.patientId === this.account?.id);

          // Format and save datetimeString for each appointment
          this.userTodaysAppointments.forEach(appointment => {
            if (appointment.apptDatetime) {
              appointment.datetimeString = dayjs(appointment.apptDatetime).format('HH:mm a');
            } else {
              appointment.datetimeString = null;
            }
          });

          this.userQueueNum = this.userTodaysAppointments[0]?.id;
          if (this.userTodaysAppointments.length > 0) {
            this.numPeopleInFront = this.appointments.findIndex(obj => obj.id === this.userQueueNum);
          }
        }
        if (this.appointments.length > 0) {
          this.currentAppointment = this.appointments[0];
          if (this.appointments.length > 1) {
            this.nextAppointment = this.appointments[1];
          }
        }
        this.lastUpdatedTime = dayjs().format('DD/MM/YYYY HH:mm:ss');
      }
    });
  }

  onClickNext(status: number): void {
    /* appt status: 0 = default, 1 = completed, 2 = misssed */
    let params = new HttpParams();
    if (this.currentAppointment !== undefined) {
      params = new HttpParams().set('id', this.currentAppointment.id).set('status', status);
    }
    this.appointmentService.updateApptStatus(params).subscribe((res: any) => {
      if (res) {
        this.getTodaysAppointments();
      }
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
