import {AfterViewInit, Component, QueryList, ViewChild, ViewChildren,} from '@angular/core';
import {CredentialRo} from "../../security/credential.model";
import {ConfirmationDialogComponent} from "../../common/dialogs/confirmation-dialog/confirmation-dialog.component";
import {MatDialog} from "@angular/material/dialog";
import {EntityStatus} from "../../common/enums/entity-status.enum";
import {CertificateDialogComponent} from "../../common/dialogs/certificate-dialog/certificate-dialog.component";
import {CredentialDialogComponent} from "../../common/dialogs/credential-dialog/credential-dialog.component";
import {BeforeLeaveGuard} from "../../window/sidenav/navigation-on-leave-guard";
import {UserCertificatePanelComponent} from "./user-certificate-panel/user-certificate-panel.component";
import {HttpErrorHandlerService} from "../../common/error/http-error-handler.service";
import {UserService} from "../../common/services/user.service";
import {MatTableDataSource} from "@angular/material/table";
import {MatPaginator} from "@angular/material/paginator";
import {TranslateService} from "@ngx-translate/core";
import {lastValueFrom} from "rxjs";


@Component({
  templateUrl: './user-certificates.component.html',
  styleUrls: ['./user-certificates.component.scss']
})
export class UserCertificatesComponent implements AfterViewInit, BeforeLeaveGuard {
  displayedColumns: string[] = ['certificates'];
  dataSource: MatTableDataSource<CredentialRo> = new MatTableDataSource();
  certificates: CredentialRo[] = [];

  @ViewChild(MatPaginator)
  paginator: MatPaginator;

  @ViewChildren(UserCertificatePanelComponent)
  userCertificateCredentialComponents: QueryList<UserCertificatePanelComponent>;

  constructor(private httpErrorHandlerService: HttpErrorHandlerService,
              private userService: UserService,
              public dialog: MatDialog,
              private translateService: TranslateService) {


    this.userService.onCertificateCredentiasUpdateSubject().subscribe((credentials: CredentialRo[]) => {
      this.updateCredentials(credentials);
    });

    this.userService.onCertificateCredentialUpdateSubject().subscribe((credential: CredentialRo) => {
      this.updateCredential(credential);
    });
    this.userService.getUserCertificateCredentials();
  }

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
  }

  public updateCredentials(certificates: CredentialRo[]) {
    this.certificates = certificates;
    this.dataSource.data = this.certificates;
  }

  public updateCredential(certificate: CredentialRo) {
    // remove the access token
    if (certificate.status == EntityStatus.REMOVED) {
      this.certificates = this.certificates.filter(item => item.credentialId !== certificate.credentialId)
    }
    if (certificate.status == EntityStatus.UPDATED) {
      // update value in the array
      let itemIndex = this.certificates.findIndex(item => item.credentialId == certificate.credentialId);
      this.certificates[itemIndex] = certificate;
    }
    if (certificate.status == EntityStatus.NEW) {
      // update value in the array

      this.certificates = [
        ...this.certificates,
        certificate];
    }

    // show current page after update if possible or previous page
    let pageIndex = Math.min(this.paginator.pageIndex,
      Math.floor(this.certificates.length / this.paginator.pageSize));
    // set data
    this.dataSource.data = this.certificates;
    // set page
    this.paginator.pageIndex = pageIndex;
    this.paginator.lastPage();
  }

  public trackListItem(index: number, credential: CredentialRo) {
    return credential.credentialId;
  }

  public async onDeleteItemClicked(credential: CredentialRo) {
    this.dialog.open(ConfirmationDialogComponent, {
      data: {
        title: await lastValueFrom(this.translateService.get("user.certificates.delete.confirmation.dialog.title")),
        description: await lastValueFrom(this.translateService.get("user.certificates.delete.confirmation.dialog.description", {credentialName: credential.name}))
      }
    }).afterClosed().subscribe(result => {
      if (result) {
        this.delete(credential);
      }
    })
  }

  public async createNew() {
    this.dialog.open(CredentialDialogComponent, {
      data: {
        credentialType: CredentialDialogComponent.CERTIFICATE_TYPE,
        formTitle: await lastValueFrom(this.translateService.get("user.certificates.credentials.dialog.title"))
      }
    }).afterClosed();

  }

  public onShowItemClicked(credential: CredentialRo) {
    this.userService.getUserCertificateCredentialObservable(credential)
      .subscribe((response: CredentialRo) => {
        this.dialog.open(CertificateDialogComponent, {
          data: {row: response.certificate}
        });

      }, error => {
        if (this.httpErrorHandlerService.logoutOnInvalidSessionError(error)) {
          return;
        }
      });
  }


  public async onSaveItemClicked(credential: CredentialRo) {

    this.dialog.open(ConfirmationDialogComponent, {
      data: {
        title: await lastValueFrom(this.translateService.get("user.certificates.update.confirmation.dialog.title")),
        description: await lastValueFrom(this.translateService.get("user.certificates.update.confirmation.dialog.description", {credentialName: credential.name}))
      }
    }).afterClosed().subscribe(result => {
      if (result) {
        this.update(credential);
      }
    })
  }

  private delete(credential: CredentialRo) {
    this.userService.deleteUserCertificateCredential(credential);
  }

  private update(credential: CredentialRo) {
    this.userService.updateUserCertificateCredential(credential);
  }

  isDirty(): boolean {
    let dirtyComp = !this.userCertificateCredentialComponents ? null : this.userCertificateCredentialComponents.find(cmp => cmp.isDirty())
    return !!dirtyComp;
  }
}

