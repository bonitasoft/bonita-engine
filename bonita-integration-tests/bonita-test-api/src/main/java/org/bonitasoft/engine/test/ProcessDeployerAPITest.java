/*
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
 */

package org.bonitasoft.engine.test;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.Problem;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessEnablementException;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.commons.io.IOUtil;
import org.bonitasoft.engine.connector.AbstractConnector;
import org.bonitasoft.engine.connector.Connector;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.Role;
import org.bonitasoft.engine.identity.User;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertNotNull;

/**
 * @author mazourd
 */
public class ProcessDeployerAPITest {

    ProcessAPI processAPI;

    public ProcessDeployerAPITest(ProcessAPI processAPI) {
        this.processAPI = processAPI;
    }

    public ProcessAPI getProcessAPI() {
        return processAPI;
    }

    public void disableAndDeleteProcess(final ProcessDefinition processDefinition) throws BonitaException {
        if (processDefinition != null) {
            disableAndDeleteProcess(processDefinition.getId());
        }
    }

    public void disableAndDeleteProcess(final long processDefinitionId) throws BonitaException {
        getProcessAPI().disableProcess(processDefinitionId);

        // Delete all process instances
        long nbDeletedProcessInstances;
        do {
            nbDeletedProcessInstances = getProcessAPI().deleteProcessInstances(processDefinitionId, 0, 100);
        } while (nbDeletedProcessInstances > 0);

        // Delete all archived process instances
        long nbDeletedArchivedProcessInstances;
        do {
            nbDeletedArchivedProcessInstances = getProcessAPI().deleteArchivedProcessInstances(processDefinitionId, 0, 100);
        } while (nbDeletedArchivedProcessInstances > 0);

        getProcessAPI().deleteProcessDefinition(processDefinitionId);
    }

    public void disableAndDeleteProcess(final ProcessDefinition... processDefinitions) throws BonitaException {
        disableAndDeleteProcess(Arrays.asList(processDefinitions));
    }

    public void disableAndDeleteProcess(final List<ProcessDefinition> processDefinitions) throws BonitaException {
        if (processDefinitions != null) {
            for (final ProcessDefinition processDefinition : processDefinitions) {
                disableAndDeleteProcess(processDefinition);
            }
        }
    }

    public void disableAndDeleteProcessById(final List<Long> processDefinitionIds) throws BonitaException {
        if (processDefinitionIds != null) {
            for (final Long id : processDefinitionIds) {
                disableAndDeleteProcess(id);
            }
        }
    }

        public ProcessDefinition deployAndEnableProcess(final DesignProcessDefinition designProcessDefinition) throws BonitaException {
        return deployAndEnableProcess(new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done());
    }

    public ProcessDefinition deployAndEnableProcess(final BusinessArchive businessArchive) throws BonitaException {
        final ProcessDefinition processDefinition = deployProcess(businessArchive);
        enableProcess(processDefinition);
        return processDefinition;

    }

    public ProcessDefinition deployAndEnableProcessWithActor(final DesignProcessDefinition designProcessDefinition, final String actorName, final User user)
            throws BonitaException {
        return deployAndEnableProcessWithActor(designProcessDefinition, Arrays.asList(actorName), Arrays.asList(user));
    }

    public ProcessDefinition deployAndEnableProcessWithActor(final DesignProcessDefinition designProcessDefinition, final String actorName,
            final List<User> users)
            throws BonitaException {
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done();
        return deployAndEnableProcessWithActor(businessArchive, actorName, users);
    }

    public ProcessDefinition deployAndEnableProcessWithActor(final BusinessArchive businessArchive, final String actorName, final List<User> users)
            throws BonitaException {
        final ProcessDefinition processDefinition = deployProcess(businessArchive);
        for (final User user : users) {
            getProcessAPI().addUserToActor(actorName, processDefinition, user.getId());
        }
        enableProcess(processDefinition);
        return processDefinition;
    }

    public ProcessDefinition deployAndEnableProcessWithActorAndParameters(final DesignProcessDefinition designProcessDefinition, final String actorName,
            final User user, final Map<String, String> parameters) throws BonitaException {
        return deployAndEnableProcessWithActorAndParameters(designProcessDefinition, Arrays.asList(actorName), Arrays.asList(user), parameters);
    }

    public ProcessDefinition deployAndEnableProcessWithActor(final DesignProcessDefinition designProcessDefinition, final List<String> actorsName,
            final List<User> users) throws BonitaException {
        return deployAndEnableProcessWithActor(new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done(),
                actorsName, users);
    }

    public ProcessDefinition deployAndEnableProcessWithActor(final BusinessArchive businessArchive, final List<String> actorsName, final List<User> users)
            throws BonitaException {
        final ProcessDefinition processDefinition = deployProcess(businessArchive);
        for (int i = 0; i < users.size(); i++) {
            getProcessAPI().addUserToActor(actorsName.get(i), processDefinition, users.get(i).getId());
        }
        enableProcess(processDefinition);
        return processDefinition;
    }

    public ProcessDefinition deployAndEnableProcessWithActor(final DesignProcessDefinition designProcessDefinition, final Map<String, List<User>> actorUsers)
            throws BonitaException {
        return deployAndEnableProcessWithActor(new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done(),
                actorUsers);
    }

    public ProcessDefinition deployAndEnableProcessWithActor(final BusinessArchive businessArchive, final Map<String, List<User>> actorUsers)
            throws BonitaException {
        final ProcessDefinition processDefinition = deployProcess(businessArchive);
        for (final Map.Entry<String, List<User>> actorUser : actorUsers.entrySet()) {
            final String actorName = actorUser.getKey();
            final List<User> users = actorUser.getValue();
            for (final User user : users) {
                getProcessAPI().addUserToActor(actorName, processDefinition, user.getId());
            }
        }

        enableProcess(processDefinition);
        return processDefinition;
    }

    public ProcessDefinition deployProcess(final BusinessArchive businessArchive) throws BonitaException {
        return processAPI.deploy(businessArchive);
    }

    private void enableProcess(final ProcessDefinition processDefinition) throws ProcessDefinitionNotFoundException, ProcessEnablementException {
        try {
            processAPI.enableProcess(processDefinition.getId());
        } catch (final ProcessEnablementException e) {
            final List<Problem> problems = processAPI.getProcessResolutionProblems(processDefinition.getId());
            throw new ProcessEnablementException("not resolved: " + problems);
        }
    }

    public ProcessDefinition deployAndEnableProcessWithActor(final BusinessArchive businessArchive, final String actorName, final User user)
            throws BonitaException {
        return deployAndEnableProcessWithActor(businessArchive, Collections.singletonList(actorName), Collections.singletonList(user));
    }

    public ProcessDefinition deployAndEnableProcessWithActorAndParameters(final DesignProcessDefinition designProcessDefinition, final List<String> actorsName,
            final List<User> users, final Map<String, String> parameters) throws BonitaException {
        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive();
        final BusinessArchive businessArchive = businessArchiveBuilder.setParameters(parameters).setProcessDefinition(designProcessDefinition).done();
        return deployAndEnableProcessWithActor(businessArchive, actorsName, users);
    }

    public ProcessDefinition deployAndEnableProcessWithActor(final DesignProcessDefinition designProcessDefinition, final String actorName, final Group group)
            throws BonitaException {
        final ProcessDefinition processDefinition = deployProcess(
                new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done());
        getProcessAPI().addGroupToActor(actorName, group.getId(), processDefinition);
        getProcessAPI().enableProcess(processDefinition.getId());
        return processDefinition;
    }

    public ProcessDefinition deployAndEnableProcessWithActor(final DesignProcessDefinition designProcessDefinition, final String actorName,
            final Group... groups)
            throws BonitaException {
        final ProcessDefinition processDefinition = deployProcess(
                new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done());
        for (final Group group : groups) {
            getProcessAPI().addGroupToActor(actorName, group.getId(), processDefinition);
        }
        getProcessAPI().enableProcess(processDefinition.getId());
        return processDefinition;
    }

    public ProcessDefinition deployAndEnableProcessWithActor(final DesignProcessDefinition designProcessDefinition, final String actorName, final Role role)
            throws BonitaException {
        final ProcessDefinition processDefinition = deployProcess(
                new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done());
        getProcessAPI().addRoleToActor(actorName,processDefinition, role.getId());
        getProcessAPI().enableProcess(processDefinition.getId());
        return processDefinition;
    }

    public ProcessDefinition deployAndEnableProcessWithActor(final DesignProcessDefinition designProcessDefinition, final String actorName, final Role... roles)
            throws BonitaException {
        final ProcessDefinition processDefinition = deployProcess(
                new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done());
        for (final Role role : roles) {
            getProcessAPI().addRoleToActor(actorName,processDefinition,role.getId());
        }
        getProcessAPI().enableProcess(processDefinition.getId());
        return processDefinition;
    }

    public ProcessDefinition deployAndEnableProcessWithActor(final DesignProcessDefinition designProcessDefinition, final String actorName, final Role role,
            final Group group) throws BonitaException {
        final ProcessDefinition processDefinition = deployProcess(
                new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done());
        getProcessAPI().addRoleAndGroupToActor(actorName, processDefinition,role.getId(), group.getId());
        getProcessAPI().enableProcess(processDefinition.getId());
        return processDefinition;
    }

    public ProcessDefinition deployAndEnableProcessWithActor(final DesignProcessDefinition designProcessDefinition, final String actorName, final long userId)
            throws BonitaException {
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done();
        final ProcessDefinition processDefinition = deployProcess(businessArchive);
        getProcessAPI().addUserToActor(actorName, processDefinition, userId);
        getProcessAPI().enableProcess(processDefinition.getId());
        return processDefinition;
    }

    public ProcessDefinition deployAndEnableProcessWithConnector(final ProcessDefinitionBuilder processDefinitionBuilder,
            final List<BarResource> connectorImplementations, final List<BarResource> generateConnectorDependencies) throws BonitaException {
        final BusinessArchiveBuilder businessArchiveBuilder = buildBusinessArchiveWithConnectorAndUserFilter(processDefinitionBuilder,
                connectorImplementations, generateConnectorDependencies, Collections.<BarResource>emptyList());
        return deployAndEnableProcess(businessArchiveBuilder.done());
    }

    public ProcessDefinition deployAndEnableProcessWithConnector(final ProcessDefinitionBuilder processDefinitionBuilder, final String connectorImplName,
            final Class<? extends AbstractConnector> clazz, final String jarName) throws BonitaException, IOException {
        return deployAndEnableProcessWithConnector(processDefinitionBuilder,
                Arrays.asList(getContentAndBuildBarResource(connectorImplName, clazz)),
                Arrays.asList(generateJarAndBuildBarResource(clazz, jarName)));
    }

    public ProcessDefinition deployAndEnableProcessWithActorAndConnector(final ProcessDefinitionBuilder processDefinitionBuilder, final String actorName,
            final User user, final String name, final Class<? extends AbstractConnector> clazz, final String jarName) throws BonitaException, IOException {
        return deployAndEnableProcessWithActorAndConnectorAndParameter(processDefinitionBuilder, actorName, user,
                Arrays.asList(getContentAndBuildBarResource(name, clazz)),
                Arrays.asList(generateJarAndBuildBarResource(clazz, jarName)), null);
    }


    public ProcessDefinition deployAndEnableProcessWithActorAndConnectorAndParameter(final ProcessDefinitionBuilder processDefinitionBuilder,
            final String actorName, final User user, final List<BarResource> connectorImplementations, final List<BarResource> generateConnectorDependencies,
            final Map<String, String> parameters) throws BonitaException {
        final BusinessArchiveBuilder businessArchiveBuilder = buildBusinessArchiveWithConnectorAndUserFilter(processDefinitionBuilder,
                connectorImplementations, generateConnectorDependencies, Collections.<BarResource> emptyList());
        if (parameters != null) {
            businessArchiveBuilder.setParameters(parameters);
        }
        return deployAndEnableProcessWithActor(businessArchiveBuilder.done(), actorName, user);
    }

    public ProcessDefinition deployAndEnableProcessWithActorAndConnectorAndUserFilter(final ProcessDefinitionBuilder processDefinitionBuilder,
            final String actorName, final User user, final List<BarResource> connectorImplementations, final List<BarResource> generateConnectorDependencies,
            final List<BarResource> userFilters) throws BonitaException {
        final BusinessArchiveBuilder businessArchiveBuilder = buildBusinessArchiveWithConnectorAndUserFilter(processDefinitionBuilder,
                connectorImplementations, generateConnectorDependencies, userFilters);
        return deployAndEnableProcessWithActor(businessArchiveBuilder.done(), actorName, user);
    }

    public ProcessDefinition deployAndEnableProcessWithActorAndConnectorAndParameter(final ProcessDefinitionBuilder processDefinitionBuilder,
            final String actorName, final User user, final Map<String, String> parameters, final String name, final Class<? extends AbstractConnector> clazz,
            final String jarName) throws BonitaException, IOException {
        return deployAndEnableProcessWithActorAndConnectorAndParameter(processDefinitionBuilder, actorName, user,
                Arrays.asList(getContentAndBuildBarResource(name, clazz)),
                Arrays.asList(generateJarAndBuildBarResource(clazz, jarName)), parameters);
    }

    public ProcessDefinition deployAndEnableProcessWithActorAndUserFilter(final ProcessDefinitionBuilder processDefinitionBuilder, final String actorName,
            final User user, final List<BarResource> generateFilterDependencies, final List<BarResource> userFilters)
            throws BonitaException {
        return deployAndEnableProcessWithActorAndConnectorAndUserFilter(processDefinitionBuilder, actorName, user, Collections.<BarResource>emptyList(),
                generateFilterDependencies, userFilters);
    }

    private static BusinessArchiveBuilder buildBusinessArchiveWithConnectorAndUserFilter(final ProcessDefinitionBuilder processDefinitionBuilder,
            final List<BarResource> connectorImplementations, final List<BarResource> generateConnectorDependencies, final List<BarResource> userFilters)
            throws InvalidProcessDefinitionException {
        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(
                processDefinitionBuilder.done());
        for (final BarResource barResource : connectorImplementations) {
            businessArchiveBuilder.addConnectorImplementation(barResource);
        }

        for (final BarResource barResource : generateConnectorDependencies) {
            businessArchiveBuilder.addClasspathResource(barResource);
        }

        for (final BarResource barResource : userFilters) {
            businessArchiveBuilder.addUserFilters(barResource);
        }
        return businessArchiveBuilder;
    }

    private static BarResource getContentAndBuildBarResource(final String name, final Class<? extends Connector> clazz) throws IOException {
        final InputStream stream = clazz.getResourceAsStream(name);
        assertNotNull(stream);
        final byte[] byteArray = IOUtils.toByteArray(stream);
        stream.close();
        return new BarResource(name, byteArray);
    }

     private static BarResource generateJarAndBuildBarResource(final Class<?> clazz, final String name) throws IOException {
        final byte[] data = IOUtil.generateJar(clazz);
        return new BarResource(name, data);
    }


}
