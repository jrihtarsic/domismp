/*-
 * #START_LICENSE#
 * smp-webapp
 * %%
 * Copyright (C) 2017 - 2024 European Commission | eDelivery | DomiSMP
 * %%
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * [PROJECT_HOME]\license\eupl-1.2\license.txt or https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 * #END_LICENSE#
 */
import {
  AfterViewInit,
  Component,
  forwardRef,
  Input,
  ViewChild,
} from '@angular/core';
import {
  ControlContainer,
  ControlValueAccessor,
  FormBuilder,
  FormControl,
  FormControlDirective,
  FormGroup,
  NG_VALUE_ACCESSOR
} from "@angular/forms";
import {MatDialog} from "@angular/material/dialog";
import {
  BeforeLeaveGuard
} from "../../../../window/sidenav/navigation-on-leave-guard";
import {GlobalLookups} from "../../../global-lookups";
import {TranslateService} from "@ngx-translate/core";
import {
  HttpErrorHandlerService
} from "../../../error/http-error-handler.service";
import {DocumentMetadataRo} from "../../../model/document-metadata-ro.model";

/**
 * Component to display the properties of a document in a table. The properties can be edited and saved.
 * @author Joze Rihtarsic
 * @since 5.1
 */
@Component({
  selector: 'document-metadata-panel',
  templateUrl: './document-metadata-panel.component.html',
  styleUrls: ['./document-metadata-panel.component.scss'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => DocumentMetadataPanelComponent),
      multi: true
    }
  ]
})
export class DocumentMetadataPanelComponent implements AfterViewInit, BeforeLeaveGuard, ControlValueAccessor {

  dataChanged = false;
  private onChangeCallback: (_: any) => void = () => {
  };

  _documentMetadata: DocumentMetadataRo;
  documentMetadataForm: FormGroup;

  constructor(
    private globalLookups: GlobalLookups,
    public dialog: MatDialog,
    private controlContainer: ControlContainer,
    private formBuilder: FormBuilder,
    private translateService: TranslateService,
    private httpErrorHandlerService: HttpErrorHandlerService) {

    this.documentMetadataForm = formBuilder.group({
      'name': new FormControl({value: null}),
      'mimeType': new FormControl({value: null}),
      'publishedVersion': new FormControl({value: null}),
      'sharingEnabled': new FormControl({value: null}),
      'allVersions': new FormControl({value: null}),
      'referenceDocumentId': new FormControl({value: null}),
    });
  }


  @ViewChild(FormControlDirective, {static: true})
  formControlDirective: FormControlDirective;
  @Input()
  formControl: FormControl;

  @Input()
  formControlName: string;  /* get hold of FormControl instance no matter formControl or    formControlName is given. If formControlName is given, then this.controlContainer.control is the parent FormGroup (or FormArray) instance. */
  get control() {
    return this.formControl || this.controlContainer.control.get(this.formControlName);
  }

  /**
   * Implementation of the ControlValueAccessor method to  write value to the component.
   * @param eventList
   */
  writeValue(data: DocumentMetadataRo): void {
    this.documentMetadata = data;
  }

  ngAfterViewInit() {

  }


  isDirty(): boolean {
    return this.documentMetadataForm.dirty;
  }

  registerOnChange(fn: any): void {
    this.onChangeCallback = fn;

  }

  registerOnTouched(fn: any): void {
    // not implemented
  }

  setDisabledState(isDisabled: boolean): void {
    // not implemented
  }


  @Input() set documentMetadata(value: DocumentMetadataRo) {
    this._documentMetadata = value;
    this.documentMetadataForm.disable();

    if (!!value) {
      this.documentMetadataForm.controls['name'].setValue(value.name);
      this.documentMetadataForm.controls['mimeType'].setValue(value.mimeType);
      this.documentMetadataForm.controls['publishedVersion'].setValue(value.publishedVersion);
      this.documentMetadataForm.controls['allVersions'].setValue(value.allVersions);
      this.documentMetadataForm.controls['referenceDocumentId'].setValue(value.referenceDocumentId);
      this.documentMetadataForm.controls['sharingEnabled'].setValue(value.sharingEnabled);
    } else {
      this.documentMetadataForm.controls['name'].setValue("");
      this.documentMetadataForm.controls['mimeType'].setValue("");
      this.documentMetadataForm.controls['publishedVersion'].setValue("");
      this.documentMetadataForm.controls['allVersions'].setValue([]);
      this.documentMetadataForm.controls['referenceDocumentId'].setValue("");
      this.documentMetadataForm.controls['sharingEnabled'].setValue(false);
      this.documentMetadataForm.controls['properties'].setValue([]);
    }
    this.documentMetadataForm.markAsPristine();
  }

  get documentMetadata(): DocumentMetadataRo {
    let docMetadata: DocumentMetadataRo = {...this._documentMetadata};

    // set new properties
    docMetadata.sharingEnabled = this.documentMetadataForm.controls['sharingEnabled'].value;
    docMetadata.name = this.documentMetadataForm.controls['name'].value;
    docMetadata.referenceDocumentId = this.documentMetadataForm.controls['referenceDocumentId'].value;
    return docMetadata;
  }
}
