/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.work;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.work.AbstractWorkSynchronization;
import org.bonitasoft.engine.work.BonitaExecutorService;
import org.bonitasoft.engine.work.WorkService;
import org.bonitasoft.engine.work.WorkSynchronizationFactory;

/**
 * @author Charles Souillard
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class ParallelWorkSynchronizationFactory implements WorkSynchronizationFactory {

    @Override
    public AbstractWorkSynchronization getWorkSynchronization(final BonitaExecutorService executorService, final TechnicalLoggerService loggerService,
            final SessionAccessor sessionAccessor, final WorkService workService) {
        return new ParallelWorkSynchronization(executorService, sessionAccessor, workService);
    }

}
