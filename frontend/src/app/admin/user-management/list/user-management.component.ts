import { Component, OnInit } from '@angular/core';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { HttpResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { combineLatest } from 'rxjs';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { SortDirective, SortByDirective } from 'app/shared/sort';
import { ALL_ITEMS, ITEMS_PER_PAGE } from 'app/config/pagination.constants';
import { ASC, DESC, SORT } from 'app/config/navigation.constants';
import { ItemCountComponent } from 'app/shared/pagination';
import { AccountService } from 'app/core/auth/account.service';
import { Account } from 'app/core/auth/account.model';
import { UserManagementService } from '../service/user-management.service';
import { User } from '../user-management.model';
import UserManagementDeleteDialogComponent from '../delete/user-management-delete-dialog.component';
import { FormBuilder, UntypedFormGroup } from '@angular/forms';

@Component({
  standalone: true,
  selector: 'jhi-user-mgmt',
  templateUrl: './user-management.component.html',
  styleUrls: ['./user-management.component.scss'],
  imports: [RouterModule, SharedModule, SortDirective, SortByDirective, UserManagementDeleteDialogComponent, ItemCountComponent],
})
export default class UserManagementComponent implements OnInit {
  currentAccount: Account | null = null;
  users: User[] | null = null;
  isLoading = false;
  totalItems = 0;
  itemsPerPage = ITEMS_PER_PAGE;
  allItems = ALL_ITEMS;
  page!: number;
  predicate!: string;
  ascending!: boolean;
  isSuccess: boolean = false;
  searchForm!: UntypedFormGroup;
  filteredUsers: User[] = [];
  displayedUsers: User[] = [];

  constructor(
    private userService: UserManagementService,
    private accountService: AccountService,
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private modalService: NgbModal,
    private fb: FormBuilder,
  ) {}

  ngOnInit(): void {
    this.searchForm = this.fb.group({
      firstName: [null],
      lastName: [null],
      login: [null],
      email: [null],
    });
    this.accountService.identity().subscribe(account => (this.currentAccount = account));
    this.handleNavigation();
  }

  clearSearch(): void {
    this.searchForm.reset();
    this.page = 0;
    this.loadAll();
  }

  filterUsers() {
    this.page = 1;
    const firstName = this.searchForm.value.firstName;
    const lastName = this.searchForm.value.lastName;
    const login = this.searchForm.value.login;
    const email = this.searchForm.value.email;
    if (this.users !== null) {
      this.filteredUsers = this.users;
      this.displayedUsers = this.users;
      if (firstName) {
        this.filteredUsers = this.filteredUsers.filter(user => user.firstName?.toLowerCase()?.includes(firstName.toLowerCase()));
      }
      if (lastName) {
        this.filteredUsers = this.filteredUsers.filter(user => user.lastName?.toLowerCase()?.includes(lastName.toLowerCase()));
      }
      if (login) {
        this.filteredUsers = this.filteredUsers.filter(user => user.login?.toLowerCase()?.includes(login.toLowerCase()));
      }
      if (email) {
        this.filteredUsers = this.filteredUsers.filter(user => user.email?.toLowerCase()?.includes(email.toLowerCase()));
      }
    }
    this.updatePagination();
  }

  setActive(user: User, isActivated: boolean): void {
    this.userService.update({ ...user, activated: isActivated }).subscribe(() => this.clearSearch());
  }

  trackIdentity(_index: number, item: User): number {
    return item.id!;
  }

  deleteUser(user: User): void {
    const modalRef = this.modalService.open(UserManagementDeleteDialogComponent, { size: 'lg', backdrop: 'static' });
    modalRef.componentInstance.user = user;
    // unsubscribe not needed because closed completes on modal close
    modalRef.closed.subscribe(reason => {
      if (reason === 'deleted') {
        this.clearSearch();
      }
    });
  }

  loadAll(): void {
    this.isLoading = true;
    this.userService
      .query({
        page: this.page - 1,
        size: this.allItems,
        sort: this.sort(),
      })
      .subscribe({
        next: (res: HttpResponse<User[]>) => {
          this.isLoading = false;
          this.onSuccess(res.body, res.headers);
        },
        error: () => (this.isLoading = false),
      });
  }

  transition(): void {
    this.router.navigate(['./'], {
      relativeTo: this.activatedRoute.parent,
      queryParams: {
        page: 1,
        sort: `${this.predicate},${this.ascending ? ASC : DESC}`,
      },
    });
    this.filterUsers();
  }

  navigateToPage(page: number): void {
    this.page = page;
    this.updatePagination();
  }

  updatePagination(): void {
    this.totalItems = this.filteredUsers.length;
    const startIndex = (this.page - 1) * this.itemsPerPage;
    const endIndex = startIndex + this.itemsPerPage;
    this.displayedUsers = this.filteredUsers.slice(startIndex, endIndex);
  }

  // Demo: Step 2
  public getAdminDetails(): void {
    console.log('button clicked');
    const params = new HttpParams().set('role', 'ROLE_ADMIN');
    this.userService.getAdminDetails(params).subscribe(res => {
      if (res) {
        this.isSuccess = true;
        console.log('results: ' + JSON.stringify(res));
      } else {
        this.isSuccess = false;
      }
    });
  }

  private handleNavigation(): void {
    combineLatest([this.activatedRoute.data, this.activatedRoute.queryParamMap]).subscribe(([data, params]) => {
      const page = params.get('page');
      this.page = +(page ?? 1);
      const sort = (params.get(SORT) ?? data['defaultSort']).split(',');
      this.predicate = sort[0];
      this.ascending = sort[1] === ASC;
      this.loadAll();
    });
  }

  private sort(): string[] {
    const result = [`${this.predicate},${this.ascending ? ASC : DESC}`];
    if (this.predicate !== 'id') {
      result.push('id');
    }
    return result;
  }

  private onSuccess(users: User[] | null, headers: HttpHeaders): void {
    this.totalItems = Number(headers.get('X-Total-Count'));
    this.users = users;
    this.filterUsers();
  }
}
