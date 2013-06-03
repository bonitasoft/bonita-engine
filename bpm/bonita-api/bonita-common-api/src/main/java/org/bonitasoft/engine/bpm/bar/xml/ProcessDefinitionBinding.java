/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.bpm.actor.ActorDefinition;
import org.bonitasoft.engine.bpm.flownode.impl.FlowElementContainerDefinition;
import org.bonitasoft.engine.bpm.parameter.ParameterDefinition;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.impl.DesignProcessDefinitionImpl;
import org.bonitasoft.engine.io.xml.XMLParseException;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class ProcessDefinitionBinding extends NamedElementBinding {

    private String version;

    private final List<ActorDefinition> actors = new ArrayList<ActorDefinition>();

    private String actorInitiatorName;

    private final Set<ParameterDefinition> parameters = new HashSet<ParameterDefinition>();

    private DesignProcessDefinitionImpl processDefinitionImpl;

    private String displayDescription;

    private String displayName;

    private FlowElementContainerDefinition processContainer;

    private final List<StringIndex> stringIndexes = new ArrayList<StringIndex>(5);

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
            actorInitiatorName = (String) value;
        } else if (XMLProcessDefinition.PARAMETER_NODE.equals(name)) {
            parameters.add((ParameterDefinition) value);
        } else if (XMLProcessDefinition.FLOW_ELEMENTS_NODE.equals(name)) {
            processContainer = (FlowElementContainerDefinition) value;
        } else if (XMLProcessDefinition.STRING_INDEX.equals(name)) {
            stringIndexes.add((StringIndex) value);
        }
    }

    @Override
    public DesignProcessDefinition getObject() {
        if (processDefinitionImpl == null) {
            processDefinitionImpl = new DesignProcessDefinitionImpl(name, version);
            processDefinitionImpl.setDescription(description);
            processDefinitionImpl.setDisplayName(displayName);
            processDefinitionImpl.setDisplayDescription(displayDescription);
            for (final StringIndex stringIndex : stringIndexes) {
                processDefinitionImpl.setStringIndex(stringIndex.getIndex(), stringIndex.getLabel(), stringIndex.getValue());
            }
            for (final ActorDefinition actor : actors) {
                processDefinitionImpl.addActor(actor);
            }
            if (actorInitiatorName != null) {
                final ActorDefinition actor = processDefinitionImpl.getActor(actorInitiatorName);
                processDefinitionImpl.setActorInitiator(actor);
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
