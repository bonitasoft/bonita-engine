/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.events.EventActionType;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.profile.exception.profileentry.SProfileEntryCreationException;
import org.bonitasoft.engine.profile.exception.profileentry.SProfileEntryDeletionException;
import org.bonitasoft.engine.profile.exception.profileentry.SProfileEntryNotFoundException;
import org.bonitasoft.engine.profile.exception.profileentry.SProfileEntryReadException;
import org.bonitasoft.engine.profile.model.SProfile;
import org.bonitasoft.engine.profile.model.SProfileEntry;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.services.QueriableLoggerService;
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
public class ProfileServiceImplForProfileEntryTest {

    @Mock
    private EventService eventService;

    @Mock
    private TechnicalLoggerService logger;

    @Mock
    private ReadPersistenceService persistenceService;

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
        final SProfileEntry sProfileEntry = mock(SProfileEntry.class);
        doReturn(1L).when(sProfileEntry).getId();

        doReturn(false).when(eventService).hasHandlers(anyString(), any(EventActionType.class));
        doNothing().when(recorder).recordInsert(any(InsertRecord.class), any(SInsertEvent.class));
        doReturn(false).when(queriableLoggerService).isLoggable(anyString(), any(SQueriableLogSeverity.class));

        final SProfileEntry result = profileServiceImpl.createProfileEntry(sProfileEntry);
        assertNotNull(result);
        assertEquals(sProfileEntry, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public final void createNullProfileEntry() throws Exception {
        profileServiceImpl.createProfileEntry(null);
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
        final SProfileEntry sProfileEntry = mock(SProfileEntry.class);
        doReturn(6L).when(sProfileEntry).getId();

        doReturn(sProfileEntry).when(persistenceService).selectById(Matchers.<SelectByIdDescriptor<SProfileEntry>> any());
        doReturn(false).when(eventService).hasHandlers(anyString(), any(EventActionType.class));
        doNothing().when(recorder).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));
        doReturn(false).when(queriableLoggerService).isLoggable(anyString(), any(SQueriableLogSeverity.class));

        profileServiceImpl.deleteProfileEntry(1);
    }

    @Test(expected = SProfileEntryNotFoundException.class)
    public final void deleteNoProfileEntryById() throws SBonitaReadException, SProfileEntryNotFoundException, SProfileEntryDeletionException {
        when(persistenceService.selectById(Matchers.<SelectByIdDescriptor<SProfileEntry>> any())).thenReturn(null);

        profileServiceImpl.deleteProfileEntry(1);
    }

    @Test(expected = SProfileEntryDeletionException.class)
    public void deleteProfileEntryByIdThrowException() throws Exception {
        final SProfileEntry sProfileEntry = mock(SProfileEntry.class);
        doReturn(6L).when(sProfileEntry).getId();

        doReturn(sProfileEntry).when(persistenceService).selectById(Matchers.<SelectByIdDescriptor<SProfileEntry>> any());
        doReturn(false).when(eventService).hasHandlers(anyString(), any(EventActionType.class));
        doThrow(new SRecorderException("")).when(recorder).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));

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
        final SProfileEntry sProfileEntry = mock(SProfileEntry.class);
        doReturn(6L).when(sProfileEntry).getId();

        doReturn(false).when(eventService).hasHandlers(anyString(), any(EventActionType.class));
        doNothing().when(recorder).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));
        doReturn(false).when(queriableLoggerService).isLoggable(anyString(), any(SQueriableLogSeverity.class));

        profileServiceImpl.deleteProfileEntry(sProfileEntry);
    }

    @Test(expected = IllegalArgumentException.class)
    public final void deleteNoProfileEntryByObject() throws SProfileEntryDeletionException {
        profileServiceImpl.deleteProfileEntry(null);
    }

    @Test(expected = SProfileEntryDeletionException.class)
    public void deleteProfileEntryByObjectThrowException() throws Exception {
        final SProfileEntry sProfileEntry = mock(SProfileEntry.class);
        doReturn(6L).when(sProfileEntry).getId();

        doReturn(false).when(eventService).hasHandlers(anyString(), any(EventActionType.class));
        doThrow(new SRecorderException("")).when(recorder).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));

        profileServiceImpl.deleteProfileEntry(sProfileEntry);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.profile.impl.ProfileServiceImpl#getEntriesOfProfileByParentId(long, long, int, int, java.lang.String, org.bonitasoft.engine.persistence.OrderByType)}
     * .
     * 
     * @throws SBonitaReadException
     * @throws SProfileEntryReadException
     */
    @Test
    public final void getEntriesOfProfileByParentId() throws SBonitaReadException, SProfileEntryReadException {
        final List<SProfileEntry> sProfileEntries = new ArrayList<SProfileEntry>();
        final SProfileEntry sProfileEntry = mock(SProfileEntry.class);
        sProfileEntries.add(sProfileEntry);

        doReturn(sProfileEntries).when(persistenceService).selectList(Matchers.<SelectListDescriptor<SProfileEntry>> any());

        assertEquals(sProfileEntries, profileServiceImpl.getEntriesOfProfileByParentId(1, 0, 0, 0, null, OrderByType.ASC));
    }

    @Test
    public final void getNoEntriesOfProfileByParentId() throws SBonitaReadException, SProfileEntryReadException {
        final List<SProfile> sProfiles = new ArrayList<SProfile>();

        doReturn(sProfiles).when(persistenceService).selectList(Matchers.<SelectListDescriptor<SProfile>> any());

        assertEquals(sProfiles, profileServiceImpl.getEntriesOfProfileByParentId(1, 0, 0, 0, null, OrderByType.ASC));
    }

    @Test(expected = SProfileEntryReadException.class)
    public final void getEntriesOfProfileByParentIdThrowException() throws SBonitaReadException, SProfileEntryReadException {
        doThrow(new SBonitaReadException("plop")).when(persistenceService).selectList(Matchers.<SelectListDescriptor<SProfile>> any());

        profileServiceImpl.getEntriesOfProfileByParentId(1, 0, 0, 0, null, null);
    }
}
