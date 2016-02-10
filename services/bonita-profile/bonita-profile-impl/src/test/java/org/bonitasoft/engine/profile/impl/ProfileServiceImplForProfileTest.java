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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.events.EventActionType;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.profile.builder.SProfileUpdateBuilder;
import org.bonitasoft.engine.profile.builder.SProfileUpdateBuilderFactory;
import org.bonitasoft.engine.profile.exception.profile.SProfileCreationException;
import org.bonitasoft.engine.profile.exception.profile.SProfileDeletionException;
import org.bonitasoft.engine.profile.exception.profile.SProfileNotFoundException;
import org.bonitasoft.engine.profile.exception.profile.SProfileUpdateException;
import org.bonitasoft.engine.profile.exception.profileentry.SProfileEntryDeletionException;
import org.bonitasoft.engine.profile.exception.profilemember.SProfileMemberDeletionException;
import org.bonitasoft.engine.profile.model.SProfile;
import org.bonitasoft.engine.profile.model.SProfileEntry;
import org.bonitasoft.engine.profile.model.SProfileMember;
import org.bonitasoft.engine.profile.persistence.SelectDescriptorBuilder;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Celine Souchet
 */
@RunWith(MockitoJUnitRunner.class)
public class ProfileServiceImplForProfileTest {

    @Mock
    private EventService eventService;

    @Mock
    private TechnicalLoggerService logger;

    @Mock
    private PersistenceService persistenceService;

    @Mock
    private QueriableLoggerService queriableLoggerService;

    @Mock
    private Recorder recorder;

    @Mock
    private SessionService sessionService;

    @Mock
    private ReadSessionAccessor readSessionAccessor;

    @Mock
    private SSession sSession;

    @Mock
    private SProfile sProfile;

    @InjectMocks
    private ProfileServiceImpl profileServiceImpl;

    @Before
    public void before() throws Exception {
        doReturn(sSession).when(sessionService).getSession(anyLong());

        doReturn(1l).when(readSessionAccessor).getSessionId();
    }

    /**
     * Test method for {@link org.bonitasoft.engine.profile.impl.ProfileServiceImpl#getNumberOfProfiles(org.bonitasoft.engine.persistence.QueryOptions)}.
     */
    @Test
    public void getNumberOfProfilesWithOptions() throws Exception {
        final QueryOptions options = new QueryOptions(0, 10);
        when(persistenceService.getNumberOfEntities(SProfile.class, options, Collections.<String, Object> emptyMap())).thenReturn(1L);

        assertEquals(1L, profileServiceImpl.getNumberOfProfiles(options));
    }

    @Test(expected = SBonitaReadException.class)
    public void getNumberOfProfilesWithOptionsThrowException() throws Exception {
        final QueryOptions options = new QueryOptions(0, 10);
        when(persistenceService.getNumberOfEntities(SProfile.class, options, Collections.<String, Object> emptyMap())).thenThrow(new SBonitaReadException(""));

        profileServiceImpl.getNumberOfProfiles(options);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.profile.impl.ProfileServiceImpl#searchProfiles(org.bonitasoft.engine.persistence.QueryOptions)}.
     */
    @Test
    public void searchProfilesWithOptions() throws Exception {
        final QueryOptions options = new QueryOptions(0, 10);
        when(persistenceService.searchEntity(SProfile.class, options, Collections.<String, Object> emptyMap())).thenReturn(new ArrayList<SProfile>());

        assertNotNull(profileServiceImpl.searchProfiles(options));
    }

    @Test(expected = SBonitaReadException.class)
    public void searchProfilesWithOptionsThrowException() throws Exception {
        final QueryOptions options = new QueryOptions(0, 10);
        when(persistenceService.searchEntity(SProfile.class, options, Collections.<String, Object> emptyMap())).thenThrow(new SBonitaReadException(""));

        profileServiceImpl.searchProfiles(options);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.profile.impl.ProfileServiceImpl#getProfile(long)}.
     */
    @Test
    public void getProfileById() throws Exception {
        final SProfile sProfile = mock(SProfile.class);

        doReturn(sProfile).when(persistenceService).selectById(Matchers.<SelectByIdDescriptor<SProfile>> any());

        assertEquals(sProfile, profileServiceImpl.getProfile(1));
    }

    @Test(expected = SProfileNotFoundException.class)
    public void getNoProfileById() throws Exception {
        when(persistenceService.selectById(Matchers.<SelectByIdDescriptor<SProfile>> any())).thenReturn(null);

        profileServiceImpl.getProfile(1);
    }

    @Test(expected = SProfileNotFoundException.class)
    public void getProfileByIdThrowException() throws Exception {
        when(persistenceService.selectById(Matchers.<SelectByIdDescriptor<SProfile>> any())).thenThrow(new SBonitaReadException(""));

        profileServiceImpl.getProfile(1);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.profile.impl.ProfileServiceImpl#getProfileByName(java.lang.String)}.
     */
    @Test
    public void getProfileByName() throws Exception {
        final String profileName = "plop";
        final SProfile sProfile = mock(SProfile.class);

        doReturn(sProfile).when(persistenceService).selectOne(Matchers.<SelectOneDescriptor<SProfile>> any());

        assertEquals(sProfile, profileServiceImpl.getProfileByName(profileName));
    }

    @Test(expected = SProfileNotFoundException.class)
    public void getNoProfileByName() throws Exception {
        when(persistenceService.selectOne(Matchers.<SelectOneDescriptor<SProfile>> any())).thenReturn(null);

        profileServiceImpl.getProfileByName("plop");
    }

    @Test(expected = SProfileNotFoundException.class)
    public void getProfileByNameThrowException() throws Exception {
        when(persistenceService.selectOne(Matchers.<SelectOneDescriptor<SProfile>> any())).thenThrow(new SBonitaReadException(""));

        profileServiceImpl.getProfileByName("plop");
    }

    /**
     * Test method for {@link org.bonitasoft.engine.profile.impl.ProfileServiceImpl#getProfiles(java.util.List)}.
     *
     * @throws SProfileNotFoundException
     * @throws SBonitaReadException
     */
    @Test
    public final void getProfiles() throws SProfileNotFoundException, SBonitaReadException {
        final List<Long> profileIds = new ArrayList<Long>();
        profileIds.add(1L);
        profileIds.add(2L);

        final List<SProfile> sProfiles = new ArrayList<SProfile>();
        final SProfile sProfile = mock(SProfile.class);
        sProfiles.add(sProfile);
        sProfiles.add(sProfile);

        doReturn(sProfile).when(persistenceService).selectById(Matchers.<SelectByIdDescriptor<SProfile>> any());

        assertEquals(sProfiles, profileServiceImpl.getProfiles(profileIds));
    }

    @Test(expected = SProfileNotFoundException.class)
    public final void getNoProfiles() throws SProfileNotFoundException, SBonitaReadException {
        final List<Long> profileIds = new ArrayList<Long>();
        profileIds.add(1L);

        doReturn(null).when(persistenceService).selectById(Matchers.<SelectByIdDescriptor<SProfile>> any());

        profileServiceImpl.getProfiles(profileIds);
    }

    @Test
    public final void getProfilesWithEmptyList() throws SProfileNotFoundException {
        final List<Long> profileIds = new ArrayList<Long>();

        assertTrue(profileServiceImpl.getProfiles(profileIds).isEmpty());
    }

    @Test
    public final void getProfilesWithNullList() throws SProfileNotFoundException {
        assertTrue(profileServiceImpl.getProfiles(null).isEmpty());
    }

    /**
     * Test method for {@link org.bonitasoft.engine.profile.impl.ProfileServiceImpl#getProfilesOfUser(long)}.
     *
     * @throws SBonitaReadException
     * @throws SProfileReadException
     */
    @Test
    public final void getProfilesOfUser() throws SBonitaReadException {
        final List<SProfile> sProfiles = new ArrayList<SProfile>();
        final SProfile sProfile = mock(SProfile.class);
        sProfiles.add(sProfile);

        doReturn(sProfiles).when(persistenceService).selectList(Matchers.<SelectListDescriptor<SProfile>> any());

        assertEquals(sProfiles, profileServiceImpl.searchProfilesOfUser(1, 0, 10, "name", OrderByType.ASC));
    }

    @Test
    public final void getNoProfilesOfUser() throws SBonitaReadException {
        final List<SProfile> sProfiles = new ArrayList<SProfile>();

        doReturn(sProfiles).when(persistenceService).selectList(Matchers.<SelectListDescriptor<SProfile>> any());

        assertEquals(sProfiles, profileServiceImpl.searchProfilesOfUser(1, 0, 10, "name", OrderByType.ASC));
    }

    @Test(expected = SBonitaReadException.class)
    public final void getProfilesOfUserThrowException() throws SBonitaReadException {
        doThrow(new SBonitaReadException("plop")).when(persistenceService).selectList(Matchers.<SelectListDescriptor<SProfile>> any());

        profileServiceImpl.searchProfilesOfUser(1, 0, 10, "name", OrderByType.ASC);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.profile.impl.ProfileServiceImpl#createProfile(org.bonitasoft.engine.profile.model.SProfile)}.
     *
     * @throws SProfileCreationException
     * @throws SRecorderException
     */
    @Test
    public final void createProfile() throws SProfileCreationException, SRecorderException {
        final SProfile sProfile = mock(SProfile.class);
        doReturn(1L).when(sProfile).getId();

        doReturn(false).when(eventService).hasHandlers(anyString(), any(EventActionType.class));
        doNothing().when(recorder).recordInsert(any(InsertRecord.class), any(SInsertEvent.class));
        doReturn(false).when(queriableLoggerService).isLoggable(anyString(), any(SQueriableLogSeverity.class));

        final SProfile result = profileServiceImpl.createProfile(sProfile);
        assertNotNull(result);
        assertEquals(sProfile, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public final void createNullProfile() throws Exception {
        profileServiceImpl.createProfile(null);
    }

    @Test(expected = SProfileCreationException.class)
    public final void createProfileThrowException() throws SRecorderException, SProfileCreationException {
        final SProfile sProfile = mock(SProfile.class);
        doReturn(1L).when(sProfile).getId();

        doReturn(false).when(eventService).hasHandlers(anyString(), any(EventActionType.class));
        doThrow(new SRecorderException("plop")).when(recorder).recordInsert(any(InsertRecord.class), any(SInsertEvent.class));
        doReturn(false).when(queriableLoggerService).isLoggable(anyString(), any(SQueriableLogSeverity.class));

        profileServiceImpl.createProfile(sProfile);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.profile.impl.ProfileServiceImpl#updateProfile(org.bonitasoft.engine.profile.model.SProfile, org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor)}
     *
     * @throws SBonitaReadException
     * @throws SRecorderException
     * @throws SProfileUpdateException
     */
    @Test
    public final void updateProfile() throws SProfileUpdateException {
        final SProfile sProfile = mock(SProfile.class);
        doReturn(3L).when(sProfile).getId();
        final SProfileUpdateBuilder sProfileUpdateBuilder = BuilderFactory.get(SProfileUpdateBuilderFactory.class).createNewInstance();
        sProfileUpdateBuilder.setDescription("newDescription").setName("newName");

        doReturn(false).when(eventService).hasHandlers(anyString(), any(EventActionType.class));
        doReturn(false).when(queriableLoggerService).isLoggable(anyString(), any(SQueriableLogSeverity.class));

        final SProfile result = profileServiceImpl.updateProfile(sProfile, sProfileUpdateBuilder.done());
        assertNotNull(result);
        assertEquals(sProfile, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public final void updateNullProfile() throws SProfileUpdateException {
        final SProfileUpdateBuilder sProfileUpdateBuilder = BuilderFactory.get(SProfileUpdateBuilderFactory.class).createNewInstance();

        profileServiceImpl.updateProfile(null, sProfileUpdateBuilder.done());
    }

    @Test(expected = IllegalArgumentException.class)
    public final void updateProfileWithNullDescriptor() throws SProfileUpdateException {
        final SProfile sProfile = mock(SProfile.class);
        doReturn(3L).when(sProfile).getId();

        profileServiceImpl.updateProfile(sProfile, null);
    }

    @Test(expected = SProfileUpdateException.class)
    public final void updateProfileThrowException() throws SRecorderException, SProfileUpdateException {
        final SProfile sProfile = mock(SProfile.class);
        doReturn(3L).when(sProfile).getId();
        final SProfileUpdateBuilder sProfileUpdateBuilder = BuilderFactory.get(SProfileUpdateBuilderFactory.class).createNewInstance();
        sProfileUpdateBuilder.setDescription("newDescription").setName("newName");

        doThrow(new SRecorderException("plop")).when(recorder).recordUpdate(any(UpdateRecord.class), any(SUpdateEvent.class));

        profileServiceImpl.updateProfile(sProfile, sProfileUpdateBuilder.done());
    }

    /**
     * Test method for {@link org.bonitasoft.engine.profile.impl.ProfileServiceImpl#deleteProfile(long)}.
     *
     * @throws SProfileDeletionException
     * @throws SProfileNotFoundException
     * @throws SRecorderException
     * @throws SBonitaReadException
     */
    @Test
    public final void deleteProfileById() throws SProfileNotFoundException, SProfileDeletionException, SRecorderException, SBonitaReadException,
            SProfileEntryDeletionException, SProfileMemberDeletionException {
        final SProfile sProfile = mock(SProfile.class);
        doReturn(3L).when(sProfile).getId();

        final List<SProfileEntry> sProfileEntries = new ArrayList<SProfileEntry>();
        final SProfileEntry sProfileEntry = mock(SProfileEntry.class);
        doReturn(6L).when(sProfileEntry).getId();
        sProfileEntries.add(sProfileEntry);

        final List<SProfileMember> sProfileMembers = new ArrayList<SProfileMember>();
        final SProfileMember sProfileMember = mock(SProfileMember.class);
        doReturn(4L).when(sProfileMember).getId();
        sProfileMembers.add(sProfileMember);

        doReturn(sProfile).when(persistenceService).selectById(Matchers.<SelectByIdDescriptor<SProfile>> any());
        doReturn(sProfileEntries).doReturn(new ArrayList<SProfileEntry>()).when(persistenceService)
                .selectList(SelectDescriptorBuilder.getEntriesOfProfile(3, 0, 1000));
        final QueryOptions queryOptions = new QueryOptions(0, 10, SProfile.class, "id", OrderByType.ASC);
        doReturn(sProfileMembers).doReturn(new ArrayList<SProfileMember>()).when(persistenceService)
                .selectList(SelectDescriptorBuilder.getSProfileMembersWithoutDisplayName(3l, queryOptions));
        doReturn(false).when(eventService).hasHandlers(anyString(), any(EventActionType.class));
        doNothing().when(recorder).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));
        doReturn(false).when(queriableLoggerService).isLoggable(anyString(), any(SQueriableLogSeverity.class));

        profileServiceImpl.deleteProfile(1);
    }

    @Test(expected = SProfileNotFoundException.class)
    public final void deleteNoProfileById() throws SBonitaReadException, SProfileDeletionException, SProfileNotFoundException, SProfileEntryDeletionException,
            SProfileMemberDeletionException {
        when(persistenceService.selectById(Matchers.<SelectByIdDescriptor<SProfile>> any())).thenReturn(null);

        profileServiceImpl.deleteProfile(1);
    }

    @Test(expected = SProfileDeletionException.class)
    public void deleteProfileByIdThrowException() throws Exception {
        final SProfile sProfile = mock(SProfile.class);
        doReturn(3L).when(sProfile).getId();

        doReturn(sProfile).when(persistenceService).selectById(Matchers.<SelectByIdDescriptor<SProfile>> any());
        doReturn(new ArrayList<SProfileEntry>()).when(persistenceService).selectList(SelectDescriptorBuilder.getEntriesOfProfile(3, 0, 1000));
        final QueryOptions queryOptions = new QueryOptions(0, 10, SProfile.class, "id", OrderByType.ASC);
        doReturn(new ArrayList<SProfileMember>()).when(persistenceService).selectList(
                SelectDescriptorBuilder.getSProfileMembersWithoutDisplayName(3, queryOptions));
        doReturn(false).when(eventService).hasHandlers(anyString(), any(EventActionType.class));
        doThrow(new SRecorderException("")).when(recorder).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));

        profileServiceImpl.deleteProfile(1);
    }

    @Test
    public final void deleteProfileByIdWithNoEntry() throws SProfileNotFoundException, SProfileDeletionException, SRecorderException, SBonitaReadException,
            SProfileEntryDeletionException, SProfileMemberDeletionException {
        final List<SProfile> sProfiles = new ArrayList<SProfile>(3);
        final SProfile sProfile = mock(SProfile.class);
        doReturn(3L).when(sProfile).getId();
        sProfiles.add(sProfile);

        final List<SProfileMember> sProfileMembers = new ArrayList<SProfileMember>();
        final SProfileMember sProfileMember = mock(SProfileMember.class);
        doReturn(4L).when(sProfileMember).getId();
        sProfileMembers.add(sProfileMember);

        doReturn(sProfile).when(persistenceService).selectById(Matchers.<SelectByIdDescriptor<SProfile>> any());
        doReturn(new ArrayList<SProfileEntry>()).when(persistenceService).selectList(SelectDescriptorBuilder.getEntriesOfProfile(3, 0, 1000));
        final QueryOptions queryOptions = new QueryOptions(0, 10, SProfile.class, "id", OrderByType.ASC);
        doReturn(sProfileMembers).doReturn(new ArrayList<SProfileMember>()).when(persistenceService)
                .selectList(SelectDescriptorBuilder.getSProfileMembersWithoutDisplayName(3l, queryOptions));
        doReturn(false).when(eventService).hasHandlers(anyString(), any(EventActionType.class));
        doNothing().when(recorder).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));
        doReturn(false).when(queriableLoggerService).isLoggable(anyString(), any(SQueriableLogSeverity.class));

        profileServiceImpl.deleteProfile(1);
    }

    @Test
    public final void deleteProfileByIdWithNoMember() throws SProfileNotFoundException, SProfileDeletionException, SRecorderException, SBonitaReadException,
            SProfileEntryDeletionException, SProfileMemberDeletionException {
        final SProfile sProfile = mock(SProfile.class);
        doReturn(3L).when(sProfile).getId();

        final List<SProfileEntry> sProfileEntries = new ArrayList<SProfileEntry>();
        final SProfileEntry sProfileEntry = mock(SProfileEntry.class);
        doReturn(6L).when(sProfileEntry).getId();
        sProfileEntries.add(sProfileEntry);

        doReturn(sProfile).when(persistenceService).selectById(Matchers.<SelectByIdDescriptor<SProfile>> any());
        doReturn(sProfileEntries).doReturn(new ArrayList<SProfileEntry>()).when(persistenceService)
                .selectList(SelectDescriptorBuilder.getEntriesOfProfile(3, 0, 1000));
        final QueryOptions queryOptions = new QueryOptions(0, 10, SProfile.class, "id", OrderByType.ASC);
        doReturn(new ArrayList<SProfileMember>()).when(persistenceService).selectList(
                SelectDescriptorBuilder.getSProfileMembersWithoutDisplayName(3, queryOptions));
        doReturn(false).when(eventService).hasHandlers(anyString(), any(EventActionType.class));
        doNothing().when(recorder).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));
        doReturn(false).when(queriableLoggerService).isLoggable(anyString(), any(SQueriableLogSeverity.class));

        profileServiceImpl.deleteProfile(1);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.profile.impl.ProfileServiceImpl#deleteProfile(org.bonitasoft.engine.profile.model.SProfile)}.
     *
     * @throws SBonitaReadException
     * @throws SRecorderException
     * @throws SProfileDeletionException
     */
    @Test
    public final void deleteProfileByObject() throws Exception,
            SProfileMemberDeletionException {
        final SProfile sProfile = mock(SProfile.class);
        doReturn(3L).when(sProfile).getId();

        final List<SProfileEntry> sProfileEntries = new ArrayList<SProfileEntry>();
        final SProfileEntry sProfileEntry = mock(SProfileEntry.class);
        doReturn(6L).when(sProfileEntry).getId();
        sProfileEntries.add(sProfileEntry);

        final List<SProfileMember> sProfileMembers = new ArrayList<SProfileMember>();
        final SProfileMember sProfileMember = mock(SProfileMember.class);
        doReturn(4L).when(sProfileMember).getId();
        sProfileMembers.add(sProfileMember);

        doReturn(sProfileEntries).doReturn(new ArrayList<SProfileEntry>()).when(persistenceService)
                .selectList(SelectDescriptorBuilder.getEntriesOfProfile(3, 0, 1000));
        final QueryOptions queryOptions = new QueryOptions(0, 10, SProfile.class, "id", OrderByType.ASC);
        doReturn(sProfileMembers).doReturn(new ArrayList<SProfileMember>()).when(persistenceService)
                .selectList(SelectDescriptorBuilder.getSProfileMembersWithoutDisplayName(3, queryOptions));
        doReturn(false).when(eventService).hasHandlers(anyString(), any(EventActionType.class));
        doNothing().when(recorder).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));
        doReturn(false).when(queriableLoggerService).isLoggable(anyString(), any(SQueriableLogSeverity.class));

        profileServiceImpl.deleteProfile(sProfile);
    }

    @Test(expected = IllegalArgumentException.class)
    public final void deleteNoProfileByObject() throws Exception {
        profileServiceImpl.deleteProfile(null);
    }

    @Test(expected = SProfileDeletionException.class)
    public void deleteProfileByObjectThrowException() throws Exception {
        doReturn(3L).when(sProfile).getId();
        doReturn(sProfile).when(persistenceService).selectById(Matchers.<SelectByIdDescriptor<SProfile>> any());
        doReturn(new ArrayList<SProfileEntry>()).when(persistenceService).selectList(SelectDescriptorBuilder.getEntriesOfProfile(3, 0, 1000));
        final QueryOptions queryOptions = new QueryOptions(0, 10, SProfile.class, "id", OrderByType.ASC);
        doReturn(new ArrayList<SProfileMember>()).when(persistenceService).selectList(
                SelectDescriptorBuilder.getSProfileMembersWithoutDisplayName(3l, queryOptions));
        doReturn(false).when(eventService).hasHandlers(anyString(), any(EventActionType.class));
        doThrow(new SRecorderException("")).when(recorder).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));

        profileServiceImpl.deleteProfile(sProfile);
    }

    @Test
    public final void deleteProfileByObjectWithNoEntry() throws Exception,
            SProfileEntryDeletionException, SProfileMemberDeletionException {
        final List<SProfile> sProfiles = new ArrayList<SProfile>(3);
        final SProfile sProfile = mock(SProfile.class);
        doReturn(3L).when(sProfile).getId();
        sProfiles.add(sProfile);

        final List<SProfileMember> sProfileMembers = new ArrayList<SProfileMember>();
        final SProfileMember sProfileMember = mock(SProfileMember.class);
        doReturn(4L).when(sProfileMember).getId();
        sProfileMembers.add(sProfileMember);

        doReturn(sProfile).when(persistenceService).selectById(Matchers.<SelectByIdDescriptor<SProfile>> any());
        doReturn(new ArrayList<SProfileEntry>()).when(persistenceService).selectList(SelectDescriptorBuilder.getEntriesOfProfile(3, 0, 1000));
        final QueryOptions queryOptions = new QueryOptions(0, 10, SProfile.class, "id", OrderByType.ASC);
        doReturn(sProfileMembers).doReturn(new ArrayList<SProfileMember>()).when(persistenceService)
                .selectList(SelectDescriptorBuilder.getSProfileMembersWithoutDisplayName(3l, queryOptions));
        doReturn(false).when(eventService).hasHandlers(anyString(), any(EventActionType.class));
        doNothing().when(recorder).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));
        doReturn(false).when(queriableLoggerService).isLoggable(anyString(), any(SQueriableLogSeverity.class));

        profileServiceImpl.deleteProfile(sProfile);
    }

    @Test
    public final void deleteProfileByObjectWithNoMember() throws Exception,
            SProfileEntryDeletionException, SProfileMemberDeletionException {
        doReturn(3L).when(sProfile).getId();

        final List<SProfileEntry> sProfileEntries = new ArrayList<SProfileEntry>();
        final SProfileEntry sProfileEntry = mock(SProfileEntry.class);
        doReturn(6L).when(sProfileEntry).getId();
        sProfileEntries.add(sProfileEntry);

        doReturn(sProfileEntries).doReturn(new ArrayList<SProfileEntry>()).when(persistenceService)
                .selectList(SelectDescriptorBuilder.getEntriesOfProfile(3, 0, 1000));
        final QueryOptions queryOptions = new QueryOptions(0, 10, SProfile.class, "id", OrderByType.ASC);
        doReturn(new ArrayList<SProfileMember>()).when(persistenceService).selectList(
                SelectDescriptorBuilder.getSProfileMembersWithoutDisplayName(3l, queryOptions));
        doReturn(false).when(eventService).hasHandlers(anyString(), any(EventActionType.class));
        doNothing().when(recorder).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));
        doReturn(false).when(queriableLoggerService).isLoggable(anyString(), any(SQueriableLogSeverity.class));

        profileServiceImpl.deleteProfile(sProfile);
    }
}
