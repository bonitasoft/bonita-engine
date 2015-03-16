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
public class OneExecutionTrigger implements Trigger {

    private final String name;

    private final Date startDate;

    private final int priority;

    private final MisfireRestartPolicy misfireHandlingPolicy;

    public OneExecutionTrigger(final String name, final Date startDate, final int priority) {
        this(name, startDate, priority, MisfireRestartPolicy.ALL);
    }

    public OneExecutionTrigger(final String name, final Date startDate, final int priority, final MisfireRestartPolicy misfireHandlingPolicy) {
        this.name = name;
        this.startDate = startDate;
        this.priority = priority;
        this.misfireHandlingPolicy = misfireHandlingPolicy;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Date getStartDate() {
        return startDate;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public MisfireRestartPolicy getMisfireHandlingPolicy() {
        return misfireHandlingPolicy;
    }

}
