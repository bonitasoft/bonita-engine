/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.api.impl.transaction.category;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.core.category.CategoryService;

/**
 * @author Celine Souchet
 */
public class GetNumberOfCategoriesUnrelatedToProcess implements TransactionContentWithResult<Long> {

    private final CategoryService categoryService;

    private final long processDefinitionId;

    private long numberOfCategories;

    public GetNumberOfCategoriesUnrelatedToProcess(final CategoryService categoryService,
            final long processDefinitionId) {
        super();
        this.categoryService = categoryService;
        this.processDefinitionId = processDefinitionId;
    }

    @Override
    public void execute() throws SBonitaException {
        numberOfCategories = categoryService.getNumberOfCategoriesUnrelatedToProcess(processDefinitionId);
    }

    @Override
    public Long getResult() {
        return numberOfCategories;
    }

}
