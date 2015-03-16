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
package org.bonitasoft.engine.scheduler.impl;

import java.sql.Connection;

import org.quartz.JobDetail;
import org.quartz.JobPersistenceException;
import org.quartz.Trigger.CompletedExecutionInstruction;
import org.quartz.impl.jdbcjobstore.JobStoreCMT;
import org.quartz.spi.OperableTrigger;

/**
 * @author Matthieu Chaffotte
 */
public class BonitaJobStoreCMT extends JobStoreCMT {

    @Override
    protected void triggeredJobComplete(final Connection conn, final OperableTrigger trigger, final JobDetail jobDetail,
            final CompletedExecutionInstruction triggerInstCode) throws JobPersistenceException {
        super.triggeredJobComplete(conn, trigger, jobDetail, triggerInstCode);
        if (CompletedExecutionInstruction.SET_TRIGGER_ERROR.equals(triggerInstCode)
                || CompletedExecutionInstruction.SET_ALL_JOB_TRIGGERS_ERROR.equals(triggerInstCode)) {
            // the super method logs already this message but only in info level. (should be logged as error)
            if (!getLog().isInfoEnabled()) {
                getLog().error("All triggers of Job " + trigger.getKey() + " set to ERROR state.");
            }
            getLog().error("In order to restart the triggers, you can either restart your node or call the platformAPI.rescheduleErroneousTriggers method.");
        }
    }

}
