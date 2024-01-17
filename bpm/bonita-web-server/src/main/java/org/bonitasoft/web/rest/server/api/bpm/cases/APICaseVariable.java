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

import java.util.List;
import java.util.Map;

import org.bonitasoft.web.rest.model.bpm.cases.CaseVariableDefinition;
import org.bonitasoft.web.rest.model.bpm.cases.CaseVariableItem;
import org.bonitasoft.web.rest.server.api.ConsoleAPI;
import org.bonitasoft.web.rest.server.datastore.bpm.cases.CaseVariableDatastore;
import org.bonitasoft.web.rest.server.framework.api.APIHasGet;
import org.bonitasoft.web.rest.server.framework.api.APIHasSearch;
import org.bonitasoft.web.rest.server.framework.api.APIHasUpdate;
import org.bonitasoft.web.rest.server.framework.api.Datastore;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;

/**
 * @author Colin PUY
 */
public class APICaseVariable extends ConsoleAPI<CaseVariableItem> implements APIHasSearch<CaseVariableItem>,
        APIHasUpdate<CaseVariableItem>, APIHasGet<CaseVariableItem> {

    private APICaseVariableAttributeChecker attributeChecker = new APICaseVariableAttributeChecker();

    @Override
    public CaseVariableItem runUpdate(APIID id, Map<String, String> attributes) {
        attributeChecker.checkUpdateAttributes(attributes);
        id.setItemDefinition(CaseVariableDefinition.get());
        CaseVariableItem item = CaseVariableItem.fromIdAndAttributes(id, attributes);
        ((CaseVariableDatastore) getDefaultDatastore()).updateVariableValue(item.getCaseId(), item.getName(),
                item.getType(), item.getValue());
        return item;
    }

    @Override
    public ItemSearchResult<CaseVariableItem> runSearch(int page, int resultsByPage, String search, String orders,
            Map<String, String> filters, List<String> deploys, List<String> counters) {
        attributeChecker.checkSearchFilters(filters);
        long caseId = Long.valueOf(filters.get(CaseVariableItem.ATTRIBUTE_CASE_ID));
        return ((CaseVariableDatastore) getDefaultDatastore()).findByCaseId(caseId, page, resultsByPage);
    }

    @Override
    public CaseVariableItem runGet(APIID id, List<String> deploys, List<String> counters) {
        id.setItemDefinition(CaseVariableDefinition.get());
        long caseId = id.getPartAsLong(CaseVariableItem.ATTRIBUTE_CASE_ID);
        String variableName = id.getPart(CaseVariableItem.ATTRIBUTE_NAME);
        return ((CaseVariableDatastore) getDefaultDatastore()).findById(caseId, variableName);
    }

    @Override
    protected Datastore defineDefaultDatastore() {
        return new CaseVariableDatastore(getEngineSession());
    }

    @Override
    protected ItemDefinition defineItemDefinition() {
        return CaseVariableDefinition.get();
    }

    /*
     * Only used for tests because we cannot pass anything to constructor due to design restrictions
     */
    public void setAttributeChecker(APICaseVariableAttributeChecker attributeChecker) {
        this.attributeChecker = attributeChecker;
    }
}
