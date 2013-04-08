/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package com.bonitasoft.engine.core.process.instance.model.builder.impl;

import com.bonitasoft.engine.core.process.instance.model.breakpoint.SBreakpoint;
import com.bonitasoft.engine.core.process.instance.model.breakpoint.impl.SBreakpointImpl;
import com.bonitasoft.engine.core.process.instance.model.builder.SBreakpointBuilder;

/**
 * @author Baptiste Mesta
 */
public class SBreakpointBuilderImpl implements SBreakpointBuilder {

    private static final String DEFINITION_ID = "definitionId";

    private static final String INSTANCE_ID = "instanceId";

    private static final String ELEMENT_NAME = "elementName";

    private static final String STATE_ID = "stateId";

    private static final String INTERRUPTED_STATE_ID = "interruptedSateId";

    private SBreakpointImpl entity;

    @Override
    public SBreakpoint done() {
        return entity;
    }

    @Override
    public SBreakpointBuilder createNewInstance(final long definitionId, final long instanceId, final String elementName, final int idOfTheStateToInterrupt,
            final int idOfTheInterruptingState) {
        entity = new SBreakpointImpl();
        entity.setDefinitionId(definitionId);
        entity.setInstanceId(instanceId);
        entity.setElementName(elementName);
        entity.setStateId(idOfTheStateToInterrupt);
        entity.setInterruptedStateId(idOfTheInterruptingState);
        entity.setInstanceScope(true);
        return this;
    }

    @Override
    public SBreakpointBuilder createNewInstance(final long definitionId, final String elementName, final int idOfTheStateToInterrupt,
            final int idOfTheInterruptingState) {
        entity = new SBreakpointImpl();
        entity.setDefinitionId(definitionId);
        entity.setElementName(elementName);
        entity.setStateId(idOfTheStateToInterrupt);
        entity.setInterruptedStateId(idOfTheInterruptingState);
        entity.setInstanceScope(false);
        return this;
    }

    @Override
    public String getDefinitionIdKey() {
        return DEFINITION_ID;
    }

    @Override
    public String getInstanceIdKey() {
        return INSTANCE_ID;
    }

    @Override
    public String getElementNameKey() {
        return ELEMENT_NAME;
    }

    @Override
    public String getStateIdKey() {
        return STATE_ID;
    }

    @Override
    public String getInterruptedStateIdKey() {
        return INTERRUPTED_STATE_ID;
    }

}
