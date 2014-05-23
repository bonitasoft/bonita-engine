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

import org.bonitasoft.engine.work.BonitaExecutorService;

import com.bonitasoft.manager.Features;
import com.bonitasoft.manager.Manager;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;
import com.hazelcast.core.MultiMap;

/**
 * A work factory that use a clustered Queue and a clustered executing work map
 * <p>
 * the queue of works is shared between all the nodes.<br/>
 * If a node crash during the execution of a work, other members are notified and the first to be notified get all executing works of the failing node and
 * requeue them
 * 
 * @author Baptiste Mesta
 */
public class ClusteredThreadPoolExecutor extends ThreadPoolExecutor implements MembershipListener, BonitaExecutorService {

    private final BlockingQueue<Runnable> workQueue;

    private final MultiMap<String, Runnable> executingWorks;

    private final String localMemberUUID;

    public ClusteredThreadPoolExecutor(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit,
            final ThreadFactory threadFactory, final RejectedExecutionHandler handler, final HazelcastInstance hazelcastInstance,
            final BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        this.workQueue = workQueue;
        if (!Manager.getInstance().isFeatureActive(Features.ENGINE_CLUSTERING)) {
            throw new IllegalStateException("The clustering is not an active feature.");
        }
        executingWorks = hazelcastInstance.getMultiMap("WORK_EXECUTING");
        hazelcastInstance.getCluster().addMembershipListener(this);
        localMemberUUID = hazelcastInstance.getCluster().getLocalMember().getUuid();
    }

    @Override
    public Future<?> submit(final Runnable task) {
        execute(task);
        return null;
    }

    @Override
    protected void beforeExecute(final Thread t, final Runnable r) {
        executingWorks.put(localMemberUUID, r);
    }

    @Override
    protected void afterExecute(final Runnable r, final Throwable t) {
        executingWorks.remove(localMemberUUID, r);
    }

    @Override
    public void memberAdded(final MembershipEvent membershipEvent) {
    }

    @Override
    public void memberRemoved(final MembershipEvent membershipEvent) {
        // reschedule executing work that are on the node that is gone
        // will be done on all node but it's ok because we lock the map on this key
        final String memberUUID = membershipEvent.getMember().getUuid();
        executingWorks.lock(memberUUID);
        final Collection<Runnable> collection = executingWorks.get(memberUUID);
        for (final Runnable runnable : collection) {
            workQueue.add(runnable);
        }
        executingWorks.unlock(memberUUID);
    }

    @Override
    public void clearQueue() {
        workQueue.clear();
        executingWorks.clear();
    }
}
