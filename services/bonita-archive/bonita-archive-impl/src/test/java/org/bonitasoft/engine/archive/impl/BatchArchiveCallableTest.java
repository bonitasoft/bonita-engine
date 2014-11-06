package org.bonitasoft.engine.archive.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.bonitasoft.engine.archive.ArchiveInsertRecord;
import org.bonitasoft.engine.persistence.ArchivedPersistentObject;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.services.PersistenceService;
import org.junit.Test;

public class BatchArchiveCallableTest {

    @Test
    public void testCreateArchivedObjectsList() throws Exception {
        ArchiveInsertRecord record1 = new ArchiveInsertRecord(mock(ArchivedPersistentObject.class));
        ArchiveInsertRecord record2 = new ArchiveInsertRecord(mock(ArchivedPersistentObject.class));
        ArchiveInsertRecord record3 = new ArchiveInsertRecord(mock(ArchivedPersistentObject.class));
        ArchiveInsertRecord[] records = new ArchiveInsertRecord[] {record1, record2, record3};
        BatchArchiveCallable callable = new BatchArchiveCallable(null, records);

        List<ArchivedPersistentObject> createArchivedObjectsList = callable.createArchivedObjectsList(records);

        assertThat(createArchivedObjectsList.size(), is(records.length));
        for (int i = 0; i < records.length; i++) {
            ArchivedPersistentObject archivedPersistentObject = createArchivedObjectsList.get(i);
            assertThat(archivedPersistentObject, is(records[i].getEntity()));
        }

    }

    @Test
    public void testCreateArchivedObjectsListWithNoRecords() throws Exception {
        ArchiveInsertRecord[] records = new ArchiveInsertRecord[] {};
        BatchArchiveCallable callable = new BatchArchiveCallable(null, records);

        List<ArchivedPersistentObject> createArchivedObjectsList = callable.createArchivedObjectsList(records);

        assertTrue(createArchivedObjectsList.isEmpty());
    }

    @Test
    public void testCreateArchivedObjectsListWithNullRecords() throws Exception {
        ArchiveInsertRecord record1 = new ArchiveInsertRecord(mock(ArchivedPersistentObject.class));
        ArchiveInsertRecord record2 = null;
        ArchiveInsertRecord record3 = new ArchiveInsertRecord(mock(ArchivedPersistentObject.class));
        ArchiveInsertRecord[] records = new ArchiveInsertRecord[] {record1, record2, record3};
        BatchArchiveCallable callable = new BatchArchiveCallable(null, records);

        List<ArchivedPersistentObject> createArchivedObjectsList = callable.createArchivedObjectsList(records);

        // The second one that is null was skipped.
        assertThat(createArchivedObjectsList.size(), is(2));

        ArchivedPersistentObject archivedPersistentObject;
        // Check the first one
        archivedPersistentObject= createArchivedObjectsList.get(0);
        assertThat(archivedPersistentObject, is(records[0].getEntity()));

        // Check the second that is the third one.
        archivedPersistentObject= createArchivedObjectsList.get(1);
        assertThat(archivedPersistentObject, is(records[2].getEntity()));
    }

    @Test
    public void testHasObjectsReturnsTrueWhenNotEmpty() throws Exception {
        ArchiveInsertRecord record1 = new ArchiveInsertRecord(mock(ArchivedPersistentObject.class));
        ArchiveInsertRecord record2 = new ArchiveInsertRecord(mock(ArchivedPersistentObject.class));
        ArchiveInsertRecord record3 = new ArchiveInsertRecord(mock(ArchivedPersistentObject.class));
        ArchiveInsertRecord[] records = new ArchiveInsertRecord[] {record1, record2, record3};
        BatchArchiveCallable callable = new BatchArchiveCallable(null, records);

        assertTrue(callable.hasObjects());
    }

    @Test
    public void testHasObjectsReturnsFalseWhenEmpty() throws Exception {
        BatchArchiveCallable callable = new BatchArchiveCallable(null, new ArchiveInsertRecord[] {});

        assertFalse(callable.hasObjects());
    }

    @Test
    public void testHasObjectsReturnsFalseWhenNull() throws Exception {
        BatchArchiveCallable callable = new BatchArchiveCallable(null, (ArchiveInsertRecord) null);

        assertFalse(callable.hasObjects());
    }

    @Test
    public void testCallWithOneRecord() throws Exception {
        final ArchivedPersistentObject entity = mock(ArchivedPersistentObject.class);
        ArchiveInsertRecord record1 = new ArchiveInsertRecord(entity);
        ArchiveInsertRecord[] records = new ArchiveInsertRecord[] {record1};
        PersistenceService persistenceService = mock(PersistenceService.class);
        BatchArchiveCallable callable = new BatchArchiveCallable(persistenceService, records);

        callable.call();

        verify(persistenceService).insert(eq(entity));
    }

    @Test
    public void testCallWithMoreThanOneRecord() throws Exception {
        ArchiveInsertRecord record1 = new ArchiveInsertRecord(mock(ArchivedPersistentObject.class));
        ArchiveInsertRecord record2 = new ArchiveInsertRecord(mock(ArchivedPersistentObject.class));
        ArchiveInsertRecord record3 = new ArchiveInsertRecord(mock(ArchivedPersistentObject.class));
        ArchiveInsertRecord[] records = new ArchiveInsertRecord[] {record1, record2, record3};
        PersistenceService persistenceService = mock(PersistenceService.class);
        BatchArchiveCallable callable = new BatchArchiveCallable(persistenceService, records);

        callable.call();

        verify(persistenceService).insertInBatch(anyListOf(PersistentObject.class));
    }

}
