import { AuditListComponent } from './audit-list.component';
import { TestBed, ComponentFixture } from '@angular/core/testing';
import { RouterModule } from '@angular/router';
import { AppModule } from 'src/app/app.module';
import { SharedModule } from 'src/app/shared/shared.module';

describe('AuditListComponent', () => {
  let component: AuditListComponent;
  let fixture: ComponentFixture<AuditListComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [AuditListComponent],
      imports: [RouterModule.forRoot([]), SharedModule, AppModule],
    });

    fixture = TestBed.createComponent(AuditListComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
