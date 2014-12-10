/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
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
 ******************************************************************************/

package org.bonitasoft.engine.api;

/**
 * <code>FlownodeCounters</code> object gives access to different counters on flownodes on a given running process instance. It does not count the flownodes of
 * sub-process instances of the given process instance.
 * 
 * @author Emmanuel Duchastenier
 */
public class FlownodeCounters {

    private long numberOfFailedFlownodes;

    private long numberOfReadyTasks;

    private long numberOfCompletedFlownodes;

    /**
     * Retrieves the number of all failed flownodes on a given process instance.
     * 
     * @return the total number of failed flownodes.
     */
    public long getNumberOfFailedFlownodes() {
        return numberOfFailedFlownodes;
    }

    public void setNumberOfFailedFlownodes(long numberOfFailedFlownodes) {
        this.numberOfFailedFlownodes = numberOfFailedFlownodes;
    }

    /**
     * Retrieves the number of all ready tasks on a given process instance.
     *
     * @return the total number of ready tasks.
     */
    public long getNumberOfReadyTasks() {
        return numberOfReadyTasks;
    }

    public void setNumberOfReadyTasks(long numberOfReadyTasks) {
        this.numberOfReadyTasks = numberOfReadyTasks;
    }

    /**
     * Retrieves the number of all successfully completed flownodes on a given process instance.
     *
     * @return the total number of successfully completed flownodes.
     */
    public long getNumberOfCompletedFlownodes() {
        return numberOfCompletedFlownodes;
    }

    public void setNumberOfCompletedFlownodes(long numberOfCompletedFlownodes) {
        this.numberOfCompletedFlownodes = numberOfCompletedFlownodes;
    }

}
