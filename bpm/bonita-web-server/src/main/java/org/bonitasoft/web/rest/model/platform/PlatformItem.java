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
package org.bonitasoft.web.rest.model.platform;

import org.bonitasoft.web.toolkit.client.data.item.IItem;
import org.bonitasoft.web.toolkit.client.data.item.Item;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;

/**
 * @author Zhiheng Yang
 */
public class PlatformItem extends Item {

    public PlatformItem() {
        super();
    }

    public PlatformItem(final IItem item) {
        super(item);
    }

    public final static String ATTRIBUTE_VERSION = "version";

    public final static String ATTRIBUTE_PRE_VERSION = "previousVersion";

    public final static String ATTRIBUTE_INIT_VERSION = "initialVersion";

    public final static String ATTRIBUTE_CREATED_DATE = "created";

    public final static String ATTRIBUTE_CREATEDBY = "createdBy";

    public final static String ATTRIBUTE_STATE = "state";

    public PlatformItem(final String version, final String preVersion, final String initVersion,
            final String createdDate, final String createdBy,
            final String state) {

        this.setAttribute(ATTRIBUTE_VERSION, version);
        this.setAttribute(ATTRIBUTE_PRE_VERSION, preVersion);
        this.setAttribute(ATTRIBUTE_INIT_VERSION, initVersion);
        this.setAttribute(ATTRIBUTE_CREATED_DATE, createdDate);
        this.setAttribute(ATTRIBUTE_CREATEDBY, createdBy);
        this.setAttribute(ATTRIBUTE_STATE, state);

    }

    @Override
    public ItemDefinition getItemDefinition() {
        return new PlatformDefinition();
    }

}
