/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package org.bonitasoft.engine.persistence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.commons.ClassReflector;
import org.bonitasoft.engine.commons.ReflectException;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sequence.SequenceManager;
import org.bonitasoft.engine.services.SPersistenceException;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.sessionaccessor.TenantIdNotSetException;
import org.bonitasoft.engine.transaction.TransactionService;

/**
 * @author Charles Souillard
 * @author Matthieu Chaffotte
 * @author Baptiste Mesta
 */
public class TenantMybatisPersistenceService extends AbstractMybatisPersistenceService {

    private final ReadSessionAccessor sessionAccessor;

    public TenantMybatisPersistenceService(final String name, final String dbIdentifier, final TransactionService txService,
            final ReadSessionAccessor sessionAccessor, final boolean cacheEnabled, final MybatisSqlSessionFactoryProvider mybatisSqlSessionFactoryProvider,
            final MyBatisConfigurationsProvider configurations, final DBConfigurationsProvider dbConfigurationsProvider, final String statementDelimiter,
            final String likeEscapeCharacter, final TechnicalLoggerService technicalLoggerService, final SequenceManager sequenceManager)
            throws SPersistenceException {
        super(name, dbIdentifier, txService, cacheEnabled, mybatisSqlSessionFactoryProvider, configurations, dbConfigurationsProvider, statementDelimiter,
                likeEscapeCharacter, technicalLoggerService, sequenceManager);
        this.sessionAccessor = sessionAccessor;
    }

    @Override
    protected Map<String, Object> getDefaultParameters() throws TenantIdNotSetException {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        final Long tenantId = sessionAccessor.getTenantId();
        parameters.put("tenantid", tenantId);
        return parameters;
    }

    @Override
    public void insert(final PersistentObject entity) throws SPersistenceException {
        setTenant(entity);
        super.insert(entity);
    }

    @Override
    public void insertInBatch(final List<PersistentObject> entities) throws SPersistenceException {
        for (final PersistentObject persistentObject : entities) {
            setTenant(persistentObject);
        }
        super.insertInBatch(entities);
    }

    protected void setTenant(final PersistentObject entity) throws SPersistenceException {
        if (entity == null) {
            return;
        }
        // if this entity has no id, set it
        Long tenantId = null;
        try {
            tenantId = ClassReflector.invokeGetter(Long.class, entity, "getTenantId");
        } catch (final Exception e) {
            // this is a new object to save
        }
        if (tenantId == null || tenantId == -1 || tenantId == 0) {
            try {
                tenantId = getTenantId();
                ClassReflector.invokeSetter(entity, "setTenantId", long.class, tenantId);
            } catch (final ReflectException e) {
                throw new SPersistenceException("Can't set tenant on entity " + entity, e);
            } catch (final TenantIdNotSetException e) {
                throw new SPersistenceException("Can't set tenant on entity " + entity, e);
            }
        }
    }

    @Override
    protected long getTenantId() throws TenantIdNotSetException {
        return sessionAccessor.getTenantId();
    }

}
