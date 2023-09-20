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
package org.bonitasoft.web.rest.server.datastore.bpm.process;

import java.io.Serializable;
import java.util.*;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.model.bpm.process.ProcessCategoryItem;
import org.bonitasoft.web.rest.server.datastore.CommonDatastore;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasAdd;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasDelete;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIForbiddenException;
import org.bonitasoft.web.toolkit.client.common.i18n.T_;
import org.bonitasoft.web.toolkit.client.common.util.MapUtil;
import org.bonitasoft.web.toolkit.client.data.APIID;

/**
 * @author Séverin Moussel
 */
public class ProcessCategoryDatastore extends CommonDatastore<ProcessCategoryItem, Serializable> implements
        DatastoreHasAdd<ProcessCategoryItem>,
        DatastoreHasDelete {

    public ProcessCategoryDatastore(final APISession engineSession) {
        super(engineSession);
    }

    @Override
    protected ProcessCategoryItem convertEngineToConsoleItem(final Serializable item) {
        // No conversion here
        return null;
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // C.R.U.D.S
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void delete(final List<APIID> ids) {
        MapUtil.iterate(
                buildCategoriesIdByProcessIdMapping(ids),
                new MapUtil.ForEach<>() {

                    @Override
                    protected void apply(Long processId, List<Long> categoriesId) {
                        removeCategoriesFromProcess(processId, categoriesId);
                    }
                });
    }

    private void removeCategoriesFromProcess(Long processId, List<Long> categoriesId) {
        try {
            getProcessAPI().removeCategoriesFromProcess(processId, categoriesId);
        } catch (BonitaException e) {
            throw new APIException(e);
        }
    }

    private Map<Long, List<Long>> buildCategoriesIdByProcessIdMapping(List<APIID> ids) {
        Map<Long, List<Long>> categoriesIdByProcessId = new HashMap<>();
        for (APIID apiid : ids) {
            Long processId = apiid.getPartAsLong(ProcessCategoryItem.ATTRIBUTE_PROCESS_ID);
            Long categoryId = apiid.getPartAsLong(ProcessCategoryItem.ATTRIBUTE_CATEGORY_ID);
            if (categoriesIdByProcessId.containsKey(processId)) {
                categoriesIdByProcessId.get(processId).add(categoryId);
            } else {
                ArrayList<Long> categoryIds = new ArrayList<>();
                categoryIds.add(categoryId);
                categoriesIdByProcessId.put(processId, categoryIds);
            }
        }
        return categoriesIdByProcessId;
    }

    @Override
    public ProcessCategoryItem add(final ProcessCategoryItem item) {
        try {
            getProcessAPI().addCategoriesToProcess(item.getProcessId().toLong(),
                    Collections.singletonList(item.getCategoryId().toLong()));

            return item;
        } catch (AlreadyExistsException e) {
            throw new APIForbiddenException(new T_("This category has already been added to this process"), e);
        } catch (final BonitaException e) {
            throw new APIException(e);
        }
    }

    protected ProcessAPI getProcessAPI() {
        try {
            return TenantAPIAccessor.getProcessAPI(getEngineSession());
        } catch (BonitaException e) {
            throw new APIException(e);
        }
    }
}
