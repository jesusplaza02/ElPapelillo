// footer.ts
import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { GlobalConfig } from '../../../constants';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './footer.html',
  styleUrl: './footer.css'
})
export class FooterComponent {
  public config = GlobalConfig;
}