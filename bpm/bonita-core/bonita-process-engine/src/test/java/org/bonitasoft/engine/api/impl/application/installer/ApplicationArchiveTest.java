/**
 * Copyright (C) 2023 Bonitasoft S.A.
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
package org.bonitasoft.engine.api.impl.application.installer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import com.vdurmont.semver4j.SemverException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ApplicationArchiveTest {

    @ParameterizedTest
    @ValueSource(strings = { "SNAPSHOT", "", ".0" })
    void unsupportedApplicationVersions(String version)
            throws Exception {
        try (var applicationArchive = new ApplicationArchive()) {
            assertThrows(SemverException.class, () -> applicationArchive.setVersion(version));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "9-SNAPSHOT", "5", "2.0", "1.0.0", "2.1-alpha", "3.3.2.beta1" })
    void supportedApplicationVersions(String version)
            throws Exception {
        try (var applicationArchive = new ApplicationArchive()) {
            applicationArchive.setVersion(version);
            assertThat(applicationArchive.hasVersion()).isTrue();
        }
    }

    @Test
    void hasGreaterVersionThan() throws Exception {
        try (var applicationArchive = new ApplicationArchive()) {
            applicationArchive.setVersion("2.0");
            assertThat(applicationArchive.hasVersionGreaterThan("1.0.0")).isTrue();

            applicationArchive.setVersion("0.0.2");
            assertThat(applicationArchive.hasVersionGreaterThan("1.0.0")).isFalse();

            applicationArchive.setVersion(null);
            assertThat(applicationArchive.hasVersionGreaterThan("1.0.0")).isFalse();
        }
    }

    @Test
    void hasEquivalentVersionTo() throws Exception {
        try (var applicationArchive = new ApplicationArchive()) {
            applicationArchive.setVersion("2.0");
            assertThat(applicationArchive.hasVersionEquivalentTo("2.0")).isTrue();

            applicationArchive.setVersion("0.0.2");
            assertThat(applicationArchive.hasVersionEquivalentTo("1.0.0")).isFalse();

            applicationArchive.setVersion(null);
            assertThat(applicationArchive.hasVersionEquivalentTo("1.0.0")).isFalse();
        }
    }
}
