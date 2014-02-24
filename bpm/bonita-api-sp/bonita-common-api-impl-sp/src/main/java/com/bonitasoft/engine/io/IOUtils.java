/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.io.FileUtils;
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

    public static Map<String, byte[]> unzip(final byte[] zippedBOM) throws IOException {
        final ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zippedBOM));
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

    public static byte[] toJar(final String dirPath) throws IOException {
        final Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final JarOutputStream jos = new JarOutputStream(baos, manifest);
        try {
            addFileHierarchy(new File(dirPath), jos, new File(dirPath));
            return baos.toByteArray();
        } finally {
            jos.closeEntry();
            jos.close();
        }
    }

    private static void addFileHierarchy(final File source, final JarOutputStream jos, final File base) throws IOException {
        final String relativeName = base.toURI().relativize(source.toURI()).getPath();
        if (source.isDirectory()) {
            String name = relativeName.replace(File.separator, "/");
            if (!name.isEmpty()) {
                if (!name.endsWith("/")) {
                    name += "/";
                }
                final JarEntry entry = new JarEntry(name);
                entry.setTime(source.lastModified());
                entry.setCrc(0);
                jos.putNextEntry(entry);
                jos.closeEntry();
            }
            for (final File f : source.listFiles()) {
                addFileHierarchy(f, jos, base);
            }
        } else {
            final JarEntry entry = new JarEntry(relativeName.replace(File.separator, "/"));
            entry.setTime(source.lastModified());
            entry.setCrc(FileUtils.checksumCRC32(source));
            jos.putNextEntry(entry);
            jos.write(FileUtils.readFileToByteArray(source));
            jos.closeEntry();
        }
    }

}
