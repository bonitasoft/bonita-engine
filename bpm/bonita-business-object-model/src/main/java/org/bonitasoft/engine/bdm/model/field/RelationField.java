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
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;

import org.bonitasoft.engine.bdm.model.BusinessObject;

/**
 * @author Colin PUY
 */
@XmlType
public class RelationField extends Field {

    // required, package-info doesn't apply to inner class
    @XmlType(namespace = "http://documentation.bonitasoft.com/bdm-xml-schema/1.0")
    public enum Type {
        AGGREGATION, COMPOSITION
    }

    // required, package-info doesn't apply to inner class
    @XmlType(namespace = "http://documentation.bonitasoft.com/bdm-xml-schema/1.0")
    public enum FetchType {
        EAGER, LAZY
    }

    @XmlAttribute(required = true)
    private Type type;

    @XmlIDREF
    @XmlAttribute(required = true)
    private BusinessObject reference;

    @XmlAttribute(required = true)
    private FetchType fetchType = FetchType.EAGER;

    public Type getType() {
        return type;
    }

    public void setType(final Type type) {
        this.type = type;
    }

    public BusinessObject getReference() {
        return reference;
    }

    public void setReference(final BusinessObject reference) {
        this.reference = reference;
    }

    public FetchType getFetchType() {
        return fetchType;
    }

    public void setFetchType(final FetchType fetchType) {
        this.fetchType = fetchType;
    }

    public boolean isLazy() {
        return fetchType == FetchType.LAZY;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), type, reference == null ? null : reference.getQualifiedName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        RelationField that = (RelationField) o;
        if (type != that.type) {
            return false;
        }
        if (reference == null ^ that.reference == null) {
            return false;
        }
        if (reference != null) {
            return Objects.equals(reference.getQualifiedName(), that.reference.getQualifiedName());
        }
        return true;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", RelationField.class.getSimpleName() + "[", "]")
                .add("name=" + getName())
                .add("type=" + type)
                .add("reference=" + reference)
                .toString();
    }

}
