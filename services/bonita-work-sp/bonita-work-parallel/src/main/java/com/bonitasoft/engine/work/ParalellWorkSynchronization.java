package com.bonitasoft.engine.work;

import java.util.Collection;
import java.util.concurrent.ThreadPoolExecutor;

import org.bonitasoft.engine.work.AbstractWorkSynchronization;

/**
 * @author Charles Souillard
 * @author Baptiste Mesta
 */
public class ParalellWorkSynchronization extends AbstractWorkSynchronization {

    public ParalellWorkSynchronization(final ThreadPoolExecutor threadPoolExecutor) {
        super(threadPoolExecutor);
    }

    @Override
    protected void executeRunnables(final Collection<Runnable> runnables) {
        for (final Runnable runnable : runnables) {
            threadPoolExecutor.submit(runnable);
        }
    }
}
