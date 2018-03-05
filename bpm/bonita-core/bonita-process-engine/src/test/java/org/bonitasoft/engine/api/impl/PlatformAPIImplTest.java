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
package org.bonitasoft.engine.api.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.eq;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.exception.BonitaHomeConfigurationException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.platform.model.impl.STenantImpl;
import org.bonitasoft.engine.scheduler.AbstractBonitaPlatformJobListener;
import org.bonitasoft.engine.scheduler.AbstractBonitaTenantJobListener;
import org.bonitasoft.engine.scheduler.JobRegister;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.exception.SSchedulerException;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.BonitaTransactionSynchronization;
import org.bonitasoft.engine.transaction.STransactionCommitException;
import org.bonitasoft.engine.transaction.STransactionCreationException;
import org.bonitasoft.engine.transaction.STransactionException;
import org.bonitasoft.engine.transaction.STransactionNotFoundException;
import org.bonitasoft.engine.transaction.STransactionRollbackException;
import org.bonitasoft.engine.transaction.TransactionService;
import org.bonitasoft.engine.transaction.TransactionState;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PlatformAPIImplTest {

    public static final long TENANT_ID = 56423L;
    private final List<STenant> tenants = Collections.singletonList(mock(STenant.class));
    private final List<AbstractBonitaTenantJobListener> tenantJobListeners = Collections.singletonList(mock(AbstractBonitaTenantJobListener.class));
    private final List<AbstractBonitaPlatformJobListener> platformJobListeners = Collections.singletonList(mock(AbstractBonitaPlatformJobListener.class));
    @Mock
    private PlatformServiceAccessor platformServiceAccessor;
    @Mock
    private SchedulerService schedulerService;
    @Mock
    private SessionService sessionService;
    @Mock
    private SessionAccessor sessionAccessor;
    @Mock
    private NodeConfiguration platformConfiguration;
    @Mock
    private TenantServiceAccessor tenantServiceAccessor;
    @Mock
    private TenantConfiguration tenantConfiguration;
    @Mock
    private BonitaHomeServer bonitaHomeServer;
    @Mock
    private PlatformService platformService;
    @Mock
    private STenant sTenant;
    private TransactionService transactionService = new MockedTransactionService();
    @Spy
    @InjectMocks
    private PlatformAPIImpl platformAPI;

    @Before
    public void setup() throws Exception {
        doReturn(schedulerService).when(platformServiceAccessor).getSchedulerService();
        doReturn(platformConfiguration).when(platformServiceAccessor).getPlatformConfiguration();
        doReturn(platformService).when(platformServiceAccessor).getPlatformService();
        doReturn(transactionService).when(platformServiceAccessor).getTransactionService();
        doReturn(tenantServiceAccessor).when(platformServiceAccessor).getTenantServiceAccessor(anyLong());
        doReturn(platformJobListeners).when(platformConfiguration).getJobListeners();

        doReturn(schedulerService).when(tenantServiceAccessor).getSchedulerService();
        doReturn(sessionService).when(tenantServiceAccessor).getSessionService();
        doReturn(tenantConfiguration).when(tenantServiceAccessor).getTenantConfiguration();
        doReturn(tenantJobListeners).when(tenantConfiguration).getJobListeners();

        doReturn(platformServiceAccessor).when(platformAPI).getPlatformAccessor();
        doReturn(sessionAccessor).when(platformAPI).createSessionAccessor();
        doReturn(-1L).when(platformAPI).createSession(anyLong(), any(SessionService.class));
        doReturn(tenants).when(platformAPI).getTenants(platformServiceAccessor);
        doReturn(bonitaHomeServer).when(platformAPI).getBonitaHomeServerInstance();
        PlatformAPIImpl.isNodeStarted = false;
    }

    @Test
    public void rescheduleErroneousTriggers_should_call_rescheduleErroneousTriggers() throws Exception {
        platformAPI.rescheduleErroneousTriggers();

        verify(schedulerService).rescheduleErroneousTriggers();
    }

    @Test(expected = UpdateException.class)
    public void rescheduleErroneousTriggers_should_throw_exception_when_rescheduleErroneousTriggers_failed() throws Exception {
        doThrow(new SSchedulerException("failed")).when(schedulerService).rescheduleErroneousTriggers();

        platformAPI.rescheduleErroneousTriggers();
    }

    @Test(expected = UpdateException.class)
    public void rescheduleErroneousTriggers_should_throw_exception_when_cant_getPlatformAccessor() throws Exception {
        doThrow(new IOException()).when(platformAPI).getPlatformAccessor();

        platformAPI.rescheduleErroneousTriggers();
    }

    @Test
    public void startNode_should_call_startScheduler_when_node_is_not_started() throws Exception {
        // Given
        doNothing().when(platformAPI).checkPlatformVersion(platformServiceAccessor);
        doNothing().when(platformAPI).startPlatformServices(platformServiceAccessor);
        doReturn(false).when(platformAPI).isNodeStarted();
        doReturn(Collections.singletonMap(sTenant, Collections.emptyList())).when(platformAPI)
                .beforeServicesStartOfRestartHandlersOfTenant(platformServiceAccessor, sessionAccessor, tenants);
        doNothing().when(platformAPI).startServicesOfTenants(platformServiceAccessor, sessionAccessor, tenants);
        doNothing().when(platformAPI).restartHandlersOfPlatform(platformServiceAccessor);
        doNothing().when(platformAPI).afterServicesStartOfRestartHandlersOfTenant(eq(platformServiceAccessor), anyMap());
        doNothing().when(platformAPI).registerMissingTenantsDefaultJobs(platformServiceAccessor, sessionAccessor, tenants);

        // When
        platformAPI.startNode();

        // Then
        verify(platformAPI).startScheduler(platformServiceAccessor, tenants);
    }

    @Test
    public void startNode_should_not_call_startScheduler_when_node_is_started() throws Exception {
        // Given
        doNothing().when(platformAPI).checkPlatformVersion(platformServiceAccessor);
        doNothing().when(platformAPI).startPlatformServices(platformServiceAccessor);
        doReturn(true).when(platformAPI).isNodeStarted();
        doNothing().when(platformAPI).startServicesOfTenants(platformServiceAccessor, sessionAccessor, tenants);
        doNothing().when(platformAPI).registerMissingTenantsDefaultJobs(platformServiceAccessor, sessionAccessor, tenants);

        // When
        platformAPI.startNode();

        // Then
        verify(platformAPI, never()).startScheduler(platformServiceAccessor, tenants);
    }

    @Test
    public void startNode_should_call_registerMissingTenantsDefaultJobs() throws Exception {
        // Given
        doNothing().when(platformAPI).checkPlatformVersion(platformServiceAccessor);
        doNothing().when(platformAPI).startPlatformServices(platformServiceAccessor);
        doReturn(true).when(platformAPI).isNodeStarted();
        doNothing().when(platformAPI).startServicesOfTenants(platformServiceAccessor, sessionAccessor, tenants);
        doNothing().when(platformAPI).registerMissingTenantsDefaultJobs(platformServiceAccessor, sessionAccessor, tenants);

        // When
        platformAPI.startNode();

        // Then
        verify(platformAPI).registerMissingTenantsDefaultJobs(platformServiceAccessor, sessionAccessor, tenants);
    }

    @Test
    public void registerMissingTenantsDefaultJobs_should_call_registerJob_when_job_is_missing() throws BonitaHomeNotSetException,
            BonitaHomeConfigurationException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException,
            SBonitaException, IOException, ClassNotFoundException {
        // Given
        final TransactionService transactionService = mock(TransactionService.class);
        doReturn(transactionService).when(platformServiceAccessor).getTransactionService();
        doNothing().when(transactionService).begin();
        doNothing().when(transactionService).complete();
        final JobRegister jobRegister = mock(JobRegister.class);
        doReturn("newJob").when(jobRegister).getJobName();
        final List<JobRegister> defaultJobs = Collections.singletonList(jobRegister);
        doReturn(defaultJobs).when(tenantConfiguration).getJobsToRegister();
        final List<String> scheduledJobNames = Collections.singletonList("someOtherJob");
        doReturn(scheduledJobNames).when(schedulerService).getJobs();
        doNothing().when(platformAPI).registerJob(schedulerService, jobRegister);

        // When
        platformAPI.registerMissingTenantsDefaultJobs(platformServiceAccessor, sessionAccessor, tenants);

        // Then
        verify(platformAPI).registerJob(schedulerService, jobRegister);
    }

    @Test
    public void registerMissingTenantsDefaultJobs_should_not_call_registerJob_when_job_is_scheduled() throws BonitaHomeNotSetException,
            BonitaHomeConfigurationException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException,
            SBonitaException, IOException, ClassNotFoundException {
        // Given
        final TransactionService transactionService = mock(TransactionService.class);
        doReturn(transactionService).when(platformServiceAccessor).getTransactionService();
        doNothing().when(transactionService).begin();
        doNothing().when(transactionService).complete();
        final JobRegister jobRegister = mock(JobRegister.class);
        doReturn("existingJob").when(jobRegister).getJobName();
        final List<JobRegister> defaultJobs = Collections.singletonList(jobRegister);
        doReturn(defaultJobs).when(tenantConfiguration).getJobsToRegister();
        final List<String> scheduledJobNames = Collections.singletonList("existingJob");
        doReturn(scheduledJobNames).when(schedulerService).getJobs();

        // When
        platformAPI.registerMissingTenantsDefaultJobs(platformServiceAccessor, sessionAccessor, tenants);

        // Then
        verify(platformAPI, never()).registerJob(schedulerService, jobRegister);
    }

    @Test
    public void startScheduler_should_register_PlatformJobListeners_and_TenantJobListeners_when_scheduler_starts() throws Exception {
        // Given
        doReturn(true).when(platformConfiguration).shouldStartScheduler();
        doReturn(false).when(schedulerService).isStarted();

        // When
        platformAPI.startScheduler(platformServiceAccessor, tenants);

        // Then
        verify(schedulerService).initializeScheduler();
        verify(schedulerService).addJobListener(anyList());
        verify(schedulerService).addJobListener(anyList(), anyString());
        verify(schedulerService).start();
    }

    @Test
    public void startScheduler_should_not_register_PlatformJobListeners_and_TenantJobListeners_when_scheduler_is_started() throws Exception {
        // Given
        doReturn(true).when(platformConfiguration).shouldStartScheduler();
        doReturn(true).when(schedulerService).isStarted();

        // When
        platformAPI.startScheduler(platformServiceAccessor, tenants);

        // Then
        verify(schedulerService, never()).initializeScheduler();
        verify(schedulerService, never()).addJobListener(anyList());
        verify(schedulerService, never()).addJobListener(anyList(), anyString());
        verify(schedulerService, never()).start();
    }

    @Test
    public void startScheduler_should_not_register_PlatformJobListeners_and_TenantJobListeners_when_scheduler_should_not_be_started() throws Exception {
        // Given
        doReturn(false).when(platformConfiguration).shouldStartScheduler();

        // When
        platformAPI.startScheduler(platformServiceAccessor, tenants);

        // Then
        verify(schedulerService, never()).initializeScheduler();
        verify(schedulerService, never()).addJobListener(anyList());
        verify(schedulerService, never()).addJobListener(anyList(), anyString());
        verify(schedulerService, never()).start();
    }

    @Test
    public void startScheduler_should_not_register_JobListeners_when_none_are_configured() throws Exception {
        // Given
        doReturn(true).when(platformConfiguration).shouldStartScheduler();
        doReturn(false).when(schedulerService).isStarted();
        doReturn(Collections.EMPTY_LIST).when(platformConfiguration).getJobListeners();
        doReturn(Collections.EMPTY_LIST).when(tenantConfiguration).getJobListeners();

        // When
        platformAPI.startScheduler(platformServiceAccessor, tenants);

        // Then
        verify(schedulerService).initializeScheduler();
        verify(schedulerService, never()).addJobListener(anyList());
        verify(schedulerService, never()).addJobListener(anyList(), anyString());
        verify(schedulerService).start();
    }

    @Test
    public void should_updateTenantPortalConfigurationFile_call_bonitaHomeServer() throws Exception {
        //when
        platformAPI.updateClientTenantConfigurationFile(TENANT_ID, "myProps.properties", "updated content".getBytes());
        //then
        verify(bonitaHomeServer).updateTenantPortalConfigurationFile(TENANT_ID, "myProps.properties", "updated content".getBytes());
    }

    @Test
    public void should_getTenantPortalConfigurationFile_call_bonitaHomeServer() throws Exception {
        //given
        final String configurationFile = "a file";
        doReturn("content".getBytes()).when(bonitaHomeServer).getTenantPortalConfiguration(TENANT_ID, configurationFile);

        //when
        final byte[] configuration = platformAPI.getClientTenantConfiguration(TENANT_ID, configurationFile);

        //then
        assertThat(configuration).as("should return file content").isEqualTo("content".getBytes());
        verify(bonitaHomeServer).getTenantPortalConfiguration(TENANT_ID, configurationFile);
    }

    @Test
    public void should_deactivate_and_delete_tenant_when_cleaning_platform() throws Exception {
        //given
        STenantImpl tenant1 = new STenantImpl("t1", "john", 123342, "ACTIVATED", true);
        tenant1.setId(1L);
        STenantImpl tenant2 = new STenantImpl("t2", "john", 12335645, "ACTIVATED", false);
        tenant2.setId(2L);
        doReturn(Arrays.asList(tenant1,
                tenant2)).when(platformService).getTenants(any(QueryOptions.class));
        doNothing().when(platformAPI).deleteTenant(anyLong());
        //when
        platformAPI.cleanPlatform();
        //then
        verify(platformService).deactiveTenant(1L);
        verify(platformService).deactiveTenant(2L);
        verify(platformAPI).deleteTenant(1L);
        verify(platformAPI).deleteTenant(2L);
    }

    private static class MockedTransactionService implements TransactionService, TransactionExecutor {

        @Override
        public void begin() throws STransactionCreationException {
        }

        @Override
        public void complete() throws STransactionCommitException, STransactionRollbackException {
        }

        public TransactionState getState() throws STransactionException {
            return null;
        }

        @Override
        public boolean isTransactionActive() throws STransactionException {
            return false;
        }

        @Override
        public void setRollbackOnly() throws STransactionException {
        }

        @Override
        public boolean isRollbackOnly() throws STransactionException {
            return false;
        }

        @Override
        public long getNumberOfActiveTransactions() {
            return 0;
        }

        @Override
        public <T> T executeInTransaction(Callable<T> callable) throws Exception {
            return callable.call();
        }

        @Override
        public void registerBonitaSynchronization(BonitaTransactionSynchronization txSync) throws STransactionNotFoundException {

        }

        @Override
        public void registerBeforeCommitCallable(Callable<Void> callable) throws STransactionNotFoundException {

        }

        @Override
        public void execute(TransactionContent transactionContent) throws SBonitaException {
            transactionContent.execute();
        }
    }
}
