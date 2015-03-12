/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.bdm.model.field;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author Colin PUY
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
public abstract class Field {

    public static final String PERSISTENCE_ID = "persistenceId";

    public static final String PERSISTENCE_VERSION = "persistenceVersion";

    @XmlAttribute(required = true)
    private String name;

    @XmlAttribute
    private Boolean nullable = true;

    @XmlAttribute
    private Boolean collection = false;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Boolean isNullable() {
        return nullable;
    }

    public void setNullable(final Boolean nullable) {
        this.nullable = nullable;
    }

    public Boolean isCollection() {
        return collection;
    }

    public void setCollection(final Boolean collection) {
        this.collection = collection;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(collection).append(name).append(nullable).toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Field) {
            final Field other = (Field) obj;
            return new EqualsBuilder().append(collection, other.collection).append(name, other.name).append(nullable, other.nullable).isEquals();
        }
        return false;
    }
}
