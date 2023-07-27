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
import java.util.StringJoiner;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Matthieu Chaffotte
 */
@XmlType
public class SimpleField extends Field {

    @XmlAttribute(required = true)
    private FieldType type;

    @XmlAttribute
    private Integer length;

    public FieldType getType() {
        return type;
    }

    public void setType(final FieldType type) {
        this.type = type;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(final Integer length) {
        this.length = length;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), type, length);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        SimpleField that = (SimpleField) o;
        return type == that.type && Objects.equals(length, that.length);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SimpleField.class.getSimpleName() + "[", "]")
                .add("name=" + getName())
                .add("nullable=" + isNullable())
                .add("collection=" + isCollection())
                .add("length=" + length)
                .add("type=" + type)
                .toString();
    }

}
