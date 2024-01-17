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
package org.bonitasoft.web.rest.model.bpm.cases;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.Item;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;

/**
 * @author Colin PUY
 */
public class CaseVariableItem extends Item {

    public static final String ATTRIBUTE_CASE_ID = "case_id";

    public static final String ATTRIBUTE_NAME = "name";

    public static final String ATTRIBUTE_TYPE = "type";

    public static final String ATTRIBUTE_VALUE = "value";

    public static final String ATTRIBUTE_DESCRIPTION = "description";

    public CaseVariableItem() {
    }

    public CaseVariableItem(final long caseId, final String name, final Serializable value, final String type,
            final String description) {
        setAttribute(ATTRIBUTE_CASE_ID, String.valueOf(caseId));
        setAttribute(ATTRIBUTE_NAME, name);
        setAttribute(ATTRIBUTE_VALUE, String.valueOf(value));
        setAttribute(ATTRIBUTE_TYPE, type);
        setAttribute(ATTRIBUTE_DESCRIPTION, description);
    }

    public static CaseVariableItem fromIdAndAttributes(final APIID apiid, final Map<String, String> attributes) {
        return new CaseVariableItem(apiid.getPartAsLong(ATTRIBUTE_CASE_ID),
                apiid.getPart(ATTRIBUTE_NAME), attributes.get(ATTRIBUTE_VALUE),
                attributes.get(ATTRIBUTE_TYPE), attributes.get(ATTRIBUTE_DESCRIPTION));
    }

    @Override
    public ItemDefinition getItemDefinition() {
        return CaseVariableDefinition.get();
    }

    public long getCaseId() {
        return getAttributeValueAsLong(ATTRIBUTE_CASE_ID);
    }

    public String getName() {
        return getAttributeValue(ATTRIBUTE_NAME);
    }

    public String getType() {
        return getAttributeValue(ATTRIBUTE_TYPE);
    }

    public String getValue() {
        return getAttributeValue(ATTRIBUTE_VALUE);
    }

    public String getDescription() {
        return getAttributeValue(ATTRIBUTE_DESCRIPTION);
    }

    public void setValue(final String value) {
        setAttribute(ATTRIBUTE_VALUE, value);
    }

}
