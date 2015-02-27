/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.log.api.impl;

import org.bonitasoft.engine.scheduler.model.SJobDescriptor;
import org.mockito.ArgumentMatcher;

/**
 * @author Elias Ricken de Medeiros
 */
public class JobDescriptorMatcher extends ArgumentMatcher<SJobDescriptor> {

    private String className;

    private String jobName;

    public JobDescriptorMatcher(String className, String jobName) {
        this.className = className;
        this.jobName = jobName;
    }

    @Override
    public boolean matches(Object argument) {
        if (!(argument instanceof SJobDescriptor)) {
            return false;
        }
        SJobDescriptor jobDescr = (SJobDescriptor) argument;
        return className.equals(jobDescr.getJobClassName())
                && jobName.equals(jobDescr.getJobName());
    }

}
