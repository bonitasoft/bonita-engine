/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class DocumentManager {

    private DocumentManager() {
        // For Sonar
    }

    public static Document generateDocument(final String s) throws ParserConfigurationException, SAXException, IOException {
        final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        final InputStream is = new ByteArrayInputStream(s.getBytes());
        try {
            return builder.parse(is, null);
        } finally {
            is.close();
        }
    }

    public static String getDocumentContent(final Document document) throws TransformerFactoryConfigurationError, TransformerException, IOException {
        final Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        final StringWriter writer = new StringWriter();
        try {
            final StreamResult result = new StreamResult(writer);
            final DOMSource source = new DOMSource(document);
            transformer.transform(source, result);
            return result.getWriter().toString();
        } finally {
            writer.close();
        }
    }

}
