/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/
package com.bonitasoft.engine.business.application.model.builder.impl;

import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

import com.bonitasoft.engine.business.application.model.builder.SApplicationUpdateBuilder;

/**
 * @author Elias Ricken de Medeiros
 * @author Emmanuel Duchastenier
 */
public class SApplicationUpdateBuilderImpl implements SApplicationUpdateBuilder {

    private final EntityUpdateDescriptor descriptor;

    public SApplicationUpdateBuilderImpl(final long updaterUserId) {
        descriptor = new EntityUpdateDescriptor();
        descriptor.addField(SApplicationFields.UPDATED_BY, updaterUserId);
        descriptor.addField(SApplicationFields.LAST_UPDATE_DATE, System.currentTimeMillis());
    }

    @Override
    public EntityUpdateDescriptor done() {
        return descriptor;
    }

    @Override
    public SApplicationUpdateBuilder updateToken(final String token) {
        descriptor.addField(SApplicationFields.TOKEN, token);
        return this;
    }

    @Override
    public SApplicationUpdateBuilder updateDisplayName(final String displayName) {
        descriptor.addField(SApplicationFields.DISPLAY_NAME, displayName);
        return this;
    }

    @Override
    public SApplicationUpdateBuilder updateVersion(final String version) {
        descriptor.addField(SApplicationFields.VERSION, version);
        return this;
    }

    @Override
    public SApplicationUpdateBuilder updateDescription(final String description) {
        descriptor.addField(SApplicationFields.DESCRIPTION, description);
        return this;
    }

    @Override
    public SApplicationUpdateBuilder updateIconPath(final String iconPath) {
        descriptor.addField(SApplicationFields.ICON_PATH, iconPath);
        return this;
    }

    @Override
    public SApplicationUpdateBuilder updateState(final String state) {
        descriptor.addField(SApplicationFields.STATE, state);
        return this;
    }

    @Override
    public SApplicationUpdateBuilder updateProfileId(final Long profileId) {
        descriptor.addField(SApplicationFields.PROFILE_ID, profileId);
        return this;
    }

    @Override
    public SApplicationUpdateBuilder updateHomePageId(final Long homePageId) {
        descriptor.addField(SApplicationFields.HOME_PAGE_ID, homePageId);
        return this;
    }

}
