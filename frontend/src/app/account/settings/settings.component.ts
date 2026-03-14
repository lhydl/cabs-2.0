import { Component, OnInit } from '@angular/core';
import { FormGroup, FormControl, Validators, FormsModule, ReactiveFormsModule } from '@angular/forms';

import SharedModule, { notAfterTodayValidator } from 'app/shared/shared.module';
import { AccountService } from 'app/core/auth/account.service';
import { Account } from 'app/core/auth/account.model';
import dayjs from 'dayjs';

const initialAccount: Account = {} as Account;

@Component({
  selector: 'jhi-settings',
  standalone: true,
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.scss'],
})
export default class SettingsComponent implements OnInit {
  success = false;
  today: string = dayjs().format('YYYY-MM-DD');
  genderList: string[] = ['Male', 'Female', 'Others'];

  settingsForm = new FormGroup({
    firstName: new FormControl(initialAccount.firstName, {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(1), Validators.maxLength(50)],
    }),
    lastName: new FormControl(initialAccount.lastName, {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(1), Validators.maxLength(50)],
    }),
    email: new FormControl(initialAccount.email, {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(5), Validators.maxLength(254), Validators.email],
    }),
    phoneNumber: new FormControl(initialAccount.phoneNumber, {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern('^[0-9]{1,8}$')],
    }),
    dob: new FormControl(initialAccount.dob, {
      nonNullable: true,
      validators: [Validators.required, notAfterTodayValidator()],
    }),
    gender: new FormControl(initialAccount.gender, {
      nonNullable: true,
      validators: [Validators.required],
    }),
    langKey: new FormControl(initialAccount.langKey, { nonNullable: true }),

    activated: new FormControl(initialAccount.activated, { nonNullable: true }),
    authorities: new FormControl(initialAccount.authorities, { nonNullable: true }),
    imageUrl: new FormControl(initialAccount.imageUrl, { nonNullable: true }),
    login: new FormControl(initialAccount.login, { nonNullable: true }),
    id: new FormControl(initialAccount.id, { nonNullable: true }),
  });

  constructor(private accountService: AccountService) {}

  ngOnInit(): void {
    this.accountService.identity().subscribe(account => {
      if (account) {
        this.settingsForm.patchValue(account);
        this.settingsForm.patchValue({
          dob: dayjs(account.dob).format('YYYY-MM-DD'),
        });
      }
    });
  }

  save(): void {
    this.success = false;
    const account = this.settingsForm.getRawValue();
    this.accountService.save(account).subscribe(() => {
      this.success = true;
      this.accountService.authenticate(account);
      setTimeout(() => {
        window.location.reload();
      }, 400);
    });
  }
}
