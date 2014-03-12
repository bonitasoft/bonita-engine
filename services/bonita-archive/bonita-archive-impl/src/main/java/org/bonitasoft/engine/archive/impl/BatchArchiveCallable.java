package org.bonitasoft.engine.archive.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.persistence.ArchivedPersistentObject;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.services.PersistenceService;

public class BatchArchiveCallable implements Callable<Void> {

    private final PersistenceService persistenceService;
    private final List<? extends ArchivedPersistentObject> archivedObjects;

    public BatchArchiveCallable(final PersistenceService persistenceService, final List<ArchivedPersistentObject> archivedObjects) {
        super();
        this.persistenceService = persistenceService;
        this.archivedObjects = archivedObjects;
    }

    @Override
    public Void call() throws Exception {
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
