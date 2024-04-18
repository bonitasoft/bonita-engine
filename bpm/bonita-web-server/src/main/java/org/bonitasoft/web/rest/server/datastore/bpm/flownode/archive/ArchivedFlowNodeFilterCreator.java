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
package org.bonitasoft.web.rest.server.datastore.bpm.flownode.archive;

import java.io.Serializable;

import org.bonitasoft.web.rest.model.bpm.flownode.IFlowNodeItem;
import org.bonitasoft.web.rest.server.datastore.bpm.flownode.FlowNodeTypeConverter;
import org.bonitasoft.web.rest.server.datastore.bpm.flownode.archive.converter.ArchivedFlowNodeSearchDescriptorConverter;
import org.bonitasoft.web.rest.server.datastore.filter.Field;
import org.bonitasoft.web.rest.server.datastore.filter.Filter;
import org.bonitasoft.web.rest.server.datastore.filter.GenericFilterCreator;
import org.bonitasoft.web.rest.server.datastore.filter.Value;

class ArchivedFlowNodeFilterCreator extends GenericFilterCreator {

    ArchivedFlowNodeFilterCreator(ArchivedFlowNodeSearchDescriptorConverter converter) {
        super(converter);
    }

    @Override
    public Filter<? extends Serializable> create(String attribute, String value) {
        if (IFlowNodeItem.ATTRIBUTE_TYPE.equals(attribute)) {
            return new Filter<>(new Field(attribute, fieldConverter), new Value<>(value, new FlowNodeTypeConverter()));
        }
        return super.create(attribute, value);
    }
}
