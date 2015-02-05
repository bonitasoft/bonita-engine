/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.data.impl;

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
