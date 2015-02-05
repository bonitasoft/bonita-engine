/*
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.bonitasoft.engine.core.process.instance.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeDefinition;
import org.bonitasoft.engine.core.process.definition.model.STransitionDefinition;
import org.bonitasoft.engine.core.process.definition.model.impl.STransitionDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SUserTaskDefinitionImpl;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.impl.SUserTaskInstanceImpl;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
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

        List<SFlowNodeDefinition> sourceElements = new ArrayList<SFlowNodeDefinition>(Arrays.<SFlowNodeDefinition> asList(step1, step2));
        List<SFlowNodeDefinition> targetElements = new ArrayList<SFlowNodeDefinition>(Arrays.<SFlowNodeDefinition> asList(step2, step3));
        List<SFlowNodeDefinition> sourceAndTarget = gatewayInstanceService.extractElementThatAreSourceAndTarget(sourceElements, targetElements);

        assertThat(sourceElements).containsOnly(step1);
        assertThat(targetElements).containsOnly(step3);
        assertThat(sourceAndTarget).containsOnly(step2);
    }

    SFlowNodeDefinition node(long id, String name, STransitionDefinition... incomingTransitions) {
        SUserTaskDefinitionImpl definition = new SUserTaskDefinitionImpl(id, name, "actor");
        for (STransitionDefinition incommingTransition : incomingTransitions) {
            definition.addIncomingTransition(incommingTransition);
        }
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
        QueryOptions searchOptions = new QueryOptions(0, 20, Collections.<OrderByOption> emptyList(), filters, null);
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
        node(1, "step1");
        node(2, "step2");
        node(3, "step3");
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
        node(2, "stepa2", transition(1, 2));
        node(3, "stepa3", transition(2, 3));
        node(4, "stepb4");
        node(5, "stepb5");
        node(6, "stepb6", transition(5, 6), transition(4, 6));

        List<STransitionDefinition> startTransition = Arrays.asList(transition(6, 666), transition(3, 666));
        List<STransitionDefinition> toComplete = new ArrayList<STransitionDefinition>();
        gatewayInstanceService.addBackwardReachableTransitions(processContainer, gate, startTransition, toComplete, Collections.<STransitionDefinition>emptyList());

        assertThat(toComplete).containsOnly(transition(1, 2), transition(2, 3), transition(4, 6), transition(5, 6), transition(3, 666), transition(6, 666));
    }

    private STransitionDefinition transition(long source, long target) {
        return new STransitionDefinitionImpl("name", source, target);
    }

}
