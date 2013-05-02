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
 * Update indexes for all elements having the same parent category
 * Indexes are always a multiple of 2
 * This is done to let web insert easily element between 2 other elements
 * e.g. if you want to insert an element index between 2 and 4 elements
 * put index to 3
 * here we update all element to have:
 * element(0) -> new index = 0
 * element(2) -> new index = 2
 * element(3) -> new index = 4
 * element(4) -> new index = 6
 * and so on
 * If element must be first just insert it with value < 0
 * 
 * @author Julien Mege
 */
public class UpdateProfileEntryIndexOnInsert implements TransactionContent {

    private final ProfileService profileService;

    private SProfileEntry insertedProfileEntry = null;

    public UpdateProfileEntryIndexOnInsert(final ProfileService profileService, final SProfileEntry profileEntry) {
        super();
        this.profileService = profileService;
        insertedProfileEntry = profileEntry;
    }

    @Override
    public void execute() throws SBonitaException {
        insertedProfileEntry = profileService.getProfileEntry(insertedProfileEntry.getId());
        final Long insertedIndex = Long.valueOf(insertedProfileEntry.getIndex());

        List<SProfileEntry> profileEntryList = getProfileEntriesForParentId(insertedProfileEntry.getParentId());
        while (profileEntryList.size() > 0) {
            for (Long i = Long.valueOf(0); i < profileEntryList.size(); i++) {
                final SProfileEntry profileEntry = profileEntryList.get(i.intValue());
                if (profileEntry.getId() != insertedProfileEntry.getId()) {
                    if (i * 2 < insertedIndex) {
                        if (i * 2 != profileEntry.getIndex()) {
                            updateProfileEntryIndex(profileEntry, i * 2);
                        }
                    }
                    if (i * 2 == insertedIndex) {
                        updateProfileEntryIndex(profileEntry, i * 2 + 2);
                    }
                    if (i * 2 > insertedIndex) {
                        updateProfileEntryIndex(profileEntry, i * 2);
                    }
                } else {
                    if (insertedIndex < 0) {
                        updateProfileEntryIndex(profileEntry, Long.valueOf(0));
                    }
                    if (insertedIndex > i * 2) {
                        updateProfileEntryIndex(profileEntry, i * 2);
                    }
                }
            }

            profileEntryList = getProfileEntriesForParentId(insertedProfileEntry.getParentId());
        }

    }

    private List<SProfileEntry> getProfileEntriesForParentId(final Long parentId) throws SBonitaSearchException {
        Long profileId = null;
        if (insertedProfileEntry != null) {
            profileId = insertedProfileEntry.getProfileId();
        }

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
