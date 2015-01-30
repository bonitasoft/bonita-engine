/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.application.impl;

import com.bonitasoft.engine.business.application.ApplicationPage;

/**
 * @author Elias Ricken de Medeiros
 * @deprecated from version 7.0 on, use {@link org.bonitasoft.engine.business.application.impl.ApplicationPageImpl} instead.
 * @see org.bonitasoft.engine.business.application.impl.ApplicationPageImpl
 */
@Deprecated
public class ApplicationPageImpl extends org.bonitasoft.engine.business.application.impl.ApplicationPageImpl implements ApplicationPage {

    private static final long serialVersionUID = -8043272410231723583L;

    public ApplicationPageImpl(final org.bonitasoft.engine.business.application.ApplicationPage toConvert) {
        super(toConvert.getApplicationId(), toConvert.getPageId(), toConvert.getToken());
        setId(toConvert.getId());
    }

}
