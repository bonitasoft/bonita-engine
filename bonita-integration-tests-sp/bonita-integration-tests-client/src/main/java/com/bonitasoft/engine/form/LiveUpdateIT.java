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

import org.bonitasoft.engine.api.PageAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.form.FormMappingModelBuilder;
import org.bonitasoft.engine.bpm.process.ConfigurationState;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.form.FormMapping;
import org.bonitasoft.engine.form.FormMappingSearchDescriptor;
import org.bonitasoft.engine.form.FormMappingTarget;
import org.bonitasoft.engine.form.FormMappingType;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.page.AuthorizationRuleConstants;
import org.bonitasoft.engine.page.Page;
import org.bonitasoft.engine.page.PageURL;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.BPMTestSPUtil;
import com.bonitasoft.engine.CommonAPISPIT;
import com.bonitasoft.engine.api.ProcessAPI;
import com.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilderExt;
import com.bonitasoft.engine.log.Log;
import com.bonitasoft.engine.log.LogSearchDescriptor;

/**
 * @author Emmanuel Duchastenier
 * @author Baptiste Mesta
 */
public class LiveUpdateIT extends CommonAPISPIT {

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
                                .addProcessOverviewForm("process1OverviewForm", FormMappingTarget.URL).build());

        ProcessDefinition p = getProcessAPI().deploy(bar.done());
        getProcessAPI().addUserToActor("actor", p, user.getId());

        assertThat(getProcessAPI().getProcessDeploymentInfo(p.getId()).getConfigurationState()).as(
                "before the update of form mapping the process should be unresolved").isEqualTo(ConfigurationState.UNRESOLVED);
        assertThat(getProcessAPI().getProcessResolutionProblems(p.getId())).as("before the update of form mapping the process should be unresolved").hasSize(1);
        long afterDeploy = System.currentTimeMillis();
        ProcessAPI processConfigurationAPI = getProcessAPI();

        //get
        FormMapping step1Form = getFormMapping(p, processConfigurationAPI, "step1");
        FormMapping step2Form = getFormMapping(p, processConfigurationAPI, "step2");

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
        PageURL pInstantiation = getCustomPageAPI().resolvePageOrURL("process/ProcessWithUpdatedFormMappings/1.0", context, true);
        PageURL pOverview = getCustomPageAPI().resolvePageOrURL("processInstance/ProcessWithUpdatedFormMappings/1.0", context, true);
        PageURL pStep1Execution = getCustomPageAPI().resolvePageOrURL("taskInstance/ProcessWithUpdatedFormMappings/1.0/step1", context, true);
        PageURL pStep2Execution = getCustomPageAPI().resolvePageOrURL("taskInstance/ProcessWithUpdatedFormMappings/1.0/step2", context, true);
        assertThat(pInstantiation.getUrl()).isEqualTo("processStartForm?tenant=" + getSession().getTenantId());
        assertThat(pInstantiation.getPageId()).isNull();
        assertThat(pOverview.getPageId()).isNull();
        assertThat(pOverview.getUrl()).isEqualTo("process1OverviewForm?tenant=" + getSession().getTenantId());
        assertThat(pStep1Execution.getUrl()).isNull();
        assertThat(pStep1Execution.getPageId()).isNull(); // referenced page does not exist
        assertThat(pStep2Execution.getPageId()).isNull();
        assertThat(pStep2Execution.getUrl()).isEqualTo("http://newFormUrlForStep2?tenant=" + getSession().getTenantId());

        // deploy 2 pages

        final Page task1Form = getCustomPageAPI().createPage("myPage1.zip",
                createTestPageContent("custompage_task1Form", "Form of task1", "the form of task 1"));
        final Page anOtherForm = getCustomPageAPI().createPage("myPage2.zip",
                createTestPageContent("custompage_anOtherForm", "an other Form of task1", "the form of task 1"));
        processConfigurationAPI.updateFormMapping(step2Form.getId(), "http://newFormUrlForStep2", null);
        processConfigurationAPI.updateFormMapping(step1Form.getId(), null, task1Form.getId());
        // check process is resolved
        assertThat(getProcessAPI().getProcessResolutionProblems(p.getId())).as("the process should not have resolution problems").isEmpty();
        assertThat(getProcessAPI().getProcessDeploymentInfo(p.getId()).getConfigurationState()).isEqualTo(ConfigurationState.RESOLVED);
        // check if the page is deleted it's unresolved
        getCustomPageAPI().deletePage(task1Form.getId());
        assertThat(getProcessAPI().getProcessResolutionProblems(p.getId())).as("the process should be unresolved").hasSize(1);
        assertThat(getProcessAPI().getProcessDeploymentInfo(p.getId()).getConfigurationState()).isEqualTo(ConfigurationState.UNRESOLVED);
        // check if we update form mapping the process is resolved
        getProcessAPI().updateFormMapping(step1Form.getId(), null, anOtherForm.getId());
        assertThat(getProcessAPI().getProcessResolutionProblems(p.getId())).as("the process should not have resolution problems").isEmpty();
        assertThat(getProcessAPI().getProcessDeploymentInfo(p.getId()).getConfigurationState()).isEqualTo(ConfigurationState.RESOLVED);
        getProcessAPI()
                .updateFormMapping(
                        step1Form.getId(),
                        "a very very very very very very very very very very very very very very very very very very very very very very "
                                + "very very very very very very very very very very very very very very very very very very very very very very very very very very long url",
                        null);
        getProcessAPI()
                .updateFormMapping(
                        step1Form.getId(),
                        "a normal url",
                        null);

        getCustomPageAPI().deletePage(anOtherForm.getId());
        final SearchResult<Log> logSearchResult = getLogAPI().searchLogs(
                new SearchOptionsBuilder(0, 140).filter(LogSearchDescriptor.ACTION_TYPE, "FORM_MAPPING_UPDATED")
                        .filter(LogSearchDescriptor.ACTION_SCOPE, String.valueOf(p.getId())).done());
        assertThat(logSearchResult.getResult())
                .hasSize(6)
                .extracting("message")
                .contains(
                        "Previous: pageId=<null> urlAdapter=<external> url=<a very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very ve",
                        "Previous: pageId=<" + anOtherForm.getId() + "> urlAdapter=<null> url=<null>",
                        "Previous: pageId=<null> urlAdapter=<external> url=<http://newFormUrlForStep2>");
        assertThat(
                getLogAPI().searchLogs(
                        new SearchOptionsBuilder(0, 140).filter(LogSearchDescriptor.ACTION_TYPE, "FORM_MAPPING_CREATED")
                                .filter(LogSearchDescriptor.ACTION_SCOPE, String.valueOf(p.getId())).done()).getResult()).hasSize(4);

        getProcessAPI().deleteProcessDefinition(p.getId());
        assertThat(
                getLogAPI().searchLogs(
                        new SearchOptionsBuilder(0, 140).filter(LogSearchDescriptor.ACTION_TYPE, "FORM_MAPPING_DELETED")
                                .filter(LogSearchDescriptor.ACTION_SCOPE, String.valueOf(p.getId())).done()).getResult()).hasSize(4);

    }

    PageAPI getCustomPageAPI() throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return TenantAPIAccessor.getCustomPageAPI(getSession());
    }

    FormMapping getFormMapping(ProcessDefinition p, ProcessAPI processAPI, String step2) throws SearchException {
        return processAPI.searchFormMappings(new SearchOptionsBuilder(0, 10)
                .filter(FormMappingSearchDescriptor.TYPE, FormMappingType.TASK).filter(FormMappingSearchDescriptor.PROCESS_DEFINITION_ID, p.getId())
                .filter(FormMappingSearchDescriptor.TASK, step2).done()).getResult().get(0);
    }

    @Test
    public void updateExpressions_of_process() throws Exception {
        final User user = getIdentityAPI().createUser("userForLiveUpdate", "bpm");
        ProcessDefinitionBuilderExt processWithExpression = new ProcessDefinitionBuilderExt().createNewInstance("LiveUpdateProcess", "4.0");
        processWithExpression.addActor("theActor");
        final Expression groovyExpr = new ExpressionBuilder().createGroovyScriptExpression("groovyExpr", "return 'before update'", String.class.getName());
        processWithExpression.addUserTask("step1", "theActor").addDisplayName(
                groovyExpr);
        final Expression constantStringExpression = new ExpressionBuilder().createConstantStringExpression("the constant before update");
        processWithExpression.addUserTask("step2", "theActor").addDisplayName(
                constantStringExpression);
        ProcessDefinition processDefinition = getProcessAPI().deploy(processWithExpression.done());
        getProcessAPI().addUserToActor("theActor", processDefinition, user.getId());
        getProcessAPI().enableProcess(processDefinition.getId());

        //start new instance and check
        ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        HumanTaskInstance step1 = waitForUserTaskAndGetIt(processInstance, "step1");
        HumanTaskInstance step2 = waitForUserTaskAndGetIt(processInstance, "step2");
        assertThat(step1.getDisplayName()).as("display name of step1 before update").isEqualTo("before update");
        assertThat(step2.getDisplayName()).as("display name of step2 before update").isEqualTo("the constant before update");

        //update expressions
        final DesignProcessDefinition designProcessDefinition = getProcessAPI().getDesignProcessDefinition(processDefinition.getId());
        Expression step1DisplayName = designProcessDefinition.getFlowElementContainer().getFlowNode("step1").getDisplayName();
        Expression step2DisplayName = designProcessDefinition.getFlowElementContainer().getFlowNode("step2").getDisplayName();
        getProcessAPI().updateExpressionContent(processDefinition.getId(), step1DisplayName.getId(), "return 'groovy after update'");
        getProcessAPI().updateExpressionContent(processDefinition.getId(), step2DisplayName.getId(), "the constant after update");

        //start new instance after update of expression and recheck
        processInstance = getProcessAPI().startProcess(processDefinition.getId());
        step1 = waitForUserTaskAndGetIt(processInstance, "step1");
        step2 = waitForUserTaskAndGetIt(processInstance, "step2");
        assertThat(step1.getDisplayName()).as("display name of step1 before update").isEqualTo("groovy after update");
        assertThat(step2.getDisplayName()).as("display name of step2 before update").isEqualTo("the constant after update");

        assertThat(
                getLogAPI().searchLogs(
                        new SearchOptionsBuilder(0, 140).filter(LogSearchDescriptor.ACTION_TYPE, "PROCESS_DEFINITION_UPDATED")
                                .filter(LogSearchDescriptor.ACTION_SCOPE, String.valueOf(processDefinition.getId())).done()).getResult())
                .hasSize(3)
                .extracting("message")
                .containsOnly("Enabling the process", "Update expression <" + groovyExpr.getId() + ">, old content is <return 'before update'>",
                        "Update expression <" + constantStringExpression.getId() + ">, old content is <the constant before update>");

        disableAndDeleteProcess(processDefinition.getId());
        deleteUser(user);
    }

}
