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
package org.bonitasoft.engine.profile;

import org.bonitasoft.engine.exception.ExecutionException;
import org.bonitasoft.engine.profile.exception.profile.SProfileUpdateException;
import org.bonitasoft.engine.profile.exception.profileentry.SProfileEntryDeletionException;
import org.bonitasoft.engine.profile.exception.profilemember.SProfileMemberDeletionException;
import org.bonitasoft.engine.profile.impl.ExportedProfile;
import org.bonitasoft.engine.profile.model.SProfile;

/**
 * @author Baptiste Mesta
 * 
 */
public abstract class ProfileImportStrategy {

    private final ProfileService profileService;

    public ProfileImportStrategy(final ProfileService profileService) {
        this.profileService = profileService;
    }

    /**
     * what to do before the import
     */
    public abstract void beforeImport() throws ExecutionException;

    /**
     * return the imported version of the exportedProfile
     */
    public abstract SProfile whenProfileExists(final long importerId,
            final ExportedProfile exportedProfile,
            final SProfile existingProfile) throws ExecutionException, SProfileEntryDeletionException,
            SProfileMemberDeletionException, SProfileUpdateException;

    /**
     * return whether the profile can be created if it does not exist
     */
    public abstract boolean canCreateProfileIfNotExists(ExportedProfile exportedProfile);

    /**
     * convert {@link ExportedProfile} to {@link SProfile}
     *
     * @return the profile service
     */

    protected ProfileService getProfileService() {
        return profileService;
    }

    public abstract boolean shouldUpdateProfileEntries(ExportedProfile exportedProfile, SProfile existingProfile);
}
