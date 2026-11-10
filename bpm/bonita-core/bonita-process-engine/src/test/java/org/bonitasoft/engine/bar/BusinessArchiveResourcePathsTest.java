/**
 * Copyright (C) 2026 Bonitasoft S.A.
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
package org.bonitasoft.engine.bar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import java.util.Map;

import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BusinessArchiveResourcePathsTest {

    @Mock
    private BusinessArchive businessArchive;

    @ParameterizedTest
    @ValueSource(strings = { "../evil.txt", "resources/../../evil.txt", "classpath/../../evil.jar", "/tmp/evil.txt",
            "..\\evil.txt", "resources\\..\\..\\evil.txt", "\\\\server\\share\\evil.txt", "C:evil.txt",
            "C:\\evil.txt", "resources/evil\u0000.txt" })
    void hasResourceOutsideOfArchive_should_return_true_when_a_resource_path_is_outside_of_archive(
            String resourcePath) {
        //given
        doReturn(Map.of("resources/document.txt", new byte[0], resourcePath, new byte[0])).when(businessArchive)
                .getResources();

        //when
        var hasResourceOutsideOfArchive = BusinessArchiveResourcePaths.hasResourceOutsideOfArchive(businessArchive);

        //then
        assertThat(hasResourceOutsideOfArchive).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = { "resources/document.txt", "resources/folder/../document.txt",
            "resources\\folder\\document.txt", "classpath/library.jar", "connector/connector.impl",
            "resources/..document.txt" })
    void hasResourceOutsideOfArchive_should_return_false_when_resource_paths_are_inside_of_archive(
            String resourcePath) {
        //given
        doReturn(Map.of(resourcePath, new byte[0])).when(businessArchive).getResources();

        //when
        var hasResourceOutsideOfArchive = BusinessArchiveResourcePaths.hasResourceOutsideOfArchive(businessArchive);

        //then
        assertThat(hasResourceOutsideOfArchive).isFalse();
    }
}
