/**
 * Copyright (C) 2015 BonitaSoft S.A.
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

package org.bonitasoft.engine.business.data.proxy;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.bdm.model.field.Field;
import org.bonitasoft.engine.business.data.BusinessDataRepository;
import org.bonitasoft.engine.business.data.NonUniqueResultException;

public class ServerLazyLoader {

    
    private BusinessDataRepository businessDataRepository;

    public ServerLazyLoader(BusinessDataRepository bdBusinessDataRepository) {
        this.businessDataRepository = bdBusinessDataRepository;
    }
    
    public Object load(final Method method, final long persistenceId) {
        EntityGetter getter = new EntityGetter(method);
        final Map<String, Serializable> queryParameters = new HashMap<>();
        queryParameters.put(Field.PERSISTENCE_ID, persistenceId);
        if (getter.returnsList()) {
            return businessDataRepository.findListByNamedQuery(getter.getAssociatedNamedQuery(), (Class<? extends Serializable>) getter.getTargetEntityClass(), queryParameters, 0, Integer.MAX_VALUE);
        }
        try {
            return businessDataRepository.findByNamedQuery(getter.getAssociatedNamedQuery(), (Class<? extends Serializable>) getter.getTargetEntityClass(), queryParameters);
        } catch (NonUniqueResultException e) {
            // cannot appear
            throw new RuntimeException();
        }
    }

   
}
