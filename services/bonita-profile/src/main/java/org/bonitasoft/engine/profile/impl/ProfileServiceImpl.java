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

import static org.bonitasoft.engine.persistence.QueryOptions.ALL_RESULTS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.commons.NullCheckingUtil;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.profile.ProfileService;
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
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;

/**
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 */
public class ProfileServiceImpl implements ProfileService {

    private final PersistenceService persistenceService;

    private final Recorder recorder;

    private final QueriableLoggerService queriableLoggerService;

    private final SessionService sessionService;

    private final ReadSessionAccessor sessionAccessor;

    public ProfileServiceImpl(PersistenceService persistenceService, Recorder recorder, QueriableLoggerService queriableLoggerService,
                              ReadSessionAccessor sessionAccessor, SessionService sessionService) {
        super();
        this.persistenceService = persistenceService;
        this.recorder = recorder;
        this.queriableLoggerService = queriableLoggerService;
        this.sessionAccessor = sessionAccessor;
        this.sessionService = sessionService;
    }

    @Override
    public SProfile createProfile(final SProfile profile) throws SProfileCreationException {
        final SProfileLogBuilderImpl logBuilder = getSProfileLog(ActionType.CREATED, "Adding a new profile");
        try {
            recorder.recordInsert(new InsertRecord(profile), PROFILE);
            log(profile.getId(), SQueriableLog.STATUS_OK, logBuilder, "createProfile");
            return profile;
        } catch (final SRecorderException re) {
            log(profile.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "createProfile");
            throw new SProfileCreationException(re);
        }
    }

    @Override
    public SProfile getProfile(final long profileId) throws SProfileNotFoundException {
        try {
            final SelectByIdDescriptor<SProfile> descriptor = SelectDescriptorBuilder.getElementById(SProfile.class, profileId);
            final SProfile profile = persistenceService.selectById(descriptor);
            if (profile == null) {
                throw new SProfileNotFoundException("No profile exists with id: " + profileId);
            }
            return profile;
        } catch (final SBonitaReadException e) {
            throw new SProfileNotFoundException(e);
        }
    }

    @Override
    public SProfile getProfileByName(final String profileName) throws SProfileNotFoundException, SBonitaReadException {
        final SelectOneDescriptor<SProfile> descriptor = SelectDescriptorBuilder.getElementByNameDescriptor(SProfile.class, "Profile", profileName);
        final SProfile profile = persistenceService.selectOne(descriptor);
        if (profile == null) {
            throw new SProfileNotFoundException("No profile exists with name: " + profileName);
        }
        return profile;
    }

    @Override
    public List<SProfile> getProfiles(final List<Long> profileIds) throws SProfileNotFoundException {
        final List<SProfile> profiles = new ArrayList<>();
        if (profileIds != null) {
            for (final Long profileId : profileIds) {
                final SProfile profile = getProfile(profileId);
                profiles.add(profile);
            }
        }
        return profiles;
    }

    @Override
    public SProfile updateProfile(final SProfile sProfile, final EntityUpdateDescriptor descriptor) throws SProfileUpdateException {
        NullCheckingUtil.checkArgsNotNull(sProfile);
        final SProfileLogBuilderImpl logBuilder = getSProfileLog(ActionType.UPDATED, "Updating profile");
        try {
            recorder.recordUpdate(UpdateRecord.buildSetFields(sProfile, descriptor), PROFILE);
            log(sProfile.getId(), SQueriableLog.STATUS_OK, logBuilder, "updateProfile");
        } catch (final SRecorderException re) {
            log(sProfile.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "updateProfile");
            throw new SProfileUpdateException(re);
        }
        return sProfile;
    }

    @Override
    public void deleteProfile(final SProfile profile) throws SProfileDeletionException, SProfileEntryDeletionException, SProfileMemberDeletionException {
        NullCheckingUtil.checkArgsNotNull(profile);
        final SProfileLogBuilderImpl logBuilder = getSProfileLog(ActionType.DELETED, "Deleting profile");
        try {
            deleteAllProfileEntriesOfProfile(profile);
            deleteAllProfileMembersOfProfile(profile);
            recorder.recordDelete(new DeleteRecord(profile), PROFILE);
            log(profile.getId(), SQueriableLog.STATUS_OK, logBuilder, "deleteProfile");
        } catch (final SRecorderException re) {
            log(profile.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "deleteProfile");
            throw new SProfileDeletionException(re);
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
            List<SProfileEntry> entries = getEntriesOfProfile(profile);
            for (final SProfileEntry entry : entries) {
                    deleteProfileEntry(entry);
                }
        } catch (final SBonitaReadException e) {
            throw new SProfileEntryDeletionException(e);
        }
    }

    @Override
    public void deleteProfile(final long profileId) throws SProfileNotFoundException, SProfileDeletionException, SProfileEntryDeletionException,
            SProfileMemberDeletionException {
        final SProfile profile = getProfile(profileId);
        deleteProfile(profile);
    }

    @Override
    public SProfileEntry getProfileEntry(final long profileEntryId) throws SProfileEntryNotFoundException {
        try {
            final SelectByIdDescriptor<SProfileEntry> descriptor = SelectDescriptorBuilder.getElementById(SProfileEntry.class, profileEntryId);
            final SProfileEntry profileEntry = persistenceService.selectById(descriptor);
            if (profileEntry == null) {
                throw new SProfileEntryNotFoundException("No entry exists with id: " + profileEntryId);
            }
            return profileEntry;
        } catch (final SBonitaReadException bre) {
            throw new SProfileEntryNotFoundException(bre);
        }
    }

    @Override
    public List<SProfileEntry> getEntriesOfProfile(String profileName) throws SBonitaReadException, SProfileNotFoundException {
        SProfile profile = getProfileByName(profileName);
        return getEntriesOfProfile(profile);
    }

    private List<SProfileEntry> getEntriesOfProfile(SProfile profile) throws SBonitaReadException {
        SelectListDescriptor<SProfileEntry> selectDescriptor = new SelectListDescriptor<>("getEntriesOfProfile",
                Collections.singletonMap("profileId", profile.getId()), SProfileEntry.class, ALL_RESULTS);
        return persistenceService.selectList(selectDescriptor);
    }

    @Override
    public SProfileEntry createProfileEntry(final SProfileEntry profileEntry) throws SProfileEntryCreationException {
        final SProfileLogBuilderImpl logBuilder = getSProfileLog(ActionType.CREATED, "Adding a new pofile entry");
        try {
            recorder.recordInsert(new InsertRecord(profileEntry), ENTRY_PROFILE);
            log(profileEntry.getId(), SQueriableLog.STATUS_OK, logBuilder, "createProfileEntry");
            return profileEntry;
        } catch (final SRecorderException re) {
            log(profileEntry.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "createProfileEntry");
            throw new SProfileEntryCreationException(re);
        }
    }

    @Override
    public SProfileEntry updateProfileEntry(final SProfileEntry profileEntry, final EntityUpdateDescriptor descriptor) throws SProfileEntryUpdateException {
        NullCheckingUtil.checkArgsNotNull(profileEntry);
        final SProfileLogBuilderImpl logBuilder = getSProfileLog(ActionType.UPDATED, "Updating profile entry");
        try {
            recorder.recordUpdate(UpdateRecord.buildSetFields(profileEntry, descriptor), ENTRY_PROFILE);
            log(profileEntry.getId(), SQueriableLog.STATUS_OK, logBuilder, "updateProfileEntry");
        } catch (final SRecorderException re) {
            log(profileEntry.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "updateProfileEntry");
            throw new SProfileEntryUpdateException(re);
        }
        return profileEntry;
    }

    @Override
    public void deleteProfileEntry(final SProfileEntry profileEntry) throws SProfileEntryDeletionException {
        final SProfileLogBuilderImpl logBuilder = getSProfileLog(ActionType.DELETED, "Deleting profile entry");
        try {
            recorder.recordDelete(new DeleteRecord(profileEntry), ENTRY_PROFILE);
            log(profileEntry.getId(), SQueriableLog.STATUS_OK, logBuilder, "deleteProfileEntry");
        } catch (final SRecorderException re) {
            log(profileEntry.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "deleteProfileEntry");
            throw new SProfileEntryDeletionException(re);
        }
    }

    @Override
    public void deleteProfileEntry(final long profileEntryId) throws SProfileEntryNotFoundException, SProfileEntryDeletionException {
        final SProfileEntry profileEntry = getProfileEntry(profileEntryId);
        deleteProfileEntry(profileEntry);
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
        final SProfileMemberImpl profileMember = buildProfileMember(profileId, firstName, lastName, userName);
        profileMember.setUserId(userId);
        createProfileMember(profileMember);
        return profileMember;
    }

    private void createProfileMember(final SProfileMemberImpl profileMember) throws SProfileMemberCreationException {
        final String message = "Adding a new profile member";
        final SProfileMemberLogBuilderImpl logBuilder = getProfileMemberLog(ActionType.CREATED, message);
        try {
            recorder.recordInsert(new InsertRecord(profileMember), PROFILE_MEMBER);
            log(profileMember.getId(), SQueriableLog.STATUS_OK, logBuilder, "insertProfileMember");
        } catch (final SRecorderException re) {
            log(profileMember.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "insertProfileMember");
            throw new SProfileMemberCreationException(re);
        }
    }

    @Override
    public SProfileMember addGroupToProfile(final long profileId, final long groupId, final String groupName, final String parentPath)
            throws SProfileMemberCreationException {
        final SProfileMemberImpl profileMember = buildProfileMember(profileId, groupName, parentPath, null);
        profileMember.setGroupId(groupId);
        createProfileMember(profileMember);
        return profileMember;
    }

    @Override
    public SProfileMember addRoleToProfile(final long profileId, final long roleId, final String roleName) throws SProfileMemberCreationException {
        final SProfileMemberImpl profileMember = buildProfileMember(profileId, roleName, null, null);
        profileMember.setRoleId(roleId);
        createProfileMember(profileMember);
        return profileMember;
    }

    @Override
    public SProfileMember addRoleAndGroupToProfile(final long profileId, final long roleId, final long groupId, final String roleName, final String groupName,
            final String groupParentPath) throws SProfileMemberCreationException {
        final SProfileMemberImpl profileMember = buildProfileMember(profileId, roleName, groupName, groupParentPath);
        profileMember.setGroupId(groupId);
        profileMember.setRoleId(roleId);
        createProfileMember(profileMember);
        return profileMember;
    }

    @Override
    public void deleteProfileMember(final long profileMemberId) throws SProfileMemberDeletionException, SProfileMemberNotFoundException {
        final SProfileMember profileMember = getProfileMemberWithoutDisplayName(profileMemberId);
        deleteProfileMember(profileMember);
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
        final String message = "Deleting profile member for userId " + profileMember.getUserId() + " with roleId " + profileMember.getRoleId() + " in groupId "
                + profileMember.getGroupId();
        final SProfileMemberLogBuilderImpl logBuilder = getProfileMemberLog(ActionType.DELETED, message);
        try {
            recorder.recordDelete(new DeleteRecord(profileMember), PROFILE_MEMBER);
            log(profileMember.getId(), SQueriableLog.STATUS_OK, logBuilder, "deleteProfileMember");
        } catch (final SRecorderException re) {
            log(profileMember.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "deleteProfileMember");
            throw new SProfileMemberDeletionException(re);
        }
    }

    @Override
    public List<SProfileMember> getProfileMembersOfUser(final long userId, final int fromIndex, final int numberOfElements, final String field,
                                                        final OrderByType order) throws SBonitaReadException {
        final SelectListDescriptor<SProfileMember> descriptor = SelectDescriptorBuilder.getDirectProfileMembersOfUser(userId, field, order, fromIndex,
                numberOfElements);
        return persistenceService.selectList(descriptor);
    }

    @Override
    public List<SProfileMember> getProfileMembersOfGroup(final long groupId, final int fromIndex, final int numberOfElements, final String field,
                                                         final OrderByType order) throws SBonitaReadException {
        final SelectListDescriptor<SProfileMember> descriptor = SelectDescriptorBuilder.getDirectProfileMembersOfGroup(groupId, field, order, fromIndex,
                numberOfElements);
        return persistenceService.selectList(descriptor);
    }

    @Override
    public List<SProfileMember> getProfileMembersOfRole(final long roleId, final int fromIndex, final int numberOfElements, final String field,
                                                        final OrderByType order) throws SBonitaReadException {
        final SelectListDescriptor<SProfileMember> descriptor = SelectDescriptorBuilder.getDirectProfileMembersOfRole(roleId, field, order, fromIndex,
                numberOfElements);
        return persistenceService.selectList(descriptor);
    }

    @Override
    public List<SProfile> searchProfilesOfUser(final long userId, final int fromIndex, final int numberOfElements, final String field, final OrderByType order)
            throws SBonitaReadException {
        final SelectListDescriptor<SProfile> descriptor = SelectDescriptorBuilder.getProfilesOfUser(userId, fromIndex, numberOfElements, field, order);
        return persistenceService.selectList(descriptor);
    }

    @Override
    public List<SProfile> getProfilesOfUser(long userId) throws SBonitaReadException {
        return searchProfilesOfUser(userId, 0, Integer.MAX_VALUE, null, null);
    }

    @Override
    public List<SProfile> searchProfilesWithNavigationOfUser(final long userId, final int fromIndex, final int numberOfElements, final String field, final OrderByType order)
            throws SBonitaReadException {
        return persistenceService.selectList(SelectDescriptorBuilder.getProfilesWithNavigationOfUser(userId, fromIndex, numberOfElements, field, order));
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
        return persistenceService.searchEntity(SProfileMember.class, querySuffix, queryOptions, null);
    }

    @Override
    public long getNumberOfProfileMembers(final String querySuffix, final QueryOptions countOptions) throws SBonitaReadException {
        return persistenceService.getNumberOfEntities(SProfileMember.class, querySuffix, countOptions, null);
    }

    @Override
    public List<SProfileMember> getProfileMembers(final List<Long> profileIds) throws SBonitaReadException {
        if (profileIds == null || profileIds.size() == 0) {
            return Collections.emptyList();
        }
        final QueryOptions queryOptions = new QueryOptions(0, QueryOptions.UNLIMITED_NUMBER_OF_RESULTS, SProfileMember.class, "id", OrderByType.ASC);
        final Map<String, Object> emptyMap = Collections.singletonMap("profileIds", profileIds);
        return persistenceService.selectList(new SelectListDescriptor<>("getProfileMembersFromProfileIds",
                emptyMap, SProfileMember.class, queryOptions));
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
            final Map<String, Object> params = new HashMap<>();
            params.put("lastUpdateDate", System.currentTimeMillis());
            params.put("lastUpdatedBy", userId);
            params.put("id", profileId);
            persistenceService.update("updateLastUpdateProfile", params);
        } catch (final SBonitaException e) {
            throw new SProfileUpdateException(e);
        }

    }

    private long getSessionUserId() {
        return sessionService.getLoggedUserFromSession(sessionAccessor);
    }
}
