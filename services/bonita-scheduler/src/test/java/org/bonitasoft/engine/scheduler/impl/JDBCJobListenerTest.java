/**
 * Copyright (C) 2019 Bonitasoft S.A.
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

import static org.bonitasoft.engine.scheduler.BonitaJobListener.TRIGGER_NEXT_FIRE_TIME;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.scheduler.BonitaJobListener;
import org.bonitasoft.engine.scheduler.JobService;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
@RunWith(MockitoJUnitRunner.class)
public class JDBCJobListenerTest {

    private static final Long JOB_DESCRIPTOR_ID = 50L;
    private final Map<String, Serializable> context = new HashMap<>();
    @Mock
    private JobService jobService;
    @Mock
    private SchedulerService schedulerService;
    private JDBCJobListener jdbcJobListener;

    @Before
    public void setUp() {
        context.put(BonitaJobListener.JOB_DESCRIPTOR_ID, JOB_DESCRIPTOR_ID);
        jdbcJobListener = new JDBCJobListener(jobService, schedulerService);
    }

    @Test
    public void should_delete_job_descriptor_when_job_may_not_fire_again() throws Exception {
        // Given
        context.put(BonitaJobListener.JOB_DESCRIPTOR_ID, JOB_DESCRIPTOR_ID);
        context.put(BonitaJobListener.JOB_NAME, "myJob");
        context.put(BonitaJobListener.JOB_GROUP, "myGroup");
        doReturn(false).when(schedulerService).mayFireAgain("myGroup", "myJob");

        // When
        jdbcJobListener.jobWasExecuted(context, null);

        // Then
        verify(jobService).deleteJobDescriptor(JOB_DESCRIPTOR_ID);
    }

    @Test
    public void should_not_delete_job_descriptor_when_job_may_not_fire_again_but_there_is_other_triggers()
            throws Exception {
        // Given
        context.put(BonitaJobListener.JOB_DESCRIPTOR_ID, JOB_DESCRIPTOR_ID);
        context.put(BonitaJobListener.JOB_NAME, "myJob");
        context.put(BonitaJobListener.JOB_GROUP, "myGroup");
        doReturn(true).when(schedulerService).mayFireAgain("myGroup", "myJob");

        // When
        jdbcJobListener.jobWasExecuted(context, null);

        // Then
        verify(jobService, never()).deleteJobDescriptor(JOB_DESCRIPTOR_ID);
    }

    @Test
    public void should_not_delete_job_descriptor_when_job_may_fire_again() throws Exception {
        // Given
        context.put(BonitaJobListener.JOB_DESCRIPTOR_ID, JOB_DESCRIPTOR_ID);
        context.put(TRIGGER_NEXT_FIRE_TIME, new Date());

        // When
        jdbcJobListener.jobWasExecuted(context, null);

        // Then
        verify(jobService, never()).deleteJobDescriptor(JOB_DESCRIPTOR_ID);
    }

    @Test
    public void should_not_delete_job_descriptor_when_job_failed() throws Exception {
        // Given
        context.put(BonitaJobListener.JOB_DESCRIPTOR_ID, JOB_DESCRIPTOR_ID);
        context.put(TRIGGER_NEXT_FIRE_TIME, new Date());

        // When
        jdbcJobListener.jobWasExecuted(context, new IllegalStateException());

        // Then
        verify(jobService, never()).deleteJobDescriptor(JOB_DESCRIPTOR_ID);
    }

    @Test
    public void jobToBeExecuted_should_do_nothing_when_no_job_descriptor_id() throws Exception {
        // Given
        context.put(BonitaJobListener.JOB_DESCRIPTOR_ID, null);

        // When
        jdbcJobListener.jobToBeExecuted(context);

        // Then
        verifyZeroInteractions(jobService);
    }

    @Test
    public void jobToBeExecuted_should_do_nothing_when_job_descriptor_id_equals_0() throws Exception {
        // Given
        context.put(BonitaJobListener.JOB_DESCRIPTOR_ID, 0L);

        // When
        jdbcJobListener.jobToBeExecuted(context);

        // Then
        verifyZeroInteractions(jobService);
    }

}
