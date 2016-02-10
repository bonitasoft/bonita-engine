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
package org.bonitasoft.engine.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.test.persistence.builder.JobDescriptorBuilder.aJobDescriptor;
import static org.bonitasoft.engine.test.persistence.builder.JobLogBuilder.aJobLog;

import java.util.List;

import javax.inject.Inject;

import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.scheduler.model.SFailedJob;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;
import org.bonitasoft.engine.test.persistence.repository.JobRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/testContext.xml" })
@Transactional
public class SchedulerQueryTest {

    @Inject
    private JobRepository jobRepository;

    @Test
    public void getFailedJobsShouldRetrieveAFailedJobIfThereIsAJobLogAndAJobDescriptor() {
        // given:
        SJobDescriptor addJobDescriptor = jobRepository.addJobDescriptor(aJobDescriptor().build());
        jobRepository.addJobLog(aJobLog().withJobDescriptorId(addJobDescriptor.getId()).build());

        // when:
        List<SFailedJob> failedJobs = jobRepository.getFailedJobs(new QueryOptions(0, 10));

        // then:
        assertThat(failedJobs).hasSize(1);
    }

    @Test
    public void getFailedJobsShouldRetrieveZeroFailedJobIfThereIsNoJobLog() {
        // given:
        jobRepository.addJobDescriptor(aJobDescriptor().build());

        // when:
        List<SFailedJob> failedJobs = jobRepository.getFailedJobs(new QueryOptions(0, 10));

        // then:
        assertThat(failedJobs).hasSize(0);
    }

    @Test
    public void getFailedJobsShouldRetrieveZeroFailedJobIfThereIsNoJobDescriptor() {
        // given:
        jobRepository.addJobLog(aJobLog().withJobDescriptorId(1234566789L).build());

        // when:
        List<SFailedJob> failedJobs = jobRepository.getFailedJobs(new QueryOptions(0, 10));

        // then:
        assertThat(failedJobs).hasSize(0);
    }

}
