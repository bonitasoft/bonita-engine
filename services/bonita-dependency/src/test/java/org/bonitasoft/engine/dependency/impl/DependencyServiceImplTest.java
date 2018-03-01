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
package org.bonitasoft.engine.dependency.impl;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.dependency.SDependencyException;
import org.bonitasoft.engine.dependency.SDependencyNotFoundException;
import org.bonitasoft.engine.dependency.model.SDependency;
import org.bonitasoft.engine.dependency.model.SDependencyMapping;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.service.BroadcastService;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.junit.Assert;
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
public class DependencyServiceImplTest {

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
    @Mock
    private ClassLoaderService classLoaderService;
    @Mock
    private BroadcastService broadcastService;
    @Mock
    private ReadSessionAccessor readSessionAccessor;
    @InjectMocks
    private DependencyServiceImpl dependencyServiceImpl;

    /**
     * Test method for {@link org.bonitasoft.engine.dependency.impl.DependencyServiceImpl#getDependency(long)}.
     */
    @Test
    public final void getDependencyById() throws SBonitaReadException, SDependencyNotFoundException {
        final SDependency sDependency = mock(SDependency.class);
        when(persistenceService.selectById(Matchers.<SelectByIdDescriptor<SDependency>> any())).thenReturn(sDependency);

        Assert.assertEquals(sDependency, dependencyServiceImpl.getDependency(456L));
    }

    @Test(expected = SDependencyNotFoundException.class)
    public final void getDependencyByIdNotExists() throws SBonitaReadException, SDependencyNotFoundException {
        when(persistenceService.selectById(Matchers.<SelectByIdDescriptor<SDependency>> any())).thenReturn(null);

        dependencyServiceImpl.getDependency(456L);
    }

    @Test(expected = SDependencyNotFoundException.class)
    public final void getDependencyByIdThrowException() throws SBonitaReadException, SDependencyNotFoundException {
        when(persistenceService.selectById(Matchers.<SelectByIdDescriptor<SDependency>> any())).thenThrow(new SBonitaReadException(""));

        dependencyServiceImpl.getDependency(456L);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.dependency.impl.DependencyServiceImpl#getDependencies(java.util.Collection)}.
     */
    @Test
    public final void getDependenciesByIds() throws SBonitaReadException, SDependencyException {
        final List<SDependency> sDependencies = new ArrayList<SDependency>();
        when(persistenceService.selectList(Matchers.<SelectListDescriptor<SDependency>> any())).thenReturn(sDependencies);

        Assert.assertEquals(sDependencies, dependencyServiceImpl.getDependencies(Collections.singletonList(456L)));
    }

    @Test(expected = SDependencyException.class)
    public final void getDependenciesByIdsThrowException() throws SBonitaReadException, SDependencyException {
        when(persistenceService.selectList(Matchers.<SelectListDescriptor<SDependency>> any())).thenThrow(new SBonitaReadException(""));

        dependencyServiceImpl.getDependencies(Collections.singletonList(456L));
    }

    /**
     * Test method for {@link org.bonitasoft.engine.dependency.impl.DependencyServiceImpl#getDependencies(org.bonitasoft.engine.persistence.QueryOptions)}.
     */
    @Test
    public final void getDependenciesWithOptions() throws SBonitaReadException, SDependencyException {
        final List<SDependency> sDependencies = new ArrayList<SDependency>();
        when(persistenceService.selectList(Matchers.<SelectListDescriptor<SDependency>> any())).thenReturn(sDependencies);

        final QueryOptions options = new QueryOptions(0, 10);
        Assert.assertEquals(sDependencies, dependencyServiceImpl.getDependencies(options));
    }

    @Test(expected = SDependencyException.class)
    public final void getDependenciesWithOptionsThrowException() throws SBonitaReadException, SDependencyException {
        when(persistenceService.selectList(Matchers.<SelectListDescriptor<SDependency>> any())).thenThrow(new SBonitaReadException(""));

        final QueryOptions options = new QueryOptions(0, 10);
        dependencyServiceImpl.getDependencies(options);
    }

    @Test
    public final void getDependencyIds() throws SBonitaReadException, SDependencyException {
        final List<SDependency> sDependencies = new ArrayList<SDependency>();
        when(persistenceService.selectList(Matchers.<SelectListDescriptor<SDependency>> any())).thenReturn(sDependencies);

        Assert.assertEquals(sDependencies, dependencyServiceImpl.getDependencyIds(54156L, ScopeType.PROCESS, 1, 100));
    }

    @Test(expected = SDependencyException.class)
    public final void getDependencyIdsThrowException() throws SBonitaReadException, SDependencyException {
        when(persistenceService.selectList(Matchers.<SelectListDescriptor<SDependency>> any())).thenThrow(new SBonitaReadException(""));

        dependencyServiceImpl.getDependencyIds(54156L, ScopeType.PROCESS, 1, 100);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.dependency.impl.DependencyServiceImpl#getDependencyMappings(long, org.bonitasoft.engine.persistence.QueryOptions)}.
     */
    @Test
    public final void getDependencyMappingsWithDependencyIdAndQueryOptions() throws SBonitaReadException, SDependencyException {
        final List<SDependencyMapping> sDependencyMappings = new ArrayList<SDependencyMapping>();
        when(persistenceService.selectList(Matchers.<SelectListDescriptor<SDependencyMapping>> any())).thenReturn(sDependencyMappings);

        final QueryOptions options = new QueryOptions(0, 10);
        Assert.assertEquals(sDependencyMappings, dependencyServiceImpl.getDependencyMappings(54156L, options));
    }

    @Test(expected = SDependencyException.class)
    public final void getDependencyMappingsWithDependencyIdAndQueryOptionsThrowException() throws SBonitaReadException, SDependencyException {
        when(persistenceService.selectList(Matchers.<SelectListDescriptor<SDependency>> any())).thenThrow(new SBonitaReadException(""));

        final QueryOptions options = new QueryOptions(0, 10);
        dependencyServiceImpl.getDependencyMappings(54156L, options);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.dependency.impl.DependencyServiceImpl#getDependencyMappings(org.bonitasoft.engine.persistence.QueryOptions)}
     * .
     */
    @Test
    public final void getDependencyMappingsWithOptions() throws SBonitaReadException, SDependencyException {
        final List<SDependencyMapping> sDependencyMappings = new ArrayList<SDependencyMapping>();
        when(persistenceService.selectList(Matchers.<SelectListDescriptor<SDependencyMapping>> any())).thenReturn(sDependencyMappings);

        final QueryOptions options = new QueryOptions(0, 10);
        Assert.assertEquals(sDependencyMappings, dependencyServiceImpl.getDependencyMappings(options));
    }

    @Test(expected = SDependencyException.class)
    public final void getDependencyMappingsWithOptionsThrowException() throws SBonitaReadException, SDependencyException {
        when(persistenceService.selectList(Matchers.<SelectListDescriptor<SDependency>> any())).thenThrow(new SBonitaReadException(""));

        final QueryOptions options = new QueryOptions(0, 10);
        dependencyServiceImpl.getDependencyMappings(options);
    }

    @Test(expected = SDependencyNotFoundException.class)
    public void deleteDependencyByNonExistingNameShouldThrowSDependencyNotFoundException() throws Exception {
        when(persistenceService.selectOne(Matchers.<SelectOneDescriptor<SDependency>> any())).thenReturn(null);
        dependencyServiceImpl.deleteDependency("notFound");
    }

    @Test(expected = SDependencyNotFoundException.class)
    public void deleteDependencyWithReadExceptionShouldThrowSDependencyNotFoundException() throws Exception {
        doThrow(new SBonitaReadException("")).when(persistenceService).selectOne(Matchers.<SelectOneDescriptor<SDependency>> any());
        dependencyServiceImpl.deleteDependency("notFound");
    }

}
