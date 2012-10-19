package com.bonitasoft.engine.work;

import java.util.concurrent.ThreadPoolExecutor;

import org.bonitasoft.engine.work.AbstractWorkSynchronization;
import org.bonitasoft.engine.work.WorkSynchronizationFactory;

/**
 * @author Charles Souillard
 * @author Baptiste Mesta
 */
public class ParalellWorkSynchronizationFactory implements WorkSynchronizationFactory {

    public AbstractWorkSynchronization getWorkSynchronization(final ThreadPoolExecutor threadPoolExecutor) {
        return new ParalellWorkSynchronization(threadPoolExecutor);
    }

}
