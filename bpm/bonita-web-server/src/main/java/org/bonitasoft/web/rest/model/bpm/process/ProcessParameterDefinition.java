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
package org.bonitasoft.web.rest.model.bpm.process;

import org.bonitasoft.web.toolkit.client.data.item.Definitions;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;
import org.bonitasoft.web.toolkit.client.data.item.attribute.ItemAttribute;
import org.bonitasoft.web.toolkit.client.data.item.attribute.validator.StringMaxLengthValidator;

/**
 * @author Yongtao Guo, Haojie Yuan, Anthony Birembaut
 */
public class ProcessParameterDefinition extends ItemDefinition<ProcessParameterItem> {

    /**
     * Singleton
     */
    public static ProcessParameterDefinition get() {
        return (ProcessParameterDefinition) Definitions.get(TOKEN);
    }

    public static final String TOKEN = "processParameter";

    /**
     * the URL of users resource
     */
    private static final String API_URL = "../API/bpm/processParameter";

    @Override
    public String defineToken() {
        return TOKEN;
    }

    @Override
    protected void definePrimaryKeys() {
        setPrimaryKeys(ProcessParameterItem.ATTRIBUTE_PROCESS_ID, ProcessParameterItem.ATTRIBUTE_NAME);
    }

    @Override
    protected String defineAPIUrl() {
        return API_URL;
    }

    /**
     * categoryList
     */
    @Override
    protected void defineAttributes() {

        createAttribute(ProcessParameterItem.ATTRIBUTE_NAME, ItemAttribute.TYPE.STRING);
        createAttribute(ProcessParameterItem.ATTRIBUTE_TYPE, ItemAttribute.TYPE.STRING);
        createAttribute(ProcessParameterItem.ATTRIBUTE_DESCRIPTION, ItemAttribute.TYPE.TEXT);
        createAttribute(ProcessParameterItem.ATTRIBUTE_VALUE, ItemAttribute.TYPE.STRING)
                .removeValidator(StringMaxLengthValidator.class.getName());

    }

    @Override
    public ProcessParameterItem _createItem() {
        return new ProcessParameterItem();
    }
}
