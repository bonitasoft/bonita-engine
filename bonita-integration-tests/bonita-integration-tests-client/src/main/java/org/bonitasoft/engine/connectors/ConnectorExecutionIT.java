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
package org.bonitasoft.engine.connectors;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.TestWithUser;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.InvalidBusinessArchiveFormatException;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.filter.user.TestFilterWithAutoAssign;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.test.BuildTestUtil;
import org.junit.After;
import org.junit.Before;

/**
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class ConnectorExecutionIT extends TestWithUser {

    protected long userId;

    public static final String DEFAULT_EXTERNAL_CONNECTOR_ID = "org.bonitasoft.connector.testExternalConnector";

    public static final String DEFAULT_EXTERNAL_CONNECTOR_VERSION = "1.0";

    @Before
    public void beforeTest() {
        userId = user.getId();
    }

    @After
    public void afterTest() {
        VariableStorage.clearAll();
    }

    private BarResource buildBarResourceForFilterWithAutoAssign() throws IOException {
        final InputStream inputStream = TestConnector.class.getClassLoader().getResourceAsStream(
                "org/bonitasoft/engine/filter/user/TestFilterWithAutoAssign.impl");
        final byte[] data = IOUtil.getAllContentFrom(inputStream);
        inputStream.close();
        return new BarResource("TestFilter.impl", data);
    }

    public ProcessDefinition deployProcessWithActorAndTestConnectorEngineExecutionContextAndFilterWithAutoAssign(
            final ProcessDefinitionBuilder processDefinitionBuilder, final String actorName, final User user) throws IOException, BonitaException {
        final List<BarResource> connectorImplementations = Arrays.asList(BuildTestUtil.getContentAndBuildBarResource(
                "TestConnectorEngineExecutionContext.impl",
                TestConnectorEngineExecutionContext.class));
        final List<BarResource> userFilters = Arrays.asList(buildBarResourceForFilterWithAutoAssign());
        final List<BarResource> generateConnectorDependencies = Arrays.asList(
                BuildTestUtil.generateJarAndBuildBarResource(TestConnectorEngineExecutionContext.class, "TestConnectorEngineExecutionContext.jar"),
                BuildTestUtil.generateJarAndBuildBarResource(TestFilterWithAutoAssign.class, "TestFilterWithAutoAssign.jar"));
        return deployAndEnableProcessWithActorAndConnectorAndUserFilter(processDefinitionBuilder, actorName, user, connectorImplementations,
                generateConnectorDependencies, userFilters);
    }

    public ProcessDefinition deployAndEnableProcessWithTestConnectorWithAPICall(final ProcessDefinitionBuilder processDefinitionBuilder)
            throws InvalidBusinessArchiveFormatException, BonitaException, IOException {
        return deployAndEnableProcessWithConnector(processDefinitionBuilder, "TestConnectorWithAPICall.impl", TestConnectorWithAPICall.class,
                "TestConnectorWithAPICall.jar");
    }

    public ProcessDefinition deployProcessWithExternalTestConnector(final ProcessDefinitionBuilder processDefinitionBuilder, final String actorName,
            final User user) throws BonitaException, IOException {
        return deployAndEnableProcessWithActorAndConnector(processDefinitionBuilder, actorName, user, "TestExternalConnector.impl",
                TestExternalConnector.class, "TestExternalConnector.jar");
    }

    public ProcessDefinition deployProcessWithActorAndTestConnectorWithConnectedResource(final ProcessDefinitionBuilder processDefinitionBuilder,
            final String actorName, final User user) throws BonitaException, IOException {
        return deployAndEnableProcessWithActorAndConnector(processDefinitionBuilder, actorName, user, "TestConnectorWithConnectedResource.impl",
                TestConnectorWithConnectedResource.class, "TestConnectorWithConnectedResource.jar");
    }

    public ProcessDefinition deployProcessWithActorAndTestConnectorWithNotSerializableOutput(final ProcessDefinitionBuilder processDefinitionBuilder,
            final String actorName, final User user) throws BonitaException, IOException {
        return deployAndEnableProcessWithActorAndConnector(processDefinitionBuilder, actorName, user, "TestConnectorWithNotSerializableOutput.impl",
                TestConnectorWithNotSerializableOutput.class, "TestConnectorWithNotSerializableOutput.jar");
    }

    public ProcessDefinition deployProcessWithActorAndTestConnector3(final ProcessDefinitionBuilder processDefinitionBuilder, final String actorName,
            final User user) throws BonitaException, IOException {
        final List<BarResource> connectorImplementations = Arrays.asList(BuildTestUtil.getContentAndBuildBarResource("TestConnector3.impl",
                TestConnector3.class));
        final List<BarResource> generateConnectorDependencies = Arrays.asList(
                BuildTestUtil.generateJarAndBuildBarResource(TestConnector3.class, "TestConnector3.jar"),
                BuildTestUtil.generateJarAndBuildBarResource(VariableStorage.class, "VariableStorage.jar"));
        return deployAndEnableProcessWithActorAndConnectorAndParameter(processDefinitionBuilder, actorName, user, connectorImplementations,
                generateConnectorDependencies, null);
    }

    public ProcessDefinition deployProcessWithActorAndTestConnectorEngineExecutionContext(final ProcessDefinitionBuilder processDefinitionBuilder,
            final String actorName, final User user) throws BonitaException, IOException {
        return deployAndEnableProcessWithActorAndConnector(processDefinitionBuilder, actorName, user, "TestConnectorEngineExecutionContext.impl",
                TestConnectorEngineExecutionContext.class, "TestConnectorEngineExecutionContext.jar");
    }

    public ProcessDefinition deployProcessWithActorAndTestConnectorWithCustomType(final ProcessDefinitionBuilder processDefinitionBuilder,
            final String actorName, final User user) throws BonitaException, IOException {
        final byte[] byteArray = IOUtil.getAllContentFrom(TestConnector.class
                .getResourceAsStream("/org/bonitasoft/engine/connectors/connector-with-custom-type.bak"));
        final BarResource barResource = new BarResource("connector-with-custom-type.jar", byteArray);

        return deployAndEnableProcessWithActorAndConnectorAndParameter(processDefinitionBuilder, actorName, user,
                Arrays.asList(BuildTestUtil.getContentAndBuildBarResource("TestConnectorWithCustomType.impl", TestConnector.class)),
                Arrays.asList(barResource), null);
    }

    public ProcessDefinition deployProcessWithActorAndTestConnectorLongToExecute(final ProcessDefinitionBuilder processDefinitionBuilder,
            final String actorName, final User user) throws BonitaException, IOException {
        return deployAndEnableProcessWithActorAndConnectorAndParameter(processDefinitionBuilder, actorName, user,
                Arrays.asList(BuildTestUtil.getContentAndBuildBarResource("TestConnectorLongToExecute.impl", TestConnectorLongToExecute.class)),
                Collections.<BarResource> emptyList(), null);
    }

    public ProcessDefinition deployProcessWithActorAndTestConnectorThatThrowException(final ProcessDefinitionBuilder processDefinitionBuilder,
            final String actor, final User user) throws BonitaException, IOException {
        return deployProcessWithActorAndTestConnectorThatThrowExceptionAndParameter(processDefinitionBuilder, actor, user, null);
    }

    public ProcessDefinition deployProcessWithActorAndTestConnectorThatThrowExceptionAndParameter(final ProcessDefinitionBuilder processDefinitionBuilder,
            final String actorName, final User user, final Map<String, String> parameters) throws BonitaException, IOException {
        return deployAndEnableProcessWithActorAndConnectorAndParameter(processDefinitionBuilder, actorName, user, parameters,
                "TestConnectorThatThrowException.impl", TestConnectorThatThrowException.class, "TestConnectorThatThrowException.jar");
    }

    public ProcessDefinition deployProcessWithActorAndTestConnector(final ProcessDefinitionBuilder processDefinitionBuilder,
            final String actor, final User user) throws BonitaException, IOException {
        return deployProcessWithActorAndTestConnectorAndParameter(processDefinitionBuilder, actor, user, null);
    }

    public ProcessDefinition deployProcessWithActorAndTestConnectorAndParameter(final ProcessDefinitionBuilder processDefinitionBuilder,
            final String actorName, final User user, final Map<String, String> parameters) throws BonitaException, IOException {
        return deployProcessWithActorAndTestConnectorAndParameter(processDefinitionBuilder, actorName, user, parameters,
                "TestConnector.impl", "TestConnector.jar");
    }

    public ProcessDefinition deployProcessWithActorAndTestConnector2(final ProcessDefinitionBuilder processDefinitionBuilder,
            final String actor, final User user) throws BonitaException, IOException {
        return deployProcessWithActorAndTestConnector2AndParameter(processDefinitionBuilder, actor, user, null);
    }

    public ProcessDefinition deployProcessWithActorAndTestConnector2AndParameter(final ProcessDefinitionBuilder processDefinitionBuilder,
            final String actorName, final User user, final Map<String, String> parameters) throws BonitaException, IOException {
        return deployProcessWithActorAndTestConnectorAndParameter(processDefinitionBuilder, actorName, user, parameters, "TestConnector2.impl",
                "TestConnector2.jar");
    }

    private ProcessDefinition deployProcessWithActorAndTestConnectorAndParameter(final ProcessDefinitionBuilder processDefinitionBuilder,
            final String actorName, final User user, final Map<String, String> parameters, final String name, final String jarName)
            throws IOException, BonitaException {
        final List<BarResource> connectorImplementations = Arrays.asList(BuildTestUtil.getContentAndBuildBarResource(name, TestConnector.class));
        final List<BarResource> generateConnectorDependencies = Arrays.asList(BuildTestUtil.generateJarAndBuildBarResource(TestConnector.class, jarName),
                BuildTestUtil.generateJarAndBuildBarResource(VariableStorage.class, "VariableStorage.jar"));
        return deployAndEnableProcessWithActorAndConnectorAndParameter(processDefinitionBuilder, actorName, user, connectorImplementations,
                generateConnectorDependencies, parameters);
    }

    public ProcessDefinition deployProcessWithActorAndTestConnectorWithOutput(final ProcessDefinitionBuilder processDefinitionBuilder,
            final String actor, final User user) throws BonitaException, IOException {
        return deployProcessWithActorAndTestConnectorWithOutputAndParameter(processDefinitionBuilder, actor, user, null);
    }

    public ProcessDefinition deployProcessWithActorAndTestConnectorWithOutputAndParameter(final ProcessDefinitionBuilder processDefinitionBuilder,
            final String actorName, final User user, final Map<String, String> parameters) throws BonitaException, IOException {
        return deployAndEnableProcessWithActorAndConnectorAndParameter(processDefinitionBuilder, actorName, user, parameters, "TestConnectorWithOutput.impl",
                TestConnectorWithOutput.class, "TestConnectorWithOutput.jar");
    }

    public ProcessDefinition deployProcessWithExternalTestConnectorAndActor(final ProcessDefinitionBuilder processDefinitionBuilder,
            final String actorName, final User user) throws BonitaException, IOException {
        return deployAndEnableProcessWithActorAndConnector(processDefinitionBuilder, actorName, user, "TestExternalConnector.impl",
                TestExternalConnector.class,
                "TestExternalConnector.jar");
    }

}
