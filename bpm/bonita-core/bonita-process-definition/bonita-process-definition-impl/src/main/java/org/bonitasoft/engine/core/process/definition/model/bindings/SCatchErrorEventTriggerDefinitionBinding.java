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

import org.bonitasoft.engine.core.process.definition.model.event.trigger.impl.SCatchErrorEventTriggerDefinitionImpl;

/**
 * @author Elias Ricken de Medeiros
 */
public class SCatchErrorEventTriggerDefinitionBinding extends SErrorEventTriggerDefinitionBinding {

    @Override
    public Object getObject() {
        final String errorCode = getErrorCode();
        return new SCatchErrorEventTriggerDefinitionImpl(errorCode);
    }

    @Override
    public String getElementTag() {
        return XMLSProcessDefinition.CATCH_ERROR_EVENT_TRIGGER_NODE;
    }

}
