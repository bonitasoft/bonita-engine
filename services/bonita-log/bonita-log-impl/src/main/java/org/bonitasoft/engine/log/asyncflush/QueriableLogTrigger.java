/**
 * Copyright (C) 2011 BonitaSoft S.A.
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
package org.bonitasoft.engine.log.asyncflush;

import java.util.Date;

import org.bonitasoft.engine.scheduler.trigger.Trigger;

/**
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class QueriableLogTrigger implements Trigger {

    private static final String GROUP_NAME = "logOneShotTrigger";

    private final Date startDate;

    private static final int DEFAULT_PRIORITY = 2;

    private final String name;

    public QueriableLogTrigger(final String name) {
        this.startDate = new Date();
        this.name = name;
    }

    public String getGroupName() {
        return GROUP_NAME;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Date getStartDate() {
        return this.startDate;
    }

    @Override
    public int getPriority() {
        return DEFAULT_PRIORITY;
    }

}
