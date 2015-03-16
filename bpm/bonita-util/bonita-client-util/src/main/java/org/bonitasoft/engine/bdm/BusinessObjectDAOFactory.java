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
package org.bonitasoft.engine.bdm;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.bonitasoft.engine.bdm.dao.BusinessObjectDAO;
import org.bonitasoft.engine.session.APISession;

/**
 * A factory to create Data Access Objects (DAO). These DAOs interact with {@link org.bonitasoft.engine.bdm.model.BusinessObject}s.
 *
 * @author Romain Bioteau
 * @author Matthieu Chaffotte
 */
public class BusinessObjectDAOFactory {

    private static final String IMPL_SUFFIX = "Impl";

    /**
     * Creates the implementation of the DAO for the given session.
     *
     * @param session the current opened session
     * @param daoInterface the interface of the DAO
     * @return the implementation of the DAO
     * @throws BusinessObjectDaoCreationException if the factory is not able to instantiate the DAO
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
        final String daoClassName = daoInterface.getName();
        Class<T> daoImplClass = null;
        try {
            daoImplClass = loadClass(daoClassName);
        } catch (final ClassNotFoundException e) {
            throw new BusinessObjectDaoCreationException(e);
        }
        if (daoImplClass != null) {
            try {
                final Constructor<T> constructor = daoImplClass.getConstructor(APISession.class);
                return constructor.newInstance(session);
            } catch (final SecurityException e) {
                throw new BusinessObjectDaoCreationException(e);
            } catch (final NoSuchMethodException e) {
                throw new BusinessObjectDaoCreationException(e);
            } catch (final IllegalArgumentException e) {
                throw new BusinessObjectDaoCreationException(e);
            } catch (final InstantiationException e) {
                throw new BusinessObjectDaoCreationException(e);
            } catch (final IllegalAccessException e) {
                throw new BusinessObjectDaoCreationException(e);
            } catch (final InvocationTargetException e) {
                throw new BusinessObjectDaoCreationException(e);
            }
        }
        return null;
    }

    /**
     * Loads the class of the {@link BusinessObjectDAO} according to its class name.
     * <p>
     * The loading is done in the current Thread.
     *
     * @param daoClassName the name of the class of the DAO
     * @return the class of the BusinessObjectDAO
     * @throws ClassNotFoundException if the daoClassName is unknown by the current Thread
     */
    @SuppressWarnings("unchecked")
    protected <T extends BusinessObjectDAO> Class<T> loadClass(final String daoClassName) throws ClassNotFoundException {
        return (Class<T>) Class.forName(toDaoImplClassName(daoClassName), true, Thread.currentThread().getContextClassLoader());
    }

    private String toDaoImplClassName(final String daoClassName) {
        return daoClassName + IMPL_SUFFIX;
    }

}
