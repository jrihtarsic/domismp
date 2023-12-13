/*-
 * #%L
 * smp-server-library
 * %%
 * Copyright (C) 2017 - 2023 European Commission | eDelivery | DomiSMP
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
 * #L%
 */
package eu.europa.ec.edelivery.smp.conversion;

import eu.europa.ec.edelivery.smp.data.model.user.DBCertificate;
import eu.europa.ec.edelivery.smp.data.ui.CertificateRO;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneId;

/**
 * @author Sebastian-Ion TINCU
 */
@Component
public class CertificateROToDBCertificateConverter implements Converter<CertificateRO, DBCertificate> {

    @Override
    public DBCertificate convert(CertificateRO source) {

        DBCertificate target = new DBCertificate();
        if (source.getValidTo() != null) {
            target.setValidTo(OffsetDateTime.ofInstant(source.getValidTo().toInstant(), ZoneId.systemDefault()));
        }
        if (source.getValidFrom() != null) {
            target.setValidFrom(OffsetDateTime.ofInstant(source.getValidFrom().toInstant(), ZoneId.systemDefault()));
        }
        target.setCertificateId(source.getCertificateId());
        target.setSerialNumber(source.getSerialNumber());
        target.setIssuer(source.getIssuer());
        target.setSubject(source.getSubject());
        target.setCrlUrl(source.getCrlUrl());
        target.setPemEncoding(source.getEncodedValue());
        return target;
    }
}
