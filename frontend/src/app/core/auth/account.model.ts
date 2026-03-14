export class Account {
  constructor(
    public activated: boolean,
    public authorities: string[],
    public email: string,
    public firstName: string | null,
    public langKey: string,
    public lastName: string | null,
    public login: string,
    public imageUrl: string | null,
    public phoneNumber: string,
    public id: number | null,
    public dob: string,
    public gender?: string,
  ) {}
}
