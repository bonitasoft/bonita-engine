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
package org.bonitasoft.engine.execution.transition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.bonitasoft.engine.commons.exceptions.SExceptionContext;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.STransitionDefinition;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityExecutionException;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.execution.flowmerger.FlowNodeTransitionsWrapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultTransitionGetterTest {

    private DefaultTransitionGetter defaultTransitionGetter;

    @Mock
    private SProcessDefinition processDefinition;

    @Mock
    private SFlowNodeInstance flowNodeInstance;

    public static final String PROCESS_NAME = "my proc";

    public static final String FLOW_NODE_NAME = "gateway";

    public static final String PROCESS_VERSION = "4.1";

    public static final long PROCESS_INSTANCE_ID = 1;

    @Before
    public void setUp() throws Exception {
        defaultTransitionGetter = new DefaultTransitionGetter();

        given(processDefinition.getName()).willReturn(PROCESS_NAME);
        given(processDefinition.getVersion()).willReturn(PROCESS_VERSION);
        given(flowNodeInstance.getName()).willReturn(FLOW_NODE_NAME);
        given(flowNodeInstance.getParentProcessInstanceId()).willReturn(PROCESS_INSTANCE_ID);

    }

    @Test
    public void getDefaultTransition_should_return_default_transition_from_wrapper() throws Exception {
        //given
        FlowNodeTransitionsWrapper wrapper = new FlowNodeTransitionsWrapper();
        STransitionDefinition transition = mock(STransitionDefinition.class);
        wrapper.setDefaultTransition(transition);

        //when
        STransitionDefinition defaultTransition = defaultTransitionGetter.getDefaultTransition(wrapper, processDefinition, flowNodeInstance);

        //then
        assertThat(defaultTransition).isEqualTo(transition);
    }

    @Test
    public void getDefaultTransition_should_throw_SActivityExecutionException_when_default_transition_is_not_set() throws Exception {
        //given
        FlowNodeTransitionsWrapper wrapper = new FlowNodeTransitionsWrapper();

        try {
            //when
            defaultTransitionGetter.getDefaultTransition(wrapper, processDefinition, flowNodeInstance);
            fail("Exception expected");
        } catch (SActivityExecutionException e) {
            //then
            assertThat(e.getMessage()).contains("There is no default transition on " + FLOW_NODE_NAME
                    + ", but no outgoing transition had a valid condition.");
            assertThat(e.getContext().get(SExceptionContext.PROCESS_NAME)).isEqualTo(PROCESS_NAME);
            assertThat(e.getContext().get(SExceptionContext.PROCESS_VERSION)).isEqualTo(PROCESS_VERSION);
            assertThat(e.getContext().get(SExceptionContext.PROCESS_INSTANCE_ID)).isEqualTo(PROCESS_INSTANCE_ID);

        }

    }

}
