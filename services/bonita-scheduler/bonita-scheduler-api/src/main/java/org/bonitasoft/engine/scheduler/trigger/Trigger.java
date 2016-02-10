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
package org.bonitasoft.engine.scheduler.trigger;

import java.util.Date;

/**
 * @author Matthieu Chaffotte
 */
public interface Trigger {

    /**
     * Specify what to do when some job were not triggered in time.
     * 
     */
    public enum MisfireRestartPolicy {
        /**
         * Restart now all instances that were missed
         */
        ALL,

        /**
         * Restart now only one instance that was not trigger and continue normally
         */
        ONE,

        /**
         * No instance that were not trigger in time are restarted
         */
        NONE
    }

    /**
     * Gets the name of the trigger
     * 
     * @return the name of the trigger
     * @since 6.0
     */
    String getName();

    /**
     * Returns when the trigger must start
     * 
     * @return a date when the trigger must start
     * @since 6.0
     */
    Date getStartDate();

    /**
     * The trigger of the highest priority will be executed first.
     * 
     * @return the trigger's priority
     * @since 6.0
     */
    int getPriority();

    /**
     * 
     * Tell the scheduler how to handle jobs that were not executed in time.
     * 
     * @return the MisfireHandlingPolicy for this trigger
     */
    MisfireRestartPolicy getMisfireHandlingPolicy();

}
