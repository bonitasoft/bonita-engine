/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.test.CommonAPILocalIT;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JobSPIT extends CommonAPILocalIT {

    @After
    public void afterTest() throws Exception {
        logoutOnTenant();
    }

    @Before
    public void beforeTest() throws Exception {
        loginOnDefaultTenantWithDefaultTechnicalUser();
    }

    @Test
    public void check_DeleteBatchJob_is_registered() throws Exception {
        final Callable<Boolean> callable = new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                final List<String> jobs = getTenantAccessor().getSchedulerService().getJobs();
                System.out.println("registered jobs=" + jobs);
                return jobs.contains("DeleteBatchJob");
            }
        };
        assertTrue("delete batch job is not registered", getPlatformAccessor().getTransactionService().executeInTransaction(callable));
    }
}
