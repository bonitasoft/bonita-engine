/**
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
 */
package org.bonitasoft.engine.jobs;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.STimerEventTriggerInstance;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.scheduler.AbstractBonitaTenantJobListener;
import org.bonitasoft.engine.scheduler.StatelessJob;
import org.bonitasoft.engine.scheduler.exception.SSchedulerException;

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

    public TimerEventTriggerJobListener(final EventInstanceService eventInstanceService, final long tenantId) {
        super(tenantId);
        this.eventInstanceService = eventInstanceService;
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
    public void jobWasExecuted(final Map<String, Serializable> context, final SSchedulerException jobException) {
        final StatelessJob bosJob = (StatelessJob) context.get(BOS_JOB);
        if (bosJob == null) {
            return;
        }

        final String triggerName = (String) context.get(TRIGGER_NAME);
        try {
            deleteTimerEventTriggerIfJobNotScheduledAnyMore(triggerName);
        } catch (final SBonitaException sbe) {
            // Nothing to do
        }
    }

    void deleteTimerEventTriggerIfJobNotScheduledAnyMore(final String triggerName) throws SBonitaException {
        final List<FilterOption> filters = Collections.singletonList(new FilterOption(STimerEventTriggerInstance.class, "jobTriggerName", triggerName));

        final QueryOptions queryOptions = new QueryOptions(0, 1, Collections.<OrderByOption> emptyList(), filters, null);
        final List<STimerEventTriggerInstance> timerEventTriggerInstances = eventInstanceService.searchEventTriggerInstances(STimerEventTriggerInstance.class,
                queryOptions);
        if (!timerEventTriggerInstances.isEmpty()) {
            eventInstanceService.deleteEventTriggerInstance(timerEventTriggerInstances.get(0));
        }
    }

}
