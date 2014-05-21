package org.bonitasoft.engine.core.process.instance.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityReadException;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.services.PersistenceService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ActivityInstanceServiceImplTest {

    @Mock
    private PersistenceService persistenceService;

    @InjectMocks
    private ActivityInstanceServiceImpl activityInstanceService;

    @Test
    public void getProcessInstanceId_should_return_container_id_if_containerType_is_process() throws Exception {
        final long expectedContainerId = 1L;

        final long processInstanceId = activityInstanceService.getProcessInstanceId(expectedContainerId, DataInstanceContainer.PROCESS_INSTANCE.name());

        assertThat(processInstanceId).isEqualTo(expectedContainerId);
    }

    @Test
    public void getProcessInstanceId_should_return_taskInstance_processId_if_containerType_is_activity() throws Exception {
        SFlowNodeInstance flowNode = mock(SFlowNodeInstance.class);
        when(flowNode.getParentProcessInstanceId()).thenReturn(1234L);
        long activityInstanceId = 456L;
        when(flowNode.getId()).thenReturn(activityInstanceId);
        ActivityInstanceServiceImpl spy = spy(activityInstanceService);
        doReturn(flowNode).when(spy).getFlowNodeInstance(activityInstanceId);
        final long processInstanceId = spy.getProcessInstanceId(flowNode.getId(), DataInstanceContainer.ACTIVITY_INSTANCE.name());

        assertThat(processInstanceId).isEqualTo(flowNode.getParentProcessInstanceId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getProcessInstanceId_should_throw_an_exception_for_a_not_supported_container_type() throws Exception {
        activityInstanceService.getProcessInstanceId(1L, DataInstanceContainer.MESSAGE_INSTANCE.name());
    }

    @Test
    public void getPossibleUserIdsOfPendingTasks() throws SActivityReadException, SBonitaReadException {
        final List<Long> sUserIds = new ArrayList<Long>();
        Collections.addAll(sUserIds, 78l, 2l, 5l, 486l);
        when(persistenceService.selectList(any(SelectListDescriptor.class))).thenReturn(sUserIds);

        final List<Long> userIds = activityInstanceService.getPossibleUserIdsOfPendingTasks(2, 0, 10);
        assertEquals(sUserIds, userIds);
    }

    @Test(expected = SActivityReadException.class)
    public void throwExceptionwhenGettingPossibleUserIdsOfPendingTasksDueToPersistenceException() throws SActivityReadException, SBonitaReadException {
        when(persistenceService.selectList(any(SelectListDescriptor.class))).thenThrow(new SBonitaReadException("database out"));
        activityInstanceService.getPossibleUserIdsOfPendingTasks(2, 0, 10);
    }

    @Test
    public void getEmptyPossibleUserIdsOfPendingTasks() throws SActivityReadException, SBonitaReadException {
        when(persistenceService.selectList(any(SelectListDescriptor.class))).thenReturn(Collections.emptyList());

        final List<Long> userIds = activityInstanceService.getPossibleUserIdsOfPendingTasks(2, 0, 10);
        assertEquals(Collections.emptyList(), userIds);
    }

}
