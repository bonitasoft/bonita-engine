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
package org.bonitasoft.web.rest.server.api.bpm.cases;

import static java.util.Arrays.asList;
import static org.bonitasoft.web.toolkit.client.data.APIID.makeAPIID;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.web.rest.model.bpm.cases.CaseVariableDefinition;
import org.bonitasoft.web.rest.model.bpm.cases.CaseVariableItem;
import org.bonitasoft.web.rest.server.APITestWithMock;
import org.bonitasoft.web.rest.server.datastore.bpm.cases.CaseVariableDatastore;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 * @author Colin PUY
 */
public class APICaseVariableTest extends APITestWithMock {

    private APICaseVariable apiCaseVariable;

    @Mock
    private CaseVariableDatastore datastore;

    @Before
    public void initializeMocks() {
        initMocks(this);
        apiCaseVariable = spy(new APICaseVariable());

        doReturn(datastore).when(apiCaseVariable).getDefaultDatastore();
    }

    private APIID buildAPIID(long caseId, String variableName) {
        APIID makeAPIID = makeAPIID(asList(String.valueOf(caseId), variableName));
        makeAPIID.setItemDefinition(CaseVariableDefinition.get());
        return makeAPIID;
    }

    private Map<String, String> buildUpdateParameters(String newValue, String className) {
        Map<String, String> map = new HashMap<>();
        map.put(CaseVariableItem.ATTRIBUTE_VALUE, newValue);
        map.put(CaseVariableItem.ATTRIBUTE_TYPE, className);
        return map;
    }

    private Map<String, String> buildCaseIdFilter(long caseId) {
        Map<String, String> filters = new HashMap<>();
        filters.put(CaseVariableItem.ATTRIBUTE_CASE_ID, String.valueOf(caseId));
        return filters;
    }

    @Test
    public void updateUpdateTheVariableValue() throws Exception {
        long caseId = 1L;
        String variableName = "aName";
        String newValue = "newValue";
        Map<String, String> parameters = buildUpdateParameters(newValue, String.class.getName());
        APIID apiid = buildAPIID(caseId, variableName);

        apiCaseVariable.runUpdate(apiid, parameters);

        verify(datastore).updateVariableValue(caseId, variableName, String.class.getName(), newValue);
    }

    @Test
    public void weCheckAttributesBeforeUpdating() throws Exception {
        APICaseVariableAttributeChecker attributeChecker = mock(APICaseVariableAttributeChecker.class);
        apiCaseVariable.setAttributeChecker(attributeChecker);
        APIID apiid = buildAPIID(1L, "aName");
        Map<String, String> attributes = buildUpdateParameters("newValue", String.class.getName());

        apiCaseVariable.runUpdate(apiid, attributes);

        verify(attributeChecker).checkUpdateAttributes(attributes);
    }

    @Test
    public void weCheckFiltersBeforeSearching() throws Exception {
        APICaseVariableAttributeChecker attributeChecker = mock(APICaseVariableAttributeChecker.class);
        apiCaseVariable.setAttributeChecker(attributeChecker);
        Map<String, String> filters = buildCaseIdFilter(1L);

        apiCaseVariable.runSearch(0, 10, null, null, filters, null, null);

        verify(attributeChecker).checkSearchFilters(filters);
    }

    @Test
    public void searchIsFilteredByCaseId() throws Exception {
        long expectedCaseId = 1L;
        Map<String, String> filters = buildCaseIdFilter(expectedCaseId);

        apiCaseVariable.runSearch(0, 10, null, null, filters, null, null);

        verify(datastore).findByCaseId(expectedCaseId, 0, 10);
    }

    @Test
    public void getSearchInDatastoreById() throws Exception {
        long expectedCaseId = 1L;
        String expectedVariableName = "aName";
        APIID apiid = buildAPIID(expectedCaseId, expectedVariableName);

        apiCaseVariable.runGet(apiid, null, null);

        verify(datastore).findById(expectedCaseId, expectedVariableName);
    }
}
