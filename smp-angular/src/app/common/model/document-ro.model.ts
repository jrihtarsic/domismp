export interface DocumentRo {
  mimeType?: string;
  name?: string;
  documentId?: string;
  currentResourceVersion?:number;
  allVersions?: number[];
  payloadVersion?:number;
  payloadCreatedOn?: Date;
  payload?:string;
}

