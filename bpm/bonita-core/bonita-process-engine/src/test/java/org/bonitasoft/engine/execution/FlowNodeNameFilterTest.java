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

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.core.process.definition.model.SFlowNodeDefinition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Elias Ricken de Medeiros
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class FlowNodeNameFilterTest {

    @Mock
    private SFlowNodeDefinition flowNode1;

    @Mock
    private SFlowNodeDefinition flowNode2;

    private final List<String> names = Arrays.asList("step1", "step3");

    @Before
    public void setUp() {
        doReturn("step1").when(flowNode1).getName();
        doReturn("step2").when(flowNode2).getName();
    }

    @Test
    public void select_return_true_if_name_is_contained_in_the_list() {
        assertTrue(new FlowNodeNameFilter(names).mustSelect(flowNode1));
    }

    @Test
    public void select_return_false_if_name_is_contained_in_the_list() {
        assertFalse(new FlowNodeNameFilter(names).mustSelect(flowNode2));
    }

}
