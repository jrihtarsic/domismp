export interface SmpInfo {
  version: string;
  contextPath?: string;
  authTypes?: string[];
  ssoAuthenticationLabel?: string;
  ssoAuthenticationURI?: string;
  passwordValidationRegExp?: string;
  passwordValidationRegExpMessage?: string;

}
