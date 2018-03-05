/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.execution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.model.SGatewayType;
import org.bonitasoft.engine.core.process.definition.model.SType;
import org.bonitasoft.engine.core.process.definition.model.event.impl.SBoundaryEventDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SContractDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SFlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SGatewayDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SInputDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SProcessDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SSubProcessDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SUserTaskDefinitionImpl;
import org.bonitasoft.engine.expression.ExpressionService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Elias Ricken de Medeiros
 */
@RunWith(MockitoJUnitRunner.class)
public class AdvancedStartProcessValidatorTest {

    private static final String GATEWAY_NAME = "gateway";

    private static final String SUBPROCESS_NAME = "subprocess";

    private static final String BOUNDARY_NAME = "boundary";

    private static final String USERTASK_NAME = "usertask";

    private final long PROCESS_DEFINITION_ID = 1234L;
    private final long PROCESS_DEFINITION_WITH_CONTRACT_INPUT_ID = 6543654223L;

    @Mock
    private ProcessDefinitionService processDefinitionService;
    @Mock
    private TechnicalLoggerService technicalLoggerService;
    @Mock
    private ExpressionService expressionService;

    @Before
    public void setUp() throws Exception {
        //process with all kind of elements
        createCompleteProcess();

        //process with contract inputs
        createProcessWithContract();

    }

    private void createProcessWithContract() throws SProcessDefinitionNotFoundException, SBonitaReadException {
        SProcessDefinitionImpl processDefinition = new SProcessDefinitionImpl("My process1", "1.0");
        SFlowElementContainerDefinitionImpl container = new SFlowElementContainerDefinitionImpl();
        processDefinition.setProcessContainer(container);
        SGatewayDefinitionImpl gateway = new SGatewayDefinitionImpl(6546543L, GATEWAY_NAME, SGatewayType.EXCLUSIVE);
        SSubProcessDefinitionImpl subProcess = new SSubProcessDefinitionImpl(64236543L, SUBPROCESS_NAME, true);
        SBoundaryEventDefinitionImpl boundary = new SBoundaryEventDefinitionImpl(67523L, BOUNDARY_NAME);
        SUserTaskDefinitionImpl userTask = new SUserTaskDefinitionImpl(23425L, USERTASK_NAME, "actor");
        container.addGateway(gateway);
        container.addSubProcess(subProcess);
        container.addActivity(userTask);
        userTask.addBoundaryEventDefinition(boundary);
        doReturn(processDefinition).when(processDefinitionService).getProcessDefinition(PROCESS_DEFINITION_ID);
    }

    private void createCompleteProcess() throws SProcessDefinitionNotFoundException, SBonitaReadException {
        SProcessDefinitionImpl procDef = new SProcessDefinitionImpl("My process2", "1.0");
        SFlowElementContainerDefinitionImpl container = new SFlowElementContainerDefinitionImpl();
        procDef.setProcessContainer(container);
        procDef.setContract(ContractBuilder.contract().input("input1", SType.TEXT).build());
        SUserTaskDefinitionImpl userTask1 = new SUserTaskDefinitionImpl(23425L, "userTask1WithContract", "actor");
        userTask1.setContract(ContractBuilder.contract().input("u1Input", SType.TEXT).build());
        SUserTaskDefinitionImpl userTask2 = new SUserTaskDefinitionImpl(23425L, "userTask2WithContract", "actor");
        userTask2.setContract(ContractBuilder.contract().input("u2Input", SType.TEXT).build());
        SUserTaskDefinitionImpl userTask3 = new SUserTaskDefinitionImpl(23425L, "userTask3WithNoContract", "actor");
        SUserTaskDefinitionImpl userTask4 = new SUserTaskDefinitionImpl(23425L, "userTask4WithMultipleContract", "actor");
        userTask4.setContract(ContractBuilder.contract().input("u4Input1", SType.TEXT).build());
        userTask4.setContract(ContractBuilder.contract().input("u4Input2", SType.INTEGER).build());
        container.addActivity(userTask1);
        container.addActivity(userTask2);
        container.addActivity(userTask3);
        container.addActivity(userTask4);
        doReturn(procDef).when(processDefinitionService).getProcessDefinition(PROCESS_DEFINITION_WITH_CONTRACT_INPUT_ID);
    }

    private AdvancedStartProcessValidator createValidatorFor(long process_definition_id) {
        return new AdvancedStartProcessValidator(processDefinitionService, process_definition_id, technicalLoggerService, expressionService);
    }

    @Test
    public void validate_return_problem_if_one_of_chosen_flownode_is_a_boundary() throws Exception {
        AdvancedStartProcessValidator starterValidator = createValidatorFor(PROCESS_DEFINITION_ID);

        List<String> problems = starterValidator.validate(Collections.singletonList(BOUNDARY_NAME), null);
        assertEquals(1, problems.size());
        assertTrue(problems.get(0).contains(BOUNDARY_NAME));
    }

    @Test
    public void validate_return_problem_if_one_of_chosen_flownode_is_a_subprocess() throws Exception {
        AdvancedStartProcessValidator starterValidator = createValidatorFor(PROCESS_DEFINITION_ID);
        List<String> problems = starterValidator.validate(Collections.singletonList(SUBPROCESS_NAME), null);
        assertEquals(1, problems.size());
        assertTrue(problems.get(0).contains(SUBPROCESS_NAME));
    }

    @Test
    public void validate_return_problem_if_one_of_chosen_flownode_is_a_gateway() throws Exception {
        AdvancedStartProcessValidator starterValidator = createValidatorFor(PROCESS_DEFINITION_ID);
        List<String> problems = starterValidator.validate(Collections.singletonList(GATEWAY_NAME), null);
        assertEquals(1, problems.size());
        assertTrue(problems.get(0).contains(GATEWAY_NAME));
    }

    @Test
    public void validate_return_empty_list_if_is_valid() throws Exception {
        AdvancedStartProcessValidator starterValidator = createValidatorFor(PROCESS_DEFINITION_ID);
        List<String> problems = starterValidator.validate(Collections.singletonList(USERTASK_NAME), null);
        assertEquals(0, problems.size());
    }

    @Test
    public void validate_return_problem_if_one_element_is_not_found() throws Exception {
        AdvancedStartProcessValidator starterValidator = createValidatorFor(PROCESS_DEFINITION_ID);
        List<String> problems = starterValidator.validate(Collections.singletonList("unknowntask"), null);
        assertEquals(1, problems.size());
        assertTrue(problems.get(0).contains("unknowntask"));
    }

    @Test
    public void validate_return_problem_if_the_flowNode_name_list_is_empty() throws Exception {
        AdvancedStartProcessValidator starterValidator = createValidatorFor(PROCESS_DEFINITION_ID);
        List<String> problems = starterValidator.validate(Collections.<String> emptyList(), null);
        assertEquals(1, problems.size());
        assertTrue(problems.get(0).contains("empty"));
    }

    @Test
    public void should_detect_no_problem_when_process_contract_input_are_present_and_required() throws Exception {
        //given
        AdvancedStartProcessValidator starterValidator = createValidatorFor(PROCESS_DEFINITION_WITH_CONTRACT_INPUT_ID);
        //when
        List<String> problems = starterValidator.validate(Collections.singletonList("userTask3WithNoContract"),
                inputs("input1"));
        //then
        assertThat(problems).isEmpty();
    }

    private Map<String, Serializable> inputs(String input1) {
        return Collections.singletonMap(input1, (Serializable) "value");
    }

    @Test
    public void should_detect_problem_when_when_process_contract_input_are_required_and_not_given() throws Exception {
        //given
        AdvancedStartProcessValidator starterValidator = createValidatorFor(PROCESS_DEFINITION_WITH_CONTRACT_INPUT_ID);
        //when
        List<String> problems = starterValidator.validate(Collections.singletonList("userTask3WithNoContract"), null);
        //then
        assertThat(problems).containsOnly("Expected input [input1] is missing on My process2");
    }

    @Test
    public void should_detect_problem_when_when_process_contract_input_are_required_and_not_all_given() throws Exception {
        //given
        AdvancedStartProcessValidator starterValidator = createValidatorFor(PROCESS_DEFINITION_WITH_CONTRACT_INPUT_ID);
        //when
        List<String> problems = starterValidator.validate(Collections.singletonList("userTask3WithNoContract"),
                inputs("unknown"));
        //then
        assertThat(problems).containsOnly("Expected input [input1] is missing on My process2");
    }

    private static class ContractBuilder {

        private SContractDefinitionImpl sContractDefinition = new SContractDefinitionImpl();

        static ContractBuilder contract() {
            return new ContractBuilder();
        }

        public SContractDefinitionImpl build() {
            return sContractDefinition;
        }

        public ContractBuilder input(String name, SType type) {
            sContractDefinition.addInput(new SInputDefinitionImpl(name, type, ""));
            return this;
        }
    }
}
