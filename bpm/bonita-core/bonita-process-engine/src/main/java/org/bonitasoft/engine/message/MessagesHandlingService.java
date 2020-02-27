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
package org.bonitasoft.engine.message;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.bonitasoft.engine.api.utils.VisibleForTesting;
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
import org.bonitasoft.engine.log.technical.TechnicalLogger;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
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
public class MessagesHandlingService implements TenantLifecycleService {

    private static final int MAX_COUPLES = 100;
    private static final String LOCK_TYPE = "EVENTS";
    public static final String NUMBER_OF_MESSAGES_EXECUTED = "bonita.bpmengine.message.executed";
    public static final String NUMBER_OF_MESSAGES_POTENTIAL_MATCHED = "bonita.bpmengine.message.potential";
    public static final String NUMBER_OF_MESSAGES_MATCHING_RETRIGGERED_TASKS = "bonita.bpmengine.message.retriggeredtasks";
    private ThreadPoolExecutor threadPoolExecutor;
    private EventInstanceService eventInstanceService;
    private WorkService workService;
    private TechnicalLogger logger;
    private LockService lockService;
    private Long tenantId;
    private UserTransactionService userTransactionService;
    private SessionAccessor sessionAccessor;
    private BPMWorkFactory workFactory;

    private final Counter executedMessagesCounter;
    private final Counter matchedPotentialMessagesCounter;
    private final Counter retriggeredMatchingTasksCounter;

    public MessagesHandlingService(EventInstanceService eventInstanceService, WorkService workService,
            TechnicalLoggerService loggerService,
            LockService lockService, Long tenantId, UserTransactionService userTransactionService,
            SessionAccessor sessionAccessor, BPMWorkFactory workFactory, MeterRegistry meterRegistry) {
        this.eventInstanceService = eventInstanceService;
        this.workService = workService;
        this.logger = loggerService.asLogger(MessagesHandlingService.class);
        this.lockService = lockService;
        this.tenantId = tenantId;
        this.userTransactionService = userTransactionService;
        this.sessionAccessor = sessionAccessor;
        this.workFactory = workFactory;
        executedMessagesCounter = Counter.builder(NUMBER_OF_MESSAGES_EXECUTED)
                .tags(Tags.of("tenant", String.valueOf(tenantId)))
                .baseUnit("messages")
                .description("BPMN message couples executed")
                .register(meterRegistry);
        matchedPotentialMessagesCounter = Counter.builder(NUMBER_OF_MESSAGES_POTENTIAL_MATCHED)
                .tags(Tags.of("tenant", String.valueOf(tenantId)))
                .baseUnit("messages")
                .description("BPMN message couples potentially matched")
                .register(meterRegistry);
        retriggeredMatchingTasksCounter = Counter.builder(NUMBER_OF_MESSAGES_MATCHING_RETRIGGERED_TASKS)
                .tags(Tags.of("tenant", String.valueOf(tenantId)))
                .baseUnit("messages matching tasks")
                .description("BPMN message matching tasks retriggered")
                .register(meterRegistry);
    }

    @Override
    public void start() {
        logger.info("Starting BPMN messages matcher thread");
        threadPoolExecutor = new ThreadPoolExecutor(1, 1, 1L, TimeUnit.HOURS,
                new ArrayBlockingQueue<>(5),
                r -> new Thread(r, "Bonita-Message-Matching"),
                (r, executor) -> logger.debug("Message matching queue capacity reached"));
        logger.info("Thread that handle messages matching successfully started");
    }

    @Override
    public void stop() {
        logger.info("Stopping BPMN messages matcher thread");
        if (threadPoolExecutor == null) {
            logger.info("BPMN messages matcher thread is already stopped");
            return;
        }
        threadPoolExecutor.shutdown();
        try {
            boolean termination = threadPoolExecutor.awaitTermination(5000, TimeUnit.MILLISECONDS);
            if (!termination) {
                logger.warn("Failed to terminate the BPMN messages matcher thread." +
                        " This will not have functional impacts but it might produce warnings on server shutdown");
            }
        } catch (InterruptedException ignored) {
        }
        threadPoolExecutor = null;
        logger.info("BPMN messages matcher thread successfully stopped");
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
            logger.warn("Cannot match messages when service is stopped. Maybe the engine is not yet started");
            return;
        }
        userTransactionService.registerBonitaSynchronization(new RegisterMessagesMatchingSynchronization());
    }

    @VisibleForTesting
    void matchEventCoupleAndTriggerExecution() throws Exception {
        userTransactionService.executeInTransaction(() -> {
            final List<SMessageEventCouple> potentialMessageCouples = eventInstanceService.getMessageEventCouples(0,
                    MAX_COUPLES);
            final int potentialMessagesCount = potentialMessageCouples.size();
            logger.info("Found {} potential message/event couples", potentialMessagesCount);
            matchedPotentialMessagesCounter.increment(potentialMessagesCount);
            final List<SMessageEventCouple> uniqueCouples = getMessageUniqueCouples(potentialMessageCouples);
            if (!uniqueCouples.isEmpty()) {
                logger.info("Triggering execution of unique {} message/event couples", uniqueCouples.size());
                executeUniqueMessageCouplesWork(uniqueCouples);
                logger.info("Execution of message/event couples triggered");
            } else {
                logger.debug("No message/event couples to be executed");
            }
            if (potentialMessagesCount == MAX_COUPLES) {
                logger.debug("There are more than {} message/event couples to match. " +
                        "Will trigger the execution again now, to match more couples", MAX_COUPLES);
                triggerMatchingOfMessages();
                retriggeredMatchingTasksCounter.increment();
            }
            return null;
        });
    }

    private void executeUniqueMessageCouplesWork(final List<SMessageEventCouple> uniqueCouples)
            throws SBonitaException {
        for (final SMessageEventCouple couple : uniqueCouples) {
            executeMessageCouple(couple.getMessageInstanceId(), couple.getWaitingMessageId());
        }
    }

    @VisibleForTesting
    void executeMessageCouple(long messageInstanceId, long waitingMessageId)
            throws SWaitingEventReadException, SMessageInstanceReadException,
            SMessageModificationException, SWaitingEventModificationException, SWorkRegisterException {
        logger.debug("Registering message/event couple execution: message {} / event {}", messageInstanceId,
                waitingMessageId);

        // Mark messages that will be treated as "treatment in progress":
        final SWaitingMessageEvent waitingMsg = eventInstanceService.getWaitingMessage(waitingMessageId);
        final SMessageInstance messageInstance = eventInstanceService.getMessageInstance(messageInstanceId);
        markMessageAsInProgress(messageInstance);

        // EVENT_SUB_PROCESS of type non-interrupted should be considered as well, as soon as we support them
        if (!SBPMEventType.START_EVENT.equals(waitingMsg.getEventType())) {
            markWaitingMessageAsInProgress(waitingMsg);
        }
        executedMessagesCounter.increment();
        workService.registerWork(workFactory.createExecuteMessageCoupleWorkDescriptor(messageInstance, waitingMsg));
    }

    /**
     * From a list of couples that may contain duplicate waiting message candidates, select only one waiting message for
     * each message instance: the first
     * matching waiting message is arbitrary chosen.
     * In the case of <code>SWaitingMessageEvent</code> of types {@link SBPMEventType#START_EVENT} or
     * {@link SBPMEventType#EVENT_SUB_PROCESS}, it can be
     * selected several times to trigger multiple instances.
     *
     * @param potentialMessageCouples all the possible couples that match the potential correlation.
     * @return the reduced list of couple, where we insure that a unique message instance is associated with a unique
     *         waiting message.
     */
    List<SMessageEventCouple> getMessageUniqueCouples(List<SMessageEventCouple> potentialMessageCouples) {
        final List<Long> takenMessages = new ArrayList<>();
        final List<Long> takenWaitings = new ArrayList<>();
        final List<SMessageEventCouple> uniqueMessageCouples = new ArrayList<>();
        for (final SMessageEventCouple couple : potentialMessageCouples) {
            final long messageInstanceId = couple.getMessageInstanceId();
            final long waitingMessageId = couple.getWaitingMessageId();
            final SBPMEventType waitingMessageEventType = couple.getWaitingMessageEventType();
            final boolean isMessageAlreadyTaken = takenMessages.contains(messageInstanceId);
            if (!isMessageAlreadyTaken && !takenWaitings.contains(waitingMessageId)) {
                takenMessages.add(messageInstanceId);
                // Starting events and Starting event sub-processes must not be considered as taken if they appear several times
                // EVENT_SUB_PROCESS of type non-interrupted should be considered as well, as soon as we support them
                if (!SBPMEventType.START_EVENT.equals(waitingMessageEventType)) {
                    takenWaitings.add(waitingMessageId);
                }
                uniqueMessageCouples.add(couple);
            } else if (logger.isTraceEnabled()) {
                logger.trace("Ignoring couple: message {} / event {}." +
                        " Duplication cause: message? {} / event? {}", couple.getMessageInstanceId(),
                        couple.getWaitingMessageId(), isMessageAlreadyTaken, takenWaitings.contains(waitingMessageId));
            }
        }
        return uniqueMessageCouples;
    }

    private void markMessageAsInProgress(final SMessageInstance messageInstance) throws SMessageModificationException {
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(SMessageInstanceBuilder.HANDLED, true);
        eventInstanceService.updateMessageInstance(messageInstance, descriptor);
    }

    private void markWaitingMessageAsInProgress(final SWaitingMessageEvent waitingMsg)
            throws SWaitingEventModificationException {
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(BuilderFactory.get(SWaitingMessageEventBuilderFactory.class).getProgressKey(),
                SWaitingMessageEventBuilderFactory.PROGRESS_IN_TREATMENT_KEY);
        eventInstanceService.updateWaitingMessage(waitingMsg, descriptor);
    }

    public void resetMessageCouple(long messageInstanceId, long waitingMessageId)
            throws SWaitingEventReadException, SWaitingEventModificationException, SMessageModificationException,
            SMessageInstanceReadException {
        resetWaitingMessage(waitingMessageId);
        resetMessageInstance(messageInstanceId);
    }

    private void resetMessageInstance(final long messageInstanceId)
            throws SMessageModificationException, SMessageInstanceReadException {
        final SMessageInstance messageInstance = eventInstanceService.getMessageInstance(messageInstanceId);
        if (messageInstance == null) {
            logger.warn("Unable to reset message instance {} because it is not found", messageInstanceId);
            return;
        }
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(SMessageInstanceBuilder.HANDLED, false);
        eventInstanceService.updateMessageInstance(messageInstance, descriptor);
    }

    private void resetWaitingMessage(final long waitingMessageId)
            throws SWaitingEventModificationException, SWaitingEventReadException {
        final SWaitingMessageEvent waitingMsg = eventInstanceService.getWaitingMessage(waitingMessageId);
        if (waitingMsg == null) {
            logger.warn("Unable to reset waiting event because it is not found", waitingMessageId);
            return;
        }
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(BuilderFactory.get(SWaitingMessageEventBuilderFactory.class).getProgressKey(),
                SWaitingMessageEventBuilderFactory.PROGRESS_FREE_KEY);
        eventInstanceService.updateWaitingMessage(waitingMsg, descriptor);
    }

    private class MessagesMatchingTask implements Callable<Void> {

        @Override
        public Void call() throws Exception {
            try {
                logger.debug("Starting messages matching");
                // we use a lock in order to have only one execution at a time even in cluster
                BonitaLock eventLock = lockService.tryLock(1L, LOCK_TYPE, 1L, TimeUnit.MILLISECONDS, tenantId);
                if (eventLock == null) {
                    // It could happen that some messages were still not triggered because the work that is currently
                    // executing was started after the last message execution
                    logger.debug(
                            "The task that matches BPMN messages is already running, this execution will be ignored");
                    return null;
                }
                try {
                    sessionAccessor.setTenantId(tenantId);
                    matchEventCoupleAndTriggerExecution();
                } finally {
                    lockService.unlock(eventLock, tenantId);
                }
                logger.debug("Messages matching completed");
            } catch (Exception e) {
                logger.error("Error while matching messages", e);
                throw e;
            }
            return null;
        }
    }

    private class RegisterMessagesMatchingSynchronization implements BonitaTransactionSynchronization {

        @Override
        public void beforeCommit() {
        }

        @Override
        public void afterCompletion(TransactionState txState) {
            threadPoolExecutor.submit(new MessagesMatchingTask());
            logger.debug("Messages matching task registered");
        }
    }
}
