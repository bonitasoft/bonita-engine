/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.businessdata.impl;

import java.util.ArrayList;
import java.util.List;

import com.bonitasoft.engine.businessdata.MultipleBusinessDataReference;

/**
 * @author Matthieu Chaffotte
 */
public class MultipleBusinessDataReferenceImpl extends BusinessDataReferenceImpl implements MultipleBusinessDataReference {

    private static final long serialVersionUID = -8221290488745270659L;

    private final List<Long> storageIds;

    public MultipleBusinessDataReferenceImpl(final String name, final String type, final List<Long> storageIds) {
        super(name, type);
        this.storageIds = new ArrayList<Long>();
        for (final Long storageId : storageIds) {
            this.storageIds.add(storageId);
        }
    }

    @Override
    public List<Long> getStorageIds() {
        return storageIds;
    }

}
