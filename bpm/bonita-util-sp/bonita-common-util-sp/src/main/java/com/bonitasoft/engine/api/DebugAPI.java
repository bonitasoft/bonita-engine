/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package com.bonitasoft.engine.api;

import java.util.List;

import org.bonitasoft.engine.exception.BonitaReadException;
import org.bonitasoft.engine.exception.InvalidSessionException;

import com.bonitasoft.engine.bpm.model.breakpoint.Breakpoint;
import com.bonitasoft.engine.bpm.model.breakpoint.BreakpointCriterion;
import com.bonitasoft.engine.exception.BreakpointCreationException;
import com.bonitasoft.engine.exception.BreakpointDeletionException;
import com.bonitasoft.engine.exception.BreakpointNotFoundException;

/**
 * Debug processes by setting breakpoint that will stop the execution of elements in a scope and a state defined
 * <p>
 * Breakpoints can be added and removed here. Once an element is interrupted, it can be resumed of executed step by step using
 * {@link ProcessAPI#executeActivity(long)} or {@link ProcessAPI#executeActivityStepByStep(long)}
 * 
 * @author Baptiste Mesta
 */
public interface DebugAPI {

    /**
     * Add a Breakpoint on a single process instance
     * 
     * @param definitionId
     *            id of the process definition that is concerned
     * @param instanceId
     *            id of the instance of this process definition to add a breakpoint on
     * @param elementName
     *            name of the element to interrupt
     * @param idOfTheStateToInterrupt
     *            the element will be interrupted when the element is in this state
     * @param idOfTheInterruptingState
     *            the element will be put in this state when it is interrupted
     * @return
     *         the id of the breakpoint that was added
     * @throws InvalidSessionException
     * @throws BreakpointCreationException
     * @since 6.0
     */
    long addBreakpoint(long definitionId, long instanceId, String elementName, int idOfTheStateToInterrupt, int idOfTheInterruptingState)
            throws InvalidSessionException, BreakpointCreationException;

    /**
     * Add a Breakpoint on all instance of a process definition
     * 
     * @param definitionId
     *            id of the process definition that is concerned
     * @param elementName
     *            name of the element to interrupt
     * @param idOfTheStateToInterrupt
     *            the element will be interrupted when the element is in this state
     * @param idOfTheInterruptingState
     *            the element will be put in this state when it is interrupted
     * @return
     *         the id of the breakpoint that was added
     * @throws InvalidSessionException
     * @throws BreakpointCreationException
     * @since 6.0
     */
    long addBreakpoint(long definitionId, String elementName, int idOfTheStateToInterrupt, int idOfTheInterruptingState) throws InvalidSessionException,
            BreakpointCreationException;

    /**
     * Remove a breakpoint
     * 
     * @param id
     *            of the breakpoint to remove
     * @throws InvalidSessionException
     * @throws BreakpointDeletionException
     * @throws BreakpointNotFoundException
     * @since 6.0
     */
    void removeBreakpoint(long id) throws InvalidSessionException, BreakpointDeletionException, BreakpointNotFoundException;

    /**
     * Return breakpoints currently set
     * 
     * @param pageNumber
     * @param numberPerPage
     * @param sort
     * @return
     *         a list of breakpoints
     * @throws InvalidSessionException
     * @throws BonitaReadException
     * @since 6.0
     */
    List<Breakpoint> getBreakpoints(int pageNumber, int numberPerPage, BreakpointCriterion sort) throws InvalidSessionException, BonitaReadException;
}
