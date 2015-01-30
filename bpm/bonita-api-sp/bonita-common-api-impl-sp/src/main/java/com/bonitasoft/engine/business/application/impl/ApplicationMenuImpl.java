/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/
package com.bonitasoft.engine.business.application.impl;

import com.bonitasoft.engine.business.application.ApplicationMenu;

/**
 * @author Elias Ricken de Medeiros
 * @deprecated from version 7.0 on, use {@link org.bonitasoft.engine.business.application.impl.ApplicationMenuImpl} instead.
 * @see org.bonitasoft.engine.business.application.impl.ApplicationMenuImpl
 */
@Deprecated
public class ApplicationMenuImpl extends org.bonitasoft.engine.business.application.impl.ApplicationMenuImpl implements ApplicationMenu {

    private static final long serialVersionUID = 5080525289831930498L;

    public ApplicationMenuImpl(org.bonitasoft.engine.business.application.ApplicationMenu applicationMenu) {
        super(applicationMenu.getDisplayName(), applicationMenu.getApplicationId(), applicationMenu.getApplicationPageId(), applicationMenu.getIndex());
        setParentId(applicationMenu.getParentId());
        setId(applicationMenu.getId());
    }

}
