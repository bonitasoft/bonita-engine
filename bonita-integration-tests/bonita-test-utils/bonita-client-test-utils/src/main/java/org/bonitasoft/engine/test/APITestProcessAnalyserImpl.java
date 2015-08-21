/*******************************************************************************
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
 ******************************************************************************/

package org.bonitasoft.engine.test;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.InvalidSessionException;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

/**
 * @author mazourd
 */
public class APITestProcessAnalyserImpl implements APITestProcessAnalyser {

    private ProcessAPI processAPI;

    public APITestProcessAnalyserImpl(ProcessAPI processAPI){
        this.processAPI = processAPI;
    }
    @Override
    public void checkFlowNodeWasntExecuted(final long processInstancedId, final String flowNodeName) {
        final List<ArchivedActivityInstance> archivedActivityInstances = getProcessAPI().getArchivedActivityInstances(processInstancedId, 0, 200,
                ActivityInstanceCriterion.DEFAULT);
        for (final ArchivedActivityInstance archivedActivityInstance : archivedActivityInstances) {
            assertFalse(flowNodeName.equals(archivedActivityInstance.getName()));
        }
    }

    @Override
    public void checkWasntExecuted(final ProcessInstance parentProcessInstance, final String flowNodeName) throws InvalidSessionException, SearchException {
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 20);
        searchOptionsBuilder.filter(ArchivedFlowNodeInstanceSearchDescriptor.PARENT_PROCESS_INSTANCE_ID, parentProcessInstance.getId());
        searchOptionsBuilder.filter(ArchivedFlowNodeInstanceSearchDescriptor.NAME, flowNodeName);
        final SearchResult<ArchivedFlowNodeInstance> searchArchivedActivities = getProcessAPI().searchArchivedFlowNodeInstances(searchOptionsBuilder.done());
        assertTrue(searchArchivedActivities.getCount() == 0);
    }

    @Override
    public void updateActivityInstanceVariablesWithOperations(final String updatedValue, final long activityInstanceId, final String dataName,
            final boolean isTransient)
            throws InvalidExpressionException, UpdateException {
        final Operation stringOperation = BuildTestUtil.buildStringOperation(dataName, updatedValue, isTransient);
        final List<Operation> operations = new ArrayList<Operation>();
        operations.add(stringOperation);
        getProcessAPI().updateActivityInstanceVariables(operations, activityInstanceId, null);
    }

    public ProcessAPI getProcessAPI() {
        return processAPI;
    }

    public void switchProcessAPI(ProcessAPI processAPI) {
        this.processAPI = processAPI;
    }
}
