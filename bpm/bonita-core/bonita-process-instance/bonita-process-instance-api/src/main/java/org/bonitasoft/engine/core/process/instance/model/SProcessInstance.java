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
package org.bonitasoft.engine.core.process.instance.model;

import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;

/**
 * @author Elias Ricken de Medeiros
 * @author Yanyan Liu
 * @author Matthieu Chaffotte
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public interface SProcessInstance extends SFlowElementsContainer {

    long getProcessDefinitionId();

    String getDescription();

    int getStateId();

    long getStartDate();

    /**
     * @return id of the user who originally started the process
     * @since 6.0.1
     */
    long getStartedBy();

    /**
     * @return id of the user (delegate) who started the process for the original starter
     * @since 6.0.1
     */
    long getStartedBySubstitute();

    long getEndDate();

    long getLastUpdate();

    @Deprecated
    long getContainerId();

    long getRootProcessInstanceId();

    /**
     * Get the id of the call activity that started the process instance.
     * 
     * @return id of the call activity that started the process instance or -1 if the process instance was not started by a call activity
     */
    long getCallerId();

    /**
     * Get the caller's SFlowNodeType.
     * 
     * @return the caller's SFlowNodeType if the it's called by a call activity or sub-process, null otherwise
     */
    SFlowNodeType getCallerType();

    /**
     * Get the id of the end error event that interrupted the process instance.
     * 
     * @return the id of the end error event that interrupted the process instance or -1 if the process was not interrupted by a end error event
     */
    long getInterruptingEventId();

    SStateCategory getStateCategory();

    long getMigrationPlanId();

    String getStringIndex1();

    String getStringIndex2();

    String getStringIndex3();

    String getStringIndex4();

    String getStringIndex5();

    boolean hasBeenInterruptedByEvent();
}
