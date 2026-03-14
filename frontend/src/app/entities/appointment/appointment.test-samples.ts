import dayjs from 'dayjs/esm';

import { IAppointment, NewAppointment } from './appointment.model';

export const sampleWithRequiredData: IAppointment = {
  id: 4352,
  apptType: 'yum fooey',
  apptDatetime: dayjs('2024-03-03T00:05'),
  patientId: 1080,
  doctorId: 13409,
};

export const sampleWithPartialData: IAppointment = {
  id: 3578,
  apptType: 'boohoo',
  apptDatetime: dayjs('2024-03-02T16:09'),
  patientId: 12660,
  doctorId: 19595,
};

export const sampleWithFullData: IAppointment = {
  id: 1889,
  apptType: 'surface',
  apptDatetime: dayjs('2024-03-02T20:03'),
  remarks: 'amid woot',
  patientId: 18981,
  doctorId: 17025,
};

export const sampleWithNewData: NewAppointment = {
  apptType: 'tensely disclosure tingle',
  apptDatetime: dayjs('2024-03-02T16:24'),
  patientId: 1882,
  doctorId: 28289,
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
