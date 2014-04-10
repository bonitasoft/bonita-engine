/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.bpm.process;

import java.util.List;
import java.util.Set;

import org.bonitasoft.engine.bpm.actor.ActorDefinition;
import org.bonitasoft.engine.bpm.flownode.impl.FlowElementContainerDefinition;
import org.bonitasoft.engine.bpm.parameter.ParameterDefinition;
import org.bonitasoft.engine.expression.Expression;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public interface DesignProcessDefinition extends ProcessDefinition {

    String getDisplayName();

    String getDisplayDescription();

    FlowElementContainerDefinition getProcessContainer();

    Set<ParameterDefinition> getParameters();

    /**
     * @return A set of ActorDefinition
     * @see #getActorsList()
     * @since 6.0
     * @deprecated As of release 6.1, replaced by {@link #getActorsList()}
     */
    @Deprecated
    Set<ActorDefinition> getActors();

    /**
     * @return A list of ActorDefinition
     * @since 6.1
     */
    List<ActorDefinition> getActorsList();

    ActorDefinition getActorInitiator();

    String getStringIndexLabel(int index);

    Expression getStringIndexValue(int index);

}
