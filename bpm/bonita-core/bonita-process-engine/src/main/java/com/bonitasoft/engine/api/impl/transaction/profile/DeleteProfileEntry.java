/**
 * Copyright (C) 2011 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.bonitasoft.engine.api.impl.transaction.profile;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.profile.ProfileService;

/**
 * @author Julien Mege
 */
public class DeleteProfileEntry implements TransactionContent {

    private final ProfileService profileService;

    private final long profileEntryId;

    public DeleteProfileEntry(final ProfileService profileService, final long profileEntryId) {
        super();
        this.profileService = profileService;
        this.profileEntryId = profileEntryId;
    }

    @Override
    public void execute() throws SBonitaException {
        this.profileService.deleteProfileEntry(this.profileEntryId);
    }

}
