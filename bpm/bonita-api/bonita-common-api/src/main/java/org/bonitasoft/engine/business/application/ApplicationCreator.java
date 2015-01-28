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
 * Describes the information about an {@link Application} to be created
 *
 * @author Elias Ricken de Medeiros
 * @since 6.4
 * @see Application
 */
public class ApplicationCreator implements Serializable {

    private static final long serialVersionUID = -916041825489100271L;

    private final Map<ApplicationField, Serializable> fields;

    /**
     * Creates an instance of <code>ApplicationCreator</code> containing mandatory information
     *
     * @param token the {@link Application} token. The token will be part of application URL. It cannot be null or empty and should contain only alpha numeric
     *        characters and the following special characters '-', '.', '_' or '~'.
     * @param displayName the <code>Application</code> display name. It cannot be null or empty
     * @param version the <code>Application</code> version
     * @see Application
     */
    public ApplicationCreator(final String token, final String displayName, final String version) {
        fields = new HashMap<ApplicationField, Serializable>(2);
        fields.put(ApplicationField.TOKEN, token);
        fields.put(ApplicationField.VERSION, version);
        fields.put(ApplicationField.DISPLAY_NAME, displayName);
    }

    /**
     * Retrieves the {@link Application} token
     *
     * @return the <code>Application</code> token
     * @see Application
     */
    public String getToken() {
        return (String) fields.get(ApplicationField.TOKEN);
    }

    /**
     * Defines the {@link Application} description and returns the current <code>ApplicationCreator</code>
     *
     * @param description the <code>Application</code> description
     * @return the current <code>ApplicationCreator</code>
     * @see Application
     */
    public ApplicationCreator setDescription(final String description) {
        fields.put(ApplicationField.DESCRIPTION, description);
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
        fields.put(ApplicationField.ICON_PATH, iconPath);
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
        fields.put(ApplicationField.PROFILE_ID, profileId);
        return this;
    }

    /**
     * Retrieves all fields defined in this <code>ApplicationCreator</code>
     *
     * @return a {@link Map}<{@link ApplicationField}, {@link Serializable}> containing all fields defined in this <code>ApplicationCreator</code>
     * @see ApplicationField
     */
    public Map<ApplicationField, Serializable> getFields() {
        return fields;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (fields == null ? 0 : fields.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ApplicationCreator other = (ApplicationCreator) obj;
        if (fields == null) {
            if (other.fields != null) {
                return false;
            }
        } else if (!fields.equals(other.fields)) {
            return false;
        }
        return true;
    }

}
