import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material';
import {AbstractControl, FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {DomainRo} from "../domain-ro.model";
import {AlertService} from "../../alert/alert.service";
import {SearchTableEntityStatus} from "../../common/search-table/search-table-entity-status.model";
import {GlobalLookups} from "../../common/global-lookups";
import {CertificateRo} from "../../user/certificate-ro.model";
import {KeystoreEditDialogComponent} from "../keystore-edit-dialog/keystore-edit-dialog.component";
import {ServiceGroupDomainEditRo} from "../../service-group-edit/service-group-domain-edit-ro.model";

@Component({
  selector: 'domain-details-dialog',
  templateUrl: './domain-details-dialog.component.html'
})
export class DomainDetailsDialogComponent {

  static readonly NEW_MODE = 'New Domain';
  static readonly EDIT_MODE = 'Domain Edit';
  readonly subDomainPattern = '^(?![0-9]+$)(?!.*-$)(?!-)[a-zA-Z0-9-]{1,63}$';
  readonly smpIdDomainPattern = '^(?![0-9]+$)(?!.*-$)(?!-)[a-zA-Z0-9-]{0,63}$';
  // is part of domain
  readonly domainCodePattern = '^[a-zA-Z0-9]{1,63}$';

  editMode: boolean;
  formTitle: string;
  current: DomainRo & { confirmation?: string };
  domainForm: FormGroup;
  domain;
  selectedSMLCert: CertificateRo =null;


  notInList(list: string[], exception: string) {
    return (c: AbstractControl): { [key: string]: any } => {
      if (c.value && c.value !== exception && list.includes(c.value))
        return {'notInList': {valid: false}};
      return null;
    }
  }

  constructor(
    public dialog: MatDialog,
    public lookups: GlobalLookups,
    private dialogRef: MatDialogRef<DomainDetailsDialogComponent>,
    private alertService: AlertService,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private fb: FormBuilder) {

    this.editMode = data.edit;
    this.formTitle = this.editMode ? DomainDetailsDialogComponent.EDIT_MODE : DomainDetailsDialogComponent.NEW_MODE;
    this.current = this.editMode
      ? {
        ...data.row,
      }
      : {
        domainCode: '',
        smlSubdomain: '',
        smlSmpId: '',
        smlClientKeyAlias: '',
        signatureKeyAlias: '',
        status: SearchTableEntityStatus.NEW,
      };

    this.domainForm = fb.group({
      'domainCode': new FormControl({value: '', disabled: this.editMode}, [Validators.pattern(this.domainCodePattern),
        this.notInList(this.lookups.cachedDomainList.map(a => a.domainCode), this.current.domainCode)]),
      'smlSubdomain': new FormControl({
        value: '',
        disabled: this.editMode
      }, [Validators.pattern(this.subDomainPattern),
        this.notInList(this.lookups.cachedDomainList.map(a => a.smlSubdomain), this.current.smlSubdomain)]),
      'smlSmpId': new FormControl({value: ''}, [Validators.pattern(this.smpIdDomainPattern),
        this.notInList(this.lookups.cachedDomainList.map(a => a.smlSmpId), this.current.smlSmpId)]),
      'smlClientCertHeader': new FormControl({value: ''}, null),
      'smlClientKeyAlias': new FormControl({value: ''}, null),
      'smlClientKeyCertificate': new FormControl({value: this.selectedSMLCert}, null),
      'signatureKeyAlias': new FormControl({value: ''}, null),

      'smlRegistered': new FormControl({value: ''}, null),
      'smlBlueCoatAuth': new FormControl({value: ''}, null),

    });

    this.domainForm.controls['domainCode'].setValue(this.current.domainCode);
    this.domainForm.controls['smlSubdomain'].setValue(this.current.smlSubdomain);
    this.domainForm.controls['smlSmpId'].setValue(this.current.smlSmpId);

    this.domainForm.controls['smlClientKeyAlias'].setValue(this.current.smlClientKeyAlias);
    this.domainForm.controls['smlClientCertHeader'].setValue(this.current.smlClientCertHeader);
    this.domainForm.controls['signatureKeyAlias'].setValue(this.current.signatureKeyAlias);

    this.domainForm.controls['smlRegistered'].setValue(this.current.smlRegistered);
    this.domainForm.controls['smlBlueCoatAuth'].setValue(this.current.smlBlueCoatAuth);

    if (this.current.smlClientKeyAlias) {
      this.selectedSMLCert = this.lookups.cachedCertificateList.find(crt => crt.alias === this.current.smlClientKeyAlias);
      this.domainForm.controls['smlClientKeyCertificate'].setValue(this.selectedSMLCert );
    }
  }

  submitForm() {
    this.checkValidity(this.domainForm)

    // check if empty domain already exists
    if(this.current.status === SearchTableEntityStatus.NEW
    && !this.domainForm.value['smlSubdomain'] ){

      var domainWithNullSML = this.lookups.cachedDomainList.filter(function(dmn) {
        return !dmn.smlSubdomain;
      })[0];

      if(!domainWithNullSML) {
        this.dialogRef.close(true);
      } else {
        this.domainForm.controls['smlSubdomain'].setErrors({'blankDomainError': true});
      }

    } else {
      this.dialogRef.close(true);
    }
  }

  checkValidity(g: FormGroup) {
    Object.keys(g.controls).forEach(key => {
      g.get(key).markAsDirty();
    });
    Object.keys(g.controls).forEach(key => {
      g.get(key).markAsTouched();
    });
    //!!! updateValueAndValidity - else some filed did no update current / on blur never happened
    Object.keys(g.controls).forEach(key => {
      g.get(key).updateValueAndValidity();
    });


  }

  public getCurrent(): DomainRo {

    if (!this.editMode) {
      this.current.domainCode = this.domainForm.value['domainCode'];
      this.current.smlSubdomain = this.domainForm.value['smlSubdomain'];
    }
    this.current.smlSmpId = this.domainForm.value['smlSmpId'];
    this.current.smlClientCertHeader = this.domainForm.value['smlClientCertHeader'];
    if (this.domainForm.value['smlClientKeyCertificate']) {
      this.current.smlClientKeyAlias = this.domainForm.value['smlClientKeyCertificate'].alias;
      this.current.smlClientCertHeader = this.domainForm.value['smlClientKeyCertificate'].blueCoatHeader;
    } else {
      this.current.smlClientKeyAlias = '';
      this.current.smlClientCertHeader = '';
    }
    this.current.signatureKeyAlias = this.domainForm.value['signatureKeyAlias'];
    this.current.smlBlueCoatAuth = this.domainForm.value['smlBlueCoatAuth'];

    return this.current;

  }

  updateDomainCode(event) {
    this.current.domainCode = event.target.value;
  }

  updateSmlDomain(event) {
    this.current.smlSubdomain = event.target.value;
  }

  updateSmlSmpId(event) {
    this.current.smlSmpId = event.target.value;
  }

  updateSmlClientKeyAlias(event) {
    this.current.smlClientKeyAlias = event.target.value;
  }

  updateSignatureKeyAlias(event) {
    this.current.signatureKeyAlias = event.target.value;
  }


  compareCertByAlias(cert, alias): boolean {
    return cert.alias === alias;
  }

  compareCertificate(certificate: CertificateRo, alias: String): boolean {
    return certificate.alias === alias;
  }

}
