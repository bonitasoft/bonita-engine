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

import org.bonitasoft.engine.bpm.contract.ComplexInputDefinition;
import org.bonitasoft.engine.bpm.contract.SimpleInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.SComplexInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.SSimpleInputDefinition;

/**
 * @author Laurent Leseigneur
 */
public class SComplexInputDefinitionImpl extends SInputDefinitionImpl implements SComplexInputDefinition {

    private static final long serialVersionUID = -3668206675533103458L;

    private final List<SSimpleInputDefinition> simpleInputDefinitions;

    private final List<SComplexInputDefinition> complexInputDefinitions;

    public SComplexInputDefinitionImpl(final String name, final String description, final List<SSimpleInputDefinition> inputDefinitions,
            final List<SComplexInputDefinition> complexDefinitions) {
        super(name);
        simpleInputDefinitions = inputDefinitions;
        complexInputDefinitions = complexDefinitions;

        setDescription(description);
    }

    public SComplexInputDefinitionImpl(final ComplexInputDefinition input) {
        this(input.getName());
        setDescription(input.getDescription());
        for (final SimpleInputDefinition simpleInputDefinition : input.getSimpleInputDefinitions()) {
            simpleInputDefinitions.add(new SSimpleInputDefinitionImpl(simpleInputDefinition));
        }
        for (final ComplexInputDefinition complexInputDefinition : input.getComplexInputDefinitions()) {
            complexInputDefinitions.add(new SComplexInputDefinitionImpl(complexInputDefinition));
        }
    }

    public SComplexInputDefinitionImpl(final String name) {
        super(name);
        simpleInputDefinitions = new ArrayList<SSimpleInputDefinition>();
        complexInputDefinitions = new ArrayList<SComplexInputDefinition>();
    }

    @Override
    public List<SSimpleInputDefinition> getSimpleInputDefinitions() {
        return simpleInputDefinitions;
    }

    @Override
    public List<SComplexInputDefinition> getComplexInputDefinitions() {
        return complexInputDefinitions;
    }
}
