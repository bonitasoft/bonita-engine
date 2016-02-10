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
public class UnixCronTrigger extends OneShotTrigger implements CronTrigger {

    private final String expression;

    private final Date endDate;

    public UnixCronTrigger(final String name, final Date startDate, final String expression) {
        super(name, startDate);
        this.expression = expression;
        this.endDate = null;
    }

    public UnixCronTrigger(final String name, final Date startDate, final String expression, final MisfireRestartPolicy misfireHandlingPolicy) {
        super(name, startDate, misfireHandlingPolicy);
        this.expression = expression;
        this.endDate = null;
    }

    @Override
    public String getExpression() {
        return this.expression;
    }

    @Override
    public Date getEndDate() {
        return this.endDate;
    }

}
