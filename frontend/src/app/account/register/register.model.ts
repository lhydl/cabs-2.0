export class Registration {
  constructor(
    public login: string,
    public email: string,
    public password: string,
    public langKey: string,
    public firstName: string,
    public lastName: string,
    public phoneNumber: string,
    public dob: string,
    public gender?: string,
  ) {}
}
