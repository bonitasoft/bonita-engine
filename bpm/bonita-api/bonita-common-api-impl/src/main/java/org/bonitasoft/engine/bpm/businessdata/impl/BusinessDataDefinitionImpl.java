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

import org.bonitasoft.engine.bpm.businessdata.BusinessDataDefinition;
import org.bonitasoft.engine.bpm.internal.NamedElementImpl;
import org.bonitasoft.engine.bpm.process.ModelFinderVisitor;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.impl.ExpressionImpl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author Emmanuel Duchastenier
 * @author Romain Bioteau
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class BusinessDataDefinitionImpl extends NamedElementImpl implements BusinessDataDefinition {

    private static final long serialVersionUID = 6900164253595599909L;
    @XmlAttribute
    private String description;
    @XmlAttribute
    private String type;
    @XmlAttribute
    private String className;
    @XmlAttribute
    private boolean multiple = false;
    @XmlElement(type = ExpressionImpl.class)
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
        return "BusinessDataDefinitionImpl [description=" + description + ", type=" + type + ", className=" + className + ", multiple=" + multiple
                + ", defaultValueExpression=" + defaultValueExpression + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (className == null ? 0 : className.hashCode());
        result = prime * result + (defaultValueExpression == null ? 0 : defaultValueExpression.hashCode());
        result = prime * result + (description == null ? 0 : description.hashCode());
        result = prime * result + (multiple ? 1231 : 1237);
        result = prime * result + (type == null ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BusinessDataDefinitionImpl other = (BusinessDataDefinitionImpl) obj;
        if (className == null) {
            if (other.className != null) {
                return false;
            }
        } else if (!className.equals(other.className)) {
            return false;
        }
        if (defaultValueExpression == null) {
            if (other.defaultValueExpression != null) {
                return false;
            }
        } else if (!defaultValueExpression.equals(other.defaultValueExpression)) {
            return false;
        }
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (multiple != other.multiple) {
            return false;
        }
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        return true;
    }

    @Override
    public void accept(ModelFinderVisitor visitor, long modelId) {
        visitor.find(this, modelId);
    }
}
