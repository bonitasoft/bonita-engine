/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.connectors;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.filter.user.TestFilterWithAutoAssign;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.io.IOUtil;
import org.junit.After;
import org.junit.Before;

/**
 * @author Baptiste Mesta
 */
public class ConnectorExecutionTest extends CommonAPITest {

    protected static final String JOHN = "john";

    protected long johnUserId;

    public static final String DEFAULT_EXTERNAL_CONNECTOR_ID = "org.bonitasoft.connector.testExternalConnector";

    public static final String DEFAULT_EXTERNAL_CONNECTOR_VERSION = "1.0";

    protected User johnUser;

    @After
    public void afterTest() throws BonitaException {
        VariableStorage.clearAll();
        deleteUser(johnUserId);
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

    protected ProcessDefinition deployProcessWithDefaultTestConnector(final String actorName, final User user,
            final ProcessDefinitionBuilder processDefinitionBuilder, final boolean hasFilter) throws BonitaException, IOException {
        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(
                processDefinitionBuilder.done());
        final List<BarResource> connectorImplementations = generateDefaultConnectorImplementations();
        for (final BarResource barResource : connectorImplementations) {
            businessArchiveBuilder.addConnectorImplementation(barResource);
        }

        final List<BarResource> generateConnectorDependencies = generateDefaultConnectorDependencies();
        for (final BarResource barResource : generateConnectorDependencies) {
            businessArchiveBuilder.addClasspathResource(barResource);
        }

        if (hasFilter) {
            final List<BarResource> impl = generateFilterImplementations();
            for (final BarResource barResource : impl) {
                businessArchiveBuilder.addUserFilters(barResource);
            }

            final List<BarResource> generateFilterDependencies = generateFilterDependencies();
            for (final BarResource barResource : generateFilterDependencies) {
                businessArchiveBuilder.addClasspathResource(barResource);
            }
        }

        return deployAndEnableWithActor(businessArchiveBuilder.done(), actorName, user);
    }

    protected ProcessDefinition deployProcessWithDefaultTestConnector(final String actorName, final List<User> users,
            final ProcessDefinitionBuilder processDefinitionBuilder, final boolean hasFilter) throws BonitaException, IOException {
        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(
                processDefinitionBuilder.done());
        final List<BarResource> connectorImplementations = generateDefaultConnectorImplementations();
        for (final BarResource barResource : connectorImplementations) {
            businessArchiveBuilder.addConnectorImplementation(barResource);
        }

        final List<BarResource> generateConnectorDependencies = generateDefaultConnectorDependencies();
        for (final BarResource barResource : generateConnectorDependencies) {
            businessArchiveBuilder.addClasspathResource(barResource);
        }

        if (hasFilter) {
            final List<BarResource> impl = generateFilterImplementations();
            for (final BarResource barResource : impl) {
                businessArchiveBuilder.addUserFilters(barResource);
            }

            final List<BarResource> generateFilterDependencies = generateFilterDependencies();
            for (final BarResource barResource : generateFilterDependencies) {
                businessArchiveBuilder.addClasspathResource(barResource);
            }
        }

        return deployAndEnableWithActor(businessArchiveBuilder.done(), actorName, users);
    }

    private List<BarResource> generateFilterImplementations() throws IOException {
        final List<BarResource> resources = new ArrayList<BarResource>(1);
        final InputStream inputStream = TestConnector.class.getClassLoader().getResourceAsStream(
                "org/bonitasoft/engine/filter/user/TestFilterWithAutoAssign.impl");
        final byte[] data = IOUtil.getAllContentFrom(inputStream);
        inputStream.close();
        resources.add(new BarResource("TestFilter.impl", data));
        return resources;
    }

    private List<BarResource> generateFilterDependencies() throws IOException {
        final List<BarResource> resources = new ArrayList<BarResource>(1);

        byte[] data = IOUtil.generateJar(TestFilterWithAutoAssign.class);
        resources.add(new BarResource("TestFilterWithAutoAssign.jar", data));
        return resources;
    }

    private List<BarResource> generateDefaultConnectorImplementations() throws IOException {
        final List<BarResource> resources = new ArrayList<BarResource>(7);
        addResource(resources, "/org/bonitasoft/engine/connectors/TestConnector.impl", "TestConnector.impl");
        addResource(resources, "/org/bonitasoft/engine/connectors/TestConnector3.impl", "TestConnector3.impl");
        addResource(resources, "/org/bonitasoft/engine/connectors/TestConnectorWithOutput.impl", "TestConnectorWithOutput.impl");
        addResource(resources, "/org/bonitasoft/engine/connectors/TestConnectorLongToExecute.impl", "TestConnectorLongToExecute.impl");
        addResource(resources, "/org/bonitasoft/engine/connectors/TestConnectorWithNotSerializableOutput.impl", "TestConnectorWithNotSerializableOutput.impl");
        addResource(resources, "/org/bonitasoft/engine/connectors/TestConnectorWithConnectedResource.impl", "TestConnectorWithConnectedResource.impl");
        addResource(resources, "/org/bonitasoft/engine/connectors/TestConnectorEngineExecutionContext.impl", "TestConnectorEngineExecutionContext.impl");
        return resources;
    }

    protected ProcessDefinition deployProcessWithExternalTestConnector(final ProcessDefinitionBuilder processDefBuilder, final String actorName,
            final User user) throws BonitaException, IOException {
        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(
                processDefBuilder.done());

        addConnectorImplemWithDependency(businessArchiveBuilder, "/org/bonitasoft/engine/connectors/TestExternalConnector.impl", "TestExternalConnector.impl",
                TestExternalConnector.class, "TestExternalConnector.jar");

        return deployAndEnableWithActor(businessArchiveBuilder.done(), actorName, user);
    }

    private List<BarResource> generateDefaultConnectorDependencies() throws IOException {
        final List<BarResource> resources = new ArrayList<BarResource>(6);
        addResource(resources, TestConnector.class, "TestConnector.jar");
        addResource(resources, TestConnector.class, "TestConnector3.jar");
        addResource(resources, TestConnectorWithOutput.class, "TestConnectorWithOutput.jar");
        addResource(resources, TestConnectorLongToExecute.class, "TestConnectorLongToExecute.jar");
        addResource(resources, TestConnectorWithNotSerializableOutput.class, "TestConnectorWithNotSerializableOutput.jar");
        addResource(resources, TestConnectorWithConnectedResource.class, "TestConnectorWithConnectedResource.jar");
        addResource(resources, TestConnectorEngineExecutionContext.class, "TestConnectorEngineExecutionContext.jar");
        return resources;
    }

    protected ProcessDefinition deployAndEnableProcessWithTestConnector(final String actorName, final User user,
            final ProcessDefinitionBuilder designProcessDefinition) throws BonitaException, IOException {
        return deployAndEnableProcessWithTestConnectorAndParameter(actorName, user, designProcessDefinition, null);
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

    protected ProcessDefinition deployAndEnableProcessWithTestConnectorAndParameter(final String actorName, final User user,
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

        return deployAndEnableWithActor(businessArchiveBuilder.done(), actorName, user);
    }

}
