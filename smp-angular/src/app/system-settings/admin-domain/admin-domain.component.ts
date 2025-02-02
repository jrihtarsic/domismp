import {
  AfterViewInit,
  Component,
  OnDestroy,
  OnInit,
  ViewChild
} from '@angular/core';
import {MatTableDataSource} from "@angular/material/table";
import {MatPaginator} from "@angular/material/paginator";
import {MatSort} from "@angular/material/sort";
import {AdminDomainService} from "./admin-domain.service";
import {
  AlertMessageService
} from "../../common/alert-message/alert-message.service";
import {
  ConfirmationDialogComponent
} from "../../common/dialogs/confirmation-dialog/confirmation-dialog.component";
import {MatDialog} from "@angular/material/dialog";
import {EntityStatus} from "../../common/enums/entity-status.enum";
import {DomainRo} from "../../common/model/domain-ro.model";
import {AdminKeystoreService} from "../admin-keystore/admin-keystore.service";
import {BeforeLeaveGuard} from "../../window/sidenav/navigation-on-leave-guard";
import {
  ResourceDefinitionRo
} from "../admin-extension/resource-definition-ro.model";
import {ExtensionService} from "../admin-extension/extension.service";
import {ExtensionRo} from "../admin-extension/extension-ro.model";
import {MatTabGroup} from "@angular/material/tabs";
import {
  CancelDialogComponent
} from "../../common/dialogs/cancel-dialog/cancel-dialog.component";
import {DomainPanelComponent} from "./domain-panel/domain-panel.component";
import {
  DomainResourceTypePanelComponent
} from "./domain-resource-type-panel/domain-resource-type-panel.component";
import {
  DomainSmlIntegrationPanelComponent
} from "./domain-sml-panel/domain-sml-integration-panel.component";
import {MemberTypeEnum} from "../../common/enums/member-type.enum";
import {firstValueFrom, lastValueFrom, Subscription} from "rxjs";
import {VisibilityEnum} from "../../common/enums/visibility.enum";
import {CertificateRo} from "../../common/model/certificate-ro.model";
import {GlobalLookups} from "../../common/global-lookups";
import {TranslateService} from "@ngx-translate/core";


@Component({
  templateUrl: './admin-domain.component.html',
  styleUrls: ['./admin-domain.component.css']
})
export class AdminDomainComponent implements OnInit, OnDestroy, AfterViewInit, BeforeLeaveGuard {
  readonly membershipType: MemberTypeEnum = MemberTypeEnum.DOMAIN;
  displayedColumns: string[] = ['domainCode'];
  dataSource: MatTableDataSource<DomainRo> = new MatTableDataSource();
  selected?: DomainRo;
  domainList: DomainRo[] = [];
  keystoreCertificates: CertificateRo[] = [];
  domiSMPResourceDefinitions: ResourceDefinitionRo[] = [];

  currenTabIndex: number = 0;
  handleTabClick = null;

  private domainUpdatedEventSub: Subscription = Subscription.EMPTY;
  private domainEntryUpdatedEventSub: Subscription = Subscription.EMPTY;


  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild(MatSort) sort: MatSort;

  @ViewChild('domainPanelComponent') domainPanelComponent: DomainPanelComponent;
  @ViewChild('domainResourceTypePanelComponent') domainResourceTypePanelComponent: DomainResourceTypePanelComponent;
  @ViewChild('domainSmlIntegrationPanelComponent') domainSmlIntegrationPanelComponent: DomainSmlIntegrationPanelComponent;


  @ViewChild('domainTabs') domainTabs: MatTabGroup;

  constructor(private domainService: AdminDomainService,
              private keystoreService: AdminKeystoreService,
              private extensionService: ExtensionService,
              protected lookups: GlobalLookups,
              private alertService: AlertMessageService,
              private dialog: MatDialog,
              private translateService: TranslateService) {

    this.domainUpdatedEventSub = domainService.onDomainUpdatedEvent()
      .subscribe((updateDomainList: DomainRo[]): void => {
          this.updateDomainList(updateDomainList);
        }
      );

    this.domainEntryUpdatedEventSub = domainService.onDomainEntryUpdatedEvent()
      .subscribe((updateEntry: DomainRo): void => {
          this.updateDomain(updateEntry);
        }
      );

    keystoreService.onKeystoreUpdatedEvent().subscribe(keystoreCertificates => {
        this.keystoreCertificates = keystoreCertificates;
      }
    );
    extensionService.onExtensionsUpdatesEvent().subscribe(updatedExtensions => {
        this.updateExtensions(updatedExtensions);
      }
    );

    extensionService.getExtensions();
    domainService.getDomains();
    keystoreService.getKeystoreData();
  }

  ngOnDestroy(): void {
    this.domainUpdatedEventSub.unsubscribe();
    this.domainEntryUpdatedEventSub.unsubscribe();
  }

  updateExtensions(extensions: ExtensionRo[]): void {
    let allResourceDefinition: ResourceDefinitionRo[] = [];
    extensions.forEach(ext => allResourceDefinition.push(...ext.resourceDefinitions))
    this.domiSMPResourceDefinitions = allResourceDefinition;
  }

  ngOnInit(): void {
    // filter predicate for search the domain
    this.dataSource.filterPredicate =
      (data: DomainRo, filter: string) => {
        return !filter || -1 != data.domainCode.toLowerCase().indexOf(filter.trim().toLowerCase())
      };
  }

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
    // currently  MatTab has only onTabChanged which is a bit to late. Register new listener to  internal
    // _handleClick handler
    this.registerTabClick();
  }

  registerTabClick(): void {
    if (!this.domainTabs) {
      return;
    }
    // Get the handler reference
    this.handleTabClick = this.domainTabs._handleClick;

    this.domainTabs._handleClick = (tab, header, newTabIndex) => {

      if (newTabIndex == this.currenTabIndex) {
        return;
      }

      if (this.isCurrentTabDirty()) {
        let canChangeTab = firstValueFrom(this.dialog.open(CancelDialogComponent).afterClosed());
        canChangeTab.then((canChange: boolean) => {
          if (canChange) {
            // reset
            this.resetCurrentTabData()
            this.handleTabClick.apply(this.domainTabs, [tab, header, newTabIndex]);
            this.currenTabIndex = newTabIndex;
            if (this.isNewDomain()) {
              this.selected = null;
            }
          }
        });
      } else {
        this.handleTabClick.apply(this.domainTabs, [tab, header, newTabIndex]);
        this.currenTabIndex = newTabIndex;
      }
    }
  }

  updateDomainList(domainList: DomainRo[]) {
    this.domainList = domainList
    this.dataSource.data = this.domainList;
  }

  async updateDomain(domain: DomainRo) {
    if (domain == null) {
      return;
    }

    if (domain.status == EntityStatus.NEW) {
      this.domainList.push(domain)
      this.selected = domain;
      this.alertService.success(await lastValueFrom(this.translateService.get("admin.domain.success.create", {domainCode: domain.domainCode})));
    } else if (domain.status == EntityStatus.UPDATED) {
      // update value in the array
      let itemIndex = this.domainList.findIndex(item => item.domainId == domain.domainId);
      this.domainList[itemIndex] = domain;
      this.selected = domain;
    } else if (domain.status == EntityStatus.REMOVED) {
      this.alertService.success(await lastValueFrom(this.translateService.get("admin.domain.success.remove", {domainCode: domain.domainCode})));
      this.selected = null;
      this.domainList = this.domainList.filter(item => item.domainCode !== domain.domainCode)
    } else if (domain.status == EntityStatus.ERROR) {
      this.alertService.error(await lastValueFrom(this.translateService.get("admin.domain.error", {actionMessage: domain.actionMessage})));
    }
    this.dataSource.data = this.domainList;

    if (domain.status == EntityStatus.NEW) {
      this.paginator.lastPage();
    }
  }

  applyDomainFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();

    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  resetUnsavedDataValidation() {
    // current tab not changed - OK to change it
    if (!this.isCurrentTabDirty()) {
      return true;
    }

    let canChangeTab = firstValueFrom(this.dialog.open(CancelDialogComponent).afterClosed());
    canChangeTab.then((canChange: boolean) => {
      if (canChange) {
        // reset
        this.resetCurrentTabData()
      }
    });
  }

  onCreateDomainClicked() {
    this.selected = this.newDomain();
    if (!this.handleTabClick) {
      this.registerTabClick();
    }
    if (!!this.domainTabs) {
      this.domainTabs.selectedIndex = 0;
      this.domainPanelComponent.setFocus();
    }
  }

  public newDomain(): DomainRo {
    return {
      index: null,
      visibility: VisibilityEnum.Public,
      domainCode: '',
      smlSubdomain: '',
      smlSmpId: '',
      smlParticipantIdentifierRegExp: '',
      smlClientKeyAlias: '',
      signatureKeyAlias: '',
      status: EntityStatus.NEW,
      smlRegistered: false,
      smlClientCertAuth: false,
      adminMemberCount: 0,
    }
  }

  onSaveEvent(domain: DomainRo) {
    if (this.isNewDomain()) {
      this.domainService.createDomain(domain);
    } else {
      this.domainService.updateDomain(domain);
    }
  }

  onDiscardNew() {
    this.selected = null;
  }

  onSaveResourceTypesEvent(domain: DomainRo) {
    this.domainService.updateDomainResourceTypes(domain);
  }

  onSaveSmlIntegrationDataEvent(domain: DomainRo) {
    this.domainService.updateDomainSMLIntegrationData(domain);

  }

  onSavePropertiesDataEvent(domain: DomainRo) {
    this.domainService.updateDomainData(domain);
  }


  onDeleteSelectedDomainClicked() {
    this.dialog.open(ConfirmationDialogComponent, {
      data: {
        title: "Delete domain [" + this.selected.domainCode + "] from DomiSMP",
        description: "Action will permanently delete domain! <br/><br/>Do you wish to continue?"
      }
    }).afterClosed().subscribe(result => {
      if (result) {
        this.deleteDomain(this.selected);
      }
    });
  }

  deleteDomain(domain: DomainRo) {
    this.domainService.deleteDomain(domain);
  }

  public domainSelected(domainSelected: DomainRo) {
    if (domainSelected && !this.handleTabClick) {
      this.registerTabClick();
    }

    if (this.selected == domainSelected) {
      return;
    }
    if (this.isCurrentTabDirty()) {
      let canChangeTab = firstValueFrom(this.dialog.open(CancelDialogComponent).afterClosed());
      canChangeTab.then((canChange: boolean) => {
        if (canChange) {
          // reset
          this.resetCurrentTabData();
          this.selected = domainSelected;
        }
      });
    } else {
      this.selected = domainSelected;
    }
  }


  isDirty(): boolean {
    return this.isCurrentTabDirty();
  }

  isCurrentTabDirty(): boolean {

    switch (this.currenTabIndex) {
      case 0:
        return this.domainPanelComponent?.isDirty();
      case 1:
        return this.domainResourceTypePanelComponent?.isDirty();
      case 2:
        return this.domainSmlIntegrationPanelComponent?.isDirty();
    }
    return false;
  }

  /**
   * Method checks if domain entity is set and domainId does not exists.
   * @return true if domain is set and domainId does not exists otherwise false
   */
  isNewDomain(): boolean {
    if (!this.selected) {
      return false;
    }
    return !this.selected.domainId
  }


  resetCurrentTabData(): void {

    switch (this.currenTabIndex) {
      case 0:
        this.domainPanelComponent.onResetButtonClicked();
        break;
      case 1:
        this.domainPanelComponent.onResetButtonClicked();
        break
      case 2:
        this.domainSmlIntegrationPanelComponent.onResetButtonClicked();
        break
    }
  }

  /**
   * The domain can not be deleted if it is  not selected or it is registered in the SML
   * or it is new domain
   */
  get canNotDelete(): boolean {
    return !this.selected || this.isNewDomain() || this.isSelectedSMPRegister ;

  }

  /**
   *  Method returns true if the SML integration is enabled and the domain is  registered
   *
   */
  get isSelectedSMPRegister(): boolean {
    return this.isSMLIntegrationEnabled && this.selected?.smlRegistered;
  }

  get isSMLIntegrationEnabled() {
    return !!this.lookups.cachedApplicationConfig?.smlIntegrationOn
  }

  get editMode(): boolean {
    return this.isCurrentTabDirty();
  }
}
