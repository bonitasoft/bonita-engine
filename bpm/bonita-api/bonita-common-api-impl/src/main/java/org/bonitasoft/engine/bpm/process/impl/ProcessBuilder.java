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

import org.bonitasoft.engine.bpm.flownode.impl.internal.FlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.bpm.process.impl.internal.DesignProcessDefinitionImpl;

/**
 * @author Matthieu Chaffotte
 */
public class ProcessBuilder extends FlowElementContainerBuilder {

    protected final DesignProcessDefinitionImpl process;

    protected ProcessBuilder(final DesignProcessDefinitionImpl process, final ProcessDefinitionBuilder processDefinitionBuilder) {
        super((FlowElementContainerDefinitionImpl) process.getProcessContainer(), processDefinitionBuilder);
        this.process = process;
    }

    /**
     * Adds an actor on this element
     * 
     * @param actorName actor name
     * @return
     */
    public ActorDefinitionBuilder addActor(final String actorName) {
        return new ActorDefinitionBuilder(getProcessBuilder(), process, actorName, false);
    }

    /**
     * Adds an actor on this process
     * 
     * @param name actor name
     * @param initiator defines whether it's the actor initiator (actor that's able to start the process)
     * @return
     */
    public ActorDefinitionBuilder addActor(final String name, final boolean initiator) {
        return new ActorDefinitionBuilder(getProcessBuilder(), process, name, initiator);
    }

    /**
     * Adds an actor initiator on this process. The actor initiator is the one that will start the process.
     * 
     * @param actorName
     * @return
     */
    public ActorDefinitionBuilder setActorInitiator(final String actorName) {
        return new ActorDefinitionBuilder(getProcessBuilder(), process, actorName, true);
    }

    /**
     * Adds a {@link org.bonitasoft.engine.bpm.document.DocumentDefinition} on this process
     * 
     * @param name document name
     * @param fileName document file name
     * @return
     */
    public DocumentDefinitionBuilder addDocumentDefinition(final String name, final String fileName) {
        return new DocumentDefinitionBuilder(getProcessBuilder(), (FlowElementContainerDefinitionImpl) process.getProcessContainer(), name, fileName);
    }

    /**
     * Add a parameter on this process.
     *
     * @param parameterName
     *        The name of the new {@link org.bonitasoft.engine.bpm.parameter.ParameterDefinition}
     * @param type
     *        The type of the new {@link org.bonitasoft.engine.bpm.parameter.ParameterDefinition} (complete class name)
     * @return The {@link ParameterDefinitionBuilder} containing the new {@link org.bonitasoft.engine.bpm.parameter.ParameterDefinition}
     */
    public ParameterDefinitionBuilder addParameter(final String parameterName, final String type) {
        return new ParameterDefinitionBuilder(getProcessBuilder(), process, parameterName, type);
    }

    DesignProcessDefinitionImpl getProcessDefinition() {
        return process;
    }

}
