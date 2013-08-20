/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.work;

import org.bonitasoft.engine.work.BonitaExecutorServiceFactory;

import com.bonitasoft.manager.Features;
import com.bonitasoft.manager.Manager;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;

/**
 * 
 * Factory that use a hazelcast executor
 * 
 * @author Baptiste Mesta
 */
public class ClusteredBonitaExecutorServiceFactory implements BonitaExecutorServiceFactory {

    private static final String EXECUTOR_NAME = "Bonita-Worker";

    private final HazelcastInstance hazelcastInstance;

    public ClusteredBonitaExecutorServiceFactory(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
        if (!Manager.getInstance().isFeatureActive(Features.ENGINE_CLUSTERING)) {
            throw new IllegalStateException("The clustering is not an active feature.");
        }
    }

    @Override
    public IExecutorService createExecutorService() {
        IExecutorService executorService = hazelcastInstance.getExecutorService(EXECUTOR_NAME);
        return executorService;
        // final BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(queueCapacity);
        // final RejectedExecutionHandler handler = new QueueRejectedExecutionHandler();
        // final WorkerThreadFactory threadFactory = new WorkerThreadFactory(EXECUTOR_NAME);
        // return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTimeSeconds, TimeUnit.SECONDS, workQueue, threadFactory, handler);
    }

}
