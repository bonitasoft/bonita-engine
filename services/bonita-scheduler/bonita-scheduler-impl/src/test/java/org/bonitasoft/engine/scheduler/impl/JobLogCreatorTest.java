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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.scheduler.JobService;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;
import org.bonitasoft.engine.scheduler.model.SJobLog;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JobLogCreatorTest {

    @Mock
    private JobService jobService;

    @Mock
    private TechnicalLoggerService loggerService;

    @InjectMocks
    private JobLogCreator creator;

    @Test
    public void createJobLog_should_call_job_service_createJobLog_when_related_job_descriptor_exists() throws Exception {
        //given
        Exception exception = new Exception("Missing mandatory parameter");
        long jobDescriptorId = 4L;
        long before = System.currentTimeMillis();
        given(jobService.getJobDescriptor(jobDescriptorId)).willReturn(mock(SJobDescriptor.class));

        //when
        creator.createJobLog(exception, jobDescriptorId);

        //then
        ArgumentCaptor<SJobLog> captor = ArgumentCaptor.forClass(SJobLog.class);
        verify(jobService).createJobLog(captor.capture());
        SJobLog jobLog = captor.getValue();
        assertThat(jobLog.getRetryNumber()).isEqualTo(0);
        assertThat(jobLog.getJobDescriptorId()).isEqualTo(jobDescriptorId);
        assertThat(jobLog.getLastMessage()).contains(exception.getMessage());
        assertThat(jobLog.getLastUpdateDate()).isGreaterThanOrEqualTo(before);
    }

    @Test
    public void createJobLog_should_not_create_job_log_when_related_job_descriptor_does_not_exist() throws Exception {
        //given
        Exception exception = new Exception("Missing mandatory parameter");
        long jobDescriptorId = 4L;
        given(jobService.getJobDescriptor(jobDescriptorId)).willReturn(null);

        //when
        creator.createJobLog(exception, jobDescriptorId);

        //then
        verify(jobService, never()).createJobLog(any(SJobLog.class));
    }
}