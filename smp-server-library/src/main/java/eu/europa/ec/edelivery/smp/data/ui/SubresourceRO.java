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

public class SubresourceRO extends BaseRO {
    private static final long serialVersionUID = 9008583888835630029L;
    String subresourceId;
    String identifierValue;
    String identifierScheme;
    String subresourceTypeIdentifier;
    Boolean isDocumentSharingEnabled = false;


    public String getSubresourceId() {
        return subresourceId;
    }

    public void setSubresourceId(String subresourceId) {
        this.subresourceId = subresourceId;
    }

    public String getIdentifierValue() {
        return identifierValue;
    }

    public void setIdentifierValue(String identifierValue) {
        this.identifierValue = identifierValue;
    }

    public String getIdentifierScheme() {
        return identifierScheme;
    }

    public void setIdentifierScheme(String identifierScheme) {
        this.identifierScheme = identifierScheme;
    }

    public String getSubresourceTypeIdentifier() {
        return subresourceTypeIdentifier;
    }

    public void setSubresourceTypeIdentifier(String subresourceTypeIdentifier) {
        this.subresourceTypeIdentifier = subresourceTypeIdentifier;
    }

    public Boolean getDocumentSharingEnabled() {
        return isDocumentSharingEnabled;
    }

    public void setDocumentSharingEnabled(Boolean documentSharingEnabled) {
        isDocumentSharingEnabled = documentSharingEnabled;
    }
}
