/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.classloader;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;

/**
 * 
 * 
 * Refresh the classloader of the current node
 * 
 * @author Baptiste Mesta
 * 
 */
final class RefreshClassLoaderTask implements Callable<TaskStatus>,
        Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private final Map<String, byte[]> resources;

    private final String origin;

    private String type;

    private long id;

    private final boolean isLocal;

    public RefreshClassLoaderTask(Map<String, byte[]> resources) {
        this.origin = ClusteredClassLoaderService.hazelcastInstance.getCluster().getLocalMember().getUuid();
        this.resources = resources;
        isLocal = false;
    }

    public RefreshClassLoaderTask(String type, long id,
            Map<String, byte[]> resources) {
        this.origin = ClusteredClassLoaderService.hazelcastInstance.getCluster().getLocalMember().getUuid();
        isLocal = true;
        this.type = type;
        this.id = id;
        this.resources = resources;

    }

    @Override
    public TaskStatus call() {
        String localUuid = ClusteredClassLoaderService.hazelcastInstance
                .getCluster().getLocalMember().getUuid();
        try {
            long before = System.currentTimeMillis();
            if (isLocal) {
                ClusteredClassLoaderService.classLoaderService.refreshLocalClassLoader(type, id, resources);
            } else {
                ClusteredClassLoaderService.classLoaderService.refreshGlobalClassLoader(resources);
            }
            long after = System.currentTimeMillis();
            TaskStatus taskStatus = new TaskStatus(origin, localUuid, null, false, after - before, isLocal, type, id);
            ClusteredClassLoaderService.loggerService.log(this.getClass(), TechnicalLogSeverity.INFO, "executed refresh triggered by "
                    + origin + " result: " + taskStatus.toString());
            return taskStatus;
        } catch (Throwable t) {
            ClusteredClassLoaderService.loggerService.log(this.getClass(), TechnicalLogSeverity.ERROR,
                    "error refreshing classloader: " + t.getMessage());
            ClusteredClassLoaderService.loggerService.log(this.getClass(), TechnicalLogSeverity.DEBUG, t);
            return new TaskStatus(origin, localUuid, t, true, 0, isLocal, type, id);
        }

    }
}
