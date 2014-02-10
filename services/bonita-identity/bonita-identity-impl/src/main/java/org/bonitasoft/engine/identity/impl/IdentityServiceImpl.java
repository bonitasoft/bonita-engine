/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.identity.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.CollectionUtil;
import org.bonitasoft.engine.commons.LogUtil;
import org.bonitasoft.engine.events.EventActionType;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilderFactory;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.SGroupCreationException;
import org.bonitasoft.engine.identity.SGroupDeletionException;
import org.bonitasoft.engine.identity.SGroupNotFoundException;
import org.bonitasoft.engine.identity.SIdentityException;
import org.bonitasoft.engine.identity.SMembershipDeletionException;
import org.bonitasoft.engine.identity.SRoleDeletionException;
import org.bonitasoft.engine.identity.SRoleNotFoundException;
import org.bonitasoft.engine.identity.SUserCreationException;
import org.bonitasoft.engine.identity.SUserDeletionException;
import org.bonitasoft.engine.identity.SUserMembershipCreationException;
import org.bonitasoft.engine.identity.SUserNotFoundException;
import org.bonitasoft.engine.identity.SUserUpdateException;
import org.bonitasoft.engine.identity.model.SContactInfo;
import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.model.SProfileMetadataDefinition;
import org.bonitasoft.engine.identity.model.SProfileMetadataValue;
import org.bonitasoft.engine.identity.model.SRole;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.SUserMembership;
import org.bonitasoft.engine.identity.model.builder.SContactInfoBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.SContactInfoLogBuilder;
import org.bonitasoft.engine.identity.model.builder.SContactInfoLogBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.SGroupLogBuilder;
import org.bonitasoft.engine.identity.model.builder.SGroupLogBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.SProfileMetadataDefinitionLogBuilder;
import org.bonitasoft.engine.identity.model.builder.SProfileMetadataDefinitionLogBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.SRoleLogBuilder;
import org.bonitasoft.engine.identity.model.builder.SRoleLogBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.SUserBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.SUserLogBuilder;
import org.bonitasoft.engine.identity.model.builder.SUserLogBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.SUserMembershipLogBuilder;
import org.bonitasoft.engine.identity.model.builder.SUserMembershipLogBuilderFactory;
import org.bonitasoft.engine.identity.recorder.SelectDescriptorBuilder;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
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
 * Default implementation of the Identity service
 * 
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Bole Zhang
 * @author Hongwen Zang
 * @author Celine Souchet
 */
public class IdentityServiceImpl implements IdentityService {

    private final ReadPersistenceService persistenceService;

    private final Recorder recorder;

    private final TechnicalLoggerService logger;

    private final QueriableLoggerService queriableLoggerService;

    private final EventService eventService;

    private final CredentialsEncrypter encrypter;

    public IdentityServiceImpl(final ReadPersistenceService persistenceService, final Recorder recorder, final EventService eventService,
            final TechnicalLoggerService logger, final QueriableLoggerService queriableLoggerService,
            final CredentialsEncrypter encrypter) {
        this.persistenceService = persistenceService;
        this.recorder = recorder;
        this.eventService = eventService;
        this.logger = logger;
        this.queriableLoggerService = queriableLoggerService;
        this.encrypter = encrypter;
    }

    @Override
    public void createGroup(final SGroup group) throws SGroupCreationException {
        logBeforeMethod("createGroup");

        final long objectId = group.getId();
        final SGroupLogBuilder logBuilder = getGroupLog(ActionType.CREATED, "Adding a new group with name " + group.getName());
        try {

            final InsertRecord insertRecord = new InsertRecord(group);
            final SInsertEvent insertEvent = getInsertEvent(group, GROUP);
            recorder.recordInsert(insertRecord, insertEvent);
            final int status = SQueriableLog.STATUS_OK;
            initiateLogBuilder(insertRecord.getEntity().getId(), status, logBuilder, "createGroup");
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "createGroup"));
            }
        } catch (final SRecorderException re) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "createGroup", re));
            }
            final int status = SQueriableLog.STATUS_FAIL;
            initiateLogBuilder(objectId, status, logBuilder, "createGroup");
            throw new SGroupCreationException(re);
        }
    }

    private SInsertEvent getInsertEvent(final Object object, final String type) {
        if (eventService.hasHandlers(type, EventActionType.CREATED)) {
            return (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent(type).setObject(object).done();
        } else {
            return null;
        }
    }

    private SDeleteEvent getDeleteEvent(final Object object, final String type) {
        if (eventService.hasHandlers(type, EventActionType.DELETED)) {
            return (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(type).setObject(object).done();
        } else {
            return null;
        }
    }

    @Override
    public void createProfileMetadataDefinition(final SProfileMetadataDefinition metadata) throws SIdentityException {
        logBeforeMethod("createProfileMetadataDefinition");
        final SProfileMetadataDefinitionLogBuilder logBuilder = getSProfileMetadataDefinitionLog(ActionType.CREATED, "Adding a profile metadata with name "
                + metadata.getName());
        try {
            final InsertRecord insertRecord = new InsertRecord(metadata);
            final SInsertEvent insertEvent = getInsertEvent(metadata, METADATA);
            recorder.recordInsert(insertRecord, insertEvent);
            initiateLogBuilder(metadata.getId(), SQueriableLog.STATUS_OK, logBuilder, "createProfileMetadataDefinition");
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "createProfileMetadataDefinition"));
            }
        } catch (final SRecorderException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "createProfileMetadataDefinition", e));
            }
            initiateLogBuilder(metadata.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "createProfileMetadataDefinition");
            throw new SIdentityException("Can't add profile metadata " + metadata, e);
        }
    }

    @Override
    public void createProfileMetadataValue(final SProfileMetadataValue metadataValue) throws SIdentityException {
        logBeforeMethod("createProfileMetadataValue");
        try {
            final InsertRecord insertRecord = new InsertRecord(metadataValue);
            final SInsertEvent insertEvent = getInsertEvent(metadataValue, METADATAVALUE);
            recorder.recordInsert(insertRecord, insertEvent);
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "createProfileMetadataValue"));
            }
        } catch (final SRecorderException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "createProfileMetadataValue", e));
            }
            throw new SIdentityException("Can't add metadata value " + metadataValue, e);
        }
    }

    @Override
    public void createRole(final SRole role) throws SIdentityException {
        logBeforeMethod("createRole");
        final SRoleLogBuilder logBuilder = getRoleLog(ActionType.CREATED, "Adding a new role with name " + role.getName());
        try {
            final InsertRecord insertRecord = new InsertRecord(role);
            final SInsertEvent insertEvent = getInsertEvent(role, ROLE);
            recorder.recordInsert(insertRecord, insertEvent);
            initiateLogBuilder(role.getId(), SQueriableLog.STATUS_OK, logBuilder, "createRole");
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "createRole"));
            }
        } catch (final SRecorderException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "createRole", e));
            }
            initiateLogBuilder(role.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "createRole");
            throw new SIdentityException("Can't add role " + role, e);
        }
    }

    @Override
    public SUser createUser(final SUser user) throws SUserCreationException {
        logBeforeMethod("createUser");
        final String message = "Adding a new user with user name " + user.getUserName() + ", first name " + user.getFirstName() + ", last name "
                + user.getLastName();
        final SUserLogBuilder logBuilder = getUserLog(ActionType.CREATED, message);

        final String hash = encrypter.hash(user.getPassword());
        final SUser hashedUser = BuilderFactory.get(SUserBuilderFactory.class).createNewInstance(user).setPassword(hash).done();
        try {
            final InsertRecord insertRecord = new InsertRecord(hashedUser);
            final SInsertEvent insertEvent = getInsertEvent(hashedUser, USER);
            recorder.recordInsert(insertRecord, insertEvent);
            initiateLogBuilder(hashedUser.getId(), SQueriableLog.STATUS_OK, logBuilder, "createUser");
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "createUser"));
            }
            return hashedUser;
        } catch (final SRecorderException re) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "createUser", re));
            }
            initiateLogBuilder(hashedUser.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "createUser");
            throw new SUserCreationException(re);
        }
    }

    @Override
    public SContactInfo createUserContactInfo(final SContactInfo contactInfo) throws SUserCreationException {
        logBeforeMethod("createUserContactInfo");
        final String message = "Adding a new user contact information for user with id " + contactInfo.getUserId();
        final SContactInfoLogBuilder logBuilder = getUserContactInfoLog(ActionType.CREATED, message, contactInfo);
        try {
            final InsertRecord insertRecord = new InsertRecord(contactInfo);
            final SInsertEvent insertEvent = getInsertEvent(contactInfo, USER_CONTACT_INFO);
            recorder.recordInsert(insertRecord, insertEvent);
            initiateLogBuilder(contactInfo.getId(), SQueriableLog.STATUS_OK, logBuilder, "createUserContactInfo");
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "createUserContactInfo"));
            }
            return contactInfo;
        } catch (final SRecorderException re) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "createUserContactInfo", re));
            }
            initiateLogBuilder(contactInfo.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "createUserContactInfo");
            throw new SUserCreationException(re);
        }
    }

    @Override
    public void createUserMembership(final SUserMembership userMembership) throws SUserMembershipCreationException {
        logBeforeMethod("createUserMembership");
        final String message = "Adding a new user membership for user " + userMembership.getUsername() + " with role " + userMembership.getRoleName()
                + " in group " + userMembership.getGroupName();

        final SUserMembershipLogBuilder logBuilder = getUserMembershipLog(ActionType.CREATED, message, userMembership);
        try {
            final InsertRecord insertRecord = new InsertRecord(userMembership);
            final SInsertEvent insertEvent = getInsertEvent(userMembership, USERMEMBERSHIP);
            recorder.recordInsert(insertRecord, insertEvent);
            initiateLogBuilder(userMembership.getId(), SQueriableLog.STATUS_OK, logBuilder, "createUserMembership");
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "createUserMembership"));
            }
        } catch (final SRecorderException re) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "createUserMembership", re));
            }
            initiateLogBuilder(userMembership.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "createUserMembership");
            throw new SUserMembershipCreationException(re);
        }
    }

    @Override
    public void deleteGroup(final long groupId) throws SGroupNotFoundException, SGroupDeletionException {
        final SGroup group = getGroup(groupId);
        this.deleteGroup(group);
    }

    @Override
    public void deleteGroup(final SGroup group) throws SGroupDeletionException {
        logBeforeMethod("deleteGroup");
        final SGroupLogBuilder logBuilder = getGroupLog(ActionType.DELETED, "Deleting group " + group.getName());
        try {
            final DeleteRecord deleteRecord = new DeleteRecord(group);
            final SDeleteEvent deleteEvent = getDeleteEvent(group, GROUP);
            recorder.recordDelete(deleteRecord, deleteEvent);
            initiateLogBuilder(group.getId(), SQueriableLog.STATUS_OK, logBuilder, "deleteGroup");
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "deleteGroup"));
            }
        } catch (final SRecorderException re) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "deleteGroup", re));
            }
            initiateLogBuilder(group.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "deleteGroup");
            throw new SGroupDeletionException(re);
        }
    }

    @Override
    public void deleteAllGroups() throws SGroupDeletionException {
        try {
            final DeleteAllRecord record = new DeleteAllRecord(SGroup.class, null);
            recorder.recordDeleteAll(record);
        } catch (final SRecorderException e) {
            throw new SGroupDeletionException("Can't delete all groups.", e);
        }
    }

    @Override
    public List<Long> deleteChildrenGroup(final long groupId) throws SGroupDeletionException, SGroupNotFoundException {
        final ArrayList<Long> deletedGroups = new ArrayList<Long>();
        logBeforeMethod("deleteChildrenGroup");
        try {
            List<SGroup> childrenGroup;
            final int nbGroup = 20;
            while (!(childrenGroup = getGroupChildren(groupId, 0, nbGroup)).isEmpty()) {
                for (final SGroup sGroup : childrenGroup) {
                    deletedGroups.addAll(deleteChildrenGroup(sGroup.getId()));
                    deletedGroups.add(sGroup.getId());
                    deleteGroup(sGroup);
                }
            }
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "deleteChildrenGroup"));
            }
        } catch (final SGroupNotFoundException e) {
            throw e;
        } catch (final SIdentityException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "deleteChildrenGroup", e));
            }
            throw new SGroupDeletionException(e);
        }
        return deletedGroups;
    }

    private <T extends SLogBuilder> void initializeLogBuilder(final T logBuilder, final String message) {
        logBuilder.actionStatus(SQueriableLog.STATUS_FAIL).severity(SQueriableLogSeverity.INTERNAL).rawMessage(message);
    }

    private <T extends HasCRUDEAction> void updateLog(final ActionType actionType, final T logBuilder) {
        logBuilder.setActionType(actionType);
    }

    private SGroupLogBuilder getGroupLog(final ActionType actionType, final String message) {
        final SGroupLogBuilder logBuilder = BuilderFactory.get(SGroupLogBuilderFactory.class).createNewInstance();
        this.initializeLogBuilder(logBuilder, message);
        this.updateLog(actionType, logBuilder);
        return logBuilder;
    }

    private SRoleLogBuilder getRoleLog(final ActionType actionType, final String message) {
        final SRoleLogBuilder logBuilder = BuilderFactory.get(SRoleLogBuilderFactory.class).createNewInstance();
        this.initializeLogBuilder(logBuilder, message);
        this.updateLog(actionType, logBuilder);
        return logBuilder;
    }

    private SProfileMetadataDefinitionLogBuilder getSProfileMetadataDefinitionLog(final ActionType actionType, final String message) {
        final SProfileMetadataDefinitionLogBuilder logBuilder = BuilderFactory.get(SProfileMetadataDefinitionLogBuilderFactory.class).createNewInstance();
        this.initializeLogBuilder(logBuilder, message);
        this.updateLog(actionType, logBuilder);
        return logBuilder;
    }

    private SUserLogBuilder getUserLog(final ActionType actionType, final String message) {
        final SUserLogBuilder logBuilder = BuilderFactory.get(SUserLogBuilderFactory.class).createNewInstance();
        this.initializeLogBuilder(logBuilder, message);
        this.updateLog(actionType, logBuilder);
        return logBuilder;
    }

    private SContactInfoLogBuilder getUserContactInfoLog(final ActionType actionType, final String message, final SContactInfo contactInfo) {
        final SContactInfoLogBuilder logBuilder = BuilderFactory.get(SContactInfoLogBuilderFactory.class).createNewInstance();
        this.initializeLogBuilder(logBuilder, message);
        this.updateLog(actionType, logBuilder);
        logBuilder.setContactInfoUserId(contactInfo.getUserId());
        return logBuilder;
    }

    private SUserMembershipLogBuilder getUserMembershipLog(final ActionType actionType, final String message, final SUserMembership userMemberShip) {
        final SUserMembershipLogBuilder logBuilder = BuilderFactory.get(SUserMembershipLogBuilderFactory.class).createNewInstance();
        this.initializeLogBuilder(logBuilder, message);
        this.updateLog(actionType, logBuilder);
        logBuilder.identityUserId(userMemberShip.getUserId());
        logBuilder.roleID(userMemberShip.getRoleId());
        logBuilder.groupId(userMemberShip.getGroupId());
        return logBuilder;
    }

    @Override
    public void deleteProfileMetadataDefinition(final long metadataDefinitionId) throws SIdentityException {
        this.deleteProfileMetadataDefinition(this.getProfileMetadataDefinition(metadataDefinitionId));
    }

    @Override
    public void deleteProfileMetadataDefinition(final SProfileMetadataDefinition metadata) throws SIdentityException {
        logBeforeMethod("deleteProfileMetadataDefinition");
        final SProfileMetadataDefinitionLogBuilder logBuilder = getSProfileMetadataDefinitionLog(ActionType.DELETED, "Deleting profile metadata with name "
                + metadata.getName());
        try {
            final DeleteRecord deleteRecord = new DeleteRecord(metadata);
            final SDeleteEvent deleteEvent = getDeleteEvent(metadata, METADATA);
            recorder.recordDelete(deleteRecord, deleteEvent);
            initiateLogBuilder(metadata.getId(), SQueriableLog.STATUS_OK, logBuilder, "deleteProfileMetadataDefinition");
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "deleteProfileMetadataDefinition"));
            }
        } catch (final SRecorderException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "deleteProfileMetadataDefinition", e));
            }
            initiateLogBuilder(metadata.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "deleteProfileMetadataDefinition");
            throw new SIdentityException("Can't delete profile metadata " + metadata, e);
        }
    }

    @Override
    public void deleteProfileMetadataValue(final long metadataValueId) throws SIdentityException {
        this.deleteProfileMetadataValue(getProfileMetadataValue(metadataValueId));
    }

    @Override
    public void deleteProfileMetadataValue(final SProfileMetadataValue metadataValue) throws SIdentityException {
        logBeforeMethod("deleteProfileMetadataValue");
        try {
            final DeleteRecord deleteRecord = new DeleteRecord(metadataValue);
            final SDeleteEvent deleteEvent = getDeleteEvent(metadataValue, METADATAVALUE);
            recorder.recordDelete(deleteRecord, deleteEvent);
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "deleteProfileMetadataDefinition"));
            }
        } catch (final SRecorderException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "deleteProfileMetadataValue", e));
            }
            throw new SIdentityException("Can't delete membership " + metadataValue, e);
        }
    }

    @Override
    public void deleteRole(final long roleId) throws SRoleNotFoundException, SRoleDeletionException {
        final SRole role = getRole(roleId);
        this.deleteRole(role);
    }

    @Override
    public void deleteRole(final SRole role) throws SRoleDeletionException {
        logBeforeMethod("deleteRole");
        final SRoleLogBuilder logBuilder = getRoleLog(ActionType.DELETED, "Deleting role with name " + role.getName());
        try {
            final DeleteRecord deleteRecord = new DeleteRecord(role);
            final SDeleteEvent deleteEvent = getDeleteEvent(role, ROLE);
            recorder.recordDelete(deleteRecord, deleteEvent);
            initiateLogBuilder(role.getId(), SQueriableLog.STATUS_OK, logBuilder, "deleteRole");
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "deleteRole"));
            }
        } catch (final SRecorderException re) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "deleteRole", re));
            }
            initiateLogBuilder(role.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "deleteRole");
            throw new SRoleDeletionException(re);
        }
    }

    @Override
    public void deleteAllRoles() throws SRoleDeletionException {
        try {
            final DeleteAllRecord record = new DeleteAllRecord(SRole.class, null);
            recorder.recordDeleteAll(record);
        } catch (final SRecorderException e) {
            throw new SRoleDeletionException("Can't delete all roles.", e);
        }
    }

    @Override
    public void deleteUser(final long userId) throws SUserDeletionException {
        SUser user;
        try {
            user = getUser(userId);
            deleteUser(user);
        } catch (final SUserNotFoundException e) {
            // ignored, let's switch to the next one
        }
    }

    @Override
    public void deleteUser(final SUser user) throws SUserDeletionException {
        logBeforeMethod("deleteUser");
        final SUserLogBuilder logBuilder = getUserLog(ActionType.DELETED, "Deleting user with username " + user.getUserName());
        try {
            final DeleteRecord deleteRecord = new DeleteRecord(user);
            final SDeleteEvent deleteEvent = getDeleteEvent(user, USER);
            recorder.recordDelete(deleteRecord, deleteEvent);
            initiateLogBuilder(user.getId(), SQueriableLog.STATUS_OK, logBuilder, "deleteUser");
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "deleteUser"));
            }
        } catch (final SRecorderException re) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "deleteUser", re));
            }
            initiateLogBuilder(user.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "deleteUser");
            throw new SUserDeletionException(re);
        }
    }

    @Override
    public void deleteAllUsers() throws SUserDeletionException {
        try {
            final DeleteAllRecord record = new DeleteAllRecord(SUser.class, null);
            recorder.recordDeleteAll(record);
        } catch (final SRecorderException e) {
            throw new SUserDeletionException("Can't delete all users.", e);
        }
    }

    @Override
    public SUserMembership getLightUserMembership(final long userMembershipId) throws SIdentityException {
        logBeforeMethod("getLightUserMembership");
        try {
            final SUserMembership selectOne = persistenceService.selectById(SelectDescriptorBuilder.getLightElementById(SUserMembership.class,
                    "SUserMembership", userMembershipId));
            if (selectOne == null) {
                throw new SIdentityException("Can't get the userMembership with id " + userMembershipId, null);
            }
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getLightUserMembership"));
            }
            return selectOne;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getLightUserMembership", e));
            }
            throw new SIdentityException("Can't get the user membership with id " + userMembershipId, e);
        }
    }

    @Override
    public void deleteUserMembership(SUserMembership userMembership) throws SMembershipDeletionException {
        try {
            // fat object, hibernate won't delete id
            if (userMembership.getGroupName() != null || userMembership.getUsername() != null || userMembership.getRoleName() != null) {
                userMembership = getLightUserMembership(userMembership.getId());
            }
            deleteLightUserMembership(userMembership);
        } catch (final SIdentityException e) {
            throw new SMembershipDeletionException("Can't delete membership " + userMembership, e);
        }
    }

    @Override
    public void deleteUserMembership(final long id) throws SMembershipDeletionException {
        try {
            final SUserMembership userMembership = getLightUserMembership(id);
            deleteLightUserMembership(userMembership);
        } catch (final SIdentityException e) {
            throw new SMembershipDeletionException("Can't delete membership with id " + id, e);
        }
    }

    @Override
    public void deleteLightUserMembership(final SUserMembership userMembership) throws SMembershipDeletionException {
        logBeforeMethod("deleteLightUserMembership");
        final String message = "Deleting user membership for user " + userMembership.getUsername() + " with role " + userMembership.getRoleName()
                + " in group " + userMembership.getGroupName();
        final SUserMembershipLogBuilder logBuilder = getUserMembershipLog(ActionType.DELETED, message, userMembership);
        try {
            final DeleteRecord deleteRecord = new DeleteRecord(userMembership);
            final SDeleteEvent deleteEvent = getDeleteEvent(userMembership, USERMEMBERSHIP);
            recorder.recordDelete(deleteRecord, deleteEvent);
            initiateLogBuilder(userMembership.getId(), SQueriableLog.STATUS_OK, logBuilder, "deleteUserMembership");
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "deleteUserMembership"));
            }
        } catch (final SRecorderException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "deleteUserMembership", e));
            }
            initiateLogBuilder(userMembership.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "deleteUserMembership");
            throw new SMembershipDeletionException("Can't delete membership " + userMembership, e);
        }
    }

    @Override
    public void deleteAllUserMemberships() throws SMembershipDeletionException {
        try {
            final DeleteAllRecord record = new DeleteAllRecord(SUserMembership.class, null);
            recorder.recordDeleteAll(record);
        } catch (final SRecorderException e) {
            throw new SMembershipDeletionException("Can't delete all user memberships.", e);
        }
    }

    @Override
    public SGroup getGroup(final long groupId) throws SGroupNotFoundException {
        logBeforeMethod("getGroup");
        try {
            final SGroup group = persistenceService.selectById(SelectDescriptorBuilder.getElementById(SGroup.class, "Group", groupId));
            if (group == null) {
                throw new SGroupNotFoundException("No group exists with id: " + groupId);
            }
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getGroup"));
            }
            return group;
        } catch (final SBonitaReadException bre) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getGroup", bre));
            }
            throw new SGroupNotFoundException(bre);
        }
    }

    @Override
    public List<SGroup> getGroupChildren(final long groupId) throws SIdentityException {
        logBeforeMethod("getGroupChildren");
        try {
            final SGroup group = getGroup(groupId);
            final List<SGroup> listGroups = persistenceService.selectList(SelectDescriptorBuilder.getChildrenOfGroup(group));
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getGroupChildren"));
            }
            return listGroups;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getGroupChildren", e));
            }
            throw new SIdentityException("Can't get the children of the group", e);
        }
    }

    @Override
    public List<SGroup> getGroupChildren(final long groupId, final int fromIndex, final int numberOfGroups) throws SIdentityException {
        logBeforeMethod("getGroupChildren");
        try {
            final SGroup group = getGroup(groupId);
            final List<SGroup> listGroups = persistenceService.selectList(SelectDescriptorBuilder.getChildrenOfGroup(group, fromIndex, numberOfGroups));
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getGroupChildren"));
            }
            return listGroups;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getGroupChildren", e));
            }
            throw new SIdentityException("Can't get the children of the group", e);
        }
    }

    @Override
    public List<SGroup> getGroupChildren(final long groupId, final int fromIndex, final int numberOfGroups, final String field, final OrderByType order)
            throws SIdentityException {
        logBeforeMethod("getGroupChildren");
        try {
            final SGroup group = getGroup(groupId);
            final List<SGroup> listGroups = persistenceService.selectList(SelectDescriptorBuilder.getChildrenOfGroup(group, field, order, fromIndex,
                    numberOfGroups));
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getGroupChildren"));
            }
            return listGroups;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getGroupChildren", e));
            }
            throw new SIdentityException("Can't get the children of the group", e);
        }
    }

    @Override
    public List<SGroup> getGroups(final int fromIndex, final int numberOfGroups) throws SIdentityException {
        logBeforeMethod("getGroups");
        try {
            final List<SGroup> listGroups = persistenceService
                    .selectList(SelectDescriptorBuilder.getElements(SGroup.class, "Group", fromIndex, numberOfGroups));
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getGroups"));
            }
            return listGroups;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getGroups", e));
            }
            throw new SIdentityException("Can't get the groups", e);
        }
    }

    @Override
    public List<SGroup> getGroups(final int fromIndex, final int numberOfGroups, final String field, final OrderByType order) throws SIdentityException {
        logBeforeMethod("getGroups");
        try {
            final List<SGroup> listGroups = persistenceService.selectList(SelectDescriptorBuilder.getElements(SGroup.class, "Group", field, order, fromIndex,
                    numberOfGroups));
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getGroups"));
            }
            return listGroups;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getGroups", e));
            }
            throw new SIdentityException("Can't get the groups", e);
        }
    }

    @Override
    public List<SGroup> getGroups(final List<Long> groupIds) throws SGroupNotFoundException {
        logBeforeMethod("getGroups");
        if (groupIds == null || groupIds.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            final List<SGroup> listGroups = persistenceService.selectList(SelectDescriptorBuilder.getElementsByIds(SGroup.class, "Group", groupIds));
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getGroups"));
            }
            return listGroups;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getGroups", e));
            }
            throw new SGroupNotFoundException(e);
        }
    }

    @Override
    public Set<SGroup> getGroupsByName(final String groupName) throws SGroupNotFoundException {
        logBeforeMethod("getGroupsByName");
        try {
            final Set<SGroup> setGroups = CollectionUtil.buildHashSetFromList(SGroup.class,
                    persistenceService.selectList(SelectDescriptorBuilder.getGroupsByName(groupName)));
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getGroupsByName"));
            }
            return setGroups;
        } catch (final SBonitaReadException bre) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getGroupsByName", bre));
            }
            throw new SGroupNotFoundException(bre);
        }
    }

    @Override
    public List<SUserMembership> getUserMemberships(final int fromIndex, final int numberOfResult, final OrderByOption orderByOption) throws SIdentityException {
        logBeforeMethod("getUserMemberships");
        try {
            List<SUserMembership> listSUserMembership;
            if (orderByOption.getClazz() == SRole.class) {
                listSUserMembership = persistenceService.selectList(SelectDescriptorBuilder.getUserMembershipsWithRole(new QueryOptions(fromIndex,
                        numberOfResult, Collections.singletonList(orderByOption))));
            } else if (orderByOption.getClazz() == SGroup.class) {
                listSUserMembership = persistenceService.selectList(SelectDescriptorBuilder.getUserMembershipsWithGroup(new QueryOptions(fromIndex,
                        numberOfResult, Collections.singletonList(orderByOption))));
            } else {
                listSUserMembership = persistenceService.selectList(SelectDescriptorBuilder.getElements(SUserMembership.class, "UserMembership",
                        new QueryOptions(fromIndex, numberOfResult, Collections.singletonList(orderByOption))));
            }
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getUserMemberships"));
            }
            return listSUserMembership;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getUserMemberships", e));
            }
            throw new SIdentityException("Can't get the memberships", e);
        }
    }

    @Override
    public List<SUserMembership> getUserMembershipsOfGroup(final long groupId, final int startIndex, final int maxResults) throws SIdentityException {
        logBeforeMethod("getUserMembershipsOfGroup");
        try {
            final List<SUserMembership> selectList = persistenceService.selectList(SelectDescriptorBuilder.getUserMembershipsByGroup(groupId, startIndex,
                    maxResults));
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getUserMembershipsOfGroup"));
            }
            return selectList;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getUserMembershipsOfGroup", e));
            }
            throw new SIdentityException("Can't get the users memberships the group " + groupId, e);
        }
    }

    @Override
    public List<SUserMembership> getUserMembershipsOfRole(final long roleId, final int startIndex, final int maxResults) throws SIdentityException {
        logBeforeMethod("getUserMembershipsOfRole");
        try {
            final List<SUserMembership> memberships = persistenceService.selectList(SelectDescriptorBuilder.getUserMembershipsByRole(roleId, startIndex,
                    maxResults));
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getUserMembershipsOfRole"));
            }
            return memberships;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getUserMembershipsOfRole", e));
            }
            throw new SIdentityException("Can't get the memberships having the role " + roleId, e);
        }
    }

    @Override
    public long getNumberOfGroupChildren(final long parentGroupId) throws SIdentityException {
        logBeforeMethod("getNumberOfGroupChildren");
        try {
            final SGroup parentGroup = getGroup(parentGroupId);
            final long number = persistenceService.selectOne(SelectDescriptorBuilder.getNumberOfGroupChildren(parentGroup.getPath()));
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getNumberOfGroupChildren"));
            }
            return number;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getNumberOfGroupChildren", e));
            }
            throw new SIdentityException("Can't get the number children of group", e);
        }
    }

    @Override
    public long getNumberOfGroups() throws SIdentityException {
        logBeforeMethod("getNumberOfGroups");
        try {
            final long number = persistenceService.selectOne(SelectDescriptorBuilder.getNumberOfElement("SGroup", SGroup.class));
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getNumberOfGroups"));
            }
            return number;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getNumberOfGroups", e));
            }
            throw new SIdentityException("Can't get the number of group", e);
        }
    }

    @Override
    public long getNumberOfProfileMetadataDefinition() throws SIdentityException {
        logBeforeMethod("getNumberOfProfileMetadataDefinition");
        try {
            final long number = persistenceService.selectOne(SelectDescriptorBuilder.getNumberOfElement("ProfileMetadata", SProfileMetadataDefinition.class));
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getNumberOfProfileMetadataDefinition"));
            }
            return number;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE,
                        LogUtil.getLogOnExceptionMethod(this.getClass(), "getNumberOfProfileMetadataDefinition", e));
            }
            throw new SIdentityException("Can't get the number of profile metadata", e);
        }
    }

    @Override
    public long getNumberOfRoles() throws SIdentityException {
        logBeforeMethod("getNumberOfRoles");
        try {
            final long number = persistenceService.selectOne(SelectDescriptorBuilder.getNumberOfElement("SRole", SRole.class));
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getNumberOfRoles"));
            }
            return number;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getNumberOfRoles", e));
            }
            throw new SIdentityException("Can't get the number of role", e);
        }
    }

    @Override
    public long getNumberOfUsers() throws SIdentityException {
        logBeforeMethod("getNumberOfUsers");
        try {
            final long number = persistenceService.selectOne(SelectDescriptorBuilder.getNumberOfElement("SUser", SUser.class));
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getNumberOfUsers"));
            }
            return number;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getNumberOfUsers", e));
            }
            throw new SIdentityException("Can't get the number of user", e);
        }
    }

    @Override
    public long getNumberOfUserMembershipsOfUser(final long userId) throws SIdentityException {
        logBeforeMethod("getNumberOfUserMembershipsOfUser");
        try {
            final long number = persistenceService.selectOne(SelectDescriptorBuilder.getNumberOfUserMembershipsOfUser(userId));
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getNumberOfUserMembershipsOfUser"));
            }
            return number;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getNumberOfUserMembershipsOfUser", e));
            }
            throw new SIdentityException("Can't get the number of usermemberships having the user " + userId, e);
        }
    }

    @Override
    public long getNumberOfUsersByGroup(final long groupId) throws SIdentityException {
        logBeforeMethod("getNumberOfUsersByGroup");
        try {
            final long number = persistenceService.selectOne(SelectDescriptorBuilder.getNumberOfUsersByGroup(groupId));
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getNumberOfUsersByGroup"));
            }
            return number;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getNumberOfUsersByGroup", e));
            }
            throw new SIdentityException("Can't get the number of users having the group " + groupId, e);
        }
    }

    @Override
    public long getNumberOfUsersByRole(final long roleId) throws SIdentityException {
        logBeforeMethod("getNumberOfUsersByRole");
        try {
            final long number = persistenceService.selectOne(SelectDescriptorBuilder.getNumberOfUsersByRole(roleId));
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getNumberOfUsersByRole"));
            }
            return number;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getNumberOfUsersByRole", e));
            }
            throw new SIdentityException("Can't get the number of users having the role " + roleId, e);
        }
    }

    @Override
    public long getNumberOfUsersByMembership(final long groupId, final long roleId) throws SIdentityException {
        logBeforeMethod("getNumberOfUsersByMembership");
        try {
            final long number = persistenceService.selectOne(SelectDescriptorBuilder.getNumberOfUsersByMembership(groupId, roleId));
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getNumberOfUsersByMembership"));
            }
            return number;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getNumberOfUsersByMembership", e));
            }
            throw new SIdentityException("Can't get the number of users having the membership with group:" + groupId + " and role:" + roleId, e);
        }
    }

    @Override
    public SProfileMetadataDefinition getProfileMetadataByName(final String metadataName) throws SIdentityException {
        logBeforeMethod("getProfileMetadataByName");
        try {
            final SProfileMetadataDefinition sProfileMetadataDefinition = persistenceService.selectOne(SelectDescriptorBuilder.getMetadataByName(metadataName));
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getProfileMetadataByName"));
            }
            return sProfileMetadataDefinition;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getProfileMetadataByName", e));
            }
            throw new SIdentityException("Can't get the metadata with name " + metadataName, e);
        }
    }

    @Override
    public List<SProfileMetadataDefinition> getProfileMetadataDefinition(final int fromIndex, final int numberOfMetadata) throws SIdentityException {
        logBeforeMethod("getProfileMetadataDefinition");
        try {
            final List<SProfileMetadataDefinition> listSProfileMetadataDefinition = persistenceService.selectList(SelectDescriptorBuilder.getElements(
                    SProfileMetadataDefinition.class, "ProfileMetadata", fromIndex, numberOfMetadata));
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getProfileMetadataDefinition"));
            }
            return listSProfileMetadataDefinition;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getProfileMetadataDefinition", e));
            }
            throw new SIdentityException("Can't get the profile metadata", e);
        }
    }

    @Override
    public SProfileMetadataDefinition getProfileMetadataDefinition(final long profileMetadataId) throws SIdentityException {
        logBeforeMethod("getProfileMetadataDefinition");
        try {
            final SProfileMetadataDefinition selectOne = persistenceService.selectById(SelectDescriptorBuilder.getElementById(SProfileMetadataDefinition.class,
                    "ProfileMetadata", profileMetadataId));
            if (selectOne == null) {
                throw new SIdentityException("Can't get the profile metadata with id " + profileMetadataId, null);
            }
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getProfileMetadataDefinition"));
            }
            return selectOne;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getProfileMetadataDefinition", e));
            }
            throw new SIdentityException("Can't get the profile metadata with id " + profileMetadataId, e);
        }
    }

    @Override
    public List<SProfileMetadataDefinition> getProfileMetadataDefinitions(final List<Long> profileMetadataDefinitionIds) throws SIdentityException {
        logBeforeMethod("getProfileMetadataDefinitions");
        if (profileMetadataDefinitionIds == null || profileMetadataDefinitionIds.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            final List<SProfileMetadataDefinition> listSProfileMetadataDefinition = persistenceService.selectList(SelectDescriptorBuilder.getElementsByIds(
                    SProfileMetadataDefinition.class, "SProfileMetadataDefinition", profileMetadataDefinitionIds));
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getProfileMetadataDefinitions"));
            }
            return listSProfileMetadataDefinition;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getProfileMetadataDefinitions", e));
            }
            throw new SIdentityException("Can't get profiles metadata with ids " + Arrays.toString(profileMetadataDefinitionIds.toArray()), e);
        }
    }

    @Override
    public SProfileMetadataValue getProfileMetadataValue(final long profileMetadataValueId) throws SIdentityException {
        logBeforeMethod("getProfileMetadataValue");
        try {
            final SProfileMetadataValue selectOne = persistenceService.selectById(SelectDescriptorBuilder.getElementById(SProfileMetadataValue.class,
                    "SProfileMetadataValue", profileMetadataValueId));
            if (selectOne == null) {
                throw new SIdentityException("Can't get the profile metadata value with id " + profileMetadataValueId, null);
            }
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getProfileMetadataValue"));
            }
            return selectOne;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getProfileMetadataValue", e));
            }
            throw new SIdentityException("Can't get the profile metadata value with id " + profileMetadataValueId, e);
        }
    }

    @Override
    public List<SProfileMetadataValue> getProfileMetadataValues(final List<Long> profileMetadataValueIds) throws SIdentityException {
        logBeforeMethod("getProfileMetadataValues");
        if (profileMetadataValueIds == null || profileMetadataValueIds.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getProfileMetadataValues"));
            }
            return persistenceService.selectList(SelectDescriptorBuilder.getElementsByIds(SProfileMetadataValue.class, "SProfileMetadataValue",
                    profileMetadataValueIds));
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getProfileMetadataValues", e));
            }
            throw new SIdentityException("Can't get profiles metadata values with ids " + Arrays.toString(profileMetadataValueIds.toArray()), e);
        }
    }

    @Override
    public SRole getRole(final long roleId) throws SRoleNotFoundException {
        logBeforeMethod("getRole");
        try {
            final SRole selectOne = persistenceService.selectById(SelectDescriptorBuilder.getElementById(SRole.class, "Role", roleId));
            if (selectOne == null) {
                throw new SRoleNotFoundException("The role with id= " + roleId + " does not exist");
            }
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getRole"));
            }
            return selectOne;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getRole", e));
            }
            throw new SRoleNotFoundException(e);
        }
    }

    @Override
    public SRole getRoleByName(final String roleName) throws SRoleNotFoundException {
        logBeforeMethod("getRoleByName");
        try {
            final SRole role = persistenceService.selectOne(SelectDescriptorBuilder.getRoleByName(roleName));
            if (role == null) {
                throw new SRoleNotFoundException("The role named " + roleName + " does not exist");
            }
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getRoleByName"));
            }
            return role;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getRoleByName", e));
            }
            throw new SRoleNotFoundException(e);
        }
    }

    @Override
    public SGroup getGroupByPath(final String groupPath) throws SGroupNotFoundException {
        logBeforeMethod("getGroupByPath");
        SelectOneDescriptor<SGroup> descriptor;
        final int lastIndexOf = groupPath.lastIndexOf('/');
        if (lastIndexOf > 0) {
            final String groupName = groupPath.substring(lastIndexOf + 1);
            final String parentPath = groupPath.substring(0, lastIndexOf);
            descriptor = SelectDescriptorBuilder.getGroupByPath(parentPath, groupName);
        } else if (lastIndexOf == 0) {
            final String groupName = groupPath.substring(lastIndexOf + 1);
            descriptor = SelectDescriptorBuilder.getGroupByName(groupName);
        } else {
            descriptor = SelectDescriptorBuilder.getGroupByName(groupPath);
        }
        try {
            final SGroup group = persistenceService.selectOne(descriptor);
            if (group == null) {
                throw new SGroupNotFoundException("The group '" + groupPath + "' does not exist");
            }
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getGroupByPath"));
            }
            return group;
        } catch (final SBonitaReadException bre) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getGroupByPath", bre));
            }
            throw new SGroupNotFoundException(bre);
        }
    }

    @Override
    public List<SRole> getRoles(final int fromIndex, final int numberOfRoles) throws SIdentityException {
        logBeforeMethod("getRoles");
        try {
            final List<SRole> listSRole = persistenceService.selectList(SelectDescriptorBuilder.getElements(SRole.class, "Role", fromIndex, numberOfRoles));
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getRoles"));
            }
            return listSRole;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getRoles", e));
            }
            throw new SIdentityException("Can't get the roles", e);
        }
    }

    @Override
    public List<SRole> getRoles(final int fromIndex, final int numberOfRoles, final String field, final OrderByType order) throws SIdentityException {
        logBeforeMethod("getRoles");
        try {
            final List<SRole> listSRole = persistenceService.selectList(SelectDescriptorBuilder.getElements(SRole.class, "Role", field, order, fromIndex,
                    numberOfRoles));
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getRoles"));
            }
            return listSRole;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getRoles", e));
            }
            throw new SIdentityException("Can't get the roles", e);
        }
    }

    @Override
    public List<SRole> getRoles(final List<Long> roleIds) throws SRoleNotFoundException {
        logBeforeMethod("getRoles");
        if (roleIds == null || roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            final List<SRole> listSRoles = persistenceService.selectList(SelectDescriptorBuilder.getElementsByIds(SRole.class, "Role", roleIds));
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getRoles"));
            }
            return listSRoles;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getRoles", e));
            }
            throw new SRoleNotFoundException(e);
        }
    }

    @Override
    public SUser getUser(final long userId) throws SUserNotFoundException {
        logBeforeMethod("getUser");
        try {
            final SUser user = persistenceService.selectById(SelectDescriptorBuilder.getElementById(SUser.class, "User", userId));
            if (user == null) {
                throw new SUserNotFoundException(userId);
            }
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getUser"));
            }
            return user;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getUser", e));
            }
            throw new SUserNotFoundException("Cannot get user with id: " + userId, e);
        }
    }

    @Override
    public SContactInfo getUserContactInfo(final long userId, final boolean isPersonal) throws SIdentityException {
        logBeforeMethod("getUserContactInfo");
        try {
            final SContactInfo contactInfo = persistenceService.selectOne(SelectDescriptorBuilder.getUserContactInfo(userId, isPersonal));
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getUserContactInfo"));
            }
            return contactInfo;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getUserContactInfo", e));
            }
            throw new SUserNotFoundException("Cannot get user contact info for user id: " + userId, e);
        }
    }

    @Override
    public SUser getUserByUserName(final String userName) throws SUserNotFoundException {
        logBeforeMethod("getUserByUserName");
        try {
            final SUser user = persistenceService.selectOne(SelectDescriptorBuilder.getUserByUserName(userName));
            if (user == null) {
                throw new SUserNotFoundException(userName);
            }
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getUserByUserName"));
            }
            return user;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getUserByUserName", e));
            }
            throw new SUserNotFoundException("Cannot get user: " + userName, e);
        }
    }

    @Override
    public SUserMembership getUserMembership(final long userMembershipId) throws SIdentityException {
        logBeforeMethod("getUserMembership");
        try {
            final Map<String, Object> parameters = Collections.singletonMap("id", (Object) userMembershipId);
            final SelectOneDescriptor<SUserMembership> desc = new SelectOneDescriptor<SUserMembership>("getSUserMembershipById", parameters,
                    SUserMembership.class);
            final SUserMembership selectOne = persistenceService.selectOne(desc);
            if (selectOne == null) {
                throw new SIdentityException("Can't get the userMembership with id " + userMembershipId, null);
            }
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getUserMembership"));
            }
            return selectOne;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getUserMembership", e));
            }
            throw new SIdentityException("Can't get the user membership with id " + userMembershipId, e);
        }
    }

    @Override
    public List<SUserMembership> getUserMembershipsOfUser(final long userId) throws SIdentityException {
        logBeforeMethod("getUserMembershipsOfUser");
        try {
            final List<SUserMembership> listSUserMembership = persistenceService.selectList(SelectDescriptorBuilder.getUserMembershipsOfUser(userId));
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getUserMembershipsOfUser"));
            }
            return listSUserMembership;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getUserMembershipsOfUser", e));
            }
            throw new SIdentityException("Can't get memberships of user: " + userId, e);
        }
    }

    @Override
    public List<SUserMembership> getUserMemberships(final List<Long> userMembershipIds) throws SIdentityException {
        logBeforeMethod("getUserMemberships");
        List<Long> localUserMembershipIds = userMembershipIds;
        if (localUserMembershipIds == null || localUserMembershipIds.isEmpty()) {
            localUserMembershipIds = Collections.emptyList();
        }
        try {
            final List<SUserMembership> listSUserMembership = persistenceService.selectList(SelectDescriptorBuilder.getElementsByIds(SUserMembership.class,
                    "SUserMembership", localUserMembershipIds));
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getUserMemberships"));
            }
            return listSUserMembership;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getUserMemberships", e));
            }
            throw new SIdentityException("Can't get users memberships with ids " + Arrays.toString(localUserMembershipIds.toArray()), e);
        }
    }

    @Override
    public List<SUser> getUsers(final int fromIndex, final int numberOfUsers) throws SIdentityException {
        logBeforeMethod("getUsers");
        try {
            final List<SUser> listSUsers = persistenceService.selectList(SelectDescriptorBuilder.getElements(SUser.class, "User", fromIndex, numberOfUsers));
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getUsers"));
            }
            return listSUsers;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getUsers", e));
            }
            throw new SIdentityException("Can't get the users", e);
        }
    }

    @Override
    public List<SUser> getUsers(final int fromIndex, final int numberOfUsers, final String field, final OrderByType order) throws SIdentityException {
        logBeforeMethod("getUsers");
        try {
            final List<SUser> listsUsers = persistenceService.selectList(SelectDescriptorBuilder.getElements(SUser.class, "User", field, order, fromIndex,
                    numberOfUsers));
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getUsers"));
            }
            return listsUsers;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getUsers", e));
            }
            throw new SIdentityException("Can't get the users", e);
        }
    }

    @Override
    public List<SUser> getUsers(final List<Long> userIds) throws SUserNotFoundException {
        logBeforeMethod("getUsers");
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            final List<SUser> listsUsers = persistenceService.selectList(SelectDescriptorBuilder.getElementsByIds(SUser.class, "User", userIds));
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getUsers"));
            }
            return listsUsers;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getUsers", e));
            }
            throw new SUserNotFoundException(e);
        }
    }

    @Override
    public List<SUser> getUsersByUsername(final List<String> userNames) throws SIdentityException {
        logBeforeMethod("getUsersByUsername");
        if (userNames == null || userNames.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            final Map<String, Object> parameters = Collections.singletonMap("userNames", (Object) userNames);
            final List<SUser> users = persistenceService.selectList(new SelectListDescriptor<SUser>("getUsersByName", parameters, SUser.class));
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getUsersByName"));
            }
            return users;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getUsersByName", e));
            }
            throw new SUserNotFoundException(e);
        }
    }

    @Override
    public List<SUser> getUsersByDelegee(final long delegateId) throws SIdentityException {
        logBeforeMethod("getUsersByDelegee");
        try {
            final SUser delegee = getUser(delegateId);
            final List<SUser> listsUsers = persistenceService.selectList(SelectDescriptorBuilder.getUsersByDelegee(delegee.getUserName()));
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getUsersByDelegee"));
            }
            return listsUsers;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getUsersByDelegee", e));
            }
            throw new SIdentityException("Can't get the users having the delegee " + delegateId, e);
        }
    }

    @Override
    public List<SUser> getUsersByGroup(final long groupId) throws SIdentityException {
        logBeforeMethod("getUsersByGroup");
        try {
            final List<SUser> listsUsers = persistenceService.selectList(SelectDescriptorBuilder.getUsersByGroup(groupId));
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getUsersByGroup"));
            }
            return listsUsers;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getUsersByGroup", e));
            }
            throw new SIdentityException("Can't get the users having the group " + groupId, e);
        }
    }

    @Override
    public List<SUser> getUsersByGroup(final long groupId, final int fromIndex, final int numberOfUsers) throws SIdentityException {
        logBeforeMethod("getUsersByGroup");
        try {
            final List<SUser> listsUsers = persistenceService.selectList(SelectDescriptorBuilder.getUsersByGroup(groupId, fromIndex, numberOfUsers));
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getUsersByGroup"));
            }
            return listsUsers;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getUsersByGroup", e));
            }
            throw new SIdentityException("Can't get the users having the group " + groupId, e);
        }
    }

    @Override
    public List<SUser> getUsersByGroup(final long groupId, final int fromIndex, final int numberOfUsers, final String field, final OrderByType order)
            throws SIdentityException {
        logBeforeMethod("getUsersByGroup");
        try {
            final List<SUser> listsSUsers = persistenceService.selectList(SelectDescriptorBuilder.getUsersByGroup(groupId, field, order, fromIndex,
                    numberOfUsers));
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getUsersByGroup"));
            }
            return listsSUsers;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getUsersByGroup", e));
            }
            throw new SIdentityException("Can't get the users having the group " + groupId, e);
        }
    }

    @Override
    public List<SUser> getUsersByManager(final long managerId) throws SIdentityException {
        logBeforeMethod("getUsersByManager");
        try {
            final List<SUser> listsSUsers = persistenceService.selectList(SelectDescriptorBuilder.getUsersByManager(managerId));
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getUsersByManager"));
            }
            return listsSUsers;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getUsersByManager", e));
            }
            throw new SIdentityException("Can't get the users having the manager " + managerId, e);
        }
    }

    @Override
    public List<SUser> getUsersByRole(final long roleId) throws SIdentityException {
        logBeforeMethod("getUsersByRole");
        try {
            final List<SUser> listsUsers = persistenceService.selectList(SelectDescriptorBuilder.getUsersByRole(roleId));
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getUsersByRole"));
            }
            return listsUsers;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getUsersByRole", e));
            }
            throw new SIdentityException("Can't get the users having the role " + roleId, e);
        }
    }

    @Override
    public List<SUser> getUsersByRole(final long roleId, final int fromIndex, final int numberOfUsers) throws SIdentityException {
        logBeforeMethod("getUsersByRole");
        try {
            final List<SUser> listsSUsers = persistenceService.selectList(SelectDescriptorBuilder.getUsersByRole(roleId, fromIndex, numberOfUsers));
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getUsersByRole"));
            }
            return listsSUsers;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getUsersByRole", e));
            }
            throw new SIdentityException("Can't get the users having the role " + roleId, e);
        }
    }

    @Override
    public List<SUser> getUsersByRole(final long roleId, final int fromIndex, final int numberOfUsers, final String field, final OrderByType order)
            throws SIdentityException {
        logBeforeMethod("getUsersByRole");
        try {
            final List<SUser> listsUsers = persistenceService
                    .selectList(SelectDescriptorBuilder.getUsersByRole(roleId, field, order, fromIndex, numberOfUsers));
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getUsersByRole"));
            }
            return listsUsers;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getUsersByRole", e));
            }
            throw new SIdentityException("Can't get the users having the role " + roleId, e);
        }
    }

    @Override
    public void updateGroup(final SGroup group, final EntityUpdateDescriptor descriptor) throws SIdentityException {
        logBeforeMethod("updateGroup");
        final SGroupLogBuilder logBuilder = getGroupLog(ActionType.UPDATED, "Updating the group");
        try {
            final UpdateRecord updateRecord = UpdateRecord.buildSetFields(group, descriptor);
            SUpdateEvent updateEvent = null;
            if (eventService.hasHandlers(GROUP, EventActionType.UPDATED)) {
                updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(GROUP).setObject(group).done();
            }
            recorder.recordUpdate(updateRecord, updateEvent);
            initiateLogBuilder(group.getId(), SQueriableLog.STATUS_OK, logBuilder, "updateGroup");
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "updateGroup"));
            }
        } catch (final SRecorderException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "updateGroup", e));
            }
            initiateLogBuilder(group.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "updateGroup");
            throw new SIdentityException("Can't update group " + group, e);
        }
    }

    @Override
    public void updateProfileMetadataDefinition(final SProfileMetadataDefinition metadata, final EntityUpdateDescriptor descriptor) throws SIdentityException {
        logBeforeMethod("updateProfileMetadataDefinition");
        final SProfileMetadataDefinitionLogBuilder logBuilder = getSProfileMetadataDefinitionLog(ActionType.UPDATED,
                "Updating the profile metadata definition with name " + metadata.getName());
        try {
            final UpdateRecord updateRecord = UpdateRecord.buildSetFields(metadata, descriptor);
            SUpdateEvent updateEvent = null;
            if (eventService.hasHandlers(METADATA, EventActionType.UPDATED)) {
                updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(METADATA).setObject(metadata).done();
            }
            recorder.recordUpdate(updateRecord, updateEvent);
            initiateLogBuilder(metadata.getId(), SQueriableLog.STATUS_OK, logBuilder, "updateProfileMetadataDefinition");
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "updateProfileMetadataDefinition"));
            }
        } catch (final SRecorderException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "updateProfileMetadataDefinition", e));
            }
            initiateLogBuilder(metadata.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "updateProfileMetadataDefinition");
            throw new SIdentityException("Can't update profile metadata definition " + metadata, e);
        }
    }

    @Override
    public void updateProfileMetadataValue(final SProfileMetadataValue metadataValue, final EntityUpdateDescriptor descriptor) throws SIdentityException {
        logBeforeMethod("updateProfileMetadataValue");
        try {
            final UpdateRecord updateRecord = UpdateRecord.buildSetFields(metadataValue, descriptor);
            SUpdateEvent updateEvent = null;
            if (eventService.hasHandlers(METADATAVALUE, EventActionType.UPDATED)) {
                updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(METADATAVALUE).setObject(metadataValue).done();
            }
            recorder.recordUpdate(updateRecord, updateEvent);
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "updateProfileMetadataValue"));
            }
        } catch (final SRecorderException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "updateProfileMetadataValue", e));
            }
            throw new SIdentityException("Can't update profile metadata definition " + metadataValue, e);
        }
    }

    @Override
    public void updateRole(final SRole role, final EntityUpdateDescriptor descriptor) throws SIdentityException {
        logBeforeMethod("updateRole");
        final SRoleLogBuilder logBuilder = getRoleLog(ActionType.UPDATED, "Updating the role with name " + role.getName());
        try {
            final UpdateRecord updateRecord = UpdateRecord.buildSetFields(role, descriptor);
            SUpdateEvent updateEvent = null;
            if (eventService.hasHandlers(ROLE, EventActionType.UPDATED)) {
                updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(ROLE).setObject(role).done();
            }
            recorder.recordUpdate(updateRecord, updateEvent);
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "updateRole"));
            }
            initiateLogBuilder(role.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "updateRole");
        } catch (final SRecorderException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "updateRole", e));
            }
            initiateLogBuilder(role.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "updateRole");
            throw new SIdentityException("Can't update role " + role, e);
        }
    }

    @Override
    public void updateUser(final SUser user, final EntityUpdateDescriptor descriptor) throws SUserUpdateException {
        updateUser(user, descriptor, false);
    }

    @Deprecated
    @Override
    public void updateUser(final SUser user, final EntityUpdateDescriptor descriptor, final boolean isPasswordEncrypted) throws SUserUpdateException {
        logBeforeMethod("updateUser");
        final StringBuilder sb = new StringBuilder();
        sb.append("Updating user with user name ");
        sb.append(user.getUserName());
        sb.append(", first name ");
        sb.append(user.getFirstName());
        sb.append(", last name ");
        sb.append(user.getLastName());
        if (!isPasswordEncrypted) {
            final String password = (String) descriptor.getFields().get("password");
            if (password != null) {
                final String hash = encrypter.hash(password);
                descriptor.getFields().put("password", hash);
            }
        }
        final SUserLogBuilder logBuilder = getUserLog(ActionType.UPDATED, sb.toString());
        try {
            final UpdateRecord updateRecord = UpdateRecord.buildSetFields(user, descriptor);
            SUpdateEvent updateEvent = null;
            if (eventService.hasHandlers(USER, EventActionType.UPDATED)) {
                updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(USER).setObject(user).done();
                final SUser oldUser = BuilderFactory.get(SUserBuilderFactory.class).createNewInstance(user).done();
                updateEvent.setOldObject(oldUser);
            }
            recorder.recordUpdate(updateRecord, updateEvent);
            initiateLogBuilder(user.getId(), SQueriableLog.STATUS_OK, logBuilder, "updateUser");
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "updateUser"));
            }
        } catch (final SRecorderException re) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "updateUser", re));
            }
            initiateLogBuilder(user.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "updateUser");
            throw new SUserUpdateException(re);
        }
    }

    @Override
    public void updateUserContactInfo(final SContactInfo contactInfo, final EntityUpdateDescriptor descriptor) throws SIdentityException {
        logBeforeMethod("updateUserContactInfo");
        final StringBuilder sb = new StringBuilder();
        sb.append("Updating " + (contactInfo.isPersonal() ? "personal" : "professional") + " user contact Info for user with Id ");
        sb.append(contactInfo.getUserId());
        final SContactInfoLogBuilder logBuilder = getUserContactInfoLog(ActionType.UPDATED, sb.toString(), contactInfo);
        try {
            final UpdateRecord updateRecord = UpdateRecord.buildSetFields(contactInfo, descriptor);
            SUpdateEvent updateEvent = null;
            if (eventService.hasHandlers(USER_CONTACT_INFO, EventActionType.UPDATED)) {
                updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(USER_CONTACT_INFO).setObject(contactInfo).done();
                final SContactInfo oldContactInfo = BuilderFactory.get(SContactInfoBuilderFactory.class).createNewInstance(contactInfo).done();
                updateEvent.setOldObject(oldContactInfo);
            }
            recorder.recordUpdate(updateRecord, updateEvent);
            initiateLogBuilder(contactInfo.getId(), SQueriableLog.STATUS_OK, logBuilder, "updateUserContactInfo");
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "updateUserContactInfo"));
            }
        } catch (final SRecorderException re) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "updateUserContactInfo", re));
            }
            initiateLogBuilder(contactInfo.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "updateUserContactInfo");
            throw new SUserUpdateException(re);
        }
    }

    @Override
    public void updateUserMembership(final SUserMembership userMembership, final EntityUpdateDescriptor descriptor) throws SIdentityException {
        logBeforeMethod("updateUserMembership");
        final StringBuilder sb = new StringBuilder();
        sb.append("Updating user membership for user ");
        sb.append(userMembership.getUsername());
        sb.append(" with role ");
        sb.append(userMembership.getRoleName());
        sb.append(" in group ");
        sb.append(userMembership.getGroupName());
        final SUserMembershipLogBuilder logBuilder = getUserMembershipLog(ActionType.UPDATED, sb.toString(), userMembership);
        try {
            final UpdateRecord updateRecord = UpdateRecord.buildSetFields(userMembership, descriptor);
            SUpdateEvent updateEvent = null;
            if (eventService.hasHandlers(USERMEMBERSHIP, EventActionType.UPDATED)) {
                updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(USERMEMBERSHIP).setObject(userMembership).done();
            }
            recorder.recordUpdate(updateRecord, updateEvent);
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "updateUserMembership"));
            }
            initiateLogBuilder(userMembership.getId(), SQueriableLog.STATUS_OK, logBuilder, "updateUserMembership");
        } catch (final SRecorderException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "updateUserMembership", e));
            }
            initiateLogBuilder(userMembership.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "updateUserMembership");
            throw new SIdentityException("Can't update user membership " + userMembership, e);
        }
    }

    @Override
    public List<SUserMembership> getUserMemberships(final int fromIndex, final int numberOfUserMemberships) throws SIdentityException {
        logBeforeMethod("getUserMemberships");
        final SelectListDescriptor<SUserMembership> descriptor = SelectDescriptorBuilder.getElements(SUserMembership.class, "UserMembership", fromIndex,
                numberOfUserMemberships);
        try {
            final List<SUserMembership> listsUserMemberships = persistenceService.selectList(descriptor);
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getUserMemberships"));
            }
            return listsUserMemberships;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getUserMemberships", e));
            }
            throw new SIdentityException("Can't get the user memberships", e);
        }
    }

    @Override
    public List<SUserMembership> getUserMembershipsOfUser(final long userId, final int fromIndex, final int numberOfUsers) throws SIdentityException {
        logBeforeMethod("getUserMembershipsOfUser");
        try {
            final SelectListDescriptor<SUserMembership> descriptor = SelectDescriptorBuilder.getUserMembershipsOfUser(userId, fromIndex, numberOfUsers);
            final List<SUserMembership> listsSUserMemberships = persistenceService.selectList(descriptor);
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getUserMembershipsOfUser"));
            }
            return listsSUserMemberships;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getUserMembershipsOfUser", e));
            }
            throw new SIdentityException("Can't get the memberships having the user " + userId, e);
        }
    }

    @Override
    public List<SUserMembership> getUserMembershipsOfUser(final long userId, final int fromIndex, final int numberOfMemberships, final String field,
            final OrderByType order) throws SIdentityException {
        logBeforeMethod("getUserMembershipsOfUser");
        try {
            final SelectListDescriptor<SUserMembership> descriptor = SelectDescriptorBuilder.getUserMembershipsOfUser(userId, field, order, fromIndex,
                    numberOfMemberships);
            final List<SUserMembership> listsSUserMemberships = persistenceService.selectList(descriptor);
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getUserMembershipsOfUser"));
            }
            return listsSUserMemberships;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getUserMembershipsOfUser", e));
            }
            throw new SIdentityException("Can't get the memberships having the user" + userId, e);
        }
    }

    @Override
    public List<SUserMembership> getUserMembershipsOfUser(final long userId, final int fromIndex, final int numberPerPage, final OrderByOption orderByOption)
            throws SIdentityException {
        logBeforeMethod("getUserMembershipsOfUser");
        try {
            final SelectListDescriptor<SUserMembership> descriptor = SelectDescriptorBuilder.getUserMembershipsOfUser(userId, new QueryOptions(fromIndex,
                    numberPerPage, Collections.singletonList(orderByOption)));
            final List<SUserMembership> listsUserMemberships = persistenceService.selectList(descriptor);
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getUserMembershipsOfUser"));
            }
            return listsUserMemberships;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getUserMembershipsOfUser", e));
            }
            throw new SIdentityException("Can't get the memberships having the user" + userId, e);
        }
    }

    @Override
    public SUserMembership getUserMembership(final long userId, final long groupId, final long roleId) throws SIdentityException {
        logBeforeMethod("getUserMembership");
        final SelectOneDescriptor<SUserMembership> descriptor = SelectDescriptorBuilder.getUserMembership(userId, groupId, roleId);
        try {
            final SUserMembership sUserMembership = getUserMembership(userId, groupId, roleId, descriptor);
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getUserMembership"));
            }
            return sUserMembership;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getUserMembership", e));
            }
            throw new SIdentityException("Can't get the userMembership with userId = " + userId + ", groupId = " + groupId + ", roleId = " + roleId, e);
        }
    }

    @Override
    public SUserMembership getLightUserMembership(final long userId, final long groupId, final long roleId) throws SIdentityException {
        logBeforeMethod("getLightUserMembership");
        final SelectOneDescriptor<SUserMembership> descriptor = SelectDescriptorBuilder.getLightUserMembership(userId, groupId, roleId);
        try {
            final SUserMembership sUserMembership = getUserMembership(userId, groupId, roleId, descriptor);
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getLightUserMembership"));
            }
            return sUserMembership;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getLightUserMembership", e));
            }
            throw new SIdentityException("Can't get the userMembership with userId = " + userId + ", groupId = " + groupId + ", roleId = " + roleId, e);
        }
    }

    private SUserMembership getUserMembership(final long userId, final long groupId, final long roleId, final SelectOneDescriptor<SUserMembership> descriptor)
            throws SBonitaReadException, SIdentityException {
        final SUserMembership sUserMembership = persistenceService.selectOne(descriptor);
        if (sUserMembership == null) {
            throw new SIdentityException("Can't get the userMembership with userId = " + userId + ", groupId = " + groupId + ", roleId = " + roleId);
        }
        return sUserMembership;
    }

    @Override
    public long getNumberOfUserMemberships() throws SIdentityException {
        logBeforeMethod("getNumberOfUserMemberships");
        try {
            final long number = persistenceService.selectOne(SelectDescriptorBuilder.getNumberOfElement("UserMembership", SUserMembership.class));
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getNumberOfUserMemberships"));
            }
            return number;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getNumberOfUserMemberships", e));
            }
            throw new SIdentityException("Can't get the number of user membership", e);
        }
    }

    @Override
    public List<SUserMembership> getLightUserMemberships(final int startIndex, final int numberOfElements) throws SIdentityException {
        logBeforeMethod("getLightUserMemberships");
        final SelectListDescriptor<SUserMembership> descriptor = SelectDescriptorBuilder.getElements(SUserMembership.class, "LightUserMembership", startIndex,
                numberOfElements);
        try {
            final List<SUserMembership> listsUserMemberships = persistenceService.selectList(descriptor);
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getLightUserMemberships"));
            }
            return listsUserMemberships;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getLightUserMemberships", e));
            }
            throw new SIdentityException("Can't get the user memberships", e);
        }
    }

    @Override
    public long getNumberOfUsers(final QueryOptions options) throws SBonitaSearchException {
        logBeforeMethod("getNumberOfUsers");
        try {
            final long number = persistenceService.getNumberOfEntities(SUser.class, options, null);
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getNumberOfUsers"));
            }
            return number;
        } catch (final SBonitaReadException bre) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getNumberOfUsers", bre));
            }
            throw new SBonitaSearchException(bre);
        }
    }

    @Override
    public List<SUser> searchUsers(final QueryOptions options) throws SBonitaSearchException {
        logBeforeMethod("searchUsers");
        try {
            final List<SUser> listsSUsers = persistenceService.searchEntity(SUser.class, options, null);
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "searchUsers"));
            }
            return listsSUsers;
        } catch (final SBonitaReadException bre) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "searchUsers", bre));
            }
            throw new SBonitaSearchException(bre);
        }
    }

    @Override
    public long getNumberOfRoles(final QueryOptions options) throws SBonitaSearchException {
        logBeforeMethod("getNumberOfRoles");
        try {
            final long number = persistenceService.getNumberOfEntities(SRole.class, options, null);
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getNumberOfRoles"));
            }
            return number;
        } catch (final SBonitaReadException bre) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getNumberOfRoles", bre));
            }
            throw new SBonitaSearchException(bre);
        }
    }

    @Override
    public List<SRole> searchRoles(final QueryOptions options) throws SBonitaSearchException {
        logBeforeMethod("searchRoles");
        try {
            final List<SRole> listsRoles = persistenceService.searchEntity(SRole.class, options, null);
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "searchRoles"));
            }
            return listsRoles;
        } catch (final SBonitaReadException bre) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "searchRoles", bre));
            }
            throw new SBonitaSearchException(bre);
        }
    }

    @Override
    public long getNumberOfGroups(final QueryOptions options) throws SBonitaSearchException {
        logBeforeMethod("getNumberOfGroups");
        try {
            final long number = persistenceService.getNumberOfEntities(SGroup.class, options, null);
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getNumberOfGroups"));
            }
            return number;
        } catch (final SBonitaReadException bre) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getNumberOfGroups", bre));
            }
            throw new SBonitaSearchException(bre);
        }
    }

    @Override
    public List<SGroup> searchGroups(final QueryOptions options) throws SBonitaSearchException {
        logBeforeMethod("searchGroups");
        try {
            final List<SGroup> listsGroups = persistenceService.searchEntity(SGroup.class, options, null);
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "searchGroups"));
            }
            return listsGroups;
        } catch (final SBonitaReadException bre) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "searchGroups", bre));
            }
            throw new SBonitaSearchException(bre);
        }
    }

    private void initiateLogBuilder(final long objectId, final int sQueriableLogStatus, final SPersistenceLogBuilder logBuilder, final String methodName) {
        logBuilder.actionScope(String.valueOf(objectId));
        logBuilder.actionStatus(sQueriableLogStatus);
        logBuilder.objectId(objectId);
        final SQueriableLog log = logBuilder.done();
        if (queriableLoggerService.isLoggable(log.getActionType(), log.getSeverity())) {
            queriableLoggerService.log(this.getClass().getName(), methodName, log);
        }
    }

    @Override
    public boolean chechCredentials(final SUser user, final String password) {
        final String hashPassword = user.getPassword();
        return encrypter.check(password, hashPassword);
    }

    @Override
    @Deprecated
    public SUser createUserWithoutEncryptingPassword(final SUser user) throws SUserCreationException {
        logBeforeMethod("createUserWithoutEncryptingPassword");
        final String message = "Adding a new user with user name " + user.getUserName() + ", first name " + user.getFirstName() + ", last name "
                + user.getLastName();
        final SUserLogBuilder logBuilder = getUserLog(ActionType.CREATED, message);

        final SUser hashedUser = BuilderFactory.get(SUserBuilderFactory.class).createNewInstance(user).done();
        try {
            final InsertRecord insertRecord = new InsertRecord(hashedUser);
            final SInsertEvent insertEvent = getInsertEvent(hashedUser, USER);
            recorder.recordInsert(insertRecord, insertEvent);
            initiateLogBuilder(hashedUser.getId(), SQueriableLog.STATUS_OK, logBuilder, "createUserWithoutEncryptingPassword");
            logAfterMethod("createUserWithoutEncryptingPassword");
            return hashedUser;
        } catch (final SRecorderException re) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE,
                        LogUtil.getLogOnExceptionMethod(this.getClass(), "createUserWithoutEncryptingPassword", re));
            }
            initiateLogBuilder(hashedUser.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "createUserWithoutEncryptingPassword");
            throw new SUserCreationException(re);
        }
    }

    private void logBeforeMethod(final String methodName) {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), methodName));
        }
    }

    private void logAfterMethod(String methodName) {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), methodName));
        }
    }
}
