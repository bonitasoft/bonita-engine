/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine;

import com.bonitasoft.engine.activity.TaskOnDemandIT;
import com.bonitasoft.engine.business.application.ApplicationAPIExtITs;
import com.bonitasoft.engine.business.application.ApplicationAPIITs;
import com.bonitasoft.engine.business.data.BDRepositoryIT;
import com.bonitasoft.engine.command.ExecuteBDMQueryCommandIT;
import com.bonitasoft.engine.external.ExternalCommandsTestSP;
import com.bonitasoft.engine.log.LogTest;
import com.bonitasoft.engine.monitoring.MonitoringAPITest;
import com.bonitasoft.engine.monitoring.PlatformMonitoringAPITest;
import com.bonitasoft.engine.page.PageAPIIT;
import com.bonitasoft.engine.platform.NodeAPITest;
import com.bonitasoft.engine.process.ProcessManagementTest;
import com.bonitasoft.engine.profile.ProfileSPITest;
import com.bonitasoft.engine.reporting.ReportingSQLValidityIT;
import com.bonitasoft.engine.search.SearchProcessInstanceTest;
import com.bonitasoft.engine.supervisor.ProcessSupervisedTest;
import com.bonitasoft.engine.tenant.TenantIT;
import org.bonitasoft.engine.activity.PendingTasksIT;
import org.bonitasoft.engine.exception.BonitaException;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        PageAPIIT.class,
        ApplicationAPIITs.class,
        ApplicationAPIExtITs.class,
        NodeAPITest.class,
        LogTest.class,
        ExternalCommandsTestSP.class,
        TaskOnDemandIT.class,
        ProcessManagementTest.class,
        PendingTasksIT.class,
        ProcessSupervisedTest.class,
        ProfileSPITest.class,
        MonitoringAPITest.class,
        SearchProcessInstanceTest.class,
        ReportingSQLValidityIT.class,
        PlatformMonitoringAPITest.class,
        TenantIT.class,
        BDRepositoryIT.class,
        org.bonitasoft.engine.business.data.BDRepositoryIT.class,
        ExecuteBDMQueryCommandIT.class
})
public class BPMSPTestsForServers {

    @BeforeClass
    public static void beforeAll() throws BonitaException {
        System.setProperty("delete.job.frequency", "0/30 * * * * ?");
    }

}
