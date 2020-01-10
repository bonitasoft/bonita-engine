/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.core.process.definition.model.event.trigger.impl;

import org.bonitasoft.engine.core.process.definition.model.event.trigger.SErrorEventTriggerDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SEventTriggerType;

/**
 * @author Elias Ricken de Medeiros
 */
public abstract class SErrorEventTriggerDefinitionImpl extends SEventTriggerDefinitionImpl
        implements SErrorEventTriggerDefinition {

    private static final long serialVersionUID = -8002085238119587513L;

    private final String errorCode;

    public SErrorEventTriggerDefinitionImpl(final String errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (errorCode == null ? 0 : errorCode.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SErrorEventTriggerDefinitionImpl other = (SErrorEventTriggerDefinitionImpl) obj;
        if (errorCode == null) {
            if (other.errorCode != null) {
                return false;
            }
        } else if (!errorCode.equals(other.errorCode)) {
            return false;
        }
        return true;
    }

    @Override
    public SEventTriggerType getEventTriggerType() {
        return SEventTriggerType.ERROR;
    }
}
