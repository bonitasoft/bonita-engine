/**
 * Copyright (C) 2022 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.web.rest.server.api.bpm.process;

import static org.bonitasoft.web.rest.server.framework.utils.SearchOptionsBuilderUtil.computeIndex;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bpm.parameter.ParameterCriterion;
import org.bonitasoft.engine.bpm.parameter.ParameterInstance;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.session.InvalidSessionException;
import org.bonitasoft.web.rest.model.bpm.process.ProcessParameterDefinition;
import org.bonitasoft.web.rest.model.bpm.process.ProcessParameterItem;
import org.bonitasoft.web.rest.server.api.ConsoleAPI;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIItemNotFoundException;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.Definitions;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;

/**
 * @author Nicolas Tith
 */
public class APIProcessParameter extends ConsoleAPI<ProcessParameterItem> {

    @Override
    protected ItemDefinition defineItemDefinition() {
        return Definitions.get(ProcessParameterDefinition.TOKEN);
    }

    protected ProcessAPI getProcessAPI()
            throws InvalidSessionException, BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return TenantAPIAccessor.getProcessAPI(getEngineSession());
    }

    @Override
    /*
     * id is a composite id which contain processId and parameterName
     */
    public ProcessParameterItem get(final APIID id) {
        final List<String> ids = id.getIds();
        final String processId = ids.get(0);
        final String parameterName = ids.get(1);
        try {
            final ProcessAPI processAPI = getProcessAPI();
            final Long lProcessid = Long.valueOf(processId);
            final ParameterInstance parameterInstance = processAPI.getParameterInstance(lProcessid, parameterName);
            final ProcessDefinition processDef = processAPI.getProcessDefinition(lProcessid);
            final ProcessDeploymentInfo processDeploy = processAPI.getProcessDeploymentInfo(lProcessid);

            if (parameterInstance == null) {
                throw new APIItemNotFoundException("parameter",
                        APIID.makeAPIID(String.valueOf(lProcessid), parameterName));
            }

            if (processDef == null) {
                throw new APIItemNotFoundException("process", APIID.makeAPIID(String.valueOf(lProcessid)));
            }

            final String paramValue = parameterInstance.getValue() == null ? ""
                    : parameterInstance.getValue().toString();
            return new ProcessParameterItem(processId, parameterInstance.getName(), parameterInstance.getType(),
                    paramValue, parameterInstance.getDescription(), processDeploy.getDisplayName(),
                    processDef.getVersion());
        } catch (final Exception e) {
            throw new APIException(e);
        }
    }

    @Override
    public String defineDefaultSearchOrder() {
        return ParameterCriterion.NAME_ASC.toString();
    }

    @Override
    public ItemSearchResult<ProcessParameterItem> search(final int page, final int resultsByPage, final String search,
            final String orders,
            final Map<String, String> filters) {
        final List<ProcessParameterItem> items = new ArrayList<>();
        List<ParameterInstance> parameters = new ArrayList<>();
        int parametersCount = 0;
        long processId = -1;

        try {
            final ProcessAPI processAPI = getProcessAPI();
            if (filters != null) {
                if (filters.containsKey(ProcessParameterItem.FILTER_PROCESS_ID)) {
                    final String value = filters.get(ProcessParameterItem.FILTER_PROCESS_ID);
                    processId = Long.parseLong(value);
                }
            }
            if (processId != -1) {
                parameters = processAPI.getParameterInstances(processId, computeIndex(page, resultsByPage),
                        resultsByPage, ParameterCriterion.valueOf(orders.toUpperCase().replace(" ", "_")));
                parametersCount = processAPI.getNumberOfParameterInstances(processId);
            }
            for (final ParameterInstance p : parameters) {
                final String paramValue = p.getValue() == null ? "" : p.getValue().toString();
                items.add(new ProcessParameterItem(String.valueOf(processId), p.getName(), p.getType(), paramValue,
                        p.getDescription(), "", ""));
            }
            return new ItemSearchResult<>(page, resultsByPage, parametersCount, items);
        } catch (final Exception e) {
            throw new APIException(e);
        }
    }

    @Override
    protected void fillDeploys(final ProcessParameterItem item, final List<String> deploys) {
    }

    @Override
    protected void fillCounters(final ProcessParameterItem item, final List<String> counters) {
    }
}
