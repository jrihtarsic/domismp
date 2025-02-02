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
package eu.europa.ec.edelivery.smp.data.enums;

import java.util.Arrays;
import java.util.List;

/**
 * Enum for member of types. At the moment there are following member of types:
 * <ul>
 *     <li>DOMAIN - membership of domain.</li>
 *     <li>GROUP - membership of domain.</li>
 *     <li>RESOURCE; - membership of domain.</li>
 * </ul>
 *
 * @author Joze Rihtarsic
 * @since 5.1
 */
public enum MemberOfType {
    DOMAIN,
    GROUP,
    RESOURCE;

    /**
     * Method to convert MembershipRoleType to List<MembershipRoleType>. if roleTypes is null or empty,
     * all values are returned as list
     *
     * @param roleTypes - list of role types
     * @return list of role types
     */
    public static List<MemberOfType> toList(MemberOfType... roleTypes) {
        return Arrays.asList(roleTypes == null || roleTypes.length == 0 ? values() : roleTypes);
    }
}
