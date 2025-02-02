import {Injectable, Input} from '@angular/core';
import {GroupRo} from "../../common/model/group-ro.model";
import {ResourceRo} from "../../common/model/resource-ro.model";
import {TableResult} from "../../common/model/table-result.model";
import {DomainRo} from "../../common/model/domain-ro.model";
import {
  ResourceDefinitionRo
} from "../../system-settings/admin-extension/resource-definition-ro.model";
import {EditResourceService} from "./edit-resource.service";
import {EditDomainService} from "../edit-domain/edit-domain.service";
import {EditGroupService} from "../edit-group/edit-group.service";
import {
  AlertMessageService
} from "../../common/alert-message/alert-message.service";
import {MatTableDataSource} from "@angular/material/table";
import {SecurityEventService} from "../../security/security-event.service";

/**
 * The purpose of the EditResourceController is to  control the data of edit resource components when navigating
 * between subpages such as EditResourceComponent, ResourceDocumentPanelComponent and SubresourceDocumentPanelComponent.
 *
 * @author Joze Rihtarsic
 * @since 5.1
 */
@Injectable()
export class EditResourceController extends MatTableDataSource<ResourceRo> {

  domainList: DomainRo[] = [];
  groupList: GroupRo[] = [];

  // data changed indicates the cached data may be outdated and need to be refreshed
  _dataChanged: boolean = false;
  _isLoadingResults:boolean = false;

  _selectedDomain: DomainRo;
  _selectedGroup: GroupRo;
  _selectedResource: ResourceRo;
  _selectedDomainResourceDefs: ResourceDefinitionRo[];

  resourcesFilter: any = {};

  dataLength:number = 0;
  pageIndex:number = 0;
  pageSize:number = 10;



  constructor(
    private domainService: EditDomainService,
    private groupService: EditGroupService,
    private resourceService: EditResourceService,
    private alertService: AlertMessageService,
    private securityEventService: SecurityEventService
  ) {
    super();
    this.securityEventService.onLogoutSuccessEvent().subscribe(value => {
      this.clearSelectedData();
    });
    this.securityEventService.onLoginSuccessEvent().subscribe(value => {
      this.clearSelectedData();
    });
  }

  // this flag is used to trigger data refresh when data is needed.
  //The data can be changed for the user when adding/creating new resource in the edit group
  @Input() set dataChanged(value: boolean) {
    this._dataChanged = value;
  }

  get dataChanged(): boolean {
    return this._dataChanged;
  }

  // this flag is used to trigger data refresh when data is needed.
  //The data can be changed for the user when adding/creating new resource in the edit group
  @Input() set isLoadingResults(value: boolean) {
    if (!value) {
      // data was loaded, and we can reset the flag for data changed
      this._dataChanged = value
    }
    this._isLoadingResults = value;
  }

  get isLoadingResults(): boolean {
    return this._isLoadingResults;
  }

  private clearSelectedData() {
    this.domainList = [];
    this.groupList = [];
    this._selectedDomain = null;
    this._selectedGroup = null;
    this._selectedResource = null;
    this._selectedDomainResourceDefs = [];
    this.resourcesFilter = {};
    this.isLoadingResults = false;
    this.updateResourceList([], 0, -1, -1);
  }

  get selectedDomain(): DomainRo {
    return this._selectedDomain;
  };

  @Input() set selectedDomain(domain: DomainRo) {
    this._selectedDomain = domain;
    if (!!this.selectedDomain || this.dataChanged) {
      this.refreshGroups();
      this.refreshDomainsResourceDefinitions();
    } else {
      this.isLoadingResults = false;
      this.groupList = [];
      this._selectedDomainResourceDefs = [];
    }
  };

  get selectedGroup(): GroupRo {
    return this._selectedGroup;
  };

  @Input() set selectedGroup(resource: GroupRo) {
    this._selectedGroup = resource;
    if (!!this._selectedGroup || this.dataChanged ) {
      this.refreshResources();
    } else {
      this.isLoadingResults = false;
      this.updateResourceList([], 0, -1, -1);
    }
  };

  get selectedResource(): ResourceRo {
    return this._selectedResource;
  };

  selectedResourceUpdated(resource: ResourceRo) {
    // find current resource from list by resource id
    if (resource.resourceId == this._selectedResource.resourceId) {
      this._selectedResource.resourceTypeIdentifier = resource.resourceTypeIdentifier;
      this._selectedResource.identifierScheme = resource.identifierScheme;
      this._selectedResource.identifierValue = resource.identifierValue;
      this._selectedResource.visibility = resource.visibility;
      this._selectedResource.reviewEnabled = resource.reviewEnabled
    } else {
      console.log('selected resource not found')
      this.selectedResource = resource;
    }
  }

  @Input() set selectedResource(resource: ResourceRo) {
    this._selectedResource = resource;
  };

  /**
   * Method refreshes data when data changed
   */
  refreshDataOnDataChange() {
    if (this.dataChanged) {
      this.refreshDomains();
    }
  }


  refreshDomains() {
    this.isLoadingResults = true;
    this.domainService.getDomainsForResourceAdminUserObservable()
      .subscribe({
        next: (result: DomainRo[]) => {
          this.updateDomainList(result)
        }, error: (err: any) => {
          this.alertService.error(err.error?.errorDescription)
          this.isLoadingResults = false;
        }
      });
  }

  refreshGroups() {
    if (!this.selectedDomain) {
      this.updateGroupList([]);

      this.isLoadingResults = false;
      return;
    }
    this.isLoadingResults = true;
    this.groupService.getDomainGroupsForResourceAdminObservable(this.selectedDomain)
      .subscribe({
        next: (result: GroupRo[]) => {
          this.updateGroupList(result)
        }, error: (error: any) => {
          this.isLoadingResults = false;
          this.alertService.error(error.error?.errorDescription)
        }
      });
  }

  refreshResources() {
    if (!this._selectedGroup) {
      this.isLoadingResults = false;
      this.updateResourceList([], 0, -1, -1);
      return;
    }
    this.isLoadingResults = true;
    this.resourceService.getGroupResourcesForResourceAdminObservable(this.selectedGroup, this.selectedDomain,
      this.resourcesFilter, this.pageIndex, this.pageSize)
      .subscribe({
        next: (result: TableResult<ResourceRo>) => {
          this.updateResourceList(result.serviceEntities, result.count,  result.page, result.pageSize);
          this.isLoadingResults = false;
        }, error: (error: any) => {
          this.isLoadingResults = false;
          this.alertService.error(error.error?.errorDescription)
        }
      });
  }

  refreshDomainsResourceDefinitions() {
    this.domainService.getDomainResourceDefinitionsObservable(this.selectedDomain)
      .subscribe({
        next: (result: ResourceDefinitionRo[]) => {
          this._selectedDomainResourceDefs = result
        }, error: (error: any) => {
          this.alertService.error(error.error?.errorDescription)
        }
      });
  }

  updateDomainList(list: DomainRo[]) {
    this.domainList = list;
    if (!!this.domainList && this.domainList.length > 0) {
      this.selectedDomain = this.domainList[0];
    } else {
      this._dataChanged = false;
      this.isLoadingResults = false;
    }
  }

  updateGroupList(list: GroupRo[]) {
    this.groupList = list
    if (!!this.groupList && this.groupList.length > 0) {
      this.selectedGroup = this.groupList[0];
    } else {
      this._dataChanged = false;
      this.isLoadingResults = false;
    }
  }

  updateResourceList(list: ResourceRo[], totalDataSize: number = 0, pageIndex: number = -1, pageSize: number = -1) {
    let currR: ResourceRo = this.selectedResource;
    this.selectedResource = null;
    this.data = list;

    this.dataLength = totalDataSize;
    if (pageIndex !== -1) {
      this.pageIndex = pageIndex;
    }
    if (pageSize !== -1) {
      this.pageSize = pageSize;
    }

    if (!!currR) {
      this.selectedResource = list.find(r =>
        r.identifierScheme == currR.identifierScheme &&
        r.identifierValue == currR.identifierValue);
    }

    if (!this.selectedResource && !!list && list.length > 0) {
      this.selectedResource = list[0];
    }
  }

  /**
   * Apply filter to resources
   * @param filterValue - filter value
   */
  applyResourceFilter(filterValue: string) {
    this.resourcesFilter["filter"] =
      !filterValue ? '' : filterValue.trim().toLowerCase();
    this.refreshResources();
  }

  applyResourcePage(pageIndex: number, pageSize: number) {
    this.pageSize = pageSize;
    this.pageIndex = pageIndex;
    this.refreshResources();
  }

  get selectedResourceDefinition(): ResourceDefinitionRo {

    if (!this._selectedResource) {
      return null;
    }
    if (!this._selectedDomainResourceDefs) {
      return null;
    }

    return this._selectedDomainResourceDefs.find(def => def.identifier == this._selectedResource.resourceTypeIdentifier)
  }

}
