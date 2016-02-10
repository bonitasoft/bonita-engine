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
package org.bonitasoft.engine.exception;

/**
 * Happens when a process instance can't be there is a parent process instance that is still active
 * Delete this parent process first.
 * 
 * @author Baptiste Mesta
 */
public class ProcessInstanceHierarchicalDeletionException extends DeletionException {

    private static final long serialVersionUID = -5157179430526887606L;

    private final long processInstanceId;

    public ProcessInstanceHierarchicalDeletionException(final String message, final long processInstanceId) {
        super(message);
        this.processInstanceId = processInstanceId;
    }

    /**
     * @return the processInstanceId that is the parent (root) of the process we try to delete
     */
    public long getProcessInstanceId() {
        return processInstanceId;
    }

}
