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
package org.bonitasoft.engine.work;

import java.util.Map;

/**
 * Callbacks the work executor makes around a work's execution: once when it starts, once when it finishes.
 *
 * @author Baptiste Mesta.
 */
public interface WorkExecutionCallback {

    /**
     * The work is about to run: the pool task has taken it and calls {@link BonitaWork#work} next. Once per execution,
     * so once more for each retry. A no-op by default: only an executor that has to tell a work that ran from one that
     * was merely submitted needs it.
     */
    default void onStart(WorkDescriptor work) {
    }

    void onSuccess(WorkDescriptor workDescriptor);

    void onFailure(WorkDescriptor work, BonitaWork bonitaWork, Map<String, Object> context,
            Throwable thrown);

}
