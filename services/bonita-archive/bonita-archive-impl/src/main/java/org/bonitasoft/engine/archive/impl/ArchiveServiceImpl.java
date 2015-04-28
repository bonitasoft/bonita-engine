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
package org.bonitasoft.engine.archive.impl;

import org.bonitasoft.engine.archive.ArchiveInsertRecord;
import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.archive.ArchivingStrategy;
import org.bonitasoft.engine.commons.ClassReflector;
import org.bonitasoft.engine.commons.LogUtil;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.ArchivedPersistentObject;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.services.SPersistenceException;
import org.bonitasoft.engine.transaction.STransactionNotFoundException;
import org.bonitasoft.engine.transaction.TransactionService;

/**
 * @author Matthieu Chaffotte
 * @author Hongwen Zang
 * @author Celine Souchet
 */
public class ArchiveServiceImpl implements ArchiveService {

    private final TransactionService transactionService;

    private final PersistenceService definitiveArchivePersistenceService;

    private final TechnicalLoggerService logger;

    private ArchivingStrategy archivingStrategy;

    public ArchiveServiceImpl(final PersistenceService definitiveArchivePersistenceService,
            final TechnicalLoggerService logger, final ArchivingStrategy archivingStrategy, final TransactionService transactionService) {
        super();
        this.definitiveArchivePersistenceService = definitiveArchivePersistenceService;
        this.archivingStrategy = archivingStrategy;
        this.logger = logger;
        this.transactionService = transactionService;
    }

    @Override
    public void recordInsert(final long time, final ArchiveInsertRecord record) throws SRecorderException {
        if (isArchivable(record.getEntity().getPersistentObjectInterface())) {
            recordInserts(time, record);
        }
    }

    @Override
    public void recordInserts(final long time, final ArchiveInsertRecord... records) throws SRecorderException {
        final String methodName = "recordInserts";
        logBeforeMethod(TechnicalLogSeverity.TRACE, methodName);
        if (records != null) {
            assignArchiveDate(time, records);
            final BatchArchiveCallable callable = buildBatchArchiveCallable(records);

            try {
                transactionService.registerBeforeCommitCallable(callable);
            } catch (final STransactionNotFoundException e) {
                if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                    logger.log(this.getClass(), TechnicalLogSeverity.ERROR, "Unable to register the beforeCommitCallable to log queriable logs: transaction not found",
                            e);
                }
            }
        }

        logAfterMethod(TechnicalLogSeverity.TRACE, methodName);
    }

    // As a protected method for test purposes.
    protected BatchArchiveCallable buildBatchArchiveCallable(final ArchiveInsertRecord... records) throws SRecorderException {
        return new BatchArchiveCallable(definitiveArchivePersistenceService, records);
    }

    private void assignArchiveDate(final long time, final ArchiveInsertRecord... records) throws SRecorderException {
        for (final ArchiveInsertRecord record : records) {
            if (record != null) {
                setArchiveDate(record.getEntity(), time);
            }
        }
    }

    private void setArchiveDate(final ArchivedPersistentObject entity, final long time) throws SRecorderException {
        if (entity.getArchiveDate() <= 0) {
            try {
                ClassReflector.invokeSetter(entity, "setArchiveDate", long.class, time);
            } catch (final Exception e) {
                throw new SRecorderException(e);
            }
        }
    }


    @Override
    public void recordDelete(final DeleteRecord record) throws SRecorderException {
        String methodName = "recordDelete";
        try {
            logBeforeMethod(TechnicalLogSeverity.TRACE, methodName);
            definitiveArchivePersistenceService.delete(record.getEntity());
            logAfterMethod(TechnicalLogSeverity.TRACE, methodName);
        } catch (final SPersistenceException e) {
            logOnExceptionMethod(TechnicalLogSeverity.TRACE, methodName, e);
            throw new SRecorderException(e);
        }
    }

    private void logOnExceptionMethod(final TechnicalLogSeverity technicalLogSeverity, final String methodName, final Exception e) {
        if (logger.isLoggable(this.getClass(), technicalLogSeverity)) {
            logger.log(this.getClass(), technicalLogSeverity, LogUtil.getLogOnExceptionMethod(this.getClass(), methodName, e));
        }
    }

    private void logAfterMethod(final TechnicalLogSeverity technicalLogSeverity, final String methodName) {
        if (logger.isLoggable(this.getClass(), technicalLogSeverity)) {
            logger.log(this.getClass(), technicalLogSeverity, LogUtil.getLogAfterMethod(this.getClass(), methodName));
        }
    }

    private void logBeforeMethod(final TechnicalLogSeverity technicalLogSeverity, final String methodName) {
        if (logger.isLoggable(this.getClass(), technicalLogSeverity)) {
            logger.log(this.getClass(), technicalLogSeverity, LogUtil.getLogBeforeMethod(this.getClass(), methodName));
        }
    }

    @Override
    public boolean isArchivable(final Class<? extends PersistentObject> sourceObjectClass) {
        return archivingStrategy.isArchivable(sourceObjectClass);
    }

    @Override
    public ReadPersistenceService getDefinitiveArchiveReadPersistenceService() {
        return definitiveArchivePersistenceService;
    }

}
