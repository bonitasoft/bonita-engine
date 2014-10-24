/*******************************************************************************
 * Copyright (C) 2013, 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.bonitasoft.engine.session.APISession;

import com.bonitasoft.engine.bdm.dao.BusinessObjectDAO;
import com.bonitasoft.engine.bdm.model.BusinessObject;

/**
 * A factory to create Data Access Objects (DAO). These DAOs interact with {@link BusinessObject}s.
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
