package org.bonitasoft.engine.api.impl;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.bpm.process.ProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.model.SFlowElementsContainerType;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.execution.TransactionalProcessInstanceInterruptor;
import org.bonitasoft.engine.lock.BonitaLock;
import org.bonitasoft.engine.lock.LockService;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.junit.Test;

public class ProcessAPIImplTest {

    @Test
    public void cancelAnUnknownProcessInstanceThrowsANotFoundException() throws Exception {
        final long tenantId = 1;
        final long processInstanceId = 45;
        final long userId = 9;
        final ProcessAPIImpl processAPI = spy(new ProcessAPIImpl());
        final TenantServiceAccessor tenantAccessor = mock(TenantServiceAccessor.class);
        final LockService lockService = mock(LockService.class);
        final TransactionalProcessInstanceInterruptor interruptor = mock(TransactionalProcessInstanceInterruptor.class);

        doReturn(tenantAccessor).when(processAPI).getTenantAccessor();
        when(tenantAccessor.getTenantId()).thenReturn(tenantId);
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
    public void should_updateProcessDataInstances_call_updateProcessDataInstance_for_each_data_to_update() throws Exception {
        ProcessAPIImpl processAPI = spy(new ProcessAPIImpl());
        doNothing().when(processAPI).updateProcessDataInstance(anyString(), anyLong(), any(Serializable.class));

        Map<String, Serializable> dataNameValues = new HashMap<String, Serializable>();
        dataNameValues.put("foo", "go");
        dataNameValues.put("bar", "go");
        final long processInstanceId = 42l;
        processAPI.updateProcessDataInstances(processInstanceId, dataNameValues);

        verify(processAPI).updateProcessDataInstance(eq("foo"), eq(processInstanceId), eq("go"));
        verify(processAPI).updateProcessDataInstance(eq("bar"), eq(processInstanceId), eq("go"));
    }
}
