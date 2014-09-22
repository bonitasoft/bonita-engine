/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
package org.bonitasoft.engine.core.process.definition.model.impl;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.bpm.contract.ConstraintDefinition;
import org.bonitasoft.engine.core.process.definition.model.SConstraintDefinition;

/**
 * @author Matthieu Chaffotte
 */
public class SConstraintDefinitionImpl extends SNamedElementImpl implements SConstraintDefinition {

    private static final long serialVersionUID = 1663246823634006952L;

    private final String expression;

    private final String explanation;

    private final List<String> inputNames;

    public SConstraintDefinitionImpl(final String name, final String expression, final String explanation) {
        super(name);
        inputNames = new ArrayList<String>();
        this.explanation = explanation;
        this.expression = expression;
    }

    public SConstraintDefinitionImpl(final ConstraintDefinition rule) {
        this(rule.getName(), rule.getExpression(), rule.getExplanation());
        for (final String inputName : rule.getInputNames()) {
            inputNames.add(inputName);
        }
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (explanation == null ? 0 : explanation.hashCode());
        result = prime * result + (expression == null ? 0 : expression.hashCode());
        result = prime * result + (inputNames == null ? 0 : inputNames.hashCode());
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
        final SConstraintDefinitionImpl other = (SConstraintDefinitionImpl) obj;
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
        return true;
    }

}
