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
 * @author Feng Hui
 * @author Baptiste Mesta
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public interface SFlowNodeInstance extends SFlowElementInstance {

    int getStateId();

    String getStateName();

    int getPreviousStateId();

    long getReachedStateDate();

    long getLastUpdateDate();

    SFlowNodeType getType();

    String getDisplayName();

    String getDisplayDescription();

    int getLoopCounter();

    /**
     * @return id of the user who originally executed the flow node
     * @since 6.0.1
     */
    long getExecutedBy();

    /**
     * @return id of the user (delegate) who executed the flow node for the original executer
     * @since 6.0.1
     */
    long getExecutedBySubstitute();

    boolean isStateExecuting();

    int getTokenCount();

    long getFlowNodeDefinitionId();
    
    /**
     * @return true if the execution must continues automatically on abort or cancel the parent process instance
     */
    boolean mustExecuteOnAbortOrCancelProcess();
}
