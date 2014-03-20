package org.bonitasoft.engine.execution;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import org.bonitasoft.engine.core.process.definition.model.SFlowNodeDefinition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Created by Vincent Elcrin
 * Date: 17/12/13
 * Time: 17:36
 */
@RunWith(MockitoJUnitRunner.class)
public class FlowNodeIdFilterTest {

    @Mock
    private SFlowNodeDefinition flownode;

    FlowNodeIdFilter filter = new FlowNodeIdFilter(3L);

    @Test
    public void select_should_return_false_if_flow_node_id_is_not_the_expected_one() throws Exception {
        doReturn(5L).when(flownode).getId();

        boolean result = filter.mustSelect(flownode);

        assertFalse(result);
    }

    @Test
    public void select_should_return_true_if_flow_node_id_is_the_expected_one() throws Exception {
        doReturn(3L).when(flownode).getId();

        boolean result = filter.mustSelect(flownode);

        assertTrue(result);
    }
}
