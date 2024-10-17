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
package org.bonitasoft.engine.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.bonitasoft.engine.commons.Pair.pair;
import static org.bonitasoft.engine.test.persistence.builder.JobDescriptorBuilder.aJobDescriptor;
import static org.bonitasoft.engine.test.persistence.builder.JobLogBuilder.aJobLog;

import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.scheduler.model.SFailedJob;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;
import org.bonitasoft.engine.scheduler.model.SJobLog;
import org.bonitasoft.engine.scheduler.model.SJobParameter;
import org.bonitasoft.engine.test.persistence.jdbc.JdbcRowMapper;
import org.bonitasoft.engine.test.persistence.repository.JobRepository;
import org.hibernate.internal.util.SerializationHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@ContextConfiguration(locations = { "/testContext.xml" })
@Transactional
public class SchedulerQueryTest {

    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void getFailedJobsShouldRetrieveAFailedJobIfThereIsAJobLogAndAJobDescriptor() {
        // given:
        SJobDescriptor addJobDescriptor = jobRepository.add(aJobDescriptor().build());
        jobRepository.add(aJobLog().withJobDescriptorId(addJobDescriptor.getId()).build());

        // when:
        List<SFailedJob> failedJobs = jobRepository.getFailedJobs(new QueryOptions(0, 10));

        // then:
        assertThat(failedJobs).hasSize(1);
    }

    @Test
    public void getFailedJobsShouldRetrieveZeroFailedJobIfThereIsNoJobLog() {
        // given:
        jobRepository.add(aJobDescriptor().build());

        // when:
        List<SFailedJob> failedJobs = jobRepository.getFailedJobs(new QueryOptions(0, 10));

        // then:
        assertThat(failedJobs).hasSize(0);
    }

    @Test
    public void getFailedJobsShouldRetrieveZeroFailedJobIfThereIsNoJobDescriptor() {
        // given:
        jobRepository.add(aJobLog().withJobDescriptorId(1234566789L).build());

        // when:
        List<SFailedJob> failedJobs = jobRepository.getFailedJobs(new QueryOptions(0, 10));

        // then:
        assertThat(failedJobs).hasSize(0);
    }

    @Test
    public void should_save_and_get_SJobDescriptor() {
        SJobDescriptor jobDescriptor = jobRepository.add(SJobDescriptor.builder()
                .jobClassName("com.bonitasoft.JobClass")
                .description("a job")
                .jobName("job name")
                .build());
        jobRepository.flush();

        Long numberOfSJobDescriptor = jobRepository.selectCount("getNumberOfSJobDescriptor");
        PersistentObject searchSJobDescriptor = jobRepository.selectOne("searchSJobDescriptor");
        Map<String, Object> jobDescriptorAsMap = jdbcTemplate.queryForObject("SELECT * FROM job_desc",
                new JdbcRowMapper("TENANTID", "ID"));

        assertThat(numberOfSJobDescriptor).isEqualTo(1);
        assertThat(searchSJobDescriptor).isEqualTo(jobDescriptor);
        assertThat(jobDescriptorAsMap).containsOnly(
                entry("TENANTID", 0L), // remove when tenant notion disappears completely
                entry("ID", jobDescriptor.getId()),
                entry("JOBCLASSNAME", "com.bonitasoft.JobClass"),
                entry("JOBNAME", "job name"),
                entry("DESCRIPTION", "a job"));
    }

    @Test
    public void should_save_and_get_SJobParameter() {
        SJobParameter jobParameter = jobRepository.add(SJobParameter.builder()
                .jobDescriptorId(1234L)
                .key("paramKeyName")
                .value("Some string value")
                .build());

        Long numberOfSJobParameters = jobRepository.selectCount("getNumberOfSJobParameter");
        PersistentObject jobParameterFromQuery = jobRepository.selectOne("getJobParameters",
                pair("jobDescriptorId", 1234L));
        Map<String, Object> jobParameterAsMap = jdbcTemplate.queryForObject("SELECT * FROM job_param",
                new JdbcRowMapper("TENANTID", "ID", "JOBDESCRIPTORID"));

        assertThat(numberOfSJobParameters).isEqualTo(1);
        assertThat(jobParameterFromQuery).isEqualTo(jobParameter);

        assertThat(jobParameterAsMap).containsOnly(
                entry("TENANTID", 0L), // remove when tenant notion disappears completely
                entry("ID", jobParameter.getId()),
                entry("JOBDESCRIPTORID", 1234L),
                entry("KEY_", "paramKeyName"),
                entry("VALUE_", SerializationHelper.serialize("Some string value")));
    }

    @Test
    public void should_save_and_get_SJobLog() {
        SJobLog jobLog = jobRepository.add(SJobLog.builder()
                .jobDescriptorId(1234L)
                .lastMessage("the last message")
                .retryNumber(13L)
                .lastUpdateDate(555555L)
                .build());

        Long numberOfSJobLog = jobRepository.selectCount("getNumberOfSJobLog");
        PersistentObject jobLogFromQuery = jobRepository.selectOne("searchSJobLog");
        Map<String, Object> jobLogAsMap = jdbcTemplate.queryForObject("SELECT * FROM job_log",
                new JdbcRowMapper("TENANTID", "ID", "JOBDESCRIPTORID", "RETRYNUMBER", "LASTUPDATEDATE"));

        assertThat(numberOfSJobLog).isEqualTo(1);
        assertThat(jobLogFromQuery).isEqualTo(jobLog);
        assertThat(jobLogAsMap).containsOnly(
                entry("TENANTID", 0L), // remove when tenant notion disappears completely
                entry("ID", jobLog.getId()),
                entry("JOBDESCRIPTORID", 1234L),
                entry("RETRYNUMBER", 13L),
                entry("LASTUPDATEDATE", 555555L),
                entry("LASTMESSAGE", "the last message"));
    }

}
