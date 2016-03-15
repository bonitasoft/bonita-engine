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
package org.bonitasoft.engine.service.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.service.BroadcastService;
import org.bonitasoft.engine.service.ServicesResolver;
import org.bonitasoft.engine.service.TaskResult;

/**
 * @author Baptiste Mesta
 */
public class BroadcastServiceLocal implements BroadcastService {

    private ServicesResolver servicesResolver;

    public BroadcastServiceLocal(ServicesResolver servicesResolver) {
        this.servicesResolver = servicesResolver;
    }

    @Override
    public <T> Map<String, TaskResult<T>> executeOnAllNodes(final Callable<T> callable) {
        try {
            servicesResolver.injectServices(null, callable);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new IllegalStateException("Unable to inject service on broadcast task", e);
        }
        try {
            T call = callable.call();
            return Collections.singletonMap("local", TaskResult.ok(call));
        } catch (Exception e) {
            return Collections.singletonMap("local", TaskResult.<T> error(e));
        }

    }

    @Override
    public <T> Map<String, TaskResult<T>> executeOnOthers(Callable<T> callable) {
        return Collections.emptyMap();
    }

    @Override
    public <T> Map<String, TaskResult<T>> executeOnAllNodes(final Callable<T> callable, final Long tenantId) {
        try {
            servicesResolver.injectServices(tenantId, callable);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new IllegalStateException("Unable to inject service on broadcast task", e);
        }
        try {
            T call = callable.call();
            return Collections.singletonMap("local", TaskResult.ok(call));
        } catch (Exception e) {
            return Collections.singletonMap("local", TaskResult.<T> error(e));
        }

    }

    @Override
    public <T> Map<String, TaskResult<T>> executeOnOthers(Callable<T> callable, Long tenantId) {
        return Collections.emptyMap();
    }

    @Override
    public void submit(final Callable<?> callable) {
        //In local it is the same as execute
        executeOnAllNodes(callable);
    }

}
