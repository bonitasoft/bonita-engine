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
package org.bonitasoft.engine.core.process.definition.model.bindings;

import org.bonitasoft.engine.core.process.definition.model.impl.SAutomaticTaskDefinitionImpl;

/**
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class SAutomaticTaskDefinitionBinding extends SActivityDefinitionBinding {

    @Override
    public Object getObject() {
        final SAutomaticTaskDefinitionImpl automaticTaskDefinitionImpl = new SAutomaticTaskDefinitionImpl(id, name);
        fillNode(automaticTaskDefinitionImpl);
        return automaticTaskDefinitionImpl;
    }

    @Override
    public String getElementTag() {
        return XMLSProcessDefinition.AUTOMATIC_TASK_NODE;
    }

    @Override
    public void setChildObject(final String name, final Object value) {
        super.setChildObject(name, value);
    }

}
