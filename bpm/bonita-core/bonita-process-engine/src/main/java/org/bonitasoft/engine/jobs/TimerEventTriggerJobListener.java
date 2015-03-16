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
package org.bonitasoft.engine.jobs;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.STimerEventTriggerInstance;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.scheduler.AbstractBonitaTenantJobListener;
import org.bonitasoft.engine.scheduler.StatelessJob;

/**
 * This listener allows to delete the {@link org.bonitasoft.engine.core.process.instance.model.event.trigger.STimerEventTriggerInstance} in the table
 * <code>event_trigger_instance</code> after the execution of the job/trigger, if the trigger is not a cycle/cron.
 *
 * @author Celine Souchet
 * @version 6.4.0
 * @since 6.4.0
 */
public class TimerEventTriggerJobListener extends AbstractBonitaTenantJobListener {

    private static final long serialVersionUID = -5060516371371295271L;

    private static final String LISTENER_NAME = "TimerEventTriggerJobListener_";

    private final EventInstanceService eventInstanceService;

    private final TechnicalLoggerService logger;

    public TimerEventTriggerJobListener(final EventInstanceService eventInstanceService, final long tenantId, final TechnicalLoggerService logger) {
        super(tenantId);
        this.eventInstanceService = eventInstanceService;
        this.logger = logger;
    }

    @Override
    public String getName() {
        return LISTENER_NAME + getTenantId();
    }

    @Override
    public void jobToBeExecuted(final Map<String, Serializable> context) {
        // nothing to do
    }

    @Override
    public void jobExecutionVetoed(final Map<String, Serializable> context) {
        // nothing to do
    }

    @Override
    public void jobWasExecuted(final Map<String, Serializable> context, final Exception jobException) {
        final StatelessJob bosJob = (StatelessJob) context.get(BOS_JOB);
        if (bosJob == null || !isTimerEventJob(context)) {
            return;
        }

        final String triggerName = (String) context.get(TRIGGER_NAME);
        try {
            deleteTimerEventTrigger(triggerName);
        } catch (final SBonitaException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.WARNING)) {
                logger.log(this.getClass(), TechnicalLogSeverity.WARNING,
                        "An exception occurs during the deleting of the timer event trigger '" + triggerName + "'.", e);
            }
        }
    }

    private boolean isTimerEventJob(final Map<String, Serializable> context) {
        return context.get(JOB_TYPE).equals(TriggerTimerEventJob.class.getName());
    }

    void deleteTimerEventTrigger(final String triggerName) throws SBonitaException {
        final List<FilterOption> filters = Collections.singletonList(new FilterOption(STimerEventTriggerInstance.class, "jobTriggerName", triggerName));
        final List<OrderByOption> orders = Arrays.asList(new OrderByOption(STimerEventTriggerInstance.class, "id", OrderByType.ASC));

        final QueryOptions queryOptions = new QueryOptions(0, 1, orders, filters, null);
        final List<STimerEventTriggerInstance> timerEventTriggerInstances = eventInstanceService.searchEventTriggerInstances(STimerEventTriggerInstance.class,
                queryOptions);
        if (!timerEventTriggerInstances.isEmpty()) {
            eventInstanceService.deleteEventTriggerInstance(timerEventTriggerInstances.get(0));
        }
    }

}
