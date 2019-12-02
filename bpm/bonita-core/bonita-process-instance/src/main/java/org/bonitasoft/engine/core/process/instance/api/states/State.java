/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.core.process.instance.api.states;

import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityStateExecutionException;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;

/**
 * @author Baptiste Mesta
 */
public interface State<T> {

    int ID_ACTIVITY_EXECUTING = 1;
    int ID_ACTIVITY_READY = 4;
    int ID_ACTIVITY_FAILED = 3;


    StateCode execute(SProcessDefinition processDefinition, T instance) throws SActivityStateExecutionException;

    /**
     *
     * Called when a children of the flow node parentInstance finish
     *
     * @return
     *         true if the state is finished (the execution will continue automatically)
     */
    boolean hit(SProcessDefinition processDefinition, T parentInstance, T childInstance) throws SActivityStateExecutionException;

    int getId();

    String getName();

    /**
     * @return true if the state is an interrupting state
     */
    boolean isInterrupting();

    /**
     * @return true if the state is stable
     *         a final state is stable
     */
    boolean isStable();

    /**
     * Checks whether the state is a terminal one.
     * 
     * @return true is the state is a terminal one; false otherwise
     */
    boolean isTerminal();

    /**
     * Get the state's category
     * 
     * @return the state's category
     */
    SStateCategory getStateCategory();
}
