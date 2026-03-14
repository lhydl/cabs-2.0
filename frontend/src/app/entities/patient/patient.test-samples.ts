import { IPatient, NewPatient } from './patient.model';

export const sampleWithRequiredData: IPatient = {
  id: 28090,
  name: 'by',
  email: '%ta9M@OcNdda!vWIY',
  phone_number: '977',
};

export const sampleWithPartialData: IPatient = {
  id: 1687,
  name: 'amongst boo',
  email: 'B%w@pqqvCZKJUc',
  phone_number: '80411025',
};

export const sampleWithFullData: IPatient = {
  id: 23875,
  name: 'apud',
  email: 'jUVadn@Fhbcg]vyEu',
  phone_number: '2',
};

export const sampleWithNewData: NewPatient = {
  name: 'combination',
  email: '-_LOw@pnzNktTKc',
  phone_number: '883',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
