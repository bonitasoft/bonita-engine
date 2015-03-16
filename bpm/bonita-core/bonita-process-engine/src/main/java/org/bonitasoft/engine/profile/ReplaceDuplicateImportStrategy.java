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

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.profile.builder.SProfileUpdateBuilder;
import org.bonitasoft.engine.profile.builder.SProfileUpdateBuilderFactory;
import org.bonitasoft.engine.profile.exception.profile.SProfileUpdateException;
import org.bonitasoft.engine.profile.exception.profileentry.SProfileEntryDeletionException;
import org.bonitasoft.engine.profile.exception.profilemember.SProfileMemberDeletionException;
import org.bonitasoft.engine.profile.impl.ExportedProfile;
import org.bonitasoft.engine.profile.model.SProfile;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Baptiste Mesta
 */
public class ReplaceDuplicateImportStrategy extends ProfileImportStrategy {

    public ReplaceDuplicateImportStrategy(final ProfileService profileService) {
        super(profileService);
    }

    @Override
    public void beforeImport() {

    }

    @Override
    public SProfile whenProfileExists(final long importerId, final ExportedProfile exportedProfile, final SProfile existingProfile)
            throws SProfileEntryDeletionException, SProfileMemberDeletionException, SProfileUpdateException {
        getProfileService().deleteAllProfileMembersOfProfile(existingProfile);
        // update profile
        if (exportedProfile.isDefault() || existingProfile.isDefault()) {
            // only update LastUpdatedBy and LastUpdateDate
            return getProfileService().updateProfile(existingProfile, getProfileUpdateDescriptor(exportedProfile, importerId, false));
        }else{
            return getProfileService().updateProfile(existingProfile, getProfileUpdateDescriptor(exportedProfile, importerId, true));
        }
    }

    @Override
    public boolean canCreateProfileIfNotExists(final ExportedProfile exportedProfile) {
        return !exportedProfile.isDefault();
    }

    @Override
    public boolean shouldUpdateProfileEntries(ExportedProfile exportedProfile, SProfile existingProfile) {
        return !exportedProfile.isDefault() && !existingProfile.isDefault();
    }

    EntityUpdateDescriptor getProfileUpdateDescriptor(final ExportedProfile exportedProfile, final long importerId, final boolean updateAllProfile) {
        final SProfileUpdateBuilder updateBuilder = BuilderFactory.get(SProfileUpdateBuilderFactory.class).createNewInstance();
        updateBuilder.setLastUpdateDate(System.currentTimeMillis()).setLastUpdatedBy(importerId);
        if (updateAllProfile) {
            updateBuilder.setDescription(exportedProfile.getDescription());
        }
        return updateBuilder.done();
    }

}
