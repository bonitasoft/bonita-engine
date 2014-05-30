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
import org.bonitasoft.engine.profile.ProfileEntrySearchDescriptor;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.builder.SProfileEntryBuilderFactory;
import org.bonitasoft.engine.profile.exception.profileentry.SProfileEntryDeletionException;
import org.bonitasoft.engine.profile.exception.profileentry.SProfileEntryUpdateException;
import org.bonitasoft.engine.profile.model.SProfileEntry;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Julien Mege
 * @author Celine Souchet
 */
public class DeleteProfileEntry implements TransactionContent {

    private static final int NUMBER_OF_RESULTS = 100;

    private final ProfileService profileService;

    private final long profileEntryId;

    private SProfileEntry sDeletedProfileEntry;

    public DeleteProfileEntry(final ProfileService profileService, final long profileEntryId) {
        super();
        this.profileService = profileService;
        this.profileEntryId = profileEntryId;
    }

    @Override
    public void execute() throws SBonitaException {
        sDeletedProfileEntry = profileService.getProfileEntry(profileEntryId);
        if (sDeletedProfileEntry != null) {
            deleteProfileEntryChildren();
            profileService.deleteProfileEntry(profileEntryId);
            updateProfileEntriesIndexOnDelete();
            profileService.updateProfileMetaData(sDeletedProfileEntry.getProfileId());
        }
    }

    private void deleteProfileEntryChildren() throws SBonitaSearchException, SProfileEntryDeletionException {
        List<SProfileEntry> sProfileEntries;
        do {
            sProfileEntries = searchProfileEntriesChildren();
            for (final SProfileEntry sProfileEntry : sProfileEntries) {
                profileService.deleteProfileEntry(sProfileEntry);
            }
        } while (!sProfileEntries.isEmpty());
    }

    private List<SProfileEntry> searchProfileEntriesChildren() throws SBonitaSearchException {
        final List<FilterOption> filters = new ArrayList<FilterOption>(2);
        filters.add(new FilterOption(SProfileEntry.class, ProfileEntrySearchDescriptor.PROFILE_ID, sDeletedProfileEntry.getProfileId()));
        filters.add(new FilterOption(SProfileEntry.class, ProfileEntrySearchDescriptor.PARENT_ID, sDeletedProfileEntry.getId()));
        final List<OrderByOption> orderByOptions = Collections.singletonList(new OrderByOption(SProfileEntry.class, ProfileEntrySearchDescriptor.INDEX,
                OrderByType.ASC));
        final QueryOptions queryOptions = new QueryOptions(0, NUMBER_OF_RESULTS, orderByOptions, filters, null);
        return profileService.searchProfileEntries(queryOptions);
    }

    private void updateProfileEntriesIndexOnDelete() throws SBonitaException {
        int fromIndex = 0;
        List<SProfileEntry> sProfileEntries;
        do {
            sProfileEntries = searchProfileEntriesWithSameParentAndInSameProfile(fromIndex);
            for (final SProfileEntry sProfileEntry : sProfileEntries) {
                final int indexInListOfSProfileEntry = sProfileEntries.indexOf(sProfileEntry) + fromIndex * NUMBER_OF_RESULTS;
                final Long doubleOfIndex = Long.valueOf(indexInListOfSProfileEntry * 2);
                if (sDeletedProfileEntry.getIndex() <= doubleOfIndex && sProfileEntry.getIndex() != doubleOfIndex) {
                    updateProfileEntryIndex(sProfileEntry, doubleOfIndex);
                }
            }
            fromIndex++;
        } while (!sProfileEntries.isEmpty());
    }

    private void updateProfileEntryIndex(final SProfileEntry profileEntry, final Long newIndex) throws SProfileEntryUpdateException {
        final EntityUpdateDescriptor entityUpdateDescriptor = new EntityUpdateDescriptor();
        entityUpdateDescriptor.addField(SProfileEntryBuilderFactory.INDEX, newIndex);
        profileService.updateProfileEntry(profileEntry, entityUpdateDescriptor);
    }

    private List<SProfileEntry> searchProfileEntriesWithSameParentAndInSameProfile(final int fromIndex) throws SBonitaSearchException {
        final List<FilterOption> filters = new ArrayList<FilterOption>(2);
        filters.add(new FilterOption(SProfileEntry.class, ProfileEntrySearchDescriptor.PROFILE_ID, sDeletedProfileEntry.getProfileId()));
        filters.add(new FilterOption(SProfileEntry.class, ProfileEntrySearchDescriptor.PARENT_ID, sDeletedProfileEntry.getParentId()));
        final List<OrderByOption> orderByOptions = Collections.singletonList(new OrderByOption(SProfileEntry.class, ProfileEntrySearchDescriptor.INDEX,
                OrderByType.ASC));
        final QueryOptions queryOptions = new QueryOptions(fromIndex * NUMBER_OF_RESULTS, NUMBER_OF_RESULTS, orderByOptions, filters, null);
        return profileService.searchProfileEntries(queryOptions);
    }

}
