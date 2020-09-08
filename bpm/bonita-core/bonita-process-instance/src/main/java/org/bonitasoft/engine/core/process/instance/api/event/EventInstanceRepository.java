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
package org.bonitasoft.engine.core.process.instance.api.event;

import java.util.List;
import java.util.Optional;

import org.bonitasoft.engine.core.process.instance.api.exceptions.event.SEventInstanceCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.SEventInstanceReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SEventTriggerInstanceCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SEventTriggerInstanceDeletionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SEventTriggerInstanceModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SEventTriggerInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SEventTriggerInstanceReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SMessageInstanceCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SMessageInstanceReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SMessageModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SWaitingEventCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SWaitingEventModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SWaitingEventReadException;
import org.bonitasoft.engine.core.process.instance.model.event.SBoundaryEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageEventCouple;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingErrorEvent;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingEvent;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingMessageEvent;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingSignalEvent;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.STimerEventTriggerInstance;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.search.SearchOptions;

/**
 * @author Pascal GARCIA
 */
public interface EventInstanceRepository {

    String EVENT_INSTANCE = "EVENT_INSTANCE";

    String EVENT_TRIGGER_INSTANCE = "EVENT_TRIGGER_INSTANCE";

    String MESSAGE_INSTANCE = "MESSAGE_INSTANCE";

    int IN_REQUEST_SIZE = 100;

    void createEventInstance(SEventInstance eventInstance) throws SEventInstanceCreationException;

    /**
     * STimerEventTriggerInstance is used to keep track of currently running timers
     * using {@link org.bonitasoft.engine.api.ProcessAPI#searchTimerEventTriggerInstances(long, SearchOptions)}
     */
    void createTimerEventTriggerInstance(STimerEventTriggerInstance sEventTriggerInstance)
            throws SEventTriggerInstanceCreationException;

    void createMessageInstance(SMessageInstance messageInstance) throws SMessageInstanceCreationException;

    void createWaitingEvent(SWaitingEvent waitingEvent) throws SWaitingEventCreationException;

    SWaitingErrorEvent getBoundaryWaitingErrorEvent(long relatedActivityInstanceId, String errorCode)
            throws SWaitingEventReadException;

    List<SEventInstance> getEventInstances(long rootContainerId, int fromIndex, int maxResults, String fieldName,
            OrderByType orderByType)
            throws SEventInstanceReadException;

    /**
     * @param activityInstanceId
     * @param fromIndex
     * @param maxResults
     * @return List of SBoundaryEventInstance, ordered by identifier ascending
     * @throws SEventInstanceReadException
     * @since 6.2
     */
    List<SBoundaryEventInstance> getActivityBoundaryEventInstances(long activityInstanceId, int fromIndex,
            int maxResults) throws SEventInstanceReadException;

    /**
     * @param entityClass
     * @param eventTriggerInstanceId
     * @return
     * @throws SEventTriggerInstanceReadException
     * @since 6.4.0
     */
    <T extends STimerEventTriggerInstance> T getEventTriggerInstance(Class<T> entityClass, long eventTriggerInstanceId)
            throws SEventTriggerInstanceReadException;

    void deleteMessageInstance(SMessageInstance messageInstance) throws SMessageModificationException;

    void deleteWaitingEvent(SWaitingEvent waitingEvent) throws SWaitingEventModificationException;

    /**
     * @param signalName
     * @param fromIndex
     * @param maxResults
     * @return
     * @throws SEventTriggerInstanceReadException
     * @since 6.3
     */
    List<SWaitingSignalEvent> getWaitingSignalEvents(String signalName, int fromIndex, int maxResults)
            throws SEventTriggerInstanceReadException;

    /**
     * search start waiting events related to a process definition (not its event sub processes)
     *
     * @param processDefinitionId
     * @return
     * @throws SBonitaReadException
     * @since 6.3
     */
    List<SWaitingEvent> getStartWaitingEventsOfProcessDefinition(long processDefinitionId)
            throws SBonitaReadException;

    List<SMessageEventCouple> getMessageEventCouples(int fromIndex, int maxResults)
            throws SEventTriggerInstanceReadException;

    SWaitingMessageEvent getWaitingMessage(long waitingMessageId) throws SWaitingEventReadException;

    SMessageInstance getMessageInstance(long messageInstanceId) throws SMessageInstanceReadException;

    void deleteMessageInstanceByIds(List<Long> ids) throws SMessageModificationException;

    List<Long> getMessageInstanceIdOlderThanCreationDate(final long creationDate,
            QueryOptions queryOptions)
            throws SEventTriggerInstanceReadException, SMessageInstanceReadException;

    void updateWaitingMessage(SWaitingMessageEvent waitingMessageEvent, EntityUpdateDescriptor descriptor)
            throws SWaitingEventModificationException;

    void updateMessageInstance(SMessageInstance messageInstance, EntityUpdateDescriptor descriptor)
            throws SMessageModificationException;

    <T extends SWaitingEvent> List<T> searchWaitingEvents(Class<T> entityClass, QueryOptions searchOptions)
            throws SBonitaReadException;

    long getNumberOfWaitingEvents(Class<? extends SWaitingEvent> entityClass, QueryOptions countOptions)
            throws SBonitaReadException;

    /**
     * @param flowNodeInstanceId the flow node instance id
     * @return the timer event trigger instance of this flow node if there is one
     */
    Optional<STimerEventTriggerInstance> getTimerEventTriggerInstanceOfFlowNode(long flowNodeInstanceId)
            throws SBonitaReadException;

    SWaitingSignalEvent getWaitingSignalEvent(long id)
            throws SEventTriggerInstanceReadException, SEventTriggerInstanceNotFoundException;

    /**
     * @param eventTriggerInstance
     * @throws SEventTriggerInstanceDeletionException
     * @since 6.1
     */
    void deleteEventTriggerInstance(STimerEventTriggerInstance eventTriggerInstance)
            throws SEventTriggerInstanceDeletionException;

    /**
     * Resets all Message Instances marked as handled, so that they are eligible to match Waiting Events again.
     *
     * @throws SMessageModificationException
     *         if an error occurs when resetting the 'handled' flag.
     */
    int resetProgressMessageInstances() throws SMessageModificationException;

    /**
     * Resets all Waiting Message Events marked as 'in progress", so that they are eligible to match Message Instances
     * again.
     *
     * @return the number of waiting events reset.
     * @throws SWaitingEventModificationException
     *         if an error occurs when resetting the 'progress' flag.
     */
    int resetInProgressWaitingEvents() throws SWaitingEventModificationException;

    /**
     * Get the number of STimerEventTriggerInstance on the specific process instance & corresponding to the criteria
     *
     * @param processInstanceId
     *        The identifier of the process instance
     * @param queryOptions
     *        Criteria of the search
     * @return The number of STimerEventTriggerInstance on the specific process instance & corresponding to the criteria
     * @since 6.4.0
     */
    long getNumberOfTimerEventTriggerInstances(long processInstanceId, QueryOptions queryOptions)
            throws SBonitaReadException;

    /**
     * Search the list of STimerEventTriggerInstance on the specific process instance & corresponding to the criteria
     *
     * @param processInstanceId
     *        The identifier of the process instance
     * @param queryOptions
     *        Criteria of the search
     * @return The list of STimerEventTriggerInstance on the specific process instance & corresponding to the criteria
     * @since 6.4.0
     */
    List<STimerEventTriggerInstance> searchTimerEventTriggerInstances(long processInstanceId, QueryOptions queryOptions)
            throws SBonitaReadException;

    /**
     * Update an event trigger instance.
     *
     * @param sTimerEventTriggerInstance
     *        The event trigger instance to update
     * @param descriptor
     *        The fields to update
     * @throws SEventTriggerInstanceModificationException
     * @since 6.4.0
     */
    void updateEventTriggerInstance(STimerEventTriggerInstance sTimerEventTriggerInstance,
            EntityUpdateDescriptor descriptor)
            throws SEventTriggerInstanceModificationException;

    List<SWaitingEvent> getWaitingEventsForFlowNodeId(long flowNodeInstanceId)
            throws SEventTriggerInstanceReadException;

    /**
     * Return waiting events related to the process instance passed as parameter, including
     * the ones at flow node level.
     */
    List<SWaitingEvent> getAllWaitingEventsForProcessInstance(long processInstanceId, final int fromIndex,
            final int maxResults) throws SEventTriggerInstanceReadException;
}
