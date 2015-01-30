/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.persistence;

import org.bonitasoft.engine.persistence.DeleteService;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.PersistentObjectWithFlag;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.services.SPersistenceException;
import org.bonitasoft.engine.services.UpdateDescriptor;

/**
 * Implementation of the delete service that just mark elements as delete and allow it for real deletion after
 * 
 * @author Baptiste Mesta
 */
public class DeleteServiceBatch implements DeleteService {

    private static final String DELETED_KEY = "deleted";

    private final PersistenceService persistenceService;

    public DeleteServiceBatch(final PersistenceService persistenceService) {
        super();
        this.persistenceService = persistenceService;
    }

    @Override
    public void delete(final PersistentObject entity) throws SPersistenceException {
        if (entity instanceof PersistentObjectWithFlag) {
            final UpdateDescriptor buildSetField = UpdateDescriptor.buildSetField(entity, DELETED_KEY, true);
            persistenceService.update(buildSetField);
        } else {
            persistenceService.delete(entity);
        }
    }

}
