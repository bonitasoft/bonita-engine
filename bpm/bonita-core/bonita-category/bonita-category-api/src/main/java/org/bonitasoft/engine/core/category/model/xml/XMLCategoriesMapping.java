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
package org.bonitasoft.engine.core.category.model.xml;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.core.category.model.SCategory;

/**
 * @author Yanyan Liu
 */
public class XMLCategoriesMapping {

    private List<XMLCategoryMapping> categoryMappingList;

    public XMLCategoriesMapping() {
        super();
        this.categoryMappingList = new ArrayList<XMLCategoryMapping>();
    }

    public XMLCategoriesMapping(final List<XMLCategoryMapping> categoryMappings) {
        super();
        this.categoryMappingList = categoryMappings;
    }

    public List<XMLCategoryMapping> getCategoryList() {
        return this.categoryMappingList;
    }

    public void addCategory(final XMLCategoryMapping categoryMapping) {
        if (this.categoryMappingList == null) {
            this.categoryMappingList = new ArrayList<XMLCategoryMapping>();
        }
        this.categoryMappingList.add(categoryMapping);
    }

    public void deleteCategory(final String name) {
        for (int i = 0; i < this.categoryMappingList.size(); i++) {
            if (this.categoryMappingList.get(i).getName().equals(name)) {
                this.categoryMappingList.remove(i);
            }
        }
        // for (final XMLCategoryMapping categoryMapping : this.categoryMappingList) {
        // if (categoryMapping.getName().equals(name)) {
        // categoryMappingList.remove(categoryMapping);
        // }
        // }
    }

    public XMLCategoryMapping getCategory(final String name) {
        for (final XMLCategoryMapping categoryMapping : this.categoryMappingList) {
            if (categoryMapping.getName().equals(name)) {
                return categoryMapping;
            }
        }
        return null;
    }

    public List<XMLCategoryMapping> getCategoriesByProcessDefinitionId(final String processDefinitionId) {
        List<XMLCategoryMapping> categoryMappings = null;
        for (final XMLCategoryMapping categoryMapping : this.categoryMappingList) {
            if (categoryMapping.getProcessDefinitions().contains(processDefinitionId)) {
                if (categoryMappings == null) {
                    categoryMappings = new ArrayList<XMLCategoryMapping>();
                }
                categoryMappings.add(categoryMapping);
            }
        }
        return categoryMappings;
    }

    public XMLProcessDefinitionsMapping getProcessDefinitions(final String categoryName) {
        for (final XMLCategoryMapping categoryMapping : this.categoryMappingList) {
            if (categoryMapping.getName().equals(categoryName)) {
                return categoryMapping.getProcessDefinitions();
            }
        }
        return null;
    }

    public void updateCategory(final String categoryName, final SCategory newCategory) {
        final XMLCategoryMapping categoryMapping = this.getCategory(categoryName);
        categoryMapping.update(newCategory);
    }

    public boolean contains(final XMLCategoryMapping xmlCategoryMapping) {
        return this.categoryMappingList.contains(xmlCategoryMapping);
    }

    public void removeProcessDefiniton(final String processDefinitionId) {
        for (final XMLCategoryMapping categoryMapping : this.categoryMappingList) {
            if (categoryMapping.getProcessDefinitions().contains(processDefinitionId)) {
                categoryMapping.deleteProcessDefinition(processDefinitionId);
            }
        }
    }
}
