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

import java.io.IOException;

import org.bonitasoft.engine.io.FileAndContent;

public class ProcessDetector {

    private static final String PROCESS_DEFINITION_NAMESPACE = "http://www.bonitasoft.org/ns/process/client/";
    private static final String PROCESS_DESIGN_DEFINITION = "process-design.xml";

    private XmlDetector xmlDetector;

    public ProcessDetector(XmlDetector xmlDetector) {
        this.xmlDetector = xmlDetector;
    }

    public boolean isCompliant(FileAndContent file) {
        if (isBarFile(file.getFileName())) {
            try {
                byte[] processDesignContent = getFileFromZip(file.getContent(), PROCESS_DESIGN_DEFINITION);
                return xmlDetector.isCompliant(processDesignContent, PROCESS_DEFINITION_NAMESPACE);
            } catch (IOException e) {
                // ignore
            }
        }
        return false;
    }

}
