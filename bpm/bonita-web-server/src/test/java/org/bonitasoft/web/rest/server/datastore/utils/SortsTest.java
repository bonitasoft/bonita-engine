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

import java.util.List;

import org.bonitasoft.engine.search.Order;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Vincent Elcrin
 */
public class SortsTest {

    @Test
    public void testNullSortListing() throws Exception {
        Sorts sorts = new Sorts(null);

        List<Sort> sortList = sorts.asList();

        Assert.assertTrue(sortList.isEmpty());
    }

    @Test
    public void testEmptySortListing() throws Exception {
        Sorts sorts = new Sorts("");

        List<Sort> sortList = sorts.asList();

        Assert.assertTrue(sortList.isEmpty());
    }

    @Test
    public void testSingleSortListing() throws Exception {
        Sorts sorts = new Sorts("attribute " + Order.ASC);

        List<Sort> sortList = sorts.asList();

        Assert.assertEquals(new Sort("attribute " + Order.ASC), sortList.get(0));
    }

    @Test
    public void testMultipleSortListing() throws Exception {
        String attribute1Order = "attribute1 " + Order.ASC;
        String attribute2Order = "attribute2 " + Order.DESC;
        Sorts sorts = new Sorts(attribute1Order + "," + attribute2Order);

        List<Sort> sortList = sorts.asList();

        Assert.assertEquals(new Sort(attribute1Order), sortList.get(0));
        Assert.assertEquals(new Sort(attribute2Order), sortList.get(1));
    }
}
