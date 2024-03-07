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
package org.bonitasoft.web.rest.model.identity;

import org.bonitasoft.web.toolkit.client.data.item.IItem;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;
import org.bonitasoft.web.toolkit.client.data.item.attribute.ItemAttribute;
import org.bonitasoft.web.toolkit.client.data.item.attribute.validator.StringRegexpValidator;

/**
 * @author Paul AMAR
 */
public abstract class AbstractContactDataDefinition extends ItemDefinition {

    @Override
    protected void defineAttributes() {
        createAttribute(AbstractContactDataItem.ATTRIBUTE_EMAIL, ItemAttribute.TYPE.EMAIL);
        createAttribute(AbstractContactDataItem.ATTRIBUTE_PHONE, ItemAttribute.TYPE.STRING)
                .addValidator(new StringRegexpValidator("[\\d\\.\\(\\)#\\+]*"));
        createAttribute(AbstractContactDataItem.ATTRIBUTE_MOBILE, ItemAttribute.TYPE.STRING)
                .addValidator(new StringRegexpValidator("[\\d\\.\\(\\)#\\+]*"));
        createAttribute(AbstractContactDataItem.ATTRIBUTE_FAX, ItemAttribute.TYPE.STRING)
                .addValidator(new StringRegexpValidator("[\\d\\.\\(\\)#\\+]*"));
        createAttribute(AbstractContactDataItem.ATTRIBUTE_BUILDING, ItemAttribute.TYPE.STRING);
        createAttribute(AbstractContactDataItem.ATTRIBUTE_ROOM, ItemAttribute.TYPE.STRING);
        createAttribute(AbstractContactDataItem.ATTRIBUTE_ADDRESS, ItemAttribute.TYPE.STRING);
        createAttribute(AbstractContactDataItem.ATTRIBUTE_ZIPCODE, ItemAttribute.TYPE.STRING);
        createAttribute(AbstractContactDataItem.ATTRIBUTE_CITY, ItemAttribute.TYPE.STRING);
        createAttribute(AbstractContactDataItem.ATTRIBUTE_STATE, ItemAttribute.TYPE.STRING);
        createAttribute(AbstractContactDataItem.ATTRIBUTE_COUNTRY, ItemAttribute.TYPE.STRING);
        createAttribute(AbstractContactDataItem.ATTRIBUTE_WEBSITE, ItemAttribute.TYPE.URL);

    }

    @Override
    protected abstract IItem _createItem();
}
