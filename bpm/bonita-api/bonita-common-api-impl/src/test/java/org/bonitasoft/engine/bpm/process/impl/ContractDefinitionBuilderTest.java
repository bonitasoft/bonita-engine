package org.bonitasoft.engine.bpm.process.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.bpm.contract.InputDefinition;
import org.bonitasoft.engine.bpm.contract.Type;
import org.bonitasoft.engine.bpm.contract.impl.ContractDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.FlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.UserTaskDefinitionImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ContractDefinitionBuilderTest {

    private static final String name = "name";

    private static final Type type = Type.TEXT;

    private static final String description = "description";

    private static final String actorName = "actor name";

    private static final String taskName = "task name";

    private static final String version = "1.0";

    @Mock
    ContractDefinitionImpl contractDefinitionImpl;

    @Mock
    private ProcessDefinitionBuilder processDefinitionBuilder;

    @Mock
    private FlowElementContainerDefinitionImpl container;

    private List<InputDefinition> inputs;

    private UserTaskDefinitionImpl activity;

    private ContractDefinitionBuilder contractDefinitionBuilder;

    @Before
    public void before() throws Exception {
        activity = new UserTaskDefinitionImpl(name, actorName);
        contractDefinitionBuilder = new ContractDefinitionBuilder(processDefinitionBuilder, container, activity);

    }

    @Test
    public void addInputTest() throws Exception {
        //when
        contractDefinitionBuilder.addInput(name, type, description);

        //then
        assertThat(activity.getContract().getInputs()).hasSize(1);
    }

    @Test
    public void addComplexInputTest() throws Exception {
        //when
        contractDefinitionBuilder.addComplexInput(name, description, new ArrayList<InputDefinition>());

        //then
        assertThat(activity.getContract().getInputs()).hasSize(1);
    }

    @Test
    public void addRuleTest() throws Exception {
        //when
        contractDefinitionBuilder.addRule(name, "expression", "explanation", "input name");

        //then
        assertThat(activity.getContract().getRules()).hasSize(1);

    }

}
