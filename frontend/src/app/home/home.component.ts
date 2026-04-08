import { AppointmentService, EntityArrayResponseType } from 'app/entities/appointment/service/appointment.service';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router, RouterModule } from '@angular/router';

import { Account } from 'app/core/auth/account.model';
import { AccountService } from 'app/core/auth/account.service';
import HasAnyAuthorityDirective from 'app/shared/auth/has-any-authority.directive';
import { HttpErrorResponse, HttpParams } from '@angular/common/http';
import { IAppointment } from 'app/entities/appointment/appointment.model';
import { MatDividerModule } from '@angular/material/divider';
import { QueueService } from 'app/queue/queue.service';
import SharedModule from 'app/shared/shared.module';
import { Subject } from 'rxjs';
import dayjs from 'dayjs';
import { takeUntil } from 'rxjs/operators';

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
  isQueueServiceUp: boolean = true;

  private readonly destroy$ = new Subject<void>();

  constructor(
    private accountService: AccountService,
    private queueService: QueueService,
    private router: Router,
  ) {}

  // NOTE: USE *jhiHasAnyAuthority in HTML! I'VE REMOVED USER ROLE FOR admin
  ngOnInit(): void {
    this.checkQueueServiceHealth();
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
            // Real-time queue update for users, polling every 10 seconds
            this.updateAppointmentsIntervalId = setInterval(() => {
              this.getTodaysAppointments();
            }, 10000);
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
    this.queueService.getTodaysAppointmentsAndQueue().subscribe({
      next: res => {
        this.isQueueServiceUp = true;
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
      },
      error: (err: HttpErrorResponse) => {
        console.error('Health check failed:', err);
        if (err.status === 503) {
          this.isQueueServiceUp = false;
        }
      },
    });
  }

  onClickNext(status: number): void {
    /* appt status: 0 = default, 1 = completed, 2 = misssed */
    let params = new HttpParams();
    if (this.currentAppointment !== undefined) {
      params = new HttpParams().set('id', this.currentAppointment.id).set('status', status);
    }
    this.queueService.updateQueueStatus(params).subscribe((res: any) => {
      if (res) {
        this.getTodaysAppointments();
      }
    });
  }

  checkQueueServiceHealth(): void {
    this.queueService.getHealth().subscribe({
      next: res => {
        console.log('Queue service healthy:', res);
        this.isQueueServiceUp = true;
      },
      error: (err: HttpErrorResponse) => {
        console.error('Health check failed:', err);
        if (err.status === 503) {
          this.isQueueServiceUp = false;
        }
      },
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    if (this.updateAppointmentsIntervalId) {
      clearInterval(this.updateAppointmentsIntervalId);
    }
  }
}
