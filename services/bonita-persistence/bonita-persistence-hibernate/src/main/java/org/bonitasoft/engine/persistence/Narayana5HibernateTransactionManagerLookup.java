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

import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.hibernate.HibernateException;
import org.hibernate.transaction.TransactionManagerLookup;

public class Narayana5HibernateTransactionManagerLookup implements TransactionManagerLookup {

    private final String userTransactionJndiName;
    private final TransactionManager transactionManager;
    
    public Narayana5HibernateTransactionManagerLookup() throws Exception {
        final Class<?> jtaPropertyManagerClass = Class.forName("com.arjuna.ats.jta.common.jtaPropertyManager");
        final Method getJTAEnvironmentBeanMethod = jtaPropertyManagerClass.getMethod("getJTAEnvironmentBean");
        final Object jtaEnvironmentBeanInstance = getJTAEnvironmentBeanMethod.invoke((Object) null);
        final Class<?> jtaEnvironmentBeanClass = jtaEnvironmentBeanInstance.getClass();
        
        final Method getUserTransactionJNDIContextMethod = jtaEnvironmentBeanClass.getMethod("getUserTransactionJNDIContext");
        this.userTransactionJndiName = (String) getUserTransactionJNDIContextMethod.invoke(jtaEnvironmentBeanInstance);
        
        final Method getTransactionManagerMethod = jtaEnvironmentBeanClass.getMethod("getTransactionManager");
        this.transactionManager = (TransactionManager) getTransactionManagerMethod.invoke(jtaEnvironmentBeanInstance);
        
        //System.err.println("Configured userTransactionJndiName:" + userTransactionJndiName);
        //System.err.println("Configured transactionManager:" + transactionManager);
    }
    
    /**
     * {@inheritDoc}
     */
    public TransactionManager getTransactionManager(Properties props) throws HibernateException {
        return this.transactionManager;
    }

    /**
     * {@inheritDoc}
     */
    public Object getTransactionIdentifier(Transaction transaction) {
        // for sane JEE/JTA containers, the transaction itself functions as its identifier...
        return transaction;
    }

    @Override
    public String getUserTransactionName() {
        return userTransactionJndiName;
    }

}
