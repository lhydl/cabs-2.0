import { NumberValueAccessor } from '@angular/forms';
import dayjs from 'dayjs/esm';

export interface IAppointment {
  id: number;
  apptType?: string | null;
  apptDatetime?: dayjs.Dayjs | null;
  datetimeString?: string | null;
  apptDate?: string | null;
  apptTime?: string | null;
  remarks?: string | null;
  patientId?: number | null;
  doctorId?: number | null;
  firstName?: string | null;
  lastName?: string | null;
  email?: string | null;
  phoneNumber?: string | null;
  dob?: Date | null;
  gender?: string | null;
  status?: number | null;
}

export class PatientDetailsDTO {
  firstName?: string;
  lastName?: string;
  phoneNumber?: string;
  email?: string;
  dob?: Date;
  gender?: string;
  login?: string;
}

export class PatientMappingsDTO {
  id?: number;
  firstName?: string;
  lastName?: string;
  login?: string;
}

export type NewAppointment = Omit<IAppointment, 'id'> & { id: null };
