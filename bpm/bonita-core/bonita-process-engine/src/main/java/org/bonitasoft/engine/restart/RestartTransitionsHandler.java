/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.restart;

import java.util.List;

import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionReadException;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.TransitionService;
import org.bonitasoft.engine.core.process.instance.model.STransitionInstance;
import org.bonitasoft.engine.execution.work.ExecuteTransitionWork;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.work.WorkRegisterException;
import org.bonitasoft.engine.work.WorkService;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class RestartTransitionsHandler implements TenantRestartHandler {

    @Override
    public void handleRestart(final PlatformServiceAccessor platformServiceAccessor, final TenantServiceAccessor tenantServiceAccessor) throws RestartException {
        final TransitionService transitionInstanceService = tenantServiceAccessor.getTransitionInstanceService();
        final int processDefinitionIndex = tenantServiceAccessor.getBPMInstanceBuilders().getSTransitionInstanceBuilder().getProcessDefinitionIndex();
        QueryOptions searchOptions = QueryOptions.defaultQueryOptions();
        List<STransitionInstance> search;
        final WorkService workService = platformServiceAccessor.getWorkService();
        final ProcessDefinitionService processDefinitionService = tenantServiceAccessor.getProcessDefinitionService();
        try {
            do {
                search = transitionInstanceService.search(searchOptions);
                searchOptions = QueryOptions.getNextPage(searchOptions);
                for (final STransitionInstance transitionInstance : search) {
                    final long processDefinitionId = transitionInstance.getLogicalGroup(processDefinitionIndex);
                    final SProcessDefinition processDefinition = processDefinitionService.getProcessDefinition(processDefinitionId);
                    workService.registerWork(new ExecuteTransitionWork(processDefinition.getId(), transitionInstance.getParentProcessInstanceId(), transitionInstance.getId()));
                }

            } while (search.size() == searchOptions.getNumberOfResults());
        } catch (final SBonitaSearchException e) {
            handleException(e, "Unable to restart transitions: can't get them from database");
        } catch (final SProcessDefinitionNotFoundException e) {
            handleException(e, "Unable to restart transitions: process definition of a transition not found");
        } catch (final SProcessDefinitionReadException e) {
            handleException(e, "Unable to restart transitions: can't read process definition");
        } catch (final WorkRegisterException e) {
            handleException(e, "Unable to restart transitions: can't read process definition");
        }
    }

    private void handleException(final Exception e, final String message) throws RestartException {
        throw new RestartException(message, e);
    }
}
