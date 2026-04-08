import { Component, Input } from '@angular/core';
import { ActivatedRoute, RouterModule } from '@angular/router';

import { HttpParams } from '@angular/common/http';
import { DurationPipe, FormatMediumDatePipe, FormatMediumDatetimePipe } from 'app/shared/date';
import SharedModule from 'app/shared/shared.module';
import dayjs from 'dayjs';
import { IAppointment, PatientDetailsDTO } from '../appointment.model';
import { AppointmentService } from '../service/appointment.service';

@Component({
  standalone: true,
  selector: 'jhi-appointment-detail',
  templateUrl: './appointment-detail.component.html',
  imports: [SharedModule, RouterModule, DurationPipe, FormatMediumDatetimePipe, FormatMediumDatePipe],
})
export class AppointmentDetailComponent {
  @Input() appointment: IAppointment | null = null;
  patientDetails: PatientDetailsDTO | null = null;
  formattedDob: string | null = null;

  constructor(
    protected activatedRoute: ActivatedRoute,
    protected appointmentService: AppointmentService,
  ) { }

  ngOnInit(): void {
    this.getPatientDetails();
  }

  public getPatientDetails(): void {
    let params = new HttpParams();
    if (this.appointment?.patientId) {
      params = new HttpParams().set('userId', this.appointment.patientId);
    }
    this.appointmentService.getPatientDetails(params).subscribe((res: any) => {
      if (res) {
        this.patientDetails = res;
        this.formattedDob = dayjs(this.patientDetails?.dob).format('DD MMM YYYY');
      }
    });
  }

  previousState(): void {
    window.history.back();
  }
}
