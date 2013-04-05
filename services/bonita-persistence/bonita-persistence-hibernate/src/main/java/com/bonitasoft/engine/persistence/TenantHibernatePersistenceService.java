package com.bonitasoft.engine.persistence;

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
import org.bonitasoft.engine.transaction.TransactionService;

public class TenantHibernatePersistenceService extends org.bonitasoft.engine.persistence.TenantHibernatePersistenceService {

    private static final String DELETED_KEY = "deleted";
    
    public TenantHibernatePersistenceService(final String name, final TransactionService txService, final ReadSessionAccessor sessionAccessor,
            final HibernateConfigurationProvider hbmConfigurationProvider, final DBConfigurationsProvider tenantConfigurationsProvider,
            final String statementDelimiter, final String likeEscapeCharacter, final TechnicalLoggerService logger, final SequenceManager sequenceManager)
            throws SPersistenceException {
        super(name, txService, sessionAccessor, hbmConfigurationProvider, tenantConfigurationsProvider, statementDelimiter, likeEscapeCharacter, logger,
                sequenceManager);
    }
    
    @Override
    public void delete(PersistentObject entity) throws SPersistenceException {
        if (entity instanceof PersistentObjectWithFlag) {
            final UpdateDescriptor buildSetField = UpdateDescriptor.buildSetField(entity, DELETED_KEY, true);
            super.update(buildSetField);
            DeleteBatchJobRegister.getInstance().registerJobIfNotRegistered();
        } else {
            super.delete(entity);
        }
    }
    
    @Override
    public <T extends PersistentObject> T selectById(SelectByIdDescriptor<T> selectDescriptor) throws SBonitaReadException {
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
