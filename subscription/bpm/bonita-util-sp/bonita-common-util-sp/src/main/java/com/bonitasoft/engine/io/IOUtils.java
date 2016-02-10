/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.io;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author Matthieu Chaffotte
 * @deprecated from version 7.0.0 on, use {@link org.bonitasoft.engine.io.IOUtils} instead.
 */
@Deprecated
public class IOUtils {

    public static byte[] zip(final String fileName, final byte[] fileContent) throws IOException {
        return org.bonitasoft.engine.io.IOUtils.zip(fileName, fileContent);
    }

    public static byte[] marshallObjectToXML(final Object jaxbModel, final URL schemaURL) throws JAXBException, IOException, SAXException {
        return org.bonitasoft.engine.io.IOUtils.marshallObjectToXML(jaxbModel, schemaURL);
    }

    public static <T> T unmarshallXMLtoObject(final byte[] xmlObject, final Class<T> objectClass, final URL schemaURL) throws JAXBException, IOException,
            SAXException {
       return org.bonitasoft.engine.io.IOUtils.unmarshallXMLtoObject(xmlObject, objectClass, schemaURL);
    }

    public static Map<String, byte[]> unzip(final byte[] zippedContent) throws IOException {
        return org.bonitasoft.engine.io.IOUtils.unzip(zippedContent);
    }

    public static File createTempDirectory(final String prefix) throws IOException {
        return org.bonitasoft.engine.io.IOUtils.createTempDirectory(prefix);
    }

    public static File createSubDirectory(final File directory, final String child) {
        return org.bonitasoft.engine.io.IOUtils.createSubDirectory(directory, child);
    }

    public static File createDirectoryIfNotExists(File dir) {
        return org.bonitasoft.engine.io.IOUtils.createDirectoryIfNotExists(dir);
    }

    public static void saveDocument(final Document document, final File destination) throws IOException, TransformerException {
        org.bonitasoft.engine.io.IOUtils.saveDocument(document, destination);
    }

}
