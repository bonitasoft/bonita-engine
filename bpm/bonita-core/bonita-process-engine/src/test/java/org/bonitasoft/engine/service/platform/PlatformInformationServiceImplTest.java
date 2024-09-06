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
package org.bonitasoft.engine.service.platform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.bonitasoft.engine.platform.exception.SPlatformUpdateException;
import org.bonitasoft.engine.platform.model.SPlatform;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.services.SPersistenceException;
import org.bonitasoft.engine.services.UpdateDescriptor;
import org.hamcrest.core.IsEqual;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PlatformInformationServiceImplTest {

    @Mock
    private PersistenceService persistenceService;

    @InjectMocks
    private PlatformInformationServiceImpl platformInformationService;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void updatePlatformInfo() throws Exception {
        //given
        SPlatform platform = mock(SPlatform.class);

        //when
        platformInformationService.updatePlatformInfo(platform, "info");

        //then
        ArgumentCaptor<UpdateDescriptor> upDescCaptor = ArgumentCaptor.forClass(UpdateDescriptor.class);
        verify(persistenceService).update(upDescCaptor.capture());
        UpdateDescriptor descriptor = upDescCaptor.getValue();
        assertThat(descriptor.getFields()).containsEntry(SPlatform.INFORMATION, "info");
        assertThat(descriptor.getEntity()).isEqualTo(platform);
    }

    @Test
    public void updatePlatformInfo_should_throw_exception_when_persistence_service_throws_exception() throws Exception {
        //given
        SPlatform platform = mock(SPlatform.class);
        SPersistenceException toBeThrown = new SPersistenceException("exception");
        doThrow(toBeThrown).when(persistenceService).update(any(UpdateDescriptor.class));

        //then
        expectedException.expect(SPlatformUpdateException.class);
        expectedException.expectCause(new IsEqual<Throwable>(toBeThrown));

        //when
        platformInformationService.updatePlatformInfo(platform, "info");
    }
}
