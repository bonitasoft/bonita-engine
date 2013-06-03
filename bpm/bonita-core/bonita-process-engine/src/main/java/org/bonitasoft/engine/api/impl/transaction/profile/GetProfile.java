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
package org.bonitasoft.engine.api.impl.transaction.profile;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.model.SProfile;

/**
 * @author Julien Mege
 */
public class GetProfile implements TransactionContentWithResult<SProfile> {

    private final ProfileService profileService;

    private SProfile profile;

    private final long profileId;

    public GetProfile(final ProfileService profileService, final long profileId) {
        super();
        this.profileService = profileService;
        this.profileId = profileId;
    }

    @Override
    public void execute() throws SBonitaException {
        this.profile = this.profileService.getProfile(this.profileId);
    }

    @Override
    public SProfile getResult() {
        return this.profile;
    }
}
