/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.reporting;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.core.process.definition.model.impl.SProcessDefinitionDeployInfoImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.SProcessInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.SUserTaskInstanceImpl;
import org.bonitasoft.engine.identity.model.impl.SUserImpl;
import org.bonitasoft.engine.persistence.TenantHibernatePersistenceService;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.services.PersistenceService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import bitronix.tm.BitronixTransactionManager;
import bitronix.tm.TransactionManagerServices;

import com.bonitasoft.engine.CommonAPISPIT;
import com.bonitasoft.engine.core.reporting.ReportingService;
import com.bonitasoft.engine.core.reporting.SReport;
import com.bonitasoft.engine.core.reporting.SReportBuilder;
import com.bonitasoft.engine.core.reporting.SReportBuilderFactory;
import com.bonitasoft.engine.core.reporting.SReportFields;
import com.bonitasoft.engine.service.impl.SpringTenantServiceAccessor;

public class TaskListIT extends CommonAPISPIT {

    private PersistenceService persistenceservice;

    private ReportingService reportingService;

    @Before
    public void setUp() {
        SpringTenantServiceAccessor accessor = new SpringTenantServiceAccessor(1L);
        persistenceservice = accessor.getBeanAccessor().getService(TenantHibernatePersistenceService.class);
        reportingService = accessor.getReportingService();
    }

    @After
    public void cleanUp() throws Exception {
        BitronixTransactionManager transactionManager = TransactionManagerServices.getTransactionManager();
        transactionManager.begin();
        persistenceservice.deleteByTenant(SProcessDefinitionDeployInfoImpl.class, null);
        persistenceservice.deleteByTenant(SProcessInstanceImpl.class, null);
        persistenceservice.deleteByTenant(SUserImpl.class, null);
        persistenceservice.deleteByTenant(SUserTaskInstanceImpl.class, null);
        transactionManager.commit();
    }

    @Test
    public void update_report() throws Exception {
        BitronixTransactionManager transactionManager = TransactionManagerServices.getTransactionManager();
        transactionManager.begin();
        final SReportBuilder reportBuilder = BuilderFactory.get(SReportBuilderFactory.class).createNewInstance("myReport", /* system user */-1, true,
                "my desc", new byte[] { 1, 2, 3 });
        reportingService.addReport(reportBuilder.done(), new byte[] { 4, 5, 6 });
        transactionManager.commit();
        long beforeUpdate = System.currentTimeMillis();
        Thread.sleep(200);
        transactionManager.begin();
        SReport myReport = reportingService.getReportByName("myReport");
        EntityUpdateDescriptor entityUpdateDescriptor = new EntityUpdateDescriptor();
        entityUpdateDescriptor.addField(SReportFields.DESCRIPTION, "new desc");
        entityUpdateDescriptor.addField(SReportFields.CONTENT, new byte[] { 8, 9 });
        entityUpdateDescriptor.addField(SReportFields.SCREENSHOT, new byte[] { 6, 7 });
        reportingService.update(myReport, entityUpdateDescriptor);
        transactionManager.commit();
        transactionManager.begin();
        SReport afterUpdateReport = reportingService.getReportByName("myReport");
        byte[] afterUpdateContent = reportingService.getReportContent(afterUpdateReport.getId());
        transactionManager.commit();
        assertThat(afterUpdateReport.getName()).isEqualTo("myReport");
        assertThat(afterUpdateReport.getDescription()).isEqualTo("new desc");
        assertThat(afterUpdateReport.getScreenshot()).as("screenshot").containsExactly(new byte[] { 6, 7 });
        assertThat(afterUpdateReport.getLastModificationDate()).as("last update date").isGreaterThan(beforeUpdate);
        assertThat(afterUpdateContent).as("Content").containsExactly(new byte[] { 8, 9 });
    }

}
