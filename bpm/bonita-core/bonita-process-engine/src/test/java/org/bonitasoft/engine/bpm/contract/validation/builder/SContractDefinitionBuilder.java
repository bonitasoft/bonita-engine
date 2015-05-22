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
 */
package org.bonitasoft.engine.bpm.contract.validation.builder;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.core.process.definition.model.SConstraintDefinition;
import org.bonitasoft.engine.core.process.definition.model.SContractDefinition;
import org.bonitasoft.engine.core.process.definition.model.SInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.impl.SContractDefinitionImpl;

public class SContractDefinitionBuilder {

    private final List<SInputDefinition> inputDefinitions = new ArrayList<>();
    private final List<SConstraintDefinition> constraints = new ArrayList<>();

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
        inputDefinitions.add(input);
        return this;
    }

    public SContractDefinitionBuilder withConstraint(final SConstraintDefinition constraint) {
        constraints.add(constraint);
        return this;
    }

    public SContractDefinition build() {
        final SContractDefinitionImpl contract = new SContractDefinitionImpl();
        for (final SInputDefinition input : inputDefinitions) {
            contract.addInput(input);
        }
        for (final SConstraintDefinition constraint : constraints) {
            contract.addConstraint(constraint);
        }
        return contract;
    }
}
