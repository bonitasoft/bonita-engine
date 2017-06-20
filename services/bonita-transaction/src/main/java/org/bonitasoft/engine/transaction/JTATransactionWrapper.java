/**
 * Copyright (C) 2015 BonitaSoft S.A.
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
package org.bonitasoft.engine.transaction;

import javax.transaction.Synchronization;

import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

public class JTATransactionWrapper implements Synchronization {

    private TechnicalLoggerService logger;
    private final BonitaTransactionSynchronization bonitaTx;

    public JTATransactionWrapper(TechnicalLoggerService logger, final BonitaTransactionSynchronization bonitaTx) {
        this.logger = logger;
        this.bonitaTx = bonitaTx;
    }

    @Override
    public void beforeCompletion() {
        bonitaTx.beforeCommit();
    }

    @Override
    public void afterCompletion(final int status) {
        try {
            bonitaTx.afterCompletion(JTATransactionServiceImpl.convert(status));
        } catch (RuntimeException e) {
            logger.log(getClass(), TechnicalLogSeverity.ERROR, "Unexpected exception on after completion", e);
            throw e;
        }
    }

}
