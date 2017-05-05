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
package org.bonitasoft.engine.transaction;

import java.lang.reflect.Method;

import javax.transaction.TransactionManager;

public class Narayana5TransactionManagerFactory {

    private static TransactionManager transactionManager;

    public Narayana5TransactionManagerFactory() throws Exception {
        final Class<?> jtaPropertyManagerClass = Class.forName("com.arjuna.ats.jta.common.jtaPropertyManager");
        final Method getJTAEnvironmentBeanMethod = jtaPropertyManagerClass.getMethod("getJTAEnvironmentBean");
        final Object jtaEnvironmentBeanInstance = getJTAEnvironmentBeanMethod.invoke((Object) null);
        final Class<?> jtaEnvironmentBeanClass = jtaEnvironmentBeanInstance.getClass();

        final Method getTransactionManagerMethod = jtaEnvironmentBeanClass.getMethod("getTransactionManager");
        transactionManager = (TransactionManager) getTransactionManagerMethod.invoke(jtaEnvironmentBeanInstance);

        // System.err.println("Configured transactionManager:" + transactionManager);
    }

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

}
