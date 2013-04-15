/*******************************************************************************
 * Copyright (C) 2012, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
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
