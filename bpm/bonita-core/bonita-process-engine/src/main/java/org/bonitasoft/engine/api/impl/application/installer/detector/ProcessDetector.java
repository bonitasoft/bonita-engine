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

import static org.bonitasoft.engine.io.FileOperations.getFileFromZip;
import static org.bonitasoft.engine.io.FileOperations.isBarFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.springframework.stereotype.Component;

@Component
public class ProcessDetector extends XmlDetector {

    private static final String PROCESS_DEFINITION_NAMESPACE = "http://www.bonitasoft.org/ns/process/client/";
    private static final String PROCESS_DESIGN_DEFINITION = "process-design.xml";

    public ProcessDetector() {
        super(PROCESS_DEFINITION_NAMESPACE);
    }

    public boolean isCompliant(File file) {
        if (isBarFile(file.getName())) {
            try {
                return super.isCompliant(
                        getFileFromZip(Files.readAllBytes(file.toPath()), PROCESS_DESIGN_DEFINITION));
            } catch (IOException ignored) {
            }
        }
        return false;
    }

}
