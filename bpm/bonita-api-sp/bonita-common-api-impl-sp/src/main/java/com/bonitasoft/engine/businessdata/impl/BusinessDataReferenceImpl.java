/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.businessdata.impl;

import com.bonitasoft.engine.businessdata.BusinessDataReference;

/**
 * @author Matthieu Chaffotte
 */
public class BusinessDataReferenceImpl implements BusinessDataReference {

    private static final long serialVersionUID = -6913883854275484141L;

    private final String name;

    private final String type;

    public BusinessDataReferenceImpl(final String name, final String type) {
        super();
        this.name = name;
        this.type = type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return type;
    }

}
