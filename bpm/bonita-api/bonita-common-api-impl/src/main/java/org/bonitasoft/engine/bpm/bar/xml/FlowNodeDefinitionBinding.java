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
package org.bonitasoft.engine.bpm.bar.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.connector.ConnectorDefinition;
import org.bonitasoft.engine.bpm.flownode.TransitionDefinition;
import org.bonitasoft.engine.bpm.flownode.impl.internal.FlowNodeDefinitionImpl;
import org.bonitasoft.engine.expression.Expression;

/**
 * @author Baptiste Mesta
 */
public abstract class FlowNodeDefinitionBinding extends NamedElementBinding {

    protected final List<ConnectorDefinition> connectors = new ArrayList<ConnectorDefinition>();

    protected final List<TransitionDefinition> incomingTransitions = new ArrayList<TransitionDefinition>();

    protected final List<TransitionDefinition> outgoingTransitions = new ArrayList<TransitionDefinition>();

    private TransitionDefinition defaultTransition;

    private Expression displayName;

    private Expression displayDescription;

    private Expression displayDescriptionAfterCompletion;

    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) {
    }

    @Override
    public void setChildObject(final String name, final Object value) {
        if (XMLProcessDefinition.DISPLAY_NAME.equals(name)) {
            displayName = (Expression) value;
        }
        if (XMLProcessDefinition.DISPLAY_DESCRIPTION.equals(name)) {
            displayDescription = (Expression) value;
        }
        if (XMLProcessDefinition.DISPLAY_DESCRIPTION_AFTER_COMPLETION.equals(name)) {
            displayDescriptionAfterCompletion = (Expression) value;
        }
        if (XMLProcessDefinition.CONNECTOR_NODE.equals(name)) {
            connectors.add((ConnectorDefinition) value);
        }
        if (XMLProcessDefinition.INCOMING_TRANSITION.equals(name)) {
            incomingTransitions.add((TransitionDefinition) value);
        }
        if (XMLProcessDefinition.OUTGOING_TRANSITION.equals(name)) {
            outgoingTransitions.add((TransitionDefinition) value);
        }
        if (XMLProcessDefinition.DEFAULT_TRANSITION.equals(name)) {
            defaultTransition = (TransitionDefinition) value;
        }
    }

    protected void fillNode(final FlowNodeDefinitionImpl flowNode) {
        flowNode.setDescription(description);
        flowNode.setDisplayName(displayName);
        flowNode.setDisplayDescriptionAfterCompletion(displayDescriptionAfterCompletion);
        flowNode.setDisplayDescription(displayDescription);
        for (final ConnectorDefinition connector : connectors) {
            flowNode.addConnector(connector);
        }
        for (final TransitionDefinition transition : incomingTransitions) {
            flowNode.addIncomingTransition(transition);
        }
        for (final TransitionDefinition transition : outgoingTransitions) {
            flowNode.addOutgoingTransition(transition);
        }
        flowNode.setDefaultTransition(defaultTransition);
    }

}
