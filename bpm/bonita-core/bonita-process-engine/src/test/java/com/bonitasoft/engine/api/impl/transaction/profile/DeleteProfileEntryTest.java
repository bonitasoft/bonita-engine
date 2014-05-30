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
