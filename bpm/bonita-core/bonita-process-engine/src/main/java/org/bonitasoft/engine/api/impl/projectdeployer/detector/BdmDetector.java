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
package org.bonitasoft.engine.api.impl.projectdeployer.detector;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.bonitasoft.engine.io.FileOperations;

public class BdmDetector {

    public boolean isCompliant(File file) {
        try {
            byte[] bdm = FileOperations.getFileFromZip(file, "bom.xml");
            // The bdm doesn't contain its namespace, so we can't perform the usual xml validation.
            // So we look for the tag 'businessObjectModel', better than nothing.
            return new String(bdm, StandardCharsets.UTF_8.name()).contains("<businessObjectModel");
        } catch (IOException e) {
            return false;
        }
    }
}
