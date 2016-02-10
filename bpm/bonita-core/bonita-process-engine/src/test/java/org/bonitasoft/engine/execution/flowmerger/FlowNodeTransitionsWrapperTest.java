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
package org.bonitasoft.engine.execution.flowmerger;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;

import org.bonitasoft.engine.core.process.definition.model.STransitionDefinition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Elias Ricken de Medeiros
 */
@RunWith(MockitoJUnitRunner.class)
public class FlowNodeTransitionsWrapperTest {

    @Mock
    private STransitionDefinition transition1;

    @Mock
    private STransitionDefinition transition2;

    private FlowNodeTransitionsWrapper flowNodeTransitionsWrapper;

    @Before
    public void setUp() {
        flowNodeTransitionsWrapper = new FlowNodeTransitionsWrapper();
    }

    @Test
    public void is_last_flowNode_if_no_valid_outgoing_transitions() {
        flowNodeTransitionsWrapper.setValidOutgoingTransitionDefinitions(Collections.<STransitionDefinition> emptyList());
        assertTrue(flowNodeTransitionsWrapper.isLastFlowNode());
    }

    @Test
    public void is_not_last_flowNode_if_valid_outgoing_transitions() {
        flowNodeTransitionsWrapper.setValidOutgoingTransitionDefinitions(Collections.singletonList(transition1));
        assertFalse(flowNodeTransitionsWrapper.isLastFlowNode());
    }

    @Test
    public void has_multiple_outgoing_transitions() {
        flowNodeTransitionsWrapper.setAllOutgoingTransitionDefinitions(Arrays.asList(transition1, transition2));
        assertTrue(flowNodeTransitionsWrapper.hasMultipleOutgoingTransitions());
    }

    @Test
    public void doesnt_have_multiple_outgoing_transitions_empty_list() {
        flowNodeTransitionsWrapper.setAllOutgoingTransitionDefinitions(Collections.<STransitionDefinition> emptyList());
        assertFalse(flowNodeTransitionsWrapper.hasMultipleOutgoingTransitions());
    }

    @Test
    public void doesnt_have_multiple_outgoing_transitions_one_transition() {
        flowNodeTransitionsWrapper.setAllOutgoingTransitionDefinitions(Arrays.asList(transition1));
        assertFalse(flowNodeTransitionsWrapper.hasMultipleOutgoingTransitions());
    }

    @Test
    public void has_multiple_incoming_transitions() {
        flowNodeTransitionsWrapper.setInputTransitionsSize(2);
        assertTrue(flowNodeTransitionsWrapper.hasMultipleIncomingTransitions());
    }

    @Test
    public void doesnt_have_multiple_incoming_transitions_zero() {
        flowNodeTransitionsWrapper.setInputTransitionsSize(0);
        assertFalse(flowNodeTransitionsWrapper.hasMultipleIncomingTransitions());
    }

    @Test
    public void doesnt_have_multiple_incoming_transitions_one() {
        flowNodeTransitionsWrapper.setInputTransitionsSize(1);
        assertFalse(flowNodeTransitionsWrapper.hasMultipleIncomingTransitions());
    }

    @Test
    public void isSimple_return_true_if_has_one_incoming_and_one_outgoing_transitions() {
        flowNodeTransitionsWrapper.setInputTransitionsSize(1);
        flowNodeTransitionsWrapper.setAllOutgoingTransitionDefinitions(Arrays.asList(transition1));
        assertTrue(flowNodeTransitionsWrapper.isSimpleMerge());
    }

    @Test
    public void isSimple_return_true_if_has_one_incoming_and_one_default_outgoing_transitions() {
        flowNodeTransitionsWrapper.setInputTransitionsSize(1);
        flowNodeTransitionsWrapper.setAllOutgoingTransitionDefinitions(Collections.<STransitionDefinition> emptyList());
        flowNodeTransitionsWrapper.setValidOutgoingTransitionDefinitions(Arrays.asList(transition1));
        assertTrue(flowNodeTransitionsWrapper.isSimpleMerge());
    }

    @Test
    public void isSimple_return_false_if_has_one_incoming_and_no_default_outgoing_transitions() {
        flowNodeTransitionsWrapper.setInputTransitionsSize(1);
        flowNodeTransitionsWrapper.setAllOutgoingTransitionDefinitions(Collections.<STransitionDefinition> emptyList());
        flowNodeTransitionsWrapper.setValidOutgoingTransitionDefinitions(Collections.<STransitionDefinition> emptyList());
        assertFalse(flowNodeTransitionsWrapper.isSimpleMerge());
    }

    @Test
    public void isSimple_return_true_if_has_zero_incoming_and_one_outgoing_transitions() {
        flowNodeTransitionsWrapper.setInputTransitionsSize(0);
        flowNodeTransitionsWrapper.setAllOutgoingTransitionDefinitions(Arrays.asList(transition1));
        assertTrue(flowNodeTransitionsWrapper.isSimpleMerge());
    }

    @Test
    public void isSimple_return_false_if_has_zero_outgoing_transitions() {
        flowNodeTransitionsWrapper.setInputTransitionsSize(1);
        flowNodeTransitionsWrapper.setAllOutgoingTransitionDefinitions(Arrays.<STransitionDefinition> asList());
        assertFalse(flowNodeTransitionsWrapper.isSimpleMerge());
    }

    @Test
    public void isSimple_return_false_if_has_one_incoming_and_multiple_outgoing_transitions() {
        flowNodeTransitionsWrapper.setInputTransitionsSize(1);
        flowNodeTransitionsWrapper.setAllOutgoingTransitionDefinitions(Arrays.asList(transition1, transition2));
        assertFalse(flowNodeTransitionsWrapper.isSimpleMerge());
    }

    @Test
    public void isSimple_return_false_if_has_multiple_incoming_and_one_outgoing_transitions() {
        flowNodeTransitionsWrapper.setInputTransitionsSize(2);
        flowNodeTransitionsWrapper.setAllOutgoingTransitionDefinitions(Arrays.asList(transition1));
        assertFalse(flowNodeTransitionsWrapper.isSimpleMerge());
    }

    @Test
    public void isSimpleToMany_return_true_if_has_one_incoming_and_multiple_outgoing_transitions() {
        flowNodeTransitionsWrapper.setInputTransitionsSize(1);
        flowNodeTransitionsWrapper.setAllOutgoingTransitionDefinitions(Arrays.asList(transition1, transition2));
        assertTrue(flowNodeTransitionsWrapper.isSimpleToMany());
    }

    @Test
    public void isSimpleToMany_return_true_if_has_zero_incoming_and_multiple_outgoing_transitions() {
        flowNodeTransitionsWrapper.setInputTransitionsSize(0);
        flowNodeTransitionsWrapper.setAllOutgoingTransitionDefinitions(Arrays.asList(transition1, transition2));
        assertTrue(flowNodeTransitionsWrapper.isSimpleToMany());
    }

    @Test
    public void isSimpleToMany_return_false_if_has_one_incoming_and_one_outgoing_transitions() {
        flowNodeTransitionsWrapper.setInputTransitionsSize(1);
        flowNodeTransitionsWrapper.setAllOutgoingTransitionDefinitions(Arrays.asList(transition1));
        assertFalse(flowNodeTransitionsWrapper.isSimpleToMany());
    }

    @Test
    public void isManyToMany_return_true_if_has_multiple_incoming_and_outgoing_transitions() {
        flowNodeTransitionsWrapper.setInputTransitionsSize(2);
        flowNodeTransitionsWrapper.setAllOutgoingTransitionDefinitions(Arrays.asList(transition1, transition2));
        assertTrue(flowNodeTransitionsWrapper.isManyToMany());
    }

    @Test
    public void isManyToMany_return_false_if_has_multiple_incoming_and_one_outgoing_transition() {
        flowNodeTransitionsWrapper.setInputTransitionsSize(2);
        flowNodeTransitionsWrapper.setAllOutgoingTransitionDefinitions(Arrays.asList(transition1));
        assertFalse(flowNodeTransitionsWrapper.isManyToMany());
    }

    @Test
    public void isManyToMany_return_false_if_has_one_incoming_and_multiple_outgoing_transition() {
        flowNodeTransitionsWrapper.setInputTransitionsSize(1);
        flowNodeTransitionsWrapper.setAllOutgoingTransitionDefinitions(Arrays.asList(transition1, transition2));
        assertFalse(flowNodeTransitionsWrapper.isManyToMany());
    }

    @Test
    public void isManyToOne_return_True_if_has_multiple_incoming_and_one_outgoing_transition() {
        flowNodeTransitionsWrapper.setInputTransitionsSize(2);
        flowNodeTransitionsWrapper.setAllOutgoingTransitionDefinitions(Arrays.asList(transition1));
        assertTrue(flowNodeTransitionsWrapper.isManyToOne());
    }

    @Test
    public void isManyToOne_return_false_if_has_multiple_incoming_and_multiple_outgoing_transition() {
        flowNodeTransitionsWrapper.setInputTransitionsSize(2);
        flowNodeTransitionsWrapper.setAllOutgoingTransitionDefinitions(Arrays.asList(transition1, transition2));
        assertFalse(flowNodeTransitionsWrapper.isManyToOne());
    }

    @Test
    public void isManyToOne_return_false_if_has_one_incoming_and_multiple_one_transition() {
        flowNodeTransitionsWrapper.setInputTransitionsSize(1);
        flowNodeTransitionsWrapper.setAllOutgoingTransitionDefinitions(Arrays.asList(transition1));
        assertFalse(flowNodeTransitionsWrapper.isManyToOne());
    }

    @Test
    public void isManyToOne_return_false_if_has_multiple_incoming_and_multiple_zero_transition() {
        flowNodeTransitionsWrapper.setInputTransitionsSize(12);
        flowNodeTransitionsWrapper.setAllOutgoingTransitionDefinitions(Arrays.<STransitionDefinition> asList());
        assertFalse(flowNodeTransitionsWrapper.isManyToOne());
    }

}
