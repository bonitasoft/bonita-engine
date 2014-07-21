/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.service;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * 
 * Small service that broadcast a call made on services to all nodes or only in local
 * 
 * @author Baptiste Mesta
 * 
 */
public interface BroadcastService {

    /**
     * 
     * @param a
     *            callable that will be executed on all nodes
     * @return
     *         a map containing the name of the node and the result of the callable
     * @throws Exception
     */
    <T> Map<String, TaskResult<T>> execute(Callable<T> callable);

    /**
     * @param a
     *            callable that will be executed on all nodes
     * @return
     *         a map containing the name of the node and the result of the callable
     * @throws Exception
     */
    <T> Map<String, TaskResult<T>> execute(Callable<T> callable, Long tenantId);

    void submit(Callable<?> callable);

}
