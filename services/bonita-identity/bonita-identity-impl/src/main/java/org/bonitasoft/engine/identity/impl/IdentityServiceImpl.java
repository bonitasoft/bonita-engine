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
package org.bonitasoft.engine.identity.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.events.EventActionType;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilderFactory;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.SCustomUserInfoDefinitionAlreadyExistsException;
import org.bonitasoft.engine.identity.SCustomUserInfoDefinitionCreationException;
import org.bonitasoft.engine.identity.SCustomUserInfoDefinitionNotFoundException;
import org.bonitasoft.engine.identity.SCustomUserInfoDefinitionReadException;
import org.bonitasoft.engine.identity.SCustomUserInfoValueNotFoundException;
import org.bonitasoft.engine.identity.SCustomUserInfoValueReadException;
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
import org.bonitasoft.engine.identity.model.SCustomUserInfoDefinition;
import org.bonitasoft.engine.identity.model.SCustomUserInfoValue;
import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.model.SRole;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.SUserLogin;
import org.bonitasoft.engine.identity.model.SUserMembership;
import org.bonitasoft.engine.identity.model.builder.SContactInfoBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.SContactInfoLogBuilder;
import org.bonitasoft.engine.identity.model.builder.SContactInfoLogBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.SCustomUserInfoDefinitionLogBuilder;
import org.bonitasoft.engine.identity.model.builder.SCustomUserInfoDefinitionLogBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.SGroupLogBuilder;
import org.bonitasoft.engine.identity.model.builder.SGroupLogBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.SRoleLogBuilder;
import org.bonitasoft.engine.identity.model.builder.SRoleLogBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.SUserBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.SUserLogBuilder;
import org.bonitasoft.engine.identity.model.builder.SUserLogBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.SUserMembershipLogBuilder;
import org.bonitasoft.engine.identity.model.builder.SUserMembershipLogBuilderFactory;
import org.bonitasoft.engine.identity.model.impl.SUserImpl;
import org.bonitasoft.engine.identity.model.impl.SUserLoginImpl;
import org.bonitasoft.engine.identity.recorder.SelectDescriptorBuilder;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
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
            final TechnicalLoggerService logger, final QueriableLoggerService queriableLoggerService, final CredentialsEncrypter encrypter) {
        this.persistenceService = persistenceService;
        this.recorder = recorder;
        this.eventService = eventService;
        this.logger = logger;
        this.queriableLoggerService = queriableLoggerService;
        this.encrypter = encrypter;
    }

    @Override
    public void createGroup(final SGroup group) throws SGroupCreationException {
        final String methodName = "createGroup";
        final long objectId = group.getId();
        final SGroupLogBuilder logBuilder = getGroupLog(ActionType.CREATED, "Adding a new group with name " + group.getName());
        try {

            final InsertRecord insertRecord = new InsertRecord(group);
            final SInsertEvent insertEvent = getInsertEvent(group, GROUP);
            recorder.recordInsert(insertRecord, insertEvent);
            final int status = SQueriableLog.STATUS_OK;
            log(insertRecord.getEntity().getId(), status, logBuilder, methodName);
        } catch (final SRecorderException re) {
            final int status = SQueriableLog.STATUS_FAIL;
            log(objectId, status, logBuilder, methodName);
            throw new SGroupCreationException(re);
        }
    }

    private SInsertEvent getInsertEvent(final Object object, final String type) {
        if (eventService.hasHandlers(type, EventActionType.CREATED)) {
            return (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent(type).setObject(object).done();
        }
        return null;
    }

    private SDeleteEvent getDeleteEvent(final Object object, final String type) {
        if (eventService.hasHandlers(type, EventActionType.DELETED)) {
            return (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(type).setObject(object).done();
        }
        return null;
    }

    @Override
    public SCustomUserInfoDefinition createCustomUserInfoDefinition(final SCustomUserInfoDefinition customUserInfo)
            throws SCustomUserInfoDefinitionAlreadyExistsException, SCustomUserInfoDefinitionCreationException {
        final String methodName = "createCustomUserInfoDefinition";
        final SCustomUserInfoDefinitionLogBuilder logBuilder = getSCustomUserInfoDefinitionLog(ActionType.CREATED, "Adding a custom user info with name "
                + customUserInfo.getName());
        try {
            throwExceptionIfAlreadyExists(customUserInfo);
            final InsertRecord insertRecord = new InsertRecord(customUserInfo);
            final SInsertEvent insertEvent = getInsertEvent(customUserInfo, CUSTOM_USER_INFO_DEFINITION);
            recorder.recordInsert(insertRecord, insertEvent);
            log(customUserInfo.getId(), SQueriableLog.STATUS_OK, logBuilder, methodName);
            return customUserInfo;
        } catch (final SRecorderException | SBonitaReadException e) {
            throw handleCustomUserInfoDefinitionCreationFailure(customUserInfo, methodName, logBuilder, e);
        }
    }

    private void throwExceptionIfAlreadyExists(final SCustomUserInfoDefinition customUserInfo) throws SBonitaReadException,
            SCustomUserInfoDefinitionAlreadyExistsException {
        final SCustomUserInfoDefinition storedDef = getCustomUserInfoDefinitionWithoutCheck(customUserInfo.getName());
        if (storedDef != null) {
            throw new SCustomUserInfoDefinitionAlreadyExistsException(customUserInfo.getName());
        }
    }

    private SCustomUserInfoDefinitionCreationException handleCustomUserInfoDefinitionCreationFailure(final SCustomUserInfoDefinition customUserInfo,
            final String methodName, final SCustomUserInfoDefinitionLogBuilder logBuilder, final SBonitaException exception) {
        log(customUserInfo.getId(), SQueriableLog.STATUS_FAIL, logBuilder, methodName);
        return new SCustomUserInfoDefinitionCreationException(customUserInfo.getName(), exception);
    }

    @Override
    public SCustomUserInfoValue createCustomUserInfoValue(final SCustomUserInfoValue customUserInfo) throws SIdentityException {
        try {
            final InsertRecord insertRecord = new InsertRecord(customUserInfo);
            final SInsertEvent insertEvent = getInsertEvent(customUserInfo, CUSTOM_USER_INFO_VALUE);
            recorder.recordInsert(insertRecord, insertEvent);
            return customUserInfo;
        } catch (final SRecorderException e) {
            throw new SIdentityException("Can't add custom user info value " + customUserInfo, e);
        }
    }

    @Override
    public void createRole(final SRole role) throws SIdentityException {
        final String methodName = "createRole";
        final SRoleLogBuilder logBuilder = getRoleLog(ActionType.CREATED, "Adding a new role with name " + role.getName());
        try {
            final InsertRecord insertRecord = new InsertRecord(role);
            final SInsertEvent insertEvent = getInsertEvent(role, ROLE);
            recorder.recordInsert(insertRecord, insertEvent);
            log(role.getId(), SQueriableLog.STATUS_OK, logBuilder, methodName);
        } catch (final SRecorderException e) {
            log(role.getId(), SQueriableLog.STATUS_FAIL, logBuilder, methodName);
            throw new SIdentityException("Can't add role " + role, e);
        }
    }

    @Override
    public SUser createUser(final SUser user) throws SUserCreationException {
        final String methodName = "createUser";
        final String hash = encrypter.hash(user.getPassword());
        final SUser hashedUser = BuilderFactory.get(SUserBuilderFactory.class).createNewInstance(user).setPassword(hash).done();
        return createUser(user, methodName, hashedUser);
    }

    @Override
    @Deprecated
    public SUser createUserWithoutEncryptingPassword(final SUser user) throws SUserCreationException {
        final String methodName = "createUserWithoutEncryptingPassword";
        final SUser hashedUser = BuilderFactory.get(SUserBuilderFactory.class).createNewInstance(user).done();
        return createUser(user, methodName, hashedUser);
    }

    private SUser createUser(final SUser user, final String methodName, final SUser hashedUser) throws SUserCreationException {
        final String message = "Adding a new user with user name " + user.getUserName() + ", first name " + user.getFirstName() + ", last name "
                + user.getLastName();
        final SUserLogBuilder logBuilder = getUserLog(ActionType.CREATED, message);
        try {
            //no insert event for user login objects
            SUserLoginImpl sUserLogin = insertUserLogin(hashedUser);
            insertUser(methodName, hashedUser, logBuilder, sUserLogin);
            return hashedUser;
        } catch (final SRecorderException re) {
            log(hashedUser.getId(), SQueriableLog.STATUS_FAIL, logBuilder, methodName);
            throw new SUserCreationException(re);
        }
    }

    private void insertUser(String methodName, SUser hashedUser, SUserLogBuilder logBuilder, SUserLoginImpl sUserLogin) throws SRecorderException {
        ((SUserImpl) hashedUser).setsUserLogin(sUserLogin);
        sUserLogin.setsUser(hashedUser);
        sUserLogin.setId(hashedUser.getId());
        sUserLogin.setTenantId(((SUserImpl) hashedUser).getTenantId());
        recorder.recordInsert(new InsertRecord(sUserLogin), null);
        log(hashedUser.getId(), SQueriableLog.STATUS_OK, logBuilder, methodName);
    }

    private SUserLoginImpl insertUserLogin(SUser hashedUser) throws SRecorderException {
        final InsertRecord insertRecord = new InsertRecord(hashedUser);
        final SInsertEvent insertEvent = getInsertEvent(hashedUser, USER);
        SUserLoginImpl sUserLogin = new SUserLoginImpl();
        recorder.recordInsert(insertRecord, insertEvent);
        return sUserLogin;
    }

    @Override
    public SContactInfo createUserContactInfo(final SContactInfo contactInfo) throws SUserCreationException {
        final String methodName = "createUserContactInfo";
        final String message = "Adding a new user contact information for user with id " + contactInfo.getUserId();
        final SContactInfoLogBuilder logBuilder = getUserContactInfoLog(ActionType.CREATED, message, contactInfo);
        try {
            final InsertRecord insertRecord = new InsertRecord(contactInfo);
            final SInsertEvent insertEvent = getInsertEvent(contactInfo, USER_CONTACT_INFO);
            recorder.recordInsert(insertRecord, insertEvent);
            log(contactInfo.getId(), SQueriableLog.STATUS_OK, logBuilder, methodName);
            return contactInfo;
        } catch (final SRecorderException re) {
            log(contactInfo.getId(), SQueriableLog.STATUS_FAIL, logBuilder, methodName);
            throw new SUserCreationException(re);
        }
    }

    @Override
    public void createUserMembership(final SUserMembership userMembership) throws SUserMembershipCreationException {
        final String methodName = "createUserMembership";
        final String message = "Adding a new user membership for user " + userMembership.getUsername() + " with role " + userMembership.getRoleName()
                + " in group " + userMembership.getGroupName();

        final SUserMembershipLogBuilder logBuilder = getUserMembershipLog(ActionType.CREATED, message, userMembership);
        try {
            final InsertRecord insertRecord = new InsertRecord(userMembership);
            final SInsertEvent insertEvent = getInsertEvent(userMembership, USERMEMBERSHIP);
            recorder.recordInsert(insertRecord, insertEvent);
            log(userMembership.getId(), SQueriableLog.STATUS_OK, logBuilder, methodName);
        } catch (final SRecorderException re) {
            log(userMembership.getId(), SQueriableLog.STATUS_FAIL, logBuilder, methodName);
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
        final String methodName = "deleteGroup";
        final SGroupLogBuilder logBuilder = getGroupLog(ActionType.DELETED, "Deleting group " + group.getName());
        try {
            final DeleteRecord deleteRecord = new DeleteRecord(group);
            final SDeleteEvent deleteEvent = getDeleteEvent(group, GROUP);
            recorder.recordDelete(deleteRecord, deleteEvent);
            log(group.getId(), SQueriableLog.STATUS_OK, logBuilder, methodName);
        } catch (final SRecorderException re) {
            log(group.getId(), SQueriableLog.STATUS_FAIL, logBuilder, methodName);
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
        final ArrayList<Long> deletedGroups = new ArrayList<>();
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
        } catch (final SGroupNotFoundException e) {
            throw e;
        } catch (final SIdentityException e) {
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

    private SCustomUserInfoDefinitionLogBuilder getSCustomUserInfoDefinitionLog(final ActionType actionType, final String message) {
        final SCustomUserInfoDefinitionLogBuilder logBuilder = BuilderFactory.get(SCustomUserInfoDefinitionLogBuilderFactory.class).createNewInstance();
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
    public void deleteCustomUserInfoDefinition(final long customUserInfoDefinitionId) throws SIdentityException {
        this.deleteCustomUserInfoDefinition(getCustomUserInfoDefinition(customUserInfoDefinitionId));
    }

    @Override
    public void deleteCustomUserInfoDefinition(final SCustomUserInfoDefinition info) throws SIdentityException {
        final String methodName = "deleteCustomUserInfoDefinition";
        final SCustomUserInfoDefinitionLogBuilder logBuilder = getSCustomUserInfoDefinitionLog(ActionType.DELETED,
                "Deleting profile custom user info definition with name " + info.getName());
        try {
            final DeleteRecord deleteRecord = new DeleteRecord(info);
            final SDeleteEvent deleteEvent = getDeleteEvent(info, CUSTOM_USER_INFO_DEFINITION);
            recorder.recordDelete(deleteRecord, deleteEvent);
            log(info.getId(), SQueriableLog.STATUS_OK, logBuilder, methodName);
        } catch (final SRecorderException e) {
            log(info.getId(), SQueriableLog.STATUS_FAIL, logBuilder, methodName);
            throw new SIdentityException("Can't delete profile custom user info definition " + info, e);
        }
    }

    @Override
    public void deleteCustomUserInfoValue(final long customUserInfoValueId) throws SIdentityException {
        this.deleteCustomUserInfoValue(getCustomUserInfoValue(customUserInfoValueId));
    }

    @Override
    public void deleteCustomUserInfoValue(final SCustomUserInfoValue customUserInfo) throws SIdentityException {
        try {
            final DeleteRecord deleteRecord = new DeleteRecord(customUserInfo);
            final SDeleteEvent deleteEvent = getDeleteEvent(customUserInfo, CUSTOM_USER_INFO_VALUE);
            recorder.recordDelete(deleteRecord, deleteEvent);
        } catch (final SRecorderException e) {
            throw new SIdentityException("Can't delete custom user info value" + customUserInfo, e);
        }
    }

    @Override
    public void deleteRole(final long roleId) throws SRoleNotFoundException, SRoleDeletionException {
        final SRole role = getRole(roleId);
        this.deleteRole(role);
    }

    @Override
    public void deleteRole(final SRole role) throws SRoleDeletionException {
        final String methodName = "deleteRole";
        final SRoleLogBuilder logBuilder = getRoleLog(ActionType.DELETED, "Deleting role with name " + role.getName());
        try {
            final DeleteRecord deleteRecord = new DeleteRecord(role);
            final SDeleteEvent deleteEvent = getDeleteEvent(role, ROLE);
            recorder.recordDelete(deleteRecord, deleteEvent);
            log(role.getId(), SQueriableLog.STATUS_OK, logBuilder, methodName);
        } catch (final SRecorderException re) {
            log(role.getId(), SQueriableLog.STATUS_FAIL, logBuilder, methodName);
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
        final String methodName = "deleteUser";
        final SUserLogBuilder logBuilder = getUserLog(ActionType.DELETED, "Deleting user with username " + user.getUserName());
        try {
            final DeleteRecord deleteRecord = new DeleteRecord(user);
            final SDeleteEvent deleteEvent = getDeleteEvent(user, USER);
            recorder.recordDelete(deleteRecord, deleteEvent);
            log(user.getId(), SQueriableLog.STATUS_OK, logBuilder, methodName);
        } catch (final SRecorderException re) {
            log(user.getId(), SQueriableLog.STATUS_FAIL, logBuilder, methodName);
            throw new SUserDeletionException(re);
        }
    }

    @Override
    public void deleteAllUsers() throws SUserDeletionException {
        try {
            final DeleteAllRecord record = new DeleteAllRecord(SUser.class, null);
            recorder.recordDeleteAll(record);
            final DeleteAllRecord record2 = new DeleteAllRecord(SUserLogin.class, null);
            recorder.recordDeleteAll(record2);
        } catch (final SRecorderException e) {
            throw new SUserDeletionException("Can't delete all users.", e);
        }
    }

    @Override
    public SUserMembership getLightUserMembership(final long userMembershipId) throws SIdentityException {
        try {
            final SUserMembership selectOne = persistenceService.selectById(SelectDescriptorBuilder.getLightElementById(SUserMembership.class,
                    "SUserMembership", userMembershipId));
            if (selectOne == null) {
                throw new SIdentityException("Can't get the userMembership with id " + userMembershipId, null);
            }
            return selectOne;
        } catch (final SBonitaReadException e) {
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
        final String methodName = "deleteLightUserMembership";
        final String message = "Deleting user membership for user " + userMembership.getUsername() + " with role " + userMembership.getRoleName()
                + " in group " + userMembership.getGroupName();
        final SUserMembershipLogBuilder logBuilder = getUserMembershipLog(ActionType.DELETED, message, userMembership);
        try {
            final DeleteRecord deleteRecord = new DeleteRecord(userMembership);
            final SDeleteEvent deleteEvent = getDeleteEvent(userMembership, USERMEMBERSHIP);
            recorder.recordDelete(deleteRecord, deleteEvent);
            log(userMembership.getId(), SQueriableLog.STATUS_OK, logBuilder, methodName);
        } catch (final SRecorderException e) {
            log(userMembership.getId(), SQueriableLog.STATUS_FAIL, logBuilder, methodName);
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
        try {
            final SGroup group = persistenceService.selectById(SelectDescriptorBuilder.getElementById(SGroup.class, "Group", groupId));
            if (group == null) {
                throw new SGroupNotFoundException("No group exists with id: " + groupId);
            }
            return group;
        } catch (final SBonitaReadException bre) {
            throw new SGroupNotFoundException(bre);
        }
    }

    @Override
    public List<SGroup> getGroupChildren(final long groupId, final int fromIndex, final int numberOfGroups) throws SIdentityException {
        try {
            final SGroup group = getGroup(groupId);
            return persistenceService.selectList(SelectDescriptorBuilder.getChildrenOfGroup(group, fromIndex, numberOfGroups));
        } catch (final SBonitaReadException e) {
            throw new SIdentityException("Can't get the children of the group", e);
        }
    }

    @Override
    public List<SGroup> getGroupChildren(final long groupId, final int fromIndex, final int numberOfGroups, final String field, final OrderByType order)
            throws SIdentityException {
        try {
            final SGroup group = getGroup(groupId);
            return persistenceService.selectList(SelectDescriptorBuilder.getChildrenOfGroup(group, field, order, fromIndex,
                    numberOfGroups));
        } catch (final SBonitaReadException e) {
            throw new SIdentityException("Can't get the children of the group", e);
        }
    }

    @Override
    public List<SGroup> getGroups(final int fromIndex, final int numberOfGroups) throws SIdentityException {
        try {
            return persistenceService
                    .selectList(SelectDescriptorBuilder.getElements(SGroup.class, "Group", fromIndex, numberOfGroups));
        } catch (final SBonitaReadException e) {
            throw new SIdentityException("Can't get the groups", e);
        }
    }

    @Override
    public List<SGroup> getGroups(final int fromIndex, final int numberOfGroups, final String field, final OrderByType order) throws SIdentityException {
        try {
            return persistenceService.selectList(SelectDescriptorBuilder.getElements(SGroup.class, "Group", field, order, fromIndex,
                    numberOfGroups));
        } catch (final SBonitaReadException e) {
            throw new SIdentityException("Can't get the groups", e);
        }
    }

    @Override
    public List<SGroup> getGroups(final List<Long> groupIds) throws SGroupNotFoundException {
        if (groupIds == null || groupIds.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return persistenceService.selectList(SelectDescriptorBuilder.getElementsByIds(SGroup.class, "Group", groupIds));
        } catch (final SBonitaReadException e) {
            throw new SGroupNotFoundException(e);
        }
    }

    @Override
    public List<SUserMembership> getUserMemberships(final int fromIndex, final int numberOfResult, final OrderByOption orderByOption)
            throws SIdentityException {
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
            return listSUserMembership;
        } catch (final SBonitaReadException e) {
            throw new SIdentityException("Can't get the memberships", e);
        }
    }

    @Override
    public List<SUserMembership> getUserMembershipsOfGroup(final long groupId, final int startIndex, final int maxResults) throws SIdentityException {
        try {
            return persistenceService.selectList(SelectDescriptorBuilder.getUserMembershipsByGroup(groupId, startIndex,
                    maxResults));
        } catch (final SBonitaReadException e) {
            throw new SIdentityException("Can't get the users memberships the group " + groupId, e);
        }
    }

    @Override
    public List<SUserMembership> getUserMembershipsOfRole(final long roleId, final int startIndex, final int maxResults) throws SIdentityException {
        try {
            return persistenceService.selectList(SelectDescriptorBuilder.getUserMembershipsByRole(roleId, startIndex,
                    maxResults));
        } catch (final SBonitaReadException e) {
            throw new SIdentityException("Can't get the memberships having the role " + roleId, e);
        }
    }

    @Override
    public long getNumberOfGroupChildren(final long parentGroupId) throws SIdentityException {
        try {
            final SGroup parentGroup = getGroup(parentGroupId);
            return persistenceService.selectOne(SelectDescriptorBuilder.getNumberOfGroupChildren(parentGroup.getPath()));
        } catch (final SBonitaReadException e) {
            throw new SIdentityException("Can't get the number children of group", e);
        }
    }

    @Override
    public long getNumberOfGroups() throws SIdentityException {
        try {
            return persistenceService.selectOne(SelectDescriptorBuilder.getNumberOfElement("SGroup", SGroup.class));
        } catch (final SBonitaReadException e) {
            throw new SIdentityException("Can't get the number of group", e);
        }
    }

    @Override
    public long getNumberOfCustomUserInfoDefinition() throws SIdentityException {
        try {
            return persistenceService.selectOne(SelectDescriptorBuilder.getNumberOfElement("CustomUserInfoDefinition",
                    SCustomUserInfoDefinition.class));
        } catch (final SBonitaReadException e) {
            throw new SIdentityException("Can't get the number of custom user info", e);
        }
    }

    @Override
    public long getNumberOfCustomUserInfoValue(final QueryOptions options) throws SBonitaReadException {
        try {
            return persistenceService.getNumberOfEntities(SCustomUserInfoValue.class, options, null);
        } catch (final SBonitaReadException e) {
            throw new SBonitaReadException(e);
        }
    }

    @Override
    public long getNumberOfRoles() throws SIdentityException {
        try {
            return persistenceService.selectOne(SelectDescriptorBuilder.getNumberOfElement("SRole", SRole.class));
        } catch (final SBonitaReadException e) {
            throw new SIdentityException("Can't get the number of role", e);
        }
    }

    @Override
    public long getNumberOfUsers() throws SIdentityException {
        try {
            return persistenceService.selectOne(SelectDescriptorBuilder.getNumberOfElement("SUser", SUser.class));
        } catch (final SBonitaReadException e) {
            throw new SIdentityException("Can't get the number of user", e);
        }
    }

    @Override
    public long getNumberOfUserMembershipsOfUser(final long userId) throws SIdentityException {
        try {
            return persistenceService.selectOne(SelectDescriptorBuilder.getNumberOfUserMembershipsOfUser(userId));
        } catch (final SBonitaReadException e) {
            throw new SIdentityException("Can't get the number of usermemberships having the user " + userId, e);
        }
    }

    @Override
    public long getNumberOfUsersByGroup(final long groupId) throws SIdentityException {
        try {
            return persistenceService.selectOne(SelectDescriptorBuilder.getNumberOfUsersByGroup(groupId));
        } catch (final SBonitaReadException e) {
            throw new SIdentityException("Can't get the number of users having the group " + groupId, e);
        }
    }

    @Override
    public long getNumberOfUsersByRole(final long roleId) throws SIdentityException {
        try {
            return persistenceService.selectOne(SelectDescriptorBuilder.getNumberOfUsersByRole(roleId));
        } catch (final SBonitaReadException e) {
            throw new SIdentityException("Can't get the number of users having the role " + roleId, e);
        }
    }

    @Override
    public long getNumberOfUsersByMembership(final long groupId, final long roleId) throws SIdentityException {
        try {
            return persistenceService.selectOne(SelectDescriptorBuilder.getNumberOfUsersByMembership(groupId, roleId));
        } catch (final SBonitaReadException e) {
            throw new SIdentityException("Can't get the number of users having the membership with group:" + groupId + " and role:" + roleId, e);
        }
    }

    @Override
    public SCustomUserInfoDefinition getCustomUserInfoDefinitionByName(final String name) throws SCustomUserInfoDefinitionNotFoundException,
            SCustomUserInfoDefinitionReadException {
        SCustomUserInfoDefinition definition;
        try {
            definition = getCustomUserInfoDefinitionWithoutCheck(name);
        } catch (final SBonitaReadException e) {
            throw new SCustomUserInfoDefinitionReadException(name, e);
        }
        if (definition == null) {
            throw new SCustomUserInfoDefinitionNotFoundException(name);
        }
        return definition;
    }

    @Override
    public boolean hasCustomUserInfoDefinition(final String name) throws SCustomUserInfoDefinitionReadException {
        SCustomUserInfoDefinition definition;
        try {
            definition = getCustomUserInfoDefinitionWithoutCheck(name);
        } catch (final SBonitaReadException e) {
            throw new SCustomUserInfoDefinitionReadException(name, e);
        }
        return definition != null;
    }

    private SCustomUserInfoDefinition getCustomUserInfoDefinitionWithoutCheck(final String name) throws SBonitaReadException {
        return persistenceService.selectOne(SelectDescriptorBuilder.getCustomUserInfoDefinitionByName(name));
    }

    @Override
    public List<SCustomUserInfoDefinition> getCustomUserInfoDefinitions(final int fromIndex, final int maxResults) throws SIdentityException {
        try {
            return persistenceService.selectList(SelectDescriptorBuilder.getElements(
                    SCustomUserInfoDefinition.class, "CustomUserInfoDefinition", fromIndex, maxResults));
        } catch (final SBonitaReadException e) {
            throw new SIdentityException("Can't get the custom user info definitions", e);
        }
    }

    @Override
    public List<Long> getUserIdsWithCustomUserInfo(final String userInfoName, String userInfoValue, final boolean usePartialMatch, final int fromIndex,
            final int maxResults) throws SIdentityException {
        try {
            String queryName;
            if (usePartialMatch) {
                queryName = "getUserIdsWithCustomUserInfoContains";
                userInfoValue = "%" + userInfoValue + "%";
            } else {
                queryName = "getUserIdsWithCustomUserInfo";
            }
            final Map<String, Object> parameters = new HashMap<>(2);
            parameters.put("userInfoName", userInfoName);
            parameters.put("userInfoValue", userInfoValue);
            final SelectListDescriptor<Long> descriptor = new SelectListDescriptor<>(queryName, parameters, SUser.class, Long.class,
                    new QueryOptions(fromIndex, maxResults));
            return persistenceService.selectList(descriptor);
        } catch (final SBonitaReadException e) {
            throw new SIdentityException("Can't get the custom user info definitions", e);
        }
    }

    @Override
    public SCustomUserInfoDefinition getCustomUserInfoDefinition(final long customUserInfoDefinitionId) throws SIdentityException {
        try {
            final SCustomUserInfoDefinition selectOne = persistenceService.selectById(SelectDescriptorBuilder.getElementById(SCustomUserInfoDefinition.class,
                    "CustomUserInfoDefinition", customUserInfoDefinitionId));
            if (selectOne == null) {
                throw new SIdentityException("Can't get the custom user info definition with id " + customUserInfoDefinitionId, null);
            }
            return selectOne;
        } catch (final SBonitaReadException e) {
            throw new SIdentityException("Can't get the custom user info definition with id " + customUserInfoDefinitionId, e);
        }
    }

    @Override
    public List<SCustomUserInfoDefinition> getCustomUserInfoDefinitions(final List<Long> customUserInfoDefinitionIds) throws SIdentityException {
        if (customUserInfoDefinitionIds == null || customUserInfoDefinitionIds.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return persistenceService.selectList(SelectDescriptorBuilder.getElementsByIds(
                    SCustomUserInfoDefinition.class, "SCustomUserInfoDefinition", customUserInfoDefinitionIds));
        } catch (final SBonitaReadException e) {
            throw new SIdentityException("Can't get custom user info definitions with ids " + Arrays.toString(customUserInfoDefinitionIds.toArray()), e);
        }
    }

    @Override
    public SCustomUserInfoValue getCustomUserInfoValue(final long customUserInfoValueId) throws SCustomUserInfoValueNotFoundException,
            SCustomUserInfoValueReadException {
        try {
            final SCustomUserInfoValue selectOne = persistenceService.selectById(SelectDescriptorBuilder.getElementById(SCustomUserInfoValue.class,
                    "SCustomUserInfoValue", customUserInfoValueId));
            if (selectOne == null) {
                throw new SCustomUserInfoValueNotFoundException(customUserInfoValueId);
            }
            return selectOne;
        } catch (final SBonitaReadException e) {
            throw new SCustomUserInfoValueReadException(e);
        }
    }

    @Override
    public List<SCustomUserInfoValue> getCustomUserInfoValues(final List<Long> customUserInfoValueIds) throws SIdentityException {
        if (customUserInfoValueIds == null || customUserInfoValueIds.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return persistenceService.selectList(SelectDescriptorBuilder.getElementsByIds(SCustomUserInfoValue.class, "SCustomUserInfoValue",
                    customUserInfoValueIds));
        } catch (final SBonitaReadException e) {
            throw new SIdentityException("Can't get custom user info values with ids " + Arrays.toString(customUserInfoValueIds.toArray()), e);
        }
    }

    @Override
    public SRole getRole(final long roleId) throws SRoleNotFoundException {
        try {
            final SRole selectOne = persistenceService.selectById(SelectDescriptorBuilder.getElementById(SRole.class, "Role", roleId));
            if (selectOne == null) {
                throw new SRoleNotFoundException("The role with id= " + roleId + " does not exist");
            }
            return selectOne;
        } catch (final SBonitaReadException e) {
            throw new SRoleNotFoundException(e);
        }
    }

    @Override
    public SRole getRoleByName(final String roleName) throws SRoleNotFoundException {
        try {
            final SRole role = persistenceService.selectOne(SelectDescriptorBuilder.getRoleByName(roleName));
            if (role == null) {
                throw new SRoleNotFoundException("The role named " + roleName + " does not exist");
            }
            return role;
        } catch (final SBonitaReadException e) {
            throw new SRoleNotFoundException(e);
        }
    }

    @Override
    public SGroup getGroupByPath(final String groupPath) throws SGroupNotFoundException {
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
            return group;
        } catch (final SBonitaReadException bre) {
            throw new SGroupNotFoundException(bre);
        }
    }

    @Override
    public List<SRole> getRoles(final int fromIndex, final int numberOfRoles) throws SIdentityException {
        try {
            return persistenceService.selectList(SelectDescriptorBuilder.getElements(SRole.class, "Role", fromIndex, numberOfRoles));
        } catch (final SBonitaReadException e) {
            throw new SIdentityException("Can't get the roles", e);
        }
    }

    @Override
    public List<SRole> getRoles(final int fromIndex, final int numberOfRoles, final String field, final OrderByType order) throws SIdentityException {
        try {
            return persistenceService.selectList(SelectDescriptorBuilder.getElements(SRole.class, "Role", field, order, fromIndex,
                    numberOfRoles));
        } catch (final SBonitaReadException e) {
            throw new SIdentityException("Can't get the roles", e);
        }
    }

    @Override
    public List<SRole> getRoles(final List<Long> roleIds) throws SRoleNotFoundException {
        if (roleIds == null || roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return persistenceService.selectList(SelectDescriptorBuilder.getElementsByIds(SRole.class, "Role", roleIds));
        } catch (final SBonitaReadException e) {
            throw new SRoleNotFoundException(e);
        }
    }

    @Override
    public SUser getUser(final long userId) throws SUserNotFoundException {
        try {
            final SUser user = persistenceService.selectById(SelectDescriptorBuilder.getElementById(SUser.class, "User", userId));
            if (user == null) {
                throw new SUserNotFoundException(userId);
            }
            return user;
        } catch (final SBonitaReadException e) {
            throw new SUserNotFoundException("Cannot get user with id: " + userId, e);
        }
    }

    @Override
    public SContactInfo getUserContactInfo(final long userId, final boolean isPersonal) throws SIdentityException {
        try {
            return persistenceService.selectOne(SelectDescriptorBuilder.getUserContactInfo(userId, isPersonal));
        } catch (final SBonitaReadException e) {
            throw new SUserNotFoundException("Cannot get user contact info for user id: " + userId, e);
        }
    }

    @Override
    public SUser getUserByUserName(final String userName) throws SUserNotFoundException {
        try {
            final SUser user = persistenceService.selectOne(SelectDescriptorBuilder.getUserByUserName(userName));
            if (user == null) {
                throw new SUserNotFoundException(userName);
            }
            return user;
        } catch (final SBonitaReadException e) {
            throw new SUserNotFoundException("Cannot get user: " + userName, e);
        }
    }

    @Override
    public SUserMembership getUserMembership(final long userMembershipId) throws SIdentityException {
        try {
            final Map<String, Object> parameters = Collections.singletonMap("id", (Object) userMembershipId);
            final SelectOneDescriptor<SUserMembership> desc = new SelectOneDescriptor<>("getSUserMembershipById", parameters,
                    SUserMembership.class);
            final SUserMembership selectOne = persistenceService.selectOne(desc);
            if (selectOne == null) {
                throw new SIdentityException("Can't get the userMembership with id " + userMembershipId, null);
            }
            return selectOne;
        } catch (final SBonitaReadException e) {
            throw new SIdentityException("Can't get the user membership with id " + userMembershipId, e);
        }
    }

    @Override
    public List<SUserMembership> getUserMemberships(final List<Long> userMembershipIds) throws SIdentityException {
        List<Long> localUserMembershipIds = userMembershipIds;
        if (localUserMembershipIds == null || localUserMembershipIds.isEmpty()) {
            localUserMembershipIds = Collections.emptyList();
        }
        try {
            return persistenceService.selectList(SelectDescriptorBuilder.getElementsByIds(SUserMembership.class,
                    "SUserMembership", localUserMembershipIds));
        } catch (final SBonitaReadException e) {
            throw new SIdentityException("Can't get users memberships with ids " + Arrays.toString(localUserMembershipIds.toArray()), e);
        }
    }

    @Override
    public List<SUser> getUsers(final int fromIndex, final int numberOfUsers) throws SIdentityException {
        try {
            return persistenceService.selectList(SelectDescriptorBuilder.getElements(SUser.class, "User", fromIndex, numberOfUsers));
        } catch (final SBonitaReadException e) {
            throw new SIdentityException("Can't get the users", e);
        }
    }

    @Override
    public List<SUser> getUsers(final int fromIndex, final int numberOfUsers, final String field, final OrderByType order) throws SIdentityException {
        try {
            return persistenceService.selectList(SelectDescriptorBuilder.getElements(SUser.class, "User", field, order, fromIndex,
                    numberOfUsers));
        } catch (final SBonitaReadException e) {
            throw new SIdentityException("Can't get the users", e);
        }
    }

    @Override
    public List<SUser> getUsers(final List<Long> userIds) throws SUserNotFoundException {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return persistenceService.selectList(SelectDescriptorBuilder.getElementsByIds(SUser.class, "User", userIds));
        } catch (final SBonitaReadException e) {
            throw new SUserNotFoundException(e);
        }
    }

    @Override
    public List<SUser> getUsersByUsername(final List<String> userNames) throws SIdentityException {
        if (userNames == null || userNames.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            final QueryOptions queryOptions = new QueryOptions(0, userNames.size(), SUser.class, "userName", OrderByType.ASC);
            final Map<String, Object> parameters = Collections.singletonMap("userNames", (Object) userNames);
            return persistenceService.selectList(new SelectListDescriptor<SUser>("getUsersByName", parameters, SUser.class, queryOptions));
        } catch (final SBonitaReadException e) {
            throw new SUserNotFoundException(e);
        }
    }

    @Override
    public List<SUser> getUsersByGroup(final long groupId, final int fromIndex, final int numberOfUsers) throws SIdentityException {
        try {
            return persistenceService.selectList(SelectDescriptorBuilder.getUsersByGroup(groupId, fromIndex, numberOfUsers));
        } catch (final SBonitaReadException e) {
            throw new SIdentityException("Can't get the users having the group " + groupId, e);
        }
    }

    @Override
    public List<SUser> getUsersByGroup(final long groupId, final int fromIndex, final int numberOfUsers, final String field, final OrderByType order)
            throws SIdentityException {
        try {
            return persistenceService.selectList(SelectDescriptorBuilder.getUsersByGroup(groupId, field, order, fromIndex,
                    numberOfUsers));
        } catch (final SBonitaReadException e) {
            throw new SIdentityException("Can't get the users having the group " + groupId, e);
        }
    }

    @Override
    public List<SUser> getUsersByManager(final long managerId, final int fromIndex, final int numberMaxOfUsers) throws SIdentityException {
        try {
            final QueryOptions queryOptions = new QueryOptions(fromIndex, numberMaxOfUsers, SUser.class, "id", OrderByType.DESC);
            return persistenceService.selectList(SelectDescriptorBuilder.getUsersByManager(managerId, queryOptions));
        } catch (final SBonitaReadException e) {
            throw new SIdentityException("Can't get the users having the manager " + managerId, e);
        }
    }

    @Override
    public List<SUser> getUsersByRole(final long roleId, final int fromIndex, final int numberOfUsers) throws SIdentityException {
        try {
            return persistenceService.selectList(SelectDescriptorBuilder.getUsersByRole(roleId, fromIndex, numberOfUsers));
        } catch (final SBonitaReadException e) {
            throw new SIdentityException("Can't get the users having the role " + roleId, e);
        }
    }

    @Override
    public List<SUser> getUsersByRole(final long roleId, final int fromIndex, final int numberOfUsers, final String field, final OrderByType order)
            throws SIdentityException {
        try {
            return persistenceService
                    .selectList(SelectDescriptorBuilder.getUsersByRole(roleId, field, order, fromIndex, numberOfUsers));
        } catch (final SBonitaReadException e) {
            throw new SIdentityException("Can't get the users having the role " + roleId, e);
        }
    }

    @Override
    public void updateGroup(final SGroup group, final EntityUpdateDescriptor descriptor) throws SIdentityException {
        final SGroupLogBuilder logBuilder = getGroupLog(ActionType.UPDATED, "Updating the group");
        try {
            final UpdateRecord updateRecord = UpdateRecord.buildSetFields(group, descriptor);
            SUpdateEvent updateEvent = null;
            if (eventService.hasHandlers(GROUP, EventActionType.UPDATED)) {
                updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(GROUP).setObject(group).done();
            }
            recorder.recordUpdate(updateRecord, updateEvent);
            log(group.getId(), SQueriableLog.STATUS_OK, logBuilder, "updateGroup");
        } catch (final SRecorderException e) {
            log(group.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "updateGroup");
            throw new SIdentityException("Can't update group " + group, e);
        }
    }

    @Override
    public void updateCustomUserInfoDefinition(final SCustomUserInfoDefinition customUserInfo, final EntityUpdateDescriptor descriptor)
            throws SIdentityException {
        final String methodName = "updateCustomUserInfoDefinition";
        final SCustomUserInfoDefinitionLogBuilder logBuilder = getSCustomUserInfoDefinitionLog(ActionType.UPDATED,
                "Updating the custom user info definition with name " + customUserInfo.getName());
        try {
            final UpdateRecord updateRecord = UpdateRecord.buildSetFields(customUserInfo, descriptor);
            SUpdateEvent updateEvent = null;
            if (eventService.hasHandlers(CUSTOM_USER_INFO_DEFINITION, EventActionType.UPDATED)) {
                updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(CUSTOM_USER_INFO_DEFINITION)
                        .setObject(customUserInfo).done();
            }
            recorder.recordUpdate(updateRecord, updateEvent);
            log(customUserInfo.getId(), SQueriableLog.STATUS_OK, logBuilder, methodName);
        } catch (final SRecorderException e) {
            log(customUserInfo.getId(), SQueriableLog.STATUS_FAIL, logBuilder, methodName);
            throw new SIdentityException("Can't update custom user info definition " + customUserInfo, e);
        }
    }

    @Override
    public void updateCustomUserInfoValue(final SCustomUserInfoValue customUserInfo, final EntityUpdateDescriptor descriptor) throws SIdentityException {
        try {
            final UpdateRecord updateRecord = UpdateRecord.buildSetFields(customUserInfo, descriptor);
            SUpdateEvent updateEvent = null;
            if (eventService.hasHandlers(CUSTOM_USER_INFO_VALUE, EventActionType.UPDATED)) {
                updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(CUSTOM_USER_INFO_VALUE).setObject(customUserInfo)
                        .done();
            }
            recorder.recordUpdate(updateRecord, updateEvent);
        } catch (final SRecorderException e) {
            throw new SIdentityException("Can't update custom user info definition " + customUserInfo, e);
        }
    }

    @Override
    public void updateRole(final SRole role, final EntityUpdateDescriptor descriptor) throws SIdentityException {
        final String methodName = "updateRole";
        final SRoleLogBuilder logBuilder = getRoleLog(ActionType.UPDATED, "Updating the role with name " + role.getName());
        try {
            final UpdateRecord updateRecord = UpdateRecord.buildSetFields(role, descriptor);
            SUpdateEvent updateEvent = null;
            if (eventService.hasHandlers(ROLE, EventActionType.UPDATED)) {
                updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(ROLE).setObject(role).done();
            }
            recorder.recordUpdate(updateRecord, updateEvent);
            log(role.getId(), SQueriableLog.STATUS_FAIL, logBuilder, methodName);
        } catch (final SRecorderException e) {
            log(role.getId(), SQueriableLog.STATUS_FAIL, logBuilder, methodName);
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
        final String methodName = "updateUser";
        if (!isPasswordEncrypted) {
            final String password = (String) descriptor.getFields().get("password");
            if (password != null) {
                final String hash = encrypter.hash(password);
                descriptor.getFields().put("password", hash);
            }
        }
        final SUserLogBuilder logBuilder = getUserLog(ActionType.UPDATED, "Updating user with user name " +
                user.getUserName() +
                ", first name " +
                user.getFirstName() +
                ", last name " +
                user.getLastName());
        try {
            final UpdateRecord updateRecord = UpdateRecord.buildSetFields(user, descriptor);
            SUpdateEvent updateEvent = null;
            if (eventService.hasHandlers(USER, EventActionType.UPDATED)) {
                updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(USER).setObject(user).done();
                final SUser oldUser = BuilderFactory.get(SUserBuilderFactory.class).createNewInstance(user).done();
                updateEvent.setOldObject(oldUser);
            }
            recorder.recordUpdate(updateRecord, updateEvent);
            log(user.getId(), SQueriableLog.STATUS_OK, logBuilder, methodName);
        } catch (final SRecorderException re) {
            log(user.getId(), SQueriableLog.STATUS_FAIL, logBuilder, methodName);
            throw new SUserUpdateException(re);
        }
    }

    @Override
    public void updateUserContactInfo(final SContactInfo contactInfo, final EntityUpdateDescriptor descriptor) throws SIdentityException {
        final String methodName = "updateUserContactInfo";
        final SContactInfoLogBuilder logBuilder = getUserContactInfoLog(ActionType.UPDATED,
                "Updating " + (contactInfo.isPersonal() ? "personal" : "professional") + " user contact Info for user with Id " +
                        contactInfo.getUserId(),
                contactInfo);
        try {
            final UpdateRecord updateRecord = UpdateRecord.buildSetFields(contactInfo, descriptor);
            SUpdateEvent updateEvent = null;
            if (eventService.hasHandlers(USER_CONTACT_INFO, EventActionType.UPDATED)) {
                updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(USER_CONTACT_INFO).setObject(contactInfo).done();
                final SContactInfo oldContactInfo = BuilderFactory.get(SContactInfoBuilderFactory.class).createNewInstance(contactInfo).done();
                updateEvent.setOldObject(oldContactInfo);
            }
            recorder.recordUpdate(updateRecord, updateEvent);
            log(contactInfo.getId(), SQueriableLog.STATUS_OK, logBuilder, methodName);
        } catch (final SRecorderException re) {
            log(contactInfo.getId(), SQueriableLog.STATUS_FAIL, logBuilder, methodName);
            throw new SUserUpdateException(re);
        }
    }

    @Override
    public void updateUserMembership(final SUserMembership userMembership, final EntityUpdateDescriptor descriptor) throws SIdentityException {
        final String methodName = "updateUserMembership";
        final SUserMembershipLogBuilder logBuilder = getUserMembershipLog(ActionType.UPDATED, "Updating user membership for user " +
                userMembership.getUsername() +
                " with role " +
                userMembership.getRoleName() +
                " in group " +
                userMembership.getGroupName(), userMembership);
        try {
            final UpdateRecord updateRecord = UpdateRecord.buildSetFields(userMembership, descriptor);
            SUpdateEvent updateEvent = null;
            if (eventService.hasHandlers(USERMEMBERSHIP, EventActionType.UPDATED)) {
                updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(USERMEMBERSHIP).setObject(userMembership).done();
            }
            recorder.recordUpdate(updateRecord, updateEvent);
            log(userMembership.getId(), SQueriableLog.STATUS_OK, logBuilder, methodName);
        } catch (final SRecorderException e) {
            log(userMembership.getId(), SQueriableLog.STATUS_FAIL, logBuilder, methodName);
            throw new SIdentityException("Can't update user membership " + userMembership, e);
        }
    }

    @Override
    public List<SUserMembership> getUserMemberships(final int fromIndex, final int numberOfUserMemberships) throws SIdentityException {
        final SelectListDescriptor<SUserMembership> descriptor = SelectDescriptorBuilder.getElements(SUserMembership.class, "UserMembership", fromIndex,
                numberOfUserMemberships);
        try {
            return persistenceService.selectList(descriptor);
        } catch (final SBonitaReadException e) {
            throw new SIdentityException("Can't get the user memberships", e);
        }
    }

    @Override
    public List<SUserMembership> getUserMembershipsOfUser(final long userId, final int fromIndex, final int numberOfUsers) throws SIdentityException {
        try {
            final SelectListDescriptor<SUserMembership> descriptor = SelectDescriptorBuilder.getUserMembershipsOfUser(userId, fromIndex, numberOfUsers);
            return persistenceService.selectList(descriptor);
        } catch (final SBonitaReadException e) {
            throw new SIdentityException("Can't get the memberships having the user " + userId, e);
        }
    }

    @Override
    public List<SUserMembership> getUserMembershipsOfUser(final long userId, final int fromIndex, final int numberOfMemberships, final String field,
            final OrderByType order) throws SIdentityException {
        try {
            final SelectListDescriptor<SUserMembership> descriptor = SelectDescriptorBuilder.getUserMembershipsOfUser(userId, field, order, fromIndex,
                    numberOfMemberships);
            return persistenceService.selectList(descriptor);
        } catch (final SBonitaReadException e) {
            throw new SIdentityException("Can't get the memberships having the user" + userId, e);
        }
    }

    @Override
    public List<SUserMembership> getUserMembershipsOfUser(final long userId, final int fromIndex, final int numberPerPage, final OrderByOption orderByOption)
            throws SIdentityException {
        try {
            final SelectListDescriptor<SUserMembership> descriptor = SelectDescriptorBuilder.getUserMembershipsOfUser(userId, new QueryOptions(fromIndex,
                    numberPerPage, Collections.singletonList(orderByOption)));
            return persistenceService.selectList(descriptor);
        } catch (final SBonitaReadException e) {
            throw new SIdentityException("Can't get the memberships having the user" + userId, e);
        }
    }

    @Override
    public SUserMembership getUserMembership(final long userId, final long groupId, final long roleId) throws SIdentityException {
        final SelectOneDescriptor<SUserMembership> descriptor = SelectDescriptorBuilder.getUserMembership(userId, groupId, roleId);
        try {
            return getUserMembership(userId, groupId, roleId, descriptor);
        } catch (final SBonitaReadException e) {
            throw new SIdentityException("Can't get the userMembership with userId = " + userId + ", groupId = " + groupId + ", roleId = " + roleId, e);
        }
    }

    @Override
    public SUserMembership getLightUserMembership(final long userId, final long groupId, final long roleId) throws SIdentityException {
        final SelectOneDescriptor<SUserMembership> descriptor = SelectDescriptorBuilder.getLightUserMembership(userId, groupId, roleId);
        try {
            return getUserMembership(userId, groupId, roleId, descriptor);
        } catch (final SBonitaReadException e) {
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
        try {
            return persistenceService.selectOne(SelectDescriptorBuilder.getNumberOfElement("UserMembership", SUserMembership.class));
        } catch (final SBonitaReadException e) {
            throw new SIdentityException("Can't get the number of user membership", e);
        }
    }

    @Override
    public List<SUserMembership> getLightUserMemberships(final int startIndex, final int numberOfElements) throws SIdentityException {
        final SelectListDescriptor<SUserMembership> descriptor = SelectDescriptorBuilder.getElements(SUserMembership.class, "LightUserMembership", startIndex,
                numberOfElements);
        try {
            return persistenceService.selectList(descriptor);
        } catch (final SBonitaReadException e) {
            throw new SIdentityException("Can't get the user memberships", e);
        }
    }

    @Override
    public long getNumberOfUsers(final QueryOptions options) throws SBonitaReadException {
        return persistenceService.getNumberOfEntities(SUser.class, options, null);
    }

    @Override
    public List<SUser> searchUsers(final QueryOptions options) throws SBonitaReadException {
        return persistenceService.searchEntity(SUser.class, options, null);
    }

    @Override
    public long getNumberOfRoles(final QueryOptions options) throws SBonitaReadException {
        return persistenceService.getNumberOfEntities(SRole.class, options, null);
    }

    @Override
    public List<SRole> searchRoles(final QueryOptions options) throws SBonitaReadException {
        return persistenceService.searchEntity(SRole.class, options, null);
    }

    @Override
    public long getNumberOfGroups(final QueryOptions options) throws SBonitaReadException {
        return persistenceService.getNumberOfEntities(SGroup.class, options, null);
    }

    @Override
    public List<SGroup> searchGroups(final QueryOptions options) throws SBonitaReadException {
        return persistenceService.searchEntity(SGroup.class, options, null);
    }

    private void log(final long objectId, final int sQueriableLogStatus, final SPersistenceLogBuilder logBuilder, final String methodName) {
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
    public List<SCustomUserInfoValue> searchCustomUserInfoValue(final QueryOptions options) throws SBonitaReadException {
        return persistenceService.searchEntity(SCustomUserInfoValue.class, options, null);
    }

    @Override
    public SUser updateUser(long userId, EntityUpdateDescriptor userUpdateDescriptor, EntityUpdateDescriptor personalDataUpdateDescriptor,
            EntityUpdateDescriptor professionalDataUpdateDescriptor) throws SIdentityException {
        // User change
        SUser sUser = getUser(userId);
        if (userUpdateDescriptor != null && !userUpdateDescriptor.getFields().isEmpty()) {
            updateUser(sUser, userUpdateDescriptor);
        }

        // Personal data change
        if (personalDataUpdateDescriptor != null && !personalDataUpdateDescriptor.getFields().isEmpty()) {
            SContactInfo persoContactInfo = getUserContactInfo(userId, true);
            if (persoContactInfo == null) {
                persoContactInfo = BuilderFactory.get(SContactInfoBuilderFactory.class).createNewInstance(userId, true).done();
                createUserContactInfo(persoContactInfo);
            }
            updateUserContactInfo(persoContactInfo, personalDataUpdateDescriptor);
        }

        // Professional data change
        if (professionalDataUpdateDescriptor != null && !professionalDataUpdateDescriptor.getFields().isEmpty()) {
            SContactInfo professContactInfo = getUserContactInfo(userId, false);
            if (professContactInfo == null) {
                professContactInfo = BuilderFactory.get(SContactInfoBuilderFactory.class).createNewInstance(userId, false).done();
                createUserContactInfo(professContactInfo);
            }
            updateUserContactInfo(professContactInfo, professionalDataUpdateDescriptor);
        }
        return sUser;
    }

    @Override
    public SUser createUser(SUser sUser, SContactInfo personalContactInfo, SContactInfo proContactInfo) throws SUserCreationException {
        SUser user = createUser(sUser);
        if (personalContactInfo != null) {
            createUserContactInfo(BuilderFactory.get(SContactInfoBuilderFactory.class).createNewInstance(personalContactInfo).setUserId(user.getId()).done());
        }
        if (proContactInfo != null) {
            createUserContactInfo(BuilderFactory.get(SContactInfoBuilderFactory.class).createNewInstance(proContactInfo).setUserId(user.getId()).done());
        }
        return user;
    }
}
