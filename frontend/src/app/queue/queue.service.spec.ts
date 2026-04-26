import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HttpParams } from '@angular/common/http';

import { QueueService } from './queue.service';

describe('Queue Service', () => {
  let service: QueueService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });

    service = TestBed.inject(QueueService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it("should fetch today's appointment queue", () => {
    const expected = [{ id: 123 }, { id: 456 }];

    service.getTodaysAppointmentsAndQueue().subscribe(response => {
      expect(response).toEqual(expected);
    });

    const req = httpMock.expectOne('/api-gateway/queue/api/queue/getTodayApptQueue');
    expect(req.request.method).toBe('GET');
    req.flush(expected);
  });

  it('should update queue status with provided params', () => {
    const params = new HttpParams().set('patientId', '1').set('status', '2');

    service.updateQueueStatus(params).subscribe(response => {
      expect(response).toBe(1);
    });

    const req = httpMock.expectOne(request => request.url === '/api-gateway/queue/api/queue/updateQueueStatus');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toBeNull();
    expect(req.request.params.get('patientId')).toBe('1');
    expect(req.request.params.get('status')).toBe('2');
    req.flush(1);
  });

  it('should request health as plain text', () => {
    service.getHealth().subscribe(response => {
      expect(response).toBe('OK');
    });

    const req = httpMock.expectOne('/api-gateway/queue/api/queue/health');
    expect(req.request.method).toBe('GET');
    expect(req.request.responseType).toBe('text');
    req.flush('OK');
  });
});
