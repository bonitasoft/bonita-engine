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
package org.bonitasoft.engine.core.process.definition.model.bindings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.core.process.definition.model.SConnectorDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeDefinition;
import org.bonitasoft.engine.core.process.definition.model.STransitionDefinition;
import org.bonitasoft.engine.core.process.definition.model.impl.SFlowNodeDefinitionImpl;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public abstract class SFlowNodeDefinitionBinding extends SNamedElementBinding {

    private final List<SConnectorDefinition> connectors = new ArrayList<SConnectorDefinition>();

    private final List<STransitionDefinition> incomingTransitions = new ArrayList<STransitionDefinition>();

    private final List<STransitionDefinition> outgoingTransitions = new ArrayList<STransitionDefinition>();

    private STransitionDefinition defaultTransition;

    private SExpression displayDescription;

    private SExpression displayName;

    private SExpression displayDescriptionAfterCompletion;

    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) {
    }

    @Override
    public void setChildObject(final String name, final Object value) {
        if (XMLSProcessDefinition.DISPLAY_DESCRIPTION.equals(name)) {
            displayDescription = (SExpression) value;
        } else if (XMLSProcessDefinition.DISPLAY_DESCRIPTION_AFTER_COMPLETION.equals(name)) {
            displayDescriptionAfterCompletion = (SExpression) value;
        } else if (XMLSProcessDefinition.DISPLAY_NAME.equals(name)) {
            displayName = (SExpression) value;
        } else if (XMLSProcessDefinition.CONNECTOR_NODE.equals(name)) {
            connectors.add((SConnectorDefinition) value);
        } else if (XMLSProcessDefinition.INCOMING_TRANSITION.equals(name)) {
            incomingTransitions.add((STransitionDefinition) value);
        } else if (XMLSProcessDefinition.OUTGOING_TRANSITION.equals(name)) {
            outgoingTransitions.add((STransitionDefinition) value);
        } else if (XMLSProcessDefinition.DEFAULT_TRANSITION.equals(name)) {
            defaultTransition = (STransitionDefinition) value;
        }
    }

    protected void fillNode(final SFlowNodeDefinition flowNode) {
        final SFlowNodeDefinitionImpl sFlowNodeDefinitionImpl = (SFlowNodeDefinitionImpl) flowNode;
        sFlowNodeDefinitionImpl.setDescription(description);
        sFlowNodeDefinitionImpl.setDisplayDescription(displayDescription);
        sFlowNodeDefinitionImpl.setDisplayDescriptionAfterCompletion(displayDescriptionAfterCompletion);
        sFlowNodeDefinitionImpl.setDisplayName(displayName);
        for (final SConnectorDefinition connector : connectors) {
            sFlowNodeDefinitionImpl.addConnector(connector);
        }
        for (final STransitionDefinition transition : incomingTransitions) {
            sFlowNodeDefinitionImpl.addIncomingTransition(transition);
        }
        for (final STransitionDefinition transition : outgoingTransitions) {
            sFlowNodeDefinitionImpl.addOutgoingTransition(transition);
        }
        sFlowNodeDefinitionImpl.setDefaultTransition(defaultTransition);
    }

}
