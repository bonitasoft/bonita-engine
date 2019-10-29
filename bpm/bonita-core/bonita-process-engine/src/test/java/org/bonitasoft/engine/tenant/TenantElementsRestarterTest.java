/**
 * Copyright (C) 2019 BonitaSoft S.A.
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
package org.bonitasoft.engine.tenant;


import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Arrays;

import org.bonitasoft.engine.tenant.restart.TenantRestartHandler;
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
    private TenantElementsRestarterSupervisor tenantElementsRestarterSupervisor;
    @Mock
    private TenantRestarter tenantRestarter;
    @Mock
    private TenantRestartHandler tenantRestartHandler1;
    @Mock
    private TenantRestartHandler tenantRestartHandler2;
    private TenantElementsRestarter tenantElementsRestarter;

    @Before
    public void before() {
        tenantElementsRestarter = new TenantElementsRestarter(Arrays.asList(tenantRestartHandler1, tenantRestartHandler2), tenantRestarter, tenantElementsRestarterSupervisor);
    }

    @Test
    public void should_prepareRestartOfElements_when_the_supervisor_says_so() throws Exception {
        doReturn(true).when(tenantElementsRestarterSupervisor).shouldRestartElements();

        tenantElementsRestarter.prepareRestartOfElements();

        verify(tenantRestarter).executeBeforeServicesStart();
    }
    @Test
    public void should_not_prepareRestartOfElements_when_the_supervisor_says_so() throws Exception {
        doReturn(false).when(tenantElementsRestarterSupervisor).shouldRestartElements();

        tenantElementsRestarter.prepareRestartOfElements();

        verify(tenantRestarter, never()).executeBeforeServicesStart();
    }
    @Test
    public void should_restartElements_and_notify_when_supervisor_says_so() throws Exception {
        doReturn(true).when(tenantElementsRestarterSupervisor).shouldRestartElements();

        tenantElementsRestarter.restartElements();

        verify(tenantRestarter).executeAfterServicesStart(Arrays.asList(tenantRestartHandler1, tenantRestartHandler2));
        verify(tenantElementsRestarterSupervisor).notifyElementsAreRestarted();
    }

    @Test
    public void should_not_restartElements_when_supervisor_says_so() throws Exception {
        doReturn(false).when(tenantElementsRestarterSupervisor).shouldRestartElements();

        tenantElementsRestarter.restartElements();

        verify(tenantRestarter, never()).executeAfterServicesStart(anyList());
        verify(tenantElementsRestarterSupervisor, never()).notifyElementsAreRestarted();
    }

}