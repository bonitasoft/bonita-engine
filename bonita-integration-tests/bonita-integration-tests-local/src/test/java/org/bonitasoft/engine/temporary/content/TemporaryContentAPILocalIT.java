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
package org.bonitasoft.engine.temporary.content;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.CommonAPIIT;
import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.api.TemporaryContentAPI;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.io.FileContent;
import org.bonitasoft.engine.io.TemporaryFileNotFoundException;
import org.bonitasoft.engine.session.PlatformSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Haroun EL ALAMI
 */
public class TemporaryContentAPILocalIT extends CommonAPIIT {

    private static PlatformSession platformSession;

    @Before
    public void before() throws BonitaException, IOException {
        platformSession = loginOnPlatform();
    }

    @After
    public void after() throws BonitaException {
        logoutOnPlatform(platformSession);
    }

    @Test
    public void shouldStoreThenRetrieveTempFileTest() throws BonitaException, IOException {

        // save tempFile
        final TemporaryContentAPI temporaryContentAPI = PlatformAPIAccessor.getTemporaryContentAPI();

        Path tempFilePath = Files.createTempFile("tempFile", ".txt");
        Files.writeString(tempFilePath, "test");

        String originalFileName = "originalFileName";
        String mimeType = "plain/text";
        FileContent fileContent = new FileContent(originalFileName, new FileInputStream(tempFilePath.toFile()),
                mimeType);

        String tmpFileKey = temporaryContentAPI.storeTempFile(fileContent);
        assertNotNull(tmpFileKey);

        // get saved tempFile
        FileContent uploaded = temporaryContentAPI.retrieveTempFile(tmpFileKey);
        assertTrue(uploaded.getFileName().equals(originalFileName));
        assertTrue(uploaded.getMimeType().equals(mimeType));
        assertArrayEquals(IOUtils.toByteArray(uploaded.getInputStream()), "test".getBytes());

        // remove temporaryContent
        temporaryContentAPI.removeTempFile(tmpFileKey);

        // check if temporaryContent not found
        assertThrows(TemporaryFileNotFoundException.class, () -> temporaryContentAPI.retrieveTempFile(tmpFileKey));
        assertThrows(TemporaryFileNotFoundException.class, () -> temporaryContentAPI.removeTempFile(tmpFileKey));
    }

}
