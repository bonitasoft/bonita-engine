package org.bonitasoft.engine;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.BonitaSuiteRunner.Initializer;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.connectors.TestConnector;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.filter.user.GroupUserFilter;
import org.bonitasoft.engine.filter.user.TestFilter;
import org.bonitasoft.engine.filter.user.TestFilterThatThrowException;
import org.bonitasoft.engine.filter.user.TestFilterUsingActorName;
import org.bonitasoft.engine.filter.user.TestFilterWithAutoAssign;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.test.APITestUtil;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(BonitaTestRunner.class)
@Initializer(TestsInitializer.class)
public abstract class CommonAPITest extends APITestUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonAPITest.class);

    @Rule
    public TestRule testWatcher = new TestWatcher() {

        @Override
        public void starting(final Description d) {
            LOGGER.info("Starting test: " + d.getClassName() + "." + d.getMethodName());
        }

        @Override
        public void failed(final Throwable cause, final Description d) {
            try {
                LOGGER.error("Failed test: " + d.getClassName() + "." + d.getMethodName());
                clean();
            } catch (final Exception be) {
                LOGGER.error("Unable to clean db", be);
            } finally {
                LOGGER.info("-----------------------------------------------------------------------------------------------");
            }
        }

        @Override
        public void succeeded(final Description d) {
            try {
                List<String> clean = null;
                try {
                    clean = clean();
                } catch (final BonitaException e) {
                    throw new BonitaRuntimeException(e);
                }
                LOGGER.info("Succeeded test: " + d.getClassName() + "." + d.getMethodName());
                if (!clean.isEmpty()) {
                    throw new BonitaRuntimeException(clean.toString());
                }
            } finally {
                LOGGER.info("-----------------------------------------------------------------------------------------------");
            }
        }
    };

    /**
     * FIXME: clean actors!
     * 
     * @return
     * @throws BonitaException
     */
    private List<String> clean() throws BonitaException {
        loginOnDefaultTenantWithDefaultTechnicalUser();

        final List<String> messages = new ArrayList<String>();
        messages.addAll(checkNoCommands());
        messages.addAll(checkNoUsers());
        messages.addAll(checkNoGroups());
        messages.addAll(checkNoRoles());
        messages.addAll(checkNoSupervisors());
        messages.addAll(checkNoProcessDefinitions());
        messages.addAll(checkNoProcessIntances());
        messages.addAll(checkNoArchivedProcessIntances());
        messages.addAll(checkNoFlowNodes());
        messages.addAll(checkNoArchivedFlowNodes());
        messages.addAll(checkNoCategories());
        messages.addAll(checkNoComments());
        messages.addAll(checkNoArchivedComments());

        logoutOnTenant();
        return messages;
    }

    public BarResource getResource(final String path, final String name) throws IOException {
        final InputStream stream = BPMRemoteTests.class.getResourceAsStream(path);
        assertNotNull(stream);
        final byte[] byteArray = IOUtils.toByteArray(stream);
        stream.close();
        return new BarResource(name, byteArray);
    }

    public void addResource(final List<BarResource> resources, final String path, final String name) throws IOException {
        final BarResource barResource = getResource(path, name);
        resources.add(barResource);
    }

    protected ProcessDefinition deployProcessWithTestFilter(final ProcessDefinitionBuilder processDefinitionBuilder, final String actorName, final User user,
            final String filterName) throws BonitaException, IOException {
        final List<BarResource> userFilters = generateFilterImplementations(filterName);
        final List<BarResource> generateFilterDependencies = generateFilterDependencies();
        return deployAndEnableProcessWithActorAndUserFilter(processDefinitionBuilder, actorName, user, generateFilterDependencies, userFilters);
    }

    private List<BarResource> generateFilterImplementations(final String filterName) throws IOException {
        final List<BarResource> resources = new ArrayList<BarResource>(1);
        final InputStream inputStream = TestConnector.class.getClassLoader().getResourceAsStream("org/bonitasoft/engine/filter/user/" + filterName + ".impl");
        final byte[] data = IOUtil.getAllContentFrom(inputStream);
        inputStream.close();
        resources.add(new BarResource(filterName + ".impl", data));
        return resources;
    }

    private List<BarResource> generateFilterDependencies() throws IOException {
        final List<BarResource> resources = new ArrayList<BarResource>(1);
        byte[] data = IOUtil.generateJar(TestFilterThatThrowException.class);
        resources.add(new BarResource("TestFilterThatThrowException.jar", data));
        data = IOUtil.generateJar(TestFilter.class);
        resources.add(new BarResource("TestFilter.jar", data));
        data = IOUtil.generateJar(TestFilterWithAutoAssign.class);
        resources.add(new BarResource("TestFilterWithAutoAssign.jar", data));
        data = IOUtil.generateJar(TestFilterUsingActorName.class);
        resources.add(new BarResource("TestFilterUsingActorName.jar", data));
        data = IOUtil.generateJar(GroupUserFilter.class);
        resources.add(new BarResource("TestGroupUserFilter.jar", data));
        return resources;
    }

}
