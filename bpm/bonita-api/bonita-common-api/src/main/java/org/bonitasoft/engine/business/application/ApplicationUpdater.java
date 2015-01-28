/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.business.application;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.profile.Profile;

/**
 * Allows to define which {@link Application} fields will be updated
 *
 * @author Elias Ricken de Medeiros
 * @see Application
 */
public class ApplicationUpdater implements Serializable {

    private static final long serialVersionUID = 4565052647320534796L;

    private final Map<ApplicationField, Serializable> fields;

    /**
     * Creates an instance of <code>ApplicationUpdater</code>
     */
    public ApplicationUpdater() {
        fields = new HashMap<ApplicationField, Serializable>(8);
    }

    /**
     * Retrieves all fields to be updated
     *
     * @return a {@link Map}<{@link ApplicationField}, {@link Serializable}> containing all fields to be updated
     * @see ApplicationField
     */
    public Map<ApplicationField, Serializable> getFields() {
        return fields;
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
        fields.put(ApplicationField.TOKEN, token);
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
        fields.put(ApplicationField.DISPLAY_NAME, displayName);
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
        fields.put(ApplicationField.VERSION, version);
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
        fields.put(ApplicationField.DESCRIPTION, description);
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
        fields.put(ApplicationField.ICON_PATH, iconPath);
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
        fields.put(ApplicationField.STATE, state);
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
        fields.put(ApplicationField.PROFILE_ID, profileId);
        return this;
    }

    /**
     * Defines the identifier of the new {@link org.bonitasoft.engine.business.application.ApplicationPage} defined as the {@link Application} home page
     *
     * @param applicationPageId the identifier of {@code ApplicationPage} associated to the {@code Application}
     * @return the current {@code ApplicationUpdater}
     * @see Application
     * @see org.bonitasoft.engine.business.application.ApplicationPage
     */
    public ApplicationUpdater setHomePageId(final Long applicationPageId) {
        fields.put(ApplicationField.HOME_PAGE_ID, applicationPageId);
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
}
