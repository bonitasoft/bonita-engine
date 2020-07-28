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
package org.bonitasoft.engine.tenant;

import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class TenantElementsRestarterTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private TenantElementsRestartSupervisor tenantElementsRestartSupervisor;
    @Mock
    private TenantRestarter tenantRestarter;
    private TenantElementsRestarter tenantElementsRestarter;

    @Before
    public void before() {
        tenantElementsRestarter = new TenantElementsRestarter(tenantRestarter, tenantElementsRestartSupervisor);
    }

    @Test
    public void should_prepareRestartOfElements_when_the_supervisor_says_so() throws Exception {
        doReturn(true).when(tenantElementsRestartSupervisor).shouldRestartElements();

        tenantElementsRestarter.prepareRestartOfElements();

        verify(tenantRestarter).executeBeforeServicesStart();
    }

    @Test
    public void should_not_prepareRestartOfElements_when_the_supervisor_says_so() throws Exception {
        doReturn(false).when(tenantElementsRestartSupervisor).shouldRestartElements();

        tenantElementsRestarter.prepareRestartOfElements();

        verify(tenantRestarter, never()).executeBeforeServicesStart();
    }

    @Test
    public void should_restartElements_and_notify_when_supervisor_says_so() throws Exception {
        doReturn(true).when(tenantElementsRestartSupervisor).willRestartElements();

        tenantElementsRestarter.restartElements();

        verify(tenantRestarter).executeAfterServicesStart();
    }

    @Test
    public void should_not_restartElements_when_supervisor_says_so() throws Exception {
        doReturn(false).when(tenantElementsRestartSupervisor).willRestartElements();

        tenantElementsRestarter.restartElements();

        verify(tenantRestarter, never()).executeAfterServicesStart();
    }

}
