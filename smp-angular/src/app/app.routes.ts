import {RouterModule, Routes} from '@angular/router';
import {LoginComponent} from './login/login.component';
import {
  ResourceSearchComponent
} from './resource-search/resource-search.component';
import {
  PropertyComponent
} from "./system-settings/admin-properties/property.component";
import {
  UserProfileComponent
} from "./user-settings/user-profile/user-profile.component";
import {authenticationGuard} from "./guards/authentication.guard";
import {
  UserAccessTokensComponent
} from "./user-settings/user-access-tokens/user-access-tokens.component";
import {
  UserCertificatesComponent
} from "./user-settings/user-certificates/user-certificates.component";
import {
  ExtensionComponent
} from "./system-settings/admin-extension/extension.component";
import {
  AdminTruststoreComponent
} from "./system-settings/admin-truststore/admin-truststore.component";
import {
  AdminKeystoreComponent
} from "./system-settings/admin-keystore/admin-keystore.component";
import {
  AdminDomainComponent
} from "./system-settings/admin-domain/admin-domain.component";
import {dirtyDeactivateGuard} from "./guards/dirty.guard";
import {
  AdminUserComponent
} from "./system-settings/admin-users/admin-user.component";
import {EditDomainComponent} from "./edit/edit-domain/edit-domain.component";
import {EditGroupComponent} from "./edit/edit-group/edit-group.component";
import {
  EditResourceComponent
} from "./edit/edit-resources/edit-resource.component";
import {
  ResourceDocumentPanelComponent
} from "./edit/edit-resources/resource-document-panel/resource-document-panel.component";
import {
  SubresourceDocumentPanelComponent
} from "./edit/edit-resources/subresource-document-panel/subresource-document-panel.component";
import {
  authorizeChildSystemAdminGuard
} from "./guards/authorize-child-system-admin.guard";
import {
  activateChildResourceGuard
} from "./guards/activate-child-document.guard";
import {
  UserAlertsComponent
} from "./user-settings/user-alerts/user-alerts.component";
import {
  AdminAlertsComponent
} from "./system-settings/admin-alerts/admin-alerts.component";
import {
  ResetCredentialComponent
} from "./security/reset-credential/reset-credential.component";
import {DnsToolsComponent} from "./tools/dns-tools/dns-tools.component";
import {ReviewTasksComponent} from "./edit/review-task/review-tasks.component";
import {
  ReviewDocumentPanelComponent
} from "./common/panels/review-tasks-panel/review-document-panel/review-document-panel.component";
import {activateChildReviewGuard} from "./guards/activate-child-review.guard";


const appRoutes: Routes = [

  {path: '', component: ResourceSearchComponent},
  {path: 'search', redirectTo: ''},
  {path: 'public/dns-tools', component: DnsToolsComponent},
  {path: 'login', component: LoginComponent},
  {path: 'reset-credential/:resetToken', component: ResetCredentialComponent},
  {
    path: 'edit',
    canActivateChild: [authenticationGuard],
    children: [
      {
        path: 'edit-domain',
        component: EditDomainComponent,
        canDeactivate: [dirtyDeactivateGuard]
      },
      {
        path: 'edit-group',
        component: EditGroupComponent,
        canDeactivate: [dirtyDeactivateGuard]
      },
      {
        path: 'edit-resource',
        canDeactivate: [dirtyDeactivateGuard],
        children: [
          {
            path: 'resource-document',
            canActivate: [activateChildResourceGuard],
            component: ResourceDocumentPanelComponent,
            canDeactivate: [dirtyDeactivateGuard]
          },
          {
            path: 'subresource-document',
            canActivate: [activateChildResourceGuard],
            component: SubresourceDocumentPanelComponent,
            canDeactivate: [dirtyDeactivateGuard]
          },
          {
            path: '',
            component: EditResourceComponent,
            canDeactivate: [dirtyDeactivateGuard]
          },
        ]
      },
      {
        path: 'review-tasks',
        children: [
          {
            path: 'review-document',
            canActivate: [activateChildReviewGuard],
            component: ReviewDocumentPanelComponent,
            canDeactivate: [dirtyDeactivateGuard]
          },
          {path: '', component: ReviewTasksComponent},]
      }
    ]
  },
  {
    path: 'system-settings',
    canActivateChild: [authenticationGuard, authorizeChildSystemAdminGuard],
    children: [
      {
        path: 'domain',
        component: AdminDomainComponent,
        canDeactivate: [dirtyDeactivateGuard]
      },
      {
        path: 'user',
        component: AdminUserComponent,
        canDeactivate: [dirtyDeactivateGuard]
      },
      {
        path: 'properties',
        component: PropertyComponent,
        canDeactivate: [dirtyDeactivateGuard]
      },
      {
        path: 'keystore',
        component: AdminKeystoreComponent,
        canDeactivate: [dirtyDeactivateGuard]
      },
      {
        path: 'truststore',
        component: AdminTruststoreComponent,
        canDeactivate: [dirtyDeactivateGuard]
      },
      {
        path: 'extension',
        component: ExtensionComponent,
        canDeactivate: [dirtyDeactivateGuard]
      },
      {
        path: 'alert',
        component: AdminAlertsComponent,
        canDeactivate: [dirtyDeactivateGuard]
      },
    ]
  },
  {
    path: 'user-settings',
    canActivateChild: [authenticationGuard],
    children: [
      {
        path: 'user-profile',
        component: UserProfileComponent,
        canDeactivate: [dirtyDeactivateGuard]
      },
      {
        path: 'user-access-token',
        component: UserAccessTokensComponent,
        canDeactivate: [dirtyDeactivateGuard]
      },
      {
        path: 'user-certificate',
        component: UserCertificatesComponent,
        canDeactivate: [dirtyDeactivateGuard]
      },
      {
        path: 'user-alert',
        component: UserAlertsComponent,
        canDeactivate: [dirtyDeactivateGuard]
      },
      {
        path: 'user-membership',
        component: UserProfileComponent,
        canDeactivate: [dirtyDeactivateGuard]
      },
    ]
  },
  {path: '**', redirectTo: ''},
];

export const routing = RouterModule.forRoot(appRoutes, {useHash: true});
