import { Injectable } from '@angular/core';
import { FormGroup, FormControl, Validators, ValidatorFn, AbstractControl } from '@angular/forms';

import dayjs from 'dayjs/esm';
import { DATE_TIME_FORMAT, TIME_FORMAT, DATE_FORMAT } from 'app/config/input.constants';
import { IAppointment, NewAppointment } from '../appointment.model';
import { notAfterTodayValidator } from 'app/shared/shared.module';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IAppointment for edit and NewAppointmentFormGroupInput for create.
 */
type AppointmentFormGroupInput = IAppointment | PartialWithRequiredKeyOf<NewAppointment>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends IAppointment | NewAppointment> = Omit<T, 'apptDatetime'> & {
  // apptDatetime?: string | null;
  apptDate?: string | null;
  apptTime?: string | null;
};

type AppointmentFormRawValue = FormValueOf<IAppointment>;

type NewAppointmentFormRawValue = FormValueOf<NewAppointment>;

type AppointmentFormDefaults = Pick<NewAppointment, 'id' | 'apptDate' | 'apptTime'>;

type AppointmentFormGroupContent = {
  id: FormControl<AppointmentFormRawValue['id'] | NewAppointment['id']>;
  apptType: FormControl<AppointmentFormRawValue['apptType']>;
  // apptDatetime: FormControl<AppointmentFormRawValue['apptDatetime']>;
  apptDate: FormControl<AppointmentFormRawValue['apptDate']>;
  apptTime: FormControl<AppointmentFormRawValue['apptTime']>;
  remarks: FormControl<AppointmentFormRawValue['remarks']>;
  patientId: FormControl<AppointmentFormRawValue['patientId']>;
  firstName: FormControl<AppointmentFormRawValue['firstName']>;
  lastName: FormControl<AppointmentFormRawValue['lastName']>;
  email: FormControl<AppointmentFormRawValue['email']>;
  phoneNumber: FormControl<AppointmentFormRawValue['phoneNumber']>;
  // doctorId: FormControl<AppointmentFormRawValue['doctorId']>;
  dob: FormControl<AppointmentFormRawValue['dob']>;
  gender: FormControl<AppointmentFormRawValue['gender']>;
};

export type AppointmentFormGroup = FormGroup<AppointmentFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class AppointmentFormService {
  createAppointmentFormGroup(
    appointment: AppointmentFormGroupInput = { id: null },
    isNewPatient: boolean,
    isAdmin: boolean,
  ): AppointmentFormGroup {
    const appointmentRawValue = this.convertAppointmentToAppointmentRawValue({
      ...this.getFormDefaults(),
      ...appointment,
    });
    return new FormGroup<AppointmentFormGroupContent>({
      id: new FormControl(
        { value: appointmentRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      apptType: new FormControl(appointmentRawValue.apptType, {
        validators: [Validators.required, Validators.maxLength(100)],
      }),
      apptDate: new FormControl(appointmentRawValue.apptDate, {
        validators: [Validators.required, this.notBeforeTodayValidator()],
      }),
      apptTime: new FormControl(appointmentRawValue.apptTime, {
        validators: [Validators.required],
      }),
      // apptDatetime: new FormControl(appointmentRawValue.apptDatetime, {
      //   validators: [Validators.required],
      // }),
      remarks: new FormControl(appointmentRawValue.remarks, {
        validators: [Validators.maxLength(500)],
      }),
      patientId: new FormControl(appointmentRawValue.patientId, {
        validators: !isNewPatient && isAdmin ? [Validators.required] : [],
      }),
      firstName: new FormControl(appointmentRawValue.firstName, {
        validators: isNewPatient && isAdmin ? [Validators.required] : [],
      }),
      lastName: new FormControl(appointmentRawValue.lastName, {
        validators: isNewPatient && isAdmin ? [Validators.required] : [],
      }),
      email: new FormControl(appointmentRawValue.email, {
        validators:
          isNewPatient && isAdmin ? [Validators.required, Validators.pattern('[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+.[A-Za-z]{2,}')] : [],
      }),
      phoneNumber: new FormControl(appointmentRawValue.phoneNumber, {
        validators: isNewPatient && isAdmin ? [Validators.required, Validators.pattern('^[0-9]{1,8}$')] : [],
      }),
      // doctorId: new FormControl(appointmentRawValue.doctorId, {
      //   validators: [Validators.required],
      // }),
      dob: new FormControl(appointmentRawValue.dob, {
        validators: isNewPatient && isAdmin ? [Validators.required, notAfterTodayValidator()] : [],
      }),
      gender: new FormControl(appointmentRawValue.gender, {
        validators: isNewPatient && isAdmin ? [Validators.required] : [],
      }),
    });
  }

  getAppointment(form: AppointmentFormGroup): IAppointment | NewAppointment {
    return this.convertAppointmentRawValueToAppointment(form.getRawValue() as AppointmentFormRawValue | NewAppointmentFormRawValue);
  }

  resetForm(form: AppointmentFormGroup, appointment: AppointmentFormGroupInput): void {
    const appointmentRawValue = this.convertAppointmentToAppointmentRawValue({ ...this.getFormDefaults(), ...appointment });
    form.reset(
      {
        ...appointmentRawValue,
        id: { value: appointmentRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  notBeforeTodayValidator(): ValidatorFn {
    return (control: AbstractControl): { [key: string]: any } | null => {
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      const selectedDate = new Date(control.value);
      return selectedDate < today ? { notBeforeToday: { value: control.value } } : null;
    };
  }

  private getFormDefaults(): AppointmentFormDefaults {
    const currentTime = dayjs();
    return {
      id: null,
      apptDate: currentTime.format(DATE_FORMAT),
      apptTime: currentTime.format(TIME_FORMAT),
      // apptDatetime: currentTime,
    };
  }

  private convertAppointmentRawValueToAppointment(
    rawAppointment: AppointmentFormRawValue | NewAppointmentFormRawValue,
  ): IAppointment | NewAppointment {
    const { apptDate, apptTime } = rawAppointment;

    if (apptDate && apptTime) {
      const datetimeString = `${apptDate} ${apptTime}`;
      // Parse the concatenated datetime string using dayjs
      const apptDatetime = dayjs(datetimeString, `${DATE_FORMAT} ${TIME_FORMAT}`);

      // Return the appointment with the parsed datetime
      return {
        ...rawAppointment,
        apptDatetime,
      };
    }
    // Return the rawAppointment as is if either apptDate or apptTime is null or undefined
    return rawAppointment;

    // return {
    //   ...rawAppointment,
    //   apptDatetime: dayjs(rawAppointment.apptDate, + ' ' + rawAppointment.apptTime, DATE_FORMAT + ' ' + TIME_FORMAT),
    // };
  }

  private convertAppointmentToAppointmentRawValue(
    appointment: IAppointment | (Partial<NewAppointment> & AppointmentFormDefaults),
  ): AppointmentFormRawValue | PartialWithRequiredKeyOf<NewAppointmentFormRawValue> {
    const apptDatetime = appointment.apptDatetime ? dayjs(appointment.apptDatetime) : null;
    return {
      ...appointment,
      apptDate: apptDatetime ? apptDatetime.format('YYYY-MM-DD') : undefined,
      apptTime: apptDatetime ? apptDatetime.format(TIME_FORMAT) : undefined,
      // apptDatetime: appointment.apptDatetime ? appointment.apptDatetime.format(DATE_TIME_FORMAT) : undefined,
    };
  }
}
