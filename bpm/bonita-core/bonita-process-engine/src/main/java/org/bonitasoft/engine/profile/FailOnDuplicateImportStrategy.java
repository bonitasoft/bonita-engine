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
import org.bonitasoft.engine.profile.impl.ExportedProfile;
import org.bonitasoft.engine.profile.model.SProfile;

/**
 * @author Baptiste Mesta
 */
public class FailOnDuplicateImportStrategy extends ProfileImportStrategy {

    public FailOnDuplicateImportStrategy(final ProfileService profileService) {
        super(profileService);
    }

    @Override
    public void beforeImport() {

    }

    @Override
    public SProfile whenProfileExists(final long importerId, final ExportedProfile exportedProfile, final SProfile existingProfile)
            throws ExecutionException {
        throw new ExecutionException("There's a same name profile when import a profile named " + exportedProfile.getName());
    }

    @Override
    public boolean canCreateProfileIfNotExists(final ExportedProfile exportedProfile) {
        return true;
    }

    @Override
    public boolean shouldUpdateProfileEntries(ExportedProfile exportedProfile, SProfile existingProfile) {
        return false;
    }

}
