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
package org.bonitasoft.engine.execution.work;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.core.process.definition.model.event.SEndEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.impl.SEndEventDefinitionImpl;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.model.STaskPriority;
import org.bonitasoft.engine.core.process.instance.model.event.SThrowEventInstance;
import org.bonitasoft.engine.core.process.instance.model.impl.SUserTaskInstanceImpl;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ExecuteConnectorOfActivityTest {

    private Map<String, Object> context;

    @Mock
    private TenantServiceAccessor accessor;

    @Mock
    private ActivityInstanceService activityInstanceService;

    @Mock
    private EventInstanceService eventService;

    @Before
    public void setUp() {
        context = new HashMap<String, Object>();
        context.put(TenantAwareBonitaWork.TENANT_ACCESSOR, accessor);

        when(accessor.getActivityInstanceService()).thenReturn(activityInstanceService);
        when(accessor.getEventInstanceService()).thenReturn(eventService);
    }

    @Test
    public void createThrowErrorEventInstance_should_return_a_well_formed_instance() throws Exception {
        final ExecuteConnectorOfActivity work = new ExecuteConnectorOfActivity(4L, 45L, 687L, 5357L, "myConnector");
        final SEndEventDefinition eventDefinition = new SEndEventDefinitionImpl(8687L, "end");
        final SUserTaskInstanceImpl instanceImpl = new SUserTaskInstanceImpl("userTask1", 1L, 2L, 3L, 4L, STaskPriority.NORMAL, 5L, 6L);
        instanceImpl.setLogicalGroup(3, 4L);
        when(activityInstanceService.getFlowNodeInstance(687L)).thenReturn(instanceImpl);

        final SThrowEventInstance errorEventInstance = work.createThrowErrorEventInstance(context, eventDefinition);

        assertThat(errorEventInstance.getParentProcessInstanceId()).isEqualTo(4L);
    }

}
