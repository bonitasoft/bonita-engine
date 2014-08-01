/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.core.process.instance.model.impl;

import com.bonitasoft.engine.core.process.instance.model.SProcessMultiRefBusinessDataInstance;

/**
 * @author Matthieu Chaffotte
 */
public class SProcessMultiRefBusinessDataInstanceImpl extends SMultiRefBusinessDataInstanceImpl implements SProcessMultiRefBusinessDataInstance {

    private static final long serialVersionUID = -8285156092879797973L;

    private long processInstanceId;

    @Override
    public long getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(final long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

}
