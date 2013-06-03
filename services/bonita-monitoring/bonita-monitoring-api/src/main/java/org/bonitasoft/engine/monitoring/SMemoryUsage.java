/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.monitoring;

import java.lang.management.MemoryUsage;

/**
 * @see MemoryUsage
 * @author Matthieu Chaffotte
 */
public interface SMemoryUsage {

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
