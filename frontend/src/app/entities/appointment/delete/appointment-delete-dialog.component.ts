import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { IAppointment } from '../appointment.model';
import { AppointmentService } from '../service/appointment.service';

@Component({
  standalone: true,
  templateUrl: './appointment-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class AppointmentDeleteDialogComponent {
  appointment?: IAppointment;

  constructor(
    protected appointmentService: AppointmentService,
    protected activeModal: NgbActiveModal,
  ) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.appointmentService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
