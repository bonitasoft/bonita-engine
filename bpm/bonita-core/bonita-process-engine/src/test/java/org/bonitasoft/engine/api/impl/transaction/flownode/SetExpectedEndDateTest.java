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
package org.bonitasoft.engine.api.impl.transaction.flownode;

import static org.mockito.Mockito.doReturn;

import java.util.Date;

import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Laurent Leseigneur
 */
@RunWith(MockitoJUnitRunner.class)
public class SetExpectedEndDateTest {

    @Mock
    private ActivityInstanceService activityInstanceService;

    @Mock
    private SFlowNodeInstance flowNodeInstance;

    @Test
    public void should_set_valid_due_date() throws Exception {
        //given
        final long dateAsLong = 45L;
        Date date = new Date(dateAsLong);
        final long instanceId = 1L;
        SetExpectedEndDate setExpectedEndDate = new SetExpectedEndDate(activityInstanceService, instanceId, date);
        doReturn(flowNodeInstance).when(activityInstanceService).getFlowNodeInstance(instanceId);
        //when
        setExpectedEndDate.execute();

        //then
        Mockito.verify(activityInstanceService).setExpectedEndDate(flowNodeInstance, dateAsLong);

    }

    @Test
    public void should_set_null_due_date() throws Exception {
        //given
        final long dateAsLong = 45L;
        Date date = new Date(dateAsLong);
        final long instanceId = 1L;
        SetExpectedEndDate setExpectedEndDate = new SetExpectedEndDate(activityInstanceService, instanceId, null);
        doReturn(flowNodeInstance).when(activityInstanceService).getFlowNodeInstance(instanceId);
        //when
        setExpectedEndDate.execute();

        //then
        Mockito.verify(activityInstanceService).setExpectedEndDate(flowNodeInstance, null);

    }

}
