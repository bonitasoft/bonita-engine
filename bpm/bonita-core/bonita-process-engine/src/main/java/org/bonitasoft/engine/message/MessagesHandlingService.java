/**
 * Copyright (C) 2016 Bonitasoft S.A.
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

package org.bonitasoft.engine.message;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.TenantLifecycleService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SMessageInstanceReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SMessageModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SWaitingEventModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SWaitingEventReadException;
import org.bonitasoft.engine.core.process.instance.model.builder.event.handling.SMessageInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.handling.SWaitingMessageEventBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SBPMEventType;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageEventCouple;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingMessageEvent;
import org.bonitasoft.engine.execution.work.BPMWorkFactory;
import org.bonitasoft.engine.lock.BonitaLock;
import org.bonitasoft.engine.lock.LockService;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.monitoring.ObservableExecutor;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.BonitaTransactionSynchronization;
import org.bonitasoft.engine.transaction.STransactionNotFoundException;
import org.bonitasoft.engine.transaction.TransactionState;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.bonitasoft.engine.work.SWorkRegisterException;
import org.bonitasoft.engine.work.WorkService;

/**
 * @author Baptiste Mesta
 */
public class MessagesHandlingService implements TenantLifecycleService, ObservableExecutor {

    private static final int MAX_COUPLES = 1000;
    private static final String LOCK_TYPE = "EVENTS";
    private ThreadPoolExecutor threadPoolExecutor;
    private EventInstanceService eventInstanceService;
    private WorkService workService;
    private TechnicalLoggerService loggerService;
    private LockService lockService;
    private Long tenantId;
    private UserTransactionService userTransactionService;
    private SessionAccessor sessionAccessor;
    private BPMWorkFactory workFactory;

    private AtomicLong executedMessages = new AtomicLong();

    public MessagesHandlingService(EventInstanceService eventInstanceService, WorkService workService, TechnicalLoggerService loggerService,
            LockService lockService, Long tenantId, UserTransactionService userTransactionService,
            SessionAccessor sessionAccessor, BPMWorkFactory workFactory) {
        this.eventInstanceService = eventInstanceService;
        this.workService = workService;
        this.loggerService = loggerService;
        this.lockService = lockService;
        this.tenantId = tenantId;
        this.userTransactionService = userTransactionService;
        this.sessionAccessor = sessionAccessor;
        this.workFactory = workFactory;
    }

    @Override
    public void start() {
        log(TechnicalLogSeverity.INFO, "Starting thread that handle messages.");
        threadPoolExecutor = new ThreadPoolExecutor(1, 1, 1L, TimeUnit.HOURS,
                new ArrayBlockingQueue<>(5),
                r -> new Thread(r, "Bonita-Message-Matching"),
                (r, executor) -> log(TechnicalLogSeverity.DEBUG, "Message matching queue capacity reached."));
    }

    @Override
    public void stop() {
        if (threadPoolExecutor == null) {
            log(TechnicalLogSeverity.INFO, "Thread that handle messages is already stopped.");
            return;
        }
        log(TechnicalLogSeverity.INFO, "Stopping thread that handle messages...");
        threadPoolExecutor.shutdown();
        try {
            boolean termination = threadPoolExecutor.awaitTermination(5000, TimeUnit.MILLISECONDS);
            if (!termination) {
                log(TechnicalLogSeverity.WARNING,
                        "Failed to terminate the thread that handle messages matching. This will not have functional impacts but it might produce warnings on server shutdown.");
            }
        } catch (InterruptedException ignored) {
        }
        threadPoolExecutor = null;
        log(TechnicalLogSeverity.INFO, "Thread that handle messages successfully stopped.");
    }

    @Override
    public void pause() {
        stop();
    }

    @Override
    public void resume() {
        start();
    }

    public void triggerMatchingOfMessages() throws STransactionNotFoundException {
        if (threadPoolExecutor == null) {
            log(TechnicalLogSeverity.WARNING, "Cannot match events when service is stopped. Maybe the engine is not yet started.");
            return;
        }
        userTransactionService.registerBonitaSynchronization(new RegisterMatchingOfEventSynchronization());
    }

    private void matchEventCoupleAndTriggerExecution() throws Exception {
        userTransactionService.executeInTransaction(() -> {
            final List<SMessageEventCouple> potentialMessageCouples = eventInstanceService.getMessageEventCouples(0,
                    MAX_COUPLES);
            final List<SMessageEventCouple> uniqueCouples = getMessageUniqueCouples(potentialMessageCouples);
            executeUniqueMessageCouplesWork(uniqueCouples);
            if (!uniqueCouples.isEmpty()) {
                log(TechnicalLogSeverity.INFO, "Triggered execution of " + uniqueCouples.size() + " event couples");
            } else {
                log(TechnicalLogSeverity.DEBUG,
                        "Executed thread to match event couples, but there is nothing to match");
            }
            if (potentialMessageCouples.size() == MAX_COUPLES) {
                log(TechnicalLogSeverity.DEBUG,
                        "There is more than " + MAX_COUPLES + " event to match. will retrigger the execution now.");
                triggerMatchingOfMessages();
            }
            return null;
        });
    }

    private void log(TechnicalLogSeverity severity, String message) {
        loggerService.log(MessagesHandlingService.class, severity, message);
    }

    private void executeUniqueMessageCouplesWork(final List<SMessageEventCouple> uniqueCouples) throws SBonitaException {
        for (final SMessageEventCouple couple : uniqueCouples) {
            executeMessageCouple(couple.getMessageInstanceId(), couple.getWaitingMessageId());
        }
    }

    void executeMessageCouple(long messageInstanceId, long waitingMessageId) throws SWaitingEventReadException, SMessageInstanceReadException,
            SMessageModificationException, SWaitingEventModificationException, SWorkRegisterException {

        // Mark messages that will be treated as "treatment in progress":
        final SWaitingMessageEvent waitingMsg = eventInstanceService.getWaitingMessage(waitingMessageId);
        final SMessageInstance messageInstance = eventInstanceService.getMessageInstance(messageInstanceId);
        markMessageAsInProgress(messageInstance);

        //EVENT_SUB_PROCESS of type non-interrupted should be considered as well, as soon as we support them
        if (!SBPMEventType.START_EVENT.equals(waitingMsg.getEventType())) {
            markWaitingMessageAsInProgress(waitingMsg);
        }
        executedMessages.incrementAndGet();
        workService.registerWork(workFactory.createExecuteMessageCoupleWorkDescriptor(messageInstance, waitingMsg));
    }

    /**
     * From a list of couples that may contain duplicate waiting message candidates, select only one waiting message for each message instance: the first
     * matching waiting message is arbitrary chosen.
     * In the case of <code>SWaitingMessageEvent</code> of types {@link SBPMEventType#START_EVENT} or {@link SBPMEventType#EVENT_SUB_PROCESS}, it can be
     * selected several times to trigger multiple instances.
     *
     * @param potentialMessageCouples all the possible couples that match the potential correlation.
     * @return the reduced list of couple, where we insure that a unique message instance is associated with a unique waiting message.
     */
    List<SMessageEventCouple> getMessageUniqueCouples(List<SMessageEventCouple> potentialMessageCouples) {
        final List<Long> takenMessages = new ArrayList<>();
        final List<Long> takenWaitings = new ArrayList<>();
        final List<SMessageEventCouple> uniqueMessageCouples = new ArrayList<>();
        for (final SMessageEventCouple couple : potentialMessageCouples) {
            final long messageInstanceId = couple.getMessageInstanceId();
            final long waitingMessageId = couple.getWaitingMessageId();
            final SBPMEventType waitingMessageEventType = couple.getWaitingMessageEventType();
            if (!takenMessages.contains(messageInstanceId) && !takenWaitings.contains(waitingMessageId)) {
                takenMessages.add(messageInstanceId);
                // Starting events and Starting event sub-processes must not be considered as taken if they appear several times
                //EVENT_SUB_PROCESS of type non-interrupted should be considered as well, as soon as we support them
                if (!SBPMEventType.START_EVENT.equals(waitingMessageEventType)) {
                    takenWaitings.add(waitingMessageId);
                }
                uniqueMessageCouples.add(couple);
            }
        }
        return uniqueMessageCouples;
    }

    private void markMessageAsInProgress(final SMessageInstance messageInstance) throws SMessageModificationException {
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(SMessageInstanceBuilder.HANDLED, true);
        eventInstanceService.updateMessageInstance(messageInstance, descriptor);
    }

    private void markWaitingMessageAsInProgress(final SWaitingMessageEvent waitingMsg) throws SWaitingEventModificationException {
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(BuilderFactory.get(SWaitingMessageEventBuilderFactory.class).getProgressKey(),
                SWaitingMessageEventBuilderFactory.PROGRESS_IN_TREATMENT_KEY);
        eventInstanceService.updateWaitingMessage(waitingMsg, descriptor);
    }

    public void resetMessageCouple(long messageInstanceId, long waitingMessageId)
            throws SWaitingEventReadException, SWaitingEventModificationException, SMessageModificationException, SMessageInstanceReadException {
        resetWaitingMessage(waitingMessageId);
        resetMessageInstance(messageInstanceId);
    }

    private void resetMessageInstance(final long messageInstanceId) throws SMessageModificationException, SMessageInstanceReadException {
        final SMessageInstance messageInstance = eventInstanceService.getMessageInstance(messageInstanceId);
        if (messageInstance == null) {
            log(TechnicalLogSeverity.WARNING, "Unable to reset message instance " + messageInstanceId + " because it is not found.");
            return;
        }
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(SMessageInstanceBuilder.HANDLED, false);
        eventInstanceService.updateMessageInstance(messageInstance, descriptor);
    }

    private void resetWaitingMessage(final long waitingMessageId) throws SWaitingEventModificationException, SWaitingEventReadException {
        final SWaitingMessageEvent waitingMsg = eventInstanceService.getWaitingMessage(waitingMessageId);
        if (waitingMsg == null) {
            log(TechnicalLogSeverity.WARNING, "Unable to reset waiting message " + waitingMessageId + " because it is not found.");
            return;
        }
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(BuilderFactory.get(SWaitingMessageEventBuilderFactory.class).getProgressKey(),
                SWaitingMessageEventBuilderFactory.PROGRESS_FREE_KEY);
        eventInstanceService.updateWaitingMessage(waitingMsg, descriptor);
    }

    @Override
    public long getPendings() {
        return 0;
    }

    @Override
    public long getRunnings() {
        return 0;
    }

    @Override
    public long getExecuted() {
        return executedMessages.get();
    }

    private class MatchEventCallable implements Callable<Void> {

        @Override
        public Void call() throws Exception {
            try {
                log(TechnicalLogSeverity.DEBUG, "start matching of messages");
                //we use a lock in order to have only one execution at a time even in cluster
                BonitaLock eventLock = lockService.tryLock(1L, LOCK_TYPE, 1L, TimeUnit.MILLISECONDS, tenantId);
                if (eventLock == null) {
                    //It could happen that some messaged where still not triggered because the work that is currently executing was started after the last message execution
                    log(TechnicalLogSeverity.DEBUG, "triggered the message event handling work but was already running");
                    return null;
                }
                try {
                    sessionAccessor.setTenantId(tenantId);
                    matchEventCoupleAndTriggerExecution();
                } finally {
                    lockService.unlock(eventLock, tenantId);
                }
            } catch (Exception e) {
                loggerService.log(MessagesHandlingService.class, TechnicalLogSeverity.ERROR, "error while matching events", e);
                throw e;
            }
            return null;
        }
    }

    private class RegisterMatchingOfEventSynchronization implements BonitaTransactionSynchronization {

        @Override
        public void beforeCommit() {
        }

        @Override
        public void afterCompletion(TransactionState txState) {
            log(TechnicalLogSeverity.DEBUG, "transaction triggered the matching of events.");
            threadPoolExecutor.submit(new MatchEventCallable());
        }
    }
}
