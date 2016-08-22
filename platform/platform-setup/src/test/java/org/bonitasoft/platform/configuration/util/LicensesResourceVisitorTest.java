/**
 * Copyright (C) 2016 Bonitasoft S.A.
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
package org.bonitasoft.platform.configuration.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.platform.configuration.model.BonitaConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * @author Laurent Leseigneur
 */
public class LicensesResourceVisitorTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void should_read_licenses_folder() throws Exception {
        //given
        Path licenseFolder = temporaryFolder.newFolder().toPath();
        Files.createDirectories(licenseFolder.resolve("subFolder"));
        Files.write(licenseFolder.resolve("license1.lic"), "license 1 content".getBytes());
        Files.write(licenseFolder.resolve("license2.lic"), "license 2 content".getBytes());
        Files.write(licenseFolder.resolve("not_a_license"), "this is not a license".getBytes());
        Files.write(licenseFolder.resolve("subFolder").resolve("ignoreMe.lic"), "this is an ignored license".getBytes());
        final List<BonitaConfiguration> bonitaConfigurations = new ArrayList<>();
        BonitaConfiguration expectedLicense1 = new BonitaConfiguration("license1.lic", "license 1 content".getBytes());
        BonitaConfiguration expectedLicense2 = new BonitaConfiguration("license2.lic", "license 2 content".getBytes());

        //when
        final LicensesResourceVisitor resourceVisitor = new LicensesResourceVisitor(bonitaConfigurations);
        Files.walkFileTree(licenseFolder, resourceVisitor);

        //then
        assertThat(bonitaConfigurations).containsOnly(expectedLicense1, expectedLicense2);

    }
}
