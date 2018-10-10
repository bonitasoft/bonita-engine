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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.profile.builder.SProfileEntryUpdateBuilder;
import org.bonitasoft.engine.profile.builder.impl.SProfileEntryUpdateBuilderImpl;
import org.bonitasoft.engine.profile.exception.profile.SProfileNotFoundException;
import org.bonitasoft.engine.profile.exception.profileentry.SProfileEntryCreationException;
import org.bonitasoft.engine.profile.exception.profileentry.SProfileEntryDeletionException;
import org.bonitasoft.engine.profile.exception.profileentry.SProfileEntryNotFoundException;
import org.bonitasoft.engine.profile.exception.profileentry.SProfileEntryUpdateException;
import org.bonitasoft.engine.profile.model.SProfile;
import org.bonitasoft.engine.profile.model.SProfileEntry;
import org.bonitasoft.engine.profile.model.impl.SProfileEntryImpl;
import org.bonitasoft.engine.profile.model.impl.SProfileImpl;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Celine Souchet
 */
@RunWith(MockitoJUnitRunner.class)
public class ProfileServiceImplForProfileEntryTest {

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

    @InjectMocks
    private ProfileServiceImpl profileServiceImpl;

    /**
     * Test method for {@link org.bonitasoft.engine.profile.impl.ProfileServiceImpl#createProfileEntry(org.bonitasoft.engine.profile.model.SProfileEntry)}.
     *
     * @throws SRecorderException
     * @throws SProfileEntryCreationException
     */
    @Test
    public final void createProfileEntry() throws SRecorderException, SProfileEntryCreationException {
        final SProfileEntry sProfileEntry = createProfileEntry(1L);

        doNothing().when(recorder).recordInsert(any(InsertRecord.class), nullable(String.class));

        final SProfileEntry result = profileServiceImpl.createProfileEntry(sProfileEntry);
        assertNotNull(result);
        assertEquals(sProfileEntry, result);
    }

    private SProfileEntry createProfileEntry(final long id) {
        final SProfileEntryImpl entry = new SProfileEntryImpl();
        entry.setId(id);
        return entry;
    }
    private SProfile profile(String name) {
        final SProfileImpl profile = new SProfileImpl();
        profile.setId(UUID.randomUUID().getLeastSignificantBits());
        profile.setName(name);
        return profile;
    }

    @Test(expected = IllegalArgumentException.class)
    public final void createNullProfileEntry() throws Exception {
        profileServiceImpl.createProfileEntry(null);
    }

    @Test(expected = SProfileEntryCreationException.class)
    public final void createProfileThrowException() throws SRecorderException, SProfileEntryCreationException {
        final SProfileEntry sProfileEntry = createProfileEntry(1);

        doThrow(new SRecorderException("plop")).when(recorder).recordInsert(any(InsertRecord.class), nullable(String.class));

        profileServiceImpl.createProfileEntry(sProfileEntry);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.profile.impl.ProfileServiceImpl#deleteProfileEntry(long)}.
     *
     * @throws SProfileEntryDeletionException
     * @throws SProfileEntryNotFoundException
     * @throws SBonitaReadException
     * @throws SRecorderException
     */
    @Test
    public final void deleteProfileEntryById() throws SProfileEntryNotFoundException, SProfileEntryDeletionException, SBonitaReadException, SRecorderException {
        final SProfileEntry sProfileEntry = createProfileEntry(6);

        doReturn(sProfileEntry).when(persistenceService).selectById(Matchers.<SelectByIdDescriptor<SProfileEntry>> any());

        profileServiceImpl.deleteProfileEntry(1);
    }

    @Test(expected = SProfileEntryNotFoundException.class)
    public final void deleteNoProfileEntryById() throws SBonitaReadException, SProfileEntryNotFoundException, SProfileEntryDeletionException {
        when(persistenceService.selectById(Matchers.<SelectByIdDescriptor<SProfileEntry>> any())).thenReturn(null);

        profileServiceImpl.deleteProfileEntry(1);
    }

    @Test(expected = SProfileEntryDeletionException.class)
    public void deleteProfileEntryByIdThrowException() throws Exception {
        final SProfileEntry sProfileEntry = createProfileEntry(6);

        doReturn(sProfileEntry).when(persistenceService).selectById(any());
        doThrow(new SRecorderException("")).when(recorder).recordDelete(any(DeleteRecord.class), nullable(String.class));

        profileServiceImpl.deleteProfileEntry(1);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.profile.impl.ProfileServiceImpl#deleteProfileEntry(org.bonitasoft.engine.profile.model.SProfileEntry)}.
     *
     * @throws SRecorderException
     * @throws SProfileEntryDeletionException
     */
    @Test
    public final void deleteProfileEntryByObject() throws SRecorderException, SProfileEntryDeletionException {
        final SProfileEntry sProfileEntry = createProfileEntry(6);

        profileServiceImpl.deleteProfileEntry(sProfileEntry);
    }

    @Test(expected = IllegalArgumentException.class)
    public final void deleteNoProfileEntryByObject() throws SProfileEntryDeletionException {
        profileServiceImpl.deleteProfileEntry(null);
    }

    @Test(expected = SProfileEntryDeletionException.class)
    public void deleteProfileEntryByObjectThrowException() throws Exception {
        final SProfileEntry sProfileEntry = createProfileEntry(6);

        doThrow(new SRecorderException("")).when(recorder).recordDelete(any(DeleteRecord.class), nullable(String.class));

        profileServiceImpl.deleteProfileEntry(sProfileEntry);
    }

    @Test
    public final void getEntriesOfProfile() throws Exception {
        final List<SProfileEntry> sProfileEntries = new ArrayList<>();
        final SProfileEntry sProfileEntry = createProfileEntry(1);
        sProfileEntries.add(sProfileEntry);

        doReturn(profile("myProfile")).when(persistenceService).selectOne(any());
        doReturn(sProfileEntries).when(persistenceService).selectList(any());

        assertEquals(sProfileEntries, profileServiceImpl.getEntriesOfProfile("myProfile"));
    }

    @Test
    public final void getNoEntriesOfProfile() throws Exception {
        final List<SProfileEntry> sProfileEntries = new ArrayList<>();

        doReturn(profile("myProfile")).when(persistenceService).selectOne(any());
        doReturn(sProfileEntries).when(persistenceService).selectList(any());

        assertEquals(sProfileEntries, profileServiceImpl.getEntriesOfProfile("myProfile"));
    }

    @Test(expected = SBonitaReadException.class)
    public final void getEntriesOfProfileThrowException() throws Exception {
        doThrow(new SBonitaReadException("plop")).when(persistenceService).selectList(any());

        doReturn(profile("myProfile")).when(persistenceService).selectOne(any());
        profileServiceImpl.getEntriesOfProfile("myProfile");
    }

    /**
     * Test method for {@link org.bonitasoft.engine.profile.impl.ProfileServiceImpl#getNumberOfProfileEntries(org.bonitasoft.engine.persistence.QueryOptions)}.
     *
     * @throws SBonitaReadException
     * @throws SBonitaReadException
     */
    @Test
    public final void getNumberOfProfileEntries() throws SBonitaReadException, SBonitaReadException {
        final QueryOptions options = new QueryOptions(0, 10);
        when(persistenceService.getNumberOfEntities(SProfileEntry.class, options, Collections.emptyMap())).thenReturn(1L);

        assertEquals(1L, profileServiceImpl.getNumberOfProfileEntries(options));
    }

    @Test(expected = SBonitaReadException.class)
    public void getNumberOfProfilesThrowException() throws Exception {
        final QueryOptions options = new QueryOptions(0, 10);
        when(persistenceService.getNumberOfEntities(SProfileEntry.class, options, Collections.emptyMap())).thenThrow(
                new SBonitaReadException(""));

        profileServiceImpl.getNumberOfProfileEntries(options);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.profile.impl.ProfileServiceImpl#getProfileEntry(long)}.
     *
     * @throws SProfileEntryNotFoundException
     * @throws SBonitaReadException
     */
    @Test
    public final void getProfileEntryById() throws SProfileEntryNotFoundException, SBonitaReadException {
        final SProfileEntry sProfileEntry = createProfileEntry(1);

        doReturn(sProfileEntry).when(persistenceService).selectById(Matchers.<SelectByIdDescriptor<SProfileEntry>> any());

        assertEquals(sProfileEntry, profileServiceImpl.getProfileEntry(1));
    }

    @Test(expected = SProfileEntryNotFoundException.class)
    public void getNoProfileEntryById() throws Exception {
        when(persistenceService.selectById(Matchers.<SelectByIdDescriptor<SProfileEntry>> any())).thenReturn(null);

        profileServiceImpl.getProfileEntry(1);
    }

    @Test(expected = SProfileEntryNotFoundException.class)
    public void getProfileEntryByIdThrowException() throws Exception {
        when(persistenceService.selectById(Matchers.<SelectByIdDescriptor<SProfileEntry>> any())).thenThrow(new SBonitaReadException(""));

        profileServiceImpl.getProfileEntry(1);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.profile.impl.ProfileServiceImpl#searchProfileEntries(org.bonitasoft.engine.persistence.QueryOptions)}.
     *
     * @throws SBonitaReadException
     * @throws SBonitaReadException
     */
    @Test
    public final void searchProfileEntries() throws SBonitaReadException {
        final QueryOptions options = new QueryOptions(0, 10);
        when(persistenceService.searchEntity(SProfileEntry.class, options, Collections.<String, Object> emptyMap())).thenReturn(new ArrayList<SProfileEntry>());

        assertNotNull(profileServiceImpl.searchProfileEntries(options));
    }

    @Test(expected = SBonitaReadException.class)
    public void searchProfileEntriesThrowException() throws Exception {
        final QueryOptions options = new QueryOptions(0, 10);
        when(persistenceService.searchEntity(SProfileEntry.class, options, Collections.<String, Object> emptyMap())).thenThrow(new SBonitaReadException(""));

        profileServiceImpl.searchProfileEntries(options);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.profile.impl.ProfileServiceImpl#updateProfileEntry(org.bonitasoft.engine.profile.model.SProfileEntry, org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor)}
     * .
     *
     * @throws SProfileEntryUpdateException
     */
    @Test
    public final void updateProfileEntry() throws SProfileEntryUpdateException {
        final SProfileEntry sProfileEntry = createProfileEntry(3);
        final SProfileEntryUpdateBuilder sProfileEntryUpdateBuilder = new SProfileEntryUpdateBuilderImpl();
        sProfileEntryUpdateBuilder.setDescription("description").setName("newName").setIndex(6).setCustom(true).setPage("page").setParentId(5858)
                .setProfileId(9)
                .setType("type");

        final SProfileEntry result = profileServiceImpl.updateProfileEntry(sProfileEntry, sProfileEntryUpdateBuilder.done());
        assertNotNull(result);
    }

    @Test(expected = IllegalArgumentException.class)
    public final void updateNullProfileEntry() throws SProfileEntryUpdateException {
        final SProfileEntryUpdateBuilder sProfileEntryUpdateBuilder = new SProfileEntryUpdateBuilderImpl();

        profileServiceImpl.updateProfileEntry(null, sProfileEntryUpdateBuilder.done());
    }

    @Test(expected = IllegalArgumentException.class)
    public final void updateProfileWithNullDescriptor() throws SProfileEntryUpdateException {
        final SProfileEntry sProfileEntry = createProfileEntry(3);

        profileServiceImpl.updateProfileEntry(sProfileEntry, null);
    }

    @Test(expected = SProfileEntryUpdateException.class)
    public final void updateProfileEntryThrowException() throws SRecorderException, SProfileEntryUpdateException {
        final SProfileEntry sProfileEntry = createProfileEntry(3);
        final SProfileEntryUpdateBuilder sProfileEntryUpdateBuilder = new SProfileEntryUpdateBuilderImpl();
        sProfileEntryUpdateBuilder.setDescription("newDescription").setName("newName");

        doThrow(new SRecorderException("plop")).when(recorder).recordUpdate(any(UpdateRecord.class), nullable(String.class));

        profileServiceImpl.updateProfileEntry(sProfileEntry, sProfileEntryUpdateBuilder.done());
    }

}
