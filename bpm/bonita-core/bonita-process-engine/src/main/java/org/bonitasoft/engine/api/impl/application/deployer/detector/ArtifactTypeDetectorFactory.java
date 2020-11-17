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
package org.bonitasoft.engine.api.impl.application.deployer.detector;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class ArtifactTypeDetectorFactory {

    private static final ArtifactTypeDetector artifactDetector = newArtifactDetector();

    public static ArtifactTypeDetector artifactTypeDetector() {
        return artifactDetector;
    }

    private static ArtifactTypeDetector newArtifactDetector() {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            try {
                documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, ""); // security-compliant
                documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, ""); // security-compliant
            } catch (IllegalArgumentException e) {
                //ignored, if not supported by the implementation
            }
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            XmlDetector xmlDetector = new XmlDetector(documentBuilder);
            return new ArtifactTypeDetector(xmlDetector, new CustomPageDetector(), new ProcessDetector(xmlDetector),
                    new ThemeDetector(), new PageAndFormDetector(), new LayoutDetector());
        } catch (ParserConfigurationException e) {
            // should never occur as we use a simple configuration which can always be honored
            throw new IllegalStateException("Unable to create a document builder during XmlValidator creation", e);
        }
    }

}
