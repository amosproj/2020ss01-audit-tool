import { Interview } from '../../data/models/interview.model';

export class AddInterview {
  static readonly type = '[AuditOverview] Add Interview';
  constructor(public interview: Interview) {}
}

export class UpdateInterview {
  static readonly type = '[AuditOverview] Update Interview';
  constructor(public id: number, public interview: Interview) {}
}