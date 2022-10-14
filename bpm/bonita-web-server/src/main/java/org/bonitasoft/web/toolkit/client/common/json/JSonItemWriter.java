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
package org.bonitasoft.web.toolkit.client.common.json;

import java.util.LinkedList;
import java.util.List;

import org.bonitasoft.web.toolkit.client.data.item.IItem;

/**
 * This class is a set of functions used to write the JSon to return to the GWT client side
 *
 * @author Séverin Moussel
 * @param <T>
 *        The class of the Items to write
 */
public class JSonItemWriter<T extends IItem> {

    /**
     * The items to write
     */
    private final List<IItem> itemList = new LinkedList<>();

    /**
     * If you use this constructor, you will have to use one of the append functions.
     */
    public JSonItemWriter() {

    }

    public JSonItemWriter<T> append(final List<T> datas) {
        this.itemList.addAll(datas);
        return this;
    }

    public JSonItemWriter<T> append(final IItem item) {
        this.itemList.add(item);
        return this;
    }

    @Override
    public String toString() {
        return JSonSerializer.serializeCollection(this.itemList);
    }

    public static String itemToJSON(final IItem item) {
        return JSonSerializer.serialize(item);
    }

}
