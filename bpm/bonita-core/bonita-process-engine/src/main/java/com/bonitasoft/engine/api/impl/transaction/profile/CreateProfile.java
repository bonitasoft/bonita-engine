/*******************************************************************************
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl.transaction.profile;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.model.SProfile;

/**
 * @author Julien Mege
 */
public class CreateProfile implements TransactionContentWithResult<SProfile> {

    private final ProfileService profileService;

    private SProfile profile;

    public CreateProfile(final ProfileService profileService, final SProfile profile) {
        super();
        this.profileService = profileService;
        this.profile = profile;
    }

    @Override
    public void execute() throws SBonitaException {
        this.profile = this.profileService.createProfile(this.profile);
    }

    @Override
    public SProfile getResult() {
        return this.profile;
    }

}
