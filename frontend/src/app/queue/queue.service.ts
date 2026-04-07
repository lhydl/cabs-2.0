import { HttpClient, HttpParams } from '@angular/common/http';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { IAppointment } from 'app/entities/appointment/appointment.model';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class QueueService {
  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/queue', 'queue');

  constructor(
    private http: HttpClient,
    protected applicationConfigService: ApplicationConfigService,
  ) {}

  /**
   * Get today's appointments and queue
   */
  getTodaysAppointmentsAndQueue(): Observable<IAppointment[]> {
    return this.http.get<IAppointment[]>(`${this.resourceUrl}/getTodayApptQueue`);
  }

  /**
   * Update queue status
   */
  updateQueueStatus(params: HttpParams): Observable<number> {
    return this.http.post<number>(`${this.resourceUrl}/updateQueueStatus`, null, { params });
  }

  getHealth(): Observable<string> {
    return this.http.get(`${this.resourceUrl}/health`, {
      responseType: 'text',
    });
  }
}
