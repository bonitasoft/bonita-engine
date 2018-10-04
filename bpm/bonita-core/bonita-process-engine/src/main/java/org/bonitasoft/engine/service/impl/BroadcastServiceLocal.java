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

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import org.bonitasoft.engine.service.BroadcastService;
import org.bonitasoft.engine.service.TaskResult;

/**
 * This implementation does nothing, to be used in a single node environment.
 *
 * When cluster feature is enabled, an other implementation of the BroadcastService dispatch calls to other nodes.
 *
 * @author Baptiste Mesta
 */
public class BroadcastServiceLocal implements BroadcastService {


    @Override
    public <T> Future<Map<String, TaskResult<T>>> executeOnOthers(Callable<T> callable) {
        return CompletableFuture.completedFuture(Collections.emptyMap());
    }

    @Override
    public <T> Future<Map<String, TaskResult<T>>> executeOnOthers(Callable<T> callable, Long tenantId) {
        return CompletableFuture.completedFuture(Collections.emptyMap());
    }

    @Override
    public <T> Map<String, TaskResult<T>> executeOnOthersAndWait(Callable<T> callable, Long tenantId) {
        return Collections.emptyMap();
    }
}
