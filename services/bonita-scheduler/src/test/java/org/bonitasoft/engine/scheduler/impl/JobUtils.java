package org.bonitasoft.engine.scheduler.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SRetryableException;
import org.bonitasoft.engine.events.model.SFireEventException;
import org.bonitasoft.engine.scheduler.StatelessJob;
import org.bonitasoft.engine.scheduler.exception.SJobConfigurationException;
import org.bonitasoft.engine.scheduler.exception.SJobExecutionException;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.impl.JobDetailImpl;

public class JobUtils {


    static JobDetailImpl createJobDetails(long tenantId, long jobDescriptorId) {
        JobDetailImpl jobDetail = new JobDetailImpl();
        jobDetail.setName("someName");
        HashMap<String, Object> map = new HashMap<>();
        map.put("tenantId", String.valueOf(tenantId));
        map.put("jobId", String.valueOf(jobDescriptorId));
        JobDataMap jobDataMap = new JobDataMap(map);
        jobDetail.setJobDataMap(jobDataMap);
        return jobDetail;
    }

    static StatelessJob jobThatFails() {
        return new StatelessJob() {
            @Override
            public String getName() {
                return null;
            }

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public void execute() throws SJobExecutionException, SFireEventException {
                throw new SJobExecutionException("exception");
            }

            @Override
            public void setAttributes(Map<String, Serializable> attributes) throws SJobConfigurationException {

            }
        };
    }
    static StatelessJob jobThatSucceed() {
        return new StatelessJob() {
            @Override
            public String getName() {
                return null;
            }

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public void execute() throws SJobExecutionException, SFireEventException {
                throw new SJobExecutionException("exception");
            }

            @Override
            public void setAttributes(Map<String, Serializable> attributes) throws SJobConfigurationException {

            }
        };
    }
    static StatelessJob jobThatThrowASRetryableException() {
        return new StatelessJob() {
            @Override
            public String getName() {
                return null;
            }

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public void execute() throws SJobExecutionException, SFireEventException {
                throw new SRetryableException("exception");
            }

            @Override
            public void setAttributes(Map<String, Serializable> attributes) throws SJobConfigurationException {

            }
        };
    }
}
