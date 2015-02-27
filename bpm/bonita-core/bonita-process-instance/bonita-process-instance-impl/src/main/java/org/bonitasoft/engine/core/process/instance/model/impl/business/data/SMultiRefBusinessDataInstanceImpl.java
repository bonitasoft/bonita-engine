/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package org.bonitasoft.engine.core.process.instance.model.impl.business.data;

import org.bonitasoft.engine.core.process.instance.model.business.data.SMultiRefBusinessDataInstance;

import java.util.List;

/**
 * @author Matthieu Chaffotte
 */
public abstract class SMultiRefBusinessDataInstanceImpl extends SRefBusinessDataInstanceImpl implements SMultiRefBusinessDataInstance {

    private static final long serialVersionUID = -7182225911903915352L;

    private List<Long> dataIds;

    public SMultiRefBusinessDataInstanceImpl() {
        super();
    }

    public void setDataIds(final List<Long> dataIds) {
        this.dataIds = dataIds;
    }

    @Override
    public List<Long> getDataIds() {
        return dataIds;
    }

}
