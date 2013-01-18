/**
 * Copyright (C) 2011 BonitaSoft S.A.
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
package org.bonitasoft.engine.persistence;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sequence.SequenceManager;
import org.bonitasoft.engine.services.SPersistenceException;
import org.bonitasoft.engine.sessionaccessor.TenantIdNotSetException;
import org.bonitasoft.engine.transaction.TransactionService;

/**
 * @author Charles Souillard
 */
public class PlatformMybatisPersistenceService extends AbstractMybatisPersistenceService {

    public PlatformMybatisPersistenceService(final String name, final String dbIdentifier, final TransactionService txService, final boolean cacheEnabled,
            final MybatisSqlSessionFactoryProvider mybatisSqlSessionFactoryProvider, final PlatformMyBatisConfigurationsProvider configurations,
            final DBConfigurationsProvider dbConfigurationsProvider, final String statementDelimiter, final TechnicalLoggerService technicalLoggerService,
            final SequenceManager sequenceManager) throws SPersistenceException {
        super(name, dbIdentifier, txService, cacheEnabled, mybatisSqlSessionFactoryProvider, configurations, dbConfigurationsProvider, statementDelimiter,
                technicalLoggerService, sequenceManager);
    }

    @Override
    protected Map<String, Object> getDefaultParameters() throws TenantIdNotSetException {
        return new HashMap<String, Object>();
    }

    @Override
    protected long getTenantId() throws TenantIdNotSetException {
        return -1;
    }

}
