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

package eu.europa.ec.smp.spi.exceptions;

/**
 * Thrown when used identifier does not fulfill requirements specified in OASIS SMP specs:
 * http://docs.oasis-open.org/bdxr/bdx-smp/v1.0/bdx-smp-v1.0.html
 * 
 * Created by gutowpa on 12/01/2017.
 */
public class MalformedIdentifierException extends IllegalArgumentException {

    private static String buildMessage(String malformedId){
        return "Malformed identifier, scheme and id should be delimited by double colon: "+malformedId;
    }

    public MalformedIdentifierException(String malformedId, Exception cause){
        super(buildMessage(malformedId), cause);
    }

    public MalformedIdentifierException(String message){
        super(message);
    }
}
