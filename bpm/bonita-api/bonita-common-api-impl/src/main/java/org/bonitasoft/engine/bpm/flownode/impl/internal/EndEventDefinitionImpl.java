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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.bpm.flownode.EndEventDefinition;
import org.bonitasoft.engine.bpm.flownode.TerminateEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.ThrowErrorEventTriggerDefinition;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class EndEventDefinitionImpl extends ThrowEventDefinitionImpl implements EndEventDefinition {

    private static final long serialVersionUID = -6726824751097930154L;

    private final List<ThrowErrorEventTriggerDefinition> errorEventTriggerDefinitions;

    private TerminateEventTriggerDefinition terminateEventTriggerDefinition;

    public EndEventDefinitionImpl(final String name) {
        super(name);
        errorEventTriggerDefinitions = new ArrayList<ThrowErrorEventTriggerDefinition>(1);
    }

    public EndEventDefinitionImpl(final long id, final String name) {
        super(id, name);
        errorEventTriggerDefinitions = new ArrayList<ThrowErrorEventTriggerDefinition>(1);
    }

    @Override
    public TerminateEventTriggerDefinition getTerminateEventTriggerDefinition() {
        return terminateEventTriggerDefinition;
    }

    public void setTerminateEventTriggerDefinition(final TerminateEventTriggerDefinition terminateEventTriggerDefinition) {
        this.terminateEventTriggerDefinition = terminateEventTriggerDefinition;
    }

    @Override
    public List<ThrowErrorEventTriggerDefinition> getErrorEventTriggerDefinitions() {
        return Collections.unmodifiableList(errorEventTriggerDefinitions);
    }

    public void addErrorEventTriggerDefinition(final ThrowErrorEventTriggerDefinition errorEventTrigger) {
        errorEventTriggerDefinitions.add(errorEventTrigger);
        addEventTrigger(errorEventTrigger);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (errorEventTriggerDefinitions == null ? 0 : errorEventTriggerDefinitions.hashCode());
        result = prime * result + (terminateEventTriggerDefinition == null ? 0 : terminateEventTriggerDefinition.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final EndEventDefinitionImpl other = (EndEventDefinitionImpl) obj;
        if (errorEventTriggerDefinitions == null) {
            if (other.errorEventTriggerDefinitions != null) {
                return false;
            }
        } else if (!errorEventTriggerDefinitions.equals(other.errorEventTriggerDefinitions)) {
            return false;
        }
        if (terminateEventTriggerDefinition == null) {
            if (other.terminateEventTriggerDefinition != null) {
                return false;
            }
        } else if (!terminateEventTriggerDefinition.equals(other.terminateEventTriggerDefinition)) {
            return false;
        }
        return true;
    }

}
