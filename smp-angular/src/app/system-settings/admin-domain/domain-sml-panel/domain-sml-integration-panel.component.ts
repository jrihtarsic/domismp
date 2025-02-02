import {Component, EventEmitter, Input, Output,} from '@angular/core';
import {DomainRo} from "../../../common/model/domain-ro.model";
import {AbstractControl, FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {AdminDomainService} from "../admin-domain.service";
import {AlertMessageService} from "../../../common/alert-message/alert-message.service";
import {MatDialog} from "@angular/material/dialog";
import {BeforeLeaveGuard} from "../../../window/sidenav/navigation-on-leave-guard";
import {ConfirmationDialogComponent} from "../../../common/dialogs/confirmation-dialog/confirmation-dialog.component";
import {GlobalLookups} from "../../../common/global-lookups";
import {HttpErrorHandlerService} from "../../../common/error/http-error-handler.service";
import {CertificateRo} from "../../../common/model/certificate-ro.model";
import {SmlIntegrationService} from "../../../common/services/sml-integration.service";
import {SMLResult} from "../../../common/model/sml-result.model";
import {TranslateService} from "@ngx-translate/core";
import {lastValueFrom} from "rxjs";


@Component({
  selector: 'domain-sml-integration-panel',
  templateUrl: './domain-sml-integration-panel.component.html',
  styleUrls: ['./domain-sml-integration-panel.component.scss']
})
export class DomainSmlIntegrationPanelComponent implements BeforeLeaveGuard {
  @Output() onSaveSmlIntegrationDataEvent: EventEmitter<DomainRo> = new EventEmitter();
  readonly dnsDomainPattern = '^([a-zA-Z]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?){0,63}$';
  readonly subDomainPattern = this.dnsDomainPattern;
  readonly smpIdDomainPattern = this.dnsDomainPattern;
  _domain: DomainRo = null;

  domainForm: FormGroup;

  readonly warningTimeout: number = 50000;
  fieldWarningTimeoutMap = {
    domainCodeTimeout: null,
    smlDomainCodeTimeout: null,
    smlsmpidTimeout: null,
  };
  editMode: boolean;


  notInList(list: string[], exception: string) {
    if (!list || !exception) {
      return (c: AbstractControl): { [key: string]: any } => {
        return null;
      }
    }

    return (c: AbstractControl): { [key: string]: any } => {
      if (c.value && c.value !== exception && list.includes(c.value))
        return {'notInList': {valid: false}};
      return null;
    }
  }

  /**
   * Show warning if domain code exceed the maxlength.
   * @param value
   */
  onFieldKeyPressed(controlName: string, showTheWarningReference: string) {
    let value = this.domainForm.get(controlName).value

    if (!!value && value.length >= 63 && !this.fieldWarningTimeoutMap[showTheWarningReference]) {
      this.fieldWarningTimeoutMap[showTheWarningReference] = setTimeout(() => {
        this.fieldWarningTimeoutMap[showTheWarningReference] = null;
      }, this.warningTimeout);
    }
  }

  constructor(private domainService: AdminDomainService,
              private alertService: AlertMessageService,
              private httpErrorHandlerService: HttpErrorHandlerService,
              protected smlIntegrationService: SmlIntegrationService,
              protected lookups: GlobalLookups,
              private dialog: MatDialog,
              private formBuilder: FormBuilder,
              private translateService: TranslateService) {

    this.domainForm = formBuilder.group({
      'smlSubdomain': new FormControl({
        value: '',
        disabled: this.editMode
      }, [Validators.pattern(this.subDomainPattern),
        this.notInList(this.lookups.cachedDomainList.map(a => a.smlSubdomain), this._domain?.smlSubdomain)]),
      'smlSmpId': new FormControl({
        value: '',
        disabled: this.isDomainRegistered
      }, [Validators.pattern(this.smpIdDomainPattern),
        this.notInList(this.lookups.cachedDomainList.map(a => a.smlSmpId), this._domain?.smlSmpId)]),
      'smlClientKeyAlias': new FormControl({value: '', readonly: true}),
      'smlClientCertAuth': new FormControl({value: '', readonly: true}),
      'smlClientKeyCertificate': new FormControl({value: '', readonly: true}),
      'smlRegistered': new FormControl({value: '', readonly: true}),

    });
  }

  get domain(): DomainRo {

    let newDomain = {...this._domain};
    newDomain.smlSubdomain = this.domainForm.get('smlSubdomain').value;
    newDomain.smlSmpId = this.domainForm.get('smlSmpId').value;
    newDomain.smlClientKeyAlias = this.domainForm.get('smlClientKeyAlias').value;
    newDomain.smlClientCertAuth = this.domainForm.get('smlClientCertAuth').value;
    return newDomain;
  }

  @Input() set domain(value: DomainRo) {
    this._domain = value;
    if (!!this._domain) {
      this.domainForm.controls['smlSubdomain'].setValue(this._domain.smlSubdomain);
      this.domainForm.controls['smlSmpId'].setValue(this._domain.smlSmpId);
      this.domainForm.controls['smlClientKeyAlias'].setValue(this._domain.smlClientKeyAlias);
      this.domainForm.controls['smlRegistered'].setValue(this._domain.smlRegistered);
      this.domainForm.controls['smlClientCertAuth'].setValue(this._domain.smlClientCertAuth);
      this.domainForm.enable();
      if (this.isDomainRegistered) {
        this.domainForm.controls['smlSmpId'].disable()
      }
    } else {
      this.domainForm.controls['smlSubdomain'].setValue("");
      this.domainForm.controls['smlSmpId'].setValue("");
      this.domainForm.controls['smlClientKeyAlias'].setValue("");
      this.domainForm.controls['smlRegistered'].setValue("");
      this.domainForm.controls['smlClientCertAuth'].setValue("");
      this.domainForm.disable();
    }

    this.domainForm.markAsPristine();
  }

  @Input() keystoreCertificates: CertificateRo[];

  get submitButtonEnabled(): boolean {
    return this.domainForm.valid && this.domainForm.dirty;
  }

  get resetButtonEnabled(): boolean {
    return this.domainForm.dirty;
  }

  public onSaveButtonClicked() {
    this.onSaveSmlIntegrationDataEvent.emit(this.domain);
  }

  public onResetButtonClicked() {
    this.domainForm.reset(this._domain);
  }

  isDirty(): boolean {
    return this.domainForm.dirty;
  }


  get isSMPIntegrationOn() {
    return this.lookups.cachedApplicationConfig?.smlIntegrationOn
  }

  enableSMLRegister(): boolean {
    if (!this._domain || !this.isSMPIntegrationOn || this.isDirty()) {
      return false;
    }

    if (!this._domain.smlClientKeyAlias ) {
      return false;
    }
    // entity must be first persisted in order to be enabled to register to SML
    return !this._domain.smlRegistered;
  }

  enableSMLUnregister(): boolean {
    if (!this._domain || !this.isSMPIntegrationOn || this.isDirty()) {
      return false;
    }

    if (!this._domain.smlClientKeyAlias && !this._domain.smlClientCertAuth) {
      return false;
    }

    // entity must be first persisted in order to be enabled to register to SML
    return this.isDomainRegistered;
  }

  get isDomainRegistered():boolean {
    return !!this._domain?.smlRegistered;
  }


  async smlUnregisterSelectedDomain() {
    if (!this._domain) {
      return false;
    }

    this.dialog.open(ConfirmationDialogComponent, {
      data: {
        title: await lastValueFrom(this.translateService.get("domain.sml.integration.panel.unregister.confirmation.dialog.title")),
        description: await lastValueFrom(this.translateService.get("domain.sml.integration.panel.unregister.confirmation.dialog.description", {domainCode: this._domain?.domainCode}))
      }
    }).afterClosed().subscribe(result => {
      if (result) {
        this.smlUnregisterDomain(this._domain);
      }
    })
  }

  async smlRegisterSelectedDomain() {
    if (!this._domain) {
      return false;
    }

    this.dialog.open(ConfirmationDialogComponent, {
      data: {
        title: await lastValueFrom(this.translateService.get("domain.sml.integration.panel.register.confirmation.dialog.title")),
        description: await lastValueFrom(this.translateService.get("domain.sml.integration.panel.register.confirmation.dialog.description", {domainCode: this._domain?.domainCode}))
      }
    }).afterClosed().subscribe(result => {
      if (result) {
        this.smlRegisterDomain(this._domain);
      }
    })
  }

  smlRegisterDomain(domain: DomainRo) {
    this.smlIntegrationService.registerDomainToSML$(domain).toPromise().then(async (res: SMLResult) => {
        if (res) {
          if (res.success) {
            this.alertService.success(await lastValueFrom(this.translateService.get("domain.sml.integration.panel.success.register", {domainCode: domain.domainCode})));
            domain.smlRegistered = true;
            this.domain = domain;
          } else {
            this.alertService.exception(await lastValueFrom(this.translateService.get("domain.sml.integration.panel.error.register", {domainCode: domain.domainCode})), res.errorMessage);
          }
        } else {
          this.alertService.exception(await lastValueFrom(this.translateService.get("domain.sml.integration.panel.error.register", {domainCode: domain.domainCode})),
            await lastValueFrom(this.translateService.get("domain.sml.integration.panel.error.register.unknown.error")));
        }
      },
      async err => {
        if (this.httpErrorHandlerService.logoutOnInvalidSessionError(err)) {
          return;
        }
        this.alertService.exception(await lastValueFrom(this.translateService.get("domain.sml.integration.panel.error.register", {domainCode: domain.domainCode})), err);
      }
    )
  }

  smlUnregisterDomain(domain: DomainRo) {

    this.smlIntegrationService.unregisterDomainToSML$(domain).toPromise().then(async (res: SMLResult) => {
        if (res) {
          if (res.success) {
            this.alertService.success(await lastValueFrom(this.translateService.get("domain.sml.integration.panel.success.unregister", {domainCode: domain.domainCode})));
            domain.smlRegistered = false;
            this.domain = domain;
          } else {
            this.alertService.exception(await lastValueFrom(this.translateService.get("domain.sml.integration.panel.error.unregister", {domainCode: domain.domainCode})), res.errorMessage);
          }
        } else {
          this.alertService.exception(await lastValueFrom(this.translateService.get("domain.sml.integration.panel.error.unregister", {domainCode: domain.domainCode})),
            await lastValueFrom(this.translateService.get("domain.sml.integration.panel.error.unregister.unknown.error")));
        }
      }
      ,
      async err => {
        if (this.httpErrorHandlerService.logoutOnInvalidSessionError(err)) {
          return;
        }
        this.alertService.exception(await lastValueFrom(this.translateService.get("domain.sml.integration.panel.error.unregister", {domainCode: domain.domainCode})), err);
      }
    )
  }
}
