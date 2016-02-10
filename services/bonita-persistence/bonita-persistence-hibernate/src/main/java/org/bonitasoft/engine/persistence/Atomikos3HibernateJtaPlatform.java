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

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.hibernate.service.jta.platform.internal.AbstractJtaPlatform;

/**
 * @author Charles Souillard
 */
public class Atomikos3HibernateJtaPlatform extends AbstractJtaPlatform {

    private static final long serialVersionUID = 4893085097625997082L;

    private final TransactionManager transactionManager;

    public Atomikos3HibernateJtaPlatform() throws Exception {
        final Class<?> userTransactionManagerClass = Class.forName("com.atomikos.icatch.jta.UserTransactionManager");
        this.transactionManager = (TransactionManager) userTransactionManagerClass.newInstance();
    }

    @Override
    protected TransactionManager locateTransactionManager() {
        return this.transactionManager;

    }

    @Override
    protected UserTransaction locateUserTransaction() {
        return (UserTransaction) this.transactionManager;
    }

}
