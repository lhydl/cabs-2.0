import { ActivatedRoute, Router } from '@angular/router';
import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'jhi-service-unavailable',
  standalone: true,
  imports: [],
  templateUrl: './service-unavailable.component.html',
  styleUrl: './service-unavailable.component.scss',
})
export class ServiceUnavailableComponent implements OnInit {
  returnUrl: string = '/';

  constructor(
    private router: Router,
    private route: ActivatedRoute,
  ) {}

  ngOnInit(): void {
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
  }

  retry(): void {
    this.router.navigateByUrl(this.returnUrl);
  }

  goHome(): void {
    this.router.navigate(['/']);
  }
}
