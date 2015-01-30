/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
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
 * Describes the information about an {@link Application} to be created
 *
 * @author Elias Ricken de Medeiros
 * @since 6.4
 * @see Application
 * @see org.bonitasoft.engine.business.application.ApplicationCreator
 * @deprecated from version 7.0 on, use {@link org.bonitasoft.engine.business.application.ApplicationCreator} instead.
 */
@Deprecated
public class ApplicationCreator implements Serializable {

    private static final long serialVersionUID = -916041825489100271L;

    private final org.bonitasoft.engine.business.application.ApplicationCreator delegate;

    /**
     * Creates an instance of <code>ApplicationCreator</code> containing mandatory information
     *
     * @param token the {@link Application} token. The token will be part of application URL. It cannot be null or empty and should contain only alpha numeric
     *        characters and the following special characters '-', '.', '_' or '~'.
     * @param displayName the <code>Application</code> display name. It cannot be null or empty
     * @param version the <code>Application</code> version
     * @see Application
     */
    @Deprecated
    public ApplicationCreator(final String token, final String displayName, final String version) {
        delegate = new org.bonitasoft.engine.business.application.ApplicationCreator(token, displayName, version);
    }

    /**
     * Retrieves the {@link Application} token
     *
     * @return the <code>Application</code> token
     * @see Application
     */
    public String getToken() {
        return delegate.getToken();
    }

    /**
     * Defines the {@link Application} description and returns the current <code>ApplicationCreator</code>
     *
     * @param description the <code>Application</code> description
     * @return the current <code>ApplicationCreator</code>
     * @see Application
     */
    public ApplicationCreator setDescription(final String description) {
        delegate.setDescription(description);
        return this;
    }

    /**
     * Defines the {@link Application} icon path and returns the current <code>ApplicationCreator</code>
     *
     * @param iconPath the <code>Application</code> icon path
     * @return the current <code>ApplicationCreator</code>
     * @see Application
     */
    public ApplicationCreator setIconPath(final String iconPath) {
        delegate.setIconPath(iconPath);
        return this;
    }

    /**
     * Defines the identifier of the {@link Profile} related to this {@link Application} and returns the current <code>ApplicationCreator</code>
     *
     * @param profileId the <code>Profile</code> identifier
     * @return the current <code>ApplicationCreator</code>
     * @see Application
     * @see Profile
     */
    public ApplicationCreator setProfileId(final Long profileId) {
        delegate.setProfileId(profileId);
        return this;
    }

    /**
     * Retrieves all fields defined in this <code>ApplicationCreator</code>
     *
     * @return a {@link Map}<{@link ApplicationField}, {@link Serializable}> containing all fields defined in this <code>ApplicationCreator</code>
     * @see ApplicationField
     */
    public Map<ApplicationField, Serializable> getFields() {
        return new EnumConverter().convert(delegate.getFields(), ApplicationField.class);
    }

    /**
     * Retrieves the community version of this class.
     *
     * @return the community version of this class.
     */
    public org.bonitasoft.engine.business.application.ApplicationCreator getDelegate() {
        return delegate;
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return delegate.equals(obj);
    }

}
