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
package org.bonitasoft.engine.dependency.impl;

import static org.bonitasoft.engine.dependency.model.ScopeType.PROCESS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.dependency.SDependencyException;
import org.bonitasoft.engine.dependency.SDependencyNotFoundException;
import org.bonitasoft.engine.dependency.model.SDependency;
import org.bonitasoft.engine.dependency.model.SDependencyMapping;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Celine Souchet
 */
@RunWith(MockitoJUnitRunner.class)
public class TenantDependencyServiceTest {

    @Mock
    private ReadPersistenceService persistenceService;
    @Mock
    private Recorder recorder;
    @Mock
    private EventService eventService;
    @Mock
    private TechnicalLoggerService logger;
    @Mock
    private QueriableLoggerService queriableLoggerService;
    @InjectMocks
    private TenantDependencyService tenantDependencyService;

    /**
     * Test method for {@link TenantDependencyService#getDependency(long)}.
     */
    @Test
    public final void getDependencyById() throws SBonitaReadException, SDependencyNotFoundException {
        final SDependency sDependency = mock(SDependency.class);
        when(persistenceService.selectById(ArgumentMatchers.<SelectByIdDescriptor<SDependency>> any()))
                .thenReturn(sDependency);

        Assert.assertEquals(sDependency, tenantDependencyService.getDependency(456L));
    }

    @Test(expected = SDependencyNotFoundException.class)
    public final void getDependencyByIdNotExists() throws SBonitaReadException, SDependencyNotFoundException {
        when(persistenceService.selectById(ArgumentMatchers.<SelectByIdDescriptor<SDependency>> any()))
                .thenReturn(null);

        tenantDependencyService.getDependency(456L);
    }

    @Test(expected = SDependencyNotFoundException.class)
    public final void getDependencyByIdThrowException() throws SBonitaReadException, SDependencyNotFoundException {
        when(persistenceService.selectById(ArgumentMatchers.<SelectByIdDescriptor<SDependency>> any()))
                .thenThrow(new SBonitaReadException(""));

        tenantDependencyService.getDependency(456L);
    }

    /**
     * Test method for {@link TenantDependencyService#getDependencies(java.util.Collection)}.
     */
    @Test
    public final void getDependenciesByIds() throws SBonitaReadException, SDependencyException {
        final List<SDependency> sDependencies = new ArrayList<SDependency>();
        when(persistenceService.selectList(ArgumentMatchers.<SelectListDescriptor<SDependency>> any()))
                .thenReturn(sDependencies);

        Assert.assertEquals(sDependencies, tenantDependencyService.getDependencies(Collections.singletonList(456L)));
    }

    @Test(expected = SDependencyException.class)
    public final void getDependenciesByIdsThrowException() throws SBonitaReadException, SDependencyException {
        when(persistenceService.selectList(ArgumentMatchers.<SelectListDescriptor<SDependency>> any()))
                .thenThrow(new SBonitaReadException(""));

        tenantDependencyService.getDependencies(Collections.singletonList(456L));
    }

    /**
     * Test method for {@link TenantDependencyService#getDependencies(org.bonitasoft.engine.persistence.QueryOptions)}.
     */
    @Test
    public final void getDependenciesWithOptions() throws SBonitaReadException, SDependencyException {
        final List<SDependency> sDependencies = new ArrayList<SDependency>();
        when(persistenceService.selectList(ArgumentMatchers.<SelectListDescriptor<SDependency>> any()))
                .thenReturn(sDependencies);

        final QueryOptions options = new QueryOptions(0, 10);
        Assert.assertEquals(sDependencies, tenantDependencyService.getDependencies(options));
    }

    @Test(expected = SDependencyException.class)
    public final void getDependenciesWithOptionsThrowException() throws SBonitaReadException, SDependencyException {
        when(persistenceService.selectList(ArgumentMatchers.<SelectListDescriptor<SDependency>> any()))
                .thenThrow(new SBonitaReadException(""));

        final QueryOptions options = new QueryOptions(0, 10);
        tenantDependencyService.getDependencies(options);
    }

    @Test
    public final void getDependencyIds() throws SBonitaReadException, SDependencyException {
        final List<SDependency> sDependencies = new ArrayList<SDependency>();
        when(persistenceService.selectList(ArgumentMatchers.<SelectListDescriptor<SDependency>> any()))
                .thenReturn(sDependencies);

        Assert.assertEquals(sDependencies, tenantDependencyService.getDependencyIds(54156L, PROCESS, 1, 100));
    }

    @Test(expected = SDependencyException.class)
    public final void getDependencyIdsThrowException() throws SBonitaReadException, SDependencyException {
        when(persistenceService.selectList(ArgumentMatchers.<SelectListDescriptor<SDependency>> any()))
                .thenThrow(new SBonitaReadException(""));

        tenantDependencyService.getDependencyIds(54156L, PROCESS, 1, 100);
    }

    /**
     * Test method for
     * {@link TenantDependencyService#getDependencyMappings(long, org.bonitasoft.engine.persistence.QueryOptions)}.
     */
    @Test
    public final void getDependencyMappingsWithDependencyIdAndQueryOptions()
            throws SBonitaReadException, SDependencyException {
        final List<SDependencyMapping> sDependencyMappings = new ArrayList<SDependencyMapping>();
        when(persistenceService.selectList(ArgumentMatchers.<SelectListDescriptor<SDependencyMapping>> any()))
                .thenReturn(sDependencyMappings);

        final QueryOptions options = new QueryOptions(0, 10);
        Assert.assertEquals(sDependencyMappings, tenantDependencyService.getDependencyMappings(54156L, options));
    }

    @Test(expected = SDependencyException.class)
    public final void getDependencyMappingsWithDependencyIdAndQueryOptionsThrowException()
            throws SBonitaReadException, SDependencyException {
        when(persistenceService.selectList(ArgumentMatchers.<SelectListDescriptor<SDependency>> any()))
                .thenThrow(new SBonitaReadException(""));

        final QueryOptions options = new QueryOptions(0, 10);
        tenantDependencyService.getDependencyMappings(54156L, options);
    }

    /**
     * Test method for
     * {@link TenantDependencyService#getDependencyMappings(org.bonitasoft.engine.persistence.QueryOptions)}
     * .
     */
    @Test
    public final void getDependencyMappingsWithOptions() throws SBonitaReadException, SDependencyException {
        final List<SDependencyMapping> sDependencyMappings = new ArrayList<SDependencyMapping>();
        when(persistenceService.selectList(ArgumentMatchers.<SelectListDescriptor<SDependencyMapping>> any()))
                .thenReturn(sDependencyMappings);

        final QueryOptions options = new QueryOptions(0, 10);
        Assert.assertEquals(sDependencyMappings, tenantDependencyService.getDependencyMappings(options));
    }

    @Test(expected = SDependencyException.class)
    public final void getDependencyMappingsWithOptionsThrowException()
            throws SBonitaReadException, SDependencyException {
        when(persistenceService.selectList(ArgumentMatchers.<SelectListDescriptor<SDependency>> any()))
                .thenThrow(new SBonitaReadException(""));

        final QueryOptions options = new QueryOptions(0, 10);
        tenantDependencyService.getDependencyMappings(options);
    }

    @Test(expected = SDependencyNotFoundException.class)
    public void deleteDependencyByNonExistingNameShouldThrowSDependencyNotFoundException() throws Exception {
        when(persistenceService.selectOne(ArgumentMatchers.<SelectOneDescriptor<SDependency>> any())).thenReturn(null);
        tenantDependencyService.deleteDependency("notFound");
    }

    @Test(expected = SDependencyNotFoundException.class)
    public void deleteDependencyWithReadExceptionShouldThrowSDependencyNotFoundException() throws Exception {
        Mockito.doThrow(new SBonitaReadException("")).when(persistenceService)
                .selectOne(ArgumentMatchers.<SelectOneDescriptor<SDependency>> any());
        tenantDependencyService.deleteDependency("notFound");
    }

}
