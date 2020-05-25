import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { FormGroup, Validators, FormBuilder, AbstractControl } from '@angular/forms';
import { ConfirmDiscardDialogComponent } from '../../dialogs/confirm-discard-dialog/confirm-discard-dialog.component';
import { Audit, AuditStatus } from 'src/app/core/data/models/audit.model';
import { Factor } from 'src/app/core/data/models/factor.model';
import { NbDialogService } from '@nebular/theme';
import { factors } from 'src/app/core/data/factors';

@Component({
  selector: 'app-audit-data-form',
  templateUrl: './audit-data-form.component.html',
  styleUrls: ['./audit-data-form.component.scss'],
})
export class AuditDataFormComponent implements OnInit {
  @Input() audit: Audit;
  @Input() submitButtonName: string;
  @Input() cancelButtonName: string;

  @Output() formSubmitted = new EventEmitter<Audit>();
  @Output() cancelled = new EventEmitter<any>();

  formFactors: Factor[];
  auditForm: FormGroup;

  constructor(private formBuilder: FormBuilder, private dialogService: NbDialogService) {}

  //#region Getters
  get auditName() {
    return this.auditForm.get('auditName');
  }

  get start() {
    return this.auditForm.get('start');
  }

  get end() {
    return this.auditForm.get('end');
  }

  get companyName() {
    return this.auditForm.get('companyName');
  }

  get sector() {
    return this.auditForm.get('sector');
  }

  get department() {
    return this.auditForm.get('department');
  }

  get corporateDivision() {
    return this.auditForm.get('corporateDivision');
  }

  get salutation() {
    return this.auditForm.get('salutation');
  }

  get title() {
    return this.auditForm.get('title');
  }

  get firstName() {
    return this.auditForm.get('firstName');
  }

  get lastName() {
    return this.auditForm.get('lastName');
  }

  get contactInformation() {
    return this.auditForm.get('contactInformation');
  }

  get status() {
    return this.auditForm.get('status');
  }

  get creationDate() {
    return this.auditForm.get('creationDate');
  }
  //#endregion

  ngOnInit(): void {
    if (this.audit?.scope) {
      // Get all criteria titles in audit
      const criteriaTitles = [];
      for (const factor of this.audit.scope) {
        for (const criteria of factor.criterias) {
          criteriaTitles.push(criteria.title);
        }
      }

      // Set selected property of criteria to true if contained in criteriaTitles
      this.formFactors = factors.map(factor => {
        const formCategories = factor.criterias.map(x => {
          return { title: x.title, selected: criteriaTitles.includes(x.title) };
        });

        const hasSelectedCategory = formCategories.find(x => x.selected) != undefined;
        return { criterias: formCategories, title: factor.title, selected: hasSelectedCategory };
      });
    } else {
      // Select every factor and criteria
      this.formFactors = factors.map(x => ({ ...x, selected: true }));
      this.formFactors.forEach(x => {
        x.criterias = x.criterias.map(x => ({ ...x, selected: true }));
      });
    }

    this.auditForm = this.formBuilder.group({
      auditName: [this.audit?.name, Validators.required],
      start: [this.audit?.start ?? new Date().setHours(0, 0, 0, 0)],
      end: [this.audit?.end, [this.startGreaterThanEndValidator.bind(this)]],
      status: [this.audit?.status ?? AuditStatus.IsPlanned],
      companyName: [this.audit?.customerData.name],
      sector: [this.audit?.customerData.sector],
      department: [this.audit?.customerData.department],
      salutation: [this.audit?.contactPerson.salutation],
      title: [this.audit?.contactPerson.title],
      corporateDivision: [this.audit?.customerData.corporateDivision],
      firstName: [this.audit?.contactPerson.firstName],
      lastName: [this.audit?.contactPerson.lastName],
      contactInformation: [this.audit?.contactPerson.information],
    });
  }

  onSubmit() {
    const filteredFactors = this.filterFactors(this.formFactors);
    let creationDate = this.audit?.creationDate;
    if (!creationDate) {
      creationDate = Date.now();
    }

    const audit: Audit = {
      name: this.auditName.value,
      status: +this.status.value,
      contactPerson: {
        salutation: this.salutation.value,
        title: this.title.value,
        firstName: this.firstName.value,
        lastName: this.lastName.value,
        information: this.contactInformation.value,
      },
      customerData: {
        department: this.department.value,
        name: this.companyName.value,
        sector: this.sector.value,
        corporateDivision: this.corporateDivision.value,
      },
      start: this.parseDate(this.start.value),
      end: this.parseDate(this.end.value),
      scope: [...filteredFactors],
      creationDate: creationDate,
    };

    this.formSubmitted.emit(audit);
  }

  /**
   * Removes factors that don't contain a selected criteria
   * and removes "selected" property from factors and categories
   *
   * @param factors The factors to filter
   */
  filterFactors(factors: Factor[]) {
    const filteredFactors = factors.filter(x => x.criterias.findIndex(x => x['selected']) != -1);

    filteredFactors.forEach(x => {
      x.criterias = x.criterias.filter(x => x['selected']);
      delete x['selected'];
      x.criterias.forEach(x => delete x['selected']);
    });

    return filteredFactors;
  }

  onCancel() {
    if (this.auditForm.dirty && this.auditForm.touched) {
      const confirmDiscardComponentRef = this.dialogService.open(ConfirmDiscardDialogComponent, {
        autoFocus: false,
        closeOnBackdropClick: false,
      });

      confirmDiscardComponentRef.componentRef.instance.onDiscardConfirm.subscribe(
        (cancelConfirmed: boolean) => {
          if (cancelConfirmed) {
            this.cancelled.emit();
          }
        },
      );
    } else {
      this.cancelled.emit();
    }
  }

  onFactorSelect(factor: Factor) {
    factor['selected'] = !factor['selected'];
    factor.criterias.forEach(x => (x['selected'] = factor['selected']));
  }

  startGreaterThanEndValidator(control: AbstractControl): { [s: string]: boolean } {
    const start = this.audit?.start ?? new Date().setHours(0, 0, 0, 0);
    return start > this.parseDate(control.value) ? { startGreaterThanEnd: true } : null;
  }

  parseDate(s: string) {
    return s ? new Date(s).getTime() : undefined;
  }
}
