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

import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.core.category.CategoryService;
import org.bonitasoft.engine.core.category.exception.SCategoryException;

/**
 * Transaction content to remove a list of categories from a Process definition.
 * 
 * @author Emmanuel Duchastenier
 */
public class RemoveCategoriesFromProcessDefinition implements TransactionContent {

    private final long processDefinitionId;

    private final CategoryService categoryService;

    private final List<Long> categoryIds;

    public RemoveCategoriesFromProcessDefinition(final long processDefinitionId, final List<Long> categoryIds, final CategoryService categoryService) {
        this.processDefinitionId = processDefinitionId;
        this.categoryIds = categoryIds;
        this.categoryService = categoryService;
    }

    @Override
    public void execute() throws SCategoryException, SBonitaException {
        categoryService.removeCategoriesFromProcessDefinition(processDefinitionId, categoryIds);
    }

}
