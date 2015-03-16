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

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

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
        return new HashCodeBuilder(17, 37).appendSuper(super.hashCode()).append(length).append(type).toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof SimpleField) {
            final SimpleField other = (SimpleField) obj;
            return new EqualsBuilder().appendSuper(super.equals(obj)).append(length, other.length).append(type, other.type).isEquals();
        }
        return false;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE).append("name", getName()).append("nullable", isNullable()).append("collection", isCollection())
                .append("length", length).append("type", type).toString();
    }

}
