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

package eu.europa.ec.edelivery.smp.data.model;

import eu.europa.ec.edelivery.smp.data.dao.utils.ColumnDescription;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.Objects;

/**
 * Database configuration entity for DomiSMP configuration properties
 *
 * @since 4.2
 * Created by Joze Rihtarsic .
 */
@Entity
@Audited
@Table(name = "SMP_CONFIGURATION")
@NamedQuery(name = "DBConfiguration.getAll", query = "SELECT d FROM DBConfiguration d")
@NamedQuery(name = "DBConfiguration.maxUpdateDate",
        query = "SELECT max(config.lastUpdatedOn) from DBConfiguration config"
)
@NamedQuery(name = "DBConfiguration.getPendingProperties",
        query = "SELECT config from DBConfiguration config where config.lastUpdatedOn > :updateDate"
)
@NamedQuery(name = "DBConfiguration.getPendingRestartProperties",
        query = "SELECT config from DBConfiguration config where config.property in (:restartPropertyList) and config.lastUpdatedOn > :serverStartedDate"
)
@org.hibernate.annotations.Table(appliesTo = "SMP_CONFIGURATION", comment = "SMP user certificates")
public class DBConfiguration extends BaseEntity {

    @Id
    @Column(name = "PROPERTY_NAME", length = CommonColumnsLengths.MAX_TEXT_LENGTH_512, nullable = false, unique = true)
    @ColumnDescription(comment = "Property name/key")
    String property;
    @Column(name = "PROPERTY_VALUE", length = CommonColumnsLengths.MAX_FREE_TEXT_LENGTH)
    @ColumnDescription(comment = "Property value")
    String value;

    @Column(name = "DESCRIPTION", length = CommonColumnsLengths.MAX_FREE_TEXT_LENGTH)
    @ColumnDescription(comment = "Property description")
    String description;

    @Override
    public Object getId() {
        return property;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DBConfiguration)) return false;
        DBConfiguration that = (DBConfiguration) o;
        return Objects.equals(property, that.property);
    }

    @Override
    public int hashCode() {
        return Objects.hash(property);
    }
}
