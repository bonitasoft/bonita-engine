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
package org.bonitasoft.engine.bpm.flownode.impl.internal;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.bonitasoft.engine.bpm.flownode.TerminateEventTriggerDefinition;
import org.bonitasoft.engine.bpm.process.ModelFinderVisitor;

/**
 * @author Matthieu Chaffotte
 */
public class TerminateEventTriggerDefinitionImpl implements TerminateEventTriggerDefinition {

    private static final long serialVersionUID = -1494352183532063268L;

    public TerminateEventTriggerDefinitionImpl() {
    }

    @Override
    public void accept(ModelFinderVisitor visitor, long modelId) {
        // nothing to do here:
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .toString();
    }
}
