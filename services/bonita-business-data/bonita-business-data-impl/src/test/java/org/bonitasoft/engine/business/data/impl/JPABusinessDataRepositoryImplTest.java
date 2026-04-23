/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.business.data.impl;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

import org.bonitasoft.engine.bdm.Entity;
import org.bonitasoft.engine.business.data.BusinessDataModelRepository;
import org.bonitasoft.engine.business.data.DataRetentionBdmTrackingService;
import org.bonitasoft.engine.business.data.SBusinessDataNotFoundException;
import org.bonitasoft.engine.business.data.SDataRetentionBdmTrackingException;
import org.bonitasoft.engine.classloader.ClassLoaderIdentifier;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SBonitaRuntimeException;
import org.bonitasoft.engine.commons.exceptions.SRetryableException;
import org.bonitasoft.engine.transaction.STransactionNotFoundException;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JPABusinessDataRepositoryImplTest {

    private static final long PRIMARY_KEY_1 = 1L;

    private JPABusinessDataRepositoryImpl repository;

    @Mock
    private UserTransactionService transactionService;

    @Mock
    private BusinessDataModelRepository businessDataModelRepository;

    @Mock
    private Map<String, Object> configuration;

    @Mock
    private ClassLoaderService classLoaderService;

    @Mock
    private DataRetentionBdmTrackingService bdmTrackingService;

    @Mock
    EntityManager manager;

    private JPABusinessDataRepositoryImpl realJPABusinessDataRepository;

    @BeforeEach
    void setUp() {
        realJPABusinessDataRepository = new JPABusinessDataRepositoryImpl(transactionService,
                businessDataModelRepository, configuration, classLoaderService, bdmTrackingService);
        repository = spy(
                realJPABusinessDataRepository);
        doReturn(manager).when(repository).getEntityManager();
        doReturn(true).when(businessDataModelRepository).isBDMDeployed();
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
        doReturn(criteriaBuilder).when(manager).getCriteriaBuilder();
        CriteriaQuery criteriaQuery = mock(CriteriaQuery.class);
        doReturn(criteriaQuery).when(criteriaBuilder).createQuery(any(Class.class));
        doReturn(criteriaQuery).when(criteriaQuery).select(any(Selection.class));
        Root root = mock(Root.class);
        doReturn(root).when(criteriaQuery).from(any(Class.class));
        doReturn(mock(Path.class)).when(root).get(anyString());
    }

    @Test
    void should_constructor_add_listener_on_classloader() {
        //then
        verify(classLoaderService).addListener(ClassLoaderIdentifier.TENANT, realJPABusinessDataRepository);
    }

    @Test
    void should_stop_close_entityManagerFactory() {
        // given
        EntityManagerFactory entityManagerFactory = mock(EntityManagerFactory.class);
        doReturn(entityManagerFactory).when(repository).getEntityManagerFactory();
        //when
        repository.stop();
        //then
        verify(entityManagerFactory).close();
    }

    @Test
    void should_onUpdate_recreate_the_entity_manager_factory() {
        //given
        EntityManagerFactory entityManagerFactory = mock(EntityManagerFactory.class);
        doReturn(entityManagerFactory).when(repository).createEntityManagerFactory();
        repository.start();
        //when
        repository.onUpdate(null);
        //then
        verify(entityManagerFactory).close();
        verify(repository, times(2)).createEntityManagerFactory();
    }

    @Test
    void onUpdate_should_not_throw_NPE_if_entity_manager_factory_is_null_but_still_recreate_the_factory() {
        //given
        EntityManagerFactory entityManagerFactory = mock(EntityManagerFactory.class);
        doReturn(entityManagerFactory).when(repository).createEntityManagerFactory();
        //when
        // entityManagerFactory == null here
        repository.onUpdate(mock(ClassLoader.class));
        //then
        verifyNoInteractions(entityManagerFactory);
        verify(repository, times(1)).createEntityManagerFactory();
    }

    @Test
    void findById_should_not_detach_entities() throws Exception {
        final Address address1 = new Address(PRIMARY_KEY_1);
        doReturn(address1).when(manager).find(Address.class, PRIMARY_KEY_1);

        final Address result = repository.findById(Address.class, PRIMARY_KEY_1);

        assertThat(result).isEqualTo(address1);
        verify(manager, never()).detach(any(Address.class));
    }

    @Test
    void findById_should_throw_an_exception_when_not_found() {
        doReturn(null).when(manager).find(Address.class, PRIMARY_KEY_1);

        assertThatExceptionOfType(SBusinessDataNotFoundException.class)
                .isThrownBy(() -> repository.findById(Address.class, PRIMARY_KEY_1));
    }

    @Test
    void findById_should_throw_an_exception_with_a_null_identifier() {
        assertThatExceptionOfType(SBusinessDataNotFoundException.class)
                .isThrownBy(() -> repository.findById(Address.class, null));
    }

    @Test
    void findById_should_throw_retryable_when_persistenceException() {
        //given
        doThrow(PersistenceException.class).when(manager).find(Address.class, PRIMARY_KEY_1);

        //when/then
        assertThatExceptionOfType(SRetryableException.class)
                .isThrownBy(() -> repository.findById(Address.class, PRIMARY_KEY_1));
    }

    @Test
    void findByIds_should_throw_retryable_when_persistenceException() {
        //given
        doThrow(PersistenceException.class).when(manager).createQuery(any(CriteriaQuery.class));

        //when/then
        assertThatExceptionOfType(SRetryableException.class)
                .isThrownBy(() -> repository.findByIds(Address.class, Collections.singletonList(PRIMARY_KEY_1)));
    }

    @Test
    void findByIdentifiers_should_throw_retryable_when_persistenceException() {
        //given
        doThrow(PersistenceException.class).when(manager).find(Address.class, PRIMARY_KEY_1);

        //when/then
        assertThatExceptionOfType(SRetryableException.class)
                .isThrownBy(
                        () -> repository.findByIdentifiers(Address.class, Collections.singletonList(PRIMARY_KEY_1)));
    }

    @Test
    void findByNamedQuery_should_throw_retryable_when_persistenceException() {
        //given
        TypedQuery typedQuery = mock(TypedQuery.class);
        doThrow(PersistenceException.class).when(typedQuery).getSingleResult();
        doReturn(typedQuery).when(manager).createNamedQuery(anyString(), any(Class.class));

        //when/then
        assertThatExceptionOfType(SRetryableException.class)
                .isThrownBy(() -> repository.findByNamedQuery("queryName", Address.class,
                        Collections.<String, Serializable> emptyMap()));
    }

    @Test
    void findListByNamedQuery_should_throw_retryable_when_persistenceException() {
        //given
        TypedQuery typedQuery = mock(TypedQuery.class);
        doThrow(PersistenceException.class).when(typedQuery).getResultList();
        doReturn(typedQuery).when(manager).createNamedQuery(anyString(), any(Class.class));

        //when/then
        assertThatExceptionOfType(SRetryableException.class)
                .isThrownBy(() -> repository.findListByNamedQuery("queryName", Address.class,
                        Collections.<String, Serializable> emptyMap(), 0, 10));
    }

    @Test
    void find_should_throw_retryable_when_persistenceException() {
        //given
        TypedQuery typedQuery = mock(TypedQuery.class);
        doThrow(PersistenceException.class).when(typedQuery).getSingleResult();
        doReturn(typedQuery).when(manager).createQuery(anyString(), any(Class.class));

        //when/then
        assertThatExceptionOfType(SRetryableException.class)
                .isThrownBy(() -> repository.find(Address.class, "the query as string",
                        Collections.<String, Serializable> emptyMap()));
    }

    @Test
    void findList_should_throw_retryable_when_persistenceException() {
        //given
        TypedQuery typedQuery = mock(TypedQuery.class);
        doThrow(PersistenceException.class).when(typedQuery).getResultList();
        doReturn(typedQuery).when(manager).createQuery(anyString(), any(Class.class));

        //when/then
        assertThatExceptionOfType(SRetryableException.class)
                .isThrownBy(() -> repository.findList(Address.class, "the query as string",
                        Collections.<String, Serializable> emptyMap(), 0, 10));
    }

    @Test
    void persist_should_throw_retryable_exception_in_case_of_persistenceException() {
        //given
        doThrow(PersistenceException.class).when(manager).persist(any(Address.class));

        //when/then
        assertThatExceptionOfType(SRetryableException.class)
                .isThrownBy(() -> repository.persist(new Address(12)));
    }

    @Test
    void persist_should_track_creation_for_new_entity() throws SBonitaException {
        //given
        var entity = new EntityPojo(); // persistenceId == null → new entity
        doAnswer(invocation -> {
            ((EntityPojo) invocation.getArgument(0)).setPersistenceId(42L);
            return null;
        }).when(manager).persist(entity);

        //when
        repository.persist(entity);

        //then
        verify(bdmTrackingService).create(42L, EntityPojo.class.getName());
    }

    @Test
    void persist_should_upsert_tracking_for_existing_entity() throws SBonitaException {
        //given
        var entity = new EntityPojo(99L); // persistenceId != null → existing entity

        //when
        repository.persist(entity);

        //then
        verify(bdmTrackingService).upsert(99L, EntityPojo.class.getName());
        verify(bdmTrackingService, never()).create(anyLong(), anyString());
    }

    @Test
    void merge_should_track_creation_for_new_entity() throws SBonitaException {
        //given
        var entity = new EntityPojo(); // persistenceId == null → new entity
        var mergedEntity = new EntityPojo(55L);
        when(manager.merge(entity)).thenReturn(mergedEntity);

        //when
        repository.merge(entity);

        //then
        verify(bdmTrackingService).create(55L, EntityPojo.class.getName());
    }

    @Test
    void merge_should_upsert_tracking_for_existing_entity() throws SBonitaException {
        //given
        var entity = new EntityPojo(10L); // persistenceId != null → existing entity
        when(manager.merge(entity)).thenReturn(entity);

        //when
        repository.merge(entity);

        //then
        verify(bdmTrackingService).upsert(10L, EntityPojo.class.getName());
        verify(bdmTrackingService, never()).create(anyLong(), anyString());
    }

    @Test
    void persist_null_entity_should_not_track() throws SBonitaException {
        //when
        repository.persist(null);

        //then
        verify(manager, never()).persist(any());
        verify(bdmTrackingService, never()).create(anyLong(), anyString());
    }

    @Test
    void merge_null_entity_should_not_track() throws SBonitaException {
        //when
        repository.merge(null);

        //then
        verify(manager, never()).merge(any());
        verify(bdmTrackingService, never()).create(anyLong(), anyString());
    }

    @Test
    void merge_should_use_original_classname_not_hibernate_proxy() throws SBonitaException {
        // given — input is a plain EntityPojo, but merge() returns a different type (simulating proxy)
        var entity = new EntityPojo(); // persistenceId == null → new entity
        var proxyResult = mock(Entity.class);
        when(proxyResult.getPersistenceId()).thenReturn(77L);
        doReturn(proxyResult).when(manager).merge(entity);

        //when
        repository.merge(entity);

        // then — dataClassname should be EntityPojo (from input), not the mock/proxy class
        verify(bdmTrackingService).create(77L, EntityPojo.class.getName());
    }

    @Test
    void persist_should_throw_SBonitaRuntimeException_when_tracking_fails() throws SBonitaException {
        //given
        var entity = new EntityPojo(); // new entity
        doAnswer(invocation -> {
            ((EntityPojo) invocation.getArgument(0)).setPersistenceId(7L);
            return null;
        }).when(manager).persist(entity);
        doThrow(new SDataRetentionBdmTrackingException("DB error"))
                .when(bdmTrackingService).create(anyLong(), anyString());

        //when-then
        assertThatThrownBy(() -> repository.persist(entity))
                .isInstanceOf(SBonitaRuntimeException.class)
                .hasMessageContaining("Failed to insert data retention tracking record")
                .hasCauseInstanceOf(SDataRetentionBdmTrackingException.class);
    }

    @Test
    void merge_should_throw_SBonitaRuntimeException_when_tracking_fails() throws SBonitaException {
        //given
        var entity = new EntityPojo(); // new entity
        var mergedEntity = new EntityPojo(8L);
        when(manager.merge(entity)).thenReturn(mergedEntity);
        doThrow(new SDataRetentionBdmTrackingException("DB error"))
                .when(bdmTrackingService).create(anyLong(), anyString());

        //when-then
        assertThatThrownBy(() -> repository.merge(entity))
                .isInstanceOf(SBonitaRuntimeException.class)
                .hasMessageContaining("Failed to insert data retention tracking record")
                .hasCauseInstanceOf(SDataRetentionBdmTrackingException.class);
    }

    @Test
    void merge_should_throw_retryable_exception_in_case_of_persistenceException() {
        //given
        doThrow(PersistenceException.class).when(manager).merge(any(Address.class));

        //when/then
        assertThatExceptionOfType(SRetryableException.class)
                .isThrownBy(() -> repository.merge(new Address(12)));
    }

    @Test
    void remove_should_throw_retryable_exception_in_case_of_persistenceException() {
        //given
        doThrow(PersistenceException.class).when(manager).remove(any(Address.class));

        //when/then
        assertThatExceptionOfType(SRetryableException.class)
                .isThrownBy(() -> repository.remove(new Address(12)));
    }

    @Test
    void remove_should_delete_tracking_record() throws SBonitaException {
        //given
        var entity = new EntityPojo(42L);

        //when
        repository.remove(entity);

        //then
        verify(manager).remove(entity);
        verify(bdmTrackingService).delete(42L, EntityPojo.class.getName());
    }

    @Test
    void remove_null_entity_should_not_delete_tracking_record() throws SBonitaException {
        //when
        repository.remove(null);

        //then
        verify(manager, never()).remove(any());
        verify(bdmTrackingService, never()).delete(anyLong(), anyString());
    }

    @Test
    void remove_entity_without_id_should_not_delete_tracking_record() throws SBonitaException {
        //given
        var entity = new EntityPojo(); // persistenceId == null

        //when
        repository.remove(entity);

        //then
        verify(manager, never()).remove(any());
        verify(bdmTrackingService, never()).delete(anyLong(), anyString());
    }

    @Test
    void remove_should_not_fail_when_tracking_deletion_fails_with_checked_exception() throws SBonitaException {
        //given
        var entity = new EntityPojo(42L);
        doThrow(new SDataRetentionBdmTrackingException("DB error"))
                .when(bdmTrackingService).delete(anyLong(), anyString());

        //when-then — should not throw
        assertThatNoException().isThrownBy(() -> repository.remove(entity));
        // entity was still removed
        verify(manager).remove(entity);
    }

    @Test
    void remove_should_not_fail_when_tracking_deletion_fails_with_runtime_exception() throws SBonitaException {
        //given
        var entity = new EntityPojo(42L);
        doThrow(new RuntimeException("unexpected error"))
                .when(bdmTrackingService).delete(anyLong(), anyString());

        //when-then — should not throw
        assertThatNoException().isThrownBy(() -> repository.remove(entity));
        // entity was still removed
        verify(manager).remove(entity);
    }

    @Test
    void removeById_should_remove_entity_and_delete_tracking_record() throws Exception {
        //given
        var entity = new EntityPojo(42L);
        var entityGraph = mock(EntityGraph.class);
        doReturn(entityGraph).when(manager).createEntityGraph(EntityPojo.class);
        var expectedHints = Map.<String, Object> of("javax.persistence.fetchgraph", entityGraph);
        when(manager.find(EntityPojo.class, 42L, expectedHints)).thenReturn(entity);

        //when
        var removed = repository.removeById(EntityPojo.class, 42L);

        //then
        assertThat(removed).isSameAs(entity);
        verify(manager).find(EntityPojo.class, 42L, expectedHints);
        verify(manager).remove(entity);
        verify(bdmTrackingService).delete(42L, EntityPojo.class.getName());
    }

    @Test
    void removeById_should_throw_when_entity_not_found() {
        //given
        var entityGraph = mock(EntityGraph.class);
        doReturn(entityGraph).when(manager).createEntityGraph(EntityPojo.class);
        when(manager.find(eq(EntityPojo.class), eq(999L), anyMap())).thenReturn(null);

        //when-then
        assertThatExceptionOfType(SBusinessDataNotFoundException.class)
                .isThrownBy(() -> repository.removeById(EntityPojo.class, 999L));
        verify(manager, never()).remove(any());
    }

    @Test
    void removeById_should_throw_retryable_on_persistence_exception() {
        //given
        var entityGraph = mock(EntityGraph.class);
        doReturn(entityGraph).when(manager).createEntityGraph(EntityPojo.class);
        when(manager.find(eq(EntityPojo.class), eq(42L), anyMap()))
                .thenThrow(new PersistenceException("db error"));

        //when-then
        assertThatExceptionOfType(SRetryableException.class)
                .isThrownBy(() -> repository.removeById(EntityPojo.class, 42L));
    }

    @Test
    void removeById_should_not_fail_when_tracking_deletion_fails() throws Exception {
        //given
        var entity = new EntityPojo(42L);
        var entityGraph = mock(EntityGraph.class);
        doReturn(entityGraph).when(manager).createEntityGraph(EntityPojo.class);
        when(manager.find(eq(EntityPojo.class), eq(42L), anyMap())).thenReturn(entity);
        doThrow(new SDataRetentionBdmTrackingException("DB error"))
                .when(bdmTrackingService).delete(anyLong(), anyString());

        //when-then — should not throw
        assertThatNoException().isThrownBy(() -> repository.removeById(EntityPojo.class, 42L));
        verify(manager).remove(entity);
    }

    @Test
    void checkParameterValue_should_transform_query_parameter_values_from_string_array_to_collection() {
        Object collection = repository
                .checkParameterValue(new String[] { "v1", "v2" });

        assertThat((Collection) collection).contains("v1", "v2");
    }

    @Test
    void checkParameterValue_should_transform_query_parameter_values_from_int_array_to_collection() {
        Object collection = repository
                .checkParameterValue(new Integer[] { 1, 2 });

        assertThat((Collection) collection).contains(1, 2);
    }

    @Test
    void checkParameterValue_should_transform_query_parameter_values_from_float_array_to_collection() {
        Object collection = repository
                .checkParameterValue(new Float[] { 1.2f, 2.0f });

        assertThat((Collection) collection).contains(1.2f, 2.0f);
    }

    @Test
    void checkParameterValue_should_transform_query_parameter_values_from_double_array_to_collection() {
        Object collection = repository
                .checkParameterValue(new Double[] { 1.2d, 2.0d });

        assertThat((Collection) collection).contains(1.2d, 2.0d);
    }

    @Test
    void checkParameterValue_should_transform_query_parameter_values_from_long_array_to_collection() {
        Object collection = repository
                .checkParameterValue(new Long[] { 12l, 23456l });

        assertThat((Collection) collection).contains(12l, 23456l);
    }

    @Test
    void checkParameterValue_should_check_supported_query_parameter_types() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> repository.checkParameterValue(new Byte[] { 0x1, 0x2 }));
    }

    // --- Defensive getEntityManager() tests (BPA-321) ---

    /**
     * Helper to inject an EntityManager into the private ThreadLocal "managers" field
     * and call the real getEntityManager() method (not the spied version).
     */
    private JPABusinessDataRepositoryImpl createRepositoryWithStaleEM(EntityManager staleEM,
            EntityManager freshEM, EntityManagerFactory emf)
            throws Exception {
        JPABusinessDataRepositoryImpl repo = spy(
                new JPABusinessDataRepositoryImpl(transactionService,
                        businessDataModelRepository, configuration, classLoaderService, bdmTrackingService));
        doReturn(emf).when(repo).getEntityManagerFactory();

        // Inject the stale EM into the private ThreadLocal
        Field managersField = JPABusinessDataRepositoryImpl.class.getDeclaredField("managers");
        managersField.setAccessible(true);
        @SuppressWarnings("unchecked")
        ThreadLocal<EntityManager> managers = (ThreadLocal<EntityManager>) managersField.get(repo);
        managers.set(staleEM);

        // The EMF will produce a fresh EM when asked
        doReturn(freshEM).when(emf).createEntityManager();

        return repo;
    }

    @Test
    void getEntityManager_should_discard_stale_EM_not_joined_to_transaction() throws Exception {
        //given
        EntityManager staleEM = mock(EntityManager.class);
        doReturn(true).when(staleEM).isOpen();
        doReturn(false).when(staleEM).isJoinedToTransaction();

        EntityManager freshEM = mock(EntityManager.class);
        EntityManagerFactory emf = mock(EntityManagerFactory.class);

        JPABusinessDataRepositoryImpl repo = createRepositoryWithStaleEM(staleEM, freshEM, emf);

        //when
        EntityManager result = repo.getEntityManager();

        //then
        verify(staleEM).close();
        verify(emf).createEntityManager();
        verify(freshEM).joinTransaction();
        assertThat(result).isSameAs(freshEM);
    }

    @Test
    void getEntityManager_should_discard_stale_EM_that_is_no_longer_open() throws Exception {
        //given
        EntityManager staleEM = mock(EntityManager.class);
        doReturn(false).when(staleEM).isOpen();

        EntityManager freshEM = mock(EntityManager.class);
        EntityManagerFactory emf = mock(EntityManagerFactory.class);

        JPABusinessDataRepositoryImpl repo = createRepositoryWithStaleEM(staleEM, freshEM, emf);

        //when
        EntityManager result = repo.getEntityManager();

        //then
        // staleEM.isOpen() returned false, so close() should NOT be called (already closed)
        verify(staleEM, never()).close();
        verify(emf).createEntityManager();
        assertThat(result).isSameAs(freshEM);
    }

    @Test
    void getEntityManager_should_discard_stale_EM_that_throws_on_state_check() throws Exception {
        //given
        EntityManager staleEM = mock(EntityManager.class);
        // Simulates a broken EM where even isOpen() throws (completely broken session)
        doThrow(new PersistenceException("Session/EntityManager is closed")).when(staleEM).isOpen();

        EntityManager freshEM = mock(EntityManager.class);
        EntityManagerFactory emf = mock(EntityManagerFactory.class);

        JPABusinessDataRepositoryImpl repo = createRepositoryWithStaleEM(staleEM, freshEM, emf);

        //when
        EntityManager result = repo.getEntityManager();

        //then
        // isOpen() threw, so closeQuietly catches the exception and moves on
        verify(staleEM, never()).close();
        verify(emf).createEntityManager();
        assertThat(result).isSameAs(freshEM);
    }

    @Test
    void getEntityManager_should_reuse_EM_when_joined_to_current_transaction() throws Exception {
        //given
        EntityManager activeEM = mock(EntityManager.class);
        doReturn(true).when(activeEM).isOpen();
        doReturn(true).when(activeEM).isJoinedToTransaction();

        EntityManagerFactory emf = mock(EntityManagerFactory.class);

        JPABusinessDataRepositoryImpl repo = spy(
                new JPABusinessDataRepositoryImpl(transactionService,
                        businessDataModelRepository, configuration, classLoaderService, bdmTrackingService));
        doReturn(emf).when(repo).getEntityManagerFactory();

        // Inject the active EM
        Field managersField = JPABusinessDataRepositoryImpl.class.getDeclaredField("managers");
        managersField.setAccessible(true);
        @SuppressWarnings("unchecked")
        ThreadLocal<EntityManager> managers = (ThreadLocal<EntityManager>) managersField.get(repo);
        managers.set(activeEM);

        //when
        EntityManager result = repo.getEntityManager();

        //then
        verify(emf, never()).createEntityManager();
        verify(activeEM, never()).close();
        verify(activeEM).joinTransaction();
        assertThat(result).isSameAs(activeEM);

        // cleanup ThreadLocal
        managers.remove();
    }

    @Test
    void getEntityManager_should_close_new_EM_when_registerBonitaSynchronization_throws() throws Exception {
        //given
        EntityManager freshEM = mock(EntityManager.class);
        doReturn(true).when(freshEM).isOpen();
        EntityManagerFactory emf = mock(EntityManagerFactory.class);
        doReturn(freshEM).when(emf).createEntityManager();

        JPABusinessDataRepositoryImpl repo = spy(
                new JPABusinessDataRepositoryImpl(transactionService,
                        businessDataModelRepository, configuration, classLoaderService, bdmTrackingService));
        doReturn(emf).when(repo).getEntityManagerFactory();

        doThrow(new STransactionNotFoundException("no active transaction"))
                .when(transactionService).registerBonitaSynchronization(any());

        //when
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(repo::getEntityManager)
                .withCauseInstanceOf(STransactionNotFoundException.class);

        //then
        verify(freshEM).close();
    }

    @Test
    void getEntityManager_should_cleanup_and_rethrow_when_joinTransaction_fails() throws Exception {
        //given
        EntityManager freshEM = mock(EntityManager.class);
        doReturn(true).when(freshEM).isOpen();
        doThrow(new javax.persistence.TransactionRequiredException("TX is rollback-only"))
                .when(freshEM).joinTransaction();
        EntityManagerFactory emf = mock(EntityManagerFactory.class);
        doReturn(freshEM).when(emf).createEntityManager();

        JPABusinessDataRepositoryImpl repo = spy(
                new JPABusinessDataRepositoryImpl(transactionService,
                        businessDataModelRepository, configuration, classLoaderService, bdmTrackingService));
        doReturn(emf).when(repo).getEntityManagerFactory();

        //when + then
        assertThatExceptionOfType(javax.persistence.TransactionRequiredException.class)
                .isThrownBy(repo::getEntityManager)
                .withMessageContaining("TX is rollback-only");

        verify(freshEM).close();

        // Verify ThreadLocal was cleaned up: a second call should create a fresh EM,
        // not reuse the poisoned one
        EntityManager secondEM = mock(EntityManager.class);
        doReturn(secondEM).when(emf).createEntityManager();
        doNothing().when(secondEM).joinTransaction();

        EntityManager result = repo.getEntityManager();

        assertThat(result).isSameAs(secondEM);
        verify(emf, times(2)).createEntityManager();
    }

    class Address implements Entity {

        private static final long serialVersionUID = 2603989953326533907L;

        private final long persistenceId;

        public Address(final long persistenceId) {
            this.persistenceId = persistenceId;
        }

        @Override
        public Long getPersistenceId() {
            return persistenceId;
        }

        @Override
        public Long getPersistenceVersion() {
            return 0L;
        }

    }

}
