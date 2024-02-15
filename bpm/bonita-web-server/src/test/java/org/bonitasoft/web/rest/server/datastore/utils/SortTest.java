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
package org.bonitasoft.web.rest.server.datastore.utils;

import org.bonitasoft.engine.bpm.flownode.ActivityInstanceSearchDescriptor;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.web.rest.model.bpm.flownode.ActivityItem;
import org.bonitasoft.web.rest.server.datastore.converter.ActivityAttributeConverter;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Vincent Elcrin
 */
public class SortTest {

    @Test
    public void testAscSortCreation() {
        String order = "attribute " + Order.ASC;

        Sort sort = new Sort(order);

        Assert.assertEquals(sort.getField(), "attribute");
        Assert.assertEquals(sort.getOrder(), Order.ASC);
    }

    @Test
    public void testDescSortCreation() {
        String order = "attribute " + Order.DESC;

        Sort sort = new Sort(order);

        Assert.assertEquals(sort.getField(), "attribute");
        Assert.assertEquals(sort.getOrder(), Order.DESC);
    }

    @Test
    public void testDefaultSortOrder() {
        String order = "attribute";

        Sort sort = new Sort(order);

        Assert.assertEquals(sort.getField(), "attribute");
        Assert.assertEquals(sort.getOrder(), Sort.DEFAULT_ORDER);
    }

    @Test
    public void testToStringForParamWithConvertionValue() {
        Sort sort = new Sort(ActivityItem.ATTRIBUTE_ROOT_CASE_ID + " " + Order.DESC, new ActivityAttributeConverter());

        Assert.assertEquals(sort.toString(), ActivityInstanceSearchDescriptor.PROCESS_INSTANCE_ID + " " + Order.DESC);
    }

    @Test
    public void testToStringForParamWithoutConvertionValue() {
        Sort sort = new Sort("TestParam " + Order.DESC, new ActivityAttributeConverter());

        Assert.assertEquals(sort.toString(), "TestParam " + Order.DESC);
    }

}
