package org.bonitasoft.engine.command.helper.designer;

import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.expression.Expression;

/**
 * Created by Vincent Elcrin
 * Date: 16/12/13
 * Time: 14:20
 */
public abstract class FlowNode {

    private String name;

    private Expression condition;

    private boolean defaultTransition;

    protected FlowNode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract void build(ProcessDefinitionBuilder builder);

    public FlowNode setCondition(Expression condition) {
        this.condition = condition;
        return this;
    }

    public FlowNode setDefault(boolean defaultTransition) {
        this.defaultTransition = defaultTransition;
        return this;
    }

    public Expression getCondition() {
        return condition;
    }

    public boolean isDefault() {
        return defaultTransition;
    }
}
