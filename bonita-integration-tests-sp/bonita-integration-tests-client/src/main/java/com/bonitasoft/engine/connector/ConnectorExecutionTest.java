/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.connector;

import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.BPMRemoteTests;
import org.bonitasoft.engine.bpm.connector.ConnectorInstancesSearchDescriptor;
import org.bonitasoft.engine.connectors.VariableStorage;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.junit.After;
import org.junit.Before;

import com.bonitasoft.engine.CommonAPISPIT;

/**
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public abstract class ConnectorExecutionTest extends CommonAPISPIT {

    public static final String DEFAULT_EXTERNAL_CONNECTOR_ID = "org.bonitasoft.connector.testExternalConnector";

    public static final String DEFAULT_EXTERNAL_CONNECTOR_VERSION = "1.0";

    public static final String CONNECTOR_WITH_OUTPUT_ID = "org.bonitasoft.connector.testConnectorWithOutput";

    public static final String TEST_CONNECTOR_ID = "org.bonitasoft.connector.testConnector";

    public static final String TEST_CONNECTOR_THAT_THROW_EXCEPTION_ID = "testConnectorThatThrowException";

    public static final String CONNECTOR_VERSION = "1.0";

    public static final String CONNECTOR_OUTPUT_NAME = "output1";

    protected static final String JOHN = "john";

    protected long johnUserId;

    protected User user;

    @After
    public void afterTest() throws BonitaException {
        VariableStorage.clearAll();
        deleteUser(JOHN);
       logoutOnTenant();
    }

    @Before
    public void beforeTest() throws BonitaException {
        loginOnDefaultTenantWithDefaultTechnicalUser();
        user = createUser(JOHN, "bpm");
        johnUserId = user.getId();
       logoutOnTenant();
        loginOnDefaultTenantWith(JOHN, "bpm");
    }

    protected byte[] generateZipByteArrayForConnector(final String implSourceFile, final Class<?> implClass) throws IOException {
        return generateZipByteArrayForConnector(implSourceFile, implClass, "classpath/");
    }

    protected byte[] generateZipByteArrayForConnector(final String implSourceFile, final Class<?> implClass, final String connectorFolder) throws IOException {
        // generate byte arrays of .impl and .jar files
        InputStream stream = null;
        ByteArrayOutputStream baos = null;
        ZipOutputStream zos = null;
        try {
            stream = BPMRemoteTests.class.getResourceAsStream(implSourceFile);
            assertNotNull(stream);
            final String baseName = implSourceFile.substring(implSourceFile.lastIndexOf('/') + 1, implSourceFile.lastIndexOf('.'));
            final byte[] byteArray = IOUtils.toByteArray(stream);
            final byte[] data = IOUtil.generateJar(implClass);
            // read bytes of files to zip file byte array
            baos = new ByteArrayOutputStream();
            zos = new ZipOutputStream(baos);
            ZipEntry entry = new ZipEntry(baseName + ".impl");
            entry.setSize(byteArray.length);
            zos.putNextEntry(entry);
            zos.write(byteArray);
            zos.closeEntry();
            entry = new ZipEntry(connectorFolder + baseName + ".jar");
            entry.setSize(data.length);
            zos.putNextEntry(entry);
            zos.write(data);
            zos.closeEntry();
            return baos.toByteArray();
        } finally {
            if (stream != null) {
                stream.close();
            }
            if (zos != null) {
                zos.close();
            }
            if (baos != null) {
                baos.close();
            }
        }
    }

    protected SearchOptionsBuilder getFirst100ConnectorInstanceSearchOptions(long containerId, String containerType) {
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 100);
        searchOptionsBuilder.filter(ConnectorInstancesSearchDescriptor.CONTAINER_ID, containerId);
        searchOptionsBuilder.filter(ConnectorInstancesSearchDescriptor.CONTAINER_TYPE, containerType);
        searchOptionsBuilder.sort(ConnectorInstancesSearchDescriptor.NAME, Order.ASC);
        return searchOptionsBuilder;
    }

}
