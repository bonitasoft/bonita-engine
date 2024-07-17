/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
 * Allows to define which {@link IApplication} fields will be updated
 *
 * @see IApplication
 * @deprecated This class should no longer be used. Since 9.0.0, Applications should be updated at startup.
 * @param <T> the concrete subtype of {@link AbstractApplicationUpdater} (for returning self, subclasses must ensure to
 *        use the concrete instance)
 */
@Deprecated(since = "10.2.0")
public abstract class AbstractApplicationUpdater<T extends AbstractApplicationUpdater<T>> implements Serializable {

    private static final long serialVersionUID = -5301609836484089900L;

    private final Map<ApplicationField, Serializable> fields;

    /**
     * Creates an instance of <code>ApplicationUpdater</code>
     */
    public AbstractApplicationUpdater() {
        fields = new HashMap<>(8);
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
     * Defines the new value for the {@link IApplication} token. It cannot be empty or null and should contain only
     * alpha numeric characters and the following special characters '-', '.', '_' or '~'.
     *
     * @param token the new value for the {@code Application} token
     * @return the current {@code ApplicationUpdater}
     * @see IApplication
     */
    public T setToken(final String token) {
        fields.put(ApplicationField.TOKEN, token);
        return (T) this;
    }

    /**
     * Defines the new value for the {@link IApplication} display name. It cannot be empty or null.
     *
     * @param displayName the new value for the {@code Application} display name
     * @return the current {@code ApplicationUpdater}
     * @see IApplication
     */
    public T setDisplayName(final String displayName) {
        fields.put(ApplicationField.DISPLAY_NAME, displayName);
        return (T) this;
    }

    /**
     * Defines the new value for the {@link IApplication} version
     *
     * @param version the new value for the {@code Application} version
     * @return the current {@code ApplicationUpdater}
     * @see IApplication
     */
    public T setVersion(final String version) {
        fields.put(ApplicationField.VERSION, version);
        return (T) this;
    }

    /**
     * Defines the new value for the {@link IApplication} description
     *
     * @param description the new value for the {@code Application} description
     * @return the current {@code ApplicationUpdater}
     * @see IApplication
     */
    public T setDescription(final String description) {
        fields.put(ApplicationField.DESCRIPTION, description);
        return (T) this;
    }

    /**
     * Defines the new value for the {@link IApplication} icon path
     *
     * @param iconPath the new value for the {@code Application} icon path
     * @return the current {@code ApplicationUpdater}
     * @see IApplication
     * @deprecated since 7.13.0, use {@link #setIcon(String, byte[])}
     */
    @Deprecated(since = "7.13.0")
    public T setIconPath(final String iconPath) {
        fields.put(ApplicationField.ICON_PATH, iconPath);
        return (T) this;
    }

    /**
     * Defines the new icon for the {@link IApplication}.
     * <p/>
     * The icons are accessible using {@link org.bonitasoft.engine.api.ApplicationAPI#getIconOfApplication(long)}
     * Calling that method with {@code setIcon(null, null)} will remove the icon.
     *
     * @param iconFileName of the icon
     * @param content of the icon
     * @return the current builder
     * @since 7.13.0
     */
    public T setIcon(String iconFileName, byte[] content) {
        fields.put(ApplicationField.ICON_FILE_NAME, iconFileName);
        fields.put(ApplicationField.ICON_CONTENT, content);
        return (T) this;
    }

    /**
     * Defines the new value for the {@link IApplication} state
     *
     * @param state the new value for the {@code Application} state
     * @return the current {@code ApplicationUpdater}
     * @see IApplication
     */
    public T setState(final String state) {
        fields.put(ApplicationField.STATE, state);
        return (T) this;
    }

    /**
     * Defines the identifier of the new {@link Profile} associated to the {@link IApplication}
     *
     * @param profileId the identifier of {@code Profile} associated to the {@code Application}
     * @return the current {@code ApplicationUpdater}
     * @see IApplication
     * @see Profile
     */
    public T setProfileId(final Long profileId) {
        fields.put(ApplicationField.PROFILE_ID, profileId);
        return (T) this;
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
