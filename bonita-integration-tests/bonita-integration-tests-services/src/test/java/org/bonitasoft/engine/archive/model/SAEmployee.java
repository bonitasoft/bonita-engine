package org.bonitasoft.engine.archive.model;

import org.bonitasoft.engine.persistence.PersistentObject;

public class SAEmployee extends SPersistentObjectImpl {

    private static final long serialVersionUID = 1L;

    private long employeeId;

    public SAEmployee() {
    }

    public SAEmployee(final long employeeId) {
        this.employeeId = employeeId;
    }

    public long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(final long employeeId) {
        this.employeeId = employeeId;
    }

    @Override
    public String getDiscriminator() {
        return SAEmployee.class.getName();
    }

    @Override
    public long getSourceObjectId() {
        return 0;
    }

    @Override
    public Class<? extends PersistentObject> getPersistentObjectInterface() {
        return Employee.class;
    }

}
