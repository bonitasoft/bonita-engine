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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.core.process.definition.model.SActorDefinition;
import org.bonitasoft.engine.core.process.definition.model.SContextEntry;
import org.bonitasoft.engine.core.process.definition.model.SContractDefinition;
import org.bonitasoft.engine.core.process.definition.model.SParameterDefinition;
import org.bonitasoft.engine.core.process.definition.model.impl.SFlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SProcessDefinitionImpl;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class SProcessDefinitionBinding extends SNamedElementBinding {

    private String version;

    private final List<SActorDefinition> actors = new ArrayList<SActorDefinition>();

    private SActorDefinition actorInitiator;

    private final Set<SParameterDefinition> parameters = new HashSet<SParameterDefinition>();

    private SFlowElementContainerDefinitionImpl processContainer;

    private final List<SStringIndex> stringIndexes = new ArrayList<SStringIndex>(5);
    private SContractDefinition contract;
    private List<SContextEntry> context;

    @Override
    public void setAttributes(final Map<String, String> attributes) {
        super.setAttributes(attributes);
        version = attributes.get(XMLSProcessDefinition.VERSION);
    }

    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) {
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setChildObject(final String name, final Object value) {
        if (XMLSProcessDefinition.ACTOR_NODE.equals(name)) {
            actors.add((SActorDefinition) value);
        } else if (XMLSProcessDefinition.INITIATOR_NODE.equals(name)) {
            actorInitiator = (SActorDefinition) value;
        } else if (XMLSProcessDefinition.PARAMETER_NODE.equals(name)) {
            parameters.add((SParameterDefinition) value);
        } else if (XMLSProcessDefinition.FLOW_ELEMENTS_NODE.equals(name)) {
            processContainer = (SFlowElementContainerDefinitionImpl) value;
        } else if (XMLSProcessDefinition.STRING_INDEX.equals(name)) {
            stringIndexes.add((SStringIndex) value);
        } else if (XMLSProcessDefinition.CONTRACT_NODE.equals(name)) {
            contract = (SContractDefinition) value;
        }else if (XMLSProcessDefinition.CONTEXT_NODE.equals(name)) {
            context = (List<SContextEntry>) value;
        }
    }

    @Override
    public SProcessDefinitionImpl getObject() {
        final SProcessDefinitionImpl processDefinitionImpl = new SProcessDefinitionImpl(name, version);
        processDefinitionImpl.setId(id);
        processDefinitionImpl.setDescription(description);
        for (final SStringIndex stringIndex : stringIndexes) {
            processDefinitionImpl.setStringIndex(stringIndex.getIndex(), stringIndex.getLabel(), stringIndex.getValue());
        }
        for (final SActorDefinition actor : actors) {
            processDefinitionImpl.addActor(actor);
        }
        if (actorInitiator != null) {
            processDefinitionImpl.setActorInitiator(actorInitiator);
        }
        for (final SParameterDefinition parameter : parameters) {
            processDefinitionImpl.addParameter(parameter);
        }
        if (processContainer != null) {
            processDefinitionImpl.setProcessContainer(processContainer);
            processContainer.setElementContainer(processDefinitionImpl);
        }
        if (contract != null) {
            processDefinitionImpl.setContract(contract);
        }
        if (contract != null) {
            processDefinitionImpl.setContract(contract);
        }
        if (context != null) {
            processDefinitionImpl.getContext().addAll(context);
        }
        return processDefinitionImpl;
    }

    @Override
    public String getElementTag() {
        return XMLSProcessDefinition.PROCESS_NODE;
    }

}
