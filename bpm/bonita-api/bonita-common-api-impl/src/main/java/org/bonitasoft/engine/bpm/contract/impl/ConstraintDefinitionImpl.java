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
package org.bonitasoft.engine.bpm.contract.impl;

import org.bonitasoft.engine.bpm.contract.ConstraintDefinition;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Matthieu Chaffotte
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ConstraintDefinitionImpl implements ConstraintDefinition {

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (explanation == null ? 0 : explanation.hashCode());
        result = prime * result + (expression == null ? 0 : expression.hashCode());
        result = prime * result + (inputNames == null ? 0 : inputNames.hashCode());
        result = prime * result + (name == null ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        switch (NaiveEqualityResult.checkEquality(this, obj)) {
            case RETURN_FALSE:
                return false;
            case RETURN_TRUE:
                return true;
            case CONTINUE:
            default:
                break;
        }
        final ConstraintDefinitionImpl other = (ConstraintDefinitionImpl) obj;
        if (explanation == null) {
            if (other.explanation != null) {
                return false;
            }
        } else if (!explanation.equals(other.explanation)) {
            return false;
        }
        if (expression == null) {
            if (other.expression != null) {
                return false;
            }
        } else if (!expression.equals(other.expression)) {
            return false;
        }
        if (inputNames == null) {
            if (other.inputNames != null) {
                return false;
            }
        } else if (!inputNames.equals(other.inputNames)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    private static final long serialVersionUID = 2793703451225519896L;
    @XmlAttribute
    private final String name;
    @XmlElement
    private final String expression;
    @XmlElement
    private final String explanation;
    @XmlElementWrapper
    @XmlElement(name = "inputName")
    private final List<String> inputNames;

    public ConstraintDefinitionImpl(final String name, final String expression, final String explanation) {
        this.name = name;
        this.explanation = explanation;
        this.expression = expression;
        inputNames = new ArrayList<String>();
    }

    public ConstraintDefinitionImpl() {
        this.name = null;
        this.explanation = null;
        this.expression = null;
        inputNames = new ArrayList<String>();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getExpression() {
        return expression;
    }

    @Override
    public String getExplanation() {
        return explanation;
    }

    @Override
    public List<String> getInputNames() {
        return inputNames;
    }

    public void addInputName(final String inputName) {
        inputNames.add(inputName);
    }

}
