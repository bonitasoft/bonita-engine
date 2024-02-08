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
package org.bonitasoft.engine.platform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.platform.model.SPlatform;
import org.bonitasoft.engine.platform.model.SPlatformProperties;
import org.bonitasoft.engine.service.BroadcastService;
import org.bonitasoft.engine.service.TaskResult;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class PlatformVersionCheckerTest {

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog().muteForSuccessfulTests();

    @Mock
    private PlatformService platformService;

    @Mock
    private SPlatform platform;

    @Mock
    private SPlatformProperties platformProperties;

    @Mock
    private BroadcastService broadcastService;

    @Spy
    @InjectMocks
    private PlatformVersionChecker platformVersionChecker;

    @Before
    public void initialize() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(platformService.getPlatform()).thenReturn(platform);
        when(platformService.getSPlatformProperties()).thenReturn(platformProperties);
        doReturn(CompletableFuture.completedFuture(Collections.emptyMap())).when(broadcastService)
                .executeOnOthers(any());
    }

    @Test
    public void check_db_and_jars_with_same_minor_version_is_accepted() throws Exception {
        givenDatabaseSchemaVersion("7.11");
        givenBinaryVersion("7.11.0");

        boolean sameVersion = platformVersionChecker.execute();

        assertThat(sameVersion).isTrue();
    }

    @Test
    public void should_allow_to_run_snapshot_on_same_schema_version() throws Exception {
        givenDatabaseSchemaVersion("6.1");
        givenBinaryVersion("6.1.1-SNAPSHOT");

        boolean sameVersion = platformVersionChecker.execute();

        assertThat(sameVersion).isTrue();
    }

    @Test
    public void should_not_allow_to_run_snapshot_on_different_schema_version() throws Exception {
        givenDatabaseSchemaVersion("6.0.3");
        givenBinaryVersion("6.1.1-SNAPSHOT");

        boolean sameVersion = platformVersionChecker.execute();

        assertThat(sameVersion).isFalse();
        assertThat(platformVersionChecker.getErrorMessage())
                .contains("Supported database schema version is <6.1> and current database schema version is <6.0.3>");
    }

    @Test
    public void should_not_allow_to_run_when_database_schema_is_in_old_format() throws Exception {
        givenDatabaseSchemaVersion("7.10.5");
        givenBinaryVersion("7.11.0");

        boolean sameVersion = platformVersionChecker.execute();

        assertThat(sameVersion).isFalse();
        assertThat(platformVersionChecker.getErrorMessage()).contains(
                "Supported database schema version is <7.11> and current database schema version is <7.10.5>");
    }

    @Test
    public void should_not_allow_to_run_when_schema_is_in_a_different_version_but_starts_with_same_digit()
            throws Exception {
        givenDatabaseSchemaVersion("6.1");
        givenBinaryVersion("6.10.1");

        boolean sameVersion = platformVersionChecker.execute();

        assertThat(sameVersion).isFalse();
        assertThat(platformVersionChecker.getErrorMessage())
                .contains("Supported database schema version is <6.10> and current database schema version is <6.1>");
    }

    @Test
    public void execute_should_log_bonita_and_database_versions() throws SBonitaException {
        // given:
        givenDatabaseSchemaVersion("7.12");
        givenBinaryVersion("7.12.3");

        systemOutRule.clearLog();

        // when:
        platformVersionChecker.execute();

        // then:
        assertThat(systemOutRule.getLog()).contains("Bonita platform version (binaries): 7.12.3",
                "Bonita database schema version: 7.12");
    }

    @Test
    public void getVersionFromOtherNodes_should_return_this_node_version_if_no_other_node() {
        // when:
        final Optional<String> versionFromOtherNodes = platformVersionChecker.getVersionFromOtherNodes();

        // then:
        assertThat(versionFromOtherNodes).isEmpty();
    }

    @Test
    public void getVersionFromOtherNodes_should_return_other_node_version_if_other_node_returns_ok() {
        // given:
        final HashMap<String, TaskResult<String>> hashMap = new HashMap<>();
        Future<Map<String, TaskResult<String>>> nodeToVersionMap = CompletableFuture.completedFuture(hashMap);
        hashMap.put("node2", new TaskResult<>("7.11.0"));
        doReturn(nodeToVersionMap).when(broadcastService).executeOnOthers(any());

        // when:
        final Optional<String> versionFromOtherNodes = platformVersionChecker.getVersionFromOtherNodes();

        // then:
        assertThat(versionFromOtherNodes.isPresent()).isTrue();
        assertThat(versionFromOtherNodes.get()).isEqualTo("7.11.0");
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void getVersionFromOtherNodes_should_throw_exception_if_no_node_can_be_accessed() {
        // given:
        final HashMap<String, TaskResult<String>> hashMap = new HashMap<>();
        Future<Map<String, TaskResult<String>>> nodeToVersionMap = CompletableFuture.completedFuture(hashMap);
        hashMap.put("node2", new TaskResult<>(new RuntimeException()));
        doReturn(nodeToVersionMap).when(broadcastService).executeOnOthers(any());

        // then:
        exception.expect(RuntimeException.class);
        exception.expectMessage("Cannot access other node version in the cluster");

        // when:
        platformVersionChecker.getVersionFromOtherNodes();
    }

    @Test
    public void getVersionFromOtherNodes_should_throw_exception_if_exception_is_thrown_by_broadcastService()
            throws Exception {
        // given:
        final Future<?> future = mock(Future.class);
        doReturn(future).when(broadcastService).executeOnOthers(any());
        doThrow(java.util.concurrent.ExecutionException.class).when(future).get();

        // then:
        exception.expect(RuntimeException.class);
        exception.expectMessage("Cannot access other node version in the cluster");

        // when:
        platformVersionChecker.getVersionFromOtherNodes();
    }

    @Test
    public void should_return_false_if_other_node_version_is_different() throws SBonitaException {
        // given:
        givenDatabaseSchemaVersion("7.11");
        final String currentBinariesVersion = "7.11.2";
        givenBinaryVersion(currentBinariesVersion);
        doReturn(Optional.of("7.11.0")).when(platformVersionChecker).getVersionFromOtherNodes();

        systemOutRule.clearLog();

        // when:
        final boolean versionValid = platformVersionChecker.execute();

        // then:
        assertThat(versionValid).isFalse();
        assertThat(systemOutRule.getLog()).contains(
                "Node cannot be started as it is in version 7.11.2 whereas other nodes are in version 7.11.0");
    }

    private void givenBinaryVersion(String jarVersion) {
        when(platformProperties.getPlatformVersion()).thenReturn(jarVersion);
    }

    private void givenDatabaseSchemaVersion(String dbVersion) {
        when(platform.getDbSchemaVersion()).thenReturn(dbVersion);
    }

}
