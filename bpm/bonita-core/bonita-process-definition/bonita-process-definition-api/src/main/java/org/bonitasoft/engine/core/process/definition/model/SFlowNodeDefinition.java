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
package org.bonitasoft.engine.core.process.definition.model;

import java.util.List;

import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Feng Hui
 * @author Zhao Na
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public interface SFlowNodeDefinition extends SNamedElement {

    SFlowElementContainerDefinition getParentContainer();

    /**
     * Gets the outgoing transitions of the activity.
     * 
     * @return the outgoing transitions of the activity
     */
    List<STransitionDefinition> getOutgoingTransitions();

    /**
     * Gets the incoming transitions of the activity.
     * 
     * @return the incoming transitions of the activity
     */
    List<STransitionDefinition> getIncomingTransitions();

    /**
     * Checks whether the activity has outgoing transitions.
     * 
     * @return true if the activity contains outgoing transitions; false otherwise;
     */
    boolean hasOutgoingTransitions();

    /**
     * Checks whether the activity contains incoming transitions.
     * 
     * @return true if the activity contains incoming transitions; false otherwise
     */
    boolean hasIncomingTransitions();

    STransitionDefinition getDefaultTransition();

    List<SConnectorDefinition> getConnectors();

    /**
     * 
     * @return
     * @since 6.3
     */
    boolean hasConnectors();

    /**
     * 
     * @param name
     * @return
     * @since 6.1
     */
    SConnectorDefinition getConnectorDefinition(String name);

    SFlowNodeType getType();

    String getDescription();

    SExpression getDisplayDescription();

    SExpression getDisplayDescriptionAfterCompletion();

    SExpression getDisplayName();

    List<SConnectorDefinition> getConnectors(ConnectorEvent connectorEvent);

    int getTransitionIndex(String transitionName);

    boolean isStartable();

    boolean isParalleleOrInclusive();

    boolean isExclusive();

    boolean isInterrupting();

    boolean isBoundaryEvent();

    boolean isEventSubProcess();

}
