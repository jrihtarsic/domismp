import {AfterViewInit, Component, QueryList, ViewChild, ViewChildren,} from '@angular/core';
import {CredentialRo} from "../../security/credential.model";
import {ConfirmationDialogComponent} from "../../common/dialogs/confirmation-dialog/confirmation-dialog.component";
import {MatDialog} from "@angular/material/dialog";
import {EntityStatus} from "../../common/enums/entity-status.enum";
import {CredentialDialogComponent} from "../../common/dialogs/credential-dialog/credential-dialog.component";
import {BeforeLeaveGuard} from "../../window/sidenav/navigation-on-leave-guard";
import {AccessTokenPanelComponent} from "./access-token-panel/access-token-panel.component";
import {MatPaginator} from "@angular/material/paginator";
import {MatTableDataSource} from "@angular/material/table";
import {UserService} from "../../common/services/user.service";
import {TranslateService} from "@ngx-translate/core";
import {lastValueFrom} from "rxjs";

@Component({
  templateUrl: './user-access-tokens.component.html',
  styleUrls: ['./user-access-tokens.component.scss']
})
export class UserAccessTokensComponent implements AfterViewInit, BeforeLeaveGuard {
  displayedColumns: string[] = ['accessTokens'];
  dataSource: MatTableDataSource<CredentialRo> = new MatTableDataSource();
  accessTokens: CredentialRo[] = [];

  @ViewChildren(AccessTokenPanelComponent)
  userTokenCredentialComponents: QueryList<AccessTokenPanelComponent>;

  @ViewChild(MatPaginator)
  paginator: MatPaginator;

  constructor(private userService: UserService,
              public dialog: MatDialog,
              private translateService: TranslateService) {
    this.userService.onAccessTokenCredentialsUpdateSubject().subscribe((credentials: CredentialRo[]) => {
      this.updateAccessTokenCredentials(credentials);
    });

    this.userService.onAccessTokenCredentialUpdateSubject().subscribe((credential: CredentialRo) => {
      this.updateAccessTokenCredential(credential);
    });

    this.userService.getUserAccessTokenCredentials();
  }

  public updateAccessTokenCredentials(userAccessTokens: CredentialRo[]) {
    this.accessTokens = userAccessTokens;
    this.dataSource.data = this.accessTokens;
  }

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
  }

  public updateAccessTokenCredential(userAccessToken: CredentialRo) {
    // remove the access token
    if (userAccessToken.status == EntityStatus.REMOVED) {
      this.accessTokens = this.accessTokens.filter(item => item.credentialId !== userAccessToken.credentialId)
    }
    if (userAccessToken.status == EntityStatus.UPDATED) {
      // update value in the array
      let itemIndex = this.accessTokens.findIndex(item => item.credentialId == userAccessToken.credentialId);
      this.accessTokens[itemIndex] = userAccessToken;
    }
    if (userAccessToken.status == EntityStatus.NEW) {
      // update value in the array

      this.accessTokens = [
        ...this.accessTokens,
        userAccessToken];
    }

    // show current page after update if possible or previous page
    let pageIndex = Math.min(this.paginator.pageIndex,
      Math.floor(this.accessTokens.length / this.paginator.pageSize));
    // set the data source
    this.dataSource.data = this.accessTokens;
    this.paginator.pageIndex = pageIndex;
  }

  public trackListItem(index: number, credential: CredentialRo) {
    return credential.credentialId;
  }

  public async onDeleteItemClicked(credential: CredentialRo) {
    this.dialog.open(ConfirmationDialogComponent, {
      data: {
        title: await lastValueFrom(this.translateService.get("user.access.tokens.delete.confirmation.dialog.title")),
        description: await lastValueFrom(this.translateService.get("user.access.tokens.delete.confirmation.dialog.description", {credentialName: credential.name}))
      }
    }).afterClosed().subscribe(result => {
      if (result) {
        this.deleteAccessToken(credential);
      }
    })
  }

  public async createNewAccessToken() {
    this.dialog.open(CredentialDialogComponent, {
      data: {
        credentialType: CredentialDialogComponent.ACCESS_TOKEN_TYPE,
        formTitle: await lastValueFrom(this.translateService.get("user.access.tokens.credentials.dialog.title"))
      }
    }).afterClosed();
  }

  public async onSaveItemClicked(credential: CredentialRo) {
    this.dialog.open(ConfirmationDialogComponent, {
      data: {
        title: await lastValueFrom(this.translateService.get("user.access.tokens.update.confirmation.dialog.title")),
        description: await lastValueFrom(this.translateService.get("user.access.tokens.update.confirmation.dialog.description", {credentialName: credential.name}))
      }
    }).afterClosed().subscribe(result => {
      if (result) {
        this.updateAccessToken(credential);
      }
    })
  }

  private deleteAccessToken(credential: CredentialRo) {
    this.userService.deleteUserAccessTokenCredential(credential);
  }

  private updateAccessToken(credential: CredentialRo) {
    this.userService.updateUserAccessTokenCredential(credential);
  }


  isDirty(): boolean {
    let dirtyComp = !this.userTokenCredentialComponents ? null : this.userTokenCredentialComponents.find(cmp => cmp.isDirty())
    return !!dirtyComp;
  }
}

