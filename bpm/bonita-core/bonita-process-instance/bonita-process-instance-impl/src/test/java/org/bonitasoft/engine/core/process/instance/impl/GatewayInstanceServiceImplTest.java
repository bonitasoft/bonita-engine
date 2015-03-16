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
package org.bonitasoft.engine.core.process.instance.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeDefinition;
import org.bonitasoft.engine.core.process.definition.model.SGatewayType;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.STransitionDefinition;
import org.bonitasoft.engine.core.process.definition.model.impl.SFlowNodeDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SProcessDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.STransitionDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SUserTaskDefinitionImpl;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SGatewayInstance;
import org.bonitasoft.engine.core.process.instance.model.impl.SGatewayInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.SUserTaskInstanceImpl;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.Recorder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GatewayInstanceServiceImplTest {

    private static final long PROCESS_INSTANCE_ID = 12l;
    @Mock
    private Recorder recorder;
    @Mock
    private EventService eventService;
    @Mock
    private ReadPersistenceService persistenceRead;
    @Mock
    private TechnicalLoggerService logger;
    @Mock
    private FlowNodeInstanceService flowNodeInstanceService;
    @Mock
    private SFlowElementContainerDefinition processContainer;
    @InjectMocks
    @Spy
    private GatewayInstanceServiceImpl gatewayInstanceService;

    @Test
    public void should_extractElementThatAreSourceAndTarget_modify_the_lists() {
        SFlowNodeDefinition step1 = node(1, "step1");
        SFlowNodeDefinition step2 = node(2, "step2");
        SFlowNodeDefinition step3 = node(3, "step3");

        List<SFlowNodeDefinition> sourceElements = new ArrayList<SFlowNodeDefinition>(Arrays.asList(step1, step2));
        List<SFlowNodeDefinition> targetElements = new ArrayList<SFlowNodeDefinition>(Arrays.asList(step2, step3));
        List<SFlowNodeDefinition> sourceAndTarget = gatewayInstanceService.extractElementThatAreSourceAndTarget(sourceElements, targetElements);

        assertThat(sourceElements).isEmpty();
        assertThat(targetElements).containsOnly(step3);
        assertThat(sourceAndTarget).containsOnly(step2, step1);//step1 because it's also a start element
    }

    SFlowNodeDefinition node(long id, String name) {
        SUserTaskDefinitionImpl definition = new SUserTaskDefinitionImpl(id, name, "actor");
        doReturn(definition).when(processContainer).getFlowNode(id);
        return definition;
    }

    @Test
    public void should_containsToken_for_source_element_with_one_token_return_true() throws Exception {
        instanceInDatabase("step1", PROCESS_INSTANCE_ID, false);
        instanceInDatabase("step2", PROCESS_INSTANCE_ID, true);

        boolean containsToken = gatewayInstanceService.containsToken(PROCESS_INSTANCE_ID, flowNodeDefList("step0", "step1", "step2"), true);

        assertThat(containsToken).isTrue();
    }

    @Test
    public void should_containsToken_for_source_element_with_no_token_return_false() throws Exception {
        instanceInDatabase("step1", PROCESS_INSTANCE_ID, false);
        instanceInDatabase("step2", PROCESS_INSTANCE_ID, false);

        boolean containsToken = gatewayInstanceService.containsToken(PROCESS_INSTANCE_ID, flowNodeDefList("step0", "step1", "step2"), true);

        assertThat(containsToken).isFalse();
    }

    @Test
    public void should_containsToken_for_target_element_with_token_return_true() throws Exception {
        instanceInDatabase("step1", PROCESS_INSTANCE_ID, true);
        instanceInDatabase("step2", PROCESS_INSTANCE_ID, false);

        boolean containsToken = gatewayInstanceService.containsToken(PROCESS_INSTANCE_ID, flowNodeDefList("step0", "step1", "step2"), false);

        assertThat(containsToken).isTrue();
    }

    @Test
    public void should_containsToken_for_target_element_with_no_token_return_false() throws Exception {
        instanceInDatabase("step1", PROCESS_INSTANCE_ID, true);
        instanceInDatabase("step2", PROCESS_INSTANCE_ID, true);

        boolean containsToken = gatewayInstanceService.containsToken(PROCESS_INSTANCE_ID, flowNodeDefList("step0", "step1", "step2"), false);

        assertThat(containsToken).isFalse();
    }

    @Test
    public void should_containsToken_for_both_element_with_token_return_true1() throws Exception {
        instanceInDatabase("step2", PROCESS_INSTANCE_ID, true);

        boolean containsToken = gatewayInstanceService.containsToken(PROCESS_INSTANCE_ID, flowNodeDefList("step0", "step1", "step2"), null);

        assertThat(containsToken).isTrue();
    }

    @Test
    public void should_containsToken_for_both_element_with_token_return_true2() throws Exception {
        instanceInDatabase("step2", PROCESS_INSTANCE_ID, false);

        boolean containsToken = gatewayInstanceService.containsToken(PROCESS_INSTANCE_ID, flowNodeDefList("step0", "step1", "step2"), null);

        assertThat(containsToken).isTrue();
    }

    @Test
    public void should_containsToken_for_both_element_with_no_token_return_false() throws Exception {

        boolean containsToken = gatewayInstanceService.containsToken(PROCESS_INSTANCE_ID, flowNodeDefList("step0", "step1", "step2"), null);

        assertThat(containsToken).isFalse();
    }

    List<SFlowNodeDefinition> flowNodeDefList(String... names) {
        ArrayList<SFlowNodeDefinition> list = new ArrayList<SFlowNodeDefinition>();
        for (String name : names) {
            list.add(node(1, name));
        }
        return list;
    }

    private void instanceInDatabase(String name, long processInstanceId, boolean terminal) throws Exception {
        List<FilterOption> filters = new ArrayList<FilterOption>();
        filters.add(new FilterOption(SFlowNodeInstance.class, "name", name));
        filters.add(new FilterOption(SFlowNodeInstance.class, "parentContainerId", processInstanceId));
        QueryOptions searchOptions = new QueryOptions(0, 20, Collections.<OrderByOption>emptyList(), filters, null);
        SUserTaskInstanceImpl sUserTaskInstance = new SUserTaskInstanceImpl();
        sUserTaskInstance.setName(name);
        sUserTaskInstance.setTerminal(terminal);
        doReturn(Arrays.asList(sUserTaskInstance)).when(flowNodeInstanceService).searchFlowNodeInstances(
                SFlowNodeInstance.class,
                searchOptions);

    }

    @Test
    public void should_transitionsContainsAToken_calls_containsToken_with_middle_node_terminal() throws Exception {
        SFlowNodeDefinition gate = node(666, "gate");
        node(1, "step1");
        node(2, "step2");
        node(3, "step3");
        instanceInDatabase("step2", PROCESS_INSTANCE_ID, true);

        boolean containsAToken = gatewayInstanceService.transitionsContainsAToken(Arrays.asList(transition(1, 2), transition(2, 3)), gate, PROCESS_INSTANCE_ID,
                processContainer);

        assertThat(containsAToken).isTrue();
    }

    @Test
    public void should_transitionsContainsAToken_calls_containsToken_with_middle_node_not_terminal() throws Exception {
        SFlowNodeDefinition gate = node(666, "gate");
        node(1, "step1");
        node(2, "step2");
        node(3, "step3");
        instanceInDatabase("step2", PROCESS_INSTANCE_ID, false);

        boolean containsAToken = gatewayInstanceService.transitionsContainsAToken(Arrays.asList(transition(1, 2), transition(2, 3)), gate, PROCESS_INSTANCE_ID,
                processContainer);

        assertThat(containsAToken).isTrue();
    }

    @Test
    public void should_transitionsContainsAToken_calls_containsToken_with_source_node_true() throws Exception {
        SFlowNodeDefinition gate = node(666, "gate");
        node(1, "step1");
        node(2, "step2");
        node(3, "step3");
        instanceInDatabase("step1", PROCESS_INSTANCE_ID, true);

        boolean containsAToken = gatewayInstanceService.transitionsContainsAToken(Arrays.asList(transition(1, 2), transition(2, 3)), gate, PROCESS_INSTANCE_ID,
                processContainer);

        assertThat(containsAToken).isTrue();
    }

    @Test
    public void should_transitionsContainsAToken_calls_containsToken_with_source_node_false() throws Exception {
        SFlowNodeDefinition gate = node(666, "gate");
        node(0, "step0");
        node(1, "step1");
        node(2, "step2");
        node(3, "step3");
        transition(0, 1);
        transition(1, 2);
        transition(2, 3);
        instanceInDatabase("step1", PROCESS_INSTANCE_ID, false);

        boolean containsAToken = gatewayInstanceService.transitionsContainsAToken(Arrays.asList(transition(1, 2), transition(2, 3)), gate, PROCESS_INSTANCE_ID,
                processContainer);

        assertThat(containsAToken).isFalse();
    }

    @Test
    public void should_transitionsContainsAToken_calls_containsToken_with_target_node_true() throws Exception {
        SFlowNodeDefinition gate = node(666, "gate");
        node(1, "step1");
        node(2, "step2");
        node(3, "step3");
        instanceInDatabase("step3", PROCESS_INSTANCE_ID, false);

        boolean containsAToken = gatewayInstanceService.transitionsContainsAToken(Arrays.asList(transition(1, 2), transition(2, 3)), gate, PROCESS_INSTANCE_ID,
                processContainer);

        assertThat(containsAToken).isTrue();
    }

    @Test
    public void should_transitionsContainsAToken_calls_containsToken_with_target_node_false() throws Exception {
        SFlowNodeDefinition gate = node(666, "gate");
        node(1, "step1");
        node(2, "step2");
        node(3, "step3");
        instanceInDatabase("step3", PROCESS_INSTANCE_ID, true);

        boolean containsAToken = gatewayInstanceService.transitionsContainsAToken(Arrays.asList(transition(1, 2), transition(2, 3)), gate, PROCESS_INSTANCE_ID,
                processContainer);

        assertThat(containsAToken).isFalse();
    }

    @Test
    public void should_transitionsContainsAToken_calls_containsToken_with_no_node() throws Exception {
        SFlowNodeDefinition gate = node(666, "gate");
        node(1, "step1");
        node(2, "step2");
        node(3, "step3");

        boolean containsAToken = gatewayInstanceService.transitionsContainsAToken(Arrays.asList(transition(1, 2), transition(2, 3)), gate, PROCESS_INSTANCE_ID,
                processContainer);

        assertThat(containsAToken).isFalse();
    }

    @Test
    public void should_addBackwardReachableTransitions_complete_list() {
        SFlowNodeDefinition gate = node(666, "gate");
        node(1, "stepa1");
        node(2, "stepa2");
        transition(1, 2);
        node(3, "stepa3");
        transition(2, 3);
        node(4, "stepb4");
        node(5, "stepb5");
        node(6, "stepb6");
        transition(5, 6);
        transition(4, 6);
        List<STransitionDefinition> startTransition = Arrays.asList(transition(6, 666), transition(3, 666));
        List<STransitionDefinition> toComplete = new ArrayList<STransitionDefinition>();
        gatewayInstanceService.addBackwardReachableTransitions(processContainer, gate, startTransition, toComplete,
                Collections.<STransitionDefinition>emptyList());

        assertThat(toComplete).containsOnly(transition(1, 2), transition(2, 3), transition(4, 6), transition(5, 6), transition(3, 666), transition(6, 666));
    }

    @Test
    public void should_addBackwardReachableTransitions_with_gateway_having_a_loop_on_itself() {
        SFlowNodeDefinition gate = node(666, "gate");
        node(1, "step1");
        node(2, "step2");
        transition(1, 666);
        transition(666, 1);
        transition(2,666);
        List<STransitionDefinition> startTransition = Arrays.asList(transition(1, 666));
        List<STransitionDefinition> toComplete = new ArrayList<STransitionDefinition>();
        gatewayInstanceService.addBackwardReachableTransitions(processContainer, gate, startTransition, toComplete,
                Collections.<STransitionDefinition>emptyList());

        assertThat(toComplete).containsOnly(transition(1, 666), transition(666, 1));
    }

    private STransitionDefinition transition(long source, long target) {
        STransitionDefinitionImpl transition = new STransitionDefinitionImpl("name", source, target);
        ((SFlowNodeDefinitionImpl) processContainer.getFlowNode(target)).addIncomingTransition(transition);
        ((SFlowNodeDefinitionImpl) processContainer.getFlowNode(source)).addOutgoingTransition(transition);
        return transition;
    }

    @Test
    public void should_checkMergingCondition_on_inclusive() throws Exception {
        doReturn(true).when(gatewayInstanceService).isInclusiveGatewayActivated(any(SProcessDefinition.class), any(SGatewayInstance.class));
        SProcessDefinitionImpl processDefinition = new SProcessDefinitionImpl("P", "1.0");
        SGatewayInstanceImpl gate = new SGatewayInstanceImpl();
        gate.setGatewayType(SGatewayType.INCLUSIVE);

        boolean mergingCondition = gatewayInstanceService.checkMergingCondition(processDefinition, gate);

        verify(gatewayInstanceService).isInclusiveGatewayActivated(processDefinition, gate);
        assertThat(mergingCondition).isTrue();
    }

    @Test
    public void should_checkMergingCondition_on_parallel() throws Exception {
        doReturn(true).when(gatewayInstanceService).isParallelGatewayActivated(any(SProcessDefinition.class), any(SGatewayInstance.class));
        SProcessDefinitionImpl processDefinition = new SProcessDefinitionImpl("P", "1.0");
        SGatewayInstanceImpl gate = new SGatewayInstanceImpl();
        gate.setGatewayType(SGatewayType.PARALLEL);

        boolean mergingCondition = gatewayInstanceService.checkMergingCondition(processDefinition, gate);

        verify(gatewayInstanceService).isParallelGatewayActivated(processDefinition, gate);
        assertThat(mergingCondition).isTrue();
    }

    @Test
    public void should_checkMergingCondition_on_exclusive() throws Exception {
        SProcessDefinitionImpl processDefinition = new SProcessDefinitionImpl("P", "1.0");
        SGatewayInstanceImpl gate = new SGatewayInstanceImpl();
        gate.setGatewayType(SGatewayType.EXCLUSIVE);

        boolean mergingCondition = gatewayInstanceService.checkMergingCondition(processDefinition, gate);

        assertThat(mergingCondition).isTrue();
    }

    @Test
    public void should_getMergedTokens_on_exclusive_return_the_first_token() throws Exception {
        SGatewayInstanceImpl gate = new SGatewayInstanceImpl();
        gate.setGatewayType(SGatewayType.EXCLUSIVE);

        List<String> mergedTokens = gatewayInstanceService.getMergedTokens(gate, Arrays.asList("1", "2"));

        assertThat(mergedTokens).containsOnly("1");
    }

    @Test
    public void should_getMergedTokens_on_parallel() throws Exception {
        SGatewayInstanceImpl gate = new SGatewayInstanceImpl();
        gate.setGatewayType(SGatewayType.PARALLEL);

        List<String> mergedTokens = gatewayInstanceService.getMergedTokens(gate, Arrays.asList("1", "2", "3", "2"));

        assertThat(mergedTokens).containsOnly("1", "2", "3");
    }

    @Test
    public void should_getMergedTokens_on_inclusive() throws Exception {
        SGatewayInstanceImpl gate = new SGatewayInstanceImpl();
        gate.setGatewayType(SGatewayType.INCLUSIVE);

        List<String> mergedTokens = gatewayInstanceService.getMergedTokens(gate, Arrays.asList("1", "2", "3", "2"));

        assertThat(mergedTokens).containsOnly("1", "2", "3");
    }

    @Test
    public void should_getRemainingToken_return_not_merged_tokens() {
        List<String> remainingTokens = gatewayInstanceService.getRemainingTokens(Arrays.asList("1", "2", "3", "2", "1"), Arrays.asList("1", "2", "3"));

        assertThat(remainingTokens).containsOnly("2", "1");
    }

    @Test
    public void should_parallelBehavior_merged() {
        node(666, "gate");
        node(1, "step1");
        node(2, "step2");
        node(3, "step3");
        SProcessDefinitionImpl processDefinition = new SProcessDefinitionImpl("P", "1.0");
        processDefinition.setProcessContainer(processContainer);
        SGatewayInstanceImpl gate = new SGatewayInstanceImpl();
        gate.setName("gate");
        gate.setHitBys("1,2,3,2");
        gate.setFlowNodeDefinitionId(666);
        node(666, "gate");
        transition(1, 666);
        transition(2, 666);
        transition(3, 666);

        boolean isMerged = gatewayInstanceService.isParallelGatewayActivated(processDefinition, gate);

        assertThat(isMerged).isTrue();
    }

    @Test
    public void should_parallelBehavior_not_merged() {
        node(666, "gate");
        node(1, "step1");
        node(2, "step2");
        node(3, "step3");
        SProcessDefinitionImpl processDefinition = new SProcessDefinitionImpl("P", "1.0");
        processDefinition.setProcessContainer(processContainer);
        SGatewayInstanceImpl gate = new SGatewayInstanceImpl();
        gate.setName("gate");
        gate.setHitBys("2,3,2");
        gate.setFlowNodeDefinitionId(666);
        node(666, "gate");
        transition(1, 666);
        transition(2, 666);
        transition(3, 666);

        boolean isMerged = gatewayInstanceService.isParallelGatewayActivated(processDefinition, gate);

        assertThat(isMerged).isFalse();
    }


    @Test
    public void should_inclusiveBehavior_merged() throws SBonitaReadException {
        node(666, "gate");
        node(1, "step1");
        node(2, "step2");
        node(3, "step3");
        SProcessDefinitionImpl processDefinition = new SProcessDefinitionImpl("P", "1.0");
        processDefinition.setProcessContainer(processContainer);
        SGatewayInstanceImpl gate = new SGatewayInstanceImpl();
        gate.setName("gate");
        gate.setHitBys("1,2,3,2");
        gate.setFlowNodeDefinitionId(666);
        gate.setParentContainerId(PROCESS_INSTANCE_ID);
        SFlowNodeDefinition nodeDefinition = node(666, "gate");
        transition(1, 666);
        transition(2, 666);
        transition(3, 666);
        doNothing().when(gatewayInstanceService).addBackwardReachableTransitions(any(SFlowElementContainerDefinition.class), any(SFlowNodeDefinition.class), anyListOf(STransitionDefinition.class), anyListOf(STransitionDefinition.class), anyListOf(STransitionDefinition.class));
        doReturn(true).when(gatewayInstanceService).transitionsContainsAToken(anyListOf(STransitionDefinition.class), any(SFlowNodeDefinition.class), anyLong(), any(SFlowElementContainerDefinition.class));

        boolean isMerged = gatewayInstanceService.isInclusiveGatewayActivated(processDefinition, gate);

        assertThat(isMerged).isFalse();
        verify(gatewayInstanceService, times(2)).addBackwardReachableTransitions(eq(processContainer), eq(nodeDefinition), anyListOf(STransitionDefinition.class), anyListOf(STransitionDefinition.class), anyListOf(STransitionDefinition.class));
        verify(gatewayInstanceService).transitionsContainsAToken(anyListOf(STransitionDefinition.class), eq(nodeDefinition), eq(PROCESS_INSTANCE_ID), eq(processContainer));
    }
}
