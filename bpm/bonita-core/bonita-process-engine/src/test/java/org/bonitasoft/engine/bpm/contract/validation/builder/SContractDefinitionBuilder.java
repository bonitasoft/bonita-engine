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

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.core.process.definition.model.SComplexInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.SConstraintDefinition;
import org.bonitasoft.engine.core.process.definition.model.SConstraintType;
import org.bonitasoft.engine.core.process.definition.model.SContractDefinition;
import org.bonitasoft.engine.core.process.definition.model.SInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.SSimpleInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.impl.SConstraintDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SContractDefinitionImpl;

public class SContractDefinitionBuilder {

    private final List<SSimpleInputDefinition> simpleInputs = new ArrayList<SSimpleInputDefinition>();
    private final List<SComplexInputDefinition> complexInputs = new ArrayList<SComplexInputDefinition>();
    private final List<SConstraintDefinition> constraints = new ArrayList<SConstraintDefinition>();

    public static SContractDefinitionBuilder aContract() {
        return new SContractDefinitionBuilder();
    }

    public SContractDefinitionBuilder withInput(final SSimpleInputDefinitionBuilder builder) {
        return withInput(builder.build());
    }

    public SContractDefinitionBuilder withInput(final SComplexInputDefinitionBuilder builder) {
        return withInput(builder.build());
    }

    public SContractDefinitionBuilder withInput(final SInputDefinition input) {
        if (input instanceof SSimpleInputDefinition) {
            simpleInputs.add((SSimpleInputDefinition) input);
        }
        if (input instanceof SComplexInputDefinition) {
            complexInputs.add((SComplexInputDefinition) input);
        }
        return this;
    }

    public SContractDefinitionBuilder withConstraint(final SConstraintDefinition constraint) {
        constraints.add(constraint);
        return this;
    }

    public SContractDefinitionBuilder withMandatoryConstraint(final String inputName) {
        final StringBuilder expression = new StringBuilder().append(inputName).append("!=null");
        expression.append(" && !");
        expression.append(inputName);
        expression.append(".toString().isEmpty()");

        final SConstraintDefinition constraint = new SConstraintDefinitionImpl(inputName, expression.toString(), new StringBuilder().append("input ")
                .append(inputName).append(" is mandatory").toString(), SConstraintType.MANDATORY);
        constraint.getInputNames().add(inputName);
        constraints.add(constraint);
        return this;
    }

    public SContractDefinition build() {
        final SContractDefinitionImpl contract = new SContractDefinitionImpl();
        for (final SSimpleInputDefinition input : simpleInputs) {
            contract.addSimpleInput(input);
        }
        for (final SComplexInputDefinition input : complexInputs) {
            contract.addComplexInput(input);
        }
        for (final SConstraintDefinition constraint : constraints) {
            contract.addRule(constraint);
        }
        return contract;
    }
}
