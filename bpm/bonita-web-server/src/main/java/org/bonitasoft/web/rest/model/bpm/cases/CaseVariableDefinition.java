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

import static org.bonitasoft.web.rest.model.bpm.cases.CaseVariableItem.ATTRIBUTE_CASE_ID;
import static org.bonitasoft.web.rest.model.bpm.cases.CaseVariableItem.ATTRIBUTE_NAME;

import org.bonitasoft.web.toolkit.client.data.item.Definitions;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;
import org.bonitasoft.web.toolkit.client.data.item.attribute.ItemAttribute;
import org.bonitasoft.web.toolkit.client.data.item.attribute.validator.StringMaxLengthValidator;

/**
 * @author Colin PUY
 */
public class CaseVariableDefinition extends ItemDefinition<CaseVariableItem> {

    private static final String API_URL = "../API/bpm/caseVariable";

    public static final String TOKEN = "caseVariable";

    public static CaseVariableDefinition get() {
        return (CaseVariableDefinition) Definitions.get(TOKEN);
    }

    @Override
    protected String defineToken() {
        return TOKEN;
    }

    @Override
    protected String defineAPIUrl() {
        return API_URL;
    }

    @Override
    protected void defineAttributes() {
        createAttribute(CaseVariableItem.ATTRIBUTE_NAME, ItemAttribute.TYPE.STRING);
        createAttribute(CaseVariableItem.ATTRIBUTE_TYPE, ItemAttribute.TYPE.STRING);
        createAttribute(CaseVariableItem.ATTRIBUTE_DESCRIPTION, ItemAttribute.TYPE.TEXT);
        createAttribute(CaseVariableItem.ATTRIBUTE_VALUE, ItemAttribute.TYPE.STRING)
                .removeValidator(StringMaxLengthValidator.class.getName());
    }

    @Override
    protected void definePrimaryKeys() {
        setPrimaryKeys(ATTRIBUTE_CASE_ID, ATTRIBUTE_NAME);
    }

    @Override
    protected CaseVariableItem _createItem() {
        return new CaseVariableItem();
    }

}
