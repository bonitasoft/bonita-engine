/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.persistence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.DBConfigurationsProvider;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.sequence.SequenceManager;
import org.bonitasoft.engine.services.SPersistenceException;
import org.bonitasoft.engine.sessionaccessor.TenantIdNotSetException;
import org.bonitasoft.engine.transaction.TransactionService;

/**
 * @author Charles Souillard
 */
public class PlatformMybatisPersistenceService extends AbstractMybatisPersistenceService {

    public PlatformMybatisPersistenceService(final String name, final String dbIdentifier, final TransactionService txService,
            final MybatisSqlSessionFactoryProvider mybatisSqlSessionFactoryProvider, final PlatformMyBatisConfigurationsProvider configurations,
            final DBConfigurationsProvider dbConfigurationsProvider, final String statementDelimiter, final String likeEscapeCharacter,
            final TechnicalLoggerService technicalLoggerService,
            final SequenceManager sequenceManager, final DataSource datasource) throws SPersistenceException {
        super(name, dbIdentifier, txService, mybatisSqlSessionFactoryProvider, configurations, dbConfigurationsProvider, statementDelimiter,
                likeEscapeCharacter,
                technicalLoggerService, sequenceManager, datasource);
    }

    @Override
    protected Map<String, Object> getDefaultParameters() throws TenantIdNotSetException {
        return new HashMap<String, Object>();
    }

    @Override
    protected long getTenantId() throws TenantIdNotSetException {
        return -1;
    }

    @Override
    public void deleteByTenant(Class<? extends PersistentObject> entityClass, List<FilterOption> filters) throws SPersistenceException {
        // TODO Auto-generated method stub
    }

}
