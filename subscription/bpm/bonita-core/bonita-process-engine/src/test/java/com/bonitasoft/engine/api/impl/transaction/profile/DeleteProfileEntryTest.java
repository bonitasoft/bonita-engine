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

import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.model.SProfileEntry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DeleteProfileEntryTest {

    @Mock
    private ProfileService profileService;

    @Mock
    private SProfileEntry sProfileEntry;

    @Before
    public void before() throws Exception {
        doReturn(sProfileEntry).when(profileService).getProfileEntry(anyLong());

        // doReturn(fields).when(entityUpdateDescriptor).getFields();
    }

    @Test
    public void should_deleteProfileEntry_update_profileMetaData_() throws Exception {
        final long profileEntryId = 1l;

        // given
        final DeleteProfileEntry deleteProfileEntry = spy(new DeleteProfileEntry(profileService, profileEntryId));

        // when
        deleteProfileEntry.execute();

        // then
        verify(profileService, times(1)).updateProfileMetaData(anyLong());

    }
}
