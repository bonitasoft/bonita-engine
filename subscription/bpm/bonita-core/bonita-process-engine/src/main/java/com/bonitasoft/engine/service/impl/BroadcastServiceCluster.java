/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
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

import org.bonitasoft.engine.service.BroadcastService;
import org.bonitasoft.engine.service.TaskResult;

import com.bonitasoft.engine.service.BroadCastedTask;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;

/**
 * @author Baptiste Mesta
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
