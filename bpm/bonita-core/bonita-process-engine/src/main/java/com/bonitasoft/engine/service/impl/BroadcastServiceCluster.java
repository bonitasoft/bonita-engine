/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
 ** 
 * @since 6.2
 */
package com.bonitasoft.engine.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.bonitasoft.engine.service.BroadCastedTask;
import com.bonitasoft.engine.service.BroadcastService;
import com.bonitasoft.engine.service.TaskResult;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;

/**
 * @author Baptiste Mesta
 * 
 */
public class BroadcastServiceCluster implements BroadcastService {

    private static final String EXECUTOR_NAME = "default";

    private final HazelcastInstance hazelcastInstance;

    private final long timeout;

    private final TimeUnit timeUnit;

    /**
     * 
     */
    public BroadcastServiceCluster(final HazelcastInstance hazelcastInstance, final long timeout, final TimeUnit timeUnit) {
        this.hazelcastInstance = hazelcastInstance;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
    }

    @Override
    public <T> Map<String, TaskResult<T>> execute(final Callable<T> callable) {
        setNameOfCallingNode(callable);
        return execute(callable, null);
    }

    @Override
    public <T> Map<String, TaskResult<T>> execute(final Callable<T> callable, final Long tenantId) {
        setNameOfCallingNode(callable);
        Callable<T> wrapped = new InTransactionCallable<T>(callable, tenantId);
        Map<Member, Future<T>> submitToAllMembers = hazelcastInstance.getExecutorService(EXECUTOR_NAME).submitToAllMembers(wrapped);
        HashMap<String, TaskResult<T>> resultMap = new HashMap<String, TaskResult<T>>();
        Set<Entry<Member, Future<T>>> entrySet = submitToAllMembers.entrySet();
        for (Entry<Member, Future<T>> entry : entrySet) {
            String uuid = entry.getKey().getUuid();
            try {
                resultMap.put(uuid, TaskResult.ok(entry.getValue().get(timeout, timeUnit)));
            } catch (InterruptedException e) {
                resultMap.put(uuid, TaskResult.<T> error(e));
            } catch (ExecutionException e) {
                resultMap.put(uuid, TaskResult.<T> error(e.getCause()));
            } catch (TimeoutException e) {
                resultMap.put(uuid, TaskResult.<T> timeout(timeout, timeUnit));
            }
        }
        return resultMap;
    }

    @Override
    public void submit(final Callable<?> callable) {
        setNameOfCallingNode(callable);
        hazelcastInstance.getExecutorService(EXECUTOR_NAME).submitToAllMembers(callable);
    }

    private void setNameOfCallingNode(final Callable<?> callable) {
        if (callable instanceof BroadCastedTask) {
            BroadCastedTask<?> task = (BroadCastedTask<?>) callable;
            task.setName(hazelcastInstance.getCluster().getLocalMember().getUuid());
        }
    }

}
