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
package org.bonitasoft.engine.core.process.definition.model.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Vincent Elcrin
 * @author Celine Souchet
 */
public class SFlowNodeDefinitionImplTest {

    private SFlowNodeDefinitionImpl flowNode;

    @Before
    public void before() {
        flowNode = new SFlowNodeDefinitionImpl(1L, "name") {

            private static final long serialVersionUID = -1297746953646018494L;

            @Override
            public SFlowNodeType getType() {
                return null;
            }
        };
    }

    @Test
    public void isStartable_return_false_if_flow_node_has_incoming_transitions() {
        flowNode.addIncomingTransition(new STransitionDefinitionImpl("incoming"));

        assertFalse(flowNode.isStartable());
    }

    @Test
    public void isStartable_return_true_if_flow_node_has_no_incoming_transitions() {
        assertTrue(flowNode.isStartable());
    }

    @Test
    public void is_not_interrupting_if_not_catch_event() {
        assertFalse(flowNode.isInterrupting());
    }

    @Test
    public void hasIncommingTransitions_return_true_if_flownode_hasIncommingTransitions() {
        flowNode.addIncomingTransition(new STransitionDefinitionImpl("incoming"));

        assertTrue(flowNode.hasIncomingTransitions());
    }

    @Test
    public void hasIncommingTransitions_return_false_if_flownode_doesnt_have_IncommingTransitions() {
        assertFalse(flowNode.hasIncomingTransitions());
    }

    @Test
    public void isEventSubProcess_return_false_if_is_not_sub_process() {
        assertFalse(flowNode.isEventSubProcess());
    }

}
