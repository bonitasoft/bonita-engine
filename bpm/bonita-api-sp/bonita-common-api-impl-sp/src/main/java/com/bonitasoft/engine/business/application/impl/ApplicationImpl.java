/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.application.impl;

import com.bonitasoft.engine.business.application.Application;

/**
 * @author Elias Ricken de Medeiros
 * @deprecated from version 7.0 on, use {@link org.bonitasoft.engine.business.application.impl.ApplicationImpl} instead.
 */
@Deprecated
public class ApplicationImpl extends org.bonitasoft.engine.business.application.impl.ApplicationImpl implements Application {

    public ApplicationImpl(final org.bonitasoft.engine.business.application.Application application) {
        super(application.getToken(), application.getVersion(), application.getDescription());
        setIconPath(application.getIconPath());
        setCreationDate(application.getCreationDate());
        setCreatedBy(application.getCreatedBy());
        setLastUpdateDate(application.getLastUpdateDate());
        setUpdatedBy(application.getUpdatedBy());
        setState(application.getState());
        setHomePageId(application.getHomePageId());
        setDisplayName(application.getDisplayName());
        setProfileId(application.getProfileId());
        setId(application.getId());
    }

}
