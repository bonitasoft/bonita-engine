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
package org.bonitasoft.engine.services.impl;

import java.util.List;

import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.services.QueriableLogSessionProvider;
import org.bonitasoft.engine.services.QueriableLoggerStrategy;
import org.bonitasoft.engine.services.SPersistenceException;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SyncQueriableLoggerServiceImpl extends AbstractQueriableLoggerImpl {

    public SyncQueriableLoggerServiceImpl(final PersistenceService persistenceService,
            final QueriableLoggerStrategy loggerStrategy, final QueriableLogSessionProvider sessionProvider, final TechnicalLoggerService logger,
            final PlatformService platformService) {
        super(persistenceService, loggerStrategy, sessionProvider, platformService, logger);
    }

    // the method log is considered to already be in a transaction.
    @Override
    protected void log(final List<SQueriableLog> loggableLogs) {
        try {
            if (logger != null && logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, "Persisting log...");
            }
            for (final SQueriableLog log : loggableLogs) {
                getPersitenceService().insert(log);
            }
            if (logger != null && logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, "Log persisted !!");
            }

        } catch (final SPersistenceException e) {
            final String message = "Error while persisting logs transaction :" + System.getProperty("line.separator") + "Logs " + loggableLogs;
            if (logger != null && logger.isLoggable(this.getClass(), TechnicalLogSeverity.ERROR)) {
                logger.log(this.getClass(), TechnicalLogSeverity.ERROR, message, e);
            }
        }
    }

}
