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
package org.bonitasoft.engine.bpm.flownode.impl.internal;

import org.bonitasoft.engine.bpm.flownode.SignalEventTriggerDefinition;

/**
 * @author Matthieu Chaffotte
 */
public abstract class SignalEventTriggerDefinitionImpl implements SignalEventTriggerDefinition {

    private static final long serialVersionUID = 7986619065007971291L;

    private final String signalName;

    public SignalEventTriggerDefinitionImpl(final String name) {
        this.signalName = name;
    }

    @Override
    public String getSignalName() {
        return signalName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((signalName == null) ? 0 : signalName.hashCode());
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
        final SignalEventTriggerDefinitionImpl other = (SignalEventTriggerDefinitionImpl) obj;
        if (signalName == null) {
            if (other.signalName != null) {
                return false;
            }
        } else if (!signalName.equals(other.signalName)) {
            return false;
        }
        return true;
    }

}
