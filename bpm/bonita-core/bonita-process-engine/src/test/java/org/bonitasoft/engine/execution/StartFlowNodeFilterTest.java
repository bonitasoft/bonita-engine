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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import org.bonitasoft.engine.core.process.definition.model.SFlowNodeDefinition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Vincent Elcrin
 */
@RunWith(MockitoJUnitRunner.class)
public class StartFlowNodeFilterTest {

    @Mock
    SFlowNodeDefinition flowNode;

    final StartFlowNodeFilter filter = new StartFlowNodeFilter();

    @Test
    public void select_should_return_false_if_flow_node_is_not_startable() {
        doReturn(false).when(flowNode).isStartable();

        assertFalse(filter.mustSelect(flowNode));
    }

    @Test
    public void select_should_return_true_if_flow_node_is_startable() {
        doReturn(true).when(flowNode).isStartable();

        assertTrue(filter.mustSelect(flowNode));
    }

}
