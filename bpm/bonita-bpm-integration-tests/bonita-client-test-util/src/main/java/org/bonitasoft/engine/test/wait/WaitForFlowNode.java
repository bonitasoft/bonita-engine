/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine.test.wait;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.StateCategory;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.WaitUntil;

@Deprecated
public class WaitForFlowNode extends WaitUntil {

    private final String name;

    private final long processInstanceId;

    private String state = null;

    private StateCategory stateCategory = null;

    private FlowNodeInstance result;

    private final boolean useRootProcessInstance;

    private final ProcessAPI processAPI;

    @Deprecated
    public WaitForFlowNode(final int repeatEach, final int timeout, final String name, final long processInstanceId, final boolean useRootProcessInstance,
            final ProcessAPI processAPI) {
        super(repeatEach, timeout);
        this.name = name;
        this.processInstanceId = processInstanceId;
        this.useRootProcessInstance = useRootProcessInstance;
        this.processAPI = processAPI;
    }

    @Deprecated
    public WaitForFlowNode(final int repeatEach, final int timeout, final String name, final long processInstanceId, final String state,
            final boolean rootProcessInstance, final ProcessAPI processAPI) {
        this(repeatEach, timeout, name, processInstanceId, rootProcessInstance, processAPI);
        this.state = state;
    }

    @Deprecated
    public WaitForFlowNode(final int repeatEach, final int timeout, final String name, final long processInstanceId, final StateCategory stateCategory,
            final boolean rootProcessInstance, final ProcessAPI processAPI) {
        this(repeatEach, timeout, name, processInstanceId, rootProcessInstance, processAPI);
        this.stateCategory = stateCategory;
    }

    @Override
    protected boolean check() throws Exception {
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        if (useRootProcessInstance) {
            searchOptionsBuilder.filter(FlowNodeInstanceSearchDescriptor.ROOT_PROCESS_INSTANCE_ID, processInstanceId);
        } else {
            searchOptionsBuilder.filter(FlowNodeInstanceSearchDescriptor.PARENT_PROCESS_INSTANCE_ID, processInstanceId);
        }
        searchOptionsBuilder.filter(FlowNodeInstanceSearchDescriptor.NAME, name);
        final SearchResult<FlowNodeInstance> searchResult = processAPI.searchFlowNodeInstances(searchOptionsBuilder.done());
        final boolean found = searchResult.getCount() > 0;
        boolean check = found;
        if (found) {
            result = searchResult.getResult().get(0);
            if (state != null) {
                check = state.equals(result.getState());
            }
            if (stateCategory != null) {
                check = stateCategory.equals(result.getStateCategory());
            }
        }
        return check;
    }

    public FlowNodeInstance getResult() {
        return result;
    }
}
