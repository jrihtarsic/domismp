import {SearchTableController} from '../search-table/search-table-controller';
import {MatDialog, MatDialogConfig, MatDialogRef} from '@angular/material/dialog';
import {UserRo} from '../model/user-ro.model';
import {EntityStatus} from '../enums/entity-status.enum';
import {GlobalLookups} from "../global-lookups";
import {SearchTableEntity} from "../search-table/search-table-entity.model";
import {SearchTableValidationResult} from "../search-table/search-table-validation-result.model";
import {SmpConstants} from "../../smp.constants";
import {HttpClient} from "@angular/common/http";

import {PasswordChangeDialogComponent} from "../dialogs/password-change-dialog/password-change-dialog.component";
import {ApplicationRoleEnum} from "../enums/application-role.enum";
import {CertificateRo} from "../model/certificate-ro.model";


export class UserController implements SearchTableController {

  nullCert:CertificateRo;
  compareUserProperties = ["username","password","emailAddress","active","role","certificate"];
  compareCertProperties = ["certificateId","subject","issuer","serialNumber","crlUrl","validFrom","validTo"];


  constructor(protected http: HttpClient, protected lookups: GlobalLookups, public dialog: MatDialog) {
    this.nullCert = this.newCertificateRo();
  }

  public showDetails(row): MatDialogRef<any> {
    return null;
  }

  public edit(row): MatDialogRef<any> {
    return null;
  }

  public delete(row: any) {
  }

  newDialog(config): MatDialogRef<any> {
    if (config && config.data && config.data.edit) {
      return this.edit(config);
    } else {
      return this.showDetails(config);
    }
  }
  public changePasswordDialog(config?: MatDialogConfig): MatDialogRef<PasswordChangeDialogComponent> {
    return this.dialog.open(PasswordChangeDialogComponent, this.convertWithMode(config));
  }

  private convertWithMode(config) {
    return (config && config.data)
      ? {
        ...config,
        data: {
          ...config.data,
        }
      }
      : config;
  }

  public newRow(): UserRo {
    return {
      id: null,
      userId:null,
      index: null,
      username: '',
      emailAddress: '',
      role: ApplicationRoleEnum.USER,
      active: true,
      status: EntityStatus.NEW,

    }
  }

  public dataSaved() {
    this.lookups.refreshUserLookup();
  }

  validateDeleteOperation(rows: Array<UserRo>) {
    var deleteRowIds = rows.map(rows => rows.userId);
    return this.http.post<SearchTableValidationResult>(SmpConstants.REST_INTERNAL_USER_VALIDATE_DELETE, deleteRowIds);
  }

  public newValidationResult(lst: Array<string>): SearchTableValidationResult {
    return {
      validOperation: false,
      stringMessage: null,
      listId: lst,
    }
  }

  isRowExpanderDisabled(row: SearchTableEntity): boolean {
    return false;
  }

  isCertificateChanged(oldCert, newCert): boolean {
    if (this.isNull(oldCert) && this.isNull(newCert)) {
      console.log("both null return false! ");
      return false;
    }

    if (this.isNull(oldCert)) {
      oldCert = this.nullCert;
    }

    if (this.isNull(newCert)) {
      newCert = this.nullCert;
    }

    return this.propertyChanged(oldCert, newCert, this.compareCertProperties);
  }

  isRecordChanged(oldModel, newModel): boolean {
    return this.propertyChanged(oldModel, newModel, this.compareUserProperties);
  }

  propertyChanged(oldModel, newModel, arrayProperties): boolean {


    let propSize = arrayProperties.length;
    for (let i = 0; i < propSize; i++) {

      let property = arrayProperties[i];
     if (property === 'certificate') {
        if (this.isCertificateChanged(oldModel[property], newModel[property])) {
          return true; // Property has changed
        }
      } else {
        const isEqual = this.isEqual(newModel[property], oldModel[property]);
        if (!isEqual) {
          console.log("property "+property+" is changed! ");
          return true; // Property has changed
        }
      }
    }
    return false;
  }

  isEqual(val1, val2): boolean {
    return (this.isEmpty(val1) && this.isEmpty(val2)
      || val1 === val2);
  }

  isEmpty(str): boolean {
    return (!str || 0 === str.length);
  }

  isNull(obj): boolean {
    return !obj
  }

  private newCertificateRo(): CertificateRo {
    return {
      subject: '',
      validFrom: null,
      validTo: null,
      issuer: '',
      serialNumber: '',
      certificateId: '',
      fingerprints: '',
    };
  }


}
