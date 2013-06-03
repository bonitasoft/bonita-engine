/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.execution.work;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.model.STransitionInstance;
import org.bonitasoft.engine.execution.ContainerExecutor;
import org.bonitasoft.engine.work.BonitaWork;

/**
 * @author Baptiste Mesta
 */
public class ExecuteTransitionWork extends BonitaWork {

    private final SProcessDefinition sDefinition;

    private final STransitionInstance sTransitionInstance;

    private final ContainerExecutor containerExecutor;

    /**
     * @param sDefinition
     * @param sTransitionInstance
     */
    public ExecuteTransitionWork(final ContainerExecutor containerExecutor, final SProcessDefinition sDefinition, final STransitionInstance sTransitionInstance) {
        this.containerExecutor = containerExecutor;
        this.sDefinition = sDefinition;
        this.sTransitionInstance = sTransitionInstance;
    }

    @Override
    protected void work() throws SBonitaException {
        containerExecutor.executeTransition(sDefinition, sTransitionInstance);
    }
}
