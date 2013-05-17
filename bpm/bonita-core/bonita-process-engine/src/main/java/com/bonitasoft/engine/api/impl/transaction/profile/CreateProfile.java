/*******************************************************************************
 * Copyright (C) 2012-2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl.transaction.profile;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.profile.ProfileCreator;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.builder.SProfileBuilder;
import org.bonitasoft.engine.profile.model.SProfile;

import com.bonitasoft.engine.service.SPModelConvertor;

/**
 * @author Julien Mege
 * @author Celine Souchet
 */
public class CreateProfile implements TransactionContentWithResult<SProfile> {

    private final ProfileService profileService;

    private final SProfileBuilder sProfileBuilder;

    private final ProfileCreator creator;

    private SProfile profile;

    public CreateProfile(final ProfileService profileService, final SProfileBuilder sProfileBuilder, final ProfileCreator creator) {
        super();
        this.profileService = profileService;
        this.sProfileBuilder = sProfileBuilder;
        this.creator = creator;
    }

    @Override
    public void execute() throws SBonitaException {
        profile = profileService.createProfile(SPModelConvertor.constructSProfile(creator, sProfileBuilder));
    }

    @Override
    public SProfile getResult() {
        return profile;
    }

}
