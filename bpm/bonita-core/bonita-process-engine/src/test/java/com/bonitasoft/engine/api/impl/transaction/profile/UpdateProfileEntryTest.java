/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl.transaction.profile;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Map;

import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.builder.SProfileEntryUpdateBuilder;
import org.bonitasoft.engine.profile.model.SProfileEntry;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.profile.ProfileEntryUpdater;

@RunWith(MockitoJUnitRunner.class)
public class UpdateProfileEntryTest {

    @Mock
    private ProfileService profileService;

    @Mock
    private SProfileEntry sProfileEntry;

    @Mock
    private ProfileEntryUpdater updateDescriptor;

    @Mock
    private SProfileEntryUpdateBuilder profileEntryUpdateBuilder;

    @Mock
    EntityUpdateDescriptor entityUpdateDescriptor;

    @Mock
    Map<String, Object> fields;

    @Before
    public void before() throws Exception {
        doReturn(sProfileEntry).when(profileService).getProfileEntry(anyLong());

        doReturn(fields).when(entityUpdateDescriptor).getFields();
    }

    @Test
    public void should_updateProfileEntry_update_profileMetaData() throws Exception {
        final Long profileEntryId = 1L;
        final long updatedById = 0;
        final UpdateProfileEntry updateProfileEntry = spy(new UpdateProfileEntry(profileService, profileEntryId, updateDescriptor));

        doReturn(profileEntryUpdateBuilder).when(updateProfileEntry).getUpdateBuilder();
        doReturn(entityUpdateDescriptor).when(updateProfileEntry).getProfileEntryUpdateDescriptor();

        // when
        updateProfileEntry.execute();

        // then
        verify(profileService, times(1)).updateProfileMetaData(anyLong());
    }
}
