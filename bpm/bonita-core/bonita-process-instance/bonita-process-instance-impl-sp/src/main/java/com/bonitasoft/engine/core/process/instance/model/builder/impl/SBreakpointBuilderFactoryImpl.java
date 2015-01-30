/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.core.process.instance.model.builder.impl;

import com.bonitasoft.engine.core.process.instance.model.breakpoint.impl.SBreakpointImpl;
import com.bonitasoft.engine.core.process.instance.model.builder.SBreakpointBuilder;
import com.bonitasoft.engine.core.process.instance.model.builder.SBreakpointBuilderFactory;

/**
 * @author Baptiste Mesta
 */
public class SBreakpointBuilderFactoryImpl implements SBreakpointBuilderFactory {

    private static final String DEFINITION_ID = "definitionId";

    private static final String INSTANCE_ID = "instanceId";

    private static final String ELEMENT_NAME = "elementName";

    private static final String STATE_ID = "stateId";

    private static final String INTERRUPTED_STATE_ID = "interruptedSateId";


    @Override
    public SBreakpointBuilder createNewInstance(final long definitionId, final long instanceId, final String elementName, final int idOfTheStateToInterrupt,
            final int idOfTheInterruptingState) {
        final SBreakpointImpl entity = new SBreakpointImpl();
        entity.setDefinitionId(definitionId);
        entity.setInstanceId(instanceId);
        entity.setElementName(elementName);
        entity.setStateId(idOfTheStateToInterrupt);
        entity.setInterruptedStateId(idOfTheInterruptingState);
        entity.setInstanceScope(true);
        return new SBreakpointBuilderImpl(entity);
    }

    @Override
    public SBreakpointBuilder createNewInstance(final long definitionId, final String elementName, final int idOfTheStateToInterrupt,
            final int idOfTheInterruptingState) {
        final SBreakpointImpl entity = new SBreakpointImpl();
        entity.setDefinitionId(definitionId);
        entity.setElementName(elementName);
        entity.setStateId(idOfTheStateToInterrupt);
        entity.setInterruptedStateId(idOfTheInterruptingState);
        entity.setInstanceScope(false);
        return new SBreakpointBuilderImpl(entity);
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
