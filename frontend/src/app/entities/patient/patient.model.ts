export interface IPatient {
  id: number;
  name?: string | null;
  email?: string | null;
  phone_number?: string | null;
}

export type NewPatient = Omit<IPatient, 'id'> & { id: null };
