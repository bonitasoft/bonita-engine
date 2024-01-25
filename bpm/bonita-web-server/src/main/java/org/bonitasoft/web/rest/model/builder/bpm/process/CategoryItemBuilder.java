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
package org.bonitasoft.web.rest.model.builder.bpm.process;

import org.bonitasoft.web.rest.model.bpm.process.CategoryItem;

/**
 * @author Colin PUY
 */
public class CategoryItemBuilder {

    private final String description = "aDescription";
    private final String name = "aCategory";

    public static CategoryItemBuilder aCategoryItem() {
        return new CategoryItemBuilder();
    }

    public CategoryItem build() {
        CategoryItem category = new CategoryItem();
        category.setName(name);
        category.setDescription(description);
        return category;
    }
}
