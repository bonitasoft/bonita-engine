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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bonitasoft.engine.archive.ArchiveInsertRecord;
import org.bonitasoft.engine.archive.ArchivingStrategy;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.ArchivedPersistentObject;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.Test;

public class ArchiveServiceImplTest {

    // Only one test has to survive !
    @Test
    public void should_recordInserts_register_beforeCommitCallable_v2() throws Exception {
        final PersistenceService definitiveArchivePersistenceService = null;
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        final ArchivingStrategy archivingStrategy = null;
        final TransactionService transactionService = mock(TransactionService.class);

        ArchiveServiceImpl archiveService = spy(new ArchiveServiceImpl(definitiveArchivePersistenceService, logger, archivingStrategy, transactionService));

        final ArchivedPersistentObjectWithSetter mockArchivedPersistentObject = mock(ArchivedPersistentObjectWithSetter.class);
        ArchiveInsertRecord record = new ArchiveInsertRecord(mockArchivedPersistentObject);

        BatchArchiveCallable mockBatchArchiveCallable = mock(BatchArchiveCallable.class);
        when(archiveService.buildBatchArchiveCallable(any(ArchiveInsertRecord.class))).thenReturn(mockBatchArchiveCallable);

        long archiveDate = 3L;
        archiveService.recordInserts(archiveDate, record);

        verify(mockArchivedPersistentObject).setArchiveDate(eq(archiveDate));
        verify(transactionService, times(1)).registerBeforeCommitCallable(eq(mockBatchArchiveCallable));
    }


    // Test with exception on TxService

    // Seen with Nicolas C. for this "interface extension" :)
    // Needed as the implementation calls setArchiveDate through reflection.
    interface ArchivedPersistentObjectWithSetter extends ArchivedPersistentObject {
        void setArchiveDate(long archiveDate);
    }

}
