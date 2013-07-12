/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.work;

import java.util.Collection;
import java.util.concurrent.ExecutorService;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.TransactionService;

/**
 * @author Charles Souillard
 * @author Baptiste Mesta
 */
public class SequenceWorkSynchronization extends AbstractWorkSynchronization {

    private final RunnableListener runnableListener;

    public SequenceWorkSynchronization(final ExecutorWorkService runnableListener, final ExecutorService executorService,
            final TechnicalLoggerService loggerService, final SessionAccessor sessionAccessor, final SessionService sessionService,
            final TransactionService transactionService) {
        super(runnableListener, executorService, loggerService, sessionAccessor, sessionService, transactionService);
        this.runnableListener = runnableListener;
    }

    @Override
    protected void executeRunnables(final Collection<AbstractBonitaWork> works) {
        executorService.submit(new SequenceRunnableExecutor(works, runnableListener, getTenantId()));
    }

}
