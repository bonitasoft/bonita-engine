/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.service;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.bonitasoft.engine.commons.PlatformLifecycleService;

/**
 * simply hold a cached thread pool executor to handle common asynchronous tasks
 */
public class BonitaTaskExecutor implements PlatformLifecycleService {

    private ExecutorService bonitaTaskExecutor;

    private void checkStarted() {
        if (bonitaTaskExecutor == null) {
            throw new IllegalStateException(this.getClass().getName() + " is not running");
        }
    }

    public Future<?> execute(RunnableWithException runnable) {
        return execute(() -> {
            runnable.run();
            return null;
        });
    }

    public <T> Future<T> execute(Callable<T> callable) {
        checkStarted();
        return bonitaTaskExecutor.submit(callable);
    }

    @Override
    public void start() {
        if (bonitaTaskExecutor == null) {
            bonitaTaskExecutor = Executors.newCachedThreadPool(r -> new Thread(r, "BonitaTaskExecutor"));
        }
    }

    @Override
    public void stop() {
        if (bonitaTaskExecutor != null) {
            bonitaTaskExecutor.shutdown();
            bonitaTaskExecutor = null;
        }
    }

}
