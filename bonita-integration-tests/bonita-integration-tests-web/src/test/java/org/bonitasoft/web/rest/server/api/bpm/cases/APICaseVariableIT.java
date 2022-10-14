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

import static org.bonitasoft.test.toolkit.bpm.ProcessVariable.*;
import static org.bonitasoft.web.toolkit.client.data.APIID.makeAPIID;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.test.toolkit.bpm.ProcessVariable;
import org.bonitasoft.test.toolkit.bpm.TestCase;
import org.bonitasoft.test.toolkit.bpm.TestProcessFactory;
import org.bonitasoft.test.toolkit.organization.TestUser;
import org.bonitasoft.test.toolkit.organization.TestUserFactory;
import org.bonitasoft.web.rest.model.bpm.cases.CaseVariableDefinition;
import org.bonitasoft.web.rest.model.bpm.cases.CaseVariableItem;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.test.AbstractConsoleTest;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.junit.Test;

/**
 * @author Colin PUY
 */
public class APICaseVariableIT extends AbstractConsoleTest {

    private APICaseVariable apiCaseVariable;

    @Override
    public void consoleTestSetUp() throws Exception {
        apiCaseVariable = new APICaseVariable();
        apiCaseVariable.setCaller(getAPICaller(getInitiator().getSession(), "API/bpm/caseVariable"));
    }

    @Override
    protected TestUser getInitiator() {
        return TestUserFactory.getJohnCarpenter();
    }

    private APIID buildAPIID(TestCase aCase, ProcessVariable expectedVariable) {
        APIID apiid = makeAPIID(Arrays.asList(String.valueOf(aCase.getId()), expectedVariable.getName()));
        apiid.setItemDefinition(CaseVariableDefinition.get());
        return apiid;
    }

    private TestCase createCaseWithVariable(ProcessVariable... expectedVariable) {
        return TestProcessFactory.createProcessWithVariables("aProcessWithVariablesVariable", expectedVariable)
                .addActor(getInitiator()).startCase();
    }

    private Map<String, String> buildUpdateAttributes(String newValue, String type) {
        Map<String, String> attributes = new HashMap<>();
        attributes.put(CaseVariableItem.ATTRIBUTE_VALUE, newValue);
        attributes.put(CaseVariableItem.ATTRIBUTE_TYPE, type);
        return attributes;
    }

    private Map<String, String> buildCaseIdFilter(TestCase aCase) {
        Map<String, String> filters = new HashMap<>();
        filters.put(CaseVariableItem.ATTRIBUTE_CASE_ID, String.valueOf(aCase.getId()));
        return filters;
    }

    @Test
    public void weCanUpdateALongValue() throws Exception {
        ProcessVariable expectedVariable = createLongVariable(1L);
        TestCase aCase = createCaseWithVariable(expectedVariable);
        String newLongValue = "2";
        Map<String, String> attributes = buildUpdateAttributes(newLongValue, Long.class.getName());

        CaseVariableItem variable = apiCaseVariable.runUpdate(buildAPIID(aCase, expectedVariable), attributes);

        assertEquals(newLongValue, variable.getValue());
    }

    @Test
    public void weCanUpdateAnIntegerValue() throws Exception {
        ProcessVariable expectedVariable = createIntVariable(1);
        TestCase aCase = createCaseWithVariable(expectedVariable);
        String newIntValue = "2";
        Map<String, String> attributes = buildUpdateAttributes(newIntValue, Integer.class.getName());

        CaseVariableItem variable = apiCaseVariable.runUpdate(buildAPIID(aCase, expectedVariable), attributes);

        assertEquals(newIntValue, variable.getValue());
    }

    @Test
    public void weCanUpdateABooleanValue() throws Exception {
        ProcessVariable expectedVariable = createBooleanVariable(true);
        TestCase aCase = createCaseWithVariable(expectedVariable);
        String newBooleanValue = "false";
        Map<String, String> attributes = buildUpdateAttributes(newBooleanValue, Boolean.class.getName());

        CaseVariableItem variable = apiCaseVariable.runUpdate(buildAPIID(aCase, expectedVariable), attributes);

        assertEquals(newBooleanValue, variable.getValue());
    }

    @Test
    public void weCanUpdateAStringValue() throws Exception {
        ProcessVariable expectedVariable = createStringVariable("aString");
        TestCase aCase = createCaseWithVariable(expectedVariable);
        String newStringValue = "aNewString";
        Map<String, String> attributes = buildUpdateAttributes(newStringValue, String.class.getName());

        CaseVariableItem variable = apiCaseVariable.runUpdate(buildAPIID(aCase, expectedVariable), attributes);

        assertEquals(newStringValue, variable.getValue());
    }

    @Test
    public void weCanUpdateADoubleValue() throws Exception {
        ProcessVariable expectedVariable = createDoubleVariable(12.3d);
        TestCase aCase = createCaseWithVariable(expectedVariable);
        String newDoubleValue = "46.35";
        Map<String, String> attributes = buildUpdateAttributes(newDoubleValue, Double.class.getName());

        CaseVariableItem variable = apiCaseVariable.runUpdate(buildAPIID(aCase, expectedVariable), attributes);

        assertEquals(newDoubleValue, variable.getValue());
    }

    @Test
    public void search() throws Exception {
        TestCase aCase = createCaseWithVariable(createLongVariable(1L), createIntVariable(1),
                createStringVariable("hello"));
        Map<String, String> filters = buildCaseIdFilter(aCase);

        ItemSearchResult<CaseVariableItem> searchResults = apiCaseVariable.runSearch(0, 2, null, null, filters, null,
                null);

        assertEquals(3L, searchResults.getTotal());
        assertEquals(2, searchResults.getLength());
        assertEquals(2, searchResults.getResults().size());
    }

    @Test
    public void getReturnACaseVariable() throws Exception {
        ProcessVariable expectedVariable = createLongVariable(1L);
        TestCase aCase = createCaseWithVariable(expectedVariable);
        APIID apiid = buildAPIID(aCase, expectedVariable);

        CaseVariableItem variable = apiCaseVariable.runGet(apiid, null, null);

        assertEquals(expectedVariable.getName(), variable.getName());
        assertEquals(expectedVariable.getClassName(), variable.getType());
        assertEquals(expectedVariable.getDefaultValue().getContent(), variable.getValue());
    }
}
