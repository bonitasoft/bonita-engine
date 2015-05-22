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
        this.explanation = explanation;
        this.expression = expression;
        inputNames = new ArrayList<String>();
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

}
