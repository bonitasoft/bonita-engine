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
package org.bonitasoft.engine.actor.mapping.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doThrow;
import static org.powermock.api.mockito.PowerMockito.doCallRealMethod;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.bonitasoft.engine.actor.mapping.SActorCreationException;
import org.bonitasoft.engine.actor.mapping.SActorDeletionException;
import org.bonitasoft.engine.actor.mapping.SActorMemberDeletionException;
import org.bonitasoft.engine.actor.mapping.SActorNotFoundException;
import org.bonitasoft.engine.actor.mapping.SActorUpdateException;
import org.bonitasoft.engine.actor.mapping.model.SActor;
import org.bonitasoft.engine.actor.mapping.model.SActorMember;
import org.bonitasoft.engine.actor.mapping.model.SActorUpdateBuilder;
import org.bonitasoft.engine.actor.mapping.model.SActorUpdateBuilderFactory;
import org.bonitasoft.engine.actor.mapping.persistence.SelectDescriptorBuilder;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.events.EventActionType;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteAllRecord;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author Celine Souchet
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ ActorMappingServiceImpl.class })
public class ActorMappingServiceImplTest {

    private Recorder recorder;

    private ReadPersistenceService persistenceService;

    private EventService eventService;

    private QueriableLoggerService queriableLoggerService;

    private IdentityService identityService;

    private ActorMappingServiceImpl actorMappingServiceImpl;

    @Before
    public void initialize() {
        recorder = mock(Recorder.class);
        persistenceService = mock(ReadPersistenceService.class);
        eventService = mock(EventService.class);
        queriableLoggerService = mock(QueriableLoggerService.class);
        identityService = mock(IdentityService.class);
        actorMappingServiceImpl = new ActorMappingServiceImpl(persistenceService, recorder, eventService, queriableLoggerService, identityService);
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.actor.mapping.impl.ActorMappingServiceImpl#getActor(long)}.
     *
     * @throws SBonitaReadException
     * @throws SActorNotFoundException
     */
    @Test
    public final void getActorById() throws SActorNotFoundException, SBonitaReadException {
        final SActor actor = mock(SActor.class);
        when(persistenceService.selectById(Matchers.<SelectByIdDescriptor<SActor>> any())).thenReturn(actor);

        Assert.assertEquals(actor, actorMappingServiceImpl.getActor(456L));
    }

    @Test(expected = SActorNotFoundException.class)
    public final void getActorByIdNotExists() throws SBonitaReadException, SActorNotFoundException {
        when(persistenceService.selectById(Matchers.<SelectByIdDescriptor<SActor>> any())).thenReturn(null);

        actorMappingServiceImpl.getActor(456L);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.actor.mapping.impl.ActorMappingServiceImpl#getNumberOfActorMembers(long)}.
     *
     * @throws SBonitaReadException
     */
    @Test
    public final void getNumberOfActorMembers() throws SBonitaReadException {
        final long actorId = 456L;
        final long numberOfActorMemebers = 1L;
        when(persistenceService.selectOne(Matchers.<SelectOneDescriptor<Long>>any())).thenReturn(numberOfActorMemebers);

        Assert.assertEquals(numberOfActorMemebers, actorMappingServiceImpl.getNumberOfActorMembers(actorId));
    }

    /**
     * Test method for {@link org.bonitasoft.engine.actor.mapping.impl.ActorMappingServiceImpl#getNumberOfUsersOfActor(long)}.
     *
     * @throws SBonitaReadException
     */
    @Test
    public final void getNumberOfUsersOfActor() throws SBonitaReadException {
        final long numberOfUsersOfActor = 155L;
        when(persistenceService.selectOne(Matchers.<SelectOneDescriptor<Long>> any())).thenReturn(numberOfUsersOfActor);

        Assert.assertEquals(numberOfUsersOfActor, actorMappingServiceImpl.getNumberOfUsersOfActor(456L));
    }

    @Test(expected = RuntimeException.class)
    public final void getNumberOfUsersOfActorThrowException() throws SBonitaReadException {
        when(persistenceService.selectOne(Matchers.<SelectOneDescriptor<SActor>> any())).thenThrow(new SBonitaReadException(""));

        actorMappingServiceImpl.getNumberOfUsersOfActor(456L);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.actor.mapping.impl.ActorMappingServiceImpl#getNumberOfRolesOfActor(long)}.
     *
     * @throws SBonitaReadException
     */
    @Test
    public final void getNumberOfRolesOfActor() throws SBonitaReadException {
        final long numberOfRolesOfActor = 155L;
        when(persistenceService.selectOne(Matchers.<SelectOneDescriptor<Long>> any())).thenReturn(numberOfRolesOfActor);

        Assert.assertEquals(numberOfRolesOfActor, actorMappingServiceImpl.getNumberOfRolesOfActor(456L));
    }

    @Test(expected = RuntimeException.class)
    public final void getNumberOfRolesOfActorThrowException() throws SBonitaReadException {
        when(persistenceService.selectOne(Matchers.<SelectOneDescriptor<SActor>> any())).thenThrow(new SBonitaReadException(""));

        actorMappingServiceImpl.getNumberOfRolesOfActor(456L);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.actor.mapping.impl.ActorMappingServiceImpl#getNumberOfGroupsOfActor(long)}.
     *
     * @throws SBonitaReadException
     */
    @Test
    public final void getNumberOfGroupsOfActor() throws SBonitaReadException {
        final long numberOfGroupsOfActor = 155L;
        when(persistenceService.selectOne(Matchers.<SelectOneDescriptor<Long>> any())).thenReturn(numberOfGroupsOfActor);

        Assert.assertEquals(numberOfGroupsOfActor, actorMappingServiceImpl.getNumberOfGroupsOfActor(456L));
    }

    @Test(expected = RuntimeException.class)
    public final void getNumberOfGroupsOfActorThrowException() throws SBonitaReadException {
        when(persistenceService.selectOne(Matchers.<SelectOneDescriptor<SActor>> any())).thenThrow(new SBonitaReadException(""));

        actorMappingServiceImpl.getNumberOfGroupsOfActor(456L);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.actor.mapping.impl.ActorMappingServiceImpl#getNumberOfMembershipsOfActor(long)}.
     *
     * @throws SBonitaReadException
     */
    @Test
    public final void getNumberOfMembershipsOfActor() throws SBonitaReadException {
        final long numberOfGroupsOfActor = 155L;
        when(persistenceService.selectOne(Matchers.<SelectOneDescriptor<Long>> any())).thenReturn(numberOfGroupsOfActor);

        Assert.assertEquals(numberOfGroupsOfActor, actorMappingServiceImpl.getNumberOfMembershipsOfActor(456L));
    }

    @Test(expected = RuntimeException.class)
    public final void getNumberOfMembershipsOfActorThrowException() throws SBonitaReadException {
        when(persistenceService.selectOne(Matchers.<SelectOneDescriptor<SActor>> any())).thenThrow(new SBonitaReadException(""));

        actorMappingServiceImpl.getNumberOfMembershipsOfActor(456L);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.actor.mapping.impl.ActorMappingServiceImpl#getActor(java.lang.String, long)}.
     *
     * @throws SBonitaReadException
     * @throws SActorNotFoundException
     */
    @Test
    public final void getActorByNameAndScopeId() throws SActorNotFoundException, SBonitaReadException {
        final SActor actor = mock(SActor.class);
        when(persistenceService.selectOne(Matchers.<SelectOneDescriptor<SActor>> any())).thenReturn(actor);

        Assert.assertEquals(actor, actorMappingServiceImpl.getActor("actorName", 69L));
    }

    @Test(expected = SActorNotFoundException.class)
    public final void getActorByNameAndScopeIdNotExists() throws SActorNotFoundException, SBonitaReadException {
        when(persistenceService.selectOne(Matchers.<SelectOneDescriptor<SActor>> any())).thenReturn(null);

        actorMappingServiceImpl.getActor("actorName", 69L);
    }

    @Test(expected = SActorNotFoundException.class)
    public final void getActorByNameAndScopeIdThrowException() throws SActorNotFoundException, SBonitaReadException {
        when(persistenceService.selectOne(Matchers.<SelectOneDescriptor<SActor>> any())).thenThrow(new SBonitaReadException(""));

        actorMappingServiceImpl.getActor("actorName", 69L);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.actor.mapping.impl.ActorMappingServiceImpl#getActorMembers(long, int, int)}.
     *
     * @throws SBonitaReadException
     */
    @Test
    public final void getActorMembersByActorPaginated() throws SBonitaReadException {
        final List<SActorMember> actors = new ArrayList<SActorMember>();
        when(persistenceService.selectList(Matchers.<SelectListDescriptor<SActorMember>>any())).thenReturn(actors);

        Assert.assertEquals(actors, actorMappingServiceImpl.getActorMembers(4115L, 0, 1));
    }

    /**
     * Test method for {@link org.bonitasoft.engine.actor.mapping.impl.ActorMappingServiceImpl#getActorMembersOfGroup(long, int, int)}.
     *
     * @throws SBonitaReadException
     */
    @Test
    public final void getActorMembersOfGroup() throws SBonitaReadException {
        final List<SActorMember> actors = new ArrayList<SActorMember>(6);
        when(persistenceService.selectList(Matchers.<SelectListDescriptor<SActorMember>> any())).thenReturn(actors);

        Assert.assertEquals(actors, actorMappingServiceImpl.getActorMembersOfGroup(41L, 0, 1));
    }

    /**
     * Test method for {@link org.bonitasoft.engine.actor.mapping.impl.ActorMappingServiceImpl#getActorMembersOfRole(long, int, int)}.
     *
     * @throws SBonitaReadException
     */
    @Test
    public final void getActorMembersOfRole() throws SBonitaReadException {
        final List<SActorMember> actors = new ArrayList<SActorMember>(3);
        when(persistenceService.selectList(Matchers.<SelectListDescriptor<SActorMember>> any())).thenReturn(actors);

        Assert.assertEquals(actors, actorMappingServiceImpl.getActorMembersOfRole(41L, 0, 1));
    }

    /**
     * Test method for {@link org.bonitasoft.engine.actor.mapping.impl.ActorMappingServiceImpl#getActorMember(long, long, long, long)}.
     */
    @Test
    public final void getActorMember() throws SBonitaReadException {
        // Given
        final SActorMember actor = mock(SActorMember.class);
        final long actorId = 1L;
        final long userId = 2L;
        final long groupId = 3L;
        final long roleId = 4L;
        when(persistenceService.selectOne(SelectDescriptorBuilder.getActorMember(actorId, userId, groupId, roleId))).thenReturn(actor);

        // When
        final SActorMember sActorMember = actorMappingServiceImpl.getActorMember(actorId, userId, groupId, roleId);

        // Then
        Assert.assertEquals(actor, sActorMember);
    }

    @Test(expected = SBonitaReadException.class)
    public final void getActorMemberThrowException() throws SBonitaReadException {
        // Given
        final long actorId = 1L;
        final long userId = 2L;
        final long groupId = 3L;
        final long roleId = 4L;
        when(persistenceService.selectOne(SelectDescriptorBuilder.getActorMember(actorId, userId, groupId, roleId)))
                .thenThrow(new SBonitaReadException("plop"));

        // When
        actorMappingServiceImpl.getActorMember(actorId, userId, groupId, roleId);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.actor.mapping.impl.ActorMappingServiceImpl#getActors(java.util.List)}.
     *
     * @throws SBonitaReadException
     */
    @Test
    public final void getActorsByListOfIds() throws SBonitaReadException {
        final List<SActor> actors = new ArrayList<SActor>(3);
        when(persistenceService.selectList(Matchers.<SelectListDescriptor<SActor>> any())).thenReturn(actors);

        final List<Long> actorIds = new ArrayList<Long>(1);
        actorIds.add(589L);
        Assert.assertEquals(actors, actorMappingServiceImpl.getActors(actorIds));
    }

    @Test
    public final void getActorsByListOfIdsWithEmptyList() throws SBonitaReadException {
        Assert.assertEquals(Collections.emptyList(), actorMappingServiceImpl.getActors(new ArrayList<Long>(0)));
    }

    @Test
    public final void getActorsByListOfIdsWithNullList() throws SBonitaReadException {
        Assert.assertEquals(Collections.emptyList(), actorMappingServiceImpl.getActors(null));
    }

    /**
     * Test method for {@link org.bonitasoft.engine.actor.mapping.impl.ActorMappingServiceImpl#getActors(long, org.bonitasoft.engine.persistence.QueryOptions)}
     * .
     *
     * @throws SBonitaReadException
     */
    @Test
    public final void getActors() throws SBonitaReadException {
        final QueryOptions queryOptions = new QueryOptions(0, 100, SActor.class, "id", OrderByType.ASC);
        final List<SActor> actors = new ArrayList<SActor>(3);
        when(persistenceService.selectList(Matchers.<SelectListDescriptor<SActor>> any())).thenReturn(actors);

        Assert.assertEquals(actors, actorMappingServiceImpl.getActors(41564L, queryOptions));
    }

    /**
     * Test method for {@link org.bonitasoft.engine.actor.mapping.impl.ActorMappingServiceImpl#canUserStartProcessDefinition(long, long)}.
     *
     * @throws SBonitaReadException
     */
    @Test
    public final void shouldBeAllowedToStartProcessDefinition() throws SBonitaReadException {
        final List<Long> actorMembers = new ArrayList<Long>(1);
        actorMembers.add(123L);
        when(persistenceService.selectList(Matchers.<SelectListDescriptor<Long>> any())).thenReturn(actorMembers);
        when(persistenceService.selectOne(Matchers.<SelectOneDescriptor<Long>>any())).thenReturn(3L);

        Assertions.assertThat(actorMappingServiceImpl.canUserStartProcessDefinition(315L, 5484L)).as("Should be allowed to start Process").isTrue();
    }

    @Test
    public final void shouldNotBeAllowedToStartProcessDefinitionIfNoActorMembers() throws SBonitaReadException {
        final List<Long> actorMembers = new ArrayList<Long>(0);
        when(persistenceService.selectList(Matchers.<SelectListDescriptor<Long>> any())).thenReturn(actorMembers);

        Assertions.assertThat(actorMappingServiceImpl.canUserStartProcessDefinition(315L, 5484L)).as("Should NOT be allowed to start Process").isFalse();
    }


    @Test
    public final void shouldNotBeAllowedToStartProcessDefinitionIfNoUserMemberships() throws SBonitaReadException {
        final List<Long> actorMembers = new ArrayList<Long>(1);
        actorMembers.add(123L);
        when(persistenceService.selectList(Matchers.<SelectListDescriptor<Long>> any())).thenReturn(actorMembers);
        when(persistenceService.selectOne(Matchers.<SelectOneDescriptor<Long>>any())).thenReturn(0L);

        Assertions.assertThat(actorMappingServiceImpl.canUserStartProcessDefinition(315L, 5484L)).as("Should NOT be allowed to start Process").isFalse();
    }

    /**
     * Test method for {@link org.bonitasoft.engine.actor.mapping.impl.ActorMappingServiceImpl#getActors(java.util.Set, java.lang.Long)}.
     *
     * @throws SBonitaReadException
     */
    @Test
    public final void getActorsByScopeIdsAndUserId() throws SBonitaReadException {
        final List<SActor> actors = new ArrayList<SActor>(3);
        when(persistenceService.selectList(Matchers.<SelectListDescriptor<SActor>> any())).thenReturn(actors);

        Assert.assertEquals(actors, actorMappingServiceImpl.getActors(new HashSet<Long>(), 5484L));
    }

    /**
     * Test method for {@link org.bonitasoft.engine.actor.mapping.impl.ActorMappingServiceImpl#addActors(java.util.Set)}.
     *
     * @throws SActorCreationException
     */
    @Test
    public final void addActors() throws SActorCreationException {
        final Set<SActor> actors = new HashSet<SActor>();
        actors.add(mock(SActor.class));

        final ActorMappingServiceImpl mockedActorMappingServiceImpl = mock(ActorMappingServiceImpl.class, withSettings().spiedInstance(actorMappingServiceImpl));
        final SActor sActor = mock(SActor.class);
        when(mockedActorMappingServiceImpl.addActor(any(SActor.class))).thenReturn(sActor);

        // Let's call it for real:
        doCallRealMethod().when(mockedActorMappingServiceImpl).addActors(actors);
        final Set<SActor> result = mockedActorMappingServiceImpl.addActors(actors);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(sActor, result.toArray()[0]);

        // and check methods are called:
        verify(mockedActorMappingServiceImpl, times(1)).addActor(any(SActor.class));
    }

    @Test
    public final void addActorsEmptyList() throws SActorCreationException {
        final Set<SActor> actors = new HashSet<SActor>();

        final Set<SActor> result = actorMappingServiceImpl.addActors(actors);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test(expected = SActorCreationException.class)
    public final void addActorsThrowException() throws SActorCreationException {
        final Set<SActor> actors = new HashSet<SActor>();
        actors.add(mock(SActor.class));

        final ActorMappingServiceImpl mockedActorMappingServiceImpl = mock(ActorMappingServiceImpl.class, withSettings().spiedInstance(actorMappingServiceImpl));
        when(mockedActorMappingServiceImpl.addActor(any(SActor.class))).thenThrow(new SActorCreationException(""));

        // Let's call it for real:
        doCallRealMethod().when(mockedActorMappingServiceImpl).addActors(actors);
        mockedActorMappingServiceImpl.addActors(actors);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.actor.mapping.impl.ActorMappingServiceImpl#addActor(org.bonitasoft.engine.actor.mapping.model.SActor)}.
     *
     * @throws Exception
     */
    @Test
    public final void addActor() throws Exception {
        final SActor sActor = mock(SActor.class);
        doReturn(1L).when(sActor).getId();

        doReturn(false).when(eventService).hasHandlers(anyString(), any(EventActionType.class));
        doNothing().when(recorder).recordInsert(any(InsertRecord.class), any(SInsertEvent.class));
        doReturn(false).when(queriableLoggerService).isLoggable(anyString(), any(SQueriableLogSeverity.class));

        final SActor result = actorMappingServiceImpl.addActor(sActor);
        assertNotNull(result);
        assertEquals(sActor, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public final void addNullActor() throws Exception {
        actorMappingServiceImpl.addActor(null);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.actor.mapping.impl.ActorMappingServiceImpl#updateActor(long, org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor)}.
     *
     * @throws SBonitaReadException
     * @throws SActorUpdateException
     * @throws SActorNotFoundException
     * @throws SRecorderException
     */
    @Test
    public final void updateActor() throws SActorNotFoundException, SActorUpdateException, SBonitaReadException {
        final SActor sActor = mock(SActor.class);
        doReturn(3L).when(sActor).getId();

        final SActorUpdateBuilder sActorUpdateBuilder = BuilderFactory.get(SActorUpdateBuilderFactory.class).createNewInstance();
        sActorUpdateBuilder.updateDescription("newDescription");
        sActorUpdateBuilder.updateDisplayName("newDisplayName");

        doReturn(sActor).when(persistenceService).selectById(Matchers.<SelectByIdDescriptor<SActor>> any());
        doReturn(false).when(eventService).hasHandlers(anyString(), any(EventActionType.class));
        doReturn(false).when(queriableLoggerService).isLoggable(anyString(), any(SQueriableLogSeverity.class));

        final SActor result = actorMappingServiceImpl.updateActor(3, sActorUpdateBuilder.done());
        assertNotNull(result);
        assertEquals(sActor, result);
    }

    @Test(expected = SActorNotFoundException.class)
    public final void updateActorNotExists() throws SActorUpdateException, SActorNotFoundException, SBonitaReadException {
        final SActorUpdateBuilder sActorUpdateBuilder = BuilderFactory.get(SActorUpdateBuilderFactory.class).createNewInstance();
        doReturn(null).when(persistenceService).selectById(Matchers.<SelectByIdDescriptor<SActor>> any());

        actorMappingServiceImpl.updateActor(4, sActorUpdateBuilder.done());
    }

    @Test(expected = SActorUpdateException.class)
    public final void updateActorThrowException() throws SActorUpdateException, SActorNotFoundException, SBonitaReadException, SRecorderException {
        final SActor sActor = mock(SActor.class);
        doReturn(3L).when(sActor).getId();

        final SActorUpdateBuilder sActorUpdateBuilder = BuilderFactory.get(SActorUpdateBuilderFactory.class).createNewInstance();
        sActorUpdateBuilder.updateDescription("newDescription");
        sActorUpdateBuilder.updateDisplayName("newDisplayName");

        doReturn(sActor).when(persistenceService).selectById(Matchers.<SelectByIdDescriptor<SActor>> any());
        doThrow(new SRecorderException("plop")).when(recorder).recordUpdate(any(UpdateRecord.class), any(SUpdateEvent.class));

        actorMappingServiceImpl.updateActor(3, sActorUpdateBuilder.done());
    }

    /**
     * Test method for {@link org.bonitasoft.engine.actor.mapping.impl.ActorMappingServiceImpl#deleteActors(long)}.
     *
     * @throws Exception
     */
    @Test
    public final void deleteActors() throws Exception {
        final int scopeId = 9;
        final SActor sActor = mock(SActor.class);
        doReturn(3L).when(sActor).getId();

        final List<SActorMember> sActorMembers = new ArrayList<SActorMember>();
        final SActorMember sActorMember = mock(SActorMember.class);
        doReturn(4L).when(sActorMember).getId();
        sActorMembers.add(sActorMember);

        doReturn(Arrays.asList(sActor)).doReturn(new ArrayList<SActor>()).when(persistenceService)
                .selectList(Matchers.<SelectListDescriptor<SActorMember>> any());
        doReturn(sActorMembers).doReturn(new ArrayList<SActorMember>()).when(persistenceService).selectList(SelectDescriptorBuilder.getActorMembers(3, 0, 50));
        doReturn(false).when(eventService).hasHandlers(anyString(), any(EventActionType.class));
        doNothing().when(recorder).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));
        doReturn(false).when(queriableLoggerService).isLoggable(anyString(), any(SQueriableLogSeverity.class));

        actorMappingServiceImpl.deleteActors(scopeId);
        // verifyPrivate(actorMappingServiceImpl, times(1)).invoke("deleteActor", any());
        // verifyPrivate(actorMappingServiceImpl, times(1)).invoke("removeActorMember", any());
    }

    @Test
    public final void deleteNoActorMembers() throws SBonitaReadException, SRecorderException, SActorDeletionException {
        final int scopeId = 9;
        final SActor sActor = mock(SActor.class);
        doReturn(3L).when(sActor).getId();

        final List<SActorMember> sActorMembers = new ArrayList<SActorMember>();

        doReturn(Arrays.asList(sActor)).doReturn(new ArrayList<SActor>()).when(persistenceService)
                .selectList(Matchers.<SelectListDescriptor<SActorMember>> any());
        doReturn(sActorMembers).when(persistenceService).selectList(SelectDescriptorBuilder.getActorMembers(3, 0, 50));
        doReturn(false).when(eventService).hasHandlers(anyString(), any(EventActionType.class));
        doNothing().when(recorder).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));
        doReturn(false).when(queriableLoggerService).isLoggable(anyString(), any(SQueriableLogSeverity.class));

        actorMappingServiceImpl.deleteActors(scopeId);
    }

    @Test
    public final void deleteNoActors() throws SBonitaReadException, SRecorderException, SActorDeletionException {
        final int scopeId = 9;

        doReturn(new ArrayList<SActor>()).when(persistenceService).selectList(Matchers.<SelectListDescriptor<SActorMember>> any());
        doReturn(false).when(eventService).hasHandlers(anyString(), any(EventActionType.class));
        doNothing().when(recorder).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));
        doReturn(false).when(queriableLoggerService).isLoggable(anyString(), any(SQueriableLogSeverity.class));

        actorMappingServiceImpl.deleteActors(scopeId);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.actor.mapping.impl.ActorMappingServiceImpl#deleteAllActorMembers()}.
     *
     * @throws SRecorderException
     * @throws SActorMemberDeletionException
     */
    @Test
    public final void deleteAllActorMembers() throws SRecorderException, SActorMemberDeletionException {
        doNothing().when(recorder).recordDeleteAll(any(DeleteAllRecord.class));

        actorMappingServiceImpl.deleteAllActorMembers();
    }

    @Test(expected = SActorMemberDeletionException.class)
    public final void deleteAllActorMembersThrowException() throws SRecorderException, SActorMemberDeletionException {
        doThrow(new SRecorderException("plop")).when(recorder).recordDeleteAll(any(DeleteAllRecord.class));

        actorMappingServiceImpl.deleteAllActorMembers();
    }

}
