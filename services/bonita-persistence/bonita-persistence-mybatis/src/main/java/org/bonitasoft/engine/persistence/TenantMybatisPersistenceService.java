/**
 * Copyright (C) 2011-2012 BonitaSoft S.A.
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
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.commons.ClassReflector;
import org.bonitasoft.engine.commons.ReflectException;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.model.TenantSequence;
import org.bonitasoft.engine.services.SPersistenceException;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.sessionaccessor.TenantIdNotSetException;
import org.bonitasoft.engine.transaction.TransactionService;

/**
 * @author Charles Souillard
 * @author Matthieu Chaffotte
 */
public class TenantMybatisPersistenceService extends AbstractMybatisPersistenceService {

    private final Map<Long, MyBatisSequenceManager<TenantSequence>> sequenceManagers;

    private final ReadSessionAccessor sessionAccessor;

    private final Map<String, Integer> rangeSizes;

    private final int rangeSize;

    public TenantMybatisPersistenceService(final String name, final String dbIdentifier, final TransactionService txService,
            final ReadSessionAccessor sessionAccessor, final boolean cacheEnabled, final MybatisSqlSessionFactoryProvider mybatisSqlSessionFactoryProvider,
            final MyBatisConfigurationsProvider configurations, final int rangeSize, final Map<String, Integer> rangeSizes,
            final DBConfigurationsProvider dbConfigurationsProvider, final String statementDelimiter, final TechnicalLoggerService technicalLoggerService)
            throws SPersistenceException {
        super(name, dbIdentifier, txService, cacheEnabled, mybatisSqlSessionFactoryProvider, configurations, rangeSize, dbConfigurationsProvider,
                statementDelimiter, technicalLoggerService);
        this.rangeSize = rangeSize;
        this.rangeSizes = rangeSizes;
        this.sessionAccessor = sessionAccessor;
        sequenceManagers = new HashMap<Long, MyBatisSequenceManager<TenantSequence>>();
    }

    @Override
    public void deleteStructure() throws SPersistenceException, IOException {
        super.deleteStructure();
        sequenceManagers.clear();
    }

    @Override
    protected Map<String, Object> getDefaultParameters() throws TenantIdNotSetException {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        final Long tenantId = sessionAccessor.getTenantId();
        parameters.put("tenantid", tenantId);
        return parameters;
    }

    @Override
    protected MyBatisSequenceManager<TenantSequence> getSequenceManager(final PersistentObject entity) throws TenantIdNotSetException {
        final Long tenantId = sessionAccessor.getTenantId();
        if (!sequenceManagers.containsKey(tenantId)) {
            synchronized (this) {
                // double check mandatory as the whole method is not synchronized
                if (!sequenceManagers.containsKey(tenantId)) {
                    final MyBatisSequenceManager<TenantSequence> sequenceManager = new MyBatisSequenceManager<TenantSequence>(this, rangeSize, rangeSizes,
                            TenantSequence.class, "getSequence", true, sequencesMappings);
                    sequenceManagers.put(tenantId, sequenceManager);
                }
            }
        }
        return sequenceManagers.get(tenantId);
    }

    @Override
    public void deleteTenant(final long tenantId) throws SPersistenceException, IOException {
        final MyBatisSequenceManager<TenantSequence> myBatisSequenceManager = sequenceManagers.get(tenantId);
        if (myBatisSequenceManager != null) {
            myBatisSequenceManager.clear();
            sequenceManagers.remove(tenantId);
        }
        super.deleteTenant(tenantId);
    }

    @Override
    protected String getInsertScript(final List<PersistentObject> entities) throws SPersistenceException {
        try {
            return super.getInsertScript(entities).replaceAll(SQLTransformer.TENANTID_TOKEN, String.valueOf(getTenantId()));
        } catch (final TenantIdNotSetException e) {
            throw new SPersistenceException(e);
        }
    }

    @Override
    public void insert(final PersistentObject entity) throws SPersistenceException {
        setTenant(entity);
        super.insert(entity);
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

    private long getTenantId() throws TenantIdNotSetException {
        return sessionAccessor.getTenantId();
    }

}
