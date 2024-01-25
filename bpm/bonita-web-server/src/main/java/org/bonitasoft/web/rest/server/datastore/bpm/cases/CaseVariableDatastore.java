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
package org.bonitasoft.web.rest.server.datastore.bpm.cases;

import static org.bonitasoft.web.rest.server.framework.utils.SearchOptionsBuilderUtil.computeIndex;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.model.bpm.cases.CaseVariableItem;
import org.bonitasoft.web.rest.server.datastore.CommonDatastore;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasSearch;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasUpdate;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.rest.server.framework.utils.converter.ConversionException;
import org.bonitasoft.web.rest.server.framework.utils.converter.TypeConverter;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIMethodNotAllowedException;
import org.bonitasoft.web.toolkit.client.data.APIID;

/**
 * @author Colin PUY
 */
public class CaseVariableDatastore extends CommonDatastore<CaseVariableItem, DataInstance>
        implements DatastoreHasSearch<CaseVariableItem>, DatastoreHasUpdate<CaseVariableItem> {

    private final TypeConverter converter = new TypeConverter();

    public CaseVariableDatastore(final APISession engineSession) {
        super(engineSession);
    }

    @Override
    protected CaseVariableItem convertEngineToConsoleItem(final DataInstance item) {
        return new CaseVariableItem(item.getContainerId(),
                item.getName(), item.getValue(), item.getClassName(), item.getDescription());
    }

    private List<CaseVariableItem> convert(final List<DataInstance> dataInstances) {
        final List<CaseVariableItem> caseVariables = new ArrayList<>();
        for (final DataInstance dataInstance : dataInstances) {
            caseVariables.add(convertEngineToConsoleItem(dataInstance));
        }
        return caseVariables;
    }

    protected ProcessAPI getEngineProcessAPI() {
        try {
            return TenantAPIAccessor.getProcessAPI(getEngineSession());
        } catch (final BonitaException e) {
            throw new APIException(e);
        }
    }

    @Override
    public CaseVariableItem update(final APIID id, final Map<String, String> attributes) {
        throw new APIMethodNotAllowedException("Not implemented / No need to / Not used");
    }

    @Override
    public ItemSearchResult<CaseVariableItem> search(final int page, final int resultsByPage, final String search,
            final String orders,
            final Map<String, String> filters) {
        throw new APIMethodNotAllowedException("Not implemented / No need to / Not used");
    }

    public void updateVariableValue(final long caseId, final String variableName, final String className,
            final String newValue) {
        try {
            final Serializable converteValue = converter.convert(className, newValue);
            getEngineProcessAPI().updateProcessDataInstance(variableName, caseId, converteValue);
        } catch (final BonitaException | ConversionException e) {
            throw new APIException("Error when updating case variable", e);
        }
    }

    public ItemSearchResult<CaseVariableItem> findByCaseId(final long caseId, final int page, final int resultsByPage) {
        try {
            final List<DataInstance> processDataInstances = getEngineProcessAPI().getProcessDataInstances(caseId,
                    computeIndex(page, resultsByPage), resultsByPage);
            return new ItemSearchResult<>(page, resultsByPage,
                    countByCaseId(caseId), convert(processDataInstances));
        } catch (final BonitaException e) {
            throw new APIException("Error when getting case variables", e);
        }
    }

    private long countByCaseId(final long caseId) throws BonitaException {
        return getEngineProcessAPI().getNumberOfProcessDataInstances(caseId);
    }

    public CaseVariableItem findById(final long caseId, final String variableName) {
        try {
            return convertEngineToConsoleItem(getEngineProcessAPI().getProcessDataInstance(variableName, caseId));
        } catch (final BonitaException e) {
            throw new APIException("Error while getting case variable", e);
        }
    }
}
