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
import org.bonitasoft.engine.services.SPersistenceException;
import org.junit.Test;

public class BatchArchiveCallableTest {

    @Test
    public void testCreateArchivedObjectsList() {
        final ArchiveInsertRecord record1 = new ArchiveInsertRecord(mock(ArchivedPersistentObject.class));
        final ArchiveInsertRecord record2 = new ArchiveInsertRecord(mock(ArchivedPersistentObject.class));
        final ArchiveInsertRecord record3 = new ArchiveInsertRecord(mock(ArchivedPersistentObject.class));
        final ArchiveInsertRecord[] records = new ArchiveInsertRecord[] { record1, record2, record3 };
        final BatchArchiveCallable callable = new BatchArchiveCallable(null, records);

        final List<ArchivedPersistentObject> createArchivedObjectsList = callable.createArchivedObjectsList(records);

        assertThat(createArchivedObjectsList.size(), is(records.length));
        for (int i = 0; i < records.length; i++) {
            final ArchivedPersistentObject archivedPersistentObject = createArchivedObjectsList.get(i);
            assertThat(archivedPersistentObject, is(records[i].getEntity()));
        }

    }

    @Test
    public void testCreateArchivedObjectsListWithNoRecords() {
        final ArchiveInsertRecord[] records = new ArchiveInsertRecord[] {};
        final BatchArchiveCallable callable = new BatchArchiveCallable(null, records);

        final List<ArchivedPersistentObject> createArchivedObjectsList = callable.createArchivedObjectsList(records);

        assertTrue(createArchivedObjectsList.isEmpty());
    }

    @Test
    public void testCreateArchivedObjectsListWithNullRecords() {
        final ArchiveInsertRecord record1 = new ArchiveInsertRecord(mock(ArchivedPersistentObject.class));
        final ArchiveInsertRecord record2 = null;
        final ArchiveInsertRecord record3 = new ArchiveInsertRecord(mock(ArchivedPersistentObject.class));
        final ArchiveInsertRecord[] records = new ArchiveInsertRecord[] { record1, record2, record3 };
        final BatchArchiveCallable callable = new BatchArchiveCallable(null, records);

        final List<ArchivedPersistentObject> createArchivedObjectsList = callable.createArchivedObjectsList(records);

        // The second one that is null was skipped.
        assertThat(createArchivedObjectsList.size(), is(2));

        ArchivedPersistentObject archivedPersistentObject;
        // Check the first one
        archivedPersistentObject = createArchivedObjectsList.get(0);
        assertThat(archivedPersistentObject, is(records[0].getEntity()));

        // Check the second that is the third one.
        archivedPersistentObject = createArchivedObjectsList.get(1);
        assertThat(archivedPersistentObject, is(records[2].getEntity()));
    }

    @Test
    public void testHasObjectsReturnsTrueWhenNotEmpty() {
        final ArchiveInsertRecord record1 = new ArchiveInsertRecord(mock(ArchivedPersistentObject.class));
        final ArchiveInsertRecord record2 = new ArchiveInsertRecord(mock(ArchivedPersistentObject.class));
        final ArchiveInsertRecord record3 = new ArchiveInsertRecord(mock(ArchivedPersistentObject.class));
        final ArchiveInsertRecord[] records = new ArchiveInsertRecord[] { record1, record2, record3 };
        final BatchArchiveCallable callable = new BatchArchiveCallable(null, records);

        assertTrue(callable.hasObjects());
    }

    @Test
    public void testHasObjectsReturnsFalseWhenEmpty() {
        final BatchArchiveCallable callable = new BatchArchiveCallable(null, new ArchiveInsertRecord[] {});

        assertFalse(callable.hasObjects());
    }

    @Test
    public void testHasObjectsReturnsFalseWhenNull() {
        final BatchArchiveCallable callable = new BatchArchiveCallable(null, (ArchiveInsertRecord) null);

        assertFalse(callable.hasObjects());
    }

    @Test
    public void testCallWithOneRecord() throws SPersistenceException {
        final ArchivedPersistentObject entity = mock(ArchivedPersistentObject.class);
        final ArchiveInsertRecord record1 = new ArchiveInsertRecord(entity);
        final ArchiveInsertRecord[] records = new ArchiveInsertRecord[] { record1 };
        final PersistenceService persistenceService = mock(PersistenceService.class);
        final BatchArchiveCallable callable = new BatchArchiveCallable(persistenceService, records);

        callable.call();

        verify(persistenceService).insert(eq(entity));
    }

    @Test
    public void testCallWithMoreThanOneRecord() throws SPersistenceException {
        final ArchiveInsertRecord record1 = new ArchiveInsertRecord(mock(ArchivedPersistentObject.class));
        final ArchiveInsertRecord record2 = new ArchiveInsertRecord(mock(ArchivedPersistentObject.class));
        final ArchiveInsertRecord record3 = new ArchiveInsertRecord(mock(ArchivedPersistentObject.class));
        final ArchiveInsertRecord[] records = new ArchiveInsertRecord[] { record1, record2, record3 };
        final PersistenceService persistenceService = mock(PersistenceService.class);
        final BatchArchiveCallable callable = new BatchArchiveCallable(persistenceService, records);

        callable.call();

        verify(persistenceService).insertInBatch(anyListOf(PersistentObject.class));
    }

}
