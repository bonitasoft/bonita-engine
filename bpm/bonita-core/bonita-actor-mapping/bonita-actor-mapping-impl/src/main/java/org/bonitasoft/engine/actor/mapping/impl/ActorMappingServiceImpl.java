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
package org.bonitasoft.engine.actor.mapping.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.actor.mapping.SActorCreationException;
import org.bonitasoft.engine.actor.mapping.SActorDeletionException;
import org.bonitasoft.engine.actor.mapping.SActorMemberCreationException;
import org.bonitasoft.engine.actor.mapping.SActorMemberDeletionException;
import org.bonitasoft.engine.actor.mapping.SActorMemberNotFoundException;
import org.bonitasoft.engine.actor.mapping.SActorNotFoundException;
import org.bonitasoft.engine.actor.mapping.SActorUpdateException;
import org.bonitasoft.engine.actor.mapping.model.SActor;
import org.bonitasoft.engine.actor.mapping.model.SActorLogBuilder;
import org.bonitasoft.engine.actor.mapping.model.SActorLogBuilderFactory;
import org.bonitasoft.engine.actor.mapping.model.SActorMember;
import org.bonitasoft.engine.actor.mapping.model.impl.SActorMemberImpl;
import org.bonitasoft.engine.actor.mapping.persistence.SelectDescriptorBuilder;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.events.EventActionType;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilderFactory;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.SIdentityException;
import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.queriablelogger.model.builder.ActionType;
import org.bonitasoft.engine.queriablelogger.model.builder.HasCRUDEAction;
import org.bonitasoft.engine.queriablelogger.model.builder.SLogBuilder;
import org.bonitasoft.engine.queriablelogger.model.builder.SPersistenceLogBuilder;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteAllRecord;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.bonitasoft.engine.services.QueriableLoggerService;

/**
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class ActorMappingServiceImpl implements ActorMappingService {

    private static final int BATCH_SIZE = 100;

    private final ReadPersistenceService persistenceService;

    private final Recorder recorder;

    private final EventService eventService;

    private final QueriableLoggerService queriableLoggerService;

    private final IdentityService identityService;

    public ActorMappingServiceImpl(final ReadPersistenceService persistenceService, final Recorder recorder, final EventService eventService,
            final QueriableLoggerService queriableLoggerService, final IdentityService identityService) {
        this.persistenceService = persistenceService;
        this.recorder = recorder;
        this.eventService = eventService;
        this.queriableLoggerService = queriableLoggerService;
        this.identityService = identityService;
    }

    @Override
    public Set<SActor> addActors(final Set<SActor> actors) throws SActorCreationException {
        final Set<SActor> sActors = new HashSet<SActor>();
        for (final SActor actor : actors) {
            sActors.add(addActor(actor));
        }
        return sActors;
    }

    @Override
    public SActor addActor(final SActor actor) throws SActorCreationException {
        final SActorLogBuilder logBuilder = getQueriableLog(ActionType.CREATED, "Creating a new actor");
        final InsertRecord insertRecord = new InsertRecord(actor);
        SInsertEvent insertEvent = null;
        if (eventService.hasHandlers(ACTOR, EventActionType.CREATED)) {
            insertEvent = (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent(ACTOR).setObject(actor).done();
        }
        try {
            recorder.recordInsert(insertRecord, insertEvent);
            initiateLogBuilder(actor.getId(), SQueriableLog.STATUS_OK, logBuilder, "addActor");
            return actor;
        } catch (final SRecorderException re) {
            initiateLogBuilder(actor.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "addActor");
            throw new SActorCreationException(re);
        }
    }

    private SActorLogBuilder getQueriableLog(final ActionType actionType, final String message) {
        final SActorLogBuilder logBuilder = BuilderFactory.get(SActorLogBuilderFactory.class).createNewInstance();
        this.initializeLogBuilder(logBuilder, message);
        this.updateLog(actionType, logBuilder);
        return logBuilder;
    }

    private <T extends SLogBuilder> void initializeLogBuilder(final T logBuilder, final String message) {
        logBuilder.actionStatus(SQueriableLog.STATUS_FAIL).severity(SQueriableLogSeverity.INTERNAL).rawMessage(message);
    }

    private <T extends HasCRUDEAction> void updateLog(final ActionType actionType, final T logBuilder) {
        logBuilder.setActionType(actionType);
    }

    @Override
    public SActor getActor(final long actorId) throws SActorNotFoundException, SBonitaReadException {
        final SelectByIdDescriptor<SActor> selectByIdDescriptor = SelectDescriptorBuilder.getActor(actorId);
        final SActor actor = persistenceService.selectById(selectByIdDescriptor);
        if (actor == null) {
            throw new SActorNotFoundException(actorId + " does not refer to any actor");
        }
        return actor;
    }

    @Override
    public long getNumberOfUsersOfActor(final long actorId) {
        final SelectOneDescriptor<Long> descriptor = SelectDescriptorBuilder.getNumberOfUsersOfActor(actorId);
        try {
            return persistenceService.selectOne(descriptor);
        } catch (final SBonitaReadException bre) {
            throw new RuntimeException(bre);
        }
    }

    @Override
    public long getNumberOfRolesOfActor(final long actorId) {
        final SelectOneDescriptor<Long> descriptor = SelectDescriptorBuilder.getNumberOfRolesOfActor(actorId);
        try {
            return persistenceService.selectOne(descriptor);
        } catch (final SBonitaReadException bre) {
            throw new RuntimeException(bre);
        }
    }

    @Override
    public long getNumberOfGroupsOfActor(final long actorId) {
        final SelectOneDescriptor<Long> descriptor = SelectDescriptorBuilder.getNumberOfGroupsOfActor(actorId);
        try {
            return persistenceService.selectOne(descriptor);
        } catch (final SBonitaReadException bre) {
            throw new RuntimeException(bre);
        }
    }

    @Override
    public long getNumberOfMembershipsOfActor(final long actorId) {
        final SelectOneDescriptor<Long> descriptor = SelectDescriptorBuilder.getNumberOfMembershipsOfActor(actorId);
        try {
            return persistenceService.selectOne(descriptor);
        } catch (final SBonitaReadException bre) {
            throw new RuntimeException(bre);
        }
    }

    @Override
    public List<SActor> getActors(final List<Long> actorIds) throws SBonitaReadException {
        if (actorIds == null || actorIds.isEmpty()) {
            return Collections.emptyList();
        }
        return persistenceService.selectList(SelectDescriptorBuilder.getElementsByIds(SActor.class, "Actor", actorIds));
    }

    @Override
    public SActor getActor(final String actorName, final long scopeId) throws SActorNotFoundException {
        final SelectOneDescriptor<SActor> selectOneDescriptor = SelectDescriptorBuilder.getActor(actorName, scopeId);
        try {
            final SActor actor = persistenceService.selectOne(selectOneDescriptor);
            if (actor == null) {
                throw new SActorNotFoundException("Actor not found with name: " + actorName + " of scopeId: " + scopeId);
            }
            return actor;
        } catch (final SBonitaReadException bre) {
            throw new SActorNotFoundException(bre);
        }
    }

    @Override
    public SActor updateActor(final long actorId, final EntityUpdateDescriptor descriptor) throws SActorNotFoundException, SActorUpdateException,
            SBonitaReadException {
        final SActor actor = getActor(actorId);
        final SActorLogBuilder logBuilder = getQueriableLog(ActionType.UPDATED, "Updating an actor");
        final UpdateRecord updateRecord = UpdateRecord.buildSetFields(actor, descriptor);
        SUpdateEvent updateEvent = null;
        if (eventService.hasHandlers(ACTOR, EventActionType.UPDATED)) {
            updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(ACTOR).setObject(actor).done();
        }
        try {
            recorder.recordUpdate(updateRecord, updateEvent);
            initiateLogBuilder(actorId, SQueriableLog.STATUS_OK, logBuilder, "updateActor");
        } catch (final SRecorderException e) {
            initiateLogBuilder(actorId, SQueriableLog.STATUS_FAIL, logBuilder, "updateActor");
            throw new SActorUpdateException(e);
        }
        return actor;
    }

    @Override
    public void deleteActors(final long scopeId) throws SActorDeletionException {
        try {
            final QueryOptions queryOptions = new QueryOptions(0, 100, SActor.class, "id", OrderByType.ASC);
            List<SActor> actors = getActors(scopeId, queryOptions);
            while (!actors.isEmpty()) {
                for (final SActor actor : actors) {
                    // First delete its members:
                    deleteActorMembers(actor);
                    // then the actor itself:
                    deleteActor(actor);
                }
                actors = getActors(scopeId, queryOptions);
            }
        } catch (final SBonitaReadException bre) {
            throw new SActorDeletionException(bre);
        } catch (final SActorMemberDeletionException e) {
            throw new SActorDeletionException(e);
        }
    }

    private void deleteActorMembers(final SActor actor) throws SBonitaReadException, SActorMemberDeletionException {
        List<SActorMember> actorMembers;
        do {
            actorMembers = getActorMembers(actor.getId(), 0, BATCH_SIZE);
            for (final SActorMember sActorMember : actorMembers) {
                deleteActorMember(sActorMember);
            }
        } while (actorMembers.size() > 0);
    }

    private void deleteActor(final SActor actor) throws SActorDeletionException {
        final SActorLogBuilder logBuilder = getQueriableLog(ActionType.DELETED, "Deleting an actor");
        final DeleteRecord deleteRecord = new DeleteRecord(actor);
        SDeleteEvent deleteEvent = null;
        if (eventService.hasHandlers(ACTOR, EventActionType.DELETED)) {
            deleteEvent = (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(ACTOR).setObject(actor).done();
        }
        try {
            recorder.recordDelete(deleteRecord, deleteEvent);
            initiateLogBuilder(actor.getId(), SQueriableLog.STATUS_OK, logBuilder, "deleteActor");
        } catch (final SRecorderException re) {
            initiateLogBuilder(actor.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "deleteActor");
            throw new SActorDeletionException(re);
        }
    }

    @Override
    public List<SActor> getActors(final Set<Long> scopeIds, final Long userId) throws SBonitaReadException {
        final SelectListDescriptor<SActor> descriptor = SelectDescriptorBuilder.getFullActorsListOfUser(scopeIds, userId);
        return persistenceService.selectList(descriptor);
    }

    @Override
    public SActorMember addUserToActor(final long actorId, final long userId) throws SActorMemberCreationException {
        final SActorMemberImpl actorMember = new SActorMemberImpl();
        actorMember.setActorId(actorId);
        actorMember.setUserId(userId);
        return addActorMember(actorMember);
    }

    private SActorMember addActorMember(final SActorMemberImpl actorMember) throws SActorMemberCreationException {
        final SActorLogBuilder logBuilder = getQueriableLog(ActionType.CREATED, "Creating a new actor member");
        final InsertRecord insertRecord = new InsertRecord(actorMember);
        SInsertEvent insertEvent = null;
        if (eventService.hasHandlers(ACTOR_MEMBER, EventActionType.CREATED)) {
            insertEvent = (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent(ACTOR_MEMBER).setObject(actorMember).done();
        }
        try {
            recorder.recordInsert(insertRecord, insertEvent);
            initiateLogBuilder(actorMember.getId(), SQueriableLog.STATUS_OK, logBuilder, "addActorMember");
            return actorMember;
        } catch (final SRecorderException re) {
            initiateLogBuilder(actorMember.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "addActorMember");
            throw new SActorMemberCreationException(re);
        }
    }

    @Override
    public SActorMember addGroupToActor(final long actorId, final long groupId) throws SActorNotFoundException, SActorMemberCreationException {
        try {
            final SActorMember addActorMember = addOnlyThisGroupToActor(actorId, groupId);
            int i = 0;
            List<SGroup> groupChildren;
            do {
                groupChildren = identityService.getGroupChildren(groupId, i, BATCH_SIZE);
                for (final SGroup child : groupChildren) {
                    // Only insert if sub-group not mapped already to this Actor :
                    if (getNumberOfActorMembersOfGroupWithActor(child.getId(), actorId) == 0) {
                        addGroupToActor(actorId, child.getId());
                    }
                }
                i += BATCH_SIZE;
            } while (groupChildren.size() == BATCH_SIZE);
            return addActorMember;
        } catch (final SIdentityException e) {
            throw new SActorMemberCreationException(e);
        } catch (final SBonitaReadException e) {
            throw new SActorMemberCreationException(e);
        }
    }

    private SActorMember addOnlyThisGroupToActor(final long actorId, final long groupId) throws SActorMemberCreationException {
        final SActorMemberImpl actorMember = new SActorMemberImpl();
        actorMember.setActorId(actorId);
        actorMember.setGroupId(groupId);
        return addActorMember(actorMember);
    }

    @Override
    public SActorMember addRoleToActor(final long actorId, final long roleId) throws SActorMemberCreationException {
        final SActorMemberImpl actorMember = new SActorMemberImpl();
        actorMember.setActorId(actorId);
        actorMember.setRoleId(roleId);
        return addActorMember(actorMember);
    }

    @Override
    public SActorMember addRoleAndGroupToActor(final long actorId, final long roleId, final long groupId) throws SActorNotFoundException,
            SActorMemberCreationException {
        try {
            final SActorMember addActorMember = addOnlyThisRoleAndGroupToActor(actorId, roleId, groupId);
            int i = 0;
            List<SGroup> groupChildren;
            do {
                groupChildren = identityService.getGroupChildren(groupId, i, BATCH_SIZE);
                for (final SGroup child : groupChildren) {
                    addRoleAndGroupToActor(actorId, roleId, child.getId());
                }
                i += BATCH_SIZE;
            } while (groupChildren.size() == BATCH_SIZE);
            return addActorMember;
        } catch (final SIdentityException e) {
            throw new SActorMemberCreationException(e);
        }
    }

    private SActorMember addOnlyThisRoleAndGroupToActor(final long actorId, final long roleId, final long groupId) throws SActorMemberCreationException {
        final SActorMemberImpl actorMember = new SActorMemberImpl();
        actorMember.setActorId(actorId);
        actorMember.setRoleId(roleId);
        actorMember.setGroupId(groupId);
        return addActorMember(actorMember);
    }

    @Override
    public SActorMember deleteActorMember(final long actorMemberId) throws SActorMemberNotFoundException, SActorMemberDeletionException {
        final SActorLogBuilder logBuilder = getQueriableLog(ActionType.DELETED, "Deleting an actor member");
        try {
            final SActorMember actorMember = getActorMember(actorMemberId);
            deleteActorMember(actorMember);
            return actorMember;
        } catch (final SBonitaReadException e) {
            initiateLogBuilder(actorMemberId, SQueriableLog.STATUS_FAIL, logBuilder, "removeActorMember");
            throw new SActorMemberDeletionException(e);
        }
    }

    @Override
    public void deleteActorMember(final SActorMember sActorMember) throws SActorMemberDeletionException {
        final SActorLogBuilder logBuilder = getQueriableLog(ActionType.DELETED, "Deleting an actor member");
        final long actorMemberId = sActorMember.getId();
        try {
            final DeleteRecord deleteRecord = new DeleteRecord(sActorMember);
            SDeleteEvent deleteEvent = null;
            if (eventService.hasHandlers(ACTOR_MEMBER, EventActionType.DELETED)) {
                deleteEvent = (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(ACTOR_MEMBER).setObject(sActorMember).done();
            }
            recorder.recordDelete(deleteRecord, deleteEvent);
            initiateLogBuilder(actorMemberId, SQueriableLog.STATUS_OK, logBuilder, "removeActorMember");
        } catch (final SRecorderException re) {
            initiateLogBuilder(actorMemberId, SQueriableLog.STATUS_FAIL, logBuilder, "removeActorMember");
            throw new SActorMemberDeletionException(re);
        }
    }

    private SActorMember getActorMember(final long actorMemberId) throws SActorMemberNotFoundException, SBonitaReadException {
        final SelectByIdDescriptor<SActorMember> selectByIdDescriptor = SelectDescriptorBuilder.getActorMember(actorMemberId);
        final SActorMember actor = persistenceService.selectById(selectByIdDescriptor);
        if (actor == null) {
            throw new SActorMemberNotFoundException(actorMemberId + " does not refer to any actor member");
        }
        return actor;
    }

    @Override
    public SActorMember getActorMember(final long actorId, final long userId, final long groupId, final long roleId) throws SBonitaReadException {
        final SelectOneDescriptor<SActorMember> descriptor = SelectDescriptorBuilder.getActorMember(actorId, userId, groupId, roleId);
        return persistenceService.selectOne(descriptor);
    }

    @Override
    public long getNumberOfActorMembers(final long actorId) throws SBonitaReadException {
        final SelectOneDescriptor<Long> descriptor = SelectDescriptorBuilder.getNumberOfActorMembers(actorId);
        return persistenceService.selectOne(descriptor);
    }

    @Override
    public List<SActorMember> getActorMembers(final long actorId, final int fromIndex, final int numberOfActorMembers) throws SBonitaReadException {
        final SelectListDescriptor<SActorMember> descriptor = SelectDescriptorBuilder.getActorMembers(actorId, fromIndex, numberOfActorMembers);
        return persistenceService.selectList(descriptor);
    }

    @Override
    public List<SActorMember> getActorMembersOfUser(final long userId, final int fromIndex, final int numberOfActorMembers) throws SBonitaReadException {
        final SelectListDescriptor<SActorMember> descriptor = SelectDescriptorBuilder.getActorMembersOfUser(userId, fromIndex, numberOfActorMembers);
        return persistenceService.selectList(descriptor);
    }

    @Override
    public List<SActorMember> getActorMembersOfGroup(final long groupId, final int fromIndex, final int numberOfActorMembers) throws SBonitaReadException {
        final SelectListDescriptor<SActorMember> descriptor = SelectDescriptorBuilder.getActorMembersOfGroup(groupId, fromIndex, numberOfActorMembers);
        return persistenceService.selectList(descriptor);
    }

    private long getNumberOfActorMembersOfGroupWithActor(final long groupId, final long actorId) throws SBonitaReadException {
        final SelectOneDescriptor<Long> descriptor = SelectDescriptorBuilder.getNumberOfActorMembersOfGroupWithActor(groupId, actorId);
        return persistenceService.selectOne(descriptor);
    }

    @Override
    public List<SActorMember> getActorMembersOfRole(final long roleId, final int fromIndex, final int numberOfActorMembers) throws SBonitaReadException {
        final SelectListDescriptor<SActorMember> descriptor = SelectDescriptorBuilder.getActorMembersOfRole(roleId, fromIndex, numberOfActorMembers);
        return persistenceService.selectList(descriptor);
    }

    @Override
    public boolean canUserStartProcessDefinition(final long userId, final long processDefinitionId) throws SBonitaReadException {
        final SelectListDescriptor<Long> descriptor = SelectDescriptorBuilder.getActorMembersInitiatorForProcess(processDefinitionId, 0,
                QueryOptions.UNLIMITED_NUMBER_OF_RESULTS);
        final List<Long> actorMembersForProcess = persistenceService.selectList(descriptor);
        final int BATCH_SIZE = 80;
        boolean found = false;
        while (!found && actorMembersForProcess.size() > 0) {
            found = 0 < persistenceService.selectOne(SelectDescriptorBuilder.getNumberOfUserMembersForUserOrManagerForActorMembers(userId,
                    retrieveFirstResultsAndRemoveFromOriginalList(BATCH_SIZE, actorMembersForProcess)));
        }
        return found;
    }

    private List<Long> retrieveFirstResultsAndRemoveFromOriginalList(int howMany, List<Long> ids) {
        List<Long> subList = new ArrayList<Long>(howMany);
        for (int i = 0; i < howMany && ids.size() > 0; i++) {
            subList.add(ids.remove(0));
        }
        return subList;
    }

    @Override
    public List<SActor> getActors(final long processDefinitionId, final QueryOptions queryOptions) throws SBonitaReadException {
        final SelectListDescriptor<SActor> descriptor = SelectDescriptorBuilder.getActorsOfScope(processDefinitionId, queryOptions);
        return persistenceService.selectList(descriptor);
    }

    private void initiateLogBuilder(final long objectId, final int sQueriableLogStatus, final SPersistenceLogBuilder logBuilder, final String callerMethodName) {
        logBuilder.actionScope(String.valueOf(objectId));
        logBuilder.actionStatus(sQueriableLogStatus);
        logBuilder.objectId(objectId);
        final SQueriableLog log = logBuilder.done();
        if (queriableLoggerService.isLoggable(log.getActionType(), log.getSeverity())) {
            queriableLoggerService.log(this.getClass().getName(), callerMethodName, log);
        }
    }

    @Override
    public void deleteAllActorMembers() throws SActorMemberDeletionException {
        try {
            final DeleteAllRecord record = new DeleteAllRecord(SActorMember.class, null);
            recorder.recordDeleteAll(record);
        } catch (final SRecorderException e) {
            throw new SActorMemberDeletionException("Can't delete all actor members.", e);
        }
    }

    @Override
    public List<Long> getPossibleUserIdsOfActorId(final long actorId, final int startIndex, final int maxResults) throws SBonitaReadException {
        final Map<String, Object> parameters = Collections.singletonMap("actorId", (Object) actorId);
        final SelectListDescriptor<Long> descriptor = new SelectListDescriptor<Long>("getPossibleUserIdsOfActorId", parameters, SActor.class, new QueryOptions(
                startIndex, maxResults));
        return persistenceService.selectList(descriptor);
    }

}
