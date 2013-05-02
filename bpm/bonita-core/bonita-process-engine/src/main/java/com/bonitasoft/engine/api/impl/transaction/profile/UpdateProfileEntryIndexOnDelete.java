/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.SProfileEntryNotFoundException;
import org.bonitasoft.engine.profile.SProfileEntryUpdateException;
import org.bonitasoft.engine.profile.builder.SProfileEntryBuilder;
import org.bonitasoft.engine.profile.model.SProfileEntry;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.search.descriptor.ProfileEntrySearchDescriptor;

/**
 * @author Julien Mege
 * @author Celine Souchet
 */
public class UpdateProfileEntryIndexOnDelete implements TransactionContent {

    private final ProfileService profileService;

    private SProfileEntry deletedProfileEntry = null;

    public UpdateProfileEntryIndexOnDelete(final ProfileService profileService, final SProfileEntry profileEntry) {
        super();
        this.profileService = profileService;
        this.deletedProfileEntry = profileEntry;
    }

    @Override
    public void execute() throws SBonitaException {
        if (deletedProfileEntry != null) {
            final long profileId = deletedProfileEntry.getProfileId();
            final long parentId = deletedProfileEntry.getParentId();
            List<SProfileEntry> profileEntryList = getProfileEntriesForParentId(profileId, parentId);
            while (profileEntryList.size() > 0) {
                for (int i = 0; i < profileEntryList.size(); i++) {
                    if (i * 2 >= deletedProfileEntry.getIndex()) {
                        final SProfileEntry profileEntry = profileEntryList.get(i);
                        if (!Long.valueOf(i * 2).equals(profileEntry.getIndex())) {
                            updateProfileEntryIndex(profileEntry, Long.valueOf(i * 2));
                        }
                    }
                }
                profileEntryList = getProfileEntriesForParentId(profileId, parentId);
            }
        }
    }

    private List<SProfileEntry> getProfileEntriesForParentId(final Long profileId, final Long parentId) throws SBonitaSearchException {
        final List<FilterOption> filters = new ArrayList<FilterOption>(2);
        filters.add(new FilterOption(SProfileEntry.class, ProfileEntrySearchDescriptor.PROFILE_ID, profileId));
        filters.add(new FilterOption(SProfileEntry.class, ProfileEntrySearchDescriptor.PARENT_ID, parentId));
        final List<OrderByOption> orderByOptions = Collections.singletonList(new OrderByOption(SProfileEntry.class, ProfileEntrySearchDescriptor.INDEX,
                OrderByType.ASC));
        final QueryOptions queryOptions = new QueryOptions(0, 100, orderByOptions, filters, null);
        return profileService.searchProfileEntries(queryOptions);
    }

    private void updateProfileEntryIndex(final SProfileEntry profileEntry, final Long profileEntryIndex) throws SProfileEntryNotFoundException,
            SProfileEntryUpdateException {
        final EntityUpdateDescriptor entityUpdateDescriptor = new EntityUpdateDescriptor();
        entityUpdateDescriptor.addField(SProfileEntryBuilder.INDEX, profileEntryIndex);
        profileService.updateProfileEntry(profileEntry, entityUpdateDescriptor);
    }

}
