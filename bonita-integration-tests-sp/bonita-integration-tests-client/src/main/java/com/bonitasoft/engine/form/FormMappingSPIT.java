/*
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 */
package com.bonitasoft.engine.form;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.form.FormMappingModelBuilder;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.form.FormMapping;
import org.bonitasoft.engine.form.FormMappingSearchDescriptor;
import org.bonitasoft.engine.form.FormMappingTarget;
import org.bonitasoft.engine.form.FormMappingType;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.page.AuthorizationRuleConstants;
import org.bonitasoft.engine.page.PageURL;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.BPMTestSPUtil;
import com.bonitasoft.engine.CommonAPISPIT;
import com.bonitasoft.engine.api.ProcessConfigurationAPI;

/**
 * author Emmanuel Duchastenier
 */
public class FormMappingSPIT extends CommonAPISPIT {

    protected User user;

    @Before
    public void before() throws Exception {
        user = BPMTestSPUtil.createUserOnDefaultTenant(USERNAME, PASSWORD);
        loginOnDefaultTenantWith(USERNAME, PASSWORD);
    }

    @After
    public void after() throws Exception {
        logoutOnTenant();
        BPMTestSPUtil.deleteUserOnDefaultTenant(user);
    }

    @Test
    public void deployProcessesWithFormMappingAndUpdateThem() throws Exception {
        ProcessDefinitionBuilder p1Builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithUpdatedFormMappings", "1.0");
        p1Builder.addUserTask("step1", "actor").addUserTask("step2", "actor");
        p1Builder.addActor("actor");
        BusinessArchiveBuilder bar = new BusinessArchiveBuilder()
                .createNewBusinessArchive()
                .setProcessDefinition(p1Builder.done())
                .setFormMappings(
                        FormMappingModelBuilder.buildFormMappingModel().addProcessStartForm("processStartForm", FormMappingTarget.URL)
                                .addTaskForm("task1Form", FormMappingTarget.INTERNAL, "step1")
                                .addProcessOverviewForm("process1OverviewForm", FormMappingTarget.INTERNAL).build());

        ProcessDefinition p = getProcessAPI().deploy(bar.done());

        long afterDeploy = System.currentTimeMillis();
        ProcessConfigurationAPI processConfigurationAPI = getProcessConfigurationAPI();

        //get
        FormMapping step2Form = processConfigurationAPI.searchFormMappings(new SearchOptionsBuilder(0, 10)
                .filter(FormMappingSearchDescriptor.TYPE, FormMappingType.TASK).filter(FormMappingSearchDescriptor.PROCESS_DEFINITION_ID, p.getId())
                .filter(FormMappingSearchDescriptor.TASK, "step2").done()).getResult().get(0);

        //update
        processConfigurationAPI.updateFormMapping(step2Form.getId(), "http://newFormUrlForStep2", null);

        FormMapping updatedStep2Form = processConfigurationAPI.getFormMapping(step2Form.getId());
        assertThat(updatedStep2Form).isEqualToIgnoringGivenFields(step2Form, "pageMappingKey", "pageURL", "target", "lastUpdateDate", "lastUpdatedBy");
        assertThat(updatedStep2Form.getPageMappingKey()).isEqualTo("taskInstance/ProcessWithUpdatedFormMappings/1.0/step2");
        assertThat(updatedStep2Form.getURL()).isEqualTo("http://newFormUrlForStep2");
        assertThat(updatedStep2Form.getTarget()).isEqualTo(FormMappingTarget.URL);
        assertThat(updatedStep2Form.getLastUpdateDate()).isAfter(new Date(afterDeploy));
        assertThat(updatedStep2Form.getLastUpdatedBy()).isEqualTo(user.getId());
        assertThat(step2Form.getLastUpdateDate()).isNull();

        //resolve urls:
        final Map<String, Serializable> context = Collections.singletonMap(AuthorizationRuleConstants.IS_ADMIN, (Serializable) true);
        PageURL pInstanciation = getProcessConfigurationAPI().resolvePageOrURL("process/ProcessWithUpdatedFormMappings/1.0", context);
        PageURL pOverview = getProcessConfigurationAPI().resolvePageOrURL("processInstance/ProcessWithUpdatedFormMappings/1.0", context);
        PageURL pStep1Execution = getProcessConfigurationAPI().resolvePageOrURL("taskInstance/ProcessWithUpdatedFormMappings/1.0/step1", context);
        PageURL pStep2Execution = getProcessConfigurationAPI().resolvePageOrURL("taskInstance/ProcessWithUpdatedFormMappings/1.0/step2", context);
        assertThat(pInstanciation.getUrl()).isEqualTo("processStartForm?tenant=" + getSession().getTenantId());
        assertThat(pInstanciation.getPageId()).isNull();
        assertThat(pOverview.getPageId()).isNull();
        assertThat(pOverview.getUrl()).isNull();
        assertThat(pStep1Execution.getUrl()).isNull();
        assertThat(pStep1Execution.getPageId()).isNull(); // referenced page does not exist
        assertThat(pStep2Execution.getPageId()).isNull();
        assertThat(pStep2Execution.getUrl()).isEqualTo("http://newFormUrlForStep2?tenant=" + getSession().getTenantId());

        getProcessAPI().deleteProcessDefinition(p.getId());
    }

}
