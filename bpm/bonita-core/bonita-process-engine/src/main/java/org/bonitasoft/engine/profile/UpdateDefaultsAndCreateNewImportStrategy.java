/**
 * Copyright (C) 2023 Bonitasoft S.A.
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
package org.bonitasoft.engine.profile;

import org.bonitasoft.engine.profile.exception.profile.SProfileUpdateException;
import org.bonitasoft.engine.profile.model.SProfile;
import org.bonitasoft.engine.profile.xml.ProfileNode;

public class UpdateDefaultsAndCreateNewImportStrategy extends UpdateDefaultsImportStrategy {

    public UpdateDefaultsAndCreateNewImportStrategy(final ProfileService profileService) {
        super(profileService);
    }

    @Override
    public SProfile whenProfileExists(final long importerId, final ProfileNode profile, final SProfile existingProfile)
            throws SProfileUpdateException {
        if (profile.isDefault() || existingProfile.isDefault()) {
            // only update LastUpdatedBy and LastUpdateDate
            return getProfileService().updateProfile(existingProfile,
                    getProfileUpdateDescriptor(profile, importerId, true));
        } else {
            throw new SProfileUpdateException("A profile already exists with name: " + profile.getName());
        }
    }

    @Override
    public boolean canCreateProfileIfNotExists(final ProfileNode profile) {
        return true;
    }
}
