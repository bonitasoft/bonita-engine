/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

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

    @XmlElement
    private String displayName;

    @XmlElement
    private String description;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
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
        return Objects.hash(name, displayName, description, nullable, collection);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Field field = (Field) o;
        return Objects.equals(name, field.name) && Objects.equals(displayName, field.displayName)
                && Objects.equals(description, field.description) && Objects.equals(nullable, field.nullable)
                && Objects.equals(collection, field.collection);
    }
}
