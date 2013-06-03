/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.api.impl.transaction.process;

import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.core.category.CategoryService;

/**
 * @author Yanyan Liu
 */
public class GetProcessDefinitionIdsOfCategory implements TransactionContentWithResult<List<Long>> {

    private final long categoryId;

    private final CategoryService categoryService;

    private List<Long> processDefinitionIds;

    public GetProcessDefinitionIdsOfCategory(final long categoryId, final CategoryService categoryService) {
        this.categoryId = categoryId;
        this.categoryService = categoryService;
    }

    @Override
    public void execute() throws SBonitaException {
        processDefinitionIds = categoryService.getProcessDefinitionIdsOfCategory(categoryId);
    }

    @Override
    public List<Long> getResult() {
        return processDefinitionIds;
    }

}
