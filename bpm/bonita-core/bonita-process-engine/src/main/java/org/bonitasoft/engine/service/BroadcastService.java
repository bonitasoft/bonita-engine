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
package org.bonitasoft.engine.service;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * 
 * Small service that broadcast a call made on services to all nodes or only in local
 * 
 * @author Baptiste Mesta
 * 
 */
public interface BroadcastService {

    /**
     * 
     * @param callable
     *            callable that will be executed on all nodes
     * @return
     *         a map containing the name of the node and the result of the callable
     * @throws Exception
     */
    <T> Map<String, TaskResult<T>> execute(Callable<T> callable);

    /**
     * @param callable
     *            callable that will be executed on all nodes
     * @return
     *         a map containing the name of the node and the result of the callable
     * @throws Exception
     */
    <T> Map<String, TaskResult<T>> execute(Callable<T> callable, Long tenantId);

    void submit(Callable<?> callable);

}
