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
package org.bonitasoft.engine.bdm.model;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

/**
 * @author Matthieu Chaffotte
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class UniqueConstraint implements NamedElement {

    @XmlAttribute(required = true)
    private String name;

    @XmlElement
    private String description;

    @XmlElementWrapper(name = "fieldNames", required = true)
    @XmlElement(name = "fieldName", required = true)
    private List<String> fieldNames;

    @Override
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getFieldNames() {
        return fieldNames;
    }

    public void setFieldNames(final List<String> fieldNames) {
        this.fieldNames = fieldNames;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UniqueConstraint that = (UniqueConstraint) o;
        return Objects.equals(name, that.name) && Objects.equals(description, that.description)
                && Objects.equals(fieldNames, that.fieldNames);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, fieldNames);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", UniqueConstraint.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .add("description='" + description + "'")
                .add("fieldNames=" + fieldNames)
                .toString();
    }

}
