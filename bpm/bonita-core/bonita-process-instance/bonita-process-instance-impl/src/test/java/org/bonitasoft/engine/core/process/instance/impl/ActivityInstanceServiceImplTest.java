package org.bonitasoft.engine.core.process.instance.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityReadException;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.SFlowNodeInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.impl.SUserTaskInstanceBuilderFactoryImpl;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.bonitasoft.engine.services.PersistenceService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ActivityInstanceServiceImplTest {

    @Mock
    private PersistenceService persistenceService;
    
    @Mock
    private Recorder recorder;
    
    @Mock
    private TechnicalLoggerService logger;

    @InjectMocks
    private ActivityInstanceServiceImpl activityInstanceService;
    
    private SFlowNodeInstanceBuilderFactory keyProvider = new SUserTaskInstanceBuilderFactoryImpl();

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
        when(persistenceService.selectList(Matchers.<SelectListDescriptor<Long>>any())).thenReturn(sUserIds);

        final List<Long> userIds = activityInstanceService.getPossibleUserIdsOfPendingTasks(2, 0, 10);
        assertEquals(sUserIds, userIds);
    }

    @Test(expected = SActivityReadException.class)
    public void throwExceptionwhenGettingPossibleUserIdsOfPendingTasksDueToPersistenceException() throws SActivityReadException, SBonitaReadException {
        when(persistenceService.selectList(Matchers.<SelectListDescriptor<Long>>any())).thenThrow(new SBonitaReadException("database out"));
        activityInstanceService.getPossibleUserIdsOfPendingTasks(2, 0, 10);
    }

    @Test
    public void getEmptyPossibleUserIdsOfPendingTasks() throws SActivityReadException, SBonitaReadException {
        when(persistenceService.selectList(Matchers.<SelectListDescriptor<Long>>any())).thenReturn(Collections.<Long>emptyList());

        final List<Long> userIds = activityInstanceService.getPossibleUserIdsOfPendingTasks(2, 0, 10);
        assertEquals(Collections.emptyList(), userIds);
    }
    
    @Test
    public void updateDisplayName_should_truncate_when_display_name_is_bigger_than_75_characters() throws Exception {
        //given
        String displayName75 = "123456789 123456789 123456789 123456789 123456789 123456789 123456789 12345";
        String displayName = displayName75 + displayName75;
        SFlowNodeInstance flowNode = mock(SFlowNodeInstance.class);
        
        //when
        activityInstanceService.updateDisplayName(flowNode, displayName);

        //then
        //the value must be truncated to 75 characters
        checkFlowNodeUpdate(keyProvider.getDisplayNameKey(), displayName75);
    }

    private void checkFlowNodeUpdate(String attributeKey, String expectedValue) throws SRecorderException {
        ArgumentCaptor<UpdateRecord> recordCaptor = ArgumentCaptor.forClass(UpdateRecord.class);
        verify(recorder, times(1)).recordUpdate(recordCaptor.capture(), any(SUpdateEvent.class));
        Object actualValue = recordCaptor.getValue().getFields().get(attributeKey);
        assertThat(actualValue).isEqualTo(expectedValue);
    }

    @Test
    public void updateDisplayName_should_not_change_value_when_display_name_is_lower_than_75_characters() throws Exception {
        //given
        String displayName = "simple task";
        SFlowNodeInstance flowNode = mock(SFlowNodeInstance.class);
        
        //when
        activityInstanceService.updateDisplayName(flowNode, displayName);
        
        //then
        //keep original value
        checkFlowNodeUpdate(keyProvider.getDisplayNameKey(), displayName);
    }

    @Test
    public void updateDisplayName_should_not_change_value_when_display_name_is_75_characters_long() throws Exception {
        //given
        String displayName75 = "123456789 123456789 123456789 123456789 123456789 123456789 123456789 12345";
        SFlowNodeInstance flowNode = mock(SFlowNodeInstance.class);
        
        //when
        activityInstanceService.updateDisplayName(flowNode, displayName75);

        //then
        //keep original value
        checkFlowNodeUpdate(keyProvider.getDisplayNameKey(), displayName75);
    }
    
    @Test
    public void updateDisplayDescription_should_truncate_when_display_description_is_bigger_than_255_characters() throws Exception {
        //given
        String displayDescr255 = "123456789 123456789 123456789 123456789 123456789 "
                + "123456789 123456789 123456789 123456789 123456789 "
                + "123456789 123456789 123456789 123456789 123456789 "
                + "123456789 123456789 123456789 123456789 123456789 "
                + "123456789 123456789 123456789 123456789 123456789 12345";
        String displayDescr = displayDescr255 + displayDescr255;
        SFlowNodeInstance flowNode = mock(SFlowNodeInstance.class);

        //when
        activityInstanceService.updateDisplayDescription(flowNode, displayDescr);

        //then
        //the value must be truncated to 255 characters
        checkFlowNodeUpdate(keyProvider.getDisplayDescriptionKey(), displayDescr255);
    }

}
