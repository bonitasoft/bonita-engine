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

import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.IItem;
import org.bonitasoft.web.toolkit.client.data.item.Item;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;

/**
 * @author Séverin Moussel
 */
public class ProcessConnectorItem extends Item {

    public ProcessConnectorItem() {
        super();
    }

    public ProcessConnectorItem(final IItem item) {
        super(item);
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ATTRIBUTES
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static final String ATTRIBUTE_NAME = "definition_id";

    public static final String ATTRIBUTE_VERSION = "definition_version";

    public static final String ATTRIBUTE_PROCESS_ID = "process_id";

    public static final String ATTRIBUTE_IMPLEMENTATION_NAME = "impl_name";

    public static final String ATTRIBUTE_IMPLEMENTATION_VERSION = "impl_version";

    public static final String ATTRIBUTE_CLASSNAME = "classname";

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SETTERS AND GETTERS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Setters

    public void setName(final String name) {
        setAttribute(ATTRIBUTE_NAME, name);
    }

    public void setVersion(final String version) {
        setAttribute(ATTRIBUTE_VERSION, version);
    }

    public void setProcessId(final String id) {
        setAttribute(ATTRIBUTE_PROCESS_ID, id);
    }

    public void setProcessId(final APIID id) {
        setAttribute(ATTRIBUTE_PROCESS_ID, id);
    }

    public void setProcessId(final long id) {
        setAttribute(ATTRIBUTE_PROCESS_ID, id);
    }

    public void setImplementationName(final String name) {
        setAttribute(ATTRIBUTE_IMPLEMENTATION_NAME, name);
    }

    public void setImplementationVersion(final String version) {
        setAttribute(ATTRIBUTE_IMPLEMENTATION_VERSION, version);
    }

    public void setClassname(final String classname) {
        setAttribute(ATTRIBUTE_CLASSNAME, classname);
    }

    // Getters

    public String getName() {
        return getAttributeValue(ATTRIBUTE_NAME);
    }

    public String getVersion() {
        return getAttributeValue(ATTRIBUTE_VERSION);
    }

    public APIID getProcessId() {
        return getAttributeValueAsAPIID(ATTRIBUTE_PROCESS_ID);
    }

    public String getImplementationName() {
        return getAttributeValue(ATTRIBUTE_IMPLEMENTATION_NAME);
    }

    public String getImplementationVersion() {
        return getAttributeValue(ATTRIBUTE_IMPLEMENTATION_VERSION);
    }

    public String getClassname() {
        return getAttributeValue(ATTRIBUTE_CLASSNAME);
    }

    // Deploys

    public ProcessItem getProcess() {
        return new ProcessItem(getDeploy(ATTRIBUTE_PROCESS_ID));
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // UTILS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public ItemDefinition getItemDefinition() {
        return new ProcessConnectorDefinition();
    }

}
