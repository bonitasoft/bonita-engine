package org.bonitasoft.engine.command.helper.designer;

import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;

/**
 * Created by Vincent Elcrin
 * Date: 16/12/13
 * Time: 11:46
 */
public class EndEvent extends FlowNode {

    public EndEvent(String name) {
        super(name);
    }

    @Override
    public void build(ProcessDefinitionBuilder builder) {
        builder.addEndEvent(getName());
    }


}
