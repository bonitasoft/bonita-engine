/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

package org.bonitasoft.engine.platform.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.platform.exception.SPlatformNotFoundException;
import org.bonitasoft.engine.platform.model.SPlatform;
import org.bonitasoft.engine.services.PersistenceService;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PlatformRetrieverImplTest {

    @Mock
    private PersistenceService persistenceService;

    @InjectMocks
    private PlatformRetrieverImpl platformRetriever;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public final void getPlatform_should_return_result_of_persistence_service() throws SBonitaException {
        SPlatform platform = mock(SPlatform.class);
        given(persistenceService.selectOne(new SelectOneDescriptor<SPlatform>("getPlatform", null, SPlatform.class))).willReturn(platform);

        //when
        SPlatform retrievedPlatform = platformRetriever.getPlatform();
        assertThat(retrievedPlatform).isEqualTo(platform);
    }

    @Test
    public final void getPlatform_should_throw_SPlatformNotFoundException_when_persistence_service_returns_null() throws SBonitaException {
        given(persistenceService.selectOne(new SelectOneDescriptor<SPlatform>("getPlatform", null, SPlatform.class))).willReturn(null);

        //then
        expectedException.expect(SPlatformNotFoundException.class);
        expectedException.expectMessage(equalTo("No platform found"));

        //when
        platformRetriever.getPlatform();
    }

    @Test
    public final void getPlatform_should_throw_SPlatformNotFoundException_when_persistence_service_throws_Exception() throws SBonitaException {
        SBonitaReadException exception = new SBonitaReadException("Unable to access database");
        given(persistenceService.selectOne(new SelectOneDescriptor<SPlatform>("getPlatform", null, SPlatform.class))).willThrow(exception);

        //then
        expectedException.expect(SPlatformNotFoundException.class);
        expectedException.expectMessage("Unable to check if a platform already exists : ");
        expectedException.expectCause(equalTo(exception));

        //when
        platformRetriever.getPlatform();
    }

}
