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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

/**
 * Service allowing to broadcast a call made on services to the other nodes of a Cluster
 * 
 * @author Baptiste Mesta
 */
public interface BroadcastService {

    /**
     * Broadcast the execution of a callable on other nodes and returns immediately
     * a future holding the execution result on each node (once available).
     * <br/>
     * The callable will be executed using a platform level session on other nodes.
     *
     * @param callable
     *        callable that will be executed on all nodes except the current one
     * @param <T>
     *        type of the returned value
     * @return
     *         a future of a map containing the name of the node and the result of the callable
     */
    <T> Future<Map<String, TaskResult<T>>> executeOnOthers(Callable<T> callable);

    /**
     * Broadcast the execution of a callable on other nodes and returns immediately
     * a future holding the execution result on each node (once available).
     * <br/>
     * The callable will be executed using a tenant level session on other nodes.
     *
     * @param callable
     *        callable that will be executed on all nodes except the current one
     * @param <T>
     *        type of the returned value
     * @param tenantId
     *        the if of the tenant
     * @return
     *         a future of a map containing the name of the node and the result of the callable
     */
    <T> Future<Map<String, TaskResult<T>>> executeOnOthers(Callable<T> callable, Long tenantId);

    <T> Map<String, TaskResult<T>> executeOnOthersAndWait(Callable<T> callable, Long tenantId) throws TimeoutException, InterruptedException, ExecutionException;

}
