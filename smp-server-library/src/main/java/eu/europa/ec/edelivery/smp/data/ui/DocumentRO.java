/*-
 * #START_LICENSE#
 * smp-server-library
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
package eu.europa.ec.edelivery.smp.data.ui;

import eu.europa.ec.edelivery.smp.config.enums.SMPPropertyTypeEnum;
import eu.europa.ec.edelivery.smp.data.enums.DocumentVersionStatusType;
import eu.europa.ec.edelivery.smp.data.ui.enums.EntityROStatus;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class DocumentRO extends BaseRO {
    private static final long serialVersionUID = 9008583888835630038L;
    String documentId;
    String mimeType;
    Integer currentResourceVersion;
    List<Integer> allVersions;
    String name;

    Integer payloadVersion;
    String payload;
    private int payloadStatus = EntityROStatus.PERSISTED.getStatusNumber();
    OffsetDateTime payloadCreatedOn;
    DocumentVersionStatusType documentVersionStatus;

    List<DocumentPropertyRO> properties = new ArrayList<>();
    List<DocumentVersionEventRO> documentVersionEvents = new ArrayList<>();
    List<DocumentVersionRO> documentVersions = new ArrayList<>();

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Integer getCurrentResourceVersion() {
        return currentResourceVersion;
    }

    public void setCurrentResourceVersion(Integer currentResourceVersion) {
        this.currentResourceVersion = currentResourceVersion;
    }

    public List<Integer> getAllVersions() {
        if (allVersions == null) {
            allVersions = new ArrayList<>();
        }
        return allVersions;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Integer getPayloadVersion() {
        return payloadVersion;
    }

    public void setPayloadVersion(Integer payloadVersion) {
        this.payloadVersion = payloadVersion;
    }

    public DocumentVersionStatusType getDocumentVersionStatus() {
        return documentVersionStatus;
    }

    public void setDocumentVersionStatus(DocumentVersionStatusType documentVersionStatus) {
        this.documentVersionStatus = documentVersionStatus;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public int getPayloadStatus() {
        return payloadStatus;
    }

    public void setPayloadStatus(int payloadStatus) {
        this.payloadStatus = payloadStatus;
    }

    public OffsetDateTime getPayloadCreatedOn() {
        return payloadCreatedOn;
    }

    public void setPayloadCreatedOn(OffsetDateTime payloadCreatedOn) {
        this.payloadCreatedOn = payloadCreatedOn;
    }

    public List<DocumentPropertyRO> getProperties() {
        return properties;
    }

    public void addProperty(String key, String value, String description, SMPPropertyTypeEnum type, boolean readonly) {
        DocumentPropertyRO propertyRO = new DocumentPropertyRO(key, value, description, readonly);
        propertyRO.setType(type);

        this.properties.add(propertyRO);
    }

    public List<DocumentVersionRO> getDocumentVersions() {
        return documentVersions;
    }

    public void setDocumentVersions(List<DocumentVersionRO> documentVersions) {
        this.documentVersions = documentVersions;
    }

    public List<DocumentVersionEventRO> getDocumentVersionEvents() {
        return documentVersionEvents;
    }

    public void addDocumentVersionEvent(DocumentVersionEventRO event) {
        this.documentVersionEvents.add(event);
    }
}
