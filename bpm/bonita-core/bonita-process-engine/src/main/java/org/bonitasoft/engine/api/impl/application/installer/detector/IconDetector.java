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
package org.bonitasoft.engine.api.impl.application.installer.detector;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;

import org.springframework.stereotype.Component;

@Component
public class IconDetector implements ArtifactDetector {

    /**
     * Determine the compliance using the guessed content-type of a given file name.
     */
    @Override
    public boolean isCompliant(File file) throws IOException {
        if (file.isFile()) {
            var contentType = URLConnection.guessContentTypeFromName(file.getName());
            return contentType != null && contentType.startsWith("image/");
        }
        return false;
    }

}
