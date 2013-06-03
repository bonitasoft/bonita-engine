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

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.core.category.CategoryService;

/**
 * @author Matthieu Chaffotte
 */
public class GetUncategorizedProcessIds implements TransactionContentWithResult<List<Long>> {

    private final CategoryService categoryService;

    private final List<Long> processDefinitionIds;

    private final List<Long> uncategorizedProcessIds;

    public GetUncategorizedProcessIds(final CategoryService categoryService, final List<Long> processDefinitionIds) {
        this.categoryService = categoryService;
        this.processDefinitionIds = processDefinitionIds;
        uncategorizedProcessIds = new ArrayList<Long>(processDefinitionIds);
    }

    @Override
    public void execute() throws SBonitaException {
        final List<Long> categorizedProcessIds = categoryService.getCategorizedProcessIds(processDefinitionIds);
        uncategorizedProcessIds.removeAll(categorizedProcessIds);
    }

    @Override
    public List<Long> getResult() {
        return uncategorizedProcessIds;
    }

}
