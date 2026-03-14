import { TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness, RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';

import { AppointmentDetailComponent } from './appointment-detail.component';

describe('Appointment Management Detail Component', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppointmentDetailComponent, RouterTestingModule.withRoutes([], { bindToComponentInputs: true })],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              component: AppointmentDetailComponent,
              resolve: { appointment: () => of({ id: 123 }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(AppointmentDetailComponent, '')
      .compileComponents();
  });

  describe('OnInit', () => {
    it('Should load appointment on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', AppointmentDetailComponent);

      // THEN
      expect(instance.appointment).toEqual(expect.objectContaining({ id: 123 }));
    });
  });
});
