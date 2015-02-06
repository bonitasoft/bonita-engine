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
package org.bonitasoft.engine.api.impl.transaction.category;

import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.core.category.CategoryService;
import org.bonitasoft.engine.core.category.model.SProcessCategoryMapping;
import org.bonitasoft.engine.core.category.model.builder.SProcessCategoryMappingBuilderFactory;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;

/**
 * @author Yanyan Liu
 * @author Celine Souchet
 */
public class RemoveProcessDefinitionsOfCategory implements TransactionContent {

    private final CategoryService categoryService;

    private final long categoryId;

    private final long processDefinitionId;

    public RemoveProcessDefinitionsOfCategory(final CategoryService categoryService, final long categoryId) {
        this.categoryService = categoryService;
        this.categoryId = categoryId;
        processDefinitionId = -1;
    }

    public RemoveProcessDefinitionsOfCategory(final long processDefinitionId, final CategoryService categoryService) {
        this.processDefinitionId = processDefinitionId;
        this.categoryService = categoryService;
        categoryId = -1;
    }

    @Override
    public void execute() throws SBonitaException {
        final FilterOption filterOption;
        if (categoryId != -1) {
            filterOption = new FilterOption(SProcessCategoryMapping.class, BuilderFactory.get(SProcessCategoryMappingBuilderFactory.class).getCategoryIdKey(), categoryId);
        } else {
            filterOption = new FilterOption(SProcessCategoryMapping.class, BuilderFactory.get(SProcessCategoryMappingBuilderFactory.class).getProcessIdKey(), processDefinitionId);
        }
        final OrderByOption order = new OrderByOption(SProcessCategoryMapping.class, BuilderFactory.get(SProcessCategoryMappingBuilderFactory.class).getIdKey(), OrderByType.ASC);
        final QueryOptions queryOptions = new QueryOptions(0, 100, Collections.singletonList(order),
                Collections.singletonList(filterOption), null);

        long deletedProcessCategoryMappings;
        do {
            final List<SProcessCategoryMapping> processCategoryMappings = categoryService.searchProcessCategoryMappings(queryOptions);
            deletedProcessCategoryMappings = categoryService.deleteProcessCategoryMappings(processCategoryMappings);
        } while (deletedProcessCategoryMappings > 0);
    }
}
