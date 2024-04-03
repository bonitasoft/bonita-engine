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
import static org.assertj.core.api.Assertions.entry;
import static org.bonitasoft.engine.commons.Pair.pair;
import static org.bonitasoft.engine.dependency.model.ScopeType.PROCESS;
import static org.bonitasoft.engine.dependency.model.ScopeType.TENANT;
import static org.junit.Assert.fail;

import java.util.Map;
import java.util.Random;

import javax.inject.Inject;

import org.bonitasoft.engine.dependency.model.*;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.test.persistence.repository.DependencyRepository;
import org.bonitasoft.engine.test.persistence.repository.PlatformRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Danila Mazour
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(locations = { "/testContext.xml" })
@Transactional
public class DependencyServiceQueriesTest {

    @Inject
    private DependencyRepository repository;
    @Inject
    private PlatformRepository platformRepository;
    @Inject
    private JdbcTemplate jdbcTemplate;

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
        SDependency resource_to_retrieve = repository.add(SDependency.builder()
                .name(getSaltString())
                .fileName("aFileName.zip")
                .value_("toutou".getBytes()).build());
        repository.add(SDependency.builder()
                .name(getSaltString())
                .fileName("file.lst")
                .value_("toutou".getBytes()).build());
        repository.add(SDependencyMapping.builder()
                .artifactId(666L)
                .artifactType(TENANT)
                .dependencyId(resource_to_retrieve.getId()).build());

        Long dependencyId = repository.getDependencyIdFromArtifact(666L, TENANT, "aFileName.zip");

        assertThat(dependencyId).as("dependency id").isEqualTo(resource_to_retrieve.getId());
    }

    @Test
    public void should_retrieve_nothing_when_BDM_not_deployed() {
        SDependency resource_to_not_retrieve = repository.add(SDependency.builder()
                .name(getSaltString())
                .fileName("aFileName.zip")
                .value_("toutou".getBytes())
                .build());
        repository.add(SDependencyMapping.builder()
                .artifactId(666L)
                .artifactType(TENANT)
                .dependencyId(resource_to_not_retrieve.getId()).build());

        Long dependencyId = repository.getDependencyIdFromArtifact(666L, TENANT, "anotherFileName.zip");

        assertThat(dependencyId).as("dependency id").isNull();
    }

    @Test
    public void should_retrieve_nothing_when_the_DB_isEmpty() {
        Long dependencyId = repository.getDependencyIdFromArtifact(666L, TENANT, "aFileName.zip");

        assertThat(dependencyId).as("dependency id").isNull();
    }

    @Test
    public void should_retrieve_dependency_content_only_using_disconnected_objects() throws Exception {
        SDependency aDependency = repository.add(SDependency.builder()
                .name(getSaltString())
                .fileName("aFileName.zip")
                .value_("toutou".getBytes()).build());

        DependencyContent dependencyContentOnly = repository.getDependencyContentOnly(aDependency.getId());

        assertThat(dependencyContentOnly.getContent()).isEqualTo("toutou".getBytes());
        assertThat(dependencyContentOnly.getFileName()).isEqualTo("aFileName.zip");
        assertThat(repository.getSession().getIdentifier(aDependency)).isNotNull();
        try {
            repository.getSession().getIdentifier(dependencyContentOnly);
            fail("should fail because the object should not be in the hibernate session");
        } catch (Exception ignored) {
        }
    }

    @Test
    public void should_save_and_get_dependency() {
        SDependency aDependency = repository.add(SDependency.builder()
                .name("dependencyName")
                .fileName("aFileName.jar")
                .description("description of the jar")
                .value_("jarContent".getBytes()).build());

        PersistentObject dependencyFromQuery = repository.selectOne("getDependencyByName",
                pair("name", "dependencyName"));
        Map<String, Object> dependencyAsMap = jdbcTemplate.queryForMap("SELECT * FROM dependency");

        assertThat(dependencyFromQuery).isEqualTo(aDependency);
        assertThat(dependencyAsMap).containsOnly(
                entry("TENANTID", 0L), // remove when tenant notion disappears completely
                entry("ID", aDependency.getId()),
                entry("NAME", "dependencyName"),
                entry("DESCRIPTION", "description of the jar"),
                entry("FILENAME", "aFileName.jar"),
                entry("VALUE_", "jarContent".getBytes()));
    }

    @Test
    public void should_save_and_get_dependency_mapping() {
        SDependency aDependency = repository.add(SDependency.builder()
                .name("dependencyName")
                .fileName("aFileName.jar")
                .description("description of the jar")
                .value_("jarContent".getBytes()).build());
        SDependencyMapping dependencyMapping = repository.add(SDependencyMapping.builder()
                .artifactId(567L)
                .artifactType(PROCESS)
                .dependencyId(aDependency.getId()).build());

        PersistentObject dependencyMappingFromQuery = repository.selectOne("getDependencyMappingsByDependency",
                pair("dependencyId", aDependency.getId()));
        Map<String, Object> dependencyMappingAsMap = jdbcTemplate
                .queryForMap("SELECT * FROM dependencymapping WHERE dependencyId=" + aDependency.getId());

        assertThat(dependencyMappingFromQuery).isEqualTo(dependencyMapping);
        assertThat(dependencyMappingAsMap).containsOnly(
                entry("TENANTID", 0L), // remove when tenant notion disappears completely
                entry("ID", dependencyMapping.getId()),
                entry("ARTIFACTID", 567L),
                entry("ARTIFACTTYPE", "PROCESS"),
                entry("DEPENDENCYID", aDependency.getId()));
    }

    @Test
    public void should_save_and_get_platform_dependency() {
        SPlatformDependency aDependency = platformRepository.add(SPlatformDependency.builder()
                .name("dependencyName")
                .fileName("aFileName.jar")
                .description("description of the jar")
                .value_("jarContent".getBytes()).build());

        PersistentObject dependencyFromQuery = platformRepository.selectOneOnPlatform("getPlatformDependencyByName",
                pair("name", "dependencyName"));
        Map<String, Object> dependencyAsMap = jdbcTemplate.queryForMap("SELECT * FROM pdependency");

        assertThat(dependencyFromQuery).isEqualTo(aDependency);
        assertThat(dependencyAsMap).containsOnly(
                entry("ID", aDependency.getId()),
                entry("NAME", "dependencyName"),
                entry("DESCRIPTION", "description of the jar"),
                entry("FILENAME", "aFileName.jar"),
                entry("VALUE_", "jarContent".getBytes()));
    }

    @Test
    public void should_save_and_get_platform_dependency_mapping() {
        SPlatformDependency aDependency = platformRepository.add(SPlatformDependency.builder()
                .name("dependencyName")
                .fileName("aFileName.jar")
                .description("description of the jar")
                .value_("jarContent".getBytes()).build());
        SPlatformDependencyMapping dependencyMapping = platformRepository.add(SPlatformDependencyMapping.builder()
                .artifactId(567L)
                .artifactType(PROCESS)
                .dependencyId(aDependency.getId()).build());

        PersistentObject dependencyMappingFromQuery = platformRepository.selectOneOnPlatform(
                "getPlatformDependencyMappingsByDependency", pair("dependencyId", aDependency.getId()));
        Map<String, Object> dependencyMappingAsMap = jdbcTemplate
                .queryForMap("SELECT * FROM pdependencymapping WHERE dependencyId=" + aDependency.getId());

        assertThat(dependencyMappingFromQuery).isEqualTo(dependencyMapping);
        assertThat(dependencyMappingAsMap).containsOnly(
                entry("ID", dependencyMapping.getId()),
                entry("ARTIFACTID", 567L),
                entry("ARTIFACTTYPE", "PROCESS"),
                entry("DEPENDENCYID", aDependency.getId()));
    }
}
