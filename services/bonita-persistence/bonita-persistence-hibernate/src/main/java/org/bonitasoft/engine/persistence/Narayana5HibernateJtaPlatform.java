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
package org.bonitasoft.engine.persistence;

import java.lang.reflect.Method;

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.hibernate.service.jta.platform.internal.AbstractJtaPlatform;

/**
 * @author Charles Souillard
 */
public class Narayana5HibernateJtaPlatform extends AbstractJtaPlatform {

    private static final long serialVersionUID = 4893085097625997082L;

    private final TransactionManager transactionManager;

    private final Method getUserTransactionMethod;

    private final Object jtaEnvironmentBeanInstance;

    public Narayana5HibernateJtaPlatform() throws Exception {
        final Class<?> jtaPropertyManagerClass = Class.forName("com.arjuna.ats.jta.common.jtaPropertyManager");
        final Method getJTAEnvironmentBeanMethod = jtaPropertyManagerClass.getMethod("getJTAEnvironmentBean");
        jtaEnvironmentBeanInstance = getJTAEnvironmentBeanMethod.invoke((Object) null);
        final Class<?> jtaEnvironmentBeanClass = jtaEnvironmentBeanInstance.getClass();

        final Method getTransactionManagerMethod = jtaEnvironmentBeanClass.getMethod("getTransactionManager");
        transactionManager = (TransactionManager) getTransactionManagerMethod.invoke(jtaEnvironmentBeanInstance);

        getUserTransactionMethod = jtaEnvironmentBeanClass.getMethod("getUserTransaction");
    }

    @Override
    protected TransactionManager locateTransactionManager() {
        return transactionManager;

    }

    @Override
    protected UserTransaction locateUserTransaction() {
        try {
            return (UserTransaction) getUserTransactionMethod.invoke(jtaEnvironmentBeanInstance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
