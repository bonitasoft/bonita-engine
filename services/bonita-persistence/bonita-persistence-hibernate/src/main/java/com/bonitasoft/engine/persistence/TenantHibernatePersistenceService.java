/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.persistence;

import java.util.Set;

import javax.sql.DataSource;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.DBConfigurationsProvider;
import org.bonitasoft.engine.persistence.HibernateConfigurationProvider;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.PersistentObjectWithFlag;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.sequence.SequenceManager;
import org.bonitasoft.engine.services.SPersistenceException;
import org.bonitasoft.engine.services.UpdateDescriptor;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;

public class TenantHibernatePersistenceService extends org.bonitasoft.engine.persistence.TenantHibernatePersistenceService {

    private static final String DELETED_KEY = "deleted";

    public TenantHibernatePersistenceService(final String name, final ReadSessionAccessor sessionAccessor,
            final HibernateConfigurationProvider hbmConfigurationProvider, final DBConfigurationsProvider tenantConfigurationsProvider,
            final String statementDelimiter, final String likeEscapeCharacter, final TechnicalLoggerService logger, final SequenceManager sequenceManager,
            final DataSource datasource, final boolean enableWordSearch, final Set<String> wordSearchExclusionMappings) throws SPersistenceException,
            ClassNotFoundException {
        super(name, sessionAccessor, hbmConfigurationProvider, tenantConfigurationsProvider, statementDelimiter, likeEscapeCharacter, logger,
                sequenceManager, datasource, enableWordSearch, wordSearchExclusionMappings);
    }

    @Override
    public void delete(final PersistentObject entity) throws SPersistenceException {
        if (entity instanceof PersistentObjectWithFlag) {
            final UpdateDescriptor buildSetField = UpdateDescriptor.buildSetField(entity, DELETED_KEY, true);
            super.update(buildSetField);
        } else {
            super.delete(entity);
        }
    }

    @Override
    public <T extends PersistentObject> T selectById(final SelectByIdDescriptor<T> selectDescriptor) throws SBonitaReadException {
        final T entity = super.selectById(selectDescriptor);
        if (entity instanceof PersistentObjectWithFlag) {
            final PersistentObjectWithFlag entityWithFlag = (PersistentObjectWithFlag) entity;
            if (entityWithFlag.isDeleted()) {
                return null;
            }
        }
        return entity;
    }
}
