/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.core.process.instance.model.builder.impl;

import org.bonitasoft.engine.core.process.instance.model.builder.impl.SProcessInstanceUpdateBuilderImpl;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

import com.bonitasoft.engine.core.process.instance.model.builder.SProcessInstanceUpdateBuilder;

/**
 * @author Celine Souchet
 */
public class SProcessInstanceUpdateBuilderExt extends SProcessInstanceUpdateBuilderImpl implements SProcessInstanceUpdateBuilder {

    public SProcessInstanceUpdateBuilderExt(final EntityUpdateDescriptor descriptor) {
        super(descriptor);
    }
    
    @Override
    public SProcessInstanceUpdateBuilder updateStringIndex1(final String stringIndex) {
        descriptor.addField(SProcessInstanceBuilderFactoryExt.STRING_INDEX_1_KEY, stringIndex);
        return this;
    }

    @Override
    public SProcessInstanceUpdateBuilder updateStringIndex2(final String stringIndex) {
        descriptor.addField(SProcessInstanceBuilderFactoryExt.STRING_INDEX_2_KEY, stringIndex);
        return this;
    }

    @Override
    public SProcessInstanceUpdateBuilder updateStringIndex3(final String stringIndex) {
        descriptor.addField(SProcessInstanceBuilderFactoryExt.STRING_INDEX_3_KEY, stringIndex);
        return this;
    }

    @Override
    public SProcessInstanceUpdateBuilder updateStringIndex4(final String stringIndex) {
        descriptor.addField(SProcessInstanceBuilderFactoryExt.STRING_INDEX_4_KEY, stringIndex);
        return this;
    }

    @Override
    public SProcessInstanceUpdateBuilder updateStringIndex5(final String stringIndex) {
        descriptor.addField(SProcessInstanceBuilderFactoryExt.STRING_INDEX_5_KEY, stringIndex);
        return this;
    }
    
    public static SProcessInstanceUpdateBuilderExt getInstance() {
        return new SProcessInstanceUpdateBuilderExt(null);
    }

}
