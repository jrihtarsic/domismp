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
package eu.europa.ec.edelivery.smp.data.ui.enums;


import java.util.Arrays;

/**
 * Enumeration of Resource Object status.
 * @author Joze Rihtarsic
 * @since 4.1
 */
public enum EntityROStatus {
    PERSISTED(0),
    UPDATED(1),
    NEW(2),
    REMOVED(3),
    ERROR(4);

    int statusNumber;

    EntityROStatus(int statusNumber) {
        this.statusNumber = statusNumber;
    }

    public final int getStatusNumber() {
        return statusNumber;
    }

    /**
     * Method returns EntityROStatus based on status number. If status number is not found, method returns null.
     * @param statusNumber status number
     * @return EntityROStatus or null
     */
    public static EntityROStatus fromStatusNumber(int statusNumber) {
        return Arrays.stream(EntityROStatus.values())
                .filter(entityROStatus -> entityROStatus.getStatusNumber() == statusNumber)
                .findFirst().orElse(null);
    }
}
