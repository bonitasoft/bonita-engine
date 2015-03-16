/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.core.process.instance.model.builder.impl;

import org.bonitasoft.engine.core.process.instance.model.builder.impl.SProcessInstanceBuilderImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.SProcessInstanceImpl;

import com.bonitasoft.engine.core.process.instance.model.builder.SProcessInstanceBuilder;

/**
 * @author Celine Souchet
 */
public class SProcessInstanceBuilderExt extends SProcessInstanceBuilderImpl implements SProcessInstanceBuilder {

    public SProcessInstanceBuilderExt(final SProcessInstanceImpl entity) {
        super(entity);
    }
    
    @Override
    public SProcessInstanceBuilder setStringIndex(final int index, final String value) {
        switch (index) {
            case 1:
                entity.setStringIndex1(value);
                break;
            case 2:
                entity.setStringIndex2(value);
                break;
            case 3:
                entity.setStringIndex3(value);
                break;
            case 4:
                entity.setStringIndex4(value);
                break;
            case 5:
                entity.setStringIndex5(value);
                break;
            default:
                throw new IndexOutOfBoundsException("string index label must be between 1 and 5 (included)");
        }
        return this;
    }

}
