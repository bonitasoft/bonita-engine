/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.core.process.instance.model.impl;

import com.bonitasoft.engine.core.process.instance.model.SSimpleRefBusinessDataInstance;

/**
 * @author Matthieu Chaffotte
 */
public abstract class SSimpleRefBusinessDataInstanceImpl extends SRefBusinessDataInstanceImpl implements SSimpleRefBusinessDataInstance {

    private static final long serialVersionUID = -6240483858780514216L;

    private Long dataId;

    public SSimpleRefBusinessDataInstanceImpl() {
        super();
    }

    @Override
    public Long getDataId() {
        return dataId;
    }

    public void setDataId(final Long dataId) {
        this.dataId = dataId;
    }

}
