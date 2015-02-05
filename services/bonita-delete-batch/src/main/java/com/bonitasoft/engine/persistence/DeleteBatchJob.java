/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.persistence;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.scheduler.InjectedService;
import org.bonitasoft.engine.scheduler.StatelessJob;
import org.bonitasoft.engine.scheduler.exception.SJobExecutionException;
import org.bonitasoft.engine.services.PersistenceService;

/**
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class DeleteBatchJob implements StatelessJob {

    private static final long serialVersionUID = 1L;

    private PersistenceService persistenceService;

    @InjectedService
    public void setPersistenceService(final PersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }

    @Override
    public String getName() {
        return "BATCH_DELETE";
    }

    @Override
    public String getDescription() {
        return "Batch delete of flagged elements";
    }

    @Override
    public void execute() throws SJobExecutionException {
        try {
            persistenceService.purge();
        } catch (final SBonitaException e) {
            throw new SJobExecutionException(e);
        }
    }

    @Override
    public void setAttributes(final Map<String, Serializable> attributes) {
    }
}
