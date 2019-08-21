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
package org.bonitasoft.engine.api.impl.application.deployer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.io.FileOperations.asInputStream;

import java.io.FileNotFoundException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Baptiste Mesta.
 */
public class ApplicationArchiveTest {

    private ApplicationArchive applicationArchive;

    @Before
    public void before() throws Exception {
        applicationArchive = new ApplicationArchive();
    }

    @After
    public void cleanUp() throws Exception {
        applicationArchive.close();
    }

    @Test
    public void should_get_file_from_archive() throws Exception {
        applicationArchive.addFile("myFile", asInputStream("the content"));

        assertThat(applicationArchive.getFile("myFile")).hasContent("the content");
    }

    @Test
    public void should_get_file_from_subfolder_of_archive() throws Exception {
        applicationArchive.addFile("subFolder/myFile", asInputStream("the content"));

        assertThat(applicationArchive.getFile("subFolder/myFile")).hasContent("the content");
    }

    @Test(expected = FileNotFoundException.class)
    public void should_throw_FileNotFound_if_file_is_not_in_archive() throws Exception {
        applicationArchive.addFile("myOtherFile", asInputStream("the content"));

        applicationArchive.getFile("myFile");
    }

}
