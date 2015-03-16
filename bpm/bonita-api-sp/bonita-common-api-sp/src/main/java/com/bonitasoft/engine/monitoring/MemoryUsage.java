/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.monitoring;

import java.io.Serializable;

/**
 * @see MemoryUsage
 * @author Matthieu Chaffotte
 */
public interface MemoryUsage extends Serializable {

    /**
     * Returns the amount of memory in bytes that is committed for the Java virtual machine to use.
     * 
     * @return the amount of committed memory in bytes.
     */
    long getCommitted();

    /**
     * Returns the maximum amount of memory in bytes that can be used for memory management. This method returns -1 if the maximum memory size is undefined.
     * This amount of memory is not guaranteed to be available for memory management if it is greater than the amount of committed memory. The Java virtual
     * machine may fail to allocate memory even if the amount of used memory does not exceed this maximum size.
     * 
     * @return the maximum amount of memory in bytes; -1 if undefined.
     */
    long getMax();

    /**
     * Returns the amount of memory in bytes that the Java virtual machine initially requests from the operating system for memory management. This method
     * returns -1 if the initial memory size is undefined.
     * 
     * @return the initial size of memory in bytes; -1 if undefined.
     */
    long getInit();

    /**
     * Returns the amount of used memory in bytes.
     * 
     * @return the amount of used memory in bytes.
     */
    long getUsed();
}
