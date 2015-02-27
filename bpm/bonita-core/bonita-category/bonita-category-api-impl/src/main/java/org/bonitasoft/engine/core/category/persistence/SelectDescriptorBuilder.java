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
package org.bonitasoft.engine.core.category.persistence;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.core.category.model.SCategory;
import org.bonitasoft.engine.core.category.model.SProcessCategoryMapping;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;

/**
 * @author Yanyan Liu
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SelectDescriptorBuilder {

    private static final String PROCESS_ID = "processId";

    public static SelectByIdDescriptor<SCategory> getCategory(final long categoryId) {
        return new SelectByIdDescriptor<SCategory>("getCategoryById", SCategory.class, categoryId);
    }

    public static SelectOneDescriptor<SCategory> getCategory(final String categoryName) {
        final Map<String, Object> parameters = Collections.singletonMap("name", (Object) categoryName);
        return new SelectOneDescriptor<SCategory>("getCategoryByName", parameters, SCategory.class);
    }

    public static SelectOneDescriptor<Long> getNumberOfElement(final String elementName, final Class<? extends PersistentObject> clazz) {
        final Map<String, Object> parameters = Collections.emptyMap();
        return new SelectOneDescriptor<Long>("getNumberOf" + elementName, parameters, clazz, Long.class);
    }

    public static SelectListDescriptor<SCategory> getCategories(final String field, final OrderByType order, final int fromIndex, final int numberOfProcesses) {
        final Map<String, Object> parameters = Collections.emptyMap();
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfProcesses, SCategory.class, field, order);
        return new SelectListDescriptor<SCategory>("getCategories", parameters, SCategory.class, queryOptions);
    }

    public static SelectListDescriptor<SCategory> getCategoriesOfProcess(final long processId, final int fromIndex, final int numberOfCategories,
            final OrderByType order) {
        final Map<String, Object> parameters = Collections.singletonMap(PROCESS_ID, (Object) processId);
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfCategories, SCategory.class, "name", order);
        return new SelectListDescriptor<SCategory>("getCategoriesOfProcess", parameters, SCategory.class, queryOptions);
    }

    public static SelectListDescriptor<SCategory> getCategoriesUnrelatedToProcess(final long processId, final int fromIndex, final int numberOfCategories,
            final OrderByType order) {
        final Map<String, Object> parameters = Collections.singletonMap(PROCESS_ID, (Object) processId);
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfCategories, SCategory.class, "name", order);
        return new SelectListDescriptor<SCategory>("getCategoriesUnrelatedToProcess", parameters, SCategory.class, queryOptions);
    }

    public static SelectOneDescriptor<Long> getNumberOfCategoriesOfProcess(final long processId) {
        final Map<String, Object> parameters = Collections.singletonMap(PROCESS_ID, (Object) processId);
        return new SelectOneDescriptor<Long>("getNumberOfCategoriesOfProcess", parameters, SProcessCategoryMapping.class);
    }

    public static SelectOneDescriptor<Long> getNumberOfCategoriesUnrelatedToProcess(final long processId) {
        final Map<String, Object> parameters = Collections.singletonMap(PROCESS_ID, (Object) processId);
        return new SelectOneDescriptor<Long>("getNumberOfCategoriesUnrelatedToProcess", parameters, SProcessCategoryMapping.class);
    }

    public static SelectOneDescriptor<Long> getNumberOfCategorizedProcessIds(final List<Long> processIds) {
        final Map<String, Object> parameters = Collections.singletonMap("processIds", (Object) processIds);
        return new SelectOneDescriptor<Long>("getNumberOfCategorizedProcessIds", parameters, SProcessCategoryMapping.class, Long.class);
    }

    public static SelectOneDescriptor<Long> isCategoryExistsInProcess(final long categoryId, final long processDefinitionId) {
        final Map<String, Object> parameters = new HashMap<String, Object>(2);
        parameters.put("categoryId", categoryId);
        parameters.put("processDefinitionId", processDefinitionId);
        return new SelectOneDescriptor<Long>("isCategoryExistsInProcess", parameters, SProcessCategoryMapping.class);
    }

    public static SelectListDescriptor<SProcessCategoryMapping> getCategoryMappingOfProcessAndCategories(final long processDefinitionId,
            final List<Long> categoryIds, final int fromIndex, final int maxResults) {
        final QueryOptions queryOptions = buildQueryOptionsForCategoryMappingOrderedByCategoryId(fromIndex, maxResults, OrderByType.ASC);
        final Map<String, Object> parameters = new HashMap<String, Object>(2);
        parameters.put("categoryIds", categoryIds);
        parameters.put("processDefinitionId", processDefinitionId);
        return new SelectListDescriptor<SProcessCategoryMapping>("getCategoryMappingOfProcessAndCategories", parameters, SProcessCategoryMapping.class,
                queryOptions);
    }

    public static QueryOptions buildQueryOptionsForCategoryMappingOrderedByCategoryId(final int fromIndex, final int maxResults, final OrderByType order) {
        return new QueryOptions(fromIndex, maxResults, SProcessCategoryMapping.class, "categoryId", order);
    }

}
