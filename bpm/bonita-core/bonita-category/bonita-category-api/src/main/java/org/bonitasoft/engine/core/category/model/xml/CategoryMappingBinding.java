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

import java.util.Map;

import org.bonitasoft.engine.xml.ElementBinding;

/**
 * @author Yanyan Liu
 */
public class CategoryMappingBinding extends ElementBinding {

    private String name;

    private String description;

    private String creator;

    private String creationDate;

    private String lastUpdateDate;

    private XMLProcessDefinitionsMapping processDefinitionsMapping;

    @Override
    public void setAttributes(final Map<String, String> attributes) {
        name = attributes.get(XMLCategoryMapping.NAME);
    }

    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) {
        if (XMLCategoryMapping.DESCRIPTION.equals(name)) {
            description = value;
        }
        if (XMLCategoryMapping.CREATOR.equals(name)) {
            creator = value;
        }
        if (XMLCategoryMapping.CREATION_DATE.equals(name)) {
            creationDate = value;
        }
        if (XMLCategoryMapping.LAST_UPDATE_DATE.equals(name)) {
            lastUpdateDate = value;
        }
    }

    @Override
    public void setChildObject(final String name, final Object value) {
        if (XMLCategoryMapping.PROCESS_DEFINITIONS.equals(name)) {
            processDefinitionsMapping = (XMLProcessDefinitionsMapping) value;
        }
    }

    @Override
    public XMLCategoryMapping getObject() {
        return new XMLCategoryMapping(name, description, creator, creationDate, lastUpdateDate, processDefinitionsMapping);
    }

    @Override
    public String getElementTag() {
        return XMLCategoryMapping.CATEGORY;
    }

}
