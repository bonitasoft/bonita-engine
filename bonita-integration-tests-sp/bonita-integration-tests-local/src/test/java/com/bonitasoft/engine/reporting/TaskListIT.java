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

import java.io.IOException;
import java.util.Date;

import com.bonitasoft.engine.core.reporting.SReport;
import com.bonitasoft.engine.core.reporting.SReportBuilder;
import com.bonitasoft.engine.core.reporting.SReportBuilderFactory;
import com.bonitasoft.engine.core.reporting.SReportFields;
import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.ConfigurationState;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.core.process.definition.model.impl.SProcessDefinitionDeployInfoImpl;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.core.process.instance.model.impl.SProcessInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.SUserTaskInstanceImpl;
import org.bonitasoft.engine.identity.model.impl.SUserImpl;
import org.bonitasoft.engine.persistence.PersistentObject;
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
    public void should_retrieve_only_not_deleted_tasks() throws Exception {
        SUserImpl user = createUser();
        SProcessDefinitionDeployInfoImpl processDef = createApps();
        SProcessInstanceImpl processInstance = createInstance(processDef);
        SUserTaskInstanceImpl deletedTask = createDeletedUserTask(1L, "shouldNotBeRetrieved", processInstance, user);
        SUserTaskInstanceImpl expectedTask = createUserTask(2L, "shouldBeRetrieved", processInstance, user);
        insert(processDef, processInstance, user, deletedTask, expectedTask);

        String csv = executeQuery(getTaskListQuery());

        assertThat(csv).contains(expectedTask.getDisplayName());
        assertThat(csv).doesNotContain(deletedTask.getDisplayName());
    }

    @Test
    public void update_report() throws Exception {
        BitronixTransactionManager transactionManager = TransactionManagerServices.getTransactionManager();
        transactionManager.begin();
        final SReportBuilder reportBuilder = BuilderFactory.get(SReportBuilderFactory.class).createNewInstance("myReport", /* system user */-1, true,
                "my desc", new byte[]{1,2,3});
        reportingService.addReport(reportBuilder.done(),new byte[]{4,5,6});
        transactionManager.commit();
        long beforeUpdate = System.currentTimeMillis();
        Thread.sleep(200);
        transactionManager.begin();
        SReport myReport = reportingService.getReportByName("myReport");
        EntityUpdateDescriptor entityUpdateDescriptor = new EntityUpdateDescriptor();
        entityUpdateDescriptor.addField(SReportFields.DESCRIPTION,"new desc");
        entityUpdateDescriptor.addField(SReportFields.CONTENT,new byte[]{8,9});
        entityUpdateDescriptor.addField(SReportFields.SCREENSHOT,new byte[]{6,7});
        reportingService.update(myReport, entityUpdateDescriptor);
        transactionManager.commit();
        transactionManager.begin();
        SReport afterUpdateReport = reportingService.getReportByName("myReport");
        byte[] afterUpdateContent = reportingService.getReportContent(afterUpdateReport.getId());
        transactionManager.commit();
        assertThat(afterUpdateReport.getName()).isEqualTo("myReport");
        assertThat(afterUpdateReport.getDescription()).isEqualTo("new desc");
        assertThat(afterUpdateReport.getScreenshot()).as("screenshot").containsExactly(new byte[]{6,7});
        assertThat(afterUpdateReport.getLastModificationDate()).as("last update date").isGreaterThan(beforeUpdate);
        assertThat(afterUpdateContent).as("Content").containsExactly(new byte[]{8,9});
    }

    private String executeQuery(String query) throws Exception {
        BitronixTransactionManager transactionManager = TransactionManagerServices.getTransactionManager();
        transactionManager.begin();
        String csv = reportingService.selectList(query);
        transactionManager.commit();
        return csv;
    }

    private void insert(PersistentObject... persistentObject) throws Exception {
        BitronixTransactionManager transactionManager = TransactionManagerServices.getTransactionManager();
        transactionManager.begin();
        for (PersistentObject o : persistentObject) {
            persistenceservice.insert(o);
        }
        transactionManager.commit();
    }

    private String getTaskListQuery() throws IOException {
        String query = IOUtils.toString(TaskListIT.class.getResourceAsStream("TaskList.sql"));
        query = query.replace("$P{BONITA_TENANT_ID}", "1");
        query = query.replace("$P!{_p_state_name}", "");
        query = query.replace("$P{_p_date_from}", "1369173600470");
        query = query.replace("$P{_p_date_to}", String.valueOf(new Date().getTime()));
        query = query.replace("$P!{_p_apps_id}", "");
        return query;
    }

    private SUserTaskInstanceImpl createUserTask(long id, String displayName, SProcessInstanceImpl instance, SUserImpl user) {
        SUserTaskInstanceImpl tsk = new SUserTaskInstanceImpl("userTak", 1L, instance.getId(), 1L, 0, null, 0L, 0L);
        tsk.setId(id);
        tsk.setRootContainerId(instance.getId());
        tsk.setStateName("completed");
        tsk.setAssigneeId(user.getId());
        tsk.setReachedStateDate(new Date().getTime());
        tsk.setTenantId(1L);
        tsk.setDisplayName(displayName);
        tsk.setDeleted(false);
        return tsk;
    }

    private SUserTaskInstanceImpl createDeletedUserTask(long id, String displayName, SProcessInstanceImpl instance, SUserImpl user) {
        SUserTaskInstanceImpl tsk = createUserTask(id, displayName, instance, user);
        tsk.setDeleted(true);
        return tsk;
    }

    private SUserImpl createUser() {
        SUserImpl user = new SUserImpl();
        user.setId(1L);
        user.setFirstName("firstname");
        user.setLastName("lastName");
        user.setUserName("firstname.lastName");
        user.setTenantId(1L);
        return user;
    }

    private SProcessInstanceImpl createInstance(SProcessDefinitionDeployInfoImpl aps) {
        SProcessInstanceImpl cs = new SProcessInstanceImpl("instance", aps.getProcessId());
        cs.setId(1L);
        cs.setTenantId(1L);
        cs.setStateCategory(SStateCategory.NORMAL);
        return cs;
    }

    private SProcessDefinitionDeployInfoImpl createApps() {
        SProcessDefinitionDeployInfoImpl aps = new SProcessDefinitionDeployInfoImpl();
        aps.setProcessId(1L);
        aps.setName("processDefName");
        aps.setId(1L);
        aps.setVersion("1.0");
        aps.setActivationState(ActivationState.ENABLED.name());
        aps.setConfigurationState(ConfigurationState.RESOLVED.name());
        aps.setTenantId(1L);
        return aps;
    }
}
