/**
 * Copyright (C) 2024 Bonitasoft S.A.
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
package org.bonitasoft.engine.execution;

import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceCreationException;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;

/**
 * Define rules to be executed before the start of a process, right after its creation.
 */
public interface ProcessStarterVerifier {

    /**
     * Verify that a process is ready to be started right after its creation.
     *
     * @param processInstance the process instance that is going to be started
     * @throws SProcessInstanceCreationException if the process is not in a valid state to start
     */
    void verify(SProcessInstance processInstance) throws SProcessInstanceCreationException;

    /**
     * Get the current number of started process instances.
     *
     * @return -1 if non relevant in this context
     */
    default long getCurrentNumberOfStartedProcessInstances() {
        return -1;
    }
}
