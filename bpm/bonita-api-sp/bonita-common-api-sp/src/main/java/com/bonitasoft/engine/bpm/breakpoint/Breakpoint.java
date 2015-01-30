/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bpm.breakpoint;

import org.bonitasoft.engine.bpm.BaseElement;

/**
 * This is used to debug a process instance using the commands {@link AddBreakpointCommand}, {@link GetBreakpointsCommand} and {@link RemoveBreakpointCommand}.
 * 
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public interface Breakpoint extends BaseElement {

    /**
     * @return The identifier of the process definition
     */
    long getDefinitionId();

    /**
     * @return The identifier of the process instance
     */
    long getInstanceId();

    /**
     * @return If the instance is scope
     */
    boolean isInstanceScope();

    /**
     * @return The element name
     */
    String getElementName();

    /**
     * @return The identifier of the state
     */
    int getStateId();

    /**
     * @return The identifier of the interrupted state
     */
    int getInterruptedStateId();

}
