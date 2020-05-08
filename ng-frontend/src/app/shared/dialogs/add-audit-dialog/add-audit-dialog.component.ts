import { Component, OnInit } from '@angular/core';
import { NbDialogRef } from '@nebular/theme';
import { Router } from '@angular/router';
import { Store } from '@ngxs/store';
import { Audit } from 'src/app/data/models/audit.model';
import { AddAudit } from 'src/app/ngxs/audit.actions';

@Component({
  selector: 'app-add-audit-dialog',
  templateUrl: './add-audit-dialog.component.html',
  styleUrls: ['./add-audit-dialog.component.scss'],
})
export class AddAuditDialogComponent implements OnInit {
  constructor(private store: Store, private dialogRef: NbDialogRef<any>, private router: Router) {}

  ngOnInit(): void {
    this.dialogRef.onClose.subscribe(() => {
      this.router.navigate(['/audits']);
    });
  }

  onSubmit(audit: Audit) {
    this.store.dispatch(new AddAudit(audit)).subscribe(() => this.dialogRef.close());
  }

  onCancel() {
    this.dialogRef.close();
  }
}