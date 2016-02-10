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

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.core.category.CategoryService;
import org.bonitasoft.engine.core.category.model.SCategory;

/**
 * @author Yanyan Liu
 */
public class GetCategory implements TransactionContentWithResult<SCategory> {

    private final CategoryService categoryService;

    private final String categoryName;

    private final long categoryId;

    private SCategory sCategory;

    public GetCategory(final CategoryService categoryService, final String categoryName) {
        this.categoryService = categoryService;
        this.categoryName = categoryName;
        categoryId = -1;
    }

    public GetCategory(final CategoryService categoryService, final long categoryId) {
        this.categoryService = categoryService;
        this.categoryId = categoryId;
        categoryName = null;
    }

    @Override
    public void execute() throws SBonitaException {
        if (categoryName != null) {
            sCategory = categoryService.getCategoryByName(categoryName);
        } else {
            sCategory = categoryService.getCategory(categoryId);
        }
    }

    @Override
    public SCategory getResult() {
        return sCategory;
    }

}
