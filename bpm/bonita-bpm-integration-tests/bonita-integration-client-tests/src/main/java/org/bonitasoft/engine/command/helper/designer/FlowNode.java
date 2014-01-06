package org.bonitasoft.engine.command.helper.designer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;

/**
 * Created by Vincent Elcrin
 * Date: 16/12/13
 * Time: 14:20
 */
public abstract class FlowNode implements Buildable, Linkable {

    private String name;

    private Map<String, Condition> conditions = new HashMap<String, Condition>();

    private Transition transition = new Transition(this);

    protected FlowNode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract void build(ProcessDefinitionBuilder builder);

    public FlowNode when(String step, Condition condition) {
        conditions.put(step, condition);
        return this;
    }

    @Override
    public void bind(List<FlowNode> origins, ProcessDefinitionBuilder builder) {
        for (FlowNode origin : origins) {
            transition.bind(origin, conditions.get(origin.getName()), builder);
        }
    }
}
