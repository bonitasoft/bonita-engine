/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
 * @author Zhao Na
 */
public class GetProfileByName implements TransactionContentWithResult<SProfile> {

    private final ProfileService profileService;

    private final String name;

    private SProfile profile;

    public GetProfileByName(final ProfileService profileService, final String name) {
        super();
        this.profileService = profileService;
        this.name = name;
    }

    @Override
    public void execute() throws SBonitaException {
        profile = profileService.getProfileByName(name);
    }

    @Override
    public SProfile getResult() {
        return profile;
    }

}
