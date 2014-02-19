package org.bonitasoft.engine.command.helper.designer;

import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.UserTaskDefinitionBuilder;

/**
 * Created by Vincent Elcrin
 * Date: 16/12/13
 * Time: 11:43
 */
public class UserTask extends FlowNode {

    private String actor = "actor";

    private BoundaryEvent event;

    public UserTask(String name) {
        super(name);
    }

    @Override
    public void build(ProcessDefinitionBuilder builder) {
        UserTaskDefinitionBuilder task = builder.addUserTask(getName(), actor);
        if(event != null) {
            event.attach(task, builder);
        }
    }

    public UserTask with(BoundaryEvent event) {
        this.event = event;
        return this;
    }
}
