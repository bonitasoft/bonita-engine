package org.bonitasoft.engine.bpm.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.bpm.BPMServicesBuilder;
import org.bonitasoft.engine.bpm.CommonBPMServicesTest;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.STimerType;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.SEventInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.event.SBoundaryEventInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.event.SEndEventInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.event.SStartEventInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.event.handling.SWaitingErrorEventBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.event.handling.SWaitingMessageEventBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.event.handling.SWaitingSignalEventBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.event.trigger.SEventTriggerInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.event.trigger.SThrowErrorEventTriggerInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.event.trigger.SThrowMessageEventTriggerInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.event.trigger.SThrowSignalEventTriggerInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.event.trigger.STimerEventTriggerInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.event.SBoundaryEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SEndEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SIntermediateCatchEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SIntermediateThrowEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SStartEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingEvent;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingMessageEvent;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingSignalEvent;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.SEventTriggerInstance;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.SThrowErrorEventTriggerInstance;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.SThrowMessageEventTriggerInstance;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.SThrowSignalEventTriggerInstance;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.STimerEventTriggerInstance;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.junit.Test;

/**
 * @author Elias Ricken de Medeiros
 */
public class EventInstanceServiceTest extends CommonBPMServicesTest {

    private final UserTransactionService userTransactionService;

    private final EventInstanceService eventInstanceService;

    private final BPMServicesBuilder servicesBuilder;

    public EventInstanceServiceTest() {
        servicesBuilder = getServicesBuilder();
        userTransactionService = servicesBuilder.getUserTransactionService();
        eventInstanceService = servicesBuilder.getEventInstanceService();
    }

    private void checkStartEventInstance(final SEventInstance expectedEventInstance, final SEventInstance actualEventInstance) {
        assertTrue(actualEventInstance instanceof SStartEventInstance);
        checkEventInstance(expectedEventInstance, actualEventInstance);
    }

    private void checkEndEventInstance(final SEventInstance expectedEventInstance, final SEventInstance actualEventInstance) {
        assertTrue(actualEventInstance instanceof SEndEventInstance);
        checkEventInstance(expectedEventInstance, actualEventInstance);
    }

    private void checkIntermediateCatchEventInstance(final SEventInstance expectedEventInstance, final SEventInstance actualEventInstance) {
        assertTrue(actualEventInstance instanceof SIntermediateCatchEventInstance);
        checkEventInstance(expectedEventInstance, actualEventInstance);
    }

    private void checkBoundaryEventInstance(final SEventInstance expectedEventInstance, final SEventInstance actualEventInstance) {
        assertTrue(actualEventInstance instanceof SBoundaryEventInstance);
        final SBoundaryEventInstance expectedBoundary = (SBoundaryEventInstance) expectedEventInstance;
        final SBoundaryEventInstance actualBoundary = (SBoundaryEventInstance) actualEventInstance;
        assertEquals(expectedBoundary.getActivityInstanceId(), actualBoundary.getActivityInstanceId());
        checkEventInstance(expectedEventInstance, actualEventInstance);
    }

    private void checkIntermediateThrowEventInstance(final SEventInstance expectedEventInstance, final SEventInstance actualEventInstance) {
        assertTrue(actualEventInstance instanceof SIntermediateThrowEventInstance);
        checkEventInstance(expectedEventInstance, actualEventInstance);
    }

    private void checkEventInstance(final SEventInstance expectedEventInstance, final SEventInstance actualEventInstance) {
        final SEndEventInstanceBuilderFactory eventInstanceBuilderFact = BuilderFactory.get(SEndEventInstanceBuilderFactory.class);
        final int processDefinitionIndex = eventInstanceBuilderFact.getProcessDefinitionIndex();
        final int processInstanceIndex = eventInstanceBuilderFact.getRootProcessInstanceIndex();

        assertEquals(expectedEventInstance.getId(), actualEventInstance.getId());
        assertEquals(expectedEventInstance.getName(), actualEventInstance.getName());
        assertEquals(expectedEventInstance.getParentContainerId(), actualEventInstance.getParentContainerId());
        assertEquals(expectedEventInstance.getStateId(), actualEventInstance.getStateId());
        assertEquals(expectedEventInstance.getLogicalGroup(processDefinitionIndex), actualEventInstance.getLogicalGroup(processDefinitionIndex));
        assertEquals(expectedEventInstance.getLogicalGroup(processInstanceIndex), actualEventInstance.getLogicalGroup(processInstanceIndex));
    }

    private List<SEventInstance> getEventInstances(final long processInstanceId, final int fromIndex,
            final int maxResult) throws Exception {
        return getEventInstances(processInstanceId, fromIndex, maxResult, BuilderFactory.get(SStartEventInstanceBuilderFactory.class).getNameKey(),
                OrderByType.ASC);
    }

    private List<SEventInstance> getEventInstances(final long processInstanceId, final int fromIndex, final int maxResult, final String fieldName,
            final OrderByType orderByType) throws Exception {
        return userTransactionService.executeInTransaction(new Callable<List<SEventInstance>>() {

            @Override
            public List<SEventInstance> call() throws Exception {
                return eventInstanceService.getEventInstances(processInstanceId, fromIndex, maxResult, fieldName, orderByType);
            }
        });
    }

    private SEventInstance getEventInstance(final long eventId) throws Exception {
        return userTransactionService.executeInTransaction(new Callable<SEventInstance>() {

            @Override
            public SEventInstance call() throws Exception {
                return eventInstanceService.getEventInstance(eventId);
            }
        });
    }

    private List<SBoundaryEventInstance> getActiviyBoundaryEventInstances(final long activityId, final int fromIndex, final int maxResults) throws Exception {
        return userTransactionService.executeInTransaction(new Callable<List<SBoundaryEventInstance>>() {

            @Override
            public List<SBoundaryEventInstance> call() throws Exception {
                return eventInstanceService.getActivityBoundaryEventInstances(activityId, fromIndex, maxResults);
            }
        });
    }

    private void checkTimerEventTriggerInstance(final STimerEventTriggerInstance expectedTriggerInstance,
            final SEventTriggerInstance retrievedEventTriggerInstance) {
        assertTrue(retrievedEventTriggerInstance instanceof STimerEventTriggerInstance);
        final STimerEventTriggerInstance retrievedTimer = (STimerEventTriggerInstance) retrievedEventTriggerInstance;
        assertEquals(expectedTriggerInstance.getTimerType(), retrievedTimer.getTimerType());
        assertEquals(expectedTriggerInstance.getTimerValue(), retrievedTimer.getTimerValue());
        checkEventTriggerInstance(expectedTriggerInstance, retrievedEventTriggerInstance);
    }

    private void checkEventTriggerInstance(final SEventTriggerInstance expectedTriggerInstance, final SEventTriggerInstance retrievedEventTriggerInstance) {
        assertEquals(expectedTriggerInstance.getId(), retrievedEventTriggerInstance.getId());
        assertEquals(expectedTriggerInstance.getEventInstanceId(), retrievedEventTriggerInstance.getEventInstanceId());
    }

    private STimerEventTriggerInstance createTimerEventTriggerInstance(final long eventInstanceId, final STimerType timerType, final long timerValue)
            throws Exception {
        final STimerEventTriggerInstance triggerInstance = BuilderFactory.get(STimerEventTriggerInstanceBuilderFactory.class)
                .createNewTimerEventTriggerInstance(eventInstanceId, timerType,
                        timerValue).done();
        createEventTriggerInstance(triggerInstance);
        return triggerInstance;
    }

    private SThrowMessageEventTriggerInstance createThrowMessageEventTriggerInstance(final long eventInstanceId, final String messageName,
            final String targetProcess, final String targetFlowNode) throws Exception {
        final SThrowMessageEventTriggerInstance messageTrigger = BuilderFactory.get(SThrowMessageEventTriggerInstanceBuilderFactory.class)
                .createNewInstance(eventInstanceId, messageName, targetProcess,
                        targetFlowNode).done();
        createEventTriggerInstance(messageTrigger);
        return messageTrigger;
    }

    private SThrowSignalEventTriggerInstance createThrowSignalEventTriggerInstance(final long eventInstanceId, final String signalName) throws Exception {
        final SThrowSignalEventTriggerInstance signalTrigger = BuilderFactory.get(SThrowSignalEventTriggerInstanceBuilderFactory.class)
                .createNewInstance(eventInstanceId, signalName).done();
        createEventTriggerInstance(signalTrigger);
        return signalTrigger;
    }

    private SThrowErrorEventTriggerInstance createThrowErrorEventTriggerInstance(final long eventInstanceId, final String errorCode) throws Exception {
        final SThrowErrorEventTriggerInstance errorTriggerInstance = BuilderFactory.get(SThrowErrorEventTriggerInstanceBuilderFactory.class)
                .createNewInstance(eventInstanceId, errorCode).done();
        createEventTriggerInstance(errorTriggerInstance);
        return errorTriggerInstance;
    }

    private void createEventTriggerInstance(final SEventTriggerInstance triggerInstance) throws Exception {
        userTransactionService.executeInTransaction(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                eventInstanceService.createEventTriggerInstance(triggerInstance);
                return null;
            }
        });
    }

    private void createWaitingEvent(final SWaitingEvent waitingEvent) throws Exception {
        userTransactionService.executeInTransaction(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                eventInstanceService.createWaitingEvent(waitingEvent);
                return null;
            }
        });
    }

    private SEventInstance createBoundaryEventInstance(final String eventName,
            final long flowNodeDefinitionId, final long rootProcessInstanceId, final long processDefinitionId, final long parentProcessInstanceId,
            final long activityInstanceId, final boolean isInterrupting) throws SBonitaException {
        final SEventInstance eventInstance = BuilderFactory
                .get(SBoundaryEventInstanceBuilderFactory.class)
                .createNewBoundaryEventInstance(eventName, isInterrupting, flowNodeDefinitionId,
                        rootProcessInstanceId, parentProcessInstanceId, processDefinitionId, rootProcessInstanceId, parentProcessInstanceId, activityInstanceId)
                .done();
        createSEventInstance(eventInstance);
        return eventInstance;
    }

    @Test
    public void testCreateAndRetrieveStartEventInstanceFromRootContainer() throws Exception {
        final SProcessInstance processInstance = createSProcessInstance();

        List<SEventInstance> eventInstances = getEventInstances(processInstance.getId(), 0, 5);
        assertTrue(eventInstances.isEmpty());

        final SEventInstance startEventInstance = createSStartEventInstance("startEvent", 1, processInstance.getId(), 5,
                processInstance.getId());
        eventInstances = getEventInstances(processInstance.getId(), 0, 5);

        assertEquals(1, eventInstances.size());
        checkStartEventInstance(startEventInstance, eventInstances.get(0));

        deleteSProcessInstance(processInstance);

        eventInstances = getEventInstances(processInstance.getId(), 0, 5);
        assertTrue(eventInstances.isEmpty());
    }

    @Test
    public void testCreateAndRetrieveEndEventInstance() throws Exception {
        final SProcessInstance processInstance = createSProcessInstance();

        List<SEventInstance> eventInstances = getEventInstances(processInstance.getId(), 0, 5);
        assertTrue(eventInstances.isEmpty());

        final SEventInstance eventInstance = createSEndEventInstance("EndEvent", 1, processInstance.getId(), 5, processInstance.getId());
        eventInstances = getEventInstances(processInstance.getId(), 0, 5);

        assertEquals(1, eventInstances.size());
        checkEndEventInstance(eventInstance, eventInstances.get(0));

        deleteSProcessInstance(processInstance);

        eventInstances = getEventInstances(processInstance.getId(), 0, 5);
        assertTrue(eventInstances.isEmpty());
    }

    @Test
    public void testCreateAndRetrieveIntermediateCatchEventInstance() throws Exception {
        final SProcessInstance processInstance = createSProcessInstance();

        List<SEventInstance> eventInstances = getEventInstances(processInstance.getId(), 0, 5);
        assertTrue(eventInstances.isEmpty());

        final SEventInstance eventInstance = createSIntermediateCatchEventInstance("IntermediateCatchEvent", 1, processInstance.getId(),
                5,
                processInstance.getId());
        eventInstances = getEventInstances(processInstance.getId(), 0, 5);

        assertEquals(1, eventInstances.size());
        checkIntermediateCatchEventInstance(eventInstance, eventInstances.get(0));

        deleteSProcessInstance(processInstance);

        eventInstances = getEventInstances(processInstance.getId(), 0, 5);
        assertTrue(eventInstances.isEmpty());
    }

    @Test
    public void testCreateAndRetrieveBoundaryEventInstance() throws Exception {
        final SProcessInstance processInstance = createSProcessInstance();

        List<SEventInstance> eventInstances = getEventInstances(processInstance.getId(), 0, 5);
        assertTrue(eventInstances.isEmpty());

        final int activityInstanceId = 10;
        final SEventInstance eventInstance = createBoundaryEventInstance("BoundaryEvent", 1, processInstance.getId(), 5,
                processInstance.getId(), activityInstanceId, true);
        eventInstances = getEventInstances(processInstance.getId(), 0, 5);

        assertEquals(1, eventInstances.size());
        checkBoundaryEventInstance(eventInstance, eventInstances.get(0));

        deleteSProcessInstance(processInstance);

        eventInstances = getEventInstances(processInstance.getId(), 0, 5);
        assertTrue(eventInstances.isEmpty());
    }

    @Test
    public void testGetActivityBoundaryEventInstances() throws Exception {
        final SProcessInstance processInstance = createSProcessInstance();
        final long processDefinitionId = 5;
        final SActivityInstance automaticTaskInstance = createSAutomaticTaskInstance("auto1", 1, processInstance.getId(), processDefinitionId,
                processInstance.getId());
        final long activityInstanceId = automaticTaskInstance.getId();

        List<SBoundaryEventInstance> boundaryEventInstances = getActiviyBoundaryEventInstances(activityInstanceId, 0, 1);
        assertTrue(boundaryEventInstances.isEmpty());

        final SEventInstance eventInstance1 = createBoundaryEventInstance("BoundaryEvent1", 2, processInstance.getId(),
                processDefinitionId, processInstance.getId(), activityInstanceId, true);
        final SEventInstance eventInstance2 = createBoundaryEventInstance("BoundaryEvent2", 3, processInstance.getId(),
                processDefinitionId, processInstance.getId(), activityInstanceId, true);

        boundaryEventInstances = getActiviyBoundaryEventInstances(activityInstanceId, 0, 3);
        assertEquals(2, boundaryEventInstances.size());
        checkBoundaryEventInstance(eventInstance1, boundaryEventInstances.get(0));
        checkBoundaryEventInstance(eventInstance2, boundaryEventInstances.get(1));

        deleteSProcessInstance(processInstance);

        boundaryEventInstances = getActiviyBoundaryEventInstances(activityInstanceId, 0, 1);
        assertTrue(boundaryEventInstances.isEmpty());
    }

    @Test
    public void testCreateAndRetrieveIntermediateThrowEventInstance() throws Exception {
        final SProcessInstance processInstance = createSProcessInstance();

        List<SEventInstance> eventInstances = getEventInstances(processInstance.getId(), 0, 5);
        assertTrue(eventInstances.isEmpty());

        final SEventInstance eventInstance = createSIntermediateThrowEventInstance("IntermediateThrowEvent", 1, processInstance.getId(),
                5,
                processInstance.getId());
        eventInstances = getEventInstances(processInstance.getId(), 0, 5);

        assertEquals(1, eventInstances.size());
        checkIntermediateThrowEventInstance(eventInstance, eventInstances.get(0));

        deleteSProcessInstance(processInstance);

        eventInstances = getEventInstances(processInstance.getId(), 0, 5);
        assertTrue(eventInstances.isEmpty());
    }

    @Test
    public void testGetEventInstanceById() throws Exception {
        final SProcessInstance processInstance = createSProcessInstance();
        final SEventInstance startEventInstance = createSStartEventInstance("startEvent", 1, processInstance.getId(), 5,
                processInstance.getId());

        final SEventInstance retrievedEventInstance = getEventInstance(startEventInstance.getId());

        checkStartEventInstance(startEventInstance, retrievedEventInstance);

        deleteSProcessInstance(processInstance);
    }

    @Test(expected = SEventInstanceNotFoundException.class)
    public void testCannotRetrieveEventUsingInvalidId() throws Exception {
        getEventInstance(100000L);
    }

    @Test
    public void testGetEventInstancesOrderByNameAsc() throws Exception {
        final SProcessInstance processInstance = createSProcessInstance();

        final SEventInstance eventInstance1 = createSEndEventInstance("EndEvent1", 1, processInstance.getId(), 5, processInstance.getId());
        final SEventInstance eventInstance2 = createSEndEventInstance("EndEvent2", 1, processInstance.getId(), 5, processInstance.getId());
        final List<SEventInstance> eventInstances = getEventInstances(processInstance.getId(), 0, 5, BuilderFactory.get(SEndEventInstanceBuilderFactory.class)
                .getNameKey(), OrderByType.ASC);

        assertEquals(2, eventInstances.size());
        checkEndEventInstance(eventInstance1, eventInstances.get(0));
        checkEndEventInstance(eventInstance2, eventInstances.get(1));

        deleteSProcessInstance(processInstance);
    }

    @Test
    public void testGetEventInstancesOrderByNameDesc() throws Exception {
        final SProcessInstance processInstance = createSProcessInstance();

        final SEventInstance eventInstance1 = createSEndEventInstance("EndEvent1", 1, processInstance.getId(), 5, processInstance.getId());
        final SEventInstance eventInstance2 = createSEndEventInstance("EndEvent2", 1, processInstance.getId(), 5, processInstance.getId());
        final List<SEventInstance> eventInstances = getEventInstances(processInstance.getId(), 0, 5, BuilderFactory.get(SEndEventInstanceBuilderFactory.class)
                .getNameKey(), OrderByType.DESC);

        assertEquals(2, eventInstances.size());
        checkEndEventInstance(eventInstance2, eventInstances.get(0));
        checkEndEventInstance(eventInstance1, eventInstances.get(1));

        deleteSProcessInstance(processInstance);
    }

    @Test(expected = SEventInstanceNotFoundException.class)
    public void testDeleteProcessInstanceAlsoDeleteEventInstance() throws Exception {
        final SProcessInstance processInstance = createSProcessInstance();
        final SEventInstance startEventInstance = createSStartEventInstance("startEvent", 1, processInstance.getId(), 5,
                processInstance.getId());

        final SEventInstance retrievedEventInstance = getEventInstance(startEventInstance.getId());

        checkStartEventInstance(startEventInstance, retrievedEventInstance);

        deleteSProcessInstance(processInstance);

        getEventInstance(startEventInstance.getId());
    }

    @Test
    public void testCreateAndRetrieveEventTriggerInstance() throws Exception {
        final SProcessInstance processInstance = createSProcessInstance();
        final SEventInstance startEventInstance = createSStartEventInstance("startEvent", 1, processInstance.getId(), 5,
                processInstance.getId());

        List<SEventTriggerInstance> triggerInstances = getEventTriggerInstances(startEventInstance.getId(), 0, 5);
        assertTrue(triggerInstances.isEmpty());

        final STimerEventTriggerInstance triggerInstance = createTimerEventTriggerInstance(startEventInstance.getId(),
                STimerType.DURATION, 1000);
        triggerInstances = getEventTriggerInstances(startEventInstance.getId(), 0, 5);
        assertEquals(1, triggerInstances.size());
        checkTimerEventTriggerInstance(triggerInstance, triggerInstances.get(0));

        deleteSProcessInstance(processInstance);

    }

    private List<SEventTriggerInstance> getEventTriggerInstances(final long eventInstanceId, final int fromIndex, final int maxResults) throws Exception {
        return userTransactionService.executeInTransaction(new Callable<List<SEventTriggerInstance>>() {

            @Override
            public List<SEventTriggerInstance> call() throws Exception {
                return eventInstanceService.getEventTriggerInstances(eventInstanceId, fromIndex, maxResults,
                        BuilderFactory.get(SEventTriggerInstanceBuilderFactory.class).getIdKey(), OrderByType.ASC);
            }
        });
    }

    private <T extends SWaitingEvent> List<T> searchWaitingEvents(final Class<T> clazz, final QueryOptions searchOptions) throws Exception {
        return transactionService.executeInTransaction(new Callable<List<T>>() {

            @Override
            public List<T> call() throws Exception {
                return eventInstanceService.searchWaitingEvents(clazz, searchOptions);
            }
        });
    }

    private long getNumberOfWaitingEvents(final Class<? extends SWaitingEvent> clazz, final QueryOptions countOptions) throws Exception {
        return transactionService.executeInTransaction(new Callable<Long>() {

            @Override
            public Long call() throws Exception {
                return eventInstanceService.getNumberOfWaitingEvents(clazz, countOptions);
            }
        });
    }

    private <T extends SEventTriggerInstance> List<T> searchEventTrigger(final Class<T> clazz, final QueryOptions searchOptions) throws Exception {
        return transactionService.executeInTransaction(new Callable<List<T>>() {

            @Override
            public List<T> call() throws Exception {
                return eventInstanceService.searchEventTriggerInstances(clazz, searchOptions);
            }
        });
    }

    private long getNumberOfEventTriggerInstances(final Class<? extends SEventTriggerInstance> clazz, final QueryOptions countOptions) throws Exception {
        return transactionService.executeInTransaction(new Callable<Long>() {

            @Override
            public Long call() throws Exception {
                return eventInstanceService.getNumberOfEventTriggerInstances(clazz, countOptions);
            }
        });
    }

    @Test
    public void testRetrieveEventTriggerInstanceById() throws Exception {
        final SProcessInstance processInstance = createSProcessInstance();
        final SEventInstance startEventInstance = createSStartEventInstance("startEvent", 1, processInstance.getId(), 5,
                processInstance.getId());

        final STimerEventTriggerInstance triggerInstance = createTimerEventTriggerInstance(startEventInstance.getId(), STimerType.DURATION, 1000);
        final SEventTriggerInstance retrievedEventTrigger = getEventTrigger(triggerInstance.getId());
        checkTimerEventTriggerInstance(triggerInstance, retrievedEventTrigger);

        deleteSProcessInstance(processInstance);
    }

    private SEventTriggerInstance getEventTrigger(final long triggerEventInstanceId) throws Exception {
        return transactionService.executeInTransaction(new Callable<SEventTriggerInstance>() {

            @Override
            public SEventTriggerInstance call() throws Exception {
                return eventInstanceService.getEventTriggerInstance(triggerEventInstanceId);
            }
        });
    }

    private List<SEventTriggerInstance> getEventTriggers(final long eventInstanceId, final int fromIndex, final int maxResults, final String fieldName,
            final OrderByType orderByType) throws Exception {
        return transactionService.executeInTransaction(new Callable<List<SEventTriggerInstance>>() {

            @Override
            public List<SEventTriggerInstance> call() throws Exception {
                return eventInstanceService.getEventTriggerInstances(eventInstanceId, fromIndex, maxResults,
                        fieldName, orderByType);
            }
        });
    }

    public void testDeleteEventInstanceAlsoDeleteEventTriggerInstance() throws Exception {
        final SProcessInstance processInstance = createSProcessInstance();
        final SEventInstance startEventInstance = createSStartEventInstance("startEvent", 1, processInstance.getId(), 5,
                processInstance.getId());

        createTimerEventTriggerInstance(startEventInstance.getId(), STimerType.DURATION, 1000);

        List<SEventTriggerInstance> eventTriggers = getEventTriggers(startEventInstance.getId(), 0, 10,
                BuilderFactory.get(SEventTriggerInstanceBuilderFactory.class).getEventInstanceIdKey(),
                OrderByType.ASC);
        assertEquals(1, eventTriggers.size());

        deleteSProcessInstance(processInstance);

        eventTriggers = getEventTriggers(startEventInstance.getId(), 0, 10, BuilderFactory.get(SEventTriggerInstanceBuilderFactory.class)
                .getEventInstanceIdKey(), OrderByType.ASC);
        assertEquals(0, eventTriggers.size());
    }

    @Test
    public void testSearchWaitingEvents() throws Exception {
        final SProcessInstance processInstance = createSProcessInstance();
        final SWaitingErrorEventBuilderFactory waitingErrorEventBuilder = BuilderFactory.get(SWaitingErrorEventBuilderFactory.class);

        final SEventInstance eventInstance = createSIntermediateCatchEventInstance("itermediate", 1, processInstance.getId(), 5, processInstance.getId());

        final Class<SWaitingEvent> waitingEventClass = SWaitingEvent.class;
        final String processDefinitionIdKey = waitingErrorEventBuilder.getProcessDefinitionIdKey();
        final String flowNodeInstanceIdKey = waitingErrorEventBuilder.getFlowNodeInstanceIdKey();
        final long eventInstanceId = eventInstance.getId();
        checkWaitingEvents(0, waitingEventClass, processDefinitionIdKey, flowNodeInstanceIdKey, eventInstanceId);

        final SWaitingMessageEvent messageWaitingEvent = BuilderFactory.get(SWaitingMessageEventBuilderFactory.class)
                .createNewWaitingMessageIntermediateEventInstance(5, processInstance.getId(), processInstance.getId(),
                        eventInstanceId, "m1", processInstance.getName(), eventInstance.getFlowNodeDefinitionId(), eventInstance.getName()).done();
        createWaitingEvent(messageWaitingEvent);

        final SWaitingSignalEvent waitingSignalEvent = BuilderFactory.get(SWaitingSignalEventBuilderFactory.class)
                .createNewWaitingSignalIntermediateEventInstance(5, processInstance.getId(), processInstance.getId(),
                        eventInstanceId, "go", processInstance.getName(), eventInstance.getFlowNodeDefinitionId(), eventInstance.getName()).done();
        createWaitingEvent(waitingSignalEvent);

        // search with SWaitingEvent
        checkWaitingEvents(2, waitingEventClass, processDefinitionIdKey, flowNodeInstanceIdKey, eventInstanceId);

        // search with SWaitingMessageEvent, SWaitingSignalEvent
        checkWaitingEvents(1, SWaitingMessageEvent.class, processDefinitionIdKey, flowNodeInstanceIdKey, eventInstanceId);
        checkWaitingEvents(1, SWaitingSignalEvent.class, processDefinitionIdKey, flowNodeInstanceIdKey, eventInstanceId);

        deleteSProcessInstance(processInstance);

        // checkWaitingEvents(0, waitingEventClass, processDefinitionIdKey, flowNodeInstanceIdKey, eventInstanceId);
    }

    private void checkWaitingEvents(final int expectedNbOfWaitingEvents, final Class<? extends SWaitingEvent> clazz, final String processDefinitionIdKey,
            final String flowNodeInstanceIdKey, final long eventInstanceId) throws Exception {
        final int maxResults = Math.max(expectedNbOfWaitingEvents + 1, 10);
        final QueryOptions queryOptions = getQueryOptions(clazz, 0, maxResults, processDefinitionIdKey, OrderByType.ASC, flowNodeInstanceIdKey, eventInstanceId);
        final QueryOptions countOptions = getCountOptions(clazz, flowNodeInstanceIdKey, eventInstanceId);
        final List<? extends SWaitingEvent> waitingErrorEvents = searchWaitingEvents(clazz, queryOptions);
        final long numberOfWaitingErrorEvents = getNumberOfWaitingEvents(clazz, countOptions);
        assertEquals(expectedNbOfWaitingEvents, numberOfWaitingErrorEvents);
        assertEquals(expectedNbOfWaitingEvents, waitingErrorEvents.size());
    }

    private QueryOptions getQueryOptions(final Class<? extends PersistentObject> clazz, final int fromIndex, final int maxResult, final String orderByField,
            final OrderByType orderByType, final String filterKey, final Object filterValue) {
        final OrderByOption orderByOption = new OrderByOption(clazz, orderByField, orderByType);
        final FilterOption filterOption = new FilterOption(clazz, filterKey, filterValue);
        final QueryOptions boundaryQueryOptions = new QueryOptions(fromIndex, maxResult, Collections.singletonList(orderByOption),
                Collections.singletonList(filterOption), null);
        return boundaryQueryOptions;
    }

    private QueryOptions getCountOptions(final Class<? extends PersistentObject> clazz, final String filterKey, final Object filterValue) {
        final FilterOption filterOption = new FilterOption(clazz, filterKey, filterValue);
        final List<OrderByOption> emptyOrderByOptions = Collections.emptyList();
        final QueryOptions countOptions = new QueryOptions(0, 1, emptyOrderByOptions, Collections.singletonList(filterOption), null);
        return countOptions;
    }

    private void checkEventTriggerInstances(final int exptectedNbOfTrigger, final Class<? extends SEventTriggerInstance> clazz,
            final String eventInstanceIdKey, final long eventInstanceId) throws Exception {
        final int maxResults = Math.max(10, exptectedNbOfTrigger + 1);
        final QueryOptions queryOptions = getQueryOptions(clazz, 0, maxResults, eventInstanceIdKey, OrderByType.ASC, eventInstanceIdKey, eventInstanceId);
        final QueryOptions countOptions = getCountOptions(clazz, eventInstanceIdKey, eventInstanceId);
        final List<? extends SEventTriggerInstance> triggers = searchEventTrigger(clazz, queryOptions);
        final long nbOfTriggers = getNumberOfEventTriggerInstances(clazz, countOptions);
        assertEquals(exptectedNbOfTrigger, nbOfTriggers);
        assertEquals(exptectedNbOfTrigger, triggers.size());
    }

    @Test
    public void testSearchEventTriggerInstances() throws Exception {
        final SProcessInstance processInstance = createSProcessInstance();

        final SEventInstance eventInstance = createSEndEventInstance("end", 1, processInstance.getId(), 5,
                processInstance.getId());
        final long eventInstanceId = eventInstance.getId();

        final Class<SEventTriggerInstance> triggerInstanceClass = SEventTriggerInstance.class;
        final String eventInstanceIdKey = BuilderFactory.get(STimerEventTriggerInstanceBuilderFactory.class).getEventInstanceIdKey();
        checkEventTriggerInstances(0, triggerInstanceClass, eventInstanceIdKey, eventInstanceId);

        createTimerEventTriggerInstance(eventInstanceId, STimerType.DURATION, 1000);
        createThrowMessageEventTriggerInstance(eventInstanceId, "m1", "p2", "start1");
        createThrowSignalEventTriggerInstance(eventInstanceId, "s1");
        createThrowErrorEventTriggerInstance(eventInstanceId, "e1");

        // search with STriggerEventInstance
        checkEventTriggerInstances(4, triggerInstanceClass, eventInstanceIdKey, eventInstanceId);

        // search with STimerEventTriggerInstance, SThrowMessageEventTriggerInstance, SThrowSignalEventTriggerInstance, SThrowErrorEventTriggerInstance
        checkEventTriggerInstances(1, STimerEventTriggerInstance.class, eventInstanceIdKey, eventInstanceId);
        checkEventTriggerInstances(1, SThrowMessageEventTriggerInstance.class, eventInstanceIdKey, eventInstanceId);
        checkEventTriggerInstances(1, SThrowSignalEventTriggerInstance.class, eventInstanceIdKey, eventInstanceId);
        checkEventTriggerInstances(1, SThrowErrorEventTriggerInstance.class, eventInstanceIdKey, eventInstanceId);

        deleteSProcessInstance(processInstance);
        checkEventTriggerInstances(0, triggerInstanceClass, eventInstanceIdKey, eventInstanceId);

    }

}
