/*******************************************************************************
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.core.process.instance.model.builder.impl;

import com.bonitasoft.engine.core.process.instance.model.breakpoint.SBreakpoint;
import com.bonitasoft.engine.core.process.instance.model.breakpoint.impl.SBreakpointImpl;
import com.bonitasoft.engine.core.process.instance.model.builder.SBreakpointBuilder;

/**
 * @author Baptiste Mesta
 */
public class SBreakpointBuilderImpl implements SBreakpointBuilder {

    private final SBreakpointImpl entity;
    
    public SBreakpointBuilderImpl(final SBreakpointImpl entity) {
        super();
        this.entity = entity;
    }

    @Override
    public SBreakpoint done() {
        return entity;
    }

}
