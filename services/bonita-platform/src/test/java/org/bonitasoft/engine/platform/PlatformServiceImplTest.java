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
package org.bonitasoft.engine.platform;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.bonitasoft.engine.cache.PlatformCacheService;
import org.bonitasoft.engine.cache.SCacheException;
import org.bonitasoft.engine.commons.CollectionUtil;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.platform.exception.SPlatformNotFoundException;
import org.bonitasoft.engine.platform.exception.STenantException;
import org.bonitasoft.engine.platform.exception.STenantNotFoundException;
import org.bonitasoft.engine.platform.exception.STenantUpdateException;
import org.bonitasoft.engine.platform.impl.PlatformServiceImpl;
import org.bonitasoft.engine.platform.model.SPlatform;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.platform.model.builder.STenantUpdateBuilder;
import org.bonitasoft.engine.platform.model.builder.impl.STenantUpdateBuilderImpl;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.services.UpdateDescriptor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PlatformServiceImplTest {

    @Mock
    private PersistenceService persistenceService;

    @Mock
    private PlatformRetriever platformRetriever;

    @Mock
    private TechnicalLoggerService logger;

    @Mock
    private PlatformCacheService platformCacheService;

    @Mock
    private Recorder recorder;

    @Mock
    private DataSource datasource;

    @Mock
    private File sqlFolder;

    @InjectMocks
    private PlatformServiceImpl platformServiceImpl;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public final void getDefaultTenant() throws SBonitaException {
        final STenant sTenant = buildTenant(1L, "myTenant");
        when(persistenceService.selectOne(new SelectOneDescriptor<STenant>("getDefaultTenant", null, STenant.class)))
                .thenReturn(sTenant);

        assertEquals(sTenant, platformServiceImpl.getDefaultTenant());
    }

    @Test(expected = STenantNotFoundException.class)
    public final void getDefaultTenantNotExists() throws SBonitaException {
        when(persistenceService.selectOne(new SelectOneDescriptor<STenant>("getDefaultTenant", null, STenant.class)))
                .thenReturn(null);

        platformServiceImpl.getDefaultTenant();
    }

    @Test(expected = STenantNotFoundException.class)
    public final void getDefaultTenantThrowException() throws SBonitaException {
        when(persistenceService.selectOne(new SelectOneDescriptor<STenant>("getDefaultTenant", null, STenant.class)))
                .thenThrow(new SBonitaReadException(""));

        platformServiceImpl.getDefaultTenant();
    }

    @Test
    public final void getNumberOfTenants() throws SBonitaException {
        final long numberOfTenants = 155L;
        final Map<String, Object> emptyMap = Collections.emptyMap();
        when(persistenceService
                .selectOne(new SelectOneDescriptor<>("getNumberOfTenants", emptyMap, STenant.class, Long.class)))
                        .thenReturn(numberOfTenants);

        assertEquals(numberOfTenants, platformServiceImpl.getNumberOfTenants());
    }

    @Test(expected = STenantException.class)
    public final void getNumberOfTenantsThrowException() throws SBonitaException {
        final Map<String, Object> emptyMap = Collections.emptyMap();
        when(persistenceService
                .selectOne(new SelectOneDescriptor<>("getNumberOfTenants", emptyMap, STenant.class, Long.class)))
                        .thenThrow(
                                new SBonitaReadException(""));

        platformServiceImpl.getNumberOfTenants();
    }

    @Test
    public final void getNumberOfTenantsWithOptions() throws SBonitaException {
        final long numberOfTenants = 155L;
        final QueryOptions options = getQueryOptions();
        when(persistenceService.getNumberOfEntities(STenant.class, options, null)).thenReturn(numberOfTenants);

        assertEquals(numberOfTenants, platformServiceImpl.getNumberOfTenants(options));
    }

    @Test(expected = SBonitaReadException.class)
    public final void getNumberOfTenantsWithOptionsThrowException() throws SBonitaException {
        final QueryOptions options = getQueryOptions();
        when(persistenceService.getNumberOfEntities(STenant.class, options, null))
                .thenThrow(new SBonitaReadException(""));

        platformServiceImpl.getNumberOfTenants(options);
    }

    @Test
    public final void getPlatform_should_return_platform_from_cache_if_available() throws SBonitaException {
        final SPlatform sPlatform = buildPlatform();
        when(platformCacheService.get(anyString(), anyString())).thenReturn(sPlatform);

        assertEquals(sPlatform, platformServiceImpl.getPlatform());
        verifyNoInteractions(platformRetriever);
    }

    @Test
    public final void getPlatform_should_throw_SPlatformNotFoundException_when_platformRetriever_throws_SPlatformNotFoundException()
            throws SBonitaException {
        //given
        SPlatformNotFoundException exception = new SPlatformNotFoundException("Not found");
        given(platformRetriever.getPlatform()).willThrow(exception);

        //then
        expectedException.expect(SPlatformNotFoundException.class);
        expectedException.expectMessage(equalTo("Not found"));

        //when
        platformServiceImpl.getPlatform();
    }

    @Test(expected = SPlatformNotFoundException.class)
    public final void getPlatform_should_throw_SPlatformNotFoundExcpetion_when_cacheSercie_throws_Exception()
            throws SBonitaException {
        when(platformCacheService.get(anyString(), anyString())).thenThrow(new SCacheException(""));

        platformServiceImpl.getPlatform();
    }

    @Test
    public final void getTenantById() throws SBonitaException {
        final STenant sTenant = buildTenant(15L, "tenant1");
        when(persistenceService.selectById(new SelectByIdDescriptor<>(STenant.class, 15L))).thenReturn(sTenant);

        assertEquals(sTenant, platformServiceImpl.getTenant(15L));
    }

    @Test(expected = STenantNotFoundException.class)
    public final void getTenantByIdNotExists() throws SBonitaException {
        when(persistenceService.selectById(new SelectByIdDescriptor<>(STenant.class, 15L))).thenReturn(null);

        platformServiceImpl.getTenant(15L);
    }

    @Test(expected = STenantNotFoundException.class)
    public final void getTenantByIdThrowException() throws SBonitaException {
        when(persistenceService.selectById(new SelectByIdDescriptor<>(STenant.class, 15L)))
                .thenThrow(new SBonitaReadException(""));

        platformServiceImpl.getTenant(15L);
    }

    @Test
    public final void getTenantByName() throws SBonitaException {
        final STenant sTenant = buildTenant(486, "name");
        final Map<String, Object> parameters = CollectionUtil.buildSimpleMap(STenant.NAME, "name");
        when(persistenceService
                .selectOne(new SelectOneDescriptor<STenant>("getTenantByName", parameters, STenant.class)))
                        .thenReturn(sTenant);

        assertEquals(sTenant, platformServiceImpl.getTenantByName("name"));
    }

    @Test(expected = STenantNotFoundException.class)
    public final void getTenantByNameNotExists() throws SBonitaException {
        final Map<String, Object> parameters = CollectionUtil.buildSimpleMap(STenant.NAME, "name");
        when(persistenceService
                .selectOne(new SelectOneDescriptor<STenant>("getTenantByName", parameters, STenant.class)))
                        .thenReturn(null);

        platformServiceImpl.getTenantByName("name");
    }

    @Test(expected = STenantNotFoundException.class)
    public final void getTenantByNameThrowException() throws SBonitaException {
        final Map<String, Object> parameters = CollectionUtil.buildSimpleMap(STenant.NAME, "name");
        when(persistenceService
                .selectOne(new SelectOneDescriptor<STenant>("getTenantByName", parameters, STenant.class))).thenThrow(
                        new SBonitaReadException(""));

        platformServiceImpl.getTenantByName("name");
    }

    @Test
    public final void getTenantsByIds() throws SBonitaException {
        final List<STenant> sTenants = new ArrayList<>();
        sTenants.add(buildTenant(15, "name"));
        final QueryOptions options = getQueryOptions();
        final List<Long> ids = Collections.singletonList(15L);
        final Map<String, Object> parameters = CollectionUtil.buildSimpleMap("ids", ids);
        when(persistenceService
                .selectList(new SelectListDescriptor<STenant>("getTenantsByIds", parameters, STenant.class, options)))
                        .thenReturn(sTenants);

        assertEquals(sTenants, platformServiceImpl.getTenants(ids, options));
    }

    private QueryOptions getQueryOptions() {
        return new QueryOptions(0, 10, Collections.singletonList(new OrderByOption(STenant.class, STenant.ID,
                OrderByType.DESC)));
    }

    @Test(expected = STenantNotFoundException.class)
    public final void getTenantsByIdsNotExists() throws SBonitaException {
        final List<STenant> sTenants = new ArrayList<>();
        sTenants.add(buildTenant(15, "name"));
        final QueryOptions options = getQueryOptions();
        final List<Long> ids = Arrays.asList(15L, 32L);
        final Map<String, Object> parameters = CollectionUtil.buildSimpleMap("ids", ids);
        when(persistenceService
                .selectList(new SelectListDescriptor<STenant>("getTenantsByIds", parameters, STenant.class, options)))
                        .thenReturn(sTenants);

        platformServiceImpl.getTenants(ids, options);
    }

    @Test(expected = STenantException.class)
    public final void getTenantsByIdsThrowException() throws SBonitaException {
        final QueryOptions options = getQueryOptions();
        final List<Long> ids = Collections.singletonList(15L);
        final Map<String, Object> parameters = CollectionUtil.buildSimpleMap("ids", ids);
        when(persistenceService
                .selectList(new SelectListDescriptor<STenant>("getTenantsByIds", parameters, STenant.class, options)))
                        .thenThrow(
                                new SBonitaReadException(""));

        platformServiceImpl.getTenants(ids, options);
    }

    @Test
    public final void getTenantsWithOptions() throws SBonitaException {
        final List<STenant> sTenants = new ArrayList<>();
        sTenants.add(buildTenant(48, "name"));
        final QueryOptions options = getQueryOptions();
        when(persistenceService
                .selectList(new SelectListDescriptor<STenant>("getTenants", null, STenant.class, options)))
                        .thenReturn(sTenants);

        assertEquals(sTenants, platformServiceImpl.getTenants(options));
    }

    @Test(expected = STenantException.class)
    public final void getTenantsWithOptionsThrowException() throws SBonitaException {
        final QueryOptions options = getQueryOptions();
        when(persistenceService
                .selectList(new SelectListDescriptor<STenant>("getTenants", null, STenant.class, options))).thenThrow(
                        new SBonitaReadException(""));

        platformServiceImpl.getTenants(options);
    }

    @Test
    public final void isPlatformCreated_already_in_cache() throws SBonitaException {
        final SPlatform sPlatform = buildPlatform();
        when(platformCacheService.get(anyString(), anyString())).thenReturn(sPlatform);

        assertTrue(platformServiceImpl.isPlatformCreated());
    }

    @Test
    public final void isPlatformCreated_should_return_false_when_platform_is_not_in_cache_neither_in_db()
            throws SBonitaException {
        // given
        when(platformCacheService.get(anyString(), anyString())).thenReturn(null);
        given(platformRetriever.getPlatform()).willThrow(new SPlatformNotFoundException("not found"));

        // when then
        assertFalse(platformServiceImpl.isPlatformCreated());
        verify(platformRetriever, times(1)).getPlatform();
    }

    @Test
    public final void isPlatformCreated() throws SBonitaException {
        final SPlatform sPlatform = buildPlatform();
        when(platformCacheService.get(anyString(), anyString())).thenReturn(sPlatform);

        assertTrue(platformServiceImpl.isPlatformCreated());
    }

    @Test
    public final void isPlatformCreated_false_when_cache_exception() throws SBonitaException {
        // given
        when(platformCacheService.get(anyString(), anyString())).thenThrow(new SCacheException(""));

        // whne then
        assertFalse(platformServiceImpl.isPlatformCreated());
    }

    @Test
    public final void searchTenants() throws SBonitaException {
        final List<STenant> sTenants = new ArrayList<>();
        sTenants.add(buildTenant(87, "tenant"));
        final QueryOptions options = getQueryOptions();
        when(persistenceService.searchEntity(STenant.class, options, null)).thenReturn(sTenants);

        assertEquals(sTenants, platformServiceImpl.searchTenants(options));
    }

    @Test(expected = SBonitaReadException.class)
    public final void searchTenantsThrowException() throws SBonitaException {
        final QueryOptions options = getQueryOptions();
        when(persistenceService.searchEntity(STenant.class, options, null)).thenThrow(new SBonitaReadException(""));

        platformServiceImpl.searchTenants(options);
    }

    @Test
    public void updateTheTenantUsingTheSameName() throws SBonitaException {
        // Given
        final STenant tenant = buildTenant(15, "tenantName");
        final STenantUpdateBuilder updateDescriptor = new STenantUpdateBuilderImpl(new EntityUpdateDescriptor());
        updateDescriptor.setName("tenant1");
        final EntityUpdateDescriptor descriptor = updateDescriptor.done();
        final UpdateRecord updateRecord = UpdateRecord.buildSetFields(tenant, descriptor);

        final Map<String, Object> parameters = CollectionUtil.buildSimpleMap(STenant.NAME, "name");
        when(persistenceService
                .selectOne(new SelectOneDescriptor<STenant>("getTenantByName", parameters, STenant.class)))
                        .thenReturn(tenant);

        // When
        platformServiceImpl.updateTenant(tenant, updateDescriptor.done());

        // Then
        verify(recorder).recordUpdate(eq(updateRecord), anyString());
    }

    @Test(expected = STenantUpdateException.class)
    public void updateAnotherTenantUsingAnExistingName() throws SBonitaException {
        final String tenantName = "tenantName";
        final STenant tenant = buildTenant(15, tenantName);
        final STenant actual = buildTenant(45, tenantName);
        final Map<String, Object> parameters = CollectionUtil.buildSimpleMap(STenant.NAME, "name");
        when(persistenceService
                .selectOne(new SelectOneDescriptor<STenant>("getTenantByName", parameters, STenant.class)))
                        .thenReturn(actual);

        final STenantUpdateBuilder updateDescriptor = new STenantUpdateBuilderImpl(new EntityUpdateDescriptor());
        updateDescriptor.setName(tenantName);

        platformServiceImpl.updateTenant(tenant, updateDescriptor.done());
    }

    @Test
    public void pause_should_update_tenant_state_to_PAUSED() throws SBonitaException {
        doReturn(new STenant()).when(persistenceService)
                .selectById(argThat(s -> s.getId() == 99 && s.getEntityType() == STenant.class));

        platformServiceImpl.pauseTenant(99L);

        verify(persistenceService).update(Mockito.<UpdateDescriptor> argThat(u -> updateOnlyStatus(STenant.PAUSED, u)));
    }

    @Test
    public void activateTenant_should_update_tenant_state_to_ACTIVATED() throws SBonitaException {
        doReturn(new STenant()).when(persistenceService)
                .selectById(argThat(s -> s.getId() == 99 && s.getEntityType() == STenant.class));

        platformServiceImpl.activateTenant(99L);

        verify(persistenceService)
                .update(Mockito.<UpdateDescriptor> argThat(u -> updateOnlyStatus(STenant.ACTIVATED, u)));
    }

    @Test
    public void deactivateTenant_should_update_tenant_state_to_DEACTIVATED() throws SBonitaException {
        doReturn(new STenant()).when(persistenceService)
                .selectById(argThat(s -> s.getId() == 99 && s.getEntityType() == STenant.class));

        platformServiceImpl.deactivateTenant(99L);

        verify(persistenceService)
                .update(Mockito.<UpdateDescriptor> argThat(u -> updateOnlyStatus(STenant.DEACTIVATED, u)));
    }

    private boolean updateOnlyStatus(String status, UpdateDescriptor u) {
        return u.getFields().size() == 1
                && u.getFields().containsKey(STenant.STATUS)
                && u.getFields().containsValue(status);
    }

    private STenant buildTenant(final long id, final String name) {
        final STenant tenant = new STenant(name, "me", 468786L, "ACTIVATED", true);
        tenant.setId(id);
        return tenant;
    }

    private SPlatform buildPlatform() {
        return new SPlatform("1.0", "0.5", "me", 654687344687645L);
    }

}
