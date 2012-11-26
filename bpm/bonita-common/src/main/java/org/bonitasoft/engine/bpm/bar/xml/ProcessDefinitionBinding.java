/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
import java.util.HashSet;
import java.util.Map;

import org.bonitasoft.engine.bpm.model.ActorDefinition;
import org.bonitasoft.engine.bpm.model.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.model.FlowElementContainerDefinition;
import org.bonitasoft.engine.bpm.model.ParameterDefinition;
import org.bonitasoft.engine.bpm.model.impl.DesignProcessDefinitionImpl;
import org.bonitasoft.engine.io.xml.exceptions.XMLParseException;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class ProcessDefinitionBinding extends NamedElementBinding {

    private String version;

    private final ArrayList<ActorDefinition> actors = new ArrayList<ActorDefinition>();

    private ActorDefinition actorInitiator;

    private final HashSet<ParameterDefinition> parameters = new HashSet<ParameterDefinition>();

    private DesignProcessDefinitionImpl processDefinitionImpl;

    private String displayDescription;

    private String displayName;

    private FlowElementContainerDefinition processContainer;

    @Override
    public void setAttributes(final Map<String, String> attributes) throws XMLParseException {
        super.setAttributes(attributes);
        version = attributes.get(XMLProcessDefinition.VERSION);
        displayName = attributes.get(XMLProcessDefinition.DISPLAY_NAME);
        displayDescription = attributes.get(XMLProcessDefinition.DISPLAY_DESCRIPTION);
    }

    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) throws XMLParseException {
    }

    @Override
    public void setChildObject(final String name, final Object value) throws XMLParseException {
        if (XMLProcessDefinition.ACTOR_NODE.equals(name)) {
            actors.add((ActorDefinition) value);
        } else if (XMLProcessDefinition.INITIATOR_NODE.equals(name)) {
            actorInitiator = (ActorDefinition) value;
        } else if (XMLProcessDefinition.PARAMETER_NODE.equals(name)) {
            parameters.add((ParameterDefinition) value);
        } else if (XMLProcessDefinition.FLOW_ELEMENTS_NODE.equals(name)) {
            processContainer = (FlowElementContainerDefinition) value;
        }
    }

    @Override
    public DesignProcessDefinition getObject() {
        if (processDefinitionImpl == null) {
            processDefinitionImpl = new DesignProcessDefinitionImpl(name, version);
            processDefinitionImpl.setDescription(description);
            processDefinitionImpl.setDisplayName(displayName);
            processDefinitionImpl.setDisplayDescription(displayDescription);
            for (final ActorDefinition actor : actors) {
                processDefinitionImpl.addActor(actor);
            }
            if (actorInitiator != null) {
                processDefinitionImpl.setActorInitiator(actorInitiator);
            }
            for (final ParameterDefinition parameter : parameters) {
                processDefinitionImpl.addParameter(parameter);
            }
            if (processContainer != null) {
                processDefinitionImpl.setProcessContainer(processContainer);
            }
        }
        return processDefinitionImpl;
    }

    @Override
    public String getElementTag() {
        return XMLProcessDefinition.PROCESS_NODE;
    }

}
