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
 * @author Baptiste Mesta
 * @author Laurent Vaills
 */
public class ClusteredThreadPoolExecutorLocalQueue extends ThreadPoolExecutor implements MembershipListener {

    private final BlockingQueue<Runnable> workQueue;

    private final IQueue<Runnable> executingRunnable;

    private final HazelcastInstance hazelcastInstance;

    public ClusteredThreadPoolExecutorLocalQueue(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit,
            final ThreadFactory threadFactory, final RejectedExecutionHandler handler, final HazelcastInstance hazelcastInstance) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, createWorkQueue(hazelcastInstance), threadFactory, handler);
        if (!Manager.getInstance().isFeatureActive(Features.ENGINE_CLUSTERING)) {
            throw new IllegalStateException("The clustering is not an active feature.");
        }
        this.hazelcastInstance = hazelcastInstance;
        Cluster cluster = hazelcastInstance.getCluster();
        cluster.addMembershipListener(this);
        this.workQueue = hazelcastInstance.getQueue(memberWorkQueueName(cluster.getLocalMember()));
        this.executingRunnable = hazelcastInstance.getQueue(memberExecutingWorkQueueName(cluster.getLocalMember()));
        // Do we have to check is the queue is empty or not ? If not, what to do ?
    }

    /**
     * @param localMember
     * @return
     */
    private static String memberExecutingWorkQueueName(final Member localMember) {
        return "ExecutingWorkQueue@" + localMember.getUuid();
    }

    private static BlockingQueue<Runnable> createWorkQueue(final HazelcastInstance hazelcastInstance) {
        workQueue = hazelcastInstance.getQueue("WorkQueue");
        return workQueue;
    }

    @Override
    public Future<?> submit(final Runnable task) {
        execute(task);
        return null;
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
        Member member = membershipEvent.getMember();
        ILock lock = hazelcastInstance.getLock("WorkLock@"+member.getUuid());
        lock.lock();
        try {
            // Transfer the member's queue into my own queue
            IQueue<Runnable> memberExecutingRunnable = hazelcastInstance.getQueue(memberExecutingWorkQueueName(member));
            executingRunnable.addAll(memberExecutingRunnable);
            memberExecutingRunnable.clear(); // No way to drop completely the queue ?
        } finally {
            lock.unlock();
        }
    }
}
