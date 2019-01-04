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


    public <T> Future<T> execute(Callable<T> callable) {
        if (bonitaTaskExecutor == null) {
            throw new IllegalStateException(this.getClass().getName() + " is not running");
        }
        return bonitaTaskExecutor.submit(callable);
    }

    @Override
    public void start() {
        bonitaTaskExecutor = Executors.newCachedThreadPool(r -> new Thread(r, "BonitaTaskExecutor"));
    }

    @Override
    public void stop() {
        bonitaTaskExecutor.shutdown();
        bonitaTaskExecutor = null;
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }
}
