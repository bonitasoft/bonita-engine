/*******************************************************************************
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
 ******************************************************************************/
package org.bonitasoft.engine.business.data.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.BOMBuilder;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.SDependencyDeletionException;
import org.bonitasoft.engine.dependency.SDependencyNotFoundException;
import org.bonitasoft.engine.dependency.model.SDependency;
import org.bonitasoft.engine.dependency.model.SDependencyMapping;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.business.data.SBusinessDataRepositoryException;

@RunWith(MockitoJUnitRunner.class)
public class BusinessDataModelRepositoryImplTest {

    @Mock
    private DependencyService dependencyService;

    private BusinessDataModelRepositoryImpl businessDataModelRepository;

    @Before
    public void setUp() {
        dependencyService = mock(DependencyService.class);
        businessDataModelRepository = spy(new BusinessDataModelRepositoryImpl(dependencyService, mock(SchemaManager.class), null, null));
    }

    @Test
    public void deployABOMShouldCreatetheBOMJARAndAddANewTenantDependency() throws Exception {
        final SDependency sDependency = mock(SDependency.class);
        final SDependencyMapping dependencyMapping = mock(SDependencyMapping.class);
        doReturn(sDependency).when(businessDataModelRepository).createSDependency(anyLong(), any(byte[].class));
        doReturn(dependencyMapping).when(businessDataModelRepository).createDependencyMapping(1, sDependency);

        BusinessObjectModel bom = BOMBuilder.aBOM().build();
        doReturn("some bytes".getBytes()).when(businessDataModelRepository).generateServerBDMJar(bom);

        businessDataModelRepository.createAndDeployServerBDMJar(1, bom);

        verify(dependencyService).createDependency(sDependency);
        verify(dependencyService).createDependencyMapping(dependencyMapping);
    }

    @Test
    public void uninstall_should_delete_a_dependency() throws Exception {
        businessDataModelRepository.uninstall(45l);

        verify(dependencyService).deleteDependency("BDR");
    }

    @Test
    public void uninstall_should_ignore_exception_if_the_dependency_does_not_exist() throws Exception {
        doThrow(new SDependencyNotFoundException("error")).when(dependencyService).deleteDependency("BDR");

        businessDataModelRepository.uninstall(45l);
    }

    @Test(expected = SBusinessDataRepositoryException.class)
    public void uninstall_should_throw_an_exception_if_an_exception_occurs_during_the_dependency_deletion() throws Exception {
        doThrow(new SDependencyDeletionException("error")).when(dependencyService).deleteDependency("BDR");

        businessDataModelRepository.uninstall(45l);
    }
}
