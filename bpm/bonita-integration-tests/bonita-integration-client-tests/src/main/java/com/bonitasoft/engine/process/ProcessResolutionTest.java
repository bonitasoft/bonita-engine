package com.bonitasoft.engine.process;

import java.util.List;

import org.bonitasoft.engine.CommonAPISPTest;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.model.ActorInstance;
import org.bonitasoft.engine.bpm.model.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.model.Problem;
import org.bonitasoft.engine.bpm.model.ProcessDefinition;
import org.bonitasoft.engine.bpm.model.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.model.ProcessDeploymentInfo;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.process.TestStates;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.bonitasoft.engine.bpm.model.ParameterDefinition;

public class ProcessResolutionTest extends CommonAPISPTest {

    @After
    public void afterTest() throws BonitaException {
        logout();
    }

    @Before
    public void beforeTest() throws BonitaException {
        login();
    }

    @Cover(classes = { Problem.class, ProcessDefinition.class, ParameterDefinition.class }, concept = BPMNConcept.PROCESS, jira = "ENGINE-531", keywords = { "process resolution" })
    @Test
    @Ignore("ENGINE-454")
    public void parameterUnset() throws BonitaException {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder();
        builder.createNewInstance("resolve", "1.0").addParameter("param1", String.class.getName()).addAutomaticTask("step1");
        final DesignProcessDefinition processDefinition = builder.done();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(processDefinition).done();
        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive);
        final ProcessDeploymentInfo deploymentInfo = getProcessAPI().getProcessDeploymentInfo(definition.getId());
        Assert.assertEquals(TestStates.getProcessDepInfoUnresolvedState(), deploymentInfo.getConfigurationState());

        final List<Problem> problems = getProcessAPI().getProcessResolutionProblems(definition.getId());
        Assert.assertEquals(1, problems.size());
        final Problem problem = problems.get(0);
        Assert.assertEquals(Problem.Level.ERROR, problem.getLevel());
        Assert.assertEquals("parameter", problem.getResource());
        Assert.assertNotNull(problem.getDescription());

        deleteProcess(definition.getId());
    }

    @Cover(classes = { Problem.class, ProcessDefinition.class, ParameterDefinition.class }, concept = BPMNConcept.PROCESS, jira = "ENGINE-531", keywords = { "process resolution" })
    @Test
    @Ignore("ENGINE-454")
    public void resolveParameter() throws BonitaException {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder();
        builder.createNewInstance("resolve", "1.0").addParameter("param1", String.class.getName()).addAutomaticTask("step1");
        final DesignProcessDefinition processDefinition = builder.done();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(processDefinition).done();
        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive);
        final ProcessDeploymentInfo deploymentInfo = getProcessAPI().getProcessDeploymentInfo(definition.getId());
        Assert.assertEquals(TestStates.getProcessDepInfoUnresolvedState(), deploymentInfo.getConfigurationState());

        getProcessAPI().updateParameterInstanceValue(definition.getId(), "param1", "value");
        final ActorInstance initiator = getProcessAPI().getActorInitiator(definition.getId());
        getProcessAPI().addUserToActor(initiator.getId(), getSession().getUserId());
        final List<Problem> problems = getProcessAPI().getProcessResolutionProblems(definition.getId());
        Assert.assertEquals(0, problems.size());

        deleteProcess(definition.getId());
    }

}
