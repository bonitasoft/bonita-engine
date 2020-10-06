/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.work.audit;

import static org.bonitasoft.engine.work.audit.ExecutionStatus.*;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import org.bonitasoft.engine.commons.time.EngineClock;
import org.bonitasoft.engine.work.WorkDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WorkExecutionAuditor {

    private static final Logger log = LoggerFactory.getLogger(WorkExecutionAuditor.class);

    private final EngineClock engineClock;
    private final AuditListener auditListener;

    private boolean activated = true;
    private final int executionCountThreshold;
    private final Duration executionCountDurationThreshold;
    private final Duration registrationDurationElapsedThreshold;

    public WorkExecutionAuditor(EngineClock engineClock,
            AuditListener auditListener,
            RegistrationDurationElapsedCheckConfig registrationDurationElapsedCheckConfig,
            ExecutionCountCheckConfig executionCountCheckConfig) {
        this.engineClock = engineClock;
        this.auditListener = auditListener;
        this.registrationDurationElapsedThreshold = registrationDurationElapsedCheckConfig.duration;
        this.executionCountThreshold = executionCountCheckConfig.executionCountThreshold;
        this.executionCountDurationThreshold = executionCountCheckConfig.executionDurationThreshold;
    }

    @Value("${bonita.tenant.work.audit.activated:true}")
    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    /**
     * Notify the listener in case of abnormal execution. Cases considered as 'abnormal execution'
     * <ul>
     * <li>large number of executions after a duration threshold since registration has elapsed</li>
     * <li>large duration since the work has been registered</li>
     * </ul>
     * <b>NOTE</b>: the listener receives the 'abnormal execution' status only once to avoid flooding it.
     *
     * @param work the work descriptor to inspect
     */
    public void detectAbnormalExecutionAndNotify(WorkDescriptor work) {
        if (!activated) {
            return;
        }
        auditListener.detectionStarted(work);
        if (work.getRegistrationDate() == null) {
            log.warn("No registration date available, unable to detect abnormal work execution status. {}", work);
            return;
        }

        if (!work.isAbnormalExecutionDetected()) {
            final ExecutionStatus executionStatus = executionStatus(work);
            if (!executionStatus.isNormalExecution()) {
                work.abnormalExecutionDetected();
                auditListener.abnormalExecutionStatusDetected(work, executionStatus);
            }
        }
    }

    /**
     * Only notify if execution has been detected as abnormal
     */
    public void notifySuccess(WorkDescriptor work) {
        if (work.isAbnormalExecutionDetected()) {
            auditListener.success(work);
        }
    }

    // Visible for Testing
    ExecutionStatus executionStatus(WorkDescriptor work) {
        Duration durationSinceWorkRegistration = Duration.between(work.getRegistrationDate(), engineClock.now());
        if (durationSinceWorkRegistration.compareTo(executionCountDurationThreshold) >= 0
                && work.getExecutionCount() >= executionCountThreshold) {
            return TOO_MANY_EXECUTIONS;
        }
        if (durationSinceWorkRegistration.compareTo(registrationDurationElapsedThreshold) >= 0) {
            return TOO_MUCH_TIME_ELAPSED_SINCE_REGISTRATION;
        }

        return OK;
    }

    @Component
    public static class RegistrationDurationElapsedCheckConfig {

        private final Duration duration;

        public RegistrationDurationElapsedCheckConfig(
                @Value("${bonita.tenant.work.audit.abnormal.execution.threshold.elapsed_duration_since_registration_amount:30}") int amount,
                @Value("${bonita.tenant.work.audit.abnormal.execution.threshold.elapsed_duration_since_registration_unit:MINUTES}") ChronoUnit unit) {
            this.duration = Duration.of(amount, unit);
        }

    }

    @Component
    public static class ExecutionCountCheckConfig {

        private final int executionCountThreshold;
        private final Duration executionDurationThreshold;

        public ExecutionCountCheckConfig(
                @Value("${bonita.tenant.work.audit.abnormal.execution.threshold.execution_count:10}") int executionCountThreshold,
                @Value("${bonita.tenant.work.audit.abnormal.execution.threshold.execution_count_duration_amount:10}") int durationThresholdAmount,
                @Value("${bonita.tenant.work.audit.abnormal.execution.threshold.execution_count_duration_unit:MINUTES}") ChronoUnit durationThresholdUnit) {
            this.executionCountThreshold = executionCountThreshold;
            this.executionDurationThreshold = Duration.of(durationThresholdAmount, durationThresholdUnit);
        }

    }

}
