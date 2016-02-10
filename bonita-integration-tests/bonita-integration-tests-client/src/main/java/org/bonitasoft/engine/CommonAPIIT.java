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
package org.bonitasoft.engine;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.connectors.TestConnector;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.filter.user.GroupUserFilter;
import org.bonitasoft.engine.filter.user.TestFilter;
import org.bonitasoft.engine.filter.user.TestFilterThatThrowException;
import org.bonitasoft.engine.filter.user.TestFilterUsingActorName;
import org.bonitasoft.engine.filter.user.TestFilterWithAutoAssign;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.test.APITestUtil;
import org.bonitasoft.engine.test.TestEngine;
import org.bonitasoft.engine.test.annotation.Engine;
import org.bonitasoft.engine.test.junit.BonitaEngineRule;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CommonAPIIT extends APITestUtil {

    @Rule
    public BonitaEngineRule bonitaEngineRule = BonitaEngineRule.create();

    @Engine
    public TestEngine testEngine;

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonAPIIT.class);

    @Rule
    public TestRule testWatcher = new PrintTestsStatusRule(LOGGER) {
        @Override
        public List<String> clean() throws Exception {
            return CommonAPIIT.this.clean();
        }
    };


    /**
     * @return warning list of unclean elements
     * @throws BonitaException
     */
    private List<String> clean() throws BonitaException {
        loginOnDefaultTenantWithDefaultTechnicalUser();

        final List<String> messages = new ArrayList<String>();
        messages.addAll(checkNoCommands());
        messages.addAll(checkNoFlowNodes());
        messages.addAll(checkNoArchivedFlowNodes());
        messages.addAll(checkNoComments());
        messages.addAll(checkNoArchivedComments());
        messages.addAll(checkNoWaitingEvent());
        messages.addAll(checkNoProcessIntances());
        messages.addAll(checkNoArchivedProcessIntances());
        messages.addAll(checkNoProcessDefinitions());
        messages.addAll(checkNoCategories());
        messages.addAll(checkNoUsers());
        messages.addAll(checkNoGroups());
        messages.addAll(checkNoRoles());
        messages.addAll(checkNoSupervisors());
        logoutOnTenant();
        return messages;
    }

    public BarResource getResource(final String path, final String name) throws IOException {
        return getBarResource(path, name, CommonAPIIT.class);
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

    protected String getContentOfResource(final String name) {
        final InputStream stream = this.getClass().getResourceAsStream(name);
        assertNotNull(stream);
        try {
            try {
                return IOUtils.toString(stream);
            } finally {
                stream.close();
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public BarResource getBarResource(String path, String name, Class<?> clazz) throws IOException {
        return super.getBarResource(path, name, clazz);
    }
}
