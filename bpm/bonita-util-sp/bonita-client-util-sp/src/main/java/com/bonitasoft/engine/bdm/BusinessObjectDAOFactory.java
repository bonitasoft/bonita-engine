/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.bonitasoft.engine.bdm;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.bonitasoft.engine.session.APISession;

import com.bonitasoft.engine.bdm.dao.BusinessObjectDAO;

/**
 * @author Romain Bioteau
 */
public class BusinessObjectDAOFactory {

    private static final String IMPL_SUFFIX = "Impl";

    /**
     * @param apiSession
     * @param daoInterface
     * @return An implementation for the requested dao interface
     */
    public <T extends BusinessObjectDAO> T createDAO(final APISession session, final Class<T> daoInterface) throws BusinessObjectDaoCreationException {
        if (session == null) {
            throw new IllegalArgumentException("session is null");
        }
        if (daoInterface == null) {
            throw new IllegalArgumentException("daoInterface is null");
        }
        if (!daoInterface.isInterface()) {
            throw new IllegalArgumentException(daoInterface.getName() + " is not an interface");
        }
        String daoClassName = daoInterface.getName();
        Class<T> daoImplClass = null;
        try {
            daoImplClass = loadClass(daoClassName);
        } catch (ClassNotFoundException e) {
            throw new BusinessObjectDaoCreationException(e);
        }
        if (daoImplClass != null) {
            try {
                Constructor<T> constructor = daoImplClass.getConstructor(APISession.class);
                return constructor.newInstance(session);
            } catch (SecurityException e) {
                throw new BusinessObjectDaoCreationException(e);
            } catch (NoSuchMethodException e) {
                throw new BusinessObjectDaoCreationException(e);
            } catch (IllegalArgumentException e) {
                throw new BusinessObjectDaoCreationException(e);
            } catch (InstantiationException e) {
                throw new BusinessObjectDaoCreationException(e);
            } catch (IllegalAccessException e) {
                throw new BusinessObjectDaoCreationException(e);
            } catch (InvocationTargetException e) {
                throw new BusinessObjectDaoCreationException(e);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    protected <T extends BusinessObjectDAO> Class<T> loadClass(final String daoClassName) throws ClassNotFoundException {
        return (Class<T>) Class.forName(toDaoImplClassName(daoClassName), true, Thread.currentThread().getContextClassLoader());
    }

    private String toDaoImplClassName(final String daoClassName) {
        return daoClassName + IMPL_SUFFIX;
    }

}
