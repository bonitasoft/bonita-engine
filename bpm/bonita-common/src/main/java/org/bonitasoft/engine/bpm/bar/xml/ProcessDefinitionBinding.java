/*
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package org.bonitasoft.engine.bpm.bar.xml;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

import org.bonitasoft.engine.bpm.model.ActorDefinition;
import org.bonitasoft.engine.bpm.model.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.model.FlowElementContainerDefinition;
import org.bonitasoft.engine.bpm.model.impl.DesignProcessDefinitionImpl;
import org.bonitasoft.engine.io.xml.exceptions.XMLParseException;

import com.bonitasoft.engine.bpm.model.ParameterDefinition;

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

    private String stringIndexLabel1;

    private String stringIndexLabel2;

    private String stringIndexLabel3;

    private String stringIndexLabel4;

    private String stringIndexLabel5;

    @Override
    public void setAttributes(final Map<String, String> attributes) throws XMLParseException {
        super.setAttributes(attributes);
        version = attributes.get(XMLProcessDefinition.VERSION);
        displayName = attributes.get(XMLProcessDefinition.DISPLAY_NAME);
        displayDescription = attributes.get(XMLProcessDefinition.DISPLAY_DESCRIPTION);
        stringIndexLabel1 = attributes.get(XMLProcessDefinition.STRING_INDEX_LABEL + 1);
        stringIndexLabel2 = attributes.get(XMLProcessDefinition.STRING_INDEX_LABEL + 2);
        stringIndexLabel3 = attributes.get(XMLProcessDefinition.STRING_INDEX_LABEL + 3);
        stringIndexLabel4 = attributes.get(XMLProcessDefinition.STRING_INDEX_LABEL + 4);
        stringIndexLabel5 = attributes.get(XMLProcessDefinition.STRING_INDEX_LABEL + 5);
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
            processDefinitionImpl.setStringIndexLabel(1, stringIndexLabel1);
            processDefinitionImpl.setStringIndexLabel(2, stringIndexLabel2);
            processDefinitionImpl.setStringIndexLabel(3, stringIndexLabel3);
            processDefinitionImpl.setStringIndexLabel(4, stringIndexLabel4);
            processDefinitionImpl.setStringIndexLabel(5, stringIndexLabel5);
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
