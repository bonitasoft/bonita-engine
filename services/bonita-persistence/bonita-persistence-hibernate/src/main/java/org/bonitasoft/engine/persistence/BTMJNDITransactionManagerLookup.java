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

import org.hibernate.HibernateException;
import org.hibernate.transaction.JNDITransactionManagerLookup;

public class BTMJNDITransactionManagerLookup extends JNDITransactionManagerLookup {

    public BTMJNDITransactionManagerLookup() {
    }

    @Override
    public String getUserTransactionName() {
        return getInternalName();
    }

    @Override
    protected String getName() {
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
        } catch (Exception e) {
            throw new HibernateException("Could not obtain BTM UserTransactionName", e);
        }
    }

}
