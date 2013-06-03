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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;

import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.profile.SProfileNotFoundException;
import org.bonitasoft.engine.profile.model.SProfile;
import org.bonitasoft.engine.recorder.Recorder;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Celine Souchet
 */
public class ProfileServiceImplForProfileTest {

    Recorder recorder;

    ReadPersistenceService persistence;

    EventService eventService;

    TechnicalLoggerService logger;

    @Before
    public void setup() {
        recorder = mock(Recorder.class);
        persistence = mock(ReadPersistenceService.class);
        eventService = mock(EventService.class);
        logger = mock(TechnicalLoggerService.class);
        // QueriableLoggerService queriableLoggerService

    }

    @Test
    public void getNumberOfProfilesWithQueryOptions() throws Exception {
        final ProfileServiceImpl profileServiceImpl = new ProfileServiceImpl(persistence, recorder, eventService, logger, null);
        final QueryOptions options = new QueryOptions(0, 10);
        when(persistence.getNumberOfEntities(SProfile.class, options, Collections.<String, Object> emptyMap())).thenReturn(1L);

        assertEquals(1L, profileServiceImpl.getNumberOfProfiles(options));
    }

    @Test(expected = SBonitaSearchException.class)
    public void getNumberOfProfilesWithQueryOptionsWithException() throws Exception {
        final ProfileServiceImpl profileServiceImpl = new ProfileServiceImpl(persistence, recorder, eventService, logger, null);
        final QueryOptions options = new QueryOptions(0, 10);
        when(persistence.getNumberOfEntities(SProfile.class, options, Collections.<String, Object> emptyMap())).thenThrow(new SBonitaReadException(""));

        profileServiceImpl.getNumberOfProfiles(options);
    }

    @Test
    public void searchProfilesWithQueryOptions() throws Exception {
        final ProfileServiceImpl profileServiceImpl = new ProfileServiceImpl(persistence, recorder, eventService, logger, null);
        final QueryOptions options = new QueryOptions(0, 10);
        when(persistence.searchEntity(SProfile.class, options, Collections.<String, Object> emptyMap())).thenReturn(new ArrayList<SProfile>());

        assertNotNull(profileServiceImpl.searchProfiles(options));
    }

    @Test(expected = SBonitaSearchException.class)
    public void searchProfilesWithQueryOptionsWithException() throws Exception {
        final ProfileServiceImpl profileServiceImpl = new ProfileServiceImpl(persistence, recorder, eventService, logger, null);
        final QueryOptions options = new QueryOptions(0, 10);
        when(persistence.searchEntity(SProfile.class, options, Collections.<String, Object> emptyMap())).thenThrow(new SBonitaReadException(""));

        profileServiceImpl.searchProfiles(options);
    }

    // @Test
    // public void getProfile() throws Exception {
    // final ProfileServiceImpl profileServiceImpl = new ProfileServiceImpl(persistence, recorder, eventService, logger, null);
    // final long groupId = 1;
    // SProfile sProfile;
    // when(persistence.selectById(any(SelectByIdDescriptor.class)).thenReturn(sProfile);
    //
    // assertEquals(3L, profileServiceImpl.getProfile(groupId));
    // }

    @Test(expected = SProfileNotFoundException.class)
    public void getNoProfile() throws Exception {
        final ProfileServiceImpl profileServiceImpl = new ProfileServiceImpl(persistence, recorder, eventService, logger, null);
        when(persistence.selectById(any(SelectByIdDescriptor.class))).thenReturn(null);

        profileServiceImpl.getProfile(1);
    }

    @Test(expected = SProfileNotFoundException.class)
    public void getProfileWithException() throws Exception {
        final ProfileServiceImpl profileServiceImpl = new ProfileServiceImpl(persistence, recorder, eventService, logger, null);
        when(persistence.selectById(any(SelectByIdDescriptor.class))).thenThrow(new SBonitaReadException(""));

        profileServiceImpl.getProfile(1);
    }

    // @Test
    // public void getProfileByName() throws Exception {
    // final ProfileServiceImpl profileServiceImpl = new ProfileServiceImpl(persistence, recorder, eventService, logger, null);
    // final String profileName = "plop";
    // SProfile sProfile;
    // when(persistence.selectOne(any(SelectOneDescriptor.class)).thenReturn(sProfile);
    //
    // assertEquals(3L, profileServiceImpl.getProfileByName(profileName));
    // }

    @Test(expected = SProfileNotFoundException.class)
    public void getNoProfileByName() throws Exception {
        final ProfileServiceImpl profileServiceImpl = new ProfileServiceImpl(persistence, recorder, eventService, logger, null);
        when(persistence.selectOne(any(SelectOneDescriptor.class))).thenReturn(null);

        profileServiceImpl.getProfileByName("plop");
    }

    @Test(expected = SProfileNotFoundException.class)
    public void getProfileByNameWithException() throws Exception {
        final ProfileServiceImpl profileServiceImpl = new ProfileServiceImpl(persistence, recorder, eventService, logger, null);
        when(persistence.selectOne(any(SelectOneDescriptor.class))).thenThrow(new SBonitaReadException(""));

        profileServiceImpl.getProfileByName("plop");
    }

}
