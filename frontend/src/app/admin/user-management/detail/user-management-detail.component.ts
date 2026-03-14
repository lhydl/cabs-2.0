import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import SharedModule from 'app/shared/shared.module';
import dayjs from 'dayjs';

import { User } from '../user-management.model';

@Component({
  standalone: true,
  selector: 'jhi-user-mgmt-detail',
  templateUrl: './user-management-detail.component.html',
  imports: [SharedModule],
})
export default class UserManagementDetailComponent implements OnInit {
  user: User | null = null;
  formattedDob: string | null = null;

  constructor(private route: ActivatedRoute) {}

  ngOnInit(): void {
    this.route.data.subscribe(({ user }) => {
      this.user = user;
      this.formattedDob = dayjs(this.user?.dob).format('DD MMM YYYY');
    });
  }

  previousState(): void {
    window.history.back();
  }
}
