/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.businessdata.impl;

import com.bonitasoft.engine.businessdata.SimpleBusinessDataReference;

/**
 * @author Matthieu Chaffotte
 */
public class SimpleBusinessDataReferenceImpl extends BusinessDataReferenceImpl implements SimpleBusinessDataReference {

    private static final long serialVersionUID = -434357449996998735L;

    private final Long storageId;

    public SimpleBusinessDataReferenceImpl(final String name, final String type, final Long storageId) {
        super(name, type);
        this.storageId = storageId;
    }

    @Override
    public Long getStorageId() {
        return storageId;
    }

}
