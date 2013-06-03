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
package org.bonitasoft.engine.platform.test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.platform.SPlatformNotFoundException;
import org.bonitasoft.engine.platform.STenantException;
import org.bonitasoft.engine.platform.STenantNotFoundException;
import org.bonitasoft.engine.platform.impl.PlatformServiceImpl;
import org.bonitasoft.engine.platform.model.SPlatform;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.platform.model.builder.STenantBuilder;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.services.TenantPersistenceService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Celine Souchet
 * 
 */
public class PlatformServiceImplTest {

    private PersistenceService persistenceService;

    private List<TenantPersistenceService> tenantPersistenceServices;

    private STenantBuilder tenantBuilder;

    private TechnicalLoggerService logger;

    private PlatformServiceImpl platformServiceImpl;

    @Before
    public void setUp() throws Exception {
        persistenceService = mock(PersistenceService.class);
        tenantPersistenceServices = new ArrayList<TenantPersistenceService>();
        tenantBuilder = mock(STenantBuilder.class);
        logger = mock(TechnicalLoggerService.class);
        platformServiceImpl = new PlatformServiceImpl(persistenceService, tenantPersistenceServices, tenantBuilder, logger);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.platform.impl.PlatformServiceImpl#getDefaultTenant()}.
     */
    @Test
    public final void getDefaultTenant() throws SBonitaReadException, STenantNotFoundException {
        final STenant sTenant = mock(STenant.class);
        when(persistenceService.selectOne(any(SelectOneDescriptor.class))).thenReturn(sTenant);

        Assert.assertEquals(sTenant, platformServiceImpl.getDefaultTenant());
    }

    @Test(expected = STenantNotFoundException.class)
    public final void getDefaultTenantNotExists() throws SBonitaReadException, STenantNotFoundException {
        when(persistenceService.selectOne(any(SelectOneDescriptor.class))).thenReturn(null);

        platformServiceImpl.getDefaultTenant();
    }

    @Test(expected = STenantNotFoundException.class)
    public final void getDefaultTenantWithException() throws SBonitaReadException, STenantNotFoundException {
        when(persistenceService.selectOne(any(SelectOneDescriptor.class))).thenThrow(new SBonitaReadException(""));

        platformServiceImpl.getDefaultTenant();
    }

    /**
     * Test method for {@link org.bonitasoft.engine.platform.impl.PlatformServiceImpl#getNumberOfTenants()}.
     */
    @Test
    public final void getNumberOfTenants() throws SBonitaReadException, STenantException {
        final long numberOfTenants = 155L;
        when(persistenceService.selectOne(any(SelectOneDescriptor.class))).thenReturn(numberOfTenants);

        Assert.assertEquals(numberOfTenants, platformServiceImpl.getNumberOfTenants());
    }

    @Test(expected = STenantException.class)
    public final void getNumberOfTenantsWithException() throws SBonitaReadException, STenantException {
        when(persistenceService.selectOne(any(SelectOneDescriptor.class))).thenThrow(new SBonitaReadException(""));

        platformServiceImpl.getNumberOfTenants();
    }

    /**
     * Test method for {@link org.bonitasoft.engine.platform.impl.PlatformServiceImpl#getNumberOfTenants(org.bonitasoft.engine.persistence.QueryOptions)}.
     */
    @Test
    public final void getNumberOfTenantsWithQueryOptions() throws SBonitaReadException, SBonitaSearchException {
        final long numberOfTenants = 155L;
        final QueryOptions options = new QueryOptions(0, 10);
        when(persistenceService.getNumberOfEntities(STenant.class, options, null)).thenReturn(numberOfTenants);

        Assert.assertEquals(numberOfTenants, platformServiceImpl.getNumberOfTenants(options));
    }

    @Test(expected = SBonitaSearchException.class)
    public final void getNumberOfTenantsWithQueryOptionsWithException() throws SBonitaReadException, SBonitaSearchException {
        final QueryOptions options = new QueryOptions(0, 10);
        when(persistenceService.getNumberOfEntities(STenant.class, options, null)).thenThrow(new SBonitaReadException(""));

        platformServiceImpl.getNumberOfTenants(options);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.platform.impl.PlatformServiceImpl#getPlatform()}.
     */
    @Test
    public final void getPlatform() throws SBonitaReadException, SPlatformNotFoundException {
        final SPlatform sPlatform = mock(SPlatform.class);
        when(persistenceService.selectOne(any(SelectOneDescriptor.class))).thenReturn(sPlatform);

        Assert.assertEquals(sPlatform, platformServiceImpl.getPlatform());
    }

    @Test(expected = SPlatformNotFoundException.class)
    public final void getPlatformNotExists() throws SBonitaReadException, SPlatformNotFoundException {
        when(persistenceService.selectOne(any(SelectOneDescriptor.class))).thenReturn(null);

        platformServiceImpl.getPlatform();
    }

    @Test(expected = SPlatformNotFoundException.class)
    public final void getPlatformWithException() throws SBonitaReadException, SPlatformNotFoundException {
        when(persistenceService.selectOne(any(SelectOneDescriptor.class))).thenThrow(new SBonitaReadException(""));

        platformServiceImpl.getPlatform();
    }

    /**
     * Test method for {@link org.bonitasoft.engine.platform.impl.PlatformServiceImpl#getTenant(long)}.
     */
    @Test
    public final void getTenantById() throws SBonitaReadException, STenantNotFoundException {
        final STenant sTenant = mock(STenant.class);
        when(persistenceService.selectById(any(SelectByIdDescriptor.class))).thenReturn(sTenant);

        Assert.assertEquals(sTenant, platformServiceImpl.getTenant(15L));
    }

    @Test(expected = STenantNotFoundException.class)
    public final void getTenantByIdNotExists() throws SBonitaReadException, STenantNotFoundException {
        when(persistenceService.selectById(any(SelectByIdDescriptor.class))).thenReturn(null);

        platformServiceImpl.getTenant(15L);
    }

    @Test(expected = STenantNotFoundException.class)
    public final void getTenantByIdWithException() throws SBonitaReadException, STenantNotFoundException {
        when(persistenceService.selectById(any(SelectByIdDescriptor.class))).thenThrow(new SBonitaReadException(""));

        platformServiceImpl.getTenant(15L);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.platform.impl.PlatformServiceImpl#getTenants(java.util.Collection, org.bonitasoft.engine.persistence.QueryOptions)}.
     */
    @Test
    public final void getTenantsByIds() throws SBonitaReadException, STenantNotFoundException, STenantException {
        final List<STenant> sTenants = new ArrayList<STenant>();
        sTenants.add(mock(STenant.class));
        final QueryOptions options = new QueryOptions(0, 10);
        when(persistenceService.selectList(any(SelectListDescriptor.class))).thenReturn(sTenants);

        Assert.assertEquals(sTenants, platformServiceImpl.getTenants(Collections.singletonList(15L), options));
    }

    @Test(expected = STenantNotFoundException.class)
    public final void getTenantByIdsNotExists() throws SBonitaReadException, STenantNotFoundException, STenantException {
        final List<STenant> sTenants = new ArrayList<STenant>();
        sTenants.add(mock(STenant.class));
        final QueryOptions options = new QueryOptions(0, 10);
        when(persistenceService.selectList(any(SelectListDescriptor.class))).thenReturn(sTenants);

        final List<Long> ids = new ArrayList<Long>();
        ids.add(15L);
        ids.add(32L);
        platformServiceImpl.getTenants(ids, options);
    }

    @Test(expected = STenantException.class)
    public final void getTenantByIdsWithException() throws SBonitaReadException, STenantNotFoundException, STenantException {
        final QueryOptions options = new QueryOptions(0, 10);
        when(persistenceService.selectList(any(SelectListDescriptor.class))).thenThrow(new SBonitaReadException(""));

        platformServiceImpl.getTenants(Collections.singletonList(15L), options);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.platform.impl.PlatformServiceImpl#createPlatformTables()}.
     */
    @Test
    public final void createPlatformTables() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.platform.impl.PlatformServiceImpl#createPlatform(org.bonitasoft.engine.platform.model.SPlatform)}.
     */
    @Test
    public final void createPlatform() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.platform.impl.PlatformServiceImpl#createTenant(org.bonitasoft.engine.platform.model.STenant)}.
     */
    @Test
    public final void createTenant() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.platform.impl.PlatformServiceImpl#createTenantTables()}.
     */
    @Test
    public final void createTenantTables() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.platform.impl.PlatformServiceImpl#initializePlatformStructure()}.
     */
    @Test
    public final void initializePlatformStructure() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.platform.impl.PlatformServiceImpl#deletePlatform()}.
     */
    @Test
    public final void deletePlatform() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.platform.impl.PlatformServiceImpl#deletePlatformTables()}.
     */
    @Test
    public final void deletePlatformTables() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.platform.impl.PlatformServiceImpl#deleteTenant(long)}.
     */
    @Test
    public final void deleteTenant() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.platform.impl.PlatformServiceImpl#deleteTenantTables()}.
     */
    @Test
    public final void deleteTenantTables() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.platform.impl.PlatformServiceImpl#deleteTenantObjects(long)}.
     */
    @Test
    public final void deleteTenantObjects() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.platform.impl.PlatformServiceImpl#isPlatformCreated()}.
     */
    @Test
    public final void isPlatformCreated() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.platform.impl.PlatformServiceImpl#getTenantByName(java.lang.String)}.
     */
    @Test
    public final void getTenantByName() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.platform.impl.PlatformServiceImpl#getTenants(org.bonitasoft.engine.persistence.QueryOptions)}.
     */
    @Test
    public final void getTenantsQueryOptions() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.platform.impl.PlatformServiceImpl#updatePlatform(org.bonitasoft.engine.platform.model.SPlatform, org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor)}
     * .
     */
    @Test
    public final void updatePlatform() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.platform.impl.PlatformServiceImpl#updateTenant(org.bonitasoft.engine.platform.model.STenant, org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor)}
     * .
     */
    @Test
    public final void updateTenant() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.platform.impl.PlatformServiceImpl#activateTenant(long)}.
     */
    @Test
    public final void activateTenant() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.platform.impl.PlatformServiceImpl#deactiveTenant(long)}.
     */
    @Test
    public final void deactiveTenant() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.platform.impl.PlatformServiceImpl#getTenants(int, int, java.lang.String, org.bonitasoft.engine.persistence.OrderByType)}.
     */
    @Test
    public final void getTenantsIntIntStringOrderByType() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.platform.impl.PlatformServiceImpl#isTenantActivated(org.bonitasoft.engine.platform.model.STenant)}.
     */
    @Test
    public final void isTenantActivated() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.platform.impl.PlatformServiceImpl#searchTenants(org.bonitasoft.engine.persistence.QueryOptions)}.
     */
    @Test
    public final void searchTenants() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.platform.impl.PlatformServiceImpl#cleanTenantTables()}.
     */
    @Test
    public final void cleanTenantTables() {
        // TODO : Not yet implemented
    }

}
