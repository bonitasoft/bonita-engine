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

import static org.bonitasoft.engine.io.FileOperations.isXmlFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;

import org.bonitasoft.engine.io.FileAndContent;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class XmlDetector {

    private final DocumentBuilder documentBuilder;

    XmlDetector(DocumentBuilder documentBuilder) {
        this.documentBuilder = documentBuilder;
    }

    public boolean isCompliant(FileAndContent file, String namespace) {
        if (isXmlFile(file.getFileName())) {
            return isCompliant(file.getContent(), namespace);
        }
        return false;
    }

    protected boolean isCompliant(byte[] fileContentBytes, String namespace) {
        try (InputStream is = new ByteArrayInputStream(fileContentBytes)) {
            final Element documentElement = documentBuilder.parse(is).getDocumentElement();
            // it should be an equals,
            // but it seems that some organization files have a /1.1 at the end and some others no ...
            // same for process definition as version depends on Bonita version
            return documentElement.getNamespaceURI() != null && documentElement.getNamespaceURI().contains(namespace);
        } catch (SAXException | IOException e) {
            return false;
        }
    }

}
