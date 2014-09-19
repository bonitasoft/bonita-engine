/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl.transaction.profile;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.builder.SProfileEntryUpdateBuilder;
import org.bonitasoft.engine.profile.model.SProfileEntry;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

@RunWith(MockitoJUnitRunner.class)
public class UpdateProfileEntryIndexOnInsertTest {

    @Mock
    private ProfileService profileService;

    @Mock
    private SProfileEntryUpdateBuilder profileEntryUpdateBuilder;

    @Mock
    EntityUpdateDescriptor entityUpdateDescriptor;

    @Mock
    private SProfileEntry sProfileEntry;

    @Test
    public void should_updateProfileEntryIndex_update_profileMetaData() throws Exception {

        final UpdateProfileEntryIndexOnInsert updateProfileEntry = spy(new UpdateProfileEntryIndexOnInsert(profileService, sProfileEntry));

        final Answer<List<SProfileEntry>> answer = new Answer<List<SProfileEntry>>() {

            @Override
            public List<SProfileEntry> answer(final InvocationOnMock invocation) {
                final Object[] args = invocation.getArguments();
                final QueryOptions queryOptions = (QueryOptions) args[0];
                if (queryOptions.getFromIndex() == 0) {
                    return Arrays.asList(sProfileEntry);
                }
                return new ArrayList<SProfileEntry>();
            }

        };

        when(profileService.searchProfileEntries(any(QueryOptions.class))).thenAnswer(answer);

        // when
        updateProfileEntry.execute();

        // then
        verify(profileService, times(1)).updateProfileMetaData(anyLong());
    }

}
