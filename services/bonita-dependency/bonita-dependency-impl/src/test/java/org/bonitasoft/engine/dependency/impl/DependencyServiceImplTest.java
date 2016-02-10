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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.dependency.ArtifactAccessor;
import org.bonitasoft.engine.dependency.SDependencyException;
import org.bonitasoft.engine.dependency.SDependencyMappingNotFoundException;
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
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

/**
 * @author Celine Souchet
 */
public class DependencyServiceImplTest {

    private ReadPersistenceService persistenceService;

    private Recorder recorder;

    private EventService eventService;

    private TechnicalLoggerService logger;

    private QueriableLoggerService queriableLoggerService;

    private ClassLoaderService classLoaderService;

    private DependencyServiceImpl dependencyServiceImpl;

    @Before
    public void setUp() {
        persistenceService = mock(ReadPersistenceService.class);
        recorder = mock(Recorder.class);
        eventService = mock(EventService.class);
        queriableLoggerService = mock(QueriableLoggerService.class);
        logger = mock(TechnicalLoggerService.class);
        classLoaderService = mock(ClassLoaderService.class);
        dependencyServiceImpl = new DependencyServiceImpl(persistenceService, recorder, eventService, logger, queriableLoggerService, classLoaderService);
    }

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

    /**
     * Test method for
     * {@link org.bonitasoft.engine.dependency.impl.DependencyServiceImpl#getDependencyIds(long, java.lang.String, org.bonitasoft.engine.persistence.QueryOptions)}
     * .
     */
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
     * Test method for {@link org.bonitasoft.engine.dependency.impl.DependencyServiceImpl#getDependencyMapping(long)}.
     */
    @Test
    public final void getDependencyMappingById() throws SBonitaReadException, SDependencyMappingNotFoundException {
        final SDependencyMapping sDependencyMapping = mock(SDependencyMapping.class);
        when(persistenceService.selectById(Matchers.<SelectByIdDescriptor<SDependencyMapping>> any())).thenReturn(sDependencyMapping);

        Assert.assertEquals(sDependencyMapping, dependencyServiceImpl.getDependencyMapping(456L));
    }

    @Test(expected = SDependencyMappingNotFoundException.class)
    public final void getDependencyMappingByIdNotExists() throws SBonitaReadException, SDependencyMappingNotFoundException {
        when(persistenceService.selectById(Matchers.<SelectByIdDescriptor<SDependencyMapping>> any())).thenReturn(null);

        dependencyServiceImpl.getDependencyMapping(456L);
    }

    @Test(expected = SDependencyMappingNotFoundException.class)
    public final void getDependencyMappingByIdThrowException() throws SBonitaReadException, SDependencyMappingNotFoundException {
        when(persistenceService.selectById(Matchers.<SelectByIdDescriptor<SDependencyMapping>> any())).thenThrow(new SBonitaReadException(""));

        dependencyServiceImpl.getDependencyMapping(456L);
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

    /**
     * Test method for
     * {@link org.bonitasoft.engine.dependency.impl.DependencyServiceImpl#getDisconnectedDependencyMappings(org.bonitasoft.engine.dependency.ArtifactAccessor, org.bonitasoft.engine.persistence.QueryOptions)}
     * .
     */
    @Test
    public final void getDisconnectedDependencyMappingsNothing() throws SBonitaReadException, SDependencyException {
        final ArtifactAccessor artifactAccessor = mock(ArtifactAccessor.class);
        when(artifactAccessor.artifactExists(any(ScopeType.class), any(Long.class))).thenReturn(true);
        final List<SDependencyMapping> sDependencyMappings = new ArrayList<SDependencyMapping>();
        sDependencyMappings.add(mock(SDependencyMapping.class));
        when(persistenceService.selectList(Matchers.<SelectListDescriptor<SDependencyMapping>> any())).thenReturn(sDependencyMappings);

        final QueryOptions options = new QueryOptions(0, 10);
        Assert.assertEquals(Collections.emptyList(), dependencyServiceImpl.getDisconnectedDependencyMappings(artifactAccessor, options));
    }

    @Test
    public final void getDisconnectedDependencyMappings() throws SBonitaReadException, SDependencyException {
        final ArtifactAccessor artifactAccessor = mock(ArtifactAccessor.class);
        when(artifactAccessor.artifactExists(any(ScopeType.class), any(Long.class))).thenReturn(false);
        final List<SDependencyMapping> sDependencyMappings = new ArrayList<SDependencyMapping>();
        sDependencyMappings.add(mock(SDependencyMapping.class));
        when(persistenceService.selectList(Matchers.<SelectListDescriptor<SDependencyMapping>> any())).thenReturn(sDependencyMappings);

        final QueryOptions options = new QueryOptions(0, 10);
        Assert.assertEquals(sDependencyMappings, dependencyServiceImpl.getDisconnectedDependencyMappings(artifactAccessor, options));
    }

    @Test(expected = SDependencyException.class)
    public final void getDisconnectedDependencyMappingsThrowException() throws SBonitaReadException, SDependencyException {
        final ArtifactAccessor artifactAccessor = mock(ArtifactAccessor.class);
        when(persistenceService.selectList(Matchers.<SelectListDescriptor<SDependency>> any())).thenThrow(new SBonitaReadException(""));

        final QueryOptions options = new QueryOptions(0, 10);
        dependencyServiceImpl.getDisconnectedDependencyMappings(artifactAccessor, options);
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
