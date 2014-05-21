/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.work;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.bonitasoft.manager.Features;
import com.bonitasoft.manager.Manager;
import com.hazelcast.core.Cluster;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.Member;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;

/**
 * A work factory that use a clustered Queue and a clustered executing work map
 * <p>
 * There is one shared queue by member.<br/>
 * If a node crash during the execution of a work, other members are notified and the first to be notified get all executing works and queued works for himself
 * There is multiple shared queue to avoid collisions that happens when there is only one
 * 
 * @author Baptiste Mesta
 * @author Laurent Vaills
 */
public class ClusteredThreadPoolExecutorLocalQueue extends ThreadPoolExecutor implements MembershipListener {

    private final BlockingQueue<Runnable> workQueue;

    private final IQueue<Runnable> executingRunnable;

    private final HazelcastInstance hazelcastInstance;

    public ClusteredThreadPoolExecutorLocalQueue(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit,
            final ThreadFactory threadFactory, final RejectedExecutionHandler handler, final HazelcastInstance hazelcastInstance,
            final BlockingQueue<Runnable> queue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, queue, threadFactory, handler);
        if (!Manager.getInstance().isFeatureActive(Features.ENGINE_CLUSTERING)) {
            throw new IllegalStateException("The clustering is not an active feature.");
        }
        this.hazelcastInstance = hazelcastInstance;
        final Cluster cluster = hazelcastInstance.getCluster();
        cluster.addMembershipListener(this);
        workQueue = queue;
        executingRunnable = hazelcastInstance.getQueue(memberExecutingWorkQueueName(cluster.getLocalMember()));
        // Do we have to check is the queue is empty or not ? If not, what to do ?
    }

    /**
     * @param localMember
     * @return
     */
    static String memberExecutingWorkQueueName(final Member localMember) {
        return "ExecutingWorkQueue@" + localMember.getUuid();
    }

    /**
     * @param localMember
     * @return
     */
    static String memberWorkQueueName(final Member localMember) {
        return "WorkQueue@" + localMember.getUuid();
    }

    @Override
    public Future<?> submit(final Runnable task) {
        execute(task);
        return null;
    }

    @Override
    public void shutdown() {
        workQueue.clear();
        super.shutdown();
        executingRunnable.clear();
    }

    @Override
    public boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException {
        return super.awaitTermination(timeout, unit);
    }

    @Override
    protected void beforeExecute(final Thread t, final Runnable r) {
        executingRunnable.offer(r);
    }

    @Override
    protected void afterExecute(final Runnable r, final Throwable t) {
        executingRunnable.remove(r);
    }

    @Override
    public void memberAdded(final MembershipEvent membershipEvent) {
    }

    @Override
    public void memberRemoved(final MembershipEvent membershipEvent) {
        final Member member = membershipEvent.getMember();
        final ILock lock = hazelcastInstance.getLock("WorkLock@" + member.getUuid());
        lock.lock();
        try {
            // Transfer the member's queues into my own queues
            final IQueue<Runnable> memberExecutingRunnable = hazelcastInstance.getQueue(memberExecutingWorkQueueName(member));
            executingRunnable.addAll(memberExecutingRunnable);
            memberExecutingRunnable.clear(); // No way to drop completely the queue ?

            final IQueue<Runnable> memberWorkQueue = hazelcastInstance.getQueue(memberWorkQueueName(member));
            workQueue.addAll(memberWorkQueue);
            memberWorkQueue.clear(); // No way to drop completely the queue ?
        } finally {
            lock.unlock();
        }
    }
}
