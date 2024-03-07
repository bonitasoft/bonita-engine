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

import org.bonitasoft.web.rest.model.bpm.flownode.ActivityItem;
import org.bonitasoft.web.rest.model.bpm.flownode.FlowNodeItem;
import org.bonitasoft.web.rest.server.datastore.bpm.flownode.FlowNodeTypeConverter;
import org.bonitasoft.web.rest.server.datastore.converter.ActivityAttributeConverter;
import org.bonitasoft.web.rest.server.datastore.converter.EmptyAttributeConverter;
import org.bonitasoft.web.rest.server.datastore.converter.StringValueConverter;
import org.bonitasoft.web.rest.server.datastore.filter.Filter.Operator;

/**
 * @author Florine Boudin
 */
public class ActivityFilterCreator extends GenericFilterCreator {

    public ActivityFilterCreator() {
        super(new EmptyAttributeConverter());
    }

    /*
     * (non-Javadoc)
     * @see org.bonitasoft.web.rest.server.credentials.filter.FilterCreator#create(java.lang.String, java.lang.String)
     */
    @Override
    public Filter<? extends Serializable> create(String attribute, String value) {
        if (ActivityItem.ATTRIBUTE_TYPE.equals(attribute)) {
            return new Filter<>(new Field(attribute, new ActivityAttributeConverter()),
                    new Value<>(value, new FlowNodeTypeConverter()));
        }
        if (ActivityItem.FILTER_IS_FAILED.equals(attribute)) {
            Operator operator = Boolean.valueOf(value) ? Operator.EQUAL : Operator.DIFFERENT_FROM;
            return new Filter<>(new Field(ActivityItem.ATTRIBUTE_STATE, new ActivityAttributeConverter()),
                    new Value<>(FlowNodeItem.VALUE_STATE_FAILED, new StringValueConverter()), operator);
        }
        return new GenericFilterCreator(new ActivityAttributeConverter()).create(attribute, value);
    }

}
