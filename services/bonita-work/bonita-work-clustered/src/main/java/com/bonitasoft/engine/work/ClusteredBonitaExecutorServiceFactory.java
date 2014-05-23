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
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.bonitasoft.engine.work.BonitaExecutorService;
import org.bonitasoft.engine.work.BonitaExecutorServiceFactory;
import org.bonitasoft.engine.work.WorkerThreadFactory;

import com.bonitasoft.manager.Features;
import com.bonitasoft.manager.Manager;
import com.hazelcast.core.HazelcastInstance;

/**
 * Factory that use a hazelcast executor
 * 
 * @author Baptiste Mesta
 */
public class ClusteredBonitaExecutorServiceFactory implements BonitaExecutorServiceFactory {

    private final HazelcastInstance hazelcastInstance;

    private final int corePoolSize;

    private final int maximumPoolSize;

    private final long keepAliveTimeSeconds;

    private final long tenantId;

    public ClusteredBonitaExecutorServiceFactory(final long tenantId, final int corePoolSize, final int maximumPoolSize, final long keepAliveTimeSeconds,
            final HazelcastInstance hazelcastInstance) {
        this.tenantId = tenantId;
        this.hazelcastInstance = hazelcastInstance;
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.keepAliveTimeSeconds = keepAliveTimeSeconds;
        if (!Manager.getInstance().isFeatureActive(Features.ENGINE_CLUSTERING)) {
            throw new IllegalStateException("The clustering is not an active feature.");
        }
    }

    @Override
    public BonitaExecutorService createExecutorService() {
        final RejectedExecutionHandler handler = new QueueRejectedExecutionHandler();
        final WorkerThreadFactory threadFactory = new WorkerThreadFactory("Bonita-Worker", tenantId, maximumPoolSize);
        final BlockingQueue<Runnable> queue = createWorkQueue(hazelcastInstance);
        return new ClusteredThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTimeSeconds,
                TimeUnit.SECONDS, threadFactory, handler, hazelcastInstance, queue);
    }

    private static BlockingQueue<Runnable> createWorkQueue(final HazelcastInstance hazelcastInstance) {
        final BlockingQueue<Runnable> workQueue = hazelcastInstance.getQueue("WorkQueue");
        return workQueue;
    }

    private final class QueueRejectedExecutionHandler implements RejectedExecutionHandler {

        public QueueRejectedExecutionHandler() {
        }

        @Override
        public void rejectedExecution(final Runnable task, final ThreadPoolExecutor executor) {
            throw new RejectedExecutionException(
                    "Unable to run the task "
                            + task
                            + "\n your work queue is full you might consider changing your configuration to scale more. See parameter 'queueCapacity' in bonita.home configuration files.");
        }

    }

}
