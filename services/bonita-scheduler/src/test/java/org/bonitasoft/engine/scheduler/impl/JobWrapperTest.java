/**
 * Copyright (C) 2015 Bonitasoft S.A.
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

package org.bonitasoft.engine.scheduler.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.scheduler.JobIdentifier;
import org.bonitasoft.engine.scheduler.JobService;
import org.bonitasoft.engine.scheduler.StatelessJob;
import org.bonitasoft.engine.scheduler.exception.SJobExecutionException;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.BonitaTransactionSynchronization;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Baptiste Mesta
 */
@RunWith(MockitoJUnitRunner.class)
public class JobWrapperTest {



    private JobWrapper jobWrapper;
    @Mock
    private TechnicalLoggerService logger;
    @Mock
    private EventService eventService;
    @Mock
    private SessionAccessor sessionAccessor;
    @Mock
    private TransactionService transactionService;
    @Mock
    private PersistenceService persistenceService;
    @Mock
    private JobService jobService;
    @Mock
    private StatelessJob job;


    @Before
    public void before(){
        jobWrapper = new JobWrapper(new JobIdentifier(145, 2, "MyJob"), job, logger, 2, eventService, sessionAccessor, transactionService, persistenceService, jobService);
    }



    @Test
    public void should_executeJob_execute_the_job() throws Exception {
        //when
        jobWrapper.execute();
        //then
        verify(job).execute();

    }

    @Test
    public void should_executeJob_call_flush_on_persistence() throws Exception {
        //when
        jobWrapper.execute();
        //then
        verify(persistenceService).flushStatements();
    }

    @Test
    public void should_execute_call_rollback_on_error() throws Exception {
        //given
        doThrow(new RuntimeException()).when(job).execute();
        //when
        try {

            jobWrapper.execute();
            fail("should have thrown exception");
        }catch (SJobExecutionException e){
            assertThat(e.getCause()).isInstanceOf(RuntimeException.class);
        }
        //then
        verify(transactionService).setRollbackOnly();

    }

    @Test
    public void should_handleFailure_register_synchro() throws Exception {
        //given

        //when
        jobWrapper.handleFailure(new RuntimeException());

        //then
        verify(transactionService).registerBonitaSynchronization(any(BonitaTransactionSynchronization.class));

    }

}
