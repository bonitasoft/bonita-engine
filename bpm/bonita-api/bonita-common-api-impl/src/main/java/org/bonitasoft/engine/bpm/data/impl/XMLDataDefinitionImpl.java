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
package org.bonitasoft.engine.bpm.data.impl;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.bonitasoft.engine.bpm.data.XMLDataDefinition;
import org.bonitasoft.engine.expression.Expression;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import java.util.Objects;

/**
 * @author Feng Hui
 * @author Celine Souchet
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class XMLDataDefinitionImpl extends DataDefinitionImpl implements XMLDataDefinition {

    private static final long serialVersionUID = 3614847378996945363L;
    @XmlAttribute
    private String namespace;
    @XmlAttribute
    private String element;

    public XMLDataDefinitionImpl(){}
    public XMLDataDefinitionImpl(final String name, final Expression defaultValueExpression) {
        super(name, defaultValueExpression);
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public String getElement() {
        return element;
    }

    public void setNamespace(final String namespace) {
        this.namespace = namespace;
    }

    public void setElement(final String element) {
        this.element = element;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("namespace", namespace)
                .append("element", element)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        XMLDataDefinitionImpl that = (XMLDataDefinitionImpl) o;
        return Objects.equals(namespace, that.namespace) &&
                Objects.equals(element, that.element);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), namespace, element);
    }
}
