/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.monitoring.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.SIdentityException;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.monitoring.SMonitoringException;
import com.bonitasoft.engine.monitoring.TenantMonitoringService;

@RunWith(MockitoJUnitRunner.class)
public class TenantMonitoringServiceImplTest {

    @Mock
    private IdentityService identityService;

    @Mock
    private EventService eventService;

    @Mock
    private SessionService sessionService;

    @Mock
    private SJobHandlerImpl jobHandler;

    @Mock
    private TransactionService transactionService;

    @Mock
    private SessionAccessor sessionAccessor;

    @Mock
    private TechnicalLoggerService loggerService;

    public TenantMonitoringService monitoringService;

    @Before
    public void setUp() throws Exception {
        monitoringService = new TenantMonitoringServiceImpl(false, identityService, eventService, transactionService, sessionAccessor, sessionService,
                jobHandler, loggerService);
    }

    @Test
    public void getNumberOfActiveTransactions() {
        when(transactionService.getNumberOfActiveTransactions()).thenReturn(6L);

        final long numberOfActiveTransactions = monitoringService.getNumberOfActiveTransactions();

        assertThat(numberOfActiveTransactions).isEqualTo(6L);
    }

    @Test
    public void getNumberOfExecutingJobs() {
        when(jobHandler.getExecutingJobs()).thenReturn(64);

        final long numberOfActiveTransactions = monitoringService.getNumberOfExecutingJobs();

        assertThat(numberOfActiveTransactions).isEqualTo(64L);
    }

    @Test
    public void getNumberOfUsers() throws SMonitoringException, SIdentityException {
        when(identityService.getNumberOfUsers()).thenReturn(1045864L);

        final long numberOfActiveTransactions = monitoringService.getNumberOfUsers();

        assertThat(numberOfActiveTransactions).isEqualTo(1045864L);
    }

    @Test(expected = SMonitoringException.class)
    public void getNumberOfUsersThrowsExceptionDueToServiceException() throws SMonitoringException, SIdentityException {
        when(identityService.getNumberOfUsers()).thenThrow(new SIdentityException("exception"));

        monitoringService.getNumberOfUsers();
    }

}
