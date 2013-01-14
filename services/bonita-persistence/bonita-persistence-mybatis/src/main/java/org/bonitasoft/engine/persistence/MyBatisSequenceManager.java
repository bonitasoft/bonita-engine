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

import java.util.Map;

import org.bonitasoft.engine.persistence.model.Sequence;
import org.bonitasoft.engine.services.SPersistenceException;
import org.bonitasoft.engine.services.UpdateDescriptor;
import org.bonitasoft.engine.transaction.TechnicalTransaction;

/**
 * @author Charles Souillard
 */
public class MyBatisSequenceManager<T extends Sequence> extends SequenceManager<T, AbstractMybatisPersistenceService> {

    public MyBatisSequenceManager(final AbstractMybatisPersistenceService persistenceService, final Integer rangeSize, final Map<Long, Integer> rangeSizes,
            final Class<T> sequenceClass, final String getSequenceQueryName, final boolean useTenant, final Map<String, Long> sequencesMappings) {
        super(persistenceService, rangeSizes, rangeSize, sequenceClass, getSequenceQueryName, useTenant, sequencesMappings);
    }

    @Override
    protected Object selectById(final TechnicalTransaction tx, final SelectByIdDescriptor<T> selectDescriptor) throws SBonitaReadException {
        final MybatisSession session = ((MybatisTechnicalTransaction) tx).getSession();
        final Object r = getPersistenceService().selectById(session, selectDescriptor);
        return r;
    }

    @Override
    protected TechnicalTransaction createTransaction(final boolean useTenant) throws SPersistenceException {
        return getPersistenceService().createTechnicalTransaction(null, false);
    }

    @Override
    protected void updateSequence(final TechnicalTransaction tx, final Sequence sequence, final long nextSequenceId) throws SPersistenceException {
        final MybatisSession session = ((MybatisTechnicalTransaction) tx).getSession();
        getPersistenceService().update(session, UpdateDescriptor.buildSetField(sequence, "nextId", nextSequenceId));
    }

}
