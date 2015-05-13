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
package org.bonitasoft.engine.bpm.contract.validation.builder;

import static java.util.Arrays.asList;

import java.util.List;

import org.bonitasoft.engine.core.process.definition.model.SConstraintDefinition;
import org.bonitasoft.engine.core.process.definition.model.impl.SConstraintDefinitionImpl;

public class SConstraintDefinitionBuilder {

    private final List<String> inputNames;
    private String expression;
    private String explanation;
    private String name;

    public SConstraintDefinitionBuilder(final String... inputNames) {
        this.inputNames = asList(inputNames);
    }

    public static SConstraintDefinitionBuilder aRuleFor(final String... inputName) {
        return new SConstraintDefinitionBuilder(inputName);
    }

    public SConstraintDefinitionBuilder expression(final String expression) {
        this.expression = expression;
        return this;
    }

    public SConstraintDefinitionBuilder explanation(final String explanation) {
        this.explanation = explanation;
        return this;
    }

    public SConstraintDefinitionBuilder name(final String name) {
        this.name = name;
        return this;
    }

    public SConstraintDefinition build() {
        final SConstraintDefinitionImpl rule = new SConstraintDefinitionImpl(name, expression, explanation);
        for (final String inputName : inputNames) {
            rule.addInputName(inputName);
        }
        return rule;
    }
}
