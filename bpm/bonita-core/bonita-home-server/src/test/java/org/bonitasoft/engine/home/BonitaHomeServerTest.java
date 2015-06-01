/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.home;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;

import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.junit.Test;

/**
 * @author Baptiste Mesta
 */
public class BonitaHomeServerTest {

    @Test(expected = BonitaHomeNotSetException.class)
    public void testBonitaHomeServerNotSet() throws BonitaException, IOException {
        System.setProperty(BonitaHome.BONITA_HOME, "");// same as not set
        final BonitaHomeServer bonitaHome = BonitaHomeServer.getInstance();
        bonitaHome.refreshBonitaHome();
        bonitaHome.getBonitaHomeFolderPath();
    }

    @Test
    public void generateRelativeResourcePathShouldHandleBackslashOS() {
        // given:
        final String pathname = "C:\\hello\\hi\\folder";
        final String resourceRelativePath = "resource/toto.lst";

        // when:
        final String generatedRelativeResourcePath = Util.generateRelativeResourcePath(new File(pathname), new File(pathname + File.separator
                + resourceRelativePath));

        // then:
        assertThat(generatedRelativeResourcePath).isEqualTo(resourceRelativePath);
    }

    @Test
    public void generateRelativeResourcePathShouldHandleNetWorkBackslash() {
        // given:
        final String pathname = "\\\\hello\\hi\\folder";
        final String resourceRelativePath = "resource/toto.lst";

        // when:
        final String generatedRelativeResourcePath = Util.generateRelativeResourcePath(new File(pathname), new File(pathname + File.separator
                + resourceRelativePath));

        // then:
        assertThat(generatedRelativeResourcePath).isEqualTo(resourceRelativePath);
    }

    @Test
    public void generateRelativeResourcePathShouldNotContainFirstSlash() {
        // given:
        final String pathname = "/home/target/some_folder/";
        final String resourceRelativePath = "resource/toto.lst";

        // when:
        final String generatedRelativeResourcePath = Util.generateRelativeResourcePath(new File(pathname), new File(pathname + File.separator
                + resourceRelativePath));

        // then:
        assertThat(generatedRelativeResourcePath).isEqualTo(resourceRelativePath);
    }

    @Test
    public void generateRelativeResourcePathShouldWorkWithRelativeInitialPath() {
        // given:
        final String pathname = "target/nuns";
        final String resourceRelativePath = "resource/toto.lst";

        // when:
        final String generatedRelativeResourcePath = Util.generateRelativeResourcePath(new File(pathname), new File(pathname + File.separator
                + resourceRelativePath));

        // then:
        assertThat(generatedRelativeResourcePath).isEqualTo(resourceRelativePath);
    }

}
