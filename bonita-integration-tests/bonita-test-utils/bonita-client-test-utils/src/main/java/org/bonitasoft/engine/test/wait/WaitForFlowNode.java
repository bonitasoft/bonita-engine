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
