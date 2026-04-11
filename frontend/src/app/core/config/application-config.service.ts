import { Injectable } from '@angular/core';
import { environment } from 'environments/environment';

@Injectable({
  providedIn: 'root',
})
export class ApplicationConfigService {
  private endpointPrefix = '';
  private microfrontend = false;

  setEndpointPrefix(endpointPrefix: string): void {
    this.endpointPrefix = endpointPrefix;
  }

  setMicrofrontend(microfrontend = true): void {
    this.microfrontend = microfrontend;
  }

  isMicrofrontend(): boolean {
    return this.microfrontend;
  }

  // getEndpointFor(api: string, microservice?: string): string {
  //   if (microservice) {
  //     this.setEndpointPrefix('/api-gateway/');
  //     return `${this.endpointPrefix}${microservice}/${api}`;
  //   }
  //   this.setEndpointPrefix('/api-gateway/cabs/');
  //   return `${this.endpointPrefix}${api}`;
  // }

  getEndpointFor(api: string, microservice?: string): string {
    if (microservice) {
      return `${environment.apiBaseUrl}/${microservice}/${api}`;
    }
    return `${environment.apiBaseUrl}/cabs/${api}`;
  }
}
