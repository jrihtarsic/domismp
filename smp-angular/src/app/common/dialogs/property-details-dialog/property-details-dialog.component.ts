import {Component, Inject, OnInit} from '@angular/core';
import {
  MAT_DIALOG_DATA,
  MatDialog,
  MatDialogRef
} from '@angular/material/dialog';
import {
  UntypedFormBuilder,
  UntypedFormControl,
  UntypedFormGroup
} from "@angular/forms";
import {
  AlertMessageService
} from "../../alert-message/alert-message.service";
import {EntityStatus} from "../../enums/entity-status.enum";
import {SmpConstants} from "../../../smp.constants";
import {HttpClient} from "@angular/common/http";
import {
  HttpErrorHandlerService
} from "../../error/http-error-handler.service";
import {
  PropertyRo
} from "../../../system-settings/admin-properties/property-ro.model";
import {
  PropertyValidationRo
} from "../../../system-settings/admin-properties/property-validate-ro.model";
import {PropertySourceEnum} from "../../enums/property-source.enum";
import {firstValueFrom, lastValueFrom} from "rxjs";
import {TranslateService} from "@ngx-translate/core";

@Component({
  selector: 'property-details-dialog',
  templateUrl: './property-details-dialog.component.html',
  styleUrls: ['./property-details-dialog.component.css']
})
export class PropertyDetailsDialogComponent implements OnInit {

  editMode: boolean;
  formTitle = "";
  current: PropertyRo & { confirmation?: string, systemDefault?: boolean };
  propertyForm: UntypedFormGroup;
  disabled: true;
  showSpinner: boolean = false;
  propertyType: PropertySourceEnum = PropertySourceEnum.SYSTEM;


  constructor(
    public dialog: MatDialog,
    private httpErrorHandlerService: HttpErrorHandlerService,
    protected http: HttpClient,
    private dialogRef: MatDialogRef<PropertyDetailsDialogComponent>,
    private alertService: AlertMessageService,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private fb: UntypedFormBuilder,
    private translateService: TranslateService) {

    this.editMode = data.edit;
    this.propertyType = data.propertyType;
    this.propertyType = !data.propertyType ? PropertySourceEnum.SYSTEM : data.propertyType;
    (async () => await this.updateFormTitle())();

    this.current = this.editMode
      ? {
        ...data.row,
      }
      : {
        property: '',
        value: '',
        type: '',
        desc: '',
        readonly: false,
        status: EntityStatus.NEW,
        systemDefault: false,
      };

    this.propertyForm = fb.group({
      'property': new UntypedFormControl({value: '', readonly: true}, null),
      'desc': new UntypedFormControl({value: '', readonly: true}, null),
      'type': new UntypedFormControl({value: '', readonly: true}, null),
      'value': new UntypedFormControl({value: ''}),
      'valuePattern': new UntypedFormControl({value: ''}),
      'errorMessage': new UntypedFormControl({value: ''}),
      'systemDefault': new UntypedFormControl({value: false}),
    });

    this.propertyForm.controls['property'].setValue(this.current.property);
    this.propertyForm.controls['desc'].setValue(this.current.desc);
    this.propertyForm.controls['type'].setValue(this.current.type);
    this.propertyForm.controls['valuePattern'].setValue(this.current.valuePattern);
    this.propertyForm.controls['systemDefault'].setValue(this.current.systemDefault);

    this.propertyForm.controls['errorMessage'].setValue('');
    this.updateValueState();
  }

  async updateFormTitle() {
    this.formTitle = this.editMode
      ? await lastValueFrom(this.translateService.get("property.details.dialog.title.edit.mode", {type: this.capitalize(this.propertyType)}))
      : await lastValueFrom(this.translateService.get("property.details.dialog.title.new.mode", {type: this.capitalize(this.propertyType)}));
  }

  ngOnInit() {

  }

  /**
   * Methods validates the property with server validator. If property value is ok
   * it closes the dialog else writes the errorMessage.
   */
  async submitForm() {
    this.checkValidity(this.propertyForm);

    let request: PropertyRo = this.getCurrent();

    // if domain property we do not need to validate
    if (this.propertyType == PropertySourceEnum.DOMAIN) {
      this.propertyForm.controls['errorMessage'].setValue("");
      // we can close the dialog
      this.closeDialog();
      return;
    }
    // if system property validate property
    let validationObservable = this.http
      .post<PropertyValidationRo>(SmpConstants.REST_INTERNAL_PROPERTY_VALIDATE, request);

    this.showSpinner = true;

    try {
      const result: PropertyValidationRo = await firstValueFrom(validationObservable);
      this.showSpinner = false;
      if (!result.propertyValid) {
        this.propertyForm.controls['errorMessage'].setValue(result.errorMessage
          ? result.errorMessage
          : await lastValueFrom(this.translateService.get("property.details.dialog.error.invalid.property")));
      } else {
        this.propertyForm.controls['errorMessage'].setValue("");
        // we can close the dialog
        this.closeDialog();
      }
    } catch (err) {
      if (this.httpErrorHandlerService.logoutOnInvalidSessionError(err)) {
        this.closeDialog();
        return;
      }
      this.alertService.error(await lastValueFrom(this.translateService.get("property.details.dialog.error.validation")), err)
      console.log("Error occurred on Validation the property: " + err);
    }
  }

  checkValidity(g: UntypedFormGroup) {
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

  /**
   * Method casts string value to correct property type for dialog component used for editing.
   * At the moment only BOOLEAN needs to be updated, other types are returned as is.
   * @param value - string value
   * @param propertyType - property type
   */
  public valueFromPropertyStringValue(value: any, propertyType: string) {

    if (propertyType === 'BOOLEAN') {
      // make sure that the value is lower case string!
      const valToString = value?.toString().toLowerCase();
      return valToString === 'true' || valToString === '1' || valToString === 'yes';
    }
    return value;
  }

  /**
   * Method casts value to string for property value. At the moment only BOOLEAN needs to be updated.
   * @param value - value
   * @param propertyType - property type
   */
  public valueToPropertyStringValue(value: string, propertyType: string) {
    if (propertyType === 'BOOLEAN') {
      // make sure that the value is lower case string!
      const valToString = value?.toString().toLowerCase();
      return valToString === 'true' || valToString === '1' || valToString === 'yes' ? 'true' : 'false';
    }
    return value;
  }

  getInputType(propertyType: string) {
    console.log("Get input type for row " + this.current.type)
    switch (propertyType) {
      case 'STRING':
      case 'LIST_STRING':
      case 'MAP_STRING':
      case 'FILENAME':
      case 'PATH':
        return 'text';
      case 'INTEGER':
        return 'text';
      case 'BOOLEAN':
        return 'checkbox';
      case 'REGEXP':
        return 'text';
      case 'EMAIL':
        return 'email';
      case 'URL':
        return 'url';
      default:
        return 'text';
    }
  }

  getInputPatternType(propertyType: string) {
    console.log("Get input pattern for row " + this.current.type)
    switch (propertyType) {
      case 'STRING':
      case 'LIST_STRING':
      case 'MAP_STRING':
      case 'FILENAME':
      case 'PATH':
        return '';
      case 'INTEGER':
        return '[0-9]*';
      case 'BOOLEAN':
        return 'true/false';
      case 'REGEXP':
        return '';
      case 'EMAIL':
        return '';
      case 'URL':
        return '';
      default:
        return '';
    }
  }

  public getCurrent(): PropertyRo {
    this.current.status = EntityStatus.UPDATED;
    this.current.value = this.valueToPropertyStringValue(this.propertyForm.value['value'], this.current.type);
    this.current.systemDefault = this.propertyForm.value['systemDefault'];
    return this.current;
  }

  closeDialog() {
    this.dialogRef.close(true);
  }

  get isSystemDefault(): boolean {
    return this.propertyForm.value['systemDefault'];
  }

  /**
   * Method updates the state of the value field based on the system default checkbox.
   */
  updateValueState(): void {
    let value;
    if (!this.isDomainProperty || !this.isSystemDefault) {
      value = this.valueFromPropertyStringValue(this.current.value, this.current.type);
      this.propertyForm.controls['value'].enable();
    } else {
      value = this.valueFromPropertyStringValue(this.current.systemDefaultValue, this.current.type);
      this.propertyForm.controls['value'].disable();
    }
    this.propertyForm.controls['value'].setValue(value);
  }

  get isDomainProperty(): boolean {
    return this.propertyType == PropertySourceEnum.DOMAIN;
  }

  capitalize<T extends string>(str: T): string {
    return (str.charAt(0).toUpperCase() + str.slice(1).toLowerCase()) as Capitalize<T>;
  }

  get isDirty(): boolean {
    return this.propertyForm.dirty;
  }
}
