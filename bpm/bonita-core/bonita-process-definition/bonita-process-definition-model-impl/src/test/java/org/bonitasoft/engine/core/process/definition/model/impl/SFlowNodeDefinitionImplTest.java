/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 **/

package org.bonitasoft.engine.core.process.definition.model.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.junit.Test;

/**
 * Created by Vincent Elcrin
 * Date: 18/12/13
 * Time: 14:57
 */
public class SFlowNodeDefinitionImplTest {

    SFlowNodeDefinitionImpl flowNode = new SFlowNodeDefinitionImpl(1L, "name") {

        private static final long serialVersionUID = -1297746953646018494L;

        @Override
        public SFlowNodeType getType() {
            return null;
        }
    };

    @Test
    public void isStartable_return_false_if_flow_node_has_incoming_transitions() {
        flowNode.addIncomingTransition(new STransitionDefinitionImpl("incoming"));

        assertFalse(flowNode.isStartable());
    }

    @Test
    public void isStartable_return_true_if_flow_node_has_no_incoming_transitions() {
        assertTrue(flowNode.isStartable());
    }

}
