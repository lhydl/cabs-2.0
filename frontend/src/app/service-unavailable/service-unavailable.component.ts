import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'jhi-service-unavailable',
  standalone: true,
  imports: [],
  templateUrl: './service-unavailable.component.html',
  styleUrl: './service-unavailable.component.scss',
})
export class ServiceUnavailableComponent {
  constructor(private router: Router) {}

  retry(): void {
    window.location.reload();
  }

  goHome(): void {
    this.router.navigate(['/']);
  }
}
