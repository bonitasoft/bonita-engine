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

import java.io.*;
import java.nio.file.Files;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public abstract class XmlDetector implements ArtifactDetector {

    protected DocumentBuilder documentBuilder;

    private final String namespace;

    protected XmlDetector(String namespace) {
        this.namespace = namespace;
        initDocumentBuilder();
    }

    private void initDocumentBuilder() {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            try {
                documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, ""); // security-compliant
                documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, ""); // security-compliant
            } catch (IllegalArgumentException e) {
                //ignored, if not supported by the implementation
            }
            documentBuilderFactory.setNamespaceAware(true);
            this.documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            // should never occur as we use a simple configuration which can always be honored
            throw new IllegalStateException("Unable to create a document builder during XmlValidator creation", e);
        }
    }

    @Override
    public boolean isCompliant(File file) throws IOException {
        if (isXmlFile(file.getName())) {
            return isCompliant(Files.readAllBytes(file.toPath()));
        }
        return false;
    }

    protected boolean isCompliant(byte[] fileContentBytes) {
        try (InputStream is = new ByteArrayInputStream(fileContentBytes)) {
            final Element documentElement = documentBuilder.parse(is).getDocumentElement();
            // it should be an equals,
            // but it seems that some organization files have a /1.1 at the end and some other no ...
            // same for process definition as version depends on Bonita version
            return documentElement.getNamespaceURI() != null && documentElement.getNamespaceURI().contains(namespace);
        } catch (SAXException | IOException e) {
            return false;
        }
    }

}
