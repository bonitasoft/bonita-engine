package com.bonitasoft.engine.business.data.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.Collections;

import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.SDependencyDeletionException;
import org.bonitasoft.engine.dependency.SDependencyNotFoundException;
import org.bonitasoft.engine.dependency.model.SDependency;
import org.bonitasoft.engine.dependency.model.SDependencyMapping;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.bdm.Entity;
import com.bonitasoft.engine.business.data.SBusinessDataNotFoundException;
import com.bonitasoft.engine.business.data.SBusinessDataRepositoryException;

public class JPABusinessDataRepositoryImplTest {

    private DependencyService dependencyService;

    private TechnicalLoggerService loggerService;

    private JPABusinessDataRepositoryImpl repository;

    @Before
    public void setUp() throws Exception {
        dependencyService = mock(DependencyService.class);
        loggerService = mock(TechnicalLoggerService.class);
        repository = new JPABusinessDataRepositoryImpl(dependencyService, loggerService, Collections.<String, Object> emptyMap());
    }

    @Test
    public void deployABOMShouldCreatetheBOMJARAndAddANewTenantDependency() throws Exception {
        final JPABusinessDataRepositoryImpl bdrService = spy(repository);
        final SDependency sDependency = mock(SDependency.class);
        final SDependencyMapping dependencyMapping = mock(SDependencyMapping.class);
        final byte[] zip = "zip".getBytes();
        final byte[] jar = "jar".getBytes();
        doReturn(jar).when(bdrService).generateBDMJar(zip);
        doReturn(sDependency).when(bdrService).createSDependency(anyLong(), any(byte[].class));
        doReturn(dependencyMapping).when(bdrService).createDependencyMapping(1, sDependency);

        bdrService.deploy(zip, 1);

        verify(dependencyService).createDependency(sDependency);
        verify(bdrService).createDependencyMapping(1, sDependency);
        verify(dependencyService).createDependencyMapping(dependencyMapping);
    }

    @Test(expected = SBusinessDataNotFoundException.class)
    public void throwAnExceptionIfTheIdentifierIsNull() throws Exception {
        repository.findById(MyEntity.class, null);
    }

    @Test
    public void uninstall_should_delete_a_dependency() throws Exception {
        repository.undeploy(45l);

        verify(dependencyService).deleteDependency("BDR");
    }

    @Test
    public void uninstall_should_ignore_exception_if_the_dependency_does_not_exist() throws Exception {
        doThrow(new SDependencyNotFoundException("error")).when(dependencyService).deleteDependency("BDR");

        repository.undeploy(45l);
    }

    @Test(expected = SBusinessDataRepositoryException.class)
    public void uninstall_should_throw_an_exception_if_an_exception_occurs_during_the_dependency_deletion() throws Exception {
        doThrow(new SDependencyDeletionException("error")).when(dependencyService).deleteDependency("BDR");

        repository.undeploy(45l);
    }

    class MyEntity implements Entity {

        @Override
        public Long getPersistenceId() {
            // TODO Implement me!
            return null;
        }

        @Override
        public Long getPersistenceVersion() {
            // TODO Implement me!
            return null;
        }

    }

}
