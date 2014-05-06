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
import org.bonitasoft.engine.profile.builder.SProfileEntryBuilderFactory;
import org.bonitasoft.engine.profile.builder.SProfileEntryUpdateBuilder;
import org.bonitasoft.engine.profile.builder.SProfileEntryUpdateBuilderFactory;
import org.bonitasoft.engine.profile.exception.profileentry.SProfileEntryUpdateException;
import org.bonitasoft.engine.profile.model.SProfileEntry;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

import com.bonitasoft.engine.profile.ProfileEntryUpdater;
import com.bonitasoft.engine.profile.ProfileEntryUpdater.ProfileEntryUpdateField;

/**
 * @author Julien Mege
 * @author Celine Souchet
 */
public class UpdateProfileEntry implements TransactionContentWithResult<SProfileEntry> {

    private final ProfileService profileService;

    private final Long profileEntryId;

    private final ProfileEntryUpdater updateDescriptor;

    private SProfileEntry sProfileEntry = null;

    public UpdateProfileEntry(final ProfileService profileService, final Long profileEntryId,
            final ProfileEntryUpdater updateDescriptor) {
        super();
        this.profileService = profileService;
        this.profileEntryId = profileEntryId;
        this.updateDescriptor = updateDescriptor;
    }

    @Override
    public void execute() throws SBonitaException {
        sProfileEntry = profileService.getProfileEntry(profileEntryId);
        final EntityUpdateDescriptor profileEntryUpdateDescriptor = getProfileEntryUpdateDescriptor();
        final Map<String, Object> fields = profileEntryUpdateDescriptor.getFields();
        final String type = (String) fields.get(SProfileEntryBuilderFactory.TYPE);
        final String page = (String) fields.get(SProfileEntryBuilderFactory.PAGE);
        if ("link".equalsIgnoreCase(type) && (page == null || "".equals(page))) {
            throw new SProfileEntryUpdateException("For a link, the page is mandatory.");
        }

        profileService.updateProfileEntry(sProfileEntry, profileEntryUpdateDescriptor);
        sProfileEntry = profileService.getProfileEntry(profileEntryId);
    }

    @Override
    public SProfileEntry getResult() {
        return sProfileEntry;
    }

    private EntityUpdateDescriptor getProfileEntryUpdateDescriptor() {
        final SProfileEntryUpdateBuilder updateBuilder = BuilderFactory.get(SProfileEntryUpdateBuilderFactory.class).createNewInstance();
        final Map<ProfileEntryUpdateField, Serializable> fields = updateDescriptor.getFields();
        for (final Entry<ProfileEntryUpdateField, Serializable> field : fields.entrySet()) {
            switch (field.getKey()) {
                case NAME:
                    updateBuilder.setName((String) field.getValue());
                    break;
                case DESCRIPTION:
                    updateBuilder.setDescription((String) field.getValue());
                    break;
                case PARENT_ID:
                    updateBuilder.setParentId((Long) field.getValue());
                    break;
                case PROFILE_ID:
                    updateBuilder.setProfileId((Long) field.getValue());
                    break;
                case INDEX:
                    updateBuilder.setIndex((Long) field.getValue());
                    break;
                case TYPE:
                    updateBuilder.setType((String) field.getValue());
                    break;
                case PAGE:
                    updateBuilder.setPage((String) field.getValue());
                    break;
                default:
                    throw new IllegalStateException();
            }
        }
        return updateBuilder.done();
    }

}
