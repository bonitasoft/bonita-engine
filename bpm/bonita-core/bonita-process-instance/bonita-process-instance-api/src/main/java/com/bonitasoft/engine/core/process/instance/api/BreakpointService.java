/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
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
package com.bonitasoft.engine.core.process.instance.api;

import java.util.List;

import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.SBonitaReadException;

import com.bonitasoft.engine.core.process.instance.api.exceptions.SBreakpointCreationException;
import com.bonitasoft.engine.core.process.instance.api.exceptions.SBreakpointDeletionException;
import com.bonitasoft.engine.core.process.instance.api.exceptions.SBreakpointNotFoundException;
import com.bonitasoft.engine.core.process.instance.model.breakpoint.SBreakpoint;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public interface BreakpointService {

    String NEW_BREAKPOINT_ADDED = "New Breakpoint added";

    String BREAKPOINT = "BREAKPOINT";

    String REMOVING_BREAKPOINT = "Removing breakpoint";

    SBreakpoint addBreakpoint(SBreakpoint breakpoint) throws SBreakpointCreationException;

    void removeBreakpoint(long id) throws SBreakpointDeletionException, SBreakpointNotFoundException, SBonitaReadException;

    SBreakpoint getBreakpoint(long id) throws SBreakpointNotFoundException, SBonitaReadException;

    boolean isBreakpointActive() throws SBonitaReadException;

    SBreakpoint getBreakPointFor(long definitionId, long instanceId, String elementName, int stateId) throws SBonitaReadException;

    long getNumberOfBreakpoints() throws SBonitaReadException;

    List<SBreakpoint> getBreakpoints(int fromIndex, int maxResults, String sortingField, OrderByType sortingOrder) throws SBonitaReadException;

}
