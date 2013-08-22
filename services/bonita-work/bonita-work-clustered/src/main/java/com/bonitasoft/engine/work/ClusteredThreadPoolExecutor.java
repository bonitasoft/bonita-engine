/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.work;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.bonitasoft.engine.work.BonitaRunnable;

import com.bonitasoft.manager.Features;
import com.bonitasoft.manager.Manager;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;
import com.hazelcast.core.MultiMap;

/**
 * 
 * @author Baptiste Mesta
 * 
 */
public class ClusteredThreadPoolExecutor extends ThreadPoolExecutor implements MembershipListener {

    private static BlockingQueue<Runnable> workQueue;

    private final MultiMap<String, BonitaRunnable> executingWorks;

    private final String localMemberUUID;

    public ClusteredThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
            ThreadFactory threadFactory, RejectedExecutionHandler handler, HazelcastInstance hazelcastInstance) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, createWorkQueue(hazelcastInstance), threadFactory, handler);
        if (!Manager.getInstance().isFeatureActive(Features.ENGINE_CLUSTERING)) {
            throw new IllegalStateException("The clustering is not an active feature.");
        }
        executingWorks = hazelcastInstance.getMultiMap("WORK_EXECUTING");
        hazelcastInstance.getCluster().addMembershipListener(this);
        localMemberUUID = hazelcastInstance.getCluster().getLocalMember().getUuid();
    }

    private static BlockingQueue<Runnable> createWorkQueue(HazelcastInstance hazelcastInstance) {
        workQueue = hazelcastInstance.getQueue("WorkQueue");
        return workQueue;
    }

    @Override
    public Future<?> submit(Runnable task) {
        execute(task);
        return null;
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        BonitaRunnable runnable = (BonitaRunnable) r;
        executingWorks.put(localMemberUUID, runnable);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        executingWorks.remove(r);
    }

    @Override
    public void memberAdded(MembershipEvent membershipEvent) {

    }

    @Override
    public void memberRemoved(MembershipEvent membershipEvent) {
        // reschedule executing work that are on the node that is gone
        // will be done on all node but it's ok because we lock the map on this key
        String memberUUID = membershipEvent.getMember().getUuid();
        executingWorks.lock(memberUUID);
        Collection<BonitaRunnable> collection = executingWorks.get(memberUUID);
        for (BonitaRunnable bonitaRunnable : collection) {
            workQueue.add(bonitaRunnable);
        }
        executingWorks.unlock(memberUUID);
    }
}
