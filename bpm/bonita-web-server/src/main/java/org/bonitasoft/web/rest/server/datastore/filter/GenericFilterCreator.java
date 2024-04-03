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
package org.bonitasoft.web.rest.server.datastore.filter;

import java.io.Serializable;

import org.bonitasoft.web.rest.server.datastore.converter.AttributeConverter;
import org.bonitasoft.web.toolkit.client.data.item.attribute.ItemAttribute.TYPE;

/**
 * @author Vincent Elcrin
 * @author Emmanuel Duchastenier
 */
public class GenericFilterCreator implements FilterCreator {

    protected final AttributeConverter fieldConverter;

    public GenericFilterCreator(AttributeConverter fieldConverter) {
        this.fieldConverter = fieldConverter;
    }

    @Override
    public Filter<? extends Serializable> create(String attribute, String value) {
        return new Filter<>(new Field(attribute, fieldConverter), getTypedValue(attribute, value));
    }

    private Value<? extends Serializable> getTypedValue(String attributeName, String attributeValue) {
        if (fieldConverter.getValueTypeMapping().get(attributeName) == TYPE.BOOLEAN) {
            return new BooleanValue(attributeValue);
        }
        return new StrValue(attributeValue);
    }

}
