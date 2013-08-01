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

import java.util.List;

import javax.sql.DataSource;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sequence.SequenceManager;
import org.bonitasoft.engine.sequence.exceptions.SequenceManagerException;
import org.bonitasoft.engine.services.SPersistenceException;
import org.bonitasoft.engine.sessionaccessor.TenantIdNotSetException;

/**
 * @author Baptiste Mesta
 * @author Yanyan Liu
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class PlatformHibernatePersistenceService extends AbstractHibernatePersistenceService {

    public PlatformHibernatePersistenceService(
            final String name,
            final HibernateConfigurationProvider hbmConfigurationProvider,
            final DBConfigurationsProvider dbConfigurationsProvider,
            final String statementDelimiter,
            final String likeEscapeCharacter,
            final TechnicalLoggerService logger,
            final SequenceManager sequenceManager,
            final DataSource datasource) throws SPersistenceException, SequenceManagerException {
        super(name, hbmConfigurationProvider, dbConfigurationsProvider, statementDelimiter, likeEscapeCharacter, logger, sequenceManager, datasource);
    }

    @Override
    protected long getTenantId() throws TenantIdNotSetException {
        return -1;
    }

    @Override
    public void flushStatements() throws SPersistenceException {
        super.flushStatements(false);
    }

    @Override
    public void deleteByTenant(Class<? extends PersistentObject> entityClass, final List<FilterOption> filters) throws SPersistenceException {
        // FIXME : Method for tenant. TODO: Refactor code for PlatformHibernatePersistenceService don't implements TenantPersistenceService
    }

}
