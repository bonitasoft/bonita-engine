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
 * Small service that broadcast a call made on services to all nodes or only in local
 * 
 * @author Baptiste Mesta
 */
public interface BroadcastService {

    /**
     * Broadcast the execution of a callable on all nodes and wait for the result.
     * The callable will be executed on the platform level and broadcasted on all nodes including the calling node.
     * The method will return only when all nodes finished executing the callable.
     * 
     * @param callable
     *        callable that will be executed on all nodes
     * @return
     *         a map containing the name of the node and the result of the callable
     */
    <T> Map<String, TaskResult<T>> executeOnAllNodes(Callable<T> callable);

    /**
     * Broadcast the execution of a callable on other nodes and wait for the result.
     * The callable will be executed on the platform level and broadcasted on other nodes, it is not executed on the calling node.
     * The method will return only when all nodes finished executing the callable.
     *
     * @param callable
     *        callable that will be executed on all nodes except the current one
     * @param <T>
     *        type of the returned value
     * @return
     *         a map containing the name of the node and the result of the callable
     */
    <T> Map<String, TaskResult<T>> executeOnOthers(Callable<T> callable);

    /**
     * Broadcast the execution of a callable on all nodes and wait for the result.
     * The callable will be executed on the tenant level and broadcasted on all nodes including the calling node.
     * The method will return only when all nodes finished executing the callable.
     *
     * @param callable
     *        callable that will be executed on all nodes
     * @param tenantId
     *        the if of the tenant
     * @return
     *         a map containing the name of the node and the result of the callable
     */
    <T> Map<String, TaskResult<T>> executeOnAllNodes(Callable<T> callable, Long tenantId);

    /**
     * Broadcast the execution of a callable on other nodes and wait for the result.
     * The callable will be executed on the tenant level and broadcasted on other nodes, it is not executed on the calling node.
     * The method will return only when all nodes finished executing the callable.
     * 
     * @param callable
     *        callable that will be executed on all nodes except the current one
     * @param <T>
     *        type of the returned value
     * @param tenantId
     *        the if of the tenant
     * @return
     *         a map containing the name of the node and the result of the callable
     */
    <T> Map<String, TaskResult<T>> executeOnOthers(Callable<T> callable, Long tenantId);

    /**
     * Broadcast the execution of a callable on all nodes but do not wait for the result.
     * The callable will be executed on the platform level and broadcasted on all nodes including the calling node.
     * The method will return after submitting the callable, it will be executed asynchronously ion each node.
     *
     * @param callable
     *        the callable that will be executed on all nodes
     */
    void submit(Callable<?> callable);

}
