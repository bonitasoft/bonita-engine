/*******************************************************************************
 * Copyright (C) 2011, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl.transaction.profile;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.builder.SProfileUpdateBuilder;
import org.bonitasoft.engine.profile.builder.SProfileUpdateBuilderFactory;
import org.bonitasoft.engine.profile.exception.profile.SProfileUpdateException;
import org.bonitasoft.engine.profile.model.SProfile;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

import com.bonitasoft.engine.profile.ProfileUpdater;
import com.bonitasoft.engine.profile.ProfileUpdater.ProfileField;

/**
 * @author Julien Mege
 * @author Celine Souchet
 */
public class UpdateProfile implements TransactionContentWithResult<SProfile> {

    private final ProfileService profileService;

    private final Long profileId;

    private final ProfileUpdater updateDescriptor;

    private final long lastUpdatorId;

    private SProfile sProfile = null;

    public UpdateProfile(final ProfileService profileService, final Long profileId, final ProfileUpdater updateDescriptor, final long lastUpdatorId) {
        super();
        this.profileService = profileService;
        this.profileId = profileId;
        this.updateDescriptor = updateDescriptor;
        this.lastUpdatorId = lastUpdatorId;
    }

    @Override
    public void execute() throws SBonitaException {
        sProfile = profileService.getProfile(profileId);
        if (!sProfile.isDefault()) {
            sProfile = profileService.updateProfile(sProfile, getProfileUpdateDescriptor());
        } else {
            throw new SProfileUpdateException("Can't update a default profile. Profile id = <" + profileId + ">, name = <" + sProfile.getName() + ">");
        }
    }

    @Override
    public SProfile getResult() {
        return sProfile;
    }

    private EntityUpdateDescriptor getProfileUpdateDescriptor() {
        final SProfileUpdateBuilder updateBuilder = BuilderFactory.get(SProfileUpdateBuilderFactory.class).createNewInstance();
        final Map<ProfileField, Serializable> fields = updateDescriptor.getFields();
        for (final Entry<ProfileField, Serializable> field : fields.entrySet()) {
            switch (field.getKey()) {
                case NAME:
                    updateBuilder.setName((String) field.getValue());
                    break;
                case DESCRIPTION:
                    updateBuilder.setDescription((String) field.getValue());
                    break;
                case ICON_PATH:
                    updateBuilder.setIconPath((String) field.getValue());
                    break;
                default:
                    throw new IllegalStateException();
            }
        }
        updateBuilder.setLastUpdateDate(System.currentTimeMillis()).setLastUpdatedBy(lastUpdatorId);
        return updateBuilder.done();
    }

}
