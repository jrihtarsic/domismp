﻿import {Injectable} from '@angular/core';
import {lastValueFrom, Observable, ReplaySubject} from 'rxjs';
import {User} from './user.model';
import {SecurityEventService} from './security-event.service';
import {HttpClient, HttpErrorResponse, HttpHeaders} from '@angular/common/http';
import {SmpConstants} from "../smp.constants";
import {Authority} from "./authority.model";
import {
  AlertMessageService
} from "../common/alert-message/alert-message.service";
import {
  PasswordChangeDialogComponent
} from "../common/dialogs/password-change-dialog/password-change-dialog.component";
import {MatDialog} from "@angular/material/dialog";
import {Router} from "@angular/router";
import {TranslateService} from "@ngx-translate/core";
import {WindowSpinnerService} from "../common/services/window-spinner.service";
import {SmpErrorCode} from "../common/enums/smp-error-code.enum";
import {LocalStorageService} from "../common/services/local-storage.service";
import {SmpInfo} from "../app-info/smp-info.model";

@Injectable()
export class SecurityService {


  public static readonly TIME_BEFORE_EXPIRATION_IN_SECONDS: number = 60;
  public static readonly DELAY_BEFORE_UI_SESSION_EXTENSION_IN_MS: number = 3000;
  public static readonly MAXIMUM_TIMEOUT_VALUE: number = 2147483647;
  readonly LOCAL_STORAGE_KEY_CURRENT_USER = 'currentUser';

  lastUIActivity: Date = new Date();
  lastUISessionCall: Date = new Date();

  constructor(
    private http: HttpClient,
    private alertService: AlertMessageService,
    private securityEventService: SecurityEventService,
    private dialog: MatDialog,
    private router: Router,
    private translateService: TranslateService,
    private windowSpinnerService: WindowSpinnerService,
    private localStorageService: LocalStorageService
  ) {
    this.securityEventService.onLogoutSuccessEvent().subscribe(() => {
      this.dialog.closeAll();
      this.router.navigateByUrl('/');
    });
    this.securityEventService.onLogoutErrorEvent().subscribe((error) => this.alertService.error(error));
  }

  login(username: string, password: string) {
    this.windowSpinnerService.showSpinner = true;
    let headers: HttpHeaders = new HttpHeaders({'Content-Type': 'application/json'});
    return this.http.post<User>(SmpConstants.REST_PUBLIC_SECURITY_AUTHENTICATION,
      JSON.stringify({
        username: username,
        password: password
      }),
      {headers})
      .subscribe({
        next: (response: User) => {
          this.updateUserDetails(response);
          this.translateService.use(response?.smpLocale);
          this.securityEventService.notifyLoginSuccessEvent(response);
        },
        error: (error: any) => {
          this.windowSpinnerService.showSpinner = false
          this.alertService.error(error.error?.errorDescription)
          this.securityEventService.notifyLoginErrorEvent(error);

        }, complete: () => {
          this.windowSpinnerService.showSpinner = false
        }

      });
  }

  requestCredentialReset(userid: string) {
    this.windowSpinnerService.showSpinner = true;
    let headers: HttpHeaders = new HttpHeaders({'Content-Type': 'application/json'});
    return this.http.post<User>(SmpConstants.REST_PUBLIC_SECURITY_RESET_CREDENTIALS_REQUEST,
      JSON.stringify({
        credentialName: userid,
        credentialType: 'USERNAME_PASSWORD',
      }),
      {headers})
      .subscribe({
        complete: () => {
          this.windowSpinnerService.showSpinner = false;
        }, // completeHandler
        error: (error: any) => {
          this.windowSpinnerService.showSpinner = false;
          this.alertService.error(error)
        },    // errorHandler
        next: async () => {
          this.alertService.success(await lastValueFrom(this.translateService.get("login.success.confirmation.email.sent", {userId: userid})),
            true, -1);
          this.router.navigate(['/search']);
        }
      });
  }

  validateCredentialReset(token: string) {
    let headers: HttpHeaders = new HttpHeaders({'Content-Type': 'application/json'});
    this.windowSpinnerService.showSpinner = true;
    return this.http.post<User>(SmpConstants.REST_PUBLIC_SECURITY_RESET_CREDENTIALS_VALIDATE,
      JSON.stringify({
        credentialType: 'USERNAME_PASSWORD',
        resetToken: token,
      }),
      {headers})
      .subscribe({
        complete: () => {
          this.windowSpinnerService.showSpinner = false;
        }, // completeHandler
        error: (error: any) => {
          this.windowSpinnerService.showSpinner = false;
          this.router.navigate(['/search']);
          this.alertService.error(error);
        }
        // errorHandler
      });
  }

  credentialReset(userid: string, token: string, newPassword: string) {
    let headers: HttpHeaders = new HttpHeaders({'Content-Type': 'application/json'});
    this.windowSpinnerService.showSpinner = true;
    return this.http.post<User>(SmpConstants.REST_PUBLIC_SECURITY_RESET_CREDENTIALS,
      JSON.stringify({
        credentialName: userid,
        credentialValue: newPassword,
        credentialType: 'USERNAME_PASSWORD',
        resetToken: token,
      }),
      {headers})
      .subscribe({
        complete: () => {
          this.windowSpinnerService.showSpinner = false;
          this.router.navigate(['/login']);
        }, // completeHandler
        error: (error: any) => {
          this.windowSpinnerService.showSpinner = false;
          // if the error is not related to the password, we redirect to the login page
          console
          if (error.error?.errorCode !== SmpErrorCode.ERROR_CODE_INVALID_NEW_PASSWORD) {
            this.router.navigate(['/login']);
          }
          this.alertService.error(error);
        },    // errorHandler
        next: async () => {
          this.alertService.success(await lastValueFrom(this.translateService.get("reset.credentials.success.password.reset")), true, -1);
        }
      });
  }

  refreshLoggedUserFromServer() {
    this.getCurrentUsernameFromServer().subscribe((userDetails: User) => {
      this.updateUserDetails(userDetails);
      this.securityEventService.notifyLoginSuccessEvent(userDetails);
      if (userDetails?.forceChangeExpiredPassword) {
        this.dialog.open(PasswordChangeDialogComponent, {
          data: {
            user: userDetails,
            adminUser: false
          }
        }).afterClosed().subscribe(res =>
          this.finalizeLogout(res)
        );
      }
    }, (error: any) => {
      // just clean local storage
      this.localStorageService.clearLocalStorage();
    });
  }

  logout() {
    this.http.delete(SmpConstants.REST_PUBLIC_SECURITY_AUTHENTICATION)
      .subscribe({
        next: (res: Response) => {
          this.finalizeLogout(res);
        },
        error: (err: any) => {
          if (err instanceof HttpErrorResponse && err.status === 401) {
            this.finalizeLogout(err);
          } else {
            this.securityEventService.notifyLogoutErrorEvent(err);
          }
        }
      });
  }

  finalizeLogout(res) {
    this.localStorageService.clearLocalStorage();
    this.securityEventService.notifyLogoutSuccessEvent(res);
  }


  getCurrentUser(): User {
    return this.localStorageService.getUserDetails();
  }

  private getCurrentUsernameFromServer(): Observable<User> {
    let subject = new ReplaySubject<User>();
    this.http.get<User>(SmpConstants.REST_PUBLIC_SECURITY_USER)
      .subscribe({
        next: (res: User) => {
          subject.next(res);
        }, error: (error: any) => {
          subject.next(null);
        }
      });
    return subject.asObservable();
  }

  isAuthenticated(callServer: boolean = false): Observable<boolean> {
    let subject = new ReplaySubject<boolean>();
    if (callServer) {
      //we get the username from the server to trigger the redirection to the login screen in case the user is not authenticated
      this.getCurrentUsernameFromServer().subscribe({
        next: (user: User) => {
          if (!user) {
            this.localStorageService.clearLocalStorage();
          }
          subject.next(user !== null);
        }, error: (user: any) => {
          subject.next(false);
        }
      });
    } else {
      subject.next(this.hasUISessionData());
    }
    return subject.asObservable();
  }

  hasUISessionData(): boolean {
    return !!this.getCurrentUser();
  }

  isCurrentUserSystemAdmin(): boolean {
    return this.isCurrentUserInRole([Authority.SYSTEM_ADMIN]);
  }

  isCurrentUserSMPAdmin(): boolean {
    return this.isCurrentUserInRole([Authority.SMP_ADMIN]);
  }

  isCurrentUserServiceGroupAdmin(): boolean {
    return this.isCurrentUserInRole([Authority.SERVICE_GROUP_ADMIN]);
  }

  isCurrentUserInRole(roles: Array<Authority>): boolean {
    let hasRole = false;
    const currentUser = this.getCurrentUser();
    if (currentUser && currentUser.authorities) {
      roles.forEach((role: Authority) => {
        if (currentUser.authorities.indexOf(role) !== -1) {
          hasRole = true;
        }
      });
    }
    return hasRole;
  }

  isAuthorized(roles: Array<Authority>): Observable<boolean> {
    let subject = new ReplaySubject<boolean>();

    this.isAuthenticated(false).subscribe((isAuthenticated: boolean) => {
      if (isAuthenticated && roles) {
        let hasRole = this.isCurrentUserInRole(roles);
        subject.next(hasRole);
      }
    });
    return subject.asObservable();
  }

  updateUserDetails(userDetails: User) {
    // store user data to local storage!
    this.localStorageService.storeUserDetails(userDetails);
  }

  /**
   *  Method clears all local storage except the theme. Theme is not
   *  cleared because it is used to set the theme on the next login.
   */
  public clearLocalStorage(): void {
    this.localStorageService.clearLocalStorage();
  }

  /**
   *
   */
  uiUserActivityDetected() {

    let user = this.getCurrentUser();
    if (!this.isAuthenticated(false)
      || !user
      || !this.lastUISessionCall) {
      return;
    }
    this.lastUIActivity = new Date();
    // to prevent multiple calls to the backend, we check if the last call
    // was more than DELAY_BEFORE_UI_SESSION_EXTENSION_IN_MS
    if (this.lastUIActivity.getTime() - this.lastUISessionCall.getTime() > SecurityService.DELAY_BEFORE_UI_SESSION_EXTENSION_IN_MS) {
      // make a call to the backend to extend the session
      this.refreshApplicationInfo();
    }
  }

  /**
   * This method is called when a UI session call to server is detected.
   */
  uiUserSessionCallDetected() {
    if (!this.isAuthenticated(false)) {
      return;
    }
    this.lastUISessionCall = new Date();
  }

  uiUserSessionExtensionDisable() {
    this.lastUISessionCall = null;
  }

  public refreshApplicationInfo() {

    this.http.get<SmpInfo>(SmpConstants.REST_PUBLIC_APPLICATION_INFO)
      .subscribe({
        next: (res: SmpInfo): void => {

        },
        error: (err: any): void => {
          console.log("getSmpInfo:" + err);
        }
      });
  }
}
