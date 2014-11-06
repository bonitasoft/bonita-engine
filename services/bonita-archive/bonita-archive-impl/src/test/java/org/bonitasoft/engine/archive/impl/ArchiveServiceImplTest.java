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
import org.bonitasoft.engine.archive.SArchiveDescriptor;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.ArchivedPersistentObject;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.Test;

public class ArchiveServiceImplTest {

    // Only one test has to survive !
    @Test
    public void should_recordInserts_register_beforeCommitCallable_v2() throws Exception {
        final SArchiveDescriptor definitiveArchiveDescriptor = null;
        final PersistenceService definitiveArchivePersistenceService = null;
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        final ArchivingStrategy archivingStrategy = null;
        final TransactionService transactionService = mock(TransactionService.class);

        ArchiveServiceImpl archiveService = spy(new ArchiveServiceImpl(definitiveArchiveDescriptor, definitiveArchivePersistenceService, logger, archivingStrategy, transactionService));

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
