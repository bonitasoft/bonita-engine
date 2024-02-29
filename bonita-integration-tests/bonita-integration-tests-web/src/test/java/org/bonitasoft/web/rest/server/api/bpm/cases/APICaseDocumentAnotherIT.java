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

import static junit.framework.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.test.toolkit.bpm.TestCase;
import org.bonitasoft.test.toolkit.bpm.TestProcessFactory;
import org.bonitasoft.test.toolkit.organization.TestUser;
import org.bonitasoft.test.toolkit.organization.TestUserFactory;
import org.bonitasoft.web.rest.model.bpm.cases.CaseDocumentItem;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.test.AbstractConsoleTest;
import org.junit.Test;

/**
 * @author Colin PUY
 */
public class APICaseDocumentAnotherIT extends AbstractConsoleTest {

    private APICaseDocument apiCaseDocument;

    @Override
    public void consoleTestSetUp() throws Exception {
        apiCaseDocument = new APICaseDocument();
        apiCaseDocument.setCaller(getAPICaller(getInitiator().getSession(), "API/bpm/caseDocument"));
    }

    @Override
    protected TestUser getInitiator() {
        return TestUserFactory.getJohnCarpenter();
    }

    @Test
    public void caseDocumentsCanBeCountedByCaseId() throws Exception {
        TestCase startCase = TestProcessFactory.getProcessWithDocumentAttached().addActor(getInitiator()).startCase();
        Map<String, String> caseIdfilter = buildCaseIdFilter(startCase.getId());

        ItemSearchResult<CaseDocumentItem> searchResult = apiCaseDocument.runSearch(0, 0, null, null, caseIdfilter,
                null, null);

        assertEquals(1L, searchResult.getTotal());
    }

    private Map<String, String> buildCaseIdFilter(long caseId) {
        Map<String, String> filters = new HashMap<>();
        filters.put(CaseDocumentItem.ATTRIBUTE_CASE_ID, String.valueOf(caseId));
        return filters;
    }

}
