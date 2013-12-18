/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 **/

package com.bonitasoft.engine.api.impl.reports;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;

import org.junit.Test;

/**
 * @author Vincent Elcrin
 *
 */
public class ZipReaderTest {

    @Test
    public void should_extract_a_zip_and_expose_content() throws Exception {
        ZipReader zip = new ZipReader("src/test/resources/reports", "myreport-content.zip");

        zip.read(new Reader() {
            @Override
            public void read(File zip, File unzipped) throws Exception {

                assertTrue(unzipped.exists());
                assertEquals("[myreport.properties]", Arrays.asList(unzipped.list()).toString());
            }
        });
    }

    @Test
    public void should_remove_temporary_files_once_finish() throws Exception {
        ZipReader zip = new ZipReader("src/test/resources/reports", "myreport-content.zip");
        final File[] files = {null};

        zip.read(new Reader() {
            @Override
            public void read(File zip, File unzipped) throws Exception {
                files[0] = unzipped;
            }
        });

        assertFalse(files[0].exists());
    }
}
