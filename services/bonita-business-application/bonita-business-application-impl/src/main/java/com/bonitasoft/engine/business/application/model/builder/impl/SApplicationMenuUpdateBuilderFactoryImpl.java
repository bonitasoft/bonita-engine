/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/
package com.bonitasoft.engine.business.application.model.builder.impl;

import com.bonitasoft.engine.business.application.model.builder.SApplicationMenuUpdateBuilder;
import com.bonitasoft.engine.business.application.model.builder.SApplicationMenuUpdateBuilderFactory;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

import com.bonitasoft.engine.business.application.model.builder.SApplicationUpdateBuilder;
import com.bonitasoft.engine.business.application.model.builder.SApplicationUpdateBuilderFactory;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class SApplicationMenuUpdateBuilderFactoryImpl implements SApplicationMenuUpdateBuilderFactory {

    @Override
    public SApplicationMenuUpdateBuilder createNewInstance() {
        return new SApplicationMenuUpdateBuilderImpl();
    }

}
