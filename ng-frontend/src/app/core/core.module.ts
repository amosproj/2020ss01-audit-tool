import { NgModule, Optional, SkipSelf } from '@angular/core';
import { NgxsModule } from '@ngxs/store';
import { AuditRegistryState } from './ngxs/audit-registry.state';

@NgModule({
  imports: [NgxsModule.forRoot([AuditRegistryState])],
  exports: [NgxsModule],
})
export class CoreModule {
  /**
   * Throws an error if a second instance of CoreModule is created
   */
  constructor(@Optional() @SkipSelf() coreModule: CoreModule) {
    if (coreModule) {
      throw new Error('CoreModule is already loaded. Import it in the AppModule only');
    }
  }
}
