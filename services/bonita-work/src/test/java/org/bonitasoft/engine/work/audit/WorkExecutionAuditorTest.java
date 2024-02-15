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

import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.work.audit.ExecutionStatus.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.stream.IntStream;

import org.bonitasoft.engine.commons.time.FixedEngineClock;
import org.bonitasoft.engine.work.WorkDescriptor;
import org.bonitasoft.engine.work.audit.WorkExecutionAuditor.ExecutionCountCheckConfig;
import org.bonitasoft.engine.work.audit.WorkExecutionAuditor.RegistrationDurationElapsedCheckConfig;
import org.junit.Test;
import org.mockito.InOrder;

public class WorkExecutionAuditorTest {

    // =================================================================================================================
    // execution status computation
    // =================================================================================================================

    @Test
    public void should_execution_status_be_ok_when_no_threshold_is_reached() {
        // given:
        WorkExecutionAuditor auditor = new WorkExecutionAuditor(new FixedEngineClock(now()), new AuditListener(),
                new RegistrationDurationElapsedCheckConfig(3, DAYS), new ExecutionCountCheckConfig(50, 3, DAYS));

        // when:
        final ExecutionStatus executionStatus = auditor.executionStatus(workDescriptor(now(), 10));

        // then:
        assertThat(executionStatus).isEqualTo(OK);
    }

    @Test
    public void should_execution_status_be_ko_when_a_large_number_of_executions_have_been_performed() {
        // given:
        WorkExecutionAuditor auditor = new WorkExecutionAuditor(new FixedEngineClock(now()), new AuditListener(),
                new RegistrationDurationElapsedCheckConfig(3, DAYS), new ExecutionCountCheckConfig(50, 3, MILLIS));

        // when:
        final ExecutionStatus executionStatus = auditor.executionStatus(workDescriptor(now().minus(12, MINUTES), 200));

        // then:
        assertThat(executionStatus).isEqualTo(TOO_MANY_EXECUTIONS);
    }

    @Test
    public void should_execution_status_be_ok_when_a_large_number_of_executions_have_been_performed_but_execution_count_duration_threshold_is_not_elapsed() {
        // given:
        WorkExecutionAuditor auditor = new WorkExecutionAuditor(new FixedEngineClock(now()), new AuditListener(),
                new RegistrationDurationElapsedCheckConfig(3, DAYS), new ExecutionCountCheckConfig(50, 3, HOURS));

        // when:
        final ExecutionStatus executionStatus = auditor.executionStatus(workDescriptor(now().minus(12, MINUTES), 200));

        // then:
        assertThat(executionStatus).isEqualTo(OK);
    }

    @Test
    public void should_execution_status_be_ko_when_too_much_time_elapsed_since_work_registration() {
        // given:
        final Instant registrationDate = now().minus(12, MINUTES);
        WorkExecutionAuditor auditor = new WorkExecutionAuditor(new FixedEngineClock(registrationDate.plusSeconds(250)),
                new AuditListener(), new RegistrationDurationElapsedCheckConfig(30, MILLIS),
                new ExecutionCountCheckConfig(50, 3, DAYS));
        // when:
        final ExecutionStatus executionStatus = auditor.executionStatus(workDescriptor(registrationDate, 1));

        // then:
        assertThat(executionStatus).isEqualTo(TOO_MUCH_TIME_ELAPSED_SINCE_REGISTRATION);
    }

    // =================================================================================================================
    // detection of abnormal execution
    // =================================================================================================================

    @Test
    public void should_trigger_listener_when_execution_status_is_abnormal() {
        //given:
        final AuditListener auditListener = mock(AuditListener.class);
        WorkExecutionAuditor auditor = auditorDetectingTooMuchExecutions(auditListener);
        final WorkDescriptor work = workDescriptor(now().minus(12, MINUTES), 10_000);

        //when:
        auditor.detectAbnormalExecutionAndNotify(work);

        //then:
        final InOrder inOrder = inOrder(auditListener);
        inOrder.verify(auditListener).detectionStarted(work);
        inOrder.verify(auditListener).abnormalExecutionStatusDetected(work, TOO_MANY_EXECUTIONS);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_not_trigger_listener_when_work_has_already_been_detected_with_abnormal_execution() {
        //given:
        final AuditListener auditListener = mock(AuditListener.class);
        WorkExecutionAuditor auditor = auditorDetectingTooMuchExecutions(auditListener);
        final WorkDescriptor work = workDescriptor();

        //when:
        auditor.detectAbnormalExecutionAndNotify(work);

        //then:
        final InOrder inOrder1stRun = inOrder(auditListener);
        inOrder1stRun.verify(auditListener).detectionStarted(work);
        inOrder1stRun.verify(auditListener).abnormalExecutionStatusDetected(work, TOO_MANY_EXECUTIONS);
        inOrder1stRun.verifyNoMoreInteractions();

        //when:
        reset(auditListener); // forget about previous calls
        auditor.detectAbnormalExecutionAndNotify(work);

        //then:
        final InOrder inOrder2ndRun = inOrder(auditListener);
        inOrder2ndRun.verify(auditListener).detectionStarted(work);
        inOrder2ndRun.verifyNoMoreInteractions();
    }

    // =================================================================================================================
    // notify success
    // =================================================================================================================

    @Test
    public void should_not_notify_success_when_execution_is_normal() {
        //given:
        final AuditListener auditListener = mock(AuditListener.class);
        final WorkDescriptor work = abnormallyExecutedWorkDescriptor();

        //when:
        auditorDetectingTooMuchExecutions(auditListener).notifySuccess(work);

        //then:
        verify(auditListener).success(work);
        verifyNoMoreInteractions(auditListener);
    }

    @Test
    public void should_notify_success_when_execution_is_normal() {
        //given:
        final AuditListener auditListener = mock(AuditListener.class);

        //when:
        auditorDetectingTooMuchExecutions(auditListener).notifySuccess(workDescriptor());

        //then:
        verifyNoMoreInteractions(auditListener);
    }

    // =================================================================================================================
    // UTILS
    // =================================================================================================================

    private static WorkDescriptor workDescriptor(Instant registrationDate, int executionCount) {
        WorkDescriptor workDescriptor = WorkDescriptor.create("NORMAL");
        workDescriptor.setRegistrationDate(registrationDate);
        IntStream.rangeClosed(1, executionCount).forEach(i -> workDescriptor.incrementExecutionCount());
        return workDescriptor;
    }

    private static WorkDescriptor abnormallyExecutedWorkDescriptor() {
        return workDescriptor().abnormalExecutionDetected();
    }

    private static WorkDescriptor workDescriptor() {
        return workDescriptor(now().minus(10, SECONDS), 3);
    }

    private static WorkExecutionAuditor auditorDetectingTooMuchExecutions(AuditListener auditListener) {
        return new WorkExecutionAuditor(
                new FixedEngineClock(now().plus(250, DAYS)),
                auditListener,
                new RegistrationDurationElapsedCheckConfig(30, MILLIS),
                new ExecutionCountCheckConfig(0, 3, MILLIS));
    }

}
