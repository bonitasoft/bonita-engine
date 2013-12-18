package org.bonitasoft.engine.command.helper.designer;

import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;

/**
 * Created by Vincent Elcrin
 * Date: 16/12/13
 * Time: 11:43
 */
public class UserTask extends FlowNode {

    private String actor = "actor";

    public UserTask(String name) {
        super(name);
    }

    @Override
    public void build(ProcessDefinitionBuilder builder) {
        builder.addUserTask(getName(), actor);
    }
}
