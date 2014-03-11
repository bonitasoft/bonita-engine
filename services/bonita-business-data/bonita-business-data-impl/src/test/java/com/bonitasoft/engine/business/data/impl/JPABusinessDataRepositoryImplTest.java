package com.bonitasoft.engine.business.data.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.Collections;

import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.model.SDependency;
import org.bonitasoft.engine.dependency.model.SDependencyMapping;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.business.data.BusinessDataNotFoundException;

public class JPABusinessDataRepositoryImplTest {

    private DependencyService dependencyService;

    @Before
    public void setUp() throws Exception {
        dependencyService = mock(DependencyService.class);
    }

    @Test
    public void deployABOMShouldCreatetheBOMJARAndAddANewTenantDependency() throws Exception {
        final JPABusinessDataRepositoryImpl bdrService = spy(new JPABusinessDataRepositoryImpl(dependencyService, Collections.<String, Object> emptyMap()));
        final SDependency sDependency = mock(SDependency.class);
        final SDependencyMapping dependencyMapping = mock(SDependencyMapping.class);
        final byte[] zip = "zip".getBytes();
        final byte[] jar = "jar".getBytes();
        doReturn(jar).when(bdrService).generateBDMJar(zip);
        doReturn(sDependency).when(bdrService).createSDependency(any(byte[].class));
        doReturn(dependencyMapping).when(bdrService).createDependencyMapping(1, sDependency);

        bdrService.deploy(zip, 1);

        verify(dependencyService).createDependency(sDependency);
        verify(bdrService).createDependencyMapping(1, sDependency);
        verify(dependencyService).createDependencyMapping(dependencyMapping);
    }

    @Test(expected = BusinessDataNotFoundException.class)
    public void throwAnExceptionIfTheIdentifierIsNull() throws Exception {
        final JPABusinessDataRepositoryImpl bdrService = new JPABusinessDataRepositoryImpl(dependencyService, Collections.<String, Object> emptyMap());

        bdrService.find(String.class, null);
    }

}
