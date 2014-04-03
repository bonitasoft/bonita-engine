/**
 * Copyright (C) 2011-2014 BonitaSoft S.A.
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

import org.bonitasoft.engine.persistence.ArchivedPersistentObject;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.services.SPersistenceException;

public class BatchArchiveCallable implements Callable<Void> {

    private final PersistenceService persistenceService;

    private final List<? extends ArchivedPersistentObject> archivedObjects;

    public BatchArchiveCallable(final PersistenceService persistenceService, final List<ArchivedPersistentObject> archivedObjects) {
        super();
        this.persistenceService = persistenceService;
        this.archivedObjects = archivedObjects;
    }

    @Override
    public Void call() throws SPersistenceException {
        if (this.archivedObjects != null && !this.archivedObjects.isEmpty()) {
            try {
                if (this.archivedObjects.size() == 1) {
                    this.persistenceService.insert(archivedObjects.get(0));
                } else {
                    this.persistenceService.insertInBatch(new ArrayList<PersistentObject>(this.archivedObjects));
                }
            } finally {
                this.archivedObjects.clear();
            }
        }
        return null;
    }

    public boolean hasObjects() {
        return this.archivedObjects != null && !this.archivedObjects.isEmpty();
    }

}
