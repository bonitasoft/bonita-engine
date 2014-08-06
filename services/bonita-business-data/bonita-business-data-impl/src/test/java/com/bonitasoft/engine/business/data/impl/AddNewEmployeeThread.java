package com.bonitasoft.engine.business.data.impl;

import javax.transaction.UserTransaction;

import bitronix.tm.TransactionManagerServices;

import com.bonitasoft.pojo.Employee;

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
