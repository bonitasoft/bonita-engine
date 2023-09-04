/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine;

import java.lang.reflect.Field;
import java.util.List;

import org.bonitasoft.engine.persistence.AbstractHibernatePersistenceService;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.service.ServiceAccessorSingleton;
import org.hibernate.SessionFactory;

public class SQLUtils {

    private static SessionFactory sessionFactory;

    public static List query(String query) throws Exception {
        return ServiceAccessorSingleton.getInstance().getUserTransactionService()
                .executeInTransaction(() -> getSessionFactory().getCurrentSession().createSQLQuery(query).list());
    }

    public static int execute(String query) throws Exception {
        return ServiceAccessorSingleton.getInstance().getUserTransactionService()
                .executeInTransaction(
                        () -> getSessionFactory().getCurrentSession().createSQLQuery(query).executeUpdate());
    }

    private static SessionFactory getSessionFactory() throws NoSuchFieldException, IllegalAccessException {
        if (sessionFactory == null) {
            sessionFactory = createSessionFactory();
        }
        return sessionFactory;
    }

    private static SessionFactory createSessionFactory() throws NoSuchFieldException, IllegalAccessException {
        ReadPersistenceService persistenceService = ServiceAccessorSingleton.getInstance().getReadPersistenceService();
        Field sessionFactoryField = AbstractHibernatePersistenceService.class.getDeclaredField("sessionFactory");
        sessionFactoryField.setAccessible(true);
        return (SessionFactory) sessionFactoryField.get(persistenceService);
    }

}
