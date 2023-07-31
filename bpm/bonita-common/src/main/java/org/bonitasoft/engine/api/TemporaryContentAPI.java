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
package org.bonitasoft.engine.api;

import org.bonitasoft.engine.io.FileContent;
import org.bonitasoft.engine.io.TemporaryFileNotFoundException;

/**
 * This API is for internal usage only.
 * It is used to store temporarily uploaded files so that they can be seen by all nodes of a cluster
 * ApiAccessType.HTTP mode is not supported as it is using InputStreams (XML serialization cannot work).
 */
@NoSessionRequired
public interface TemporaryContentAPI {

    /**
     * Store temporary content and return the temp file name
     *
     * @param fileContent file name and stream for the content
     * @return a unique identifier for the file
     */
    @Internal
    String storeTempFile(FileContent fileContent);

    /**
     * Retrieve temporary content stored using storeTempFile based on the temp file name
     *
     * @param tempFileKey temporary file identifier
     * @return a FileContent object containing the file name and stream for the content
     * @throws TemporaryFileNotFoundException if there is no temporary content in the database for this key
     */
    @Internal
    FileContent retrieveTempFile(String tempFileKey) throws TemporaryFileNotFoundException;

    /**
     * Remove temporary content stored using storeTempFile
     *
     * @param tempFileKey temporary file identifier
     * @throws TemporaryFileNotFoundException if there is no temporary content in the database for this key
     */
    @Internal
    void removeTempFile(String tempFileKey) throws TemporaryFileNotFoundException;
}
