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

import java.util.List;

import org.bonitasoft.engine.xml.XMLNode;

/**
 * @author Yanyan Liu
 * @author Matthieu Chaffotte
 */
public class CategoryMappingNodeBuilder {

    public static XMLNode getDocument(final XMLCategoriesMapping xmlCategoriesMapping) {
        final XMLNode document = getRootNode();
        final List<XMLCategoryMapping> categoryMappingList = xmlCategoriesMapping.getCategoryList();
        if (categoryMappingList != null) {
            for (final XMLCategoryMapping categoryMapping : categoryMappingList) {
                document.addChild(getCategoryNode(categoryMapping));
            }
        }
        return document;
    }

    public static XMLNode getCategoryNode(final XMLCategoryMapping xmlCategoryMapping) {
        final XMLNode categoryMapping = new XMLNode(XMLCategoryMapping.CATEGORY);
        categoryMapping.addAttribute(XMLCategoryMapping.NAME, xmlCategoryMapping.getName());
        final XMLNode description = getSubNode(XMLCategoryMapping.DESCRIPTION, xmlCategoryMapping.getDescription());
        categoryMapping.addChild(description);
        final XMLNode creator = getSubNode(XMLCategoryMapping.CREATOR, xmlCategoryMapping.getCreator());
        categoryMapping.addChild(creator);
        final XMLNode creationDate = getSubNode(XMLCategoryMapping.CREATION_DATE, xmlCategoryMapping.getCreationDate());
        categoryMapping.addChild(creationDate);
        final XMLNode lastUpdateDate = getSubNode(XMLCategoryMapping.LAST_UPDATE_DATE, xmlCategoryMapping.getLastUpdateDate());
        categoryMapping.addChild(lastUpdateDate);
        final XMLProcessDefinitionsMapping processDefinitions = xmlCategoryMapping.getProcessDefinitions();
        if (processDefinitions != null) {
            final XMLNode processDefinitionNode = getProcessDefinitions(processDefinitions);
            categoryMapping.addChild(processDefinitionNode);
        }
        return categoryMapping;
    }

    public static XMLNode getRootNode() {
        return new XMLNode(XMLCategoryMapping.CATEGORIES);
    }

    private static XMLNode getSubNode(final String name, final String content) {
        final XMLNode desc = new XMLNode(name);
        desc.setContent(content);
        return desc;
    }

    private static XMLNode getProcessDefinitions(final XMLProcessDefinitionsMapping processDefinitions) {
        final XMLNode processDefinitionsNode = new XMLNode(XMLCategoryMapping.PROCESS_DEFINITIONS);
        for (final String id : processDefinitions.getIds()) {
            final XMLNode processDefinitionNode = new XMLNode(XMLCategoryMapping.PROCESS_DEFINITION);
            // processDefinitionNode.addAttribute(XMLCategoryMapping.PROCESS_DEFINITION_ID, id);
            processDefinitionNode.setContent(id);
            processDefinitionsNode.addChild(processDefinitionNode);
        }
        return processDefinitionsNode;
    }

}
