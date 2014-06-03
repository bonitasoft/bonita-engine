/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.core.process.instance.model.builder.impl;

import com.bonitasoft.engine.core.process.instance.model.SRefBusinessDataInstance;
import com.bonitasoft.engine.core.process.instance.model.builder.SRefBusinessDataInstanceBuilder;
import com.bonitasoft.engine.core.process.instance.model.impl.SRefBusinessDataInstanceImpl;

/**
 * @author Matthieu Chaffotte
 */
public class SRefBusinessDataInstanceBuilderImpl implements SRefBusinessDataInstanceBuilder {

    private final SRefBusinessDataInstanceImpl entity;

    public SRefBusinessDataInstanceBuilderImpl(final SRefBusinessDataInstanceImpl entity) {
        super();
        this.entity = entity;
    }

    @Override
    public SRefBusinessDataInstance done() {
        return entity;
    }

}
