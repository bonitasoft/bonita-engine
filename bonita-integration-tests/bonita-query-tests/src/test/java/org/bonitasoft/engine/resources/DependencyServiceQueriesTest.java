/**
 * Copyright (C) 2017 Bonitasoft S.A.
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
package org.bonitasoft.engine.resources;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Random;

import javax.inject.Inject;

import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.dependency.model.impl.SDependencyImpl;
import org.bonitasoft.engine.dependency.model.impl.SDependencyMappingImpl;
import org.bonitasoft.engine.test.persistence.repository.DependencyRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Danila Mazour
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(locations = {"/testContext.xml"})
@Transactional
public class DependencyServiceQueriesTest {

    @Inject
    private DependencyRepository repository;

    private static SDependencyImpl createDependency(final String fileName, Long tenantId) {
        SDependencyImpl dependency = new SDependencyImpl(getSaltString(), fileName, "toutou".getBytes());
        dependency.setId(new Random().nextLong());
        dependency.setTenantId(tenantId);
        return dependency;
    }

    private static SDependencyMappingImpl createDependencyMapping(Long artifactId, Long id, Long tenantId) {
        SDependencyMappingImpl dependencyMapping = new SDependencyMappingImpl(artifactId, ScopeType.TENANT, id);
        dependencyMapping.setTenantId(tenantId);
        return dependencyMapping;
    }

    private static String getSaltString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 10) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        return salt.toString();
    }

    @Test
    public void should_retrieve_the_correct_dependencyId_from_tenant_and_filename() {

        //given
        SDependencyImpl resource_to_retrieve = (SDependencyImpl) repository.add(createDependency("aFileName.zip", 1L));

        repository.add(createDependency("file.lst", 1L));

        repository.add(createDependencyMapping(666L, resource_to_retrieve.getId(), 1L));

        //when
        Long dependencyId = repository.getDependencyIdFromArtifact(666L, ScopeType.TENANT, "aFileName.zip");

        //then
        assertThat(dependencyId).as("dependency id").isEqualTo(resource_to_retrieve.getId());
    }

    @Test
    public void should_retrieve_nothing_when_BDM_not_deployed() {

        //given
        SDependencyImpl resource_to_not_retrieve = (SDependencyImpl) repository.add(createDependency("aFileName.zip", 1L));

        repository.add(createDependencyMapping(666L, resource_to_not_retrieve.getId(), 1L));

        //when
        Long dependencyId = repository.getDependencyIdFromArtifact(666L, ScopeType.TENANT, "anotherFileName.zip");

        //then
        assertThat(dependencyId).as("dependency id").isNull();
    }

    @Test
    public void should_retrieve_nothing_when_the_DB_isEmpty() {

        //given

        //when
        Long dependencyId = repository.getDependencyIdFromArtifact(666L, ScopeType.TENANT, "aFileName.zip");

        //then
        assertThat(dependencyId).as("dependency id").isNull();
    }
}
