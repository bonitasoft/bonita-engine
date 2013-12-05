/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
import java.util.Properties;

import javax.naming.NamingException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.hibernate.HibernateException;
import org.hibernate.transaction.TransactionManagerLookup;

public class BTMJNDITransactionManagerLookup implements TransactionManagerLookup {

    public BTMJNDITransactionManagerLookup() {
    }

    @Override
    public String getUserTransactionName() {
        return getInternalName();
    }

    private String getInternalName() {
        try {
            final Class<?> transactionManagerServiceClass = Class.forName("bitronix.tm.TransactionManagerServices");
            final Method getConfigurationMethod = transactionManagerServiceClass.getMethod("getConfiguration");
            final Object configurationInstance = getConfigurationMethod.invoke((Object) null);
            final Class<?> bitronixConfigurationClass = configurationInstance.getClass();
            final Method getJndiUserTransactionNameMethod = bitronixConfigurationClass.getMethod("getJndiUserTransactionName");
            final String configuredJndiUserTransactionName = (String) getJndiUserTransactionNameMethod.invoke(configurationInstance);
            if (configuredJndiUserTransactionName != null && configuredJndiUserTransactionName.trim().length() >= 0) {
                return configuredJndiUserTransactionName;
            }
            return "java:comp/UserTransaction";
        } catch (final Exception e) {
            throw new HibernateException("Could not obtain BTM UserTransactionName", e);
        }
    }

    @Override
    public TransactionManager getTransactionManager(final Properties props) throws HibernateException {
        try {
            return (TransactionManager) NamingHelper.getInitialContext(props).lookup(getInternalName());
        } catch (final NamingException ne) {
            throw new HibernateException("Could not locate TransactionManager", ne);
        }
    }

    @Override
    public Object getTransactionIdentifier(final Transaction transaction) {
        // for sane JEE/JTA containers, the transaction itself functions as its identifier...
        return transaction;
    }

}
