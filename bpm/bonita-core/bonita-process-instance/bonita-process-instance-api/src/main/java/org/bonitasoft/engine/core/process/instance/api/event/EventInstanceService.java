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
package org.bonitasoft.engine.core.process.instance.api.event;

import java.util.List;

import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.SEventInstanceCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.SEventInstanceReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SEventTriggerInstanceCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SEventTriggerInstanceDeletionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SEventTriggerInstanceModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SEventTriggerInstanceReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SMessageInstanceCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SMessageInstanceReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SMessageModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SWaitingEventCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SWaitingEventModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SWaitingEventReadException;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SBoundaryEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageEventCouple;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingErrorEvent;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingEvent;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingMessageEvent;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingSignalEvent;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.SEventTriggerInstance;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.STimerEventTriggerInstance;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public interface EventInstanceService extends FlowNodeInstanceService {

    String EVENT_INSTANCE = "EVENT_INSTANCE";

    String EVENT_TRIGGER_INSTANCE = "EVENT_TRIGGER_INSTANCE";

    String MESSAGE_INSTANCE = "MESSAGE_INSTANCE";

    void createEventInstance(SEventInstance eventInstance) throws SEventInstanceCreationException;

    void createEventTriggerInstance(SEventTriggerInstance sEventTriggerInstance) throws SEventTriggerInstanceCreationException;

    void createMessageInstance(SMessageInstance messageInstance) throws SMessageInstanceCreationException;

    void createWaitingEvent(SWaitingEvent waitingEvent) throws SWaitingEventCreationException;

    SWaitingErrorEvent getBoundaryWaitingErrorEvent(long relatedActivityInstanceId, String errorCode) throws SWaitingEventReadException;

    List<SEventInstance> getEventInstances(long rootContainerId, int fromIndex, int maxResults, String fieldName, OrderByType orderByType)
            throws SEventInstanceReadException;

    /**
     * @param activityInstanceId
     * @param fromIndex
     * @param maxResults
     * @return List of SBoundaryEventInstance, ordered by identifier ascending
     * @throws SEventInstanceReadException
     * @since 6.2
     */
    List<SBoundaryEventInstance> getActivityBoundaryEventInstances(long activityInstanceId, int fromIndex, int maxResults) throws SEventInstanceReadException;

    /**
     * @param entityClass
     * @param eventTriggerInstanceId
     * @return
     * @throws SEventTriggerInstanceReadException
     * @since 6.4.0
     */
    <T extends SEventTriggerInstance> T getEventTriggerInstance(Class<T> entityClass, long eventTriggerInstanceId) throws SEventTriggerInstanceReadException;

    List<SEventTriggerInstance> getEventTriggerInstances(long eventInstanceId, QueryOptions queryOptions) throws SEventTriggerInstanceReadException;

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
    List<SWaitingSignalEvent> getWaitingSignalEvents(String signalName, int fromIndex, int maxResults) throws SEventTriggerInstanceReadException;

    /**
     * @param processDefinitionId
     * @param searchOptions
     * @return
     * @throws SBonitaReadException
     * @since 6.3
     */
    List<SWaitingEvent> searchStartWaitingEvents(long processDefinitionId, QueryOptions queryOptions) throws SBonitaReadException;

    List<SMessageEventCouple> getMessageEventCouples(int fromIndex, int maxResults) throws SEventTriggerInstanceReadException;

    SWaitingMessageEvent getWaitingMessage(long waitingMessageId) throws SWaitingEventReadException;

    SMessageInstance getMessageInstance(long messageInstanceId) throws SMessageInstanceReadException;

    void updateWaitingMessage(SWaitingMessageEvent waitingMessageEvent, EntityUpdateDescriptor descriptor) throws SWaitingEventModificationException;

    void updateMessageInstance(SMessageInstance messageInstance, EntityUpdateDescriptor descriptor) throws SMessageModificationException;

    <T extends SWaitingEvent> List<T> searchWaitingEvents(Class<T> entityClass, QueryOptions searchOptions) throws SBonitaReadException;

    long getNumberOfWaitingEvents(Class<? extends SWaitingEvent> entityClass, QueryOptions countOptions) throws SBonitaReadException;

    <T extends SEventTriggerInstance> List<T> searchEventTriggerInstances(Class<T> entityClass, QueryOptions searchOptions) throws SBonitaReadException;

    long getNumberOfEventTriggerInstances(Class<? extends SEventTriggerInstance> entityClass, QueryOptions countOptions) throws SBonitaReadException;

    /**
     * @param eventInstanceId
     * @throws SEventTriggerInstanceReadException
     * @throws SEventTriggerInstanceDeletionException
     * @since 6.1
     */
    void deleteEventTriggerInstances(long eventInstanceId) throws SEventTriggerInstanceReadException, SEventTriggerInstanceDeletionException;

    /**
     * @param eventTriggerInstance
     * @throws SEventTriggerInstanceDeletionException
     * @since 6.1
     */
    void deleteEventTriggerInstance(SEventTriggerInstance eventTriggerInstance) throws SEventTriggerInstanceDeletionException;

    /**
     * @param flowNodeInstance
     * @throws SWaitingEventModificationException
     * @throws SFlowNodeReadException
     * @since 6.1
     */
    void deleteWaitingEvents(SFlowNodeInstance flowNodeInstance) throws SWaitingEventModificationException, SBonitaReadException;

    /**
     * Resets all Message Instances marked as handled, so that they are eligible to match Waiting Events again.
     *
     * @throws SMessageModificationException
     *         if an error occurs when resetting the 'handled' flag.
     */
    int resetProgressMessageInstances() throws SMessageModificationException;

    /**
     * Resets all Waiting Message Events marked as 'in progress", so that they are eligible to match Message Instances again.
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
     * @param searchOptions
     *        Criteria of the search
     * @return The number of STimerEventTriggerInstance on the specific process instance & corresponding to the criteria
     * @since 6.4.0
     */
    long getNumberOfTimerEventTriggerInstances(long processInstanceId, QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * Search the list of STimerEventTriggerInstance on the specific process instance & corresponding to the criteria
     *
     * @param processInstanceId
     *        The identifier of the process instance
     * @param searchOptions
     *        Criteria of the search
     * @return The list of STimerEventTriggerInstance on the specific process instance & corresponding to the criteria
     * @since 6.4.0
     */
    List<STimerEventTriggerInstance> searchTimerEventTriggerInstances(long processInstanceId, QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * Update an event trigger instance.
     *
     * @param sEventTriggerInstance
     *        The event trigger instance to update
     * @param descriptor
     *        The fields to update
     * @throws SEventTriggerModificationException
     * @since 6.4.0
     */
    void updateEventTriggerInstance(SEventTriggerInstance sEventTriggerInstance, EntityUpdateDescriptor descriptor)
            throws SEventTriggerInstanceModificationException;

}
