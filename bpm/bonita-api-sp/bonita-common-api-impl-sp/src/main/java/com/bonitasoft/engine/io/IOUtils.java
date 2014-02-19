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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

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

    public static byte[] marshallObjectToXML(final Object jaxbModel) throws JAXBException, IOException {
        if (jaxbModel == null) {
            return null;
        }
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            final JAXBContext contextObj = JAXBContext.newInstance(jaxbModel.getClass());
            final Marshaller m = contextObj.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            m.marshal(jaxbModel, baos);
        } finally {
            baos.close();
        }
        return baos.toByteArray();
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

    public static <T> T unmarshallXMLtoObject(final byte[] xmlObject, final Class<T> objectClass) throws JAXBException, IOException {
        if (xmlObject == null) {
            return null;
        }
        final JAXBContext contextObj = JAXBContext.newInstance(objectClass);
        final Unmarshaller um = contextObj.createUnmarshaller();
        final ByteArrayInputStream bais = new ByteArrayInputStream(xmlObject);
        final StreamSource ss = new StreamSource(bais);
        try {
            final JAXBElement<T> jaxbElement = um.unmarshal(ss, objectClass);
            return jaxbElement.getValue();
        } finally {
            bais.close();
        }
    }

}
