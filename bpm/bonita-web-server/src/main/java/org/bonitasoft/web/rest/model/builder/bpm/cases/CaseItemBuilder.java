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
package org.bonitasoft.web.rest.model.builder.bpm.cases;

import org.bonitasoft.web.rest.model.bpm.cases.CaseItem;

public class CaseItemBuilder {

    private long processId;

    private String jsonVariables;

    private long userId;

    public static CaseItemBuilder aCaseItem() {
        return new CaseItemBuilder();
    }

    public CaseItemBuilder withProcessId(final long processId) {
        this.processId = processId;
        return this;
    }

    public CaseItemBuilder withUserId(final long userId) {
        this.userId = userId;
        return this;
    }

    public CaseItemBuilder withVariables(final String jsonVariables) {
        this.jsonVariables = jsonVariables;
        return this;
    }

    public CaseItem build() {
        final CaseItem item = new CaseItem();
        setAttributeIfNotNull(item, CaseItem.ATTRIBUTE_PROCESS_ID, processId);
        setAttributeIfNotNull(item, CaseItem.ATTRIBUTE_STARTED_BY_SUBSTITUTE_USER_ID, userId);
        setAttributeIfNotNull(item, CaseItem.ATTRIBUTE_VARIABLES, jsonVariables);
        return item;
    }

    private void setAttributeIfNotNull(final CaseItem caseItem, final String attributeName,
            final Object attributeValue) {
        if (attributeValue != null) {
            caseItem.setAttribute(attributeName, attributeValue);
        }
    }
}
