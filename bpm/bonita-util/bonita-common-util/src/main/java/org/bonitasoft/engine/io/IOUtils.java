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
package org.bonitasoft.engine.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author Matthieu Chaffotte
 */
public class IOUtils {

    public static byte[] zip(final String fileName, final byte[] fileContent) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ZipOutputStream zos = new ZipOutputStream(baos);
        try {
            zos.putNextEntry(new ZipEntry(fileName));
            zos.write(fileContent);
        } finally {
            zos.closeEntry();
            zos.close();
        }
        return baos.toByteArray();
    }

    public static byte[] marshallObjectToXML(final Object jaxbModel, final URL schemaURL) throws JAXBException, IOException, SAXException {
        if (jaxbModel == null) {
            return null;
        }
        if (schemaURL == null) {
            throw new IllegalArgumentException("schemaURL is null");
        }
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        final Schema schema = sf.newSchema(schemaURL);
        try {
            final JAXBContext contextObj = JAXBContext.newInstance(jaxbModel.getClass());
            final Marshaller m = contextObj.createMarshaller();
            m.setSchema(schema);
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            m.marshal(jaxbModel, baos);
        } finally {
            baos.close();
        }
        return baos.toByteArray();
    }

    public static <T> T unmarshallXMLtoObject(final byte[] xmlObject, final Class<T> objectClass, final URL schemaURL) throws JAXBException, IOException,
            SAXException {
        if (xmlObject == null) {
            return null;
        }
        if (schemaURL == null) {
            throw new IllegalArgumentException("schemaURL is null");
        }
        final SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        final Schema schema = sf.newSchema(schemaURL);
        final JAXBContext contextObj = JAXBContext.newInstance(objectClass);
        final Unmarshaller um = contextObj.createUnmarshaller();
        um.setSchema(schema);
        final ByteArrayInputStream bais = new ByteArrayInputStream(xmlObject);
        final StreamSource ss = new StreamSource(bais);
        try {
            final JAXBElement<T> jaxbElement = um.unmarshal(ss, objectClass);
            return jaxbElement.getValue();
        } finally {
            bais.close();
        }
    }

    public static Map<String, byte[]> unzip(final byte[] zippedContent) throws IOException {
        final ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zippedContent));
        final Map<String, byte[]> resources = new HashMap<String, byte[]>();
        try {
            ZipEntry entry = zis.getNextEntry();
            while (entry != null) {
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try {
                    int len;
                    final byte[] buffer = new byte[1024];
                    while ((len = zis.read(buffer)) > 0) {
                        baos.write(buffer, 0, len);
                    }
                } finally {
                    baos.close();
                }
                resources.put(entry.getName(), baos.toByteArray());
                entry = zis.getNextEntry();
            }
        } finally {
            zis.closeEntry();
            zis.close();
        }
        return resources;
    }

    public static File createTempDirectory(final String prefix) throws IOException {
        final File tmpDirectory = File.createTempFile(prefix, null);
        return createDirectory(tmpDirectory);
    }

    public static File createSubDirectory(final File directory, final String child) {
        final File subDir = new File(directory, child);
        return createDirectory(subDir);
    }

    public static File createDirectoryIfNotExists(File dir) {
        if (!dir.exists()) {
            return createDirectory(dir);
        }
        return dir;
    }
    
    private static File createDirectory(final File dir) {
        dir.delete();
        dir.mkdir();
        return dir;
    }

    public static void saveDocument(final Document document, final File destination) throws IOException, TransformerException {
        if (document == null) {
            throw new IllegalArgumentException("Document should not be null.");
        }
        final Transformer tf = TransformerFactory.newInstance().newTransformer();
        tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tf.setOutputProperty(OutputKeys.INDENT, "yes");
        final FileOutputStream fos = new FileOutputStream(destination);
        try {
            final StreamResult outputTarget = new StreamResult(fos);
            tf.transform(new DOMSource(document), outputTarget);
        } finally {
            fos.close();
        }
    }

}
