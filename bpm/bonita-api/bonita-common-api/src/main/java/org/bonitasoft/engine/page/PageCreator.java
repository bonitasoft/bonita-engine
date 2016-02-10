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
package org.bonitasoft.engine.page;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Laurent Leseigneur
 */
public class PageCreator implements Serializable {

    public enum PageField {
        NAME, DISPLAY_NAME, DESCRIPTION, CONTENT_NAME, CONTENT_TYPE, PROCESS_DEFINITION_ID
    }

    private static final long serialVersionUID = 8174091386958635983L;

    private final Map<PageField, Serializable> fields;

    public PageCreator(final String name, final String zipName) {
        fields = new HashMap<>();
        fields.put(PageField.NAME, name);
        fields.put(PageField.CONTENT_NAME, zipName);
        setContentType(ContentType.PAGE);
    }

    public PageCreator(String name, String zipName, String contentType, Long processDefinitionId) {
        this(name, zipName);
        setContentType(contentType);
        setProcessDefinitionId(processDefinitionId);
    }

    public String getName() {
        return fields.get(PageField.NAME).toString();
    }

    public PageCreator setDescription(final String description) {
        fields.put(PageField.DESCRIPTION, description);
        return this;
    }

    public PageCreator setDisplayName(final String displayName) {
        fields.put(PageField.DISPLAY_NAME, displayName);
        return this;
    }

    public PageCreator setContentType(final String contentType) {
        fields.put(PageField.CONTENT_TYPE, contentType);
        return this;
    }

    public PageCreator setProcessDefinitionId(final Long processDefinitionId) {
        fields.put(PageField.PROCESS_DEFINITION_ID, processDefinitionId);
        return this;
    }

    public Map<PageField, Serializable> getFields() {
        return fields;
    }

    @Override
    public String toString() {
        return "PageCreator [fields=" + fields + "]";
    }

}
