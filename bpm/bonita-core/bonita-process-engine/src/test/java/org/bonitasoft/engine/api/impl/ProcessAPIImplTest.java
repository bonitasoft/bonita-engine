package org.bonitasoft.engine.api.impl;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.actor.mapping.SActorNotFoundException;
import org.bonitasoft.engine.actor.mapping.model.SActor;
import org.bonitasoft.engine.bpm.process.ProcessInstanceNotFoundException;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.model.SFlowElementsContainerType;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceReadException;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.execution.TransactionalProcessInstanceInterruptor;
import org.bonitasoft.engine.lock.BonitaLock;
import org.bonitasoft.engine.lock.LockService;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.model.SJobParameter;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProcessAPIImplTest {

    final long tenantId = 1;

    private static final long ACTOR_ID = 100;

    private static final long PROCESS_DEFINITION_ID = 110;

    private static final String ACTOR_NAME = "employee";

    @Mock
    private TenantServiceAccessor tenantAccessor;

    private ProcessAPIImpl processAPI;

    @Before
    public void setup() {
        processAPI = spy(new ProcessAPIImpl());
        doReturn(tenantAccessor).when(processAPI).getTenantAccessor();
        when(tenantAccessor.getTenantId()).thenReturn(tenantId);
    }

    @Test
    public void cancelAnUnknownProcessInstanceThrowsANotFoundException() throws Exception {
        final long processInstanceId = 45;
        final long userId = 9;
        final LockService lockService = mock(LockService.class);
        final TransactionalProcessInstanceInterruptor interruptor = mock(TransactionalProcessInstanceInterruptor.class);

        when(tenantAccessor.getLockService()).thenReturn(lockService);
        doReturn(userId).when(processAPI).getUserId();
        doReturn(interruptor).when(processAPI).buildProcessInstanceInterruptor(tenantAccessor);
        doThrow(new SProcessInstanceNotFoundException(processInstanceId)).when(interruptor).interruptProcessInstance(processInstanceId,
                SStateCategory.CANCELLING, userId);

        try {
            processAPI.cancelProcessInstance(processInstanceId);
            fail("The process instance does not exists");
        } catch (final ProcessInstanceNotFoundException pinfe) {
            verify(lockService).lock(processInstanceId, SFlowElementsContainerType.PROCESS.name(), tenantId);
            verify(lockService).unlock(any(BonitaLock.class), eq(tenantId));
        }
    }

    @Test
    public void should_updateProcessDataInstance_call_updateProcessDataInstances() throws Exception {
        final long processInstanceId = 42l;
        doNothing().when(processAPI).updateProcessDataInstances(eq(processInstanceId), any(Map.class));

        processAPI.updateProcessDataInstance("foo", processInstanceId, "go");

        verify(processAPI).updateProcessDataInstances(eq(processInstanceId), eq(Collections.<String, Serializable> singletonMap("foo", "go")));
    }

    @Test
    public void should_updateProcessDataInstances_call_DataInstanceService() throws Exception {
        final long processInstanceId = 42l;

        final TenantServiceAccessor tenantAccessor = mock(TenantServiceAccessor.class);
        final DataInstanceService dataInstanceService = mock(DataInstanceService.class);

        doReturn(null).when(processAPI).getProcessInstanceClassloader(any(TenantServiceAccessor.class), anyLong());

        doReturn(tenantAccessor).when(processAPI).getTenantAccessor();
        doReturn(dataInstanceService).when(tenantAccessor).getDataInstanceService();

        SDataInstance sDataFoo = mock(SDataInstance.class);
        doReturn("foo").when(sDataFoo).getName();
        SDataInstance sDataBar = mock(SDataInstance.class);
        doReturn("bar").when(sDataBar).getName();
        doReturn(asList(sDataFoo, sDataBar)).when(dataInstanceService).getDataInstances(eq(asList("foo", "bar")), anyLong(), anyString());

        // Then update the data instances
        Map<String, Serializable> dataNameValues = new HashMap<String, Serializable>();
        dataNameValues.put("foo", "go");
        dataNameValues.put("bar", "go");
        processAPI.updateProcessDataInstances(processInstanceId, dataNameValues);

        // Check that we called DataInstanceService for each pair data/value
        verify(dataInstanceService, times(2)).updateDataInstance(any(SDataInstance.class), any(EntityUpdateDescriptor.class));
        verify(dataInstanceService).updateDataInstance(eq(sDataFoo), any(EntityUpdateDescriptor.class));
        verify(dataInstanceService).updateDataInstance(eq(sDataBar), any(EntityUpdateDescriptor.class));
    }

    @Test
    public void should_updateProcessDataInstances_call_DataInstance_on_non_existing_data_throw_UpdateException() throws Exception {
        final long processInstanceId = 42l;

        final TenantServiceAccessor tenantAccessor = mock(TenantServiceAccessor.class);
        final DataInstanceService dataInstanceService = mock(DataInstanceService.class);

        doReturn(null).when(processAPI).getProcessInstanceClassloader(any(TenantServiceAccessor.class), anyLong());

        doReturn(tenantAccessor).when(processAPI).getTenantAccessor();
        doReturn(dataInstanceService).when(tenantAccessor).getDataInstanceService();

        doThrow(new SDataInstanceReadException("Mocked")).when(dataInstanceService).getDataInstances(eq(asList("foo", "bar")), anyLong(), anyString());

        // Then update the data instances
        Map<String, Serializable> dataNameValues = new HashMap<String, Serializable>();
        dataNameValues.put("foo", "go");
        dataNameValues.put("bar", "go");
        try {
            processAPI.updateProcessDataInstances(processInstanceId, dataNameValues);
            fail("An exception should have been thrown.");
        } catch (UpdateException e) {
            // Ok
        }

        // Check that we called DataInstanceService for each pair data/value
        verify(dataInstanceService, never()).updateDataInstance(any(SDataInstance.class), any(EntityUpdateDescriptor.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void replayingAFailedJobNoParamShouldExecuteAgainSchedulerServiceWithNoParameters() throws Exception {
        long jobDescriptorId = 25L;
        SchedulerService schedulerService = mock(SchedulerService.class);
        when(tenantAccessor.getSchedulerService()).thenReturn(schedulerService);
        doNothing().when(schedulerService).executeAgain(anyLong(), anyList());

        processAPI.replayFailedJob(jobDescriptorId, null);
        processAPI.replayFailedJob(jobDescriptorId, Collections.EMPTY_MAP);

        verify(schedulerService, times(2)).executeAgain(jobDescriptorId);
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void replayingAFailedJobShouldExecuteAgainSchedulerServiceWithSomeParameters() throws Exception {
        final Map<String, Serializable> parameters = Collections.singletonMap("anyparam", (Serializable) Boolean.FALSE);
        long jobDescriptorId = 544L;
        SchedulerService schedulerService = mock(SchedulerService.class);
        when(tenantAccessor.getSchedulerService()).thenReturn(schedulerService);
        doNothing().when(schedulerService).executeAgain(anyLong(), anyList());

        doReturn(new ArrayList()).when(processAPI).getJobParameters(parameters);

        processAPI.replayFailedJob(jobDescriptorId, parameters);

        verify(schedulerService).executeAgain(eq(jobDescriptorId), anyList());
    }

    @Test
    public void replayingAFailedJobWithNoParamShouldCallWithNullParams() throws Exception {
        long jobDescriptorId = 544L;

        // This spy is specific to this test method:
        final ProcessAPIImpl myProcessAPI = spy(new ProcessAPIImpl());
        doNothing().when(myProcessAPI).replayFailedJob(jobDescriptorId, null);

        myProcessAPI.replayFailedJob(jobDescriptorId);

        verify(myProcessAPI).replayFailedJob(jobDescriptorId, null);
    }

    @Test
    public void getJobParametersShouldConvertMapIntoList() {
        // given:
        Map<String, Serializable> parameters = new HashMap<String, Serializable>(2);
        String key1 = "mon param 1";
        String key2 = "my second param";
        SJobParameter expectedValue1 = mockSJobParameter(key1);
        parameters.put(expectedValue1.getKey(), expectedValue1.getValue());

        SJobParameter expectedValue2 = mockSJobParameter(key2);
        parameters.put(expectedValue2.getKey(), expectedValue2.getValue());

        doReturn(expectedValue1).when(processAPI).buildSJobParameter(eq(key1), any(Serializable.class));
        doReturn(expectedValue2).when(processAPI).buildSJobParameter(eq(key2), any(Serializable.class));

        // when:
        List<SJobParameter> jobParameters = processAPI.getJobParameters(parameters);

        // then:
        assertThat(jobParameters).containsOnly(expectedValue1, expectedValue2);
    }

    private SJobParameter mockSJobParameter(final String key) {
        SJobParameter jobParam = mock(SJobParameter.class);
        when(jobParam.getKey()).thenReturn(key);
        when(jobParam.getValue()).thenReturn(Integer.MAX_VALUE);
        return jobParam;
    }

    @Test
    public void getUserIdsForActor_returns_result_of_actor_mapping_service() throws Exception {
        // given
        SActor actor = mock(SActor.class);
        when(actor.getId()).thenReturn(ACTOR_ID);

        ActorMappingService actorMappingService = mock(ActorMappingService.class);
        when(tenantAccessor.getActorMappingService()).thenReturn(actorMappingService);
        when(actorMappingService.getPossibleUserIdsOfActorId(ACTOR_ID, 0, 10)).thenReturn(Arrays.asList(1L, 10L));
        when(actorMappingService.getActor(ACTOR_NAME, PROCESS_DEFINITION_ID)).thenReturn(actor);

        // when
        List<Long> userIdsForActor = processAPI.getUserIdsForActor(PROCESS_DEFINITION_ID, ACTOR_NAME, 0, 10);

        // then
        assertThat(userIdsForActor).containsExactly(1L, 10L);
    }

    @Test
    public void getUserIdsForActor_throws_RetrieveException_when_actorMappingService_throws_SBonitaException() throws Exception {
        // given
        SActor actor = mock(SActor.class);
        when(actor.getId()).thenReturn(ACTOR_ID);

        ActorMappingService actorMappingService = mock(ActorMappingService.class);
        when(tenantAccessor.getActorMappingService()).thenReturn(actorMappingService);
        when(actorMappingService.getActor(ACTOR_NAME, PROCESS_DEFINITION_ID)).thenThrow(new SActorNotFoundException(""));

        // when
        try {
            processAPI.getUserIdsForActor(PROCESS_DEFINITION_ID, ACTOR_NAME, 0, 10);
            fail("Exception expected");
        } catch (RetrieveException e) {
            // then ok
        }

    }

}
