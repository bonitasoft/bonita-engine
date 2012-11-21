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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.model.PlatformSequence;
import org.bonitasoft.engine.services.SPersistenceException;
import org.bonitasoft.engine.sessionaccessor.TenantIdNotSetException;
import org.bonitasoft.engine.transaction.TransactionService;

/**
 * @author Charles Souillard
 */
public class PlatformMybatisPersistenceService extends AbstractMybatisPersistenceService {

    private final MyBatisSequenceManager<PlatformSequence> sequenceManager;

    public PlatformMybatisPersistenceService(final String name, final String dbIdentifier, final TransactionService txService, final boolean cacheEnabled,
            final MybatisSqlSessionFactoryProvider mybatisSqlSessionFactoryProvider, final PlatformMyBatisConfigurationsProvider configurations,
            final DBConfigurationsProvider dbConfigurationsProvider, final int rangeSize, final String statementDelimiter,
            final TechnicalLoggerService technicalLoggerService) throws SPersistenceException {
        super(name, dbIdentifier, txService, cacheEnabled, mybatisSqlSessionFactoryProvider, configurations, rangeSize, dbConfigurationsProvider,
                statementDelimiter, technicalLoggerService);
        sequenceManager = new MyBatisSequenceManager<PlatformSequence>(this, rangeSize, PlatformSequence.class, "getPlatformSequence", false);
    }

    @Override
    public void deleteStructure() throws SPersistenceException, IOException {
        super.deleteStructure();
        sequenceManager.reset();
    }

    @Override
    protected Map<String, Object> getDefaultParameters() throws TenantIdNotSetException {
        return new HashMap<String, Object>();
    }

    @Override
    protected MyBatisSequenceManager<PlatformSequence> getSequenceManager() {
        return sequenceManager;
    }

}
