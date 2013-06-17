/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl.transaction.profile;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.profile.ProfileService;

/**
 * @author Julien Mege
 */
public class DeleteProfile implements TransactionContent {

    private final ProfileService profileService;

    private final long profileId;

    public DeleteProfile(final ProfileService profileService, final long profileId) {
        super();
        this.profileService = profileService;
        this.profileId = profileId;
    }

    @Override
    public void execute() throws SBonitaException {
        this.profileService.deleteProfile(this.profileId);
    }

}
