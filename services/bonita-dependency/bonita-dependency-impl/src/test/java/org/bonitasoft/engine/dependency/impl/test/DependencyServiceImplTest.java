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
package org.bonitasoft.engine.dependency.impl.test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.dependency.SDependencyException;
import org.bonitasoft.engine.dependency.SDependencyNotFoundException;
import org.bonitasoft.engine.dependency.impl.DependencyServiceImpl;
import org.bonitasoft.engine.dependency.model.SDependency;
import org.bonitasoft.engine.dependency.model.builder.DependencyBuilderAccessor;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Celine Souchet
 * 
 */
public class DependencyServiceImplTest {

    private DependencyBuilderAccessor builderAccessor;

    private ReadPersistenceService persistenceService;

    private Recorder recorder;

    private EventService eventService;

    private TechnicalLoggerService logger;

    private QueriableLoggerService queriableLoggerService;

    private ClassLoaderService classLoaderService;

    private DependencyServiceImpl dependencyServiceImpl;

    @Before
    public void setUp() throws Exception {
        builderAccessor = mock(DependencyBuilderAccessor.class);
        persistenceService = mock(ReadPersistenceService.class);
        recorder = mock(Recorder.class);
        eventService = mock(EventService.class);
        queriableLoggerService = mock(QueriableLoggerService.class);
        logger = mock(TechnicalLoggerService.class);
        classLoaderService = mock(ClassLoaderService.class);
        dependencyServiceImpl = new DependencyServiceImpl(builderAccessor, persistenceService, recorder, eventService, logger, queriableLoggerService,
                classLoaderService);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.dependency.impl.DependencyServiceImpl#getDependency(long)}.
     */
    @Test
    public final void getDependencyById() throws SBonitaReadException, SDependencyNotFoundException {
        final SDependency sCategory = mock(SDependency.class);
        when(persistenceService.selectById(any(SelectByIdDescriptor.class))).thenReturn(sCategory);

        Assert.assertEquals(sCategory, dependencyServiceImpl.getDependency(456L));
    }

    @Test(expected = SDependencyNotFoundException.class)
    public final void getDependencyByIdNotExists() throws SBonitaReadException, SDependencyNotFoundException {
        when(persistenceService.selectById(any(SelectByIdDescriptor.class))).thenReturn(null);

        dependencyServiceImpl.getDependency(456L);
    }

    @Test(expected = SDependencyNotFoundException.class)
    public final void getDependencyByIdWithException() throws SBonitaReadException, SDependencyNotFoundException {
        when(persistenceService.selectById(any(SelectByIdDescriptor.class))).thenThrow(new SBonitaReadException(""));

        dependencyServiceImpl.getDependency(456L);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.dependency.impl.DependencyServiceImpl#getDependencies(java.util.Collection)}.
     */
    @Test
    public final void getDependenciesByIds() throws SBonitaReadException, SDependencyException {
        final List<SDependency> sDependencies = new ArrayList<SDependency>();
        when(persistenceService.selectList(any(SelectListDescriptor.class))).thenReturn(sDependencies);

        Assert.assertEquals(sDependencies, dependencyServiceImpl.getDependencies(Collections.singletonList(456L)));
    }

    @Test(expected = SDependencyException.class)
    public final void getDependenciesByIdsWithException() throws SBonitaReadException, SDependencyException {
        when(persistenceService.selectList(any(SelectListDescriptor.class))).thenThrow(new SBonitaReadException(""));

        dependencyServiceImpl.getDependencies(Collections.singletonList(456L));
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.dependency.impl.DependencyServiceImpl#getDependencyIds(long, java.lang.String, org.bonitasoft.engine.persistence.QueryOptions)}
     * .
     */
    @Test
    public final void getDependencyIds() throws SBonitaReadException, SDependencyException {
        final List<SDependency> sDependencies = new ArrayList<SDependency>();
        when(persistenceService.selectList(any(SelectListDescriptor.class))).thenReturn(sDependencies);

        final QueryOptions options = new QueryOptions(0, 10);
        Assert.assertEquals(sDependencies, dependencyServiceImpl.getDependencyIds(54156L, "artifactType", options));
    }

    @Test(expected = SDependencyException.class)
    public final void getDependencyIdsWithException() throws SBonitaReadException, SDependencyException {
        when(persistenceService.selectList(any(SelectListDescriptor.class))).thenThrow(new SBonitaReadException(""));

        final QueryOptions options = new QueryOptions(0, 10);
        dependencyServiceImpl.getDependencyIds(54156L, "artifactType", options);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.dependency.impl.DependencyServiceImpl#createDependency(org.bonitasoft.engine.dependency.model.SDependency)}.
     */
    @Test
    public final void createDependency() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.dependency.impl.DependencyServiceImpl#createDependencyMapping(org.bonitasoft.engine.dependency.model.SDependencyMapping)}.
     */
    @Test
    public final void createDependencyMapping() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.dependency.impl.DependencyServiceImpl#deleteAllDependencies()}.
     */
    @Test
    public final void deleteAllDependencies() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.dependency.impl.DependencyServiceImpl#deleteAllDependencyMappings()}.
     */
    @Test
    public final void deleteAllDependencyMappings() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.dependency.impl.DependencyServiceImpl#deleteDependency(org.bonitasoft.engine.dependency.model.SDependency)}.
     */
    @Test
    public final void deleteDependencySDependency() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.dependency.impl.DependencyServiceImpl#deleteDependency(long)}.
     */
    @Test
    public final void deleteDependencyLong() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.dependency.impl.DependencyServiceImpl#deleteDependency(java.lang.String)}.
     */
    @Test
    public final void deleteDependencyString() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.dependency.impl.DependencyServiceImpl#deleteDependencyMapping(long)}.
     */
    @Test
    public final void deleteDependencyMappingLong() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.dependency.impl.DependencyServiceImpl#deleteDependencyMapping(org.bonitasoft.engine.dependency.model.SDependencyMapping)}.
     */
    @Test
    public final void deleteDependencyMappingSDependencyMapping() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.dependency.impl.DependencyServiceImpl#getDependencies(org.bonitasoft.engine.persistence.QueryOptions)}.
     */
    @Test
    public final void getDependenciesQueryOptions() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.dependency.impl.DependencyServiceImpl#getDependencyMapping(long)}.
     */
    @Test
    public final void getDependencyMapping() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.dependency.impl.DependencyServiceImpl#getDependencyMappings(org.bonitasoft.engine.persistence.QueryOptions)}
     * .
     */
    @Test
    public final void getDependencyMappingsQueryOptions() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.dependency.impl.DependencyServiceImpl#getDependencyMappings(long, java.lang.String, org.bonitasoft.engine.persistence.QueryOptions)}
     * .
     */
    @Test
    public final void getDependencyMappingsLongStringQueryOptions() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.dependency.impl.DependencyServiceImpl#getDependencyMappings(long, org.bonitasoft.engine.persistence.QueryOptions)}.
     */
    @Test
    public final void getDependencyMappingsLongQueryOptions() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.dependency.impl.DependencyServiceImpl#getLastUpdatedTimestamp(java.lang.String, long)}.
     */
    @Test
    public final void getLastUpdatedTimestamp() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.dependency.impl.DependencyServiceImpl#updateDependency(org.bonitasoft.engine.dependency.model.SDependency, org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor)}
     * .
     */
    @Test
    public final void updateDependency() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.dependency.impl.DependencyServiceImpl#updateDependencyMapping(org.bonitasoft.engine.dependency.model.SDependencyMapping, org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor)}
     * .
     */
    @Test
    public final void updateDependencyMapping() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.dependency.impl.DependencyServiceImpl#removeDisconnectedDependencyMappings(org.bonitasoft.engine.dependency.ArtifactAccessor)}
     * .
     */
    @Test
    public final void removeDisconnectedDependencyMappings() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.dependency.impl.DependencyServiceImpl#getDisconnectedDependencyMappings(org.bonitasoft.engine.dependency.ArtifactAccessor, org.bonitasoft.engine.persistence.QueryOptions)}
     * .
     */
    @Test
    public final void getDisconnectedDependencyMappings() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.dependency.impl.DependencyServiceImpl#deleteDependencies(long, java.lang.String)}.
     */
    @Test
    public final void deleteDependencies() {
        // TODO : Not yet implemented
    }

}
