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

import org.bonitasoft.web.toolkit.client.data.item.IItem;
import org.bonitasoft.web.toolkit.client.data.item.Item;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;

/**
 * @author Ruiheng Fan
 */
public class ProcessParameterItem extends Item {

    public ProcessParameterItem() {
        super();
    }

    public ProcessParameterItem(final IItem item) {
        super(item);
    }

    public static final String ATTRIBUTE_PROCESS_ID = "process_id";

    public static final String ATTRIBUTE_NAME = "name";

    public static final String ATTRIBUTE_TYPE = "type";

    public static final String ATTRIBUTE_VALUE = "value";

    public static final String ATTRIBUTE_DESCRIPTION = "description";

    public static final String ATTRIBUTE_PROCESS_NAME = "process_name";

    public static final String ATTRIBUTE_PROCESS_VERSION = "process_version";

    public static final String FILTER_PROCESS_ID = "process_id";

    /**
     * Default Constructor.
     *
     * @param ProcessParameter
     *        name
     * @param ProcessParameter
     *        type
     * @param ProcessParameter
     *        value
     * @param ProcessParameter
     *        description
     */
    public ProcessParameterItem(final String processId, final String name, final String type, final String value,
            final String description,
            final String processName, final String processVersion) {
        this.setAttribute(ATTRIBUTE_PROCESS_ID, processId);
        this.setAttribute(ATTRIBUTE_NAME, name);
        this.setAttribute(ATTRIBUTE_TYPE, type);
        this.setAttribute(ATTRIBUTE_VALUE, value);
        this.setAttribute(ATTRIBUTE_DESCRIPTION, description);
        this.setAttribute(ATTRIBUTE_PROCESS_NAME, processName);
        this.setAttribute(ATTRIBUTE_PROCESS_VERSION, processVersion);
    }

    @Override
    public ItemDefinition<ProcessParameterItem> getItemDefinition() {
        return new ProcessParameterDefinition();
    }

    public String getName() {
        return getAttributeValue(ATTRIBUTE_NAME);
    }

    public String getType() {
        return getAttributeValue(ATTRIBUTE_TYPE);
    }

    public String getValue() {
        return getAttributeValue(ATTRIBUTE_VALUE);
    }

    public String getDescription() {
        return getAttributeValue(ATTRIBUTE_DESCRIPTION);
    }

    public void setValue(String value) {
        setAttribute(ATTRIBUTE_VALUE, value);
    }

}
