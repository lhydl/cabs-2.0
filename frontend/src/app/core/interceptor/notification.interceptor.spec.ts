import { HttpHandler, HttpHeaders, HttpRequest, HttpResponse } from '@angular/common/http';
import { of } from 'rxjs';

import { AlertService } from 'app/core/util/alert.service';

import { NotificationInterceptor } from './notification.interceptor';

describe('NotificationInterceptor', () => {
  let interceptor: NotificationInterceptor;
  let alertService: jest.Mocked<Pick<AlertService, 'addAlert'>>;

  beforeEach(() => {
    alertService = {
      addAlert: jest.fn(),
    };

    interceptor = new NotificationInterceptor(alertService as AlertService);
  });

  it('should create a success alert from app-alert headers', done => {
    const next: HttpHandler = {
      handle: jest.fn().mockReturnValue(
        of(
          new HttpResponse({
            status: 200,
            headers: new HttpHeaders({ 'X-cabsApp-alert': 'Appointment created' }),
          }),
        ),
      ),
    };

    interceptor.intercept(new HttpRequest('POST', '/api/appointments'), next).subscribe(() => {
      expect(alertService.addAlert).toHaveBeenCalledWith({
        type: 'success',
        message: 'Appointment created',
      });
      done();
    });
  });

  it('should ignore responses without app-alert headers', done => {
    const next: HttpHandler = {
      handle: jest.fn().mockReturnValue(of(new HttpResponse({ status: 200 }))),
    };

    interceptor.intercept(new HttpRequest('GET', '/api/appointments'), next).subscribe(() => {
      expect(alertService.addAlert).not.toHaveBeenCalled();
      done();
    });
  });
});
