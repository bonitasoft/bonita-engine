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
package org.bonitasoft.engine.core.category.model.builder.impl;

import org.bonitasoft.engine.core.category.model.builder.SProcessCategoryMappingBuilder;
import org.bonitasoft.engine.core.category.model.builder.SProcessCategoryMappingBuilderFactory;
import org.bonitasoft.engine.core.category.model.impl.SProcessCategoryMappingImpl;

/**
 * @author Matthieu Chaffotte
 */
public class SProcessCategoryMappingBuilderFactoryImpl implements SProcessCategoryMappingBuilderFactory {

    @Override
    public SProcessCategoryMappingBuilder createNewInstance(final long categoryId, final long processId) {
        final SProcessCategoryMappingImpl entity = new SProcessCategoryMappingImpl(categoryId, processId);
        return new SProcessCategoryMappingBuilderImpl(entity);
    }

    @Override
    public String getIdKey() {
        return "id";
    }

    @Override
    public String getCategoryIdKey() {
        return "categoryId";
    }

    @Override
    public String getProcessIdKey() {
        return "processId";
    }

}
