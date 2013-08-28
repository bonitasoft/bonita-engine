/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
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
import org.bonitasoft.engine.execution.ContainerRegistry;

/**
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class NotifyChildFinishedWork extends TxLockProcessInstanceWork {

    private static final long serialVersionUID = -8987586943379865375L;

    private final long processDefinitionId;

    private final long flowNodeInstanceId;

    private final String parentType;

    private final int stateId;

    private final long parentId;

    public NotifyChildFinishedWork(final long processDefinitionId, final long processInstanceId, final long flowNodeInstanceId, final long parentId, final String parentType,
            final int stateId) {
        super(processInstanceId);
        this.processDefinitionId = processDefinitionId;
        this.flowNodeInstanceId = flowNodeInstanceId;
        this.parentId = parentId;
        this.parentType = parentType;
        this.stateId = stateId;
    }

    protected ClassLoader getClassLoader() throws SBonitaException {
        return getTenantAccessor().getClassLoaderService().getLocalClassLoader("process", processDefinitionId);
    }

    @Override
    protected void work() throws Exception {
        final ClassLoader processClassloader = getClassLoader();
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(processClassloader);
            final ContainerRegistry containerRegistry = getTenantAccessor().getContainerRegistry();
            containerRegistry.nodeReachedState(processDefinitionId, flowNodeInstanceId, stateId, parentId, parentType);
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    @Override
    public String getDescription() {
        return getClass().getSimpleName() + ": processInstanceId:" + parentId + ", flowNodeInstanceId: " + flowNodeInstanceId;
    }

}
