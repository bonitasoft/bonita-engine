package org.bonitasoft.engine.execution;

import org.bonitasoft.engine.core.process.definition.model.SFlowNodeDefinition;

/**
 * Created by Vincent Elcrin
 * Date: 17/12/13
 * Time: 17:34
 */
public class FlowNodeIdFilter implements Filter<SFlowNodeDefinition> {

    private final long id;

    public FlowNodeIdFilter(long id) {
        this.id = id;
    }

    @Override
    public boolean mustSelect(SFlowNodeDefinition flowNode) {
        return flowNode.getId() == id;
    }
}
