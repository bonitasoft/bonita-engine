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
public interface CronTrigger extends Trigger {

    /**
     * Returns the expression as a unix-like cron expressions
     * "* * * * * ?"
     * 1. seconds
     * 2. minutes
     * 3. hours
     * 4. day of the month
     * 5. month
     * 6 day of the week
     * 7 year (optional)
     * 
     * @return a unix-like CRON expression
     */
    String getExpression();

    /**
     * Returns when the trigger ends. It can be null which means that the trigger never ends
     * 
     * @return
     */
    Date getEndDate();

}
