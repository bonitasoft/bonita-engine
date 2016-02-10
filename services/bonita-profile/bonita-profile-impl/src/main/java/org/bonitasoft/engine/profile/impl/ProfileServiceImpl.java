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
package org.bonitasoft.engine.profile.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.LogUtil;
import org.bonitasoft.engine.commons.NullCheckingUtil;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.events.EventActionType;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilderFactory;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.builder.SProfileBuilderFactory;
import org.bonitasoft.engine.profile.builder.SProfileEntryBuilderFactory;
import org.bonitasoft.engine.profile.builder.impl.SProfileLogBuilderImpl;
import org.bonitasoft.engine.profile.builder.impl.SProfileMemberLogBuilderImpl;
import org.bonitasoft.engine.profile.exception.profile.SProfileCreationException;
import org.bonitasoft.engine.profile.exception.profile.SProfileDeletionException;
import org.bonitasoft.engine.profile.exception.profile.SProfileNotFoundException;
import org.bonitasoft.engine.profile.exception.profile.SProfileUpdateException;
import org.bonitasoft.engine.profile.exception.profileentry.SProfileEntryCreationException;
import org.bonitasoft.engine.profile.exception.profileentry.SProfileEntryDeletionException;
import org.bonitasoft.engine.profile.exception.profileentry.SProfileEntryNotFoundException;
import org.bonitasoft.engine.profile.exception.profileentry.SProfileEntryUpdateException;
import org.bonitasoft.engine.profile.exception.profilemember.SProfileMemberCreationException;
import org.bonitasoft.engine.profile.exception.profilemember.SProfileMemberDeletionException;
import org.bonitasoft.engine.profile.exception.profilemember.SProfileMemberNotFoundException;
import org.bonitasoft.engine.profile.model.SProfile;
import org.bonitasoft.engine.profile.model.SProfileEntry;
import org.bonitasoft.engine.profile.model.SProfileMember;
import org.bonitasoft.engine.profile.model.impl.SProfileMemberImpl;
import org.bonitasoft.engine.profile.persistence.SelectDescriptorBuilder;
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
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.bonitasoft.engine.session.SSessionNotFoundException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.sessionaccessor.SessionIdNotSetException;

/**
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 */
public class ProfileServiceImpl implements ProfileService {

    private static final int BATCH_NUMBER = 1000;

    private static final String QUERY_UPDATE_LASTUPDATE_PROFILE = "updateLastupdatePtofile";

    private final PersistenceService persistenceService;

    private final Recorder recorder;

    private final EventService eventService;

    private final TechnicalLoggerService logger;

    private final QueriableLoggerService queriableLoggerService;

    private final SessionService sessionService;

    private final ReadSessionAccessor sessionAccessor;

    public ProfileServiceImpl(final PersistenceService persistenceService, final Recorder recorder, final EventService eventService,
            final TechnicalLoggerService logger, final QueriableLoggerService queriableLoggerService, final ReadSessionAccessor sessionAccessor,
            final SessionService sessionService) {
        super();
        this.persistenceService = persistenceService;
        this.recorder = recorder;
        this.eventService = eventService;
        this.logger = logger;
        this.queriableLoggerService = queriableLoggerService;
        this.sessionAccessor = sessionAccessor;
        this.sessionService = sessionService;
    }

    @Override
    public SProfile createProfile(final SProfile profile) throws SProfileCreationException {
        logBeforeMethod("createProfile");
        final SProfileLogBuilderImpl logBuilder = getSProfileLog(ActionType.CREATED, "Adding a new profile");
        final InsertRecord insertRecord = new InsertRecord(profile);
        SInsertEvent insertEvent = null;
        if (eventService.hasHandlers(PROFILE, EventActionType.CREATED)) {
            insertEvent = (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent(PROFILE).setObject(profile).done();
        }
        try {
            recorder.recordInsert(insertRecord, insertEvent);
            log(profile.getId(), SQueriableLog.STATUS_OK, logBuilder, "createProfile");
            logAfterMethod("createProfile");
            return profile;
        } catch (final SRecorderException re) {
            logOnExceptionMethod("createProfile", re);
            log(profile.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "createProfile");
            throw new SProfileCreationException(re);
        }
    }

    @Override
    public SProfile getProfile(final long profileId) throws SProfileNotFoundException {
        logBeforeMethod("getProfile");
        try {
            final SelectByIdDescriptor<SProfile> descriptor = SelectDescriptorBuilder.getElementById(SProfile.class, "Profile", profileId);
            final SProfile profile = persistenceService.selectById(descriptor);
            if (profile == null) {
                throw new SProfileNotFoundException("No profile exists with id: " + profileId);
            }
            logAfterMethod("getProfile");
            return profile;
        } catch (final SBonitaReadException e) {
            logOnExceptionMethod("getProfile", e);
            throw new SProfileNotFoundException(e);
        }
    }

    @Override
    public SProfile getProfileByName(final String profileName) throws SProfileNotFoundException {
        logBeforeMethod("getProfileByName");
        try {
            final SelectOneDescriptor<SProfile> descriptor = SelectDescriptorBuilder.getElementByNameDescriptor(SProfile.class, "Profile", profileName);
            final SProfile profile = persistenceService.selectOne(descriptor);
            if (profile == null) {
                throw new SProfileNotFoundException("No profile exists with name: " + profileName);
            }
            logAfterMethod("getProfileByName");
            return profile;
        } catch (final SBonitaReadException e) {
            logOnExceptionMethod("getProfileByName", e);
            throw new SProfileNotFoundException(e);
        }
    }

    @Override
    public List<SProfile> getProfiles(final List<Long> profileIds) throws SProfileNotFoundException {
        logBeforeMethod("getProfiles");
        final List<SProfile> profiles = new ArrayList<SProfile>();
        if (profileIds != null) {
            for (final Long profileId : profileIds) {
                final SProfile profile = getProfile(profileId);
                profiles.add(profile);
            }
        }
        logAfterMethod("getProfiles");
        return profiles;
    }

    @Override
    public SProfile updateProfile(final SProfile sProfile, final EntityUpdateDescriptor descriptor) throws SProfileUpdateException {
        logBeforeMethod("updateProfile");
        NullCheckingUtil.checkArgsNotNull(sProfile);
        final SProfileLogBuilderImpl logBuilder = getSProfileLog(ActionType.UPDATED, "Updating profile");
        final SProfile oldUser = BuilderFactory.get(SProfileBuilderFactory.class).createNewInstance(sProfile).done();
        final UpdateRecord updateRecord = UpdateRecord.buildSetFields(sProfile, descriptor);
        SUpdateEvent updateEvent = null;
        if (eventService.hasHandlers(PROFILE, EventActionType.UPDATED)) {
            updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(PROFILE).setObject(sProfile).done();
            updateEvent.setOldObject(oldUser);
        }
        try {
            recorder.recordUpdate(updateRecord, updateEvent);
            log(sProfile.getId(), SQueriableLog.STATUS_OK, logBuilder, "updateProfile");
            logAfterMethod("updateProfile");
        } catch (final SRecorderException re) {
            logOnExceptionMethod("updateProfile", re);
            log(sProfile.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "updateProfile");
            throw new SProfileUpdateException(re);
        }
        return sProfile;
    }

    @Override
    public void deleteProfile(final SProfile profile) throws SProfileDeletionException, SProfileEntryDeletionException, SProfileMemberDeletionException {
        logBeforeMethod("deleteProfile");
        final SProfileLogBuilderImpl logBuilder = getSProfileLog(ActionType.DELETED, "Deleting profile");
        final DeleteRecord deleteRecord = new DeleteRecord(profile);
        SDeleteEvent deleteEvent = null;
        if (eventService.hasHandlers(PROFILE, EventActionType.DELETED)) {
            deleteEvent = (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(PROFILE).setObject(profile).done();
        }
        try {
            deleteAllProfileEntriesOfProfile(profile);
            deleteAllProfileMembersOfProfile(profile);
            recorder.recordDelete(deleteRecord, deleteEvent);
            log(profile.getId(), SQueriableLog.STATUS_OK, logBuilder, "deleteProfile");
            logAfterMethod("deleteProfile");
        } catch (final SRecorderException re) {
            logOnExceptionMethod("deleteProfile", re);
            log(profile.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "deleteProfile");
            throw new SProfileDeletionException(re);
        } catch (final SProfileEntryDeletionException e) {
            logOnExceptionMethod("deleteProfile", e);
            throw e;
        } catch (final SProfileMemberDeletionException e) {
            logOnExceptionMethod("deleteProfile", e);
            throw e;
        }
    }

    @Override
    public void deleteAllProfileMembersOfProfile(final SProfile profile) throws SProfileMemberDeletionException {
        final QueryOptions queryOptions = new QueryOptions(0, 100, SProfileMember.class, "id", OrderByType.ASC);
        try {
            List<SProfileMember> sProfileMembers;
            do {
                sProfileMembers = getProfileMembers(profile.getId(), queryOptions);
                for (final SProfileMember profileUser : sProfileMembers) {
                    deleteProfileMember(profileUser);
                }
            } while (!sProfileMembers.isEmpty());
        } catch (final SProfileMemberNotFoundException e) {
            throw new SProfileMemberDeletionException(e);
        }
    }

    @Override
    public void deleteAllProfileEntriesOfProfile(final SProfile profile) throws SProfileEntryDeletionException {
        try {
            List<SProfileEntry> entries;
            do {
                entries = getEntriesOfProfile(profile.getId(), 0, BATCH_NUMBER, "id", OrderByType.ASC);
                for (final SProfileEntry entry : entries) {
                    deleteProfileEntry(entry);
                }
            } while (!entries.isEmpty());
        } catch (final SBonitaReadException e) {
            throw new SProfileEntryDeletionException(e);
        }
    }

    @Override
    public void deleteProfile(final long profileId) throws SProfileNotFoundException, SProfileDeletionException, SProfileEntryDeletionException,
            SProfileMemberDeletionException {
        logBeforeMethod("deleteProfile");
        final SProfile profile = getProfile(profileId);
        deleteProfile(profile);
        logAfterMethod("deleteProfile");
    }

    @Override
    public SProfileEntry getProfileEntry(final long profileEntryId) throws SProfileEntryNotFoundException {
        logBeforeMethod("getProfileEntry");
        try {
            final SelectByIdDescriptor<SProfileEntry> descriptor = SelectDescriptorBuilder.getElementById(SProfileEntry.class, "ProfileEntry", profileEntryId);
            final SProfileEntry profileEntry = persistenceService.selectById(descriptor);
            if (profileEntry == null) {
                throw new SProfileEntryNotFoundException("No entry exists with id: " + profileEntryId);
            }
            logAfterMethod("getProfileEntry");
            return profileEntry;
        } catch (final SBonitaReadException bre) {
            logOnExceptionMethod("getProfileEntry", bre);
            throw new SProfileEntryNotFoundException(bre);
        }
    }

    @Override
    public List<SProfileEntry> getEntriesOfProfile(final long profileId, final int fromIndex, final int numberOfProfileEntries, final String field,
            final OrderByType order) throws SBonitaReadException {
        logBeforeMethod("getEntriesOfProfile");
        try {
            final List<SProfileEntry> listspEntries = persistenceService.selectList(SelectDescriptorBuilder.getEntriesOfProfile(profileId, field, order,
                    fromIndex, numberOfProfileEntries));
            logAfterMethod("getEntriesOfProfile");
            return listspEntries;
        } catch (final SBonitaReadException bre) {
            logOnExceptionMethod("getEntriesOfProfile", bre);
            throw bre;
        }
    }

    @Override
    public List<SProfileEntry> getEntriesOfProfileByParentId(final long profileId, final long parentId, final int fromIndex, final int numberOfProfileEntries,
            final String field, final OrderByType order) throws SBonitaReadException {
        logBeforeMethod("getEntriesOfProfileByParentId");
        try {
            final List<SProfileEntry> listspEntries = persistenceService.selectList(SelectDescriptorBuilder.getEntriesOfProfile(profileId, parentId, field,
                    order, fromIndex, numberOfProfileEntries));
            logAfterMethod("getEntriesOfProfileByParentId");
            return listspEntries;
        } catch (final SBonitaReadException bre) {
            logOnExceptionMethod("getEntriesOfProfileByParentId", bre);
            throw bre;
        }
    }

    @Override
    public SProfileEntry createProfileEntry(final SProfileEntry profileEntry) throws SProfileEntryCreationException {
        logBeforeMethod("createProfileEntry");
        final SProfileLogBuilderImpl logBuilder = getSProfileLog(ActionType.CREATED, "Adding a new pofile entry");
        final InsertRecord insertRecord = new InsertRecord(profileEntry);
        SInsertEvent insertEvent = null;
        if (eventService.hasHandlers(PROFILE, EventActionType.CREATED)) {
            insertEvent = (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent(ENTRY_PROFILE).setObject(profileEntry).done();
        }
        try {
            recorder.recordInsert(insertRecord, insertEvent);
            log(profileEntry.getId(), SQueriableLog.STATUS_OK, logBuilder, "createProfileEntry");
            logAfterMethod("createProfileEntry");
            return profileEntry;
        } catch (final SRecorderException re) {
            logOnExceptionMethod("createProfileEntry", re);
            log(profileEntry.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "createProfileEntry");
            throw new SProfileEntryCreationException(re);
        }
    }

    @Override
    public SProfileEntry updateProfileEntry(final SProfileEntry profileEntry, final EntityUpdateDescriptor descriptor) throws SProfileEntryUpdateException {
        logBeforeMethod("updateProfileEntry");
        NullCheckingUtil.checkArgsNotNull(profileEntry);
        final SProfileLogBuilderImpl logBuilder = getSProfileLog(ActionType.UPDATED, "Updating profile entry");
        try {
            final SProfileEntry oldProfileEntry = BuilderFactory.get(SProfileEntryBuilderFactory.class).createNewInstance(profileEntry).done();
            final UpdateRecord updateRecord = UpdateRecord.buildSetFields(profileEntry, descriptor);
            SUpdateEvent updateEvent = null;
            if (eventService.hasHandlers(ENTRY_PROFILE, EventActionType.UPDATED)) {
                updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(ENTRY_PROFILE).setObject(profileEntry).done();
                updateEvent.setOldObject(oldProfileEntry);
            }
            recorder.recordUpdate(updateRecord, updateEvent);
            log(profileEntry.getId(), SQueriableLog.STATUS_OK, logBuilder, "updateProfileEntry");
            logAfterMethod("updateProfileEntry");
        } catch (final SRecorderException re) {
            logOnExceptionMethod("updateProfileEntry", re);
            log(profileEntry.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "updateProfileEntry");
            throw new SProfileEntryUpdateException(re);
        }
        return profileEntry;
    }

    @Override
    public void deleteProfileEntry(final SProfileEntry profileEntry) throws SProfileEntryDeletionException {
        logBeforeMethod("deleteProfileEntry");
        final SProfileLogBuilderImpl logBuilder = getSProfileLog(ActionType.DELETED, "Deleting profile entry");
        try {
            final DeleteRecord deleteRecord = new DeleteRecord(profileEntry);
            SDeleteEvent deleteEvent = null;
            if (eventService.hasHandlers(ENTRY_PROFILE, EventActionType.DELETED)) {
                deleteEvent = (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(ENTRY_PROFILE).setObject(profileEntry).done();
            }
            recorder.recordDelete(deleteRecord, deleteEvent);
            log(profileEntry.getId(), SQueriableLog.STATUS_OK, logBuilder, "deleteProfileEntry");
            logAfterMethod("deleteProfileEntry");
        } catch (final SRecorderException re) {
            logOnExceptionMethod("deleteProfileEntry", re);
            log(profileEntry.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "deleteProfileEntry");
            throw new SProfileEntryDeletionException(re);
        }
    }

    @Override
    public void deleteProfileEntry(final long profileEntryId) throws SProfileEntryNotFoundException, SProfileEntryDeletionException {
        logBeforeMethod("deleteProfileEntry");
        final SProfileEntry profileEntry = getProfileEntry(profileEntryId);
        deleteProfileEntry(profileEntry);
        logAfterMethod("deleteProfileEntry");
    }

    private SProfileMemberImpl buildProfileMember(final long profileId, final String displayNamePart1, final String displayNamePart2,
            final String displayNamePart3) {
        final SProfileMemberImpl profileMember = new SProfileMemberImpl(profileId);
        profileMember.setDisplayNamePart1(displayNamePart1);
        profileMember.setDisplayNamePart2(displayNamePart2);
        profileMember.setDisplayNamePart1(displayNamePart3);
        return profileMember;
    }

    @Override
    public SProfileMember addUserToProfile(final long profileId, final long userId, final String firstName, final String lastName, final String userName)
            throws SProfileMemberCreationException {
        logBeforeMethod("addUserToProfile");
        final SProfileMemberImpl profileMember = buildProfileMember(profileId, firstName, lastName, userName);
        profileMember.setUserId(userId);
        createProfileMember(profileMember);
        logAfterMethod("addUserToProfile");
        return profileMember;
    }

    private void createProfileMember(final SProfileMemberImpl profileMember) throws SProfileMemberCreationException {
        final String message = "Adding a new profile member";
        final SProfileMemberLogBuilderImpl logBuilder = getProfileMemberLog(ActionType.CREATED, message);
        try {
            final InsertRecord insertRecord = new InsertRecord(profileMember);
            SInsertEvent insertEvent = null;
            if (eventService.hasHandlers(PROFILE, EventActionType.CREATED)) {
                insertEvent = (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent(PROFILE_MEMBER).setObject(profileMember).done();
            }
            recorder.recordInsert(insertRecord, insertEvent);
            log(profileMember.getId(), SQueriableLog.STATUS_OK, logBuilder, "insertProfileMember");
        } catch (final SRecorderException re) {
            log(profileMember.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "insertProfileMember");
            throw new SProfileMemberCreationException(re);
        }
    }

    @Override
    public SProfileMember addGroupToProfile(final long profileId, final long groupId, final String groupName, final String parentPath)
            throws SProfileMemberCreationException {
        logBeforeMethod("addGroupToProfile");
        final SProfileMemberImpl profileMember = buildProfileMember(profileId, groupName, parentPath, null);
        profileMember.setGroupId(groupId);
        createProfileMember(profileMember);
        logAfterMethod("addGroupToProfile");
        return profileMember;
    }

    @Override
    public SProfileMember addRoleToProfile(final long profileId, final long roleId, final String roleName) throws SProfileMemberCreationException {
        logBeforeMethod("addRoleToProfile");
        final SProfileMemberImpl profileMember = buildProfileMember(profileId, roleName, null, null);
        profileMember.setRoleId(roleId);
        createProfileMember(profileMember);
        logAfterMethod("addRoleToProfile");
        return profileMember;
    }

    @Override
    public SProfileMember addRoleAndGroupToProfile(final long profileId, final long roleId, final long groupId, final String roleName, final String groupName,
            final String groupParentPath) throws SProfileMemberCreationException {
        logBeforeMethod("addRoleAndGroupToProfile");
        final SProfileMemberImpl profileMember = buildProfileMember(profileId, roleName, groupName, groupParentPath);
        profileMember.setGroupId(groupId);
        profileMember.setRoleId(roleId);
        createProfileMember(profileMember);
        logAfterMethod("addRoleAndGroupToProfile");
        return profileMember;
    }

    @Override
    public void deleteProfileMember(final long profileMemberId) throws SProfileMemberDeletionException, SProfileMemberNotFoundException {
        logBeforeMethod("deleteProfileMember");
        final SProfileMember profileMember = getProfileMemberWithoutDisplayName(profileMemberId);
        deleteProfileMember(profileMember);
        logAfterMethod("deleteProfileMember");
    }

    @Override
    public SProfileMember getProfileMemberWithoutDisplayName(final long profileMemberId) throws SProfileMemberNotFoundException {
        final SelectByIdDescriptor<SProfileMember> selectByIdDescriptor = SelectDescriptorBuilder.getProfileMemberWithoutDisplayName(profileMemberId);
        try {
            final SProfileMember profileMember = persistenceService.selectById(selectByIdDescriptor);
            if (profileMember == null) {
                throw new SProfileMemberNotFoundException(profileMemberId + " does not refer to any profile member");
            }
            return profileMember;
        } catch (final SBonitaReadException bre) {
            throw new SProfileMemberNotFoundException(bre);
        }
    }

    @Override
    public void deleteProfileMember(final SProfileMember profileMember) throws SProfileMemberDeletionException {
        logBeforeMethod("deleteProfileMember");
        final String message = "Deleting profile member for userId " + profileMember.getUserId() + " with roleId " + profileMember.getRoleId() + " in groupId "
                + profileMember.getGroupId();
        final SProfileMemberLogBuilderImpl logBuilder = getProfileMemberLog(ActionType.DELETED, message);
        try {
            final DeleteRecord deleteRecord = new DeleteRecord(profileMember);
            SDeleteEvent deleteEvent = null;
            if (eventService.hasHandlers(PROFILE_MEMBER, EventActionType.DELETED)) {
                deleteEvent = (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(PROFILE_MEMBER).setObject(profileMember).done();
            }
            recorder.recordDelete(deleteRecord, deleteEvent);
            log(profileMember.getId(), SQueriableLog.STATUS_OK, logBuilder, "deleteProfileMember");
            logAfterMethod("deleteProfileMember");
        } catch (final SRecorderException re) {
            logOnExceptionMethod("deleteProfileMember", re);
            log(profileMember.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "deleteProfileMember");
            throw new SProfileMemberDeletionException(re);
        }
    }

    @Override
    public List<SProfileMember> getProfileMembersOfUser(final long userId, final int fromIndex, final int numberOfElements, final String field,
            final OrderByType order) throws SBonitaReadException {
        logBeforeMethod("getProfileMembersOfUser");
        try {
            final SelectListDescriptor<SProfileMember> descriptor = SelectDescriptorBuilder.getDirectProfileMembersOfUser(userId, field, order, fromIndex,
                    numberOfElements);
            final List<SProfileMember> listspProfileMembers = persistenceService.selectList(descriptor);
            logAfterMethod("getProfileMembersOfUser");
            return listspProfileMembers;
        } catch (final SBonitaReadException bre) {
            logOnExceptionMethod("getProfileMembersOfUser", bre);
            throw bre;
        }
    }

    @Override
    public List<SProfileMember> getProfileMembersOfGroup(final long groupId, final int fromIndex, final int numberOfElements, final String field,
            final OrderByType order) throws SBonitaReadException {
        logBeforeMethod("getProfileMembersOfGroup");
        try {
            final SelectListDescriptor<SProfileMember> descriptor = SelectDescriptorBuilder.getDirectProfileMembersOfGroup(groupId, field, order, fromIndex,
                    numberOfElements);
            final List<SProfileMember> listspProfileMembers = persistenceService.selectList(descriptor);
            logAfterMethod("getProfileMembersOfGroup");
            return listspProfileMembers;
        } catch (final SBonitaReadException bre) {
            logOnExceptionMethod("getProfileMembersOfGroup", bre);
            throw bre;
        }
    }

    @Override
    public List<SProfileMember> getProfileMembersOfRole(final long roleId, final int fromIndex, final int numberOfElements, final String field,
            final OrderByType order) throws SBonitaReadException {
        logBeforeMethod("getProfileMembersOfRole");
        try {
            final SelectListDescriptor<SProfileMember> descriptor = SelectDescriptorBuilder.getDirectProfileMembersOfRole(roleId, field, order, fromIndex,
                    numberOfElements);
            final List<SProfileMember> listspProfileMembers = persistenceService.selectList(descriptor);
            logAfterMethod("getProfileMembersOfRole");
            return listspProfileMembers;
        } catch (final SBonitaReadException bre) {
            logOnExceptionMethod("getProfileMembersOfRole", bre);
            throw bre;
        }
    }

    @Override
    public List<SProfile> searchProfilesOfUser(final long userId, final int fromIndex, final int numberOfElements, final String field, final OrderByType order)
            throws SBonitaReadException {
        logBeforeMethod("getProfilesOfUser");
        final SelectListDescriptor<SProfile> descriptor = SelectDescriptorBuilder.getProfilesOfUser(userId, fromIndex, numberOfElements, field, order);
        try {
            final List<SProfile> sProfiles = persistenceService.selectList(descriptor);
            logAfterMethod("getProfilesOfUser");
            return sProfiles;
        } catch (final SBonitaReadException bre) {
            logOnExceptionMethod("getProfilesOfUser", bre);
            throw bre;
        }
    }

    @Override
    public List<SProfileMember> getProfileMembers(final long profileId, final QueryOptions queryOptions) throws SProfileMemberNotFoundException {
        try {
            final SelectListDescriptor<SProfileMember> descriptor = SelectDescriptorBuilder.getSProfileMembersWithoutDisplayName(profileId, queryOptions);
            return persistenceService.selectList(descriptor);
        } catch (final SBonitaReadException bre) {
            throw new SProfileMemberNotFoundException(bre);
        }
    }

    @Override
    public List<SProfileMember> searchProfileMembers(final String querySuffix, final QueryOptions queryOptions) throws SBonitaReadException {
        logBeforeMethod("searchProfileMembers");
        final List<SProfileMember> listSProfileMembers = persistenceService.searchEntity(SProfileMember.class, querySuffix, queryOptions, null);
        logAfterMethod("searchProfileMembers");
        return listSProfileMembers;
    }

    @Override
    public long getNumberOfProfileMembers(final String querySuffix, final QueryOptions countOptions) throws SBonitaReadException {
        logBeforeMethod("getNumberOfProfileMembers");
        final long number = persistenceService.getNumberOfEntities(SProfileMember.class, querySuffix, countOptions, null);
        logAfterMethod("getNumberOfProfileMembers");
        return number;
    }

    @Override
    public List<SProfileMember> getProfileMembers(final List<Long> profileIds) throws SBonitaReadException {
        logBeforeMethod("getProfileMembers");
        if (profileIds == null || profileIds.size() == 0) {
            return Collections.emptyList();
        }
        try {
            final QueryOptions queryOptions = new QueryOptions(0, QueryOptions.UNLIMITED_NUMBER_OF_RESULTS, SProfileMember.class, "id", OrderByType.ASC);
            final Map<String, Object> emptyMap = Collections.singletonMap("profileIds", (Object) profileIds);
            final List<SProfileMember> results = persistenceService.selectList(new SelectListDescriptor<SProfileMember>("getProfileMembersFromProfileIds",
                    emptyMap, SProfileMember.class, queryOptions));
            logAfterMethod("getProfileMembers");
            return results;
        } catch (final SBonitaReadException e) {
            logOnExceptionMethod("getProfileMembers", e);
            throw new SBonitaReadException(e);
        }
    }

    private void log(final long objectId, final int sQueriableLogStatus, final SPersistenceLogBuilder logBuilder, final String callerMethodName) {
        logBuilder.actionScope(String.valueOf(objectId));
        logBuilder.actionStatus(sQueriableLogStatus);
        logBuilder.objectId(objectId);
        final SQueriableLog log = logBuilder.done();
        if (queriableLoggerService.isLoggable(log.getActionType(), log.getSeverity())) {
            queriableLoggerService.log(this.getClass().getName(), callerMethodName, log);
        }
    }

    @Override
    public void deleteAllProfileMembers() throws SProfileMemberDeletionException {
        try {
            final DeleteAllRecord record = new DeleteAllRecord(SProfileMember.class, null);
            recorder.recordDeleteAll(record);
        } catch (final SRecorderException e) {
            throw new SProfileMemberDeletionException("Can't delete all profile members.", e);
        }
    }

    @Override
    public long getNumberOfProfiles(final QueryOptions queryOptions) throws SBonitaReadException {
        final Map<String, Object> parameters = Collections.emptyMap();
        return persistenceService.getNumberOfEntities(SProfile.class, queryOptions, parameters);
    }

    @Override
    public List<SProfile> searchProfiles(final QueryOptions queryOptions) throws SBonitaReadException {
        final Map<String, Object> parameters = Collections.emptyMap();
        return persistenceService.searchEntity(SProfile.class, queryOptions, parameters);
    }

    public List<SProfileMember> getProfileMembers(final int fromIndex, final int numberOfElements, final String field, final OrderByType order)
            throws SBonitaReadException {
        final SelectListDescriptor<SProfileMember> descriptor = SelectDescriptorBuilder.getProfileMembers(fromIndex, numberOfElements, field, order);
        return persistenceService.selectList(descriptor);
    }

    @Override
    public long getNumberOfProfileEntries(final QueryOptions queryOptions) throws SBonitaReadException {
        final Map<String, Object> parameters = Collections.emptyMap();
        return persistenceService.getNumberOfEntities(SProfileEntry.class, queryOptions, parameters);
    }

    @Override
    public List<SProfileEntry> searchProfileEntries(final QueryOptions queryOptions) throws SBonitaReadException {
        final Map<String, Object> parameters = Collections.emptyMap();
        return persistenceService.searchEntity(SProfileEntry.class, queryOptions, parameters);
    }

    private void logBeforeMethod(final String methodName) {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), methodName));
        }
    }

    private void logAfterMethod(final String methodName) {
        final Class<? extends ProfileServiceImpl> thisClass = this.getClass();
        if (logger.isLoggable(thisClass, TechnicalLogSeverity.TRACE)) {
            logger.log(thisClass, TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(thisClass, methodName));
        }
    }

    private void logOnExceptionMethod(final String methodName, final SBonitaException e) {
        final Class<? extends ProfileServiceImpl> thisClass = this.getClass();
        if (logger.isLoggable(thisClass, TechnicalLogSeverity.TRACE)) {
            logger.log(thisClass, TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(thisClass, methodName, e));
        }
    }

    private SProfileLogBuilderImpl getSProfileLog(final ActionType actionType, final String message) {
        final SProfileLogBuilderImpl logBuilder = new SProfileLogBuilderImpl();
        this.initializeLogBuilder(logBuilder, message);
        this.updateLog(actionType, logBuilder);
        return logBuilder;
    }

    private SProfileMemberLogBuilderImpl getProfileMemberLog(final ActionType actionType, final String message) {
        final SProfileMemberLogBuilderImpl logBuilder = new SProfileMemberLogBuilderImpl();
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
    public void updateProfileMetaData(final long profileId) throws SProfileUpdateException {
        long userId;
        try {
            userId = getSessionUserId();
            final Map<String, Object> params = new HashMap<String, Object>();
            params.put("lastUpdateDate", System.currentTimeMillis());
            params.put("lastUpdatedBy", userId);
            params.put("id", profileId);
            persistenceService.update(QUERY_UPDATE_LASTUPDATE_PROFILE, params);
        } catch (final SBonitaException e) {
            throw new SProfileUpdateException(e);
        }

    }

    private long getSessionUserId() throws SSessionNotFoundException, SessionIdNotSetException {
        return sessionService.getLoggedUserFromSession(sessionAccessor);
    }
}
