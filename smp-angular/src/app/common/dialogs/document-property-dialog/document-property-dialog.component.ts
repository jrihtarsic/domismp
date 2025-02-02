import {Component, Inject} from '@angular/core';
import {
  MAT_DIALOG_DATA,
  MatDialog,
  MatDialogRef
} from '@angular/material/dialog';
import {
  AbstractControl,
  UntypedFormBuilder,
  UntypedFormControl,
  UntypedFormGroup, Validators
} from "@angular/forms";
import {
  AlertMessageService
} from "../../alert-message/alert-message.service";
import {EntityStatus} from "../../enums/entity-status.enum";
import {HttpClient} from "@angular/common/http";
import {DocumentPropertyRo} from "../../model/document-property-ro.model";
import {PropertyValueTypeEnum} from "../../enums/property-value-type.enum";
import {
  PropertyValueTypeEnumUtil
} from "../../enums/utils/PropertyValueTypeEnumUtil";
import {TranslateService} from "@ngx-translate/core";
import {lastValueFrom} from "rxjs";

@Component({
  selector: 'document-property-dialog',
  templateUrl: './document-property-dialog.component.html',
  styleUrls: ['./document-property-dialog.component.css']
})
export class DocumentPropertyDialogComponent {

  public propertyTypes: string[] = Object.keys(PropertyValueTypeEnum)
  protected readonly PropertyValueTypeEnum = PropertyValueTypeEnum;
  // regular expression pattern must start with a letter and contain only letters, numbers and dots and must not be bigger than 255 characters  long
  formTitle = "";
  current: DocumentPropertyRo;
  propertyForm: UntypedFormGroup;
  disabled: true;
  private allPropertyNames: string[] = [];

  notInList(list: string[], exception: string) {
    if (!list || !exception) {
      return (c: AbstractControl): { [key: string]: any } => {
        return null;
      }
    }

    return (c: AbstractControl): { [key: string]: any } => {
      console.log("Check if value is in list: " + c.value + " type: " + typeof c.value);
      let inputVal = typeof  c?.value?.trim === "function" ? c.value.trim().toLowerCase() : c.value;
      if (inputVal&& inputVal !== exception
        && list.includes(inputVal))
        return {'notInList': {valid: false}};
      return null;
    }
  }


  constructor(
    public dialog: MatDialog,
    protected http: HttpClient,
    private dialogRef: MatDialogRef<DocumentPropertyDialogComponent>,
    private alertService: AlertMessageService,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private fb: UntypedFormBuilder,
    private translateService: TranslateService) {

    this.current = {...data.row};
    this.allPropertyNames = data.allPropertyNames;

    (async () => this.updateFormTitle())();

    this.propertyForm = fb.group({
      'property': new UntypedFormControl({value: '', readonly: true,}, [
        this.notInList(this.allPropertyNames, this.current.property), Validators.pattern(PropertyValueTypeEnumUtil.PROPERTY_NAME_PATTERN)] ),
      'desc': new UntypedFormControl({value: '', readonly: true}, null),
      'type': new UntypedFormControl({value: '', readonly: true}, null),
      'value': new UntypedFormControl({value: ''}),
      'valuePattern': new UntypedFormControl({value: ''}),
      'errorMessage': new UntypedFormControl({value: ''}),
    });

    this.propertyForm.controls['errorMessage'].setValue('');
    this.updateValueState();
  }

  get isNewItem(): boolean {
    return this.current?.status === EntityStatus.NEW;
  }

  get isReadOnly(): boolean {
    return this.current?.readonly;
  }

  private async updateFormTitle() {
    this.formTitle = this.isNewItem
      ? await lastValueFrom(this.translateService.get("document.property.dialog.title.new.mode"))
      : await lastValueFrom(this.translateService.get("document.property.dialog.title.edit.mode"));
  }

  /**
   * Methods validates the property with server validator. If property value is ok
   * it closes the dialog else writes the errorMessage.
   */
  async submitForm() {
    this.checkValidity(this.propertyForm)
    this.dialogRef.close(this.getCurrent());
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
   * @param value
   * @param propertyType
   */
  public valueFromPropertyStringValue(value: string, propertyType: PropertyValueTypeEnum) {
    if (propertyType === PropertyValueTypeEnum.BOOLEAN) {
      return value === 'true';
    } else {
      return value;
    }
  }

  public valueToPropertyStringValue(value: any, propertyType: PropertyValueTypeEnum) {
    if (propertyType === PropertyValueTypeEnum.BOOLEAN) {
      return value ? 'true' : 'false';
    } else {
      return value;
    }
  }

  getInputType(propertyType: PropertyValueTypeEnum) {
    console.log("Get input type for row " + PropertyValueTypeEnumUtil.getKeyName(this.current.type))
    switch (propertyType) {
      case PropertyValueTypeEnum.STRING:
      case PropertyValueTypeEnum.LIST_STRING:
      case PropertyValueTypeEnum.MAP_STRING:
      case PropertyValueTypeEnum.FILENAME:
      case PropertyValueTypeEnum.PATH:
        return 'text';
      case PropertyValueTypeEnum.INTEGER:
        return 'text';
      case PropertyValueTypeEnum.BOOLEAN:
        return 'checkbox';
      case PropertyValueTypeEnum.REGEXP:
        return 'text';
      case PropertyValueTypeEnum.EMAIL:
        return 'email';
      case PropertyValueTypeEnum.URL:
        return 'url';
      default:
        return 'text';
    }
  }

  public getCurrent(): DocumentPropertyRo {
    if (!this.propertyForm.dirty) {
      return this.current;
    }
    if (this.current.status === EntityStatus.NEW) {
      this.current.property = this.propertyForm.value['property'];
    } else if (this.current.status === EntityStatus.PERSISTED) {
      this.current.status = EntityStatus.UPDATED;
    }
    this.current.desc = this.propertyForm.value['desc'];
    this.current.type = this.propertyForm.value['type'];
    this.current.value = this.valueToPropertyStringValue(this.propertyForm.value['value'], this.current.type);
    return this.current;
  }

  closeDialog() {
    // just close the dialog without any result
    this.dialogRef.close(null);
  }

  /**
   * Method updates the state of the value field based on the system default checkbox.
   */
  updateValueState(): void {

    if (!this.isNewItem || this.isReadOnly) {
      this.propertyForm.controls['property'].disable();
    } else {
      this.propertyForm.controls['property'].enable();
      this.propertyForm.markAsDirty();
    }

    if (this.isReadOnly) {
      this.propertyForm.controls['value'].disable();
      this.propertyForm.controls['desc'].disable();
      this.propertyForm.controls['type'].disable();
    } else {
      this.propertyForm.controls['value'].enable();
      this.propertyForm.controls['desc'].enable();
      this.propertyForm.controls['type'].enable();
    }
    // update values
    this.propertyForm.controls['property'].setValue(this.current.property);
    this.propertyForm.controls['desc'].setValue(this.current.desc);
    this.propertyForm.controls['type'].setValue(this.current.type);
    this.propertyForm.controls['value'].setValue(
      this.valueFromPropertyStringValue(this.current.value, this.current.type));
  }

  get isDirty(): boolean {
    return this.propertyForm.dirty;
  }

  propertyValueTypeDescription(name: string): string {
    return PropertyValueTypeEnumUtil.getDescription(PropertyValueTypeEnum[name]);
  }
}
