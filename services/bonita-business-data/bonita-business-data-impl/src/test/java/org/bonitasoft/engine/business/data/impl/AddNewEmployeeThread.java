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
package org.bonitasoft.engine.business.data.impl;

import javax.transaction.UserTransaction;

import bitronix.tm.TransactionManagerServices;

import com.company.pojo.Employee;

public class AddNewEmployeeThread extends Thread {

    private final JPABusinessDataRepositoryImpl repository;

    private final long employeeId;

    public AddNewEmployeeThread(final JPABusinessDataRepositoryImpl repository, final long employeeId) {
        this.repository = repository;
        this.employeeId = employeeId;
    }

    @Override
    public void run() {
        final UserTransaction transaction = TransactionManagerServices.getTransactionManager();
        try {
            transaction.begin();

            final Employee myEmployee = new Employee("John" + employeeId, "Doe");
            Thread.sleep(150);
            repository.persist(myEmployee);

            transaction.commit();
        } catch (final Exception e) {
            try {
                transaction.rollback();
            } catch (final Exception e1) {
                e.printStackTrace();
                throw new IllegalArgumentException(e1);
            }
            throw new IllegalArgumentException(e);
        }
    }

}
