/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl.transaction.expression.bdm;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.bonitasoft.engine.api.impl.transaction.expression.bdm.internal.EntityGetter;
import com.bonitasoft.engine.bdm.model.field.Field;
import com.bonitasoft.engine.business.data.BusinessDataRepository;
import com.bonitasoft.engine.business.data.NonUniqueResultException;

public class ServerLazyLoader {

    
    private BusinessDataRepository businessDataRepository;

    public ServerLazyLoader(BusinessDataRepository bdBusinessDataRepository) {
        this.businessDataRepository = bdBusinessDataRepository;
    }
    
    public Object load(final Method method, final long persistenceId) {
        EntityGetter getter = new EntityGetter(method);
        final Map<String, Serializable> queryParameters = new HashMap<String, Serializable>();
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
