/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.core.process.instance.model.builder.impl;

import org.bonitasoft.engine.core.process.instance.model.builder.impl.SProcessInstanceUpdateBuilderFactoryImpl;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

import com.bonitasoft.engine.core.process.instance.model.builder.SProcessInstanceUpdateBuilderFactory;

/**
 * @author Celine Souchet
 */
public class SProcessInstanceUpdateBuilderFactoryExt extends SProcessInstanceUpdateBuilderFactoryImpl implements SProcessInstanceUpdateBuilderFactory {

    @Override
    public com.bonitasoft.engine.core.process.instance.model.builder.SProcessInstanceUpdateBuilder createNewInstance() {
        return new SProcessInstanceUpdateBuilderExt(new EntityUpdateDescriptor());
    }
    
}
