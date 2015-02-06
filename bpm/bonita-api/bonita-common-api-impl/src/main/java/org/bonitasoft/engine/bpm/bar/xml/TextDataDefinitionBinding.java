/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.bpm.bar.xml;

import java.util.Map;

import org.bonitasoft.engine.bpm.data.impl.DataDefinitionImpl;
import org.bonitasoft.engine.bpm.data.impl.TextDataDefinitionImpl;

/**
 * @author Matthieu Chaffotte
 */
public class TextDataDefinitionBinding extends DataDefinitionBinding {

    private boolean longText;

    @Override
    public void setAttributes(final Map<String, String> attributes) {
        super.setAttributes(attributes);
        longText = Boolean.valueOf(attributes.get(XMLProcessDefinition.TEXT_DATA_DEFINITION_LONG));
    }

    @Override
    public DataDefinitionImpl getObject() {
        final TextDataDefinitionImpl dataDefinitionImpl = new TextDataDefinitionImpl(name, defaultValue);
        dataDefinitionImpl.setLongText(longText);
        if (description != null) {
            dataDefinitionImpl.setDescription(description);
        }
        dataDefinitionImpl.setClassName(className);
        dataDefinitionImpl.setTransientData(Boolean.valueOf(isTransient));
        return dataDefinitionImpl;
    }

    @Override
    public String getElementTag() {
        return XMLProcessDefinition.TEXT_DATA_DEFINITION_NODE;
    }

}
