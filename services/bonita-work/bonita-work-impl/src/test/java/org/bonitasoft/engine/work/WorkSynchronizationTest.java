/**
 * Copyright (C) 2017 Bonitasoft S.A.
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

package org.bonitasoft.engine.work;

import static org.bonitasoft.engine.transaction.TransactionState.COMMITTED;
import static org.bonitasoft.engine.transaction.TransactionState.ROLLEDBACK;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Baptiste Mesta.
 */
@RunWith(MockitoJUnitRunner.class)
public class WorkSynchronizationTest {

    @Mock
    private WorkService workService;
    @Mock
    private SessionAccessor sessionAccessor;
    @Mock
    private BonitaExecutorService bonitaExecutorService;
    @InjectMocks
    private WorkSynchronization workSynchronization;
    @Mock
    private BonitaWork bonitaWork1;
    @Mock
    private BonitaWork bonitaWork2;

    @Test
    public void should_submit_work_on_commit() throws Exception {
        workSynchronization.addWork(bonitaWork1);

        workSynchronization.afterCompletion(COMMITTED);

        verify(bonitaExecutorService).execute(bonitaWork1);
    }

    @Test
    public void should_submit_all_work_on_commit() throws Exception {
        workSynchronization.addWork(bonitaWork1);
        workSynchronization.addWork(bonitaWork2);

        workSynchronization.afterCompletion(COMMITTED);

        verify(bonitaExecutorService).execute(bonitaWork1);
        verify(bonitaExecutorService).execute(bonitaWork2);
    }

    @Test
    public void should_not_submit_work_on_transation_not_in_connitted_state() throws Exception {
        workSynchronization.addWork(bonitaWork1);

        workSynchronization.afterCompletion(ROLLEDBACK);

        verify(bonitaExecutorService, never()).execute(bonitaWork1);
    }

}
