import { Component, OnInit } from '@angular/core';
import { FormGroup, FormControl, FormsModule, ReactiveFormsModule, Validators, ValidatorFn, AbstractControl } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import SharedModule, { notAfterTodayValidator } from 'app/shared/shared.module';
import { IUser } from '../user-management.model';
import { UserManagementService } from '../service/user-management.service';
import dayjs from 'dayjs';

const userTemplate = {} as IUser;

const newUser: IUser = {
  activated: true,
} as IUser;

@Component({
  standalone: true,
  selector: 'jhi-user-mgmt-update',
  templateUrl: './user-management-update.component.html',
  styleUrl: './user-management-update.component.scss',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export default class UserManagementUpdateComponent implements OnInit {
  authorities: string[] = [];
  isSaving = false;
  today: string = dayjs().format('YYYY-MM-DD');
  genderList: string[] = ['Male', 'Female', 'Others'];

  editForm = new FormGroup({
    id: new FormControl(userTemplate.id),
    login: new FormControl(userTemplate.login, {
      nonNullable: true,
      validators: [
        Validators.required,
        Validators.minLength(1),
        Validators.maxLength(50),
        Validators.pattern('^[a-zA-Z0-9!$&*+=?^_`{|}~.-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$|^[_.@A-Za-z0-9-]+$'),
      ],
    }),
    firstName: new FormControl(userTemplate.firstName, { validators: [Validators.required, Validators.maxLength(50)] }),
    lastName: new FormControl(userTemplate.lastName, { validators: [Validators.required, Validators.maxLength(50)] }),
    phoneNumber: new FormControl(userTemplate.phoneNumber, {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern('^[0-9]{1,8}$')],
    }),
    dob: new FormControl(userTemplate.dob, {
      nonNullable: true,
      validators: [Validators.required, notAfterTodayValidator()],
    }),
    gender: new FormControl(userTemplate.gender, {
      nonNullable: true,
      validators: [Validators.required],
    }),
    email: new FormControl(userTemplate.email, {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(5), Validators.maxLength(254), Validators.email],
    }),
    activated: new FormControl(userTemplate.activated, { nonNullable: true }),
    authorities: new FormControl(userTemplate.authorities, { nonNullable: true }),
  });

  constructor(
    private userService: UserManagementService,
    private route: ActivatedRoute,
  ) {}

  ngOnInit(): void {
    this.route.data.subscribe(({ user }) => {
      if (user) {
        this.editForm.reset(user);
        this.editForm.patchValue({
          dob: dayjs(user.dob).format('YYYY-MM-DD'),
        });
      } else {
        this.editForm.reset(newUser);
      }
    });
    this.userService.authorities().subscribe(authorities => (this.authorities = authorities));
  }

  // notAfterTodayValidator(): ValidatorFn {
  //   return (control: AbstractControl): { [key: string]: any } | null => {
  //     const today = new Date();
  //     today.setHours(0, 0, 0, 0);
  //     const selectedDate = new Date(control.value);
  //     selectedDate.setHours(0, 0, 0, 0);
  //     return selectedDate > today ? { notAfterToday: { value: control.value } } : null;
  //   };
  // }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const user = this.editForm.getRawValue();
    if (user.id !== null) {
      this.userService.update(user).subscribe({
        next: () => this.onSaveSuccess(),
        error: () => this.onSaveError(),
      });
    } else {
      this.userService.create(user).subscribe({
        next: () => this.onSaveSuccess(),
        error: () => this.onSaveError(),
      });
    }
  }

  private onSaveSuccess(): void {
    this.isSaving = false;
    this.previousState();
  }

  private onSaveError(): void {
    this.isSaving = false;
  }
}
