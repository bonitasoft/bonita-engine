/**
 * Copyright (C) 2018 Bonitasoft S.A.
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

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.bonitasoft.engine.io.FileAndContent;
import org.bonitasoft.engine.io.FileOperations;

public class BdmDetector {

    public boolean isCompliant(FileAndContent file) {
        byte[] bdm;
        try {
            try {
                bdm = FileOperations.getFileFromZip(file.getContent(), "bom.xml");
            } catch (FileNotFoundException e) {
                return false;
            }
            // The bdm doesn't contain its namespace, so we can't perform the usual xml validation.
            // So we look for the tag 'businessObjectModel', better than nothing:
            return new String(bdm, UTF_8).contains("<businessObjectModel");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
