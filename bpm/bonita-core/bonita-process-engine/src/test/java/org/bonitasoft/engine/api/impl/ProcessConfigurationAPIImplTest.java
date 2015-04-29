/*
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
 */
package org.bonitasoft.engine.api.impl;

import static org.mockito.Mockito.*;

import org.bonitasoft.engine.exception.UnauthorizedAccessException;
import org.bonitasoft.engine.page.PageMappingService;
import org.bonitasoft.engine.page.SAuthorizationException;
import org.bonitasoft.engine.page.SPageMapping;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * author Emmanuel Duchastenier
 */
@RunWith(MockitoJUnitRunner.class)
public class ProcessConfigurationAPIImplTest {

    @Mock
    PageMappingService pageMappingService;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void resolvePageOrURLShouldThrowUnauthorizedAccessExceptionIfSAuthorizRaised() throws Exception {
        final String pageKey = "pageKey";
        final SPageMapping pageMapping = mock(SPageMapping.class);
        doReturn(pageMapping).when(pageMappingService).get(pageKey);
        doThrow(SAuthorizationException.class).when(pageMappingService).resolvePageURL(pageMapping, null, true);

        final ProcessConfigurationAPIImpl processConfigurationAPI = spy(new ProcessConfigurationAPIImpl());
        doReturn(pageMappingService).when(processConfigurationAPI).retrievePageMappingService();

        expectedException.expect(UnauthorizedAccessException.class);

        processConfigurationAPI.resolvePageOrURL(pageKey, null, true);
    }
}
