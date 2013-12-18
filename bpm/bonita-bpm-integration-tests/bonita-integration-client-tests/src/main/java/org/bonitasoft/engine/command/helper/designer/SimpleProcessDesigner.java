package org.bonitasoft.engine.command.helper.designer;

import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Vincent Elcrin
 * Date: 16/12/13
 * Time: 18:38
 */
public class SimpleProcessDesigner {

    private final ProcessDefinitionBuilder builder;

    private List<FlowNode> currents = new ArrayList<FlowNode>();

    public static final String ACTOR_NAME = "Employee actor";

    public SimpleProcessDesigner(ProcessDefinitionBuilder builder) {
        this.builder = builder;
    }

    public SimpleProcessDesigner start() {
        return startWith(new StartEvent("start"));
    }

    public SimpleProcessDesigner startWith(FlowNode start) {
        currents.add(start);
        start.build(builder);
        return this;
    }

    public SimpleProcessDesigner then(FlowNode... flownodes) {
        assert !currents.isEmpty() : "startWith method need to be called first";

        for (FlowNode flownode : flownodes) {
            flownode.build(builder);
            addTransitions(currents, flownode);
        }
        currents.clear();
        currents.addAll(Arrays.asList(flownodes));
        return this;
    }

    private void addTransitions(List<FlowNode> currents, FlowNode flownode) {
        for (FlowNode current : currents) {
            if(flownode.isDefault()) {
                builder.addDefaultTransition(current.getName(), flownode.getName());
            } else {
                builder.addTransition(current.getName(), flownode.getName(), flownode.getCondition());
            }
        }
    }

    public SimpleProcessDesigner end() {
        return then(new EndEvent("end"));
    }

    public DesignProcessDefinition done() throws InvalidProcessDefinitionException {
        return builder.done();
    }

}
