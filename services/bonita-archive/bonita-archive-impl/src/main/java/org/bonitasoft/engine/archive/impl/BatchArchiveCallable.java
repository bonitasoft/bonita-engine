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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.archive.ArchiveInsertRecord;
import org.bonitasoft.engine.persistence.ArchivedPersistentObject;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.services.SPersistenceException;

public class BatchArchiveCallable implements Callable<Void> {

    private final PersistenceService persistenceService;

    private final List<? extends ArchivedPersistentObject> archivedObjects;

    public BatchArchiveCallable(final PersistenceService persistenceService, final ArchiveInsertRecord... records) {
        this.persistenceService = persistenceService;
        if (records == null) {
            archivedObjects = new ArrayList<ArchivedPersistentObject>();
        } else {
            archivedObjects = createArchivedObjectsList(records);
        }
    }

    /**
     * @param time
     * @param records
     * @return
     * @throws SRecorderException
     */
    protected List<ArchivedPersistentObject> createArchivedObjectsList(final ArchiveInsertRecord... records) {
        final List<ArchivedPersistentObject> archivedObjects = new ArrayList<ArchivedPersistentObject>();
        for (final ArchiveInsertRecord record : records) {
            if (record != null) {
                archivedObjects.add(record.getEntity());
            }
        }
        return archivedObjects;
    }

    @Override
    public Void call() throws SPersistenceException {
        if (hasObjects()) {
            try {
                if (archivedObjects.size() == 1) {
                    persistenceService.insert(archivedObjects.get(0));
                } else {
                    persistenceService.insertInBatch(new ArrayList<PersistentObject>(archivedObjects));
                }
            } finally {
                // Do we still need to clear the list even if there was some Exceptions ?
                // What happens with the retry ?
                archivedObjects.clear();
            }
        }
        return null;
    }

    public boolean hasObjects() {
        return archivedObjects != null && !archivedObjects.isEmpty();
    }

}
