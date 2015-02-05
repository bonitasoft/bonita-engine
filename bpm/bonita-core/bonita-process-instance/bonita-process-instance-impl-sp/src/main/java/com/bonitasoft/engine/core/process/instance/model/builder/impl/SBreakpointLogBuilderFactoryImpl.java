/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.core.process.instance.model.builder.impl;

import org.bonitasoft.engine.queriablelogger.model.builder.impl.CRUDELogBuilderFactory;

import com.bonitasoft.engine.core.process.instance.model.builder.SBreakpointLogBuilder;
import com.bonitasoft.engine.core.process.instance.model.builder.SBreakpointLogBuilderFactory;

/**
 * @author Baptiste Mesta
 */
public class SBreakpointLogBuilderFactoryImpl extends CRUDELogBuilderFactory implements SBreakpointLogBuilderFactory {

    @Override
    public SBreakpointLogBuilder createNewInstance() {
        return new SBreakpointLogBuilderImpl();
    }
    
    @Override
    public String getObjectIdKey() {
        return "numericIndex2";
    }

    @Override
    public String getDefinitionIdKey() {
        return "numericIndex1";
    }

}
