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

import org.bonitasoft.engine.core.category.model.SCategory;

/**
 * @author Yanyan Liu
 */
public class XMLCategoryMapping implements Comparable<XMLCategoryMapping> {

    public static final String CATEGORIES = "categories";

    public static final String CATEGORY = "category";

    public static final String NAME = "name";

    public static final String DESCRIPTION = "description";

    public static final String CREATOR = "creator";

    public static final String CREATION_DATE = "creationDate";

    public static final String LAST_UPDATE_DATE = "lastUpdateDate";

    public static final String PROCESS_DEFINITIONS = "processDefinitions";

    public static final String PROCESS_DEFINITION = "processDefinition";

    public static final String PROCESS_DEFINITION_ID = "processDefinitionId";

    private String name;

    private String description;

    private String creator;

    private String creationDate;

    private String lastUpdateDate;

    private XMLProcessDefinitionsMapping processDefinitions;

    public XMLCategoryMapping(final String name) {
        this.name = name;
        this.processDefinitions = new XMLProcessDefinitionsMapping();
    }

    public XMLCategoryMapping(final String name, final String description, final String creator, final String creationDate, final String lastUpdateDate,
            final XMLProcessDefinitionsMapping processDefinitions) {
        super();
        this.name = name;
        this.description = description;
        this.creator = creator;
        this.creationDate = creationDate;
        this.lastUpdateDate = lastUpdateDate;
        this.processDefinitions = processDefinitions;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public String getCreator() {
        return this.creator;
    }

    public String getCreationDate() {
        return this.creationDate;
    }

    public String getLastUpdateDate() {
        return this.lastUpdateDate;
    }

    public XMLProcessDefinitionsMapping getProcessDefinitions() {
        if (this.processDefinitions == null) {
            this.processDefinitions = new XMLProcessDefinitionsMapping();
        }
        return this.processDefinitions;
    }

    public void addProcessDefinition(final String processDefinitionId) {
        if (this.processDefinitions == null) {
            this.processDefinitions = new XMLProcessDefinitionsMapping();
        }
        this.processDefinitions.addId(processDefinitionId);
    }

    public boolean deleteProcessDefinition(final String processDefinitionId) {
        return this.processDefinitions.deleteId(processDefinitionId);
    }

    public void deleteAllProcessDefinition() {
        this.processDefinitions.deleteAll();
    }

    public void update(final SCategory newCategory) {
        if (newCategory.getName() != null) {
            this.name = newCategory.getName();
        }
        if (newCategory.getDescription() != null) {
            this.description = newCategory.getDescription();
        }
        this.lastUpdateDate = String.valueOf(System.currentTimeMillis());
    }

    @Override
    public int compareTo(final XMLCategoryMapping categoryMapping) {
        return this.name.compareTo(categoryMapping.getName());
    }
}
