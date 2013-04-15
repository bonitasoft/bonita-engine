/*******************************************************************************
 * Copyright (C) 2011, 2012 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.core.process.instance.model.builder.impl;

import org.bonitasoft.engine.core.process.instance.model.builder.impl.BPMinstanceBuildersImpl;

import com.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;
import com.bonitasoft.engine.core.process.instance.model.builder.SBreakpointBuilder;
import com.bonitasoft.engine.core.process.instance.model.builder.SBreakpointLogBuilder;

/**
 * @author Celine Souchet
 */
public class BPMinstanceBuildersExt extends BPMinstanceBuildersImpl implements BPMInstanceBuilders {

    @Override
    public SBreakpointBuilder getSBreakpointBuilder() {
        return new SBreakpointBuilderImpl();
    }

    @Override
    public SBreakpointLogBuilder getSBreakpointLogBuilder() {
        return new SBreakpointLogBuilderImpl();
    }

}
