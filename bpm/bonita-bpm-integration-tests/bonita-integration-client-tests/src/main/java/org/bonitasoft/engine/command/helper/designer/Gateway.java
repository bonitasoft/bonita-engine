package org.bonitasoft.engine.command.helper.designer;

import org.bonitasoft.engine.bpm.flownode.GatewayType;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;

/**
 * Created by Vincent Elcrin
 * Date: 16/12/13
 * Time: 12:20
 */
public class Gateway extends FlowNode {

    private GatewayType type;

    public Gateway(String name, GatewayType type) {
        super(name);
        this.type = type;
    }

    @Override
    public void build(ProcessDefinitionBuilder builder) {
        builder.addGateway(getName(), type);
    }
}
