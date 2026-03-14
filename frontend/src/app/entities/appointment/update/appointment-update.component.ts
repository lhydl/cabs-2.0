import { Component, OnInit } from '@angular/core';
import { HttpParams, HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Router } from '@angular/router';
import { Observable, Subject } from 'rxjs';
import { finalize, takeUntil } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormBuilder, FormsModule, ReactiveFormsModule, UntypedFormGroup, Validators } from '@angular/forms';

import { IAppointment } from '../appointment.model';
import { AppointmentService } from '../service/appointment.service';
import { AppointmentFormService, AppointmentFormGroup } from './appointment-form.service';
import { faAnglesDown, faMillSign } from '@fortawesome/free-solid-svg-icons';
import HasAnyAuthorityDirective from 'app/shared/auth/has-any-authority.directive';
import { AccountService } from 'app/core/auth/account.service';
import { Account } from 'app/core/auth/account.model';
import { UserManagementService } from 'app/admin/user-management/service/user-management.service';
import { User } from 'app/admin/user-management/user-management.model';
import { DATE_FORMAT, TIME_FORMAT } from 'app/config/input.constants';
import dayjs, { Dayjs } from 'dayjs/esm';
import { addMinutes, format } from 'date-fns';
import { DatePipe } from '@angular/common';

@Component({
  standalone: true,
  selector: 'jhi-appointment-update',
  templateUrl: './appointment-update.component.html',
  styleUrls: ['./appointment-update.component.scss'],
  imports: [SharedModule, FormsModule, ReactiveFormsModule, HasAnyAuthorityDirective],
  providers: [DatePipe],
})
export class AppointmentUpdateComponent implements OnInit {
  isSaving = false;
  appointment: IAppointment | null = null;
  isNewPatient: boolean = false;
  account: Account | null = null;
  isEdit: boolean = false;
  isAdmin: boolean = this.accountService.hasAnyAuthority('ROLE_ADMIN');
  userList: User[] | null = null;
  apptTypeList: string[] = ['Consultation', 'Urgent Care', 'Dental', 'Pharmacy'];
  timeslots: string[] = [];
  existingTimeslots: string[] = [];
  formattedExistingTimeslots: string[] = [];
  selectedDate: string | undefined;
  formattedSelectedDate: string | undefined;
  today: string = dayjs().format('YYYY-MM-DD');
  genderList: string[] = ['Male', 'Female', 'Others'];
  isValidDate: boolean = true;

  editForm: AppointmentFormGroup = this.appointmentFormService.createAppointmentFormGroup({ id: null }, this.isNewPatient, this.isAdmin);

  private readonly destroy$ = new Subject<void>();

  constructor(
    protected appointmentService: AppointmentService,
    protected appointmentFormService: AppointmentFormService,
    protected activatedRoute: ActivatedRoute,
    protected router: Router,
    protected fb: FormBuilder,
    private accountService: AccountService,
    private userService: UserManagementService,
    private datePipe: DatePipe,
  ) {}

  ngOnInit(): void {
    this.accountService
      .getAuthenticationState()
      .pipe(takeUntil(this.destroy$))
      .subscribe(account => (this.account = account));
    this.activatedRoute.data.subscribe(({ appointment }) => {
      this.appointment = appointment;
      if (appointment) {
        this.isEdit = true;
        this.updateForm(appointment);
      }
    });
    if (this.isAdmin) {
      this.getUserList();
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  public toggleIsNewPatient(): void {
    this.initializeForm();
    this.selectedDate = undefined;
    // this.editForm.get('patientId')?.reset();
    // this.editForm.get('firstName')?.reset();
    // this.editForm.get('lastName')?.reset();
    // this.editForm.get('phoneNumber')?.reset();
    // this.editForm.get('email')?.reset();
    // this.updateValidations();
  }

  // public updateValidations(): void {
  //   if (!this.isNewPatient) {
  //     this.editForm.get('patientId')?.clearValidators();
  //     this.editForm.get('firstName')?.addValidators([Validators.required]);
  //     this.editForm.get('lastName')?.addValidators([Validators.required]);
  //     this.editForm.get('phoneNumber')?.addValidators([Validators.required]);
  //     this.editForm.get('email')?.addValidators([Validators.required]);
  //   } else {
  //     this.editForm.get('patientId')?.addValidators([Validators.required])
  //     this.editForm.get('firstName')?.clearValidators();
  //     this.editForm.get('lastName')?.clearValidators();
  //     this.editForm.get('phoneNumber')?.clearValidators();
  //     this.editForm.get('email')?.clearValidators();
  //   }
  // }

  public getUserList(): void {
    this.userService.getUserList().subscribe((res: any) => {
      if (res) {
        this.userList = res;
      } else {
        this.userList = [];
      }
    });
  }

  initializeForm(): void {
    this.editForm = this.appointmentFormService.createAppointmentFormGroup({ id: null }, this.isNewPatient, this.isAdmin);
  }

  previousState(): void {
    window.history.back();
  }

  generateTimeSlots(event?: Event, patchedDate?: string): void {
    this.timeslots = [];
    if (event === undefined) {
      this.selectedDate = patchedDate; // For edit appt
    } else {
      const input = event.target as HTMLInputElement;
      this.selectedDate = input.value; // For create new appt
    }
    if (this.selectedDate !== undefined) {
      if (this.selectedDate < this.today) {
        this.isValidDate = false;
      } else {
        this.isValidDate = true;
        // Get booked time slots from db based on selected date
        this.editForm.get('apptTime')?.reset();
        this.formattedSelectedDate = dayjs(this.selectedDate).format('DD/MM/YYYY');
        this.getExistingTimeSlots(this.selectedDate);
      }
    }
  }

  public getExistingTimeSlots(selectedDate: string): void {
    const params = new HttpParams().set('selectedDate', selectedDate);
    this.appointmentService.getExistingTimeSlots(params).subscribe((res: any) => {
      if (res) {
        this.existingTimeslots = res;
        this.formatTimeslots();
        const startTime = new Date('2000-01-01T08:00:00'); // 08:00 AM
        const endTime = new Date('2000-01-01T20:00:00'); // 08:00 PM
        // If selected today's date, available timeslots are after current time
        if (this.selectedDate === this.today) {
          const now = new Date();
          startTime.setHours(now.getHours() + 1); // Move to the next hour
          startTime.setMinutes(30);
        }
        let currentTime = startTime;
        // Generate available timeslots
        while (currentTime < endTime) {
          const timeLabel = `${format(currentTime, 'HH:mm')}`;
          if (!this.formattedExistingTimeslots.includes(timeLabel)) {
            this.timeslots.push(timeLabel);
          }
          currentTime = addMinutes(currentTime, 30); // One time slot every 30 mins
        }
      }
    });
  }

  formatTimeslots(): void {
    this.formattedExistingTimeslots = this.existingTimeslots
      .map(slot => {
        const dateObject = new Date(slot);
        return this.datePipe.transform(dateObject, 'HH:mm');
      })
      .filter(formattedSlot => formattedSlot !== null) as string[];
  }

  save(): void {
    this.isSaving = true;
    const appointment = this.appointmentFormService.getAppointment(this.editForm);
    if (appointment.apptDate && appointment.apptTime) {
      const datetimeString = `${appointment.apptDate} ${appointment.apptTime}`;
      appointment.apptDatetime = dayjs(datetimeString);
    }
    if (appointment.id !== null) {
      this.subscribeToSaveResponse(this.appointmentService.update(appointment));
    } else {
      if (appointment.patientId === null) {
        appointment.patientId = this.account?.id;
      }
      this.subscribeToSaveResponse(this.appointmentService.create(appointment));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IAppointment>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    // this.previousState();
    this.router.navigate(['/appointment']);
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(appointment: IAppointment): void {
    this.appointment = appointment;
    const patchedDate = appointment.apptDatetime;
    const dateLabel = patchedDate?.format('YYYY-MM-DD');
    this.generateTimeSlots(undefined, dateLabel?.toString());
    this.appointmentFormService.resetForm(this.editForm, appointment);
  }
}
