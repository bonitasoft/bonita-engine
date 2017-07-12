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
package org.bonitasoft.engine.scheduler.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.scheduler.exception.failedJob.SFailedJobReadException;
import org.bonitasoft.engine.scheduler.model.SFailedJob;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Celine Souchet
 */
@RunWith(MockitoJUnitRunner.class)
public class JobServiceImplForFailedJobTest {

    @Mock
    private EventService eventService;

    @Mock
    private QueriableLoggerService queriableLoggerService;

    @Mock
    private ReadPersistenceService readPersistenceService;

    @Mock
    private Recorder recorder;

    @InjectMocks
    private JobServiceImpl jobServiceImpl;

    @Test
    public final void getFailedJobs() throws SBonitaReadException, SFailedJobReadException {
        // Given
        final SFailedJob sFailedJob = mock(SFailedJob.class);
        when(readPersistenceService.selectList(Matchers.<SelectListDescriptor<SFailedJob>> any())).thenReturn(Collections.singletonList(sFailedJob));

        // When
        final SFailedJob result = jobServiceImpl.getFailedJobs(0, 10).get(0);

        // Then
        assertEquals(sFailedJob, result);
        verify(readPersistenceService).selectList(Matchers.<SelectListDescriptor<SFailedJob>> any());
    }

    @Test(expected = SFailedJobReadException.class)
    public void getFailedJobs_should_throw_exception_when_persistenceService_failed() throws SBonitaReadException, SFailedJobReadException {
        doThrow(new SBonitaReadException("")).when(readPersistenceService).selectList(Matchers.<SelectListDescriptor<SFailedJob>> any());

        jobServiceImpl.getFailedJobs(0, 10);
    }
}
