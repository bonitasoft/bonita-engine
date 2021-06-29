/**
 * Copyright (C) 2021 Bonitasoft S.A.
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

public enum InternalProfiles {

    INTERNAL_PROFILE_SUPER_ADMIN("_BONITA_INTERNAL_PROFILE_SUPER_ADMIN", ApplicationVisibility.TECHNICAL_USER),

    INTERNAL_PROFILE_ALL("_BONITA_INTERNAL_PROFILE_ALL", ApplicationVisibility.ALL);

    InternalProfiles(String profileName, ApplicationVisibility applicationVisibility) {
        this.profileName = profileName;
        this.applicationVisibility = applicationVisibility;
    }

    private final String profileName;

    private final ApplicationVisibility applicationVisibility;

    public String getProfileName() {
        return profileName;
    }

    public ApplicationVisibility getApplicationVisibility() {
        return applicationVisibility;
    }

    public static ApplicationVisibility getApplicationVisibilityByProfileName(String profileName) {
        for (InternalProfiles internalProfile : InternalProfiles.values()) {
            if (internalProfile.getProfileName().equals(profileName)) {
                return internalProfile.applicationVisibility;
            }
        }
        return ApplicationVisibility.RESTRICTED;
    }

    public static InternalProfiles getInternalProfileByProfileName(String profileName) {
        for (InternalProfiles internalProfile : InternalProfiles.values()) {
            if (internalProfile.getProfileName().equals(profileName)) {
                return internalProfile;
            }
        }
        return null;
    }

}
