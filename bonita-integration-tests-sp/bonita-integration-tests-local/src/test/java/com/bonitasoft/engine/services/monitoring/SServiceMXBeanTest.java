/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.services.monitoring;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import com.bonitasoft.engine.CommonBPMServicesSPTest;
import com.bonitasoft.engine.monitoring.TenantMonitoringService;
import com.bonitasoft.engine.monitoring.mbean.SServiceMXBean;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.test.util.TestUtil;
import org.junit.Before;
import org.junit.Test;

public class SServiceMXBeanTest extends CommonBPMServicesSPTest {

    private SchedulerService schedulerService;

    private MBeanServer mbserver = null;

    private ObjectName entityMB;

    private ObjectName serviceMB;

    public SServiceMXBeanTest() {
        schedulerService = getTenantAccessor().getSchedulerService();
    }

    @Before
    public void disableMBeans() throws Exception {
        TestUtil.startScheduler(schedulerService);
        final ArrayList<MBeanServer> mbservers = MBeanServerFactory.findMBeanServer(null);
        if (mbservers.size() > 0) {
            mbserver = mbservers.get(0);
        }
        if (mbserver == null) {
            mbserver = MBeanServerFactory.createMBeanServer();
        }
        // Constructs the mbean names
        entityMB = new ObjectName(TenantMonitoringService.ENTITY_MBEAN_PREFIX + getDefaultTenantId());
        serviceMB = new ObjectName(TenantMonitoringService.SERVICE_MBEAN_PREFIX + getDefaultTenantId());

        unregisterMBeans();

    }

    public SServiceMXBean getServiceMXBean() {
        return getTenantAccessor().getTenantMonitoringService().getServiceBean();
    }

    /**
     * Assure that no Bonitasoft MBeans are registered in the MBServer before
     * each test.
     *
     * @throws javax.management.MBeanRegistrationException
     * @throws javax.management.InstanceNotFoundException
     */
    public void unregisterMBeans() throws MBeanRegistrationException, InstanceNotFoundException {
        if (mbserver.isRegistered(entityMB)) {
            mbserver.unregisterMBean(entityMB);
        }
        if (mbserver.isRegistered(serviceMB)) {
            mbserver.unregisterMBean(serviceMB);
        }
    }

    @Test
    public void getActiveTransactionTest() throws Exception {
        //given
        // start the ServiceMXBean
        final SServiceMXBean svcMB = getServiceMXBean();
        svcMB.start();
        final String numberOfActiveTransactions = "NumberOfActiveTransactions";

        //when
        //open transactions
        getTransactionService().begin();
        //get number of active transactions just after beginning a transaction
        Long activeTransitions = (Long) mbserver.getAttribute(serviceMB, numberOfActiveTransactions);

        //close transaction
        getTransactionService().complete();
        svcMB.stop();

        //then
        // check the transaction has been successfully counted
        // use a soft assertions because asynchronous code can also create transactions (like QuartzJobs)
        // so the number of active transactions can be greater than 1
        assertThat(activeTransitions).isGreaterThanOrEqualTo(1L);

    }

}
