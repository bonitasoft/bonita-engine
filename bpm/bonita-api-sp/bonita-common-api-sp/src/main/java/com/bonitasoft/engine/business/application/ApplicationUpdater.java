/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.application;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.profile.Profile;

import com.bonitasoft.engine.converter.EnumConverter;

/**
 * Allows to define which {@link Application} fields will be updated
 *
 * @author Elias Ricken de Medeiros
 * @deprecated from version 7.0 on, use {@link org.bonitasoft.engine.business.application.ApplicationUpdater} instead.
 * @see Application
 * @see org.bonitasoft.engine.business.application.ApplicationUpdater
 */
@Deprecated
public class ApplicationUpdater implements Serializable {

    private static final long serialVersionUID = 4565052647320534796L;

    org.bonitasoft.engine.business.application.ApplicationUpdater delegate;

    /**
     * Creates an instance of <code>ApplicationUpdater</code>
     */
    public ApplicationUpdater() {
        delegate = new org.bonitasoft.engine.business.application.ApplicationUpdater();
    }

    /**
     * Retrieves all fields to be updated
     *
     * @return a {@link Map}<{@link ApplicationField}, {@link Serializable}> containing all fields to be updated
     * @see ApplicationField
     */
    public Map<ApplicationField, Serializable> getFields() {
        return new EnumConverter().convert(delegate.getFields(), ApplicationField.class);
    }

    /**
     * Defines the new value for the {@link Application} token. It cannot be empty or null and should contain only alpha numeric
     * characters and the following special characters '-', '.', '_' or '~'.
     *
     * @param token the new value for the {@code Application} token
     * @return the current {@code ApplicationUpdater}
     * @see Application
     */
    public ApplicationUpdater setToken(final String token) {
        delegate.setToken(token);
        return this;
    }

    /**
     * Defines the new value for the {@link Application} display name. It cannot be empty or null.
     *
     * @param displayName the new value for the {@code Application} display name
     * @return the current {@code ApplicationUpdater}
     * @see Application
     */
    public ApplicationUpdater setDisplayName(final String displayName) {
        delegate.setDisplayName(displayName);
        return this;
    }

    /**
     * Defines the new value for the {@link Application} version
     *
     * @param version the new value for the {@code Application} version
     * @return the current {@code ApplicationUpdater}
     * @see Application
     */
    public ApplicationUpdater setVersion(final String version) {
        delegate.setVersion(version);
        return this;
    }

    /**
     * Defines the new value for the {@link Application} description
     *
     * @param description the new value for the {@code Application} description
     * @return the current {@code ApplicationUpdater}
     * @see Application
     */
    public ApplicationUpdater setDescription(final String description) {
        delegate.setDescription(description);
        return this;
    }

    /**
     * Defines the new value for the {@link Application} icon path
     *
     * @param iconPath the new value for the {@code Application} icon path
     * @return the current {@code ApplicationUpdater}
     * @see Application
     */
    public ApplicationUpdater setIconPath(final String iconPath) {
        delegate.setIconPath(iconPath);
        return this;
    }

    /**
     * Defines the new value for the {@link Application} state
     *
     * @param state the new value for the {@code Application} state
     * @return the current {@code ApplicationUpdater}
     * @see Application
     */
    public ApplicationUpdater setState(final String state) {
        delegate.setState(state);
        return this;
    }

    /**
     * Defines the identifier of the new {@link Profile} associated to the {@link Application}
     *
     * @param profileId the identifier of {@code Profile} associated to the {@code Application}
     * @return the current {@code ApplicationUpdater}
     * @see Application
     * @see Profile
     */
    public ApplicationUpdater setProfileId(final Long profileId) {
        delegate.setProfileId(profileId);
        return this;
    }

    /**
     * Defines the identifier of the new {@link com.bonitasoft.engine.business.application.ApplicationPage} defined as the {@link Application} home page
     *
     * @param applicationPageId the identifier of {@code ApplicationPage} associated to the {@code Application}
     * @return the current {@code ApplicationUpdater}
     * @see Application
     * @see com.bonitasoft.engine.business.application.ApplicationPage
     */
    public ApplicationUpdater setHomePageId(final Long applicationPageId) {
        delegate.setHomePageId(applicationPageId);
        return this;
    }

    /**
     * Determines if this updater has at least one field to update
     *
     * @return true if there is at least one field to update; false otherwise
     */
    public boolean hasFields() {
        return !getFields().isEmpty();
    }

    public org.bonitasoft.engine.business.application.ApplicationUpdater getDelegate() {
        return delegate;
    }

    @Override
    public boolean equals(final Object obj) {
        return delegate.equals(obj);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }
}
