/**
 * Copyright (C) 2011-2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.core.process.definition.model.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bonitasoft.engine.bpm.model.ActorDefinition;
import org.bonitasoft.engine.bpm.model.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.model.ParameterDefinition;
import org.bonitasoft.engine.core.operation.model.builder.SOperationBuilders;
import org.bonitasoft.engine.core.process.definition.model.SActorDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SParameterDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.data.definition.model.builder.SDataDefinitionBuilders;
import org.bonitasoft.engine.expression.model.builder.SExpressionBuilders;

/**
 * @author Matthieu Chaffotte
 * @author Zhao Na
 * @author Elias Ricken de Medeiros
 * @author Yanyan Liu
 */
public class SProcessDefinitionImpl extends SNamedElementImpl implements SProcessDefinition {

    private static final long serialVersionUID = -8961400385282752731L;

    private final String version;

    private String description;

    private final Set<SParameterDefinition> parameters;

    private final Set<SActorDefinition> actors;

    private SActorDefinition sActorInitiator;

    private SFlowElementContainerDefinition container;

    public SProcessDefinitionImpl(final DesignProcessDefinition processDefinition, final SExpressionBuilders sExpressionBuilders,
            final SDataDefinitionBuilders sDataDefinitionBuilders, final SOperationBuilders sOperationBuilders) {

        super(processDefinition.getName());
        description = processDefinition.getDescription();
        version = processDefinition.getVersion();
        actors = new HashSet<SActorDefinition>();
        for (final ActorDefinition actor : processDefinition.getActors()) {
            actors.add(new SActorDefinitionImpl(actor));
        }

        parameters = new HashSet<SParameterDefinition>();
        for (final ParameterDefinition parameterDefinition : processDefinition.getParameters()) {
            parameters.add(new SParameterDefinitionImpl(parameterDefinition));
        }

        final ActorDefinition actorInitiator = processDefinition.getActorInitiator();
        if (actorInitiator != null) {
            sActorInitiator = new SActorDefinitionImpl(actorInitiator);
        }

        container = new SFlowElementContainerDefinitionImpl(this, processDefinition.getProcessContainer(), sExpressionBuilders, sDataDefinitionBuilders,
                sOperationBuilders);
    }

    public SProcessDefinitionImpl(final String name, final String version) {
        super(name);
        this.version = version;
        actors = new HashSet<SActorDefinition>();
        parameters = new HashSet<SParameterDefinition>();
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public Set<SParameterDefinition> getParameters() {
        return parameters;
    }

    @Override
    public SParameterDefinition getParameter(final String parameterName) {
        final Iterator<SParameterDefinition> iterator = parameters.iterator();
        SParameterDefinition found = null;
        while (found == null && iterator.hasNext()) {
            final SParameterDefinition next = iterator.next();
            if (next.getName().equals(parameterName)) {
                found = next;
            }
        }
        return found;
    }

    @Override
    public Set<SActorDefinition> getActors() {
        return actors;
    }

    public void addActor(final SActorDefinition actor) {
        actors.add(actor);
    }

    public void addParameter(final SParameterDefinition parameter) {
        parameters.add(parameter);
    }

    @Override
    public SActorDefinition getActorInitiator() {
        return sActorInitiator;
    }

    public void setActorInitiator(final SActorDefinition sActorInitiator) {
        this.sActorInitiator = sActorInitiator;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public SFlowElementContainerDefinition getProcessContainer() {
        return container;
    }

    public void setProcessContainer(final SFlowElementContainerDefinition processContainer) {
        container = processContainer;
    }

}
