/**
 * Copyright (C) 2011, 2013 BonitaSoft S.A.
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

import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.builder.SProfileBuilderFactory;
import org.bonitasoft.engine.profile.model.SProfile;

/**
 * @author Zhao Na
 * @author Celine Souchet
 */
public class DeleteAllExistingProfiles implements TransactionContent {

    private final ProfileService profileService;

    private List<SProfile> profiles;

    public DeleteAllExistingProfiles(final ProfileService profileService) {
        super();
        this.profileService = profileService;
    }

    @Override
    public void execute() throws SBonitaException {
        final QueryOptions queryOptions = new QueryOptions(0, 100, Collections.singletonList(new OrderByOption(SProfile.class,
                SProfileBuilderFactory.NAME, OrderByType.ASC)), Collections.<FilterOption> emptyList(), null);
        do {
            profiles = profileService.searchProfiles(queryOptions);
            for (final SProfile sProfile : profiles) {
                profileService.deleteProfile(sProfile);
            }
        } while (!profiles.isEmpty());
    }

}
