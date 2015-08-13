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
package org.bonitasoft.engine.bpm.businessdata.impl;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.bonitasoft.engine.bpm.businessdata.BusinessDataDefinition;
import org.bonitasoft.engine.bpm.internal.NamedElementImpl;
import org.bonitasoft.engine.bpm.process.ModelFinderVisitor;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.impl.ExpressionImpl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.Objects;

/**
 * @author Emmanuel Duchastenier
 * @author Romain Bioteau
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class BusinessDataDefinitionImpl extends NamedElementImpl implements BusinessDataDefinition {

    private static final long serialVersionUID = 6900164253595599909L;
    @XmlElement
    private String description;
    @XmlAttribute
    private String type;
    @XmlAttribute
    private String className;
    @XmlAttribute
    private boolean multiple = false;
    @XmlElement(type = ExpressionImpl.class,name = "defaultValue")
    private Expression defaultValueExpression;

    public BusinessDataDefinitionImpl(final String name, final Expression defaultValueExpression) {
        super(name);
        this.defaultValueExpression = defaultValueExpression;
    }

    public BusinessDataDefinitionImpl(){}
    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public Expression getDefaultValueExpression() {
        return defaultValueExpression;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setDefaultValueExpression(final Expression defaultValueExpression) {
        this.defaultValueExpression = defaultValueExpression;
    }

    public void setClassName(final String className) {
        this.className = className;
    }

    @Override
    public boolean isMultiple() {
        return multiple;
    }

    public void setMultiple(final boolean multiple) {
        this.multiple = multiple;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("description", description)
                .append("type", type)
                .append("className", className)
                .append("multiple", multiple)
                .append("defaultValueExpression", defaultValueExpression)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        BusinessDataDefinitionImpl that = (BusinessDataDefinitionImpl) o;
        return Objects.equals(multiple, that.multiple) &&
                Objects.equals(description, that.description) &&
                Objects.equals(type, that.type) &&
                Objects.equals(className, that.className) &&
                Objects.equals(defaultValueExpression, that.defaultValueExpression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), description, type, className, multiple, defaultValueExpression);
    }

    @Override
    public void accept(ModelFinderVisitor visitor, long modelId) {
        visitor.find(this, modelId);
    }
}
