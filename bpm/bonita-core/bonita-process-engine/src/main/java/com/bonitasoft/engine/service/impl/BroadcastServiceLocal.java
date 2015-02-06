/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.service.impl;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;

import com.bonitasoft.engine.service.BroadcastService;
import com.bonitasoft.engine.service.TaskResult;

/**
 * @author Baptiste Mesta
 * 
 */
public class BroadcastServiceLocal implements BroadcastService {

    @Override
    public <T> Map<String, TaskResult<T>> execute(final Callable<T> callable) {
        try {
            T call = callable.call();
            return Collections.singletonMap("local", TaskResult.ok(call));
        } catch (Exception e) {
            return Collections.singletonMap("local", TaskResult.<T> error(e));
        }

    }

    @Override
    public <T> Map<String, TaskResult<T>> execute(final Callable<T> callable, final Long tenantId) {
        return execute(callable);
    }

    @Override
    public void submit(final Callable<?> callable) {
        try {
            callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
