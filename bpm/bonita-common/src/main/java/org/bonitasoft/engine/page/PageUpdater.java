/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.page;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Laurent Leseigneur
 */
public class PageUpdater implements Serializable {

    private static final long serialVersionUID = 4295108162470507415L;

    public enum PageUpdateField {

        /**
         * @deprecated since bonita 7.13 the update of name is no more supported, it will not be taken into account on
         *             the page update
         **/
        @Deprecated
        NAME, DISPLAY_NAME, DESCRIPTION, CONTENT_NAME, CONTENT_TYPE, PROCESS_DEFINITION_ID, HIDDEN
    }

    private final Map<PageUpdateField, Serializable> fields;

    public PageUpdater() {
        fields = new HashMap<>();
    }

    public PageUpdater setDescription(final String description) {
        fields.put(PageUpdateField.DESCRIPTION, description);
        return this;
    }

    /**
     * @deprecated since bonita 7.13 the update of name is no more supported, it will not be taken into account on the
     *             page update
     */
    @Deprecated
    public PageUpdater setName(final String name) {
        fields.put(PageUpdateField.NAME, name);
        return this;
    }

    public PageUpdater setDisplayName(final String displayName) {
        fields.put(PageUpdateField.DISPLAY_NAME, displayName);
        return this;
    }

    public PageUpdater setContentName(final String contentName) {
        fields.put(PageUpdateField.CONTENT_NAME, contentName);
        return this;
    }

    public PageUpdater setContentType(final String contentType) {
        fields.put(PageUpdateField.CONTENT_TYPE, contentType);
        return this;
    }

    public PageUpdater setProcessDefinitionId(final Long processDefinitionId) {
        fields.put(PageUpdateField.PROCESS_DEFINITION_ID, processDefinitionId);
        return this;
    }

    public PageUpdater setHidden(Boolean hidden) {
        fields.put(PageUpdateField.HIDDEN, hidden);
        return this;
    }

    public Map<PageUpdateField, Serializable> getFields() {
        return fields;
    }

}
