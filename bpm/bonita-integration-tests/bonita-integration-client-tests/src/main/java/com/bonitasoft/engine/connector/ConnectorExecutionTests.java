package com.bonitasoft.engine.connector;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.BPMRemoteTests;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.model.ProcessDefinition;
import org.bonitasoft.engine.connector.AbstractConnector;
import org.bonitasoft.engine.connectors.TestConnector;
import org.bonitasoft.engine.connectors.TestConnectorThatThrowException;
import org.bonitasoft.engine.connectors.TestConnectorWithOutput;
import org.bonitasoft.engine.connectors.TestExternalConnector;
import org.bonitasoft.engine.connectors.VariableStorage;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.util.IOUtil;
import org.junit.After;
import org.junit.Before;

import com.bonitasoft.engine.CommonAPISPTest;
import com.bonitasoft.engine.bpm.model.ProcessDefinitionBuilder;

/**
 * @author Baptiste Mesta
 */
public class ConnectorExecutionTests extends CommonAPISPTest {
    protected static final String JOHN = "john";

    protected long johnUserId;

    public static final String DEFAULT_EXTERNAL_CONNECTOR_ID = "org.bonitasoft.connector.testExternalConnector";

    public static final String DEFAULT_EXTERNAL_CONNECTOR_VERSION = "1.0";

    protected User johnUser;

    @After
    public void afterTest() throws BonitaException {
        VariableStorage.clearAll();
        logout();
    }

    @Before
    public void beforeTest() throws BonitaException {
        login();
        johnUser = createUser(JOHN, "bpm");
        johnUserId = johnUser.getId();
        logout();
        loginWith(JOHN, "bpm");
    }

    protected ProcessDefinition deployProcessWithDefaultTestConnector(final String delivery, final long userId,
            final ProcessDefinitionBuilder designProcessDefinition) throws BonitaException, IOException {
        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(
                designProcessDefinition.done());
        final List<BarResource> connectorImplementations = generateDefaultConnectorImplementations();
        for (final BarResource barResource : connectorImplementations) {
            businessArchiveBuilder.addConnectorImplementation(barResource);
        }

        final List<BarResource> generateConnectorDependencies = generateDefaultConnectorDependencies();
        for (final BarResource barResource : generateConnectorDependencies) {
            businessArchiveBuilder.addClasspathResource(barResource);
        }

        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchiveBuilder.done());
        addMappingOfActorsForUser(delivery, userId, processDefinition);
        getProcessAPI().enableProcess(processDefinition.getId());
        return processDefinition;
    }

    protected void addResource(final List<BarResource> resources, final String path, final String name) throws IOException {
        final InputStream stream = BPMRemoteTests.class.getResourceAsStream(path);
        assertNotNull(stream);
        final byte[] byteArray = IOUtils.toByteArray(stream);
        stream.close();
        resources.add(new BarResource(name, byteArray));
    }

    private List<BarResource> generateDefaultConnectorImplementations() throws IOException {
        final List<BarResource> resources = new ArrayList<BarResource>(3);
        addResource(resources, "/org/bonitasoft/engine/connectors/TestConnector.impl", "TestConnector.impl");
        addResource(resources, "/org/bonitasoft/engine/connectors/TestConnector3.impl", "TestConnector3.impl");
        addResource(resources, "/org/bonitasoft/engine/connectors/TestConnectorWithOutput.impl", "TestConnectorWithOutput.impl");
        addResource(resources, "/org/bonitasoft/engine/connectors/TestConnectorLongToExecute.impl", "TestConnectorLongToExecute.impl");
        return resources;
    }

    protected ProcessDefinition deployProcessWithExternalTestConnector(final ProcessDefinitionBuilder processDefBuilder, final String delivery,
            final long userId) throws BonitaException, IOException {
        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(
                processDefBuilder.done());

        addConnectorImplemWithDependency(businessArchiveBuilder, "/org/bonitasoft/engine/connectors/TestExternalConnector.impl", "TestExternalConnector.impl",
                TestExternalConnector.class, "TestExternalConnector.jar");

        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchiveBuilder.done());
        addMappingOfActorsForUser(delivery, userId, processDefinition);
        getProcessAPI().enableProcess(processDefinition.getId());
        return processDefinition;
    }

    private void addConnectorImplemWithDependency(final BusinessArchiveBuilder bizArchive, final String implemPath, final String implemName,
            final Class<? extends AbstractConnector> dependencyClassName, final String dependencyJarName) throws IOException {
        bizArchive.addConnectorImplementation(new BarResource(implemName, IOUtils.toByteArray(BPMRemoteTests.class.getResourceAsStream(implemPath))));
        bizArchive.addClasspathResource(new BarResource(dependencyJarName, IOUtil.generateJar(dependencyClassName)));
    }

    private List<BarResource> generateDefaultConnectorDependencies() throws IOException {
        final List<BarResource> resources = new ArrayList<BarResource>(2);
        addResource(resources, TestConnector.class, "TestConnector.jar");
        addResource(resources, TestConnector.class, "TestConnector3.jar");
        addResource(resources, TestConnectorWithOutput.class, "TestConnectorWithOutput.jar");
        return resources;
    }

    protected ProcessDefinition deployProcessWithTestConnector(final String actor, final long userId, final ProcessDefinitionBuilder designProcessDefinition)
            throws BonitaException, IOException {
        return deployProcessWithTestConnectorAndParameter(actor, userId, designProcessDefinition, null);
    }

    protected void addResource(final List<BarResource> resources, final Class<?> clazz, final String name) throws IOException {
        final byte[] data = IOUtil.generateJar(clazz);
        resources.add(new BarResource(name, data));
    }

    protected List<BarResource> generateConnectorImplementations() throws IOException {
        final List<BarResource> resources = new ArrayList<BarResource>(2);
        addResource(resources, "/org/bonitasoft/engine/connectors/TestConnector.impl", "TestConnector.impl");
        addResource(resources, "/org/bonitasoft/engine/connectors/TestConnectorWithOutput.impl", "TestConnectorWithOutput.impl");
        addResource(resources, "/org/bonitasoft/engine/connectors/TestConnectorThatThrowException.impl", "TestConnectorThatThrowException.impl");
        return resources;
    }

    protected List<BarResource> generateConnectorDependencies() throws IOException {
        final List<BarResource> resources = new ArrayList<BarResource>(2);
        addResource(resources, TestConnector.class, "TestConnector.jar");
        addResource(resources, TestConnectorWithOutput.class, "TestConnectorWithOutput.jar");
        addResource(resources, VariableStorage.class, "VariableStorage.jar");
        addResource(resources, TestConnectorThatThrowException.class, "TestConnectorThatThrowException.jar");
        return resources;
    }

    protected ProcessDefinition deployProcessWithTestConnectorAndParameter(final String delivery, final long userId,
            final ProcessDefinitionBuilder designProcessDefinition, final Map<String, String> parameters) throws BonitaException, IOException {
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        if (parameters != null) {
            businessArchive.setParameters(parameters);
        }
        final BusinessArchiveBuilder businessArchiveBuilder = businessArchive.setProcessDefinition(designProcessDefinition.done());
        final List<BarResource> connectorImplementations = generateConnectorImplementations();
        for (final BarResource barResource : connectorImplementations) {
            businessArchiveBuilder.addConnectorImplementation(barResource);
        }

        final List<BarResource> generateConnectorDependencies = generateConnectorDependencies();
        for (final BarResource barResource : generateConnectorDependencies) {
            businessArchiveBuilder.addClasspathResource(barResource);
        }

        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchiveBuilder.done());
        addMappingOfActorsForUser(delivery, userId, processDefinition);
        getProcessAPI().enableProcess(processDefinition.getId());
        return processDefinition;
    }


}
