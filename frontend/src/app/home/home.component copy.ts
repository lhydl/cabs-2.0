import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Data, ParamMap, Router, RouterModule } from '@angular/router';
import { Observable, Subject, combineLatest } from 'rxjs';
import { filter, switchMap, takeUntil, tap } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { AccountService } from 'app/core/auth/account.service';
import { Account } from 'app/core/auth/account.model';
import { AppointmentService, EntityArrayResponseType } from 'app/entities/appointment/service/appointment.service';
import { IAppointment, PatientMappingsDTO } from 'app/entities/appointment/appointment.model';
import { FormBuilder, FormsModule, UntypedFormGroup } from '@angular/forms';
import { ITEMS_PER_PAGE, PAGE_HEADER, TOTAL_COUNT_RESPONSE_HEADER } from 'app/config/pagination.constants';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { HttpHeaders, HttpParams } from '@angular/common/http';
import { ASC, DEFAULT_SORT_DATA, DESC, ITEM_DELETED_EVENT, SORT } from 'app/config/navigation.constants';
import dayjs from 'dayjs';
import { SortByDirective, SortDirective } from 'app/shared/sort';
import { DurationPipe, FormatMediumDatePipe, FormatMediumDatetimePipe } from 'app/shared/date';
import { ItemCountComponent } from 'app/shared/pagination';
import HasAnyAuthorityDirective from 'app/shared/auth/has-any-authority.directive';
import { MatChipsModule } from '@angular/material/chips';
import { AppointmentDeleteDialogComponent } from 'app/entities/appointment/delete/appointment-delete-dialog.component';

import { MatDividerModule } from '@angular/material/divider';

@Component({
  standalone: true,
  selector: 'jhi-home',
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss',
  imports: [
    RouterModule,
    FormsModule,
    SharedModule,
    SortDirective,
    SortByDirective,
    DurationPipe,
    FormatMediumDatetimePipe,
    FormatMediumDatePipe,
    ItemCountComponent,
    HasAnyAuthorityDirective,
    MatChipsModule,
  ],
})
export default class HomeComponent implements OnInit, OnDestroy {
  account: Account | null = null;

  appointments?: IAppointment[];
  filteredAppointments: IAppointment[] = [];
  state: 'upcoming' | 'past' = 'upcoming';
  isLoading = false;

  predicate = 'id';
  ascending = true;

  itemsPerPage = ITEMS_PER_PAGE;
  totalItems = 0;
  page = 1;
  isAdmin: boolean = this.accountService.hasAnyAuthority('ROLE_ADMIN');
  displayTitle: string | null = null;
  patientMappings: PatientMappingsDTO[] = [];
  searchForm!: UntypedFormGroup;
  apptTypeList: string[] = ['Consultation', 'Urgent Care', 'Dental', 'Pharmacy'];
  apptType: string | null = null;

  private readonly destroy$ = new Subject<void>();
  allAppointments?: IAppointment[];
  onlyFirstFive: string | undefined;

  constructor(
    protected appointmentService: AppointmentService,
    private accountService: AccountService,
    private router: Router,

    protected activatedRoute: ActivatedRoute,
    protected modalService: NgbModal,
    private fb: FormBuilder,
  ) {}

  trackId = (_index: number, item: IAppointment): number => this.appointmentService.getAppointmentIdentifier(item);

  ngOnInit(): void {
    this.accountService
      .getAuthenticationState()
      .pipe(takeUntil(this.destroy$))
      .subscribe(account => (this.account = account));
    this.searchForm = this.fb.group({
      apptType: [null],
      apptDate: [null],
      remarks: [null],
      patientName: [null],
    });
    this.setUserRoleContent();
    this.accountService
      .getAuthenticationState()
      .pipe(takeUntil(this.destroy$))
      .subscribe(account => (this.account = account));
    this.load();
  }

  login(): void {
    this.router.navigate(['/login']);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  delete(appointment: IAppointment): void {
    const modalRef = this.modalService.open(AppointmentDeleteDialogComponent, { size: 'lg', backdrop: 'static' });
    modalRef.componentInstance.appointment = appointment;
    // unsubscribe not needed because closed completes on modal close
    modalRef.closed
      .pipe(
        filter(reason => reason === ITEM_DELETED_EVENT),
        switchMap(() => this.loadFromBackendWithRouteInformations()),
      )
      .subscribe({
        next: (res: EntityArrayResponseType) => {
          this.load();
        },
      });
  }

  complete(appointment: IAppointment): void {
    // Update the appointment status to 2 (completed)
    appointment.status = 1;

    // Call the appointment service to update the appointment
    this.appointmentService.update(appointment).subscribe({
      next: () => {
        // After updating, reload the data
        this.load();
      },
      error: error => {
        // Handle error if needed
        console.error('Error updating appointment:', error);
      },
    });
  }

  refreshList(): void {
    this.load();
  }

  setUserRoleContent(): void {
    this.accountService.identity().subscribe(account => {
      if (account) {
        if (this.accountService.hasAnyAuthority(['ROLE_ADMIN'])) {
          this.displayTitle = 'Manage Current Queue';
          this.onlyFirstFive = '';
        } else {
          this.displayTitle = 'Your Today Appointment';
          this.onlyFirstFive = '(Only first 5 appointment is shown)';
        }
      }
    });
  }

  clearSearch(): void {
    this.searchForm.reset();
    this.filterAppointments();
  }

  filterAppointments(): void {
    const today = dayjs().startOf('day');
    if (this.appointments) {
      this.allAppointments = this.appointments.slice();

      this.allAppointments = this.allAppointments.filter(appointment => appointment.status !== 1);

      this.allAppointments = this.allAppointments.filter(appointment => {
        const appointmentDate = dayjs(appointment.apptDatetime, 'DD MMM YYYY HH:mm:ss');
        return appointmentDate.isSame(today, 'day');
      });

      this.allAppointments.sort((a, b) => {
        const dateA = dayjs(a.apptDatetime, 'DD MMM YYYY HH:mm:ss');
        const dateB = dayjs(b.apptDatetime, 'DD MMM YYYY HH:mm:ss');
        return dateA.diff(dateB);
      });
      if (!this.accountService.hasAnyAuthority('ROLE_ADMIN')) {
        this.filteredAppointments = this.allAppointments.filter(appointment => appointment.patientId === this.account?.id);
      } else {
        this.filteredAppointments = this.allAppointments || [];
      }
    }
  }

  load(): void {
    // if (this.isAdmin) {
    this.loadFromBackendWithRouteInformations().subscribe({
      next: (res: EntityArrayResponseType) => {
        this.onResponseSuccess(res);
      },
    });
    this.getPatientMappings();
    // } else {
    //   this.loadUserAppt();
    // }
  }

  getPatientMappings(): void {
    this.appointmentService.getPatientMappings().subscribe(res => {
      this.patientMappings = res;
    });
  }

  getPatientNameById(patientId: number | null | undefined): string {
    if (patientId == null) {
      return 'Unknown';
    }
    const foundMapping = this.patientMappings.find(mapping => mapping.id?.toString() === patientId.toString());
    if (!foundMapping) {
      return 'Unknown';
    }
    return foundMapping.firstName + ' ' + foundMapping.lastName;
  }

  navigateToWithComponentValues(): void {
    this.handleNavigation(this.page, this.predicate, this.ascending);
  }

  navigateToPage(page = this.page): void {
    this.handleNavigation(page, this.predicate, this.ascending);
  }

  loadUserAppt(): void {
    const userId = this.account?.id;
    let params = new HttpParams();
    if (userId !== null && userId !== undefined) {
      params = new HttpParams().set('userId', userId);
    }
    this.appointmentService.getUserAppt(params).subscribe(res => {
      this.onResponseSuccess(res);
    });
  }

  statusColors: { [key: number]: string } = {
    0: 'green',
    1: 'red',
  };

  getStatusColorClass(status: number | null | undefined): string {
    if (status === null || status === undefined) {
      return '';
    }
    return this.statusColors[status] || '';
  }

  protected loadFromBackendWithRouteInformations(): Observable<EntityArrayResponseType> {
    return combineLatest([this.activatedRoute.queryParamMap, this.activatedRoute.data]).pipe(
      tap(([params, data]) => this.fillComponentAttributeFromRoute(params, data)),
      switchMap(() => this.queryBackend(this.page, this.predicate, this.ascending)),
    );
  }

  protected fillComponentAttributeFromRoute(params: ParamMap, data: Data): void {
    const page = params.get(PAGE_HEADER);
    this.page = +(page ?? 1);

    // Ensure that sort is a valid string before splitting it
    const sortValue = params.get(SORT) ?? data[DEFAULT_SORT_DATA];
    const sort = typeof sortValue === 'string' ? sortValue.split(',') : [this.predicate, ASC];

    // Ensure that sort has at least two elements before accessing them
    this.predicate = sort[0] || this.predicate;
    this.ascending = sort[1] === ASC;
  }

  protected onResponseSuccess(response: EntityArrayResponseType): void {
    this.fillComponentAttributesFromResponseHeader(response.headers);
    const dataFromBody = this.fillComponentAttributesFromResponseBody(response.body);
    this.appointments = dataFromBody;
    this.filterAppointments();
  }

  protected fillComponentAttributesFromResponseBody(data: IAppointment[] | null): IAppointment[] {
    return data ?? [];
  }

  protected fillComponentAttributesFromResponseHeader(headers: HttpHeaders): void {
    this.totalItems = Number(headers.get(TOTAL_COUNT_RESPONSE_HEADER));
  }

  protected queryBackend(page?: number, predicate?: string, ascending?: boolean): Observable<EntityArrayResponseType> {
    this.isLoading = true;
    const pageToLoad: number = page ?? 1;
    const queryObject: any = {
      page: pageToLoad - 1,
      size: this.itemsPerPage,
      sort: this.getSortQueryParam(predicate, ascending),
    };
    return this.appointmentService.query(queryObject).pipe(tap(() => (this.isLoading = false)));
  }

  protected handleNavigation(page = this.page, predicate?: string, ascending?: boolean): void {
    const queryParamsObj = {
      page,
      size: this.itemsPerPage,
      sort: this.getSortQueryParam(predicate, ascending),
    };

    this.router.navigate(['./'], {
      relativeTo: this.activatedRoute,
      queryParams: queryParamsObj,
    });
  }

  protected getSortQueryParam(predicate = this.predicate, ascending = this.ascending): string[] {
    const ascendingQueryParam = ascending ? ASC : DESC;
    if (predicate === '') {
      return [];
    } else {
      return [predicate + ',' + ascendingQueryParam];
    }
  }
}
