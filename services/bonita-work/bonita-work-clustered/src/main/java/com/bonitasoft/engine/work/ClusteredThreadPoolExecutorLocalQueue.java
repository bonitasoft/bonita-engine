/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.work;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.hazelcast.core.MemberAttributeEvent;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.work.BonitaExecutorService;

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
public class ClusteredThreadPoolExecutorLocalQueue extends ThreadPoolExecutor implements MembershipListener, BonitaExecutorService {

    private final BlockingQueue<Runnable> workQueue;

    private final BlockingQueue<Runnable> executingRunnable;

    private final HazelcastInstance hazelcastInstance;

    private final TechnicalLoggerService logger;

    private final long tenantId;

    public ClusteredThreadPoolExecutorLocalQueue(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit,
            final ThreadFactory threadFactory, final RejectedExecutionHandler handler, final HazelcastInstance hazelcastInstance,
            final BlockingQueue<Runnable> queue, final BlockingQueue<Runnable> executingRunnable, final TechnicalLoggerService logger, final long tenantId) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, queue, threadFactory, handler);
        this.executingRunnable = executingRunnable;
        this.tenantId = tenantId;

        /*
         * This is due to an hazelcast bug that don't allow to interrupt IQueue
         * if it's set to true the threadpool use poll with a timeout on the queue instead of take
         * and so we wait the keepAliveTime that the thread finished the poll
         * Need to update the Hazelcast version
         * see https://github.com/hazelcast/hazelcast/pull/2132
         */
        this.allowCoreThreadTimeOut(true);
        this.logger = logger;
        if (!Manager.getInstance().isFeatureActive(Features.ENGINE_CLUSTERING)) {
            throw new IllegalStateException("The clustering is not an active feature.");
        }
        this.hazelcastInstance = hazelcastInstance;
        final Cluster cluster = hazelcastInstance.getCluster();
        cluster.addMembershipListener(this);
        workQueue = queue;
    }

    static String memberExecutingWorkQueueName(final Member localMember, final long tenantId) {
        return memberExecutingWorkQueueName(localMember.getUuid(), tenantId);
    }

    static String memberExecutingWorkQueueName(final String uuid, final long tenantId) {
        return "ExecutingWorkQueue@" + tenantId + "@" + uuid;
    }

    static String memberWorkQueueName(final Member localMember, final long tenantId) {
        return memberWorkQueueName(localMember.getUuid(), tenantId);
    }

    static String memberWorkQueueName(final String uuid, final long tenantId) {
        return "WorkQueue@" + tenantId + "@" + uuid;
    }

    @Override
    public Future<?> submit(final Runnable task) {
        if (isShutdown()) {
            // in cluster we still add it to the queue
            // add to executing jobs queue
            logger.log(getClass(), TechnicalLogSeverity.WARNING,
                    "Work was added to the queue when it was shutdown, added it to the executing so it's get by others " + task);
            executingRunnable.add(task);
        } else {
            execute(task);
        }
        return null;
    }

    @Override
    protected void beforeExecute(final Thread t, final Runnable r) {
        executingRunnable.offer(r);
    }

    @Override
    protected void afterExecute(final Runnable r, final Throwable t) {
        super.afterExecute(r, t);
        if (t != null) {
            if (isShutdown()) {
                logger.log(ClusteredThreadPoolExecutorLocalQueue.class, TechnicalLogSeverity.WARNING, "Work finished with exception because of shutdown", t);
                // do not remove from queue in case of exception because shutdown
                return;
            }
        }
        executingRunnable.remove(r);
    }

    @Override
    public void memberAdded(final MembershipEvent membershipEvent) {
    }

    @Override
    public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {

    }

    @Override
    public void memberRemoved(final MembershipEvent membershipEvent) {
        final Member member = membershipEvent.getMember();
        String uuid = member.getUuid();
        restartWorkFromNode(uuid);
    }

    private void restartWorkFromNode(final String uuid) {
        if (isShutdown()) {
            return;
        }
        final ILock lock = hazelcastInstance.getLock("WorkLock@" + uuid);
        if (logger.isLoggable(ClusteredThreadPoolExecutorLocalQueue.class, TechnicalLogSeverity.INFO)) {
            logger.log(ClusteredThreadPoolExecutorLocalQueue.class, TechnicalLogSeverity.INFO, "[" + hazelcastInstance.getCluster().getLocalMember().getUuid()
                    + "] Detected Member " + uuid + " is shutting down.");
        }
        lock.lock();
        try {
            // Transfer the member's queues into my own queues
            final IQueue<Runnable> memberExecutingRunnable = hazelcastInstance.getQueue(memberExecutingWorkQueueName(uuid, tenantId));
            if (logger.isLoggable(ClusteredThreadPoolExecutorLocalQueue.class, TechnicalLogSeverity.INFO)) {
                logger.log(ClusteredThreadPoolExecutorLocalQueue.class, TechnicalLogSeverity.INFO, "["
                        + hazelcastInstance.getCluster().getLocalMember().getUuid() + "] adding " + memberExecutingRunnable.size() + " from executingRunnable");
            }
            for (Runnable runnable : memberExecutingRunnable) {
                submit(runnable);
            }
            memberExecutingRunnable.clear(); // No way to drop completely the queue ?

            final IQueue<Runnable> memberWorkQueue = hazelcastInstance.getQueue(memberWorkQueueName(uuid, tenantId));
            if (logger.isLoggable(ClusteredThreadPoolExecutorLocalQueue.class, TechnicalLogSeverity.INFO)) {
                logger.log(ClusteredThreadPoolExecutorLocalQueue.class, TechnicalLogSeverity.INFO, "["
                        + hazelcastInstance.getCluster().getLocalMember().getUuid() + "] adding " + memberWorkQueue.size() + " from workQueue");
            }
            for (Runnable runnable : memberWorkQueue) {
                submit(runnable);
            }
            memberWorkQueue.clear(); // No way to drop completely the queue ?
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void clearAllQueues() {
        if (logger.isLoggable(ClusteredThreadPoolExecutorLocalQueue.class, TechnicalLogSeverity.INFO)) {
            logger.log(ClusteredThreadPoolExecutorLocalQueue.class, TechnicalLogSeverity.INFO, "Clearing Queue From ClusteredThreadPoolExecutorLocalQueue "
                    + hazelcastInstance.getCluster().getLocalMember().getUuid());
        }
        workQueue.clear();
        executingRunnable.clear();
    }

    @Override
    public List<Runnable> shutdownNow() {
        throw new UnsupportedOperationException("Use stop instead");
    }

    @Override
    public void shutdown() {
        throw new UnsupportedOperationException("Use stop instead");
    }

    @Override
    public void shutdownAndEmptyQueue() {
        super.shutdown();
        int size = workQueue.size();
        // add the workQueue to executing because we don't want them to be executed now
        executingRunnable.addAll(workQueue);
        workQueue.clear();
        if (logger.isLoggable(ClusteredThreadPoolExecutorLocalQueue.class, TechnicalLogSeverity.INFO)) {
            logger.log(ClusteredThreadPoolExecutorLocalQueue.class, TechnicalLogSeverity.INFO, "Shutdown the thread pool executor service, there was still "
                    + size + " elements, queue " + hazelcastInstance.getCluster().getLocalMember().getUuid());
        }
    }

    @Override
    public void notifyNodeStopped(final String nodeName) {
        restartWorkFromNode(nodeName);
    }
}
