package org.bonitasoft.engine.command.helper.designer;

import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;

/**
 * Created by Vincent Elcrin
 * Date: 16/12/13
 * Time: 11:45
 */
public class StartEvent extends FlowNode {

    public StartEvent(String name) {
        super(name);
    }

    @Override
    public void build(ProcessDefinitionBuilder builder) {
        builder.addStartEvent(getName());
    }
}
