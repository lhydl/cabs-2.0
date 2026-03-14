import { Component, OnInit } from '@angular/core';
import { HttpHeaders, HttpParams } from '@angular/common/http';
import { ActivatedRoute, Data, ParamMap, Router, RouterModule } from '@angular/router';
import { combineLatest, filter, Observable, Subject, switchMap, takeUntil, tap } from 'rxjs';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { SortDirective, SortByDirective } from 'app/shared/sort';
import { DurationPipe, FormatMediumDatetimePipe, FormatMediumDatePipe } from 'app/shared/date';
import { ItemCountComponent } from 'app/shared/pagination';
import { FormBuilder, FormsModule, UntypedFormGroup } from '@angular/forms';
import { MatChipsModule } from '@angular/material/chips';

import { ALL_ITEMS, ITEMS_PER_PAGE, PAGE_HEADER, TOTAL_COUNT_RESPONSE_HEADER } from 'app/config/pagination.constants';
import { ASC, DESC, SORT, ITEM_DELETED_EVENT, DEFAULT_SORT_DATA } from 'app/config/navigation.constants';
import { IAppointment, PatientMappingsDTO } from '../appointment.model';
import { EntityArrayResponseType, AppointmentService } from '../service/appointment.service';
import { AppointmentDeleteDialogComponent } from '../delete/appointment-delete-dialog.component';
import { AccountService } from 'app/core/auth/account.service';
import { Account } from 'app/core/auth/account.model';
import HasAnyAuthorityDirective from 'app/shared/auth/has-any-authority.directive';
import dayjs from 'dayjs/esm';
import { Dayjs } from 'dayjs';

@Component({
  standalone: true,
  selector: 'jhi-appointment',
  templateUrl: './appointment.component.html',
  styleUrls: ['./appointment.component.scss'],
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
export class AppointmentComponent implements OnInit {
  appointments?: IAppointment[];
  filteredAppointments: IAppointment[] = [];
  displayedAppointments: IAppointment[] = [];
  state: 'upcoming' | 'past' = 'upcoming';
  isLoading = false;

  predicate = 'id';
  ascending = true;

  allItems = ALL_ITEMS;
  itemsPerPage = ITEMS_PER_PAGE;
  totalItems = 0;
  page = 1;
  account: Account | null = null;
  isAdmin: boolean = this.accountService.hasAnyAuthority('ROLE_ADMIN');
  displayTitle: string | null = null;
  patientMappings: PatientMappingsDTO[] = [];
  searchForm!: UntypedFormGroup;
  apptTypeList: string[] = ['Consultation', 'Urgent Care', 'Dental', 'Pharmacy'];
  apptType: string | null = null;
  statusColors: { [key: number]: string } = {
    0: 'red',
    1: 'green',
    2: 'red',
  };

  private readonly destroy$ = new Subject<void>();

  constructor(
    protected appointmentService: AppointmentService,
    protected activatedRoute: ActivatedRoute,
    public router: Router,
    protected modalService: NgbModal,
    private accountService: AccountService,
    private fb: FormBuilder,
  ) {}

  trackId = (_index: number, item: IAppointment): number => this.appointmentService.getAppointmentIdentifier(item);

  ngOnInit(): void {
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

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  setUserRoleContent(): void {
    this.accountService.identity().subscribe(account => {
      if (account) {
        if (this.accountService.hasAnyAuthority(['ROLE_ADMIN'])) {
          this.displayTitle = 'Manage Appointments';
        } else {
          this.displayTitle = 'Manage Your Appointments';
        }
      }
    });
  }

  toggleState(state: 'upcoming' | 'past') {
    this.searchForm.reset();
    this.state = state;
    this.filterAppointments();
  }

  clearSearch(): void {
    this.searchForm.reset();
    this.filterAppointments();
  }

  filterAppointments() {
    const today = dayjs();
    this.page = 1;
    if (this.appointments != null) {
      // if (!this.isAdmin) {
      //   this.appointments = this.appointments.filter(appointment => appointment.patientId === this.account?.id);
      // }
      // console.log('all:::' + this.appointments.length);
      if (this.state === 'upcoming') {
        this.filteredAppointments = this.appointments.filter(appointment => dayjs(appointment.apptDatetime).isAfter(today));
        // console.log('upcoming:::' + this.filteredAppointments.length);
      } else {
        this.filteredAppointments = this.appointments.filter(appointment => dayjs(appointment.apptDatetime).isBefore(today));
        // console.log('past:::' + this.filteredAppointments.length);
      }

      this.apptType = this.searchForm.value.apptType;
      const apptDate = this.searchForm.value.apptDate;
      const remarks = this.searchForm.value.remarks;
      const patientName = this.searchForm.value.patientName;
      if (this.apptType) {
        this.filteredAppointments = this.filteredAppointments.filter(appointment => appointment.apptType === this.apptType);
      }
      if (apptDate) {
        this.filteredAppointments = this.filteredAppointments.filter(
          appointment => appointment.apptDatetime?.format('YYYY-MM-DD') === apptDate,
        );
      }
      if (remarks) {
        this.filteredAppointments = this.filteredAppointments.filter(
          appointment => appointment.remarks?.toLowerCase()?.includes(remarks.toLowerCase()),
        );
      }
      if (patientName) {
        const patient = this.patientMappings.find(p =>
          (p.firstName?.toLowerCase() + ' ' + p.lastName?.toLowerCase() + ' ' + p.login?.toLowerCase()).includes(patientName.toLowerCase()),
        );
        this.filteredAppointments = this.filteredAppointments.filter(appointment => appointment.patientId?.toString() === patient?.id);
      }
    }

    this.updatePagination();
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

  load(): void {
    if (this.isAdmin) {
      this.loadFromBackendWithRouteInformations().subscribe({
        next: (res: EntityArrayResponseType) => {
          this.onResponseSuccess(res);
        },
      });
      this.getPatientMappings();
    } else {
      this.loadUserAppt('appt_datetime', 'ASC');
    }
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
    return foundMapping.firstName + ' ' + foundMapping.lastName + ' ' + '(' + foundMapping.login + ')';
  }

  navigateToWithComponentValues(): void {
    this.handleNavigation(this.page, this.predicate, this.ascending);
  }

  // navigateToPage(page = this.page): void {
  //   this.handleNavigation(page, this.predicate, this.ascending);
  // }

  navigateToPage(page: number): void {
    this.page = page;
    // console.log('page:::' + this.page);
    this.updatePagination();
  }

  updatePagination(): void {
    this.totalItems = this.filteredAppointments.length;
    const startIndex = (this.page - 1) * this.itemsPerPage;
    const endIndex = startIndex + this.itemsPerPage;
    this.displayedAppointments = this.filteredAppointments.slice(startIndex, endIndex);
    // console.log('display:::' + this.displayedAppointments.length);
  }

  loadUserAppt(predicate: string, sort: string): void {
    const userId = this.account?.id;
    let params = new HttpParams();
    if (userId !== null && userId !== undefined) {
      params = new HttpParams().set('userId', userId).set('predicate', predicate).set('sort', sort);
    }
    this.appointmentService.getUserAppt(params).subscribe(res => {
      this.onResponseSuccess(res);
    });
  }

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
    const sort = (params.get(SORT) ?? data[DEFAULT_SORT_DATA]).split(',');
    this.predicate = sort[0];
    this.ascending = sort[1] === ASC;
  }

  protected onResponseSuccess(response: EntityArrayResponseType): void {
    this.fillComponentAttributesFromResponseHeader(response.headers);
    const dataFromBody = this.fillComponentAttributesFromResponseBody(response.body);
    this.appointments = dataFromBody;
    // console.log('appt::::' + this.appointments.length);
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
      size: this.allItems,
      sort: this.getSortQueryParam(predicate, ascending),
    };
    return this.appointmentService.query(queryObject).pipe(tap(() => (this.isLoading = false)));
  }

  protected handleNavigation(page = this.page, predicate?: string, ascending?: boolean): void {
    const queryParamsObj = {
      page: 1,
      size: this.allItems,
      sort: this.getSortQueryParam(predicate, ascending),
    };

    if (this.isAdmin) {
      this.router.navigate(['./'], {
        relativeTo: this.activatedRoute,
        queryParams: queryParamsObj,
      });
    } else {
      let sort = 'ASC';
      let pred = 'appt_datetime';
      if (ascending === false) {
        sort = 'DESC';
      }
      if (predicate === 'apptType') {
        pred = 'appt_type';
      } else if (predicate === 'apptDatetime') {
        pred = 'appt_datetime';
      } else if (predicate === 'remarks') {
        pred = 'remarks';
      } else if (predicate === 'status') {
        pred = 'status';
      }
      this.loadUserAppt(pred, sort);
    }
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
