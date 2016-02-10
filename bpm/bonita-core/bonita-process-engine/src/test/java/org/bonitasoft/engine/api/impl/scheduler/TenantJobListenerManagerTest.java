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
package org.bonitasoft.engine.api.impl.scheduler;

import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.scheduler.AbstractBonitaTenantJobListener;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TenantJobListenerManagerTest {

    @Mock
    private SchedulerService schedulerService;

    @InjectMocks
    private TenantJobListenerManager listenerManager;

    @Test
    public void registerListeners_should_call_schedulerService_addTenantListener() throws Exception {
        //given
        List<AbstractBonitaTenantJobListener> listeners = Arrays.asList(mock(AbstractBonitaTenantJobListener.class));

        //when
        listenerManager.registerListeners(listeners, 2L);

        //then
        verify(schedulerService).addJobListener(listeners, "2");
    }

    @Test
    public void registerListeners_should_do_nothing_when_listeners_is_empty() throws Exception {
        //given

        //when
        listenerManager.registerListeners(Collections.<AbstractBonitaTenantJobListener> emptyList(), 2L);

        //then
        verify(schedulerService, never()).addJobListener(anyList(), anyString());
    }

}
