/*******************************************************************************
 * Copyright (C) 2011, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
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

    private static final int NUMBER_OF_RESULTS = 100;

    private final ProfileService profileService;

    private SProfileEntry deletedProfileEntry = null;

    public UpdateProfileEntryIndexOnDelete(final ProfileService profileService, final SProfileEntry profileEntry) {
        super();
        this.profileService = profileService;
        this.deletedProfileEntry = profileEntry;
    }

    @Override
    public void execute() throws SBonitaException {
        int fromIndex = 0;
        List<SProfileEntry> profileEntryList;
        do {
            profileEntryList = searchProfileEntriesForParentIdAndProfileId(fromIndex);
            for (final SProfileEntry profileEntry : profileEntryList) {
                updateProfileEntryIndex(profileEntry, profileEntryList.indexOf(profileEntry) + fromIndex * NUMBER_OF_RESULTS);
            }
            fromIndex++;
        } while (!profileEntryList.isEmpty());
    }

    private void updateProfileEntryIndex(final SProfileEntry profileEntry, final int i) throws SProfileEntryNotFoundException,
            SProfileEntryUpdateException {
        if (deletedProfileEntry.getIndex() <= i * 2 && profileEntry.getIndex() != Long.valueOf(i * 2)) {
            updateProfileEntry(profileEntry, Long.valueOf(i * 2));
        }
    }

    private List<SProfileEntry> searchProfileEntriesForParentIdAndProfileId(final int fromIndex) throws SBonitaSearchException {
        Long profileId = null;
        Long parentId = null;
        if (deletedProfileEntry != null) {
            profileId = deletedProfileEntry.getProfileId();
            parentId = deletedProfileEntry.getParentId();
        }

        final List<FilterOption> filters = new ArrayList<FilterOption>(2);
        filters.add(new FilterOption(SProfileEntry.class, ProfileEntrySearchDescriptor.PROFILE_ID, profileId));
        filters.add(new FilterOption(SProfileEntry.class, ProfileEntrySearchDescriptor.PARENT_ID, parentId));
        final List<OrderByOption> orderByOptions = Collections.singletonList(new OrderByOption(SProfileEntry.class, ProfileEntrySearchDescriptor.INDEX,
                OrderByType.ASC));
        final QueryOptions queryOptions = new QueryOptions(fromIndex * NUMBER_OF_RESULTS, NUMBER_OF_RESULTS, orderByOptions, filters, null);
        return profileService.searchProfileEntries(queryOptions);
    }

    private void updateProfileEntry(final SProfileEntry profileEntry, final Long profileEntryIndex) throws SProfileEntryNotFoundException,
            SProfileEntryUpdateException {
        final EntityUpdateDescriptor entityUpdateDescriptor = new EntityUpdateDescriptor();
        entityUpdateDescriptor.addField(SProfileEntryBuilder.INDEX, profileEntryIndex);
        profileService.updateProfileEntry(profileEntry, entityUpdateDescriptor);
    }

}
