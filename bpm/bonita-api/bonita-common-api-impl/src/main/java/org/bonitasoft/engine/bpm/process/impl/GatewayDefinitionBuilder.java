/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.bpm.process.impl;

import org.bonitasoft.engine.bpm.flownode.GatewayType;
import org.bonitasoft.engine.bpm.flownode.impl.internal.FlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.GatewayDefinitionImpl;
import org.bonitasoft.engine.expression.Expression;

/**
 * @author Feng Hui
 * @author Zhao Na
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class GatewayDefinitionBuilder extends FlowElementContainerBuilder implements DescriptionBuilder {

    private final GatewayDefinitionImpl gateway;

    GatewayDefinitionBuilder(final ProcessDefinitionBuilder processDefinitionBuilder, final FlowElementContainerDefinitionImpl container, final String name,
            final GatewayType gatewayType) {
        super(container, processDefinitionBuilder);
        gateway = new GatewayDefinitionImpl(name, gatewayType);

        if (!container.getGatewaysList().contains(gateway)) {
            container.addGateway(gateway);
        }
    }

    /**
     * Adds a default transition
     * 
     * @param target target element
     * @return
     */
    public TransitionDefinitionBuilder addDefaultTransition(final String target) {
        return new TransitionDefinitionBuilder(getProcessBuilder(), getContainer(), gateway.getName(), target, true);
    }

    @Override
    public GatewayDefinitionBuilder addDescription(final String description) {
        gateway.setDescription(description);
        return this;
    }

    /**
     * Sets the display description on this gateway
     * 
     * @param displayDescription
     *        expression representing the display description
     * @return
     */
    public GatewayDefinitionBuilder addDisplayDescription(final Expression displayDescription) {
        gateway.setDisplayDescription(displayDescription);
        return this;
    }

    /**
     * Sets the display name on this gateway
     * 
     * @param displayName
     *        expression representing the display name
     * @return
     */
    public GatewayDefinitionBuilder addDisplayName(final Expression displayName) {
        gateway.setDisplayName(displayName);
        return this;
    }

    /**
     * Sets the display description after completion on this gateway. This will be used to updated the display description when the gateway completes its
     * execution
     * 
     * @param displayDescriptionAfterCompletion
     *        expression representing the new display description after the gateway completion.
     * @return
     */
    public GatewayDefinitionBuilder addDisplayDescriptionAfterCompletion(final Expression displayDescriptionAfterCompletion) {
        gateway.setDisplayDescriptionAfterCompletion(displayDescriptionAfterCompletion);
        return this;
    }

}
