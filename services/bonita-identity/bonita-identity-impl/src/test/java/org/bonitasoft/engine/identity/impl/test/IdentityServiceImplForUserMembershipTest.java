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
package org.bonitasoft.engine.identity.impl.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.identity.SIdentityException;
import org.bonitasoft.engine.identity.impl.IdentityServiceImpl;
import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.model.SRole;
import org.bonitasoft.engine.identity.model.SUserMembership;
import org.bonitasoft.engine.identity.recorder.SelectDescriptorBuilder;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.Recorder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.stubbing.answers.Returns;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class IdentityServiceImplForUserMembershipTest {

    Recorder recorder;

    ReadPersistenceService persistence;

    EventService eventService;

    TechnicalLoggerService logger;

    private IdentityServiceImpl identityServiceImpl;

    @Before
    public void setup() {
        recorder = mock(Recorder.class);
        persistence = mock(ReadPersistenceService.class);
        eventService = mock(EventService.class);
        logger = mock(TechnicalLoggerService.class, new Returns(true));
        identityServiceImpl = new IdentityServiceImpl(persistence, recorder, eventService, null, logger, null, null);
    }

    @Test
    public void getUserMemberships() throws Exception {
        final SUserMembership userMembership = mock(SUserMembership.class);
        final OrderByOption orderByOption = new OrderByOption(SUserMembership.class, "username", OrderByType.ASC);
        when(
                persistence.selectList(SelectDescriptorBuilder.getElements(SUserMembership.class, "UserMembership",
                        new QueryOptions(0, 10, Collections.singletonList(orderByOption))))).thenReturn(Collections.singletonList(userMembership));

        final List<SUserMembership> userMemberships = identityServiceImpl.getUserMemberships(0, 10, orderByOption);

        assertEquals(userMembership, userMemberships.get(0));
    }

    @Test(expected = SIdentityException.class)
    public void getUserMembershipsThrowExceptions() throws Exception {
        final OrderByOption orderByOption = new OrderByOption(SUserMembership.class, "username", OrderByType.ASC);
        when(
                persistence.selectList(SelectDescriptorBuilder.getElements(SUserMembership.class, "UserMembership",
                        new QueryOptions(0, 10, Collections.singletonList(orderByOption))))).thenThrow(new SBonitaReadException(""));

        identityServiceImpl.getUserMemberships(0, 10, orderByOption);
    }

    @Test
    public void getUserMembershipsOrderByRole() throws Exception {
        final SUserMembership userMembership = mock(SUserMembership.class);
        final OrderByOption orderByOption = new OrderByOption(SRole.class, "name", OrderByType.ASC);
        when(persistence.selectList(SelectDescriptorBuilder.getUserMembershipsWithRole(new QueryOptions(0, 10, Collections.singletonList(orderByOption)))))
                .thenReturn(Collections.singletonList(userMembership));

        final List<SUserMembership> userMemberships = identityServiceImpl.getUserMemberships(0, 10, orderByOption);

        assertEquals(userMembership, userMemberships.get(0));
    }

    @Test
    public void getUserMembershipsOrderByGroup() throws Exception {
        final SUserMembership userMembership = mock(SUserMembership.class);
        final OrderByOption orderByOption = new OrderByOption(SGroup.class, "name", OrderByType.ASC);
        when(persistence.selectList(SelectDescriptorBuilder.getUserMembershipsWithGroup(new QueryOptions(0, 10, Collections.singletonList(orderByOption)))))
                .thenReturn(Collections.singletonList(userMembership));

        final List<SUserMembership> userMemberships = identityServiceImpl.getUserMemberships(0, 10, orderByOption);

        assertEquals(userMembership, userMemberships.get(0));
    }

    @Test
    public void getUserMembershipsOfGroup() throws Exception {
        final SUserMembership userMembership = mock(SUserMembership.class);
        when(persistence.selectList(SelectDescriptorBuilder.getUserMembershipsByGroup(1l))).thenReturn(Collections.singletonList(userMembership));

        final List<SUserMembership> userMemberships = identityServiceImpl.getUserMembershipsOfGroup(1l);

        assertEquals(userMembership, userMemberships.get(0));
    }

    @Test(expected = SIdentityException.class)
    public void getUserMembershipsOfGroupThrowExceptions() throws Exception {
        when(persistence.selectList(SelectDescriptorBuilder.getUserMembershipsByGroup(1l))).thenThrow(new SBonitaReadException(""));

        identityServiceImpl.getUserMembershipsOfGroup(1l);
    }

    @Test
    public void getUserMembershipsOfRole() throws Exception {
        final SUserMembership userMembership = mock(SUserMembership.class);
        when(persistence.selectList(SelectDescriptorBuilder.getUserMembershipsByRole(1l))).thenReturn(Collections.singletonList(userMembership));

        final List<SUserMembership> userMemberships = identityServiceImpl.getUserMembershipsOfRole(1l);

        assertEquals(userMembership, userMemberships.get(0));
    }

    @Test(expected = SIdentityException.class)
    public void getUserMembershipsOfRoleThrowExceptions() throws Exception {
        when(persistence.selectList(SelectDescriptorBuilder.getUserMembershipsByRole(1l))).thenThrow(new SBonitaReadException(""));

        identityServiceImpl.getUserMembershipsOfRole(1l);
    }

}
