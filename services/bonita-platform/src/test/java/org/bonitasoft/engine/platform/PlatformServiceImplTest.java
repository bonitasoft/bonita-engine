/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.platform;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.platform.exception.SPlatformNotFoundException;
import org.bonitasoft.engine.platform.impl.PlatformServiceImpl;
import org.bonitasoft.engine.platform.model.SPlatform;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.services.UpdateDescriptor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PlatformServiceImplTest {

    @Mock
    private PersistenceService persistenceService;

    @Mock
    private PlatformRetriever platformRetriever;

    @Mock
    private Recorder recorder;

    @InjectMocks
    private PlatformServiceImpl platformServiceImpl;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public final void getPlatform_should_throw_SPlatformNotFoundException_when_platformRetriever_throws_SPlatformNotFoundException()
            throws SBonitaException {
        //given
        SPlatformNotFoundException exception = new SPlatformNotFoundException("Not found");
        given(platformRetriever.getPlatform()).willThrow(exception);

        //then
        expectedException.expect(SPlatformNotFoundException.class);
        expectedException.expectMessage(equalTo("Not found"));

        //when
        platformServiceImpl.getPlatform();
    }

    @Test
    public final void isPlatformCreated() throws SBonitaException {
        final SPlatform sPlatform = buildPlatform();
        when(platformServiceImpl.getPlatform()).thenReturn(sPlatform);

        assertTrue(platformServiceImpl.isPlatformCreated());
    }

    @Test
    public void pause_should_update_platform_state_to_PAUSED() throws SBonitaException {
        // when
        platformServiceImpl.pauseServices();

        // then
        verify(persistenceService).update(argThat(this::updateOnlyStatus));
    }

    private boolean updateOnlyStatus(UpdateDescriptor u) {
        return u.getFields().size() == 1
                && u.getFields().containsKey(SPlatform.MAINTENANCE_ENABLED)
                && u.getFields().containsValue(true);
    }

    private SPlatform buildPlatform() {
        return new SPlatform("1.0", "0.5", "0.0.0", null, false, "me", 654687344687645L, false);
    }

}
