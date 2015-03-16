/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.businessdata.impl;

import com.bonitasoft.engine.businessdata.MultipleBusinessDataReference;

/**
 * @author Matthieu Chaffotte
 * @deprecated since version 7.0.0, use {@link org.bonitasoft.engine.business.data.impl.MultipleBusinessDataReferenceImpl}
 */
@Deprecated
public class MultipleBusinessDataReferenceImpl extends org.bonitasoft.engine.business.data.impl.MultipleBusinessDataReferenceImpl implements MultipleBusinessDataReference {

    private static final long serialVersionUID = -8221290488745270659L;

    public MultipleBusinessDataReferenceImpl(org.bonitasoft.engine.business.data.MultipleBusinessDataReference dataReference) {
        super(dataReference.getName(), dataReference.getType(), dataReference.getStorageIds());
    }

}
