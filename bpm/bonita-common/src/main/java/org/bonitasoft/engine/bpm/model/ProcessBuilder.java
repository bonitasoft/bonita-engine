/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.bpm.model;

import org.bonitasoft.engine.bpm.model.impl.DesignProcessDefinitionImpl;
import org.bonitasoft.engine.bpm.model.impl.FlowElementContainerDefinitionImpl;

/**
 * @author Matthieu Chaffotte
 */
public class ProcessBuilder extends FlowElementContainerBuilder {

    private final DesignProcessDefinitionImpl process;

    ProcessBuilder(final DesignProcessDefinitionImpl process, final ProcessDefinitionBuilder processDefinitionBuilder) {
        super((FlowElementContainerDefinitionImpl) process.getProcessContainer(), processDefinitionBuilder);
        this.process = process;
    }

    public ParameterDefinitionBuilder addParameter(final String parameterName, final String type) {
        return new ParameterDefinitionBuilder(getProcessBuilder(), process, parameterName, type);
    }

    public ActorDefinitionBuilder addActor(final String actorName) {
        return new ActorDefinitionBuilder(getProcessBuilder(), process, actorName, false);
    }

    public ActorDefinitionBuilder addActor(final String name, final boolean initiator) {
        return new ActorDefinitionBuilder(getProcessBuilder(), process, name, initiator);
    }

    public ActorDefinitionBuilder setActorInitiator(final String actorName) {
        return new ActorDefinitionBuilder(getProcessBuilder(), process, actorName, true);
    }

    public DocumentDefinitionBuilder addDocumentDefinition(final String name, final String fileName) {
        return new DocumentDefinitionBuilder(getProcessBuilder(), (FlowElementContainerDefinitionImpl) process.getProcessContainer(), name, fileName);
    }

    DesignProcessDefinitionImpl getProcessDefinition() {
        return process;
    }

}
