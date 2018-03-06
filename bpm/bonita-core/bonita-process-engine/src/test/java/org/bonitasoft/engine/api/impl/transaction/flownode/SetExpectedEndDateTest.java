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
