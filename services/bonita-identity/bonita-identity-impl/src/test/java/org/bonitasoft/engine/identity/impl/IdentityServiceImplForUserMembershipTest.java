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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.identity.SIdentityException;
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
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class IdentityServiceImplForUserMembershipTest {

    @Mock
    private Recorder recorder;

    @Mock
    private ReadPersistenceService persistenceService;

    @Mock
    private EventService eventService;

    @Mock
    private TechnicalLoggerService logger;

    @InjectMocks
    private IdentityServiceImpl identityServiceImpl;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.identity.impl.IdentityServiceImpl#getLightUserMembership(long)}.
     */
    @Test
    public final void getLightUserMembershipById() throws SBonitaReadException, SIdentityException {
        final SUserMembership userMembership = mock(SUserMembership.class);
        final long userMembershipId = 546L;
        doReturn(userMembership).when(persistenceService).selectById(SelectDescriptorBuilder.getLightElementById(SUserMembership.class,
                "SUserMembership", userMembershipId));

        assertEquals(userMembership, identityServiceImpl.getLightUserMembership(userMembershipId));
    }

    @Test(expected = SIdentityException.class)
    public final void getLightUserMembershipByIdNotExist() throws SBonitaReadException, SIdentityException {
        final long userMembershipId = 546L;
        doReturn(null).when(persistenceService).selectById(SelectDescriptorBuilder.getLightElementById(SUserMembership.class,
                "SUserMembership", userMembershipId));

        identityServiceImpl.getLightUserMembership(userMembershipId);
    }

    @Test(expected = SIdentityException.class)
    public final void getLightUserMembershipByIdThrowException() throws SBonitaReadException, SIdentityException {
        final long userMembershipId = 546L;
        doThrow(new SBonitaReadException("")).when(persistenceService).selectById(SelectDescriptorBuilder.getLightElementById(SUserMembership.class,
                "SUserMembership", userMembershipId));

        identityServiceImpl.getLightUserMembership(userMembershipId);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.identity.impl.IdentityServiceImpl#getLightUserMembership(long, long, long)}.
     */
    @Test
    public final void getLightUserMembershipByUserAndGroupAndRole() throws SBonitaReadException, SIdentityException {
        final SUserMembership userMembership = mock(SUserMembership.class);
        final long userId = 546L;
        final long groupId = 565L;
        final long roleId = 54L;
        doReturn(userMembership).when(persistenceService).selectOne(SelectDescriptorBuilder.getLightUserMembership(userId, groupId, roleId));

        assertEquals(userMembership, identityServiceImpl.getLightUserMembership(userId, groupId, roleId));
    }

    @Test(expected = SIdentityException.class)
    public final void getLightUserMembershipByUserAndGroupAndRoleNotExist() throws SBonitaReadException, SIdentityException {
        final long userId = 546L;
        final long groupId = 565L;
        final long roleId = 54L;
        doReturn(null).when(persistenceService).selectOne(SelectDescriptorBuilder.getLightUserMembership(userId, groupId, roleId));

        identityServiceImpl.getLightUserMembership(userId, groupId, roleId);
    }

    @Test(expected = SIdentityException.class)
    public final void getLightUserMembershipByUserAndGroupAndRoleThrowException() throws SBonitaReadException, SIdentityException {
        final long userId = 546L;
        final long groupId = 565L;
        final long roleId = 54L;
        doThrow(new SBonitaReadException("")).when(persistenceService).selectOne(SelectDescriptorBuilder.getLightUserMembership(userId, groupId, roleId));

        identityServiceImpl.getLightUserMembership(userId, groupId, roleId);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.identity.impl.IdentityServiceImpl#getLightUserMemberships(int, int)}.
     */
    @Test
    public final void getLightUserMembershipsPaginated() throws SBonitaReadException, SIdentityException {
        final SUserMembership userMembership = mock(SUserMembership.class);
        final List<SUserMembership> userMemberships = Collections.singletonList(userMembership);
        final int startIndex = 546;
        final int numberOfElements = 565;
        doReturn(userMemberships).when(persistenceService).selectList(
                SelectDescriptorBuilder.getElements(SUserMembership.class, "LightUserMembership", startIndex, numberOfElements));

        assertEquals(userMemberships, identityServiceImpl.getLightUserMemberships(startIndex, numberOfElements));
    }

    @Test(expected = SIdentityException.class)
    public final void getLightUserMembershipsPaginatedThrowException() throws SBonitaReadException, SIdentityException {
        final int startIndex = 546;
        final int numberOfElements = 565;
        doThrow(new SBonitaReadException("")).when(persistenceService).selectList(
                SelectDescriptorBuilder.getElements(SUserMembership.class, "LightUserMembership", startIndex, numberOfElements));

        identityServiceImpl.getLightUserMemberships(startIndex, numberOfElements);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.identity.impl.IdentityServiceImpl#getNumberOfUserMemberships()}.
     */
    @Test
    public final void getNumberOfUserMemberships() throws SBonitaReadException, SIdentityException {
        final long numberOfUserMemberships = 3;
        doReturn(numberOfUserMemberships).when(persistenceService).selectOne(
                SelectDescriptorBuilder.getNumberOfElement("UserMembership", SUserMembership.class));

        assertEquals(numberOfUserMemberships, identityServiceImpl.getNumberOfUserMemberships());
    }

    @Test(expected = SIdentityException.class)
    public final void getNumberOfUserMembershipsThrowException() throws SBonitaReadException, SIdentityException {
        doThrow(new SBonitaReadException("")).when(persistenceService).selectOne(
                SelectDescriptorBuilder.getNumberOfElement("UserMembership", SUserMembership.class));

        identityServiceImpl.getNumberOfUserMemberships();
    }

    /**
     * Test method for {@link org.bonitasoft.engine.identity.impl.IdentityServiceImpl#getNumberOfUserMembershipsOfUser(long)}.
     */
    @Test
    public final void getNumberOfUserMembershipsOfUser() throws SBonitaReadException, SIdentityException {
        final long numberOfUserMemberships = 3;
        final long userId = 554L;
        doReturn(numberOfUserMemberships).when(persistenceService).selectOne(SelectDescriptorBuilder.getNumberOfUserMembershipsOfUser(userId));

        assertEquals(numberOfUserMemberships, identityServiceImpl.getNumberOfUserMembershipsOfUser(userId));
    }

    @Test(expected = SIdentityException.class)
    public final void getNumberOfUserMembershipsOfUserThrowException() throws SBonitaReadException, SIdentityException {
        final long userId = 554L;
        doThrow(new SBonitaReadException("")).when(persistenceService).selectOne(SelectDescriptorBuilder.getNumberOfUserMembershipsOfUser(userId));

        identityServiceImpl.getNumberOfUserMembershipsOfUser(userId);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.identity.impl.IdentityServiceImpl#getUserMembership(long)}.
     */
    @Test
    public final void getUserMembershipById() throws SBonitaReadException, SIdentityException {
        final SUserMembership userMembership = mock(SUserMembership.class);
        doReturn(userMembership).when(persistenceService).selectOne(Matchers.<SelectOneDescriptor<SUserMembership>> any());

        assertEquals(userMembership, identityServiceImpl.getUserMembership(546L));
    }

    @Test(expected = SIdentityException.class)
    public final void getUserMembershipByIdNotExist() throws SBonitaReadException, SIdentityException {
        doReturn(null).when(persistenceService).selectOne(Matchers.<SelectOneDescriptor<SUserMembership>> any());

        identityServiceImpl.getUserMembership(546L);
    }

    @Test(expected = SIdentityException.class)
    public final void getUserMembershipByIdThrowException() throws SBonitaReadException, SIdentityException {
        doThrow(new SBonitaReadException("")).when(persistenceService).selectOne(Matchers.<SelectOneDescriptor<SUserMembership>> any());

        identityServiceImpl.getUserMembership(546L);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.identity.impl.IdentityServiceImpl#getUserMembership(long, long, long)}.
     */
    @Test
    public final void getUserMembershipByUserAndGroupAndRole() throws SBonitaReadException, SIdentityException {
        final SUserMembership userMembership = mock(SUserMembership.class);
        doReturn(userMembership).when(persistenceService).selectOne(Matchers.<SelectOneDescriptor<SUserMembership>> any());

        assertEquals(userMembership, identityServiceImpl.getUserMembership(546L, 565L, 54L));
    }

    @Test(expected = SIdentityException.class)
    public final void getUserMembershipByUserAndGroupAndRoleNotExist() throws SBonitaReadException, SIdentityException {
        doReturn(null).when(persistenceService).selectOne(Matchers.<SelectOneDescriptor<SUserMembership>> any());

        identityServiceImpl.getUserMembership(546L, 565L, 54L);
    }

    @Test(expected = SIdentityException.class)
    public final void getUserMembershipByUserAndGroupAndRoleThrowException() throws SBonitaReadException, SIdentityException {
        doThrow(new SBonitaReadException("")).when(persistenceService).selectOne(Matchers.<SelectOneDescriptor<SUserMembership>> any());

        identityServiceImpl.getUserMembership(546L, 565L, 54L);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.identity.impl.IdentityServiceImpl#getUserMembershipsOfGroup(long)}.
     */
    @Test
    public void getUserMembershipsOfGroup() throws Exception {
        final SUserMembership userMembership = mock(SUserMembership.class);
        when(persistenceService.selectList(SelectDescriptorBuilder.getUserMembershipsByGroup(1l, 0, 20))).thenReturn(Collections.singletonList(userMembership));

        final List<SUserMembership> userMemberships = identityServiceImpl.getUserMembershipsOfGroup(1l, 0, 20);

        assertEquals(userMembership, userMemberships.get(0));
    }

    @Test(expected = SIdentityException.class)
    public void getUserMembershipsOfGroupThrowException() throws Exception {
        when(persistenceService.selectList(SelectDescriptorBuilder.getUserMembershipsByGroup(1l, 0, 20))).thenThrow(new SBonitaReadException(""));

        identityServiceImpl.getUserMembershipsOfGroup(1l, 0, 20);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.identity.impl.IdentityServiceImpl#getUserMembershipsOfRole(long)}.
     */
    @Test
    public void getUserMembershipsOfRole() throws Exception {
        final SUserMembership userMembership = mock(SUserMembership.class);
        when(persistenceService.selectList(SelectDescriptorBuilder.getUserMembershipsByRole(1l, 0, 20))).thenReturn(Collections.singletonList(userMembership));

        final List<SUserMembership> userMemberships = identityServiceImpl.getUserMembershipsOfRole(1l, 0, 20);

        assertEquals(userMembership, userMemberships.get(0));
    }

    @Test(expected = SIdentityException.class)
    public void getUserMembershipsOfRoleThrowException() throws Exception {
        when(persistenceService.selectList(SelectDescriptorBuilder.getUserMembershipsByRole(1l, 0, 20))).thenThrow(new SBonitaReadException(""));

        identityServiceImpl.getUserMembershipsOfRole(1l, 0, 20);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.identity.impl.IdentityServiceImpl#getUserMemberships(int, int, org.bonitasoft.engine.persistence.OrderByOption)}.
     */
    @Test
    public void getUserMembershipsPaginatedWithOrder() throws Exception {
        final SUserMembership userMembership = mock(SUserMembership.class);
        final OrderByOption orderByOption = new OrderByOption(SUserMembership.class, "username", OrderByType.ASC);
        doReturn(Collections.singletonList(userMembership)).when(persistenceService)
                .selectList(
                        SelectDescriptorBuilder.getElements(SUserMembership.class, "UserMembership",
                                new QueryOptions(0, 10, Collections.singletonList(orderByOption))));

        final List<SUserMembership> userMemberships = identityServiceImpl.getUserMemberships(0, 10, orderByOption);

        assertEquals(userMembership, userMemberships.get(0));
    }

    @Test(expected = SIdentityException.class)
    public void getUserMembershipsPaginatedWithOrderThrowException() throws Exception {
        final OrderByOption orderByOption = new OrderByOption(SUserMembership.class, "username", OrderByType.ASC);
        when(
                persistenceService.selectList(SelectDescriptorBuilder.getElements(SUserMembership.class, "UserMembership",
                        new QueryOptions(0, 10, Collections.singletonList(orderByOption))))).thenThrow(new SBonitaReadException(""));

        identityServiceImpl.getUserMemberships(0, 10, orderByOption);
    }

    @Test
    public void getUserMembershipsOrderByRole() throws Exception {
        final SUserMembership userMembership = mock(SUserMembership.class);
        final OrderByOption orderByOption = new OrderByOption(SRole.class, "name", OrderByType.ASC);
        when(
                persistenceService.selectList(SelectDescriptorBuilder.getUserMembershipsWithRole(new QueryOptions(0, 10, Collections
                        .singletonList(orderByOption))))).thenReturn(Collections.singletonList(userMembership));

        final List<SUserMembership> userMemberships = identityServiceImpl.getUserMemberships(0, 10, orderByOption);

        assertEquals(userMembership, userMemberships.get(0));
    }

    @Test
    public void getUserMembershipsOrderByGroup() throws Exception {
        final SUserMembership userMembership = mock(SUserMembership.class);
        final OrderByOption orderByOption = new OrderByOption(SGroup.class, "name", OrderByType.ASC);
        when(
                persistenceService.selectList(SelectDescriptorBuilder.getUserMembershipsWithGroup(new QueryOptions(0, 10, Collections
                        .singletonList(orderByOption))))).thenReturn(Collections.singletonList(userMembership));

        final List<SUserMembership> userMemberships = identityServiceImpl.getUserMemberships(0, 10, orderByOption);

        assertEquals(userMembership, userMemberships.get(0));
    }

    /**
     * Test method for {@link org.bonitasoft.engine.identity.impl.IdentityServiceImpl#getUserMemberships(int, int)}.
     */
    @Test
    public final void getUserMembershipsPaginated() throws SBonitaReadException, SIdentityException {
        final SUserMembership userMembership = mock(SUserMembership.class);
        final List<SUserMembership> userMemberships = Collections.singletonList(userMembership);
        doReturn(userMemberships).when(persistenceService).selectList(Matchers.<SelectListDescriptor<SUserMembership>> any());

        assertEquals(userMemberships, identityServiceImpl.getUserMemberships(0, 10));
    }

    @Test(expected = SIdentityException.class)
    public final void getUserMembershipsPaginatedThrowException() throws SBonitaReadException, SIdentityException {
        doThrow(new SBonitaReadException("")).when(persistenceService).selectList(Matchers.<SelectListDescriptor<SUserMembership>> any());

        identityServiceImpl.getUserMemberships(0, 10);
    }

}
